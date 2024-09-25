/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.service.impl;

import io.maestro3.agent.dao.ServerConfigDao;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackSecurityGroupInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackMoveToVlanService;
import io.maestro3.agent.service.IOpenStackProjectCustomSecurityGroupService;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OpenStackMoveToVlanService implements IOpenStackMoveToVlanService {

    private static final Logger LOG = LogManager.getLogger(OpenStackMoveToVlanService.class);

    private OpenStackApiProvider apiProvider;
    private ServerConfigDao serverConfigDao;
    private IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService;

    @Autowired
    public OpenStackMoveToVlanService(OpenStackApiProvider apiProvider, ServerConfigDao serverConfigDao,
                                      IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService) {
        this.apiProvider = apiProvider;
        this.serverConfigDao = serverConfigDao;
        this.projectCustomSecurityGroupService = projectCustomSecurityGroupService;
    }

    @Override
    public void updateInstanceVLAN(OpenStackTenant openStackTenant, OpenStackRegionConfig region,
                                   OpenStackServerConfig instance, String vlanName, String targetNetworkId, String ipAddress) {

        IOpenStackApi openStackApi = apiProvider.adminOpenStack(region);
        IOpenStackApi openStackApiByProjectUser = apiProvider.openStack(openStackTenant, region);

        Port sourcePort = null;
        try {
            List<Port> ports = openStackApi.networking().ports().listByDeviceId(instance.getNativeId());
            if (CollectionUtils.isEmpty(ports)) {
                throw new ReadableAgentException(String.format("Instance %s has no ports.", instance.getNameAlias()));
            }
            sourcePort = ports.get(0);
        } catch (OSClientException e) {
            handleException("Can not get ports for instance " + instance.getNameAlias(), e.getMessage());
        }

        try {
            openStackApi.compute().portInterfaces().detach(instance.getNativeId(), sourcePort.getId());
        } catch (OSClientException e) {
            handleException("Can not detach port from instance " + instance.getNameAlias(), e.getMessage());
        }

        try {
            openStackApi.networking().ports().delete(sourcePort.getId());
        } catch (OSClientException e) {
            rollbackDetach(openStackApi, sourcePort, instance.getNativeId());
            handleException("Some errors were occurred with port deletion " + sourcePort.getId(), e.getMessage());
        }

        Port newPort = null;
        try {
            CreatePortRequest request = CreatePortRequest.build()
                    .withIpAddress(ipAddress)
                    .withMacAddress(sourcePort.getMacAddress())
                    .withNetworkId(targetNetworkId)
                    .withSecurityGroupId(openStackTenant.getSecurityGroupId())
                    .get();
            newPort = openStackApiByProjectUser.networking().ports().create(request);
        } catch (OSClientException e) {
            rollbackPortRemoving(openStackApi, sourcePort, instance);
            handleException("Can not create port for instance with id " + instance.getNameAlias(), e.getMessage());
        }

        try {
            openStackApiByProjectUser.compute().portInterfaces().attach(instance.getNativeId(), newPort.getId());
        } catch (OSClientException e) {
            rollbackPortRemoving(openStackApi, sourcePort, instance);
            handleException(String.format("Can not attach port %s for instance with id %s", newPort.getId(), instance.getNameAlias()), e.getMessage());
        }

        try {
            List<Port> ports = openStackApi.networking().ports().listByDeviceId(instance.getNativeId());
            if (CollectionUtils.isEmpty(ports)) {
                throw new ReadableAgentException("No ports found attached to mentioned server.");
            }
            Port targetPort = ports.get(0);
            OpenStackNetworkInterfaceInfo networkInterfaceInfo = new OpenStackNetworkInterfaceInfo();
            networkInterfaceInfo.setNetworkId(targetNetworkId);
            networkInterfaceInfo.setVlanName(vlanName);
            Set<OpenStackSecurityGroupInfo> securityGroups = getSecurityGroupIds(region, openStackTenant, instance);
            openStackApi.networking().ports().updateSecurityGroups(targetPort.getId(),
                    securityGroups.stream()
                            .map(OpenStackSecurityGroupInfo::getNativeId)
                            .collect(Collectors.toList()));
            networkInterfaceInfo.setSecurityGroupInfos(securityGroups);
        } catch (OSClientException e) {
            LOG.error(e.getMessage());
            throw new ReadableAgentException("Security group was not updated to project default.");
        } finally {
            instance.setLastIpOperationDate(System.currentTimeMillis());
            serverConfigDao.saveServerConfig(instance);
        }
    }

    private Set<OpenStackSecurityGroupInfo> getSecurityGroupIds(OpenStackRegionConfig region, OpenStackTenant tenant,
                                                                OpenStackServerConfig instance) {
        Set<OpenStackSecurityGroupInfo> securityGroupIds = new HashSet<>();
        if (!StringUtils.isBlank(tenant.getSecurityGroupId())) {
            securityGroupIds.add(new OpenStackSecurityGroupInfo(tenant.getSecurityGroupId(), tenant.getSecurityGroupName()));
        }

        region.getSecurityModeConfiguration(tenant.getSecurityMode())
                .ifPresent(configuration -> securityGroupIds.add(new OpenStackSecurityGroupInfo(configuration.getAdminSecurityGroupId(), null)));

        Set<OpenStackSecurityGroupInfo> customGroupIds = projectCustomSecurityGroupService.findForInstance(instance)
                .stream()
                .map(info -> new OpenStackSecurityGroupInfo(info.getOpenStackId(), info.getName()))
                .collect(Collectors.toSet());
        securityGroupIds.addAll(customGroupIds);

        return securityGroupIds;
    }

    private void rollbackDetach(IOpenStackApi openStackApi, Port port, String openStackInstanceId) {
        try {
            openStackApi.compute().portInterfaces().attach(openStackInstanceId, port.getId());
        } catch (OSClientException e) {
            handleException(String.format("Rollback detach action failed. Tried to attach server %s to an old port %s.",
                    openStackInstanceId, port.getId()), e.getMessage());
        }
    }

    private void rollbackPortRemoving(IOpenStackApi openStackApi, Port port, OpenStackServerConfig instance) {
        try {
            openStackApi.compute().portInterfaces()
                    .attach(instance.getNativeId(), port.getNetworkId(), instance.getNetworkInterfaceInfo().getPrivateIP());
        } catch (OSClientException e) {
            handleException(String.format("Rollback create removed port and attach actions failed for server %s with IP %s.",
                    instance.getNativeId(), instance.getNetworkInterfaceInfo().getPrivateIP()), e.getMessage());
        }
    }

    private void handleException(String errorMessage, String logMessage) {
        LOG.error(logMessage);
        throw new ReadableAgentException(errorMessage);
    }
}
