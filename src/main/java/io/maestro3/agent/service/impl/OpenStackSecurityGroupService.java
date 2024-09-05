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

import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.network.SecurityModeConfiguration;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class OpenStackSecurityGroupService implements IOpenStackSecurityGroupService {

    private static final Logger LOG = LogManager.getLogger(OpenStackSecurityGroupService.class);

    @Autowired
    private OpenStackApiProvider osClientProvider;
    @Autowired
    private IOpenStackTenantRepository tenantRepository;
    @Autowired
    private ServerDbService instanceService;

    @Override
    public boolean isSecurityGroupExist(OpenStackRegionConfig region, String securityGroupId) {
        IOpenStackApi api = osClientProvider.adminOpenStack(region);
        try {
            SecurityGroup securityGroup = api.networking().securityGroups().detail(securityGroupId);
            return securityGroup != null;
        } catch (OSClientException e) {
            throw new ReadableAgentException(
                    "Failed to get admin security group " + securityGroupId + " details for project. Reason: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateSecurityType(OpenStackTenant project, OpenStackRegionConfig zone, SecurityModeConfiguration newModeConfig) {
        List<String> notStates = Stream.of(ServerStateEnum.TERMINATING, ServerStateEnum.TERMINATED, ServerStateEnum.UNKNOWN, ServerStateEnum.ERROR)
                .map(ServerStateEnum::name)
                .collect(Collectors.toList());
        Collection<OpenStackServerConfig> instances = instanceService.findTenantServersNotInState(zone.getId(), project.getId(), notStates);
        if (CollectionUtils.isEmpty(instances)) {
            LOG.debug("Active instances not found for project {} zone {}", project.getTenantAlias(), zone.getRegionAlias());
            return;
        }

        IOpenStackApi api = osClientProvider.adminOpenStack(zone);
        for (OpenStackServerConfig openStackInstance : instances) {
            boolean securityGroupsChanged = updateInstanceSecurityMode(openStackInstance, zone, project, newModeConfig, api);
            if (securityGroupsChanged) {
                instanceService.saveServerConfig(openStackInstance);
            }
        }
        project.setSecurityMode(newModeConfig.getName());
        tenantRepository.save(project);
    }

    private boolean updateInstanceSecurityMode(OpenStackServerConfig instance,
                                               OpenStackRegionConfig zone,
                                               OpenStackTenant project,
                                               SecurityModeConfiguration newModeConfig,
                                               IOpenStackApi api) {
        boolean securityGroupsChanged = attachSecurityGroup(instance, api, newModeConfig);
        if (StringUtils.isNotBlank(project.getSecurityMode())) {
            Optional<SecurityModeConfiguration> previousMode = zone.getSecurityModeConfiguration(project.getSecurityMode());
            if (previousMode.isPresent()) {
                securityGroupsChanged |= detachSecurityGroup(instance, api, previousMode.get());
            }
        }
        return securityGroupsChanged;
    }

    private boolean detachSecurityGroup(OpenStackServerConfig instance, IOpenStackApi api,
                                        SecurityModeConfiguration previousMode) {
        String groupToRemove = previousMode.getAdminSecurityGroupId();
        if (!instance.getSecurityGroups().contains(groupToRemove)) {
            LOG.info("Security group type {} already detach from instance {}", previousMode.getName(), instance.getNameAlias());
            return false;
        }
        try {
            api.compute().servers().removeSecurityGroup(instance.getNativeId(), groupToRemove);
            instance.getSecurityGroups().remove(groupToRemove);
            return true;
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to detach admin security group from instance %s. Reason: %s",
                    instance.getNativeId(), e.getMessage()), e);
        }
        return false;
    }

    private boolean attachSecurityGroup(OpenStackServerConfig instance, IOpenStackApi api, SecurityModeConfiguration configuration) {
        String modeName = configuration.getName();
        String groupToAdd = configuration.getAdminSecurityGroupId();
        if (instance.getSecurityGroups().contains(groupToAdd)) {
            LOG.info("Security group type {} already attach to instance {}", modeName, instance.getNameAlias());
            return false;
        }
        try {
            api.compute().servers().addSecurityGroup(instance.getNativeId(), groupToAdd);

            instance.getSecurityGroups().add(groupToAdd);
            return true;
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to attach admin security group to instance %s. Reason: %s",
                    instance.getNativeId(), e.getMessage()), e);
        }
        return false;
    }

    @Override
    public void attachAdminSecurityGroup(OpenStackTenant project, OpenStackRegionConfig zone, OpenStackServerConfig instance) {
        IOpenStackApi api = osClientProvider.adminOpenStack(zone);
        boolean groupAttached = zone.getSecurityModeConfiguration(project.getSecurityMode())
                .map(configuration -> attachSecurityGroup(instance, api, configuration))
                .orElse(false);
        if (groupAttached) {
            instanceService.saveServerConfig(instance);
        }
    }

    @Override
    public void changeSecurityGroupAfterInstanceMovedToAnotherProject(OpenStackServerConfig instance,
                                                                      OpenStackTenant project,
                                                                      OpenStackRegionConfig zone) {
        if (StringUtils.isBlank(project.getSecurityMode())) {
            return;
        }
        IOpenStackApi api = osClientProvider.adminOpenStack(zone);
        zone.getSecurityModeConfiguration(project.getSecurityMode())
                .ifPresentOrElse(configuration -> attachSecurityGroup(instance, api, configuration),
                        () -> LOG.warn("{} admin security group not configured for {} zone", project.getSecurityMode(), zone.getRegionAlias()));
    }
}
