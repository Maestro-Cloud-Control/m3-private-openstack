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
import io.maestro3.agent.model.network.NetworkType;
import io.maestro3.agent.model.network.StartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.OpenStackProjectCustomSecurityGroup;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackInstanceProvisioningValidationService;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IOpenStackProjectCustomSecurityGroupService;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.request.instance.RunInstanceRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

class OpenStackInstanceProvisioningValidationService implements IOpenStackInstanceProvisioningValidationService {

    private static final Logger LOG = LogManager.getLogger(OpenStackInstanceProvisioningValidationService.class);

    private final IOpenStackClientProvider clientProvider;
    private final IOpenStackNetworkingProvider networking;
    private final IOpenStackVLANService vlanService;
    private final OpenStackRegionConfig zone;
    private final IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService;

    public OpenStackInstanceProvisioningValidationService(OpenStackRegionConfig zone,
                                                          IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                                          IOpenStackStaticIpService staticIpService, IOpenStackTenantRepository projectService, IOpenStackVLANService vlanService,
                                                          IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService,
                                                          IOpenStackSecurityGroupService openStackSecurityGroupService,
                                                          boolean floatingIpPreferable) {
        this.clientProvider = clientProvider;
        this.projectCustomSecurityGroupService = projectCustomSecurityGroupService;
        this.vlanService = vlanService;
        this.zone = zone;
        networking = NetworkProviders.networking(zone, clientProvider, instanceService, staticIpService,
            projectService, vlanService, openStackSecurityGroupService, floatingIpPreferable);
    }

    @Override
    public StartupNetworkingConfiguration getNetworkConfiguration(OpenStackTenant tenant,
                                                                  RunInstanceRequest parameters) {
        validateNetworks(tenant, parameters);
        return networking.getNetworkConfiguration(tenant, parameters.getNetworks(), parameters.getIp());
    }

    @Override
    public Set<String> validateSecurityGroups(OpenStackTenant project, RunInstanceRequest parameters) throws OSClientException {
        Set<String> securityGroups = parameters.getSecurityGroups() == null ? new HashSet<>() : new HashSet<>(parameters.getSecurityGroups());
        Set<String> networks = parameters.getNetworks() == null ? new HashSet<>() : new HashSet<>(parameters.getNetworks());

        boolean defaultNetworksSpecified = networks.stream()
            .anyMatch(networkId -> StringUtils.isEqualsIgnoreCase(project.getNetworkId(), networkId));

        if (CollectionUtils.isNotEmpty(networks) && !defaultNetworksSpecified && isSecurityGroupDisabled(project, networks)) {
            return Collections.emptySet();
        }

        IOpenStackClient client = clientProvider.getClient(zone, project);
        Set<String> securityGroupsForLaunch = new HashSet<>();

        if (isEmpty(securityGroups)) {
            String defaultSecurityGroupId = networking.getDefaultSecurityGroupId(project);
            if (StringUtils.isBlank(defaultSecurityGroupId)) {
                assertDefaultSecurityGroupIsRequired(project);
            } else {
                securityGroupsForLaunch.add(defaultSecurityGroupId);
            }

            Set<String> customGroupIds = projectCustomSecurityGroupService.findForAllInstances(project.getId())
                .stream()
                .map(OpenStackProjectCustomSecurityGroup::getOpenStackId)
                .collect(Collectors.toSet());
            securityGroupsForLaunch.addAll(customGroupIds);
        } else {
            List<SecurityGroup> realGroups = client.listSecurityGroups();
            ensureSecurityGroup(realGroups, securityGroups, project.getTenantAlias());
            securityGroupsForLaunch.addAll(securityGroups);
        }

        return securityGroupsForLaunch;
    }

    private void validateNetworks(OpenStackTenant project, RunInstanceRequest parameters) {
        Set<String> networks = parameters.getNetworks() == null ? new HashSet<>() : new HashSet<>(parameters.getNetworks());
        if (CollectionUtils.isEmpty(networks) || project.getNetworkType() != NetworkType.SECURED) {
            return;
        }

        for (String network : networks) {
            if (StringUtils.isBlank(network)) {
                continue;
            }

            if (!Objects.equals(project.getNetworkId(), network)) {
                throw new ReadableAgentException(
                    String.format("Instance in secured project %s should be run in default network %s, actual %s.",
                        project.getTenantAlias(), project.getNetworkId(), network));
            }

            List<OpenStackVLAN> vlans = vlanService.findByOpenStackNetworkId(project.getRegionId(), project.getId(), network);
            for (OpenStackVLAN vlan : vlans) {
                if (!vlan.isSdn()) {
                    throw new ReadableAgentException(
                        String.format("Default network %s of secured project %s should be in SDN.",
                            project.getNetworkId(), project.getTenantAlias()));
                }
            }
        }
    }

    private boolean isSecurityGroupDisabled(OpenStackTenant project, Set<String> networks) {
        return networks.stream().map(netId -> vlanService.findByOpenStackNetworkId(zone.getId(), project.getId(), netId))
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .anyMatch(OpenStackVLAN::isSecurityGroupDisabled);
    }


    private void ensureSecurityGroup(Iterable<SecurityGroup> realSecurityGroups, Iterable<String> securityGroups, String pmcCode) {
        Assert.notNull(realSecurityGroups, "No real groups, security groups can't be applied. First create at least one SG.");
        Assert.notNull(securityGroups, "No security groups provided.");
        for (String securityGroup : securityGroups) {
            if (securityGroup == null) {
                LOG.warn("Null was found while processing inputted groups. Check it");
                continue;
            }
            for (SecurityGroup realSecurityGroup : realSecurityGroups) {
                if (realSecurityGroup == null) {
                    LOG.warn(String.format("Null was found while processing real groups for %s in %s. Check it", pmcCode, zone.getRegionAlias()));
                    continue;
                }
                if (securityGroup.equalsIgnoreCase(realSecurityGroup.getId()) || securityGroup.equalsIgnoreCase(realSecurityGroup.getName())) {
                    continue;
                }
                throw new ReadableAgentException(String.format("Security group %s can not be applied for %s in %s", securityGroup, pmcCode, zone.getRegionAlias()));
            }
        }
    }

    private void assertDefaultSecurityGroupIsRequired(OpenStackTenant project) {
        if (networking.isDefaultSecurityGroupRequired()) {
            throw new ReadableAgentException("Default security group was removed at OpenStack for " + project.getTenantAlias() + " in " + zone.getRegionAlias());
        }
    }
}
