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

import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.network.Direction;
import io.maestro3.agent.model.network.SecurityGroupType;
import io.maestro3.agent.model.network.impl.ProjectSource;
import io.maestro3.agent.model.network.impl.SecurityGroupExtension;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackSecurityGroupInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroupRule;
import io.maestro3.agent.openstack.api.networking.request.CreateSecurityGroup;
import io.maestro3.agent.openstack.api.networking.request.CreateSecurityGroupRule;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackCustomSecurityConfigService;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.IStaticIpService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.IpProtocol;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class OpenStackSecurityGroupService implements IOpenStackSecurityGroupService {

    private static final Logger LOG = LogManager.getLogger(OpenStackSecurityGroupService.class);

    private static final List<String> ANY_PROTOCOL_VALUES = Arrays.asList("any", "0");
    private static final List<Integer> SECURE_CONNECTION_PORTS = Arrays.asList(22, 443, 3389, 5986, 5985);
    private static final String IP_V4 = "IPv4";
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String ALL_PROTOCOLS_ALL_PORTS = "-1";
    private static final String ALL_IP_RANGE = "0.0.0.0/0";
    private static final String ALLOW_ALL_RULE_DESCRIPTION = "Allow all";
    private static final String OPEN_STACK_METADATA_RULE_DESCRIPTION = "Allow OpenStack metadata";
    private static final String SECURE_PROTOCOL_RULE_DESCRIPTION = "Allow traffic on secure connection ports";
    private static final String PROJECT_SOURCES_RULE_DESCRIPTION = "Allow project sources";
    private static final String ORCHESTRATOR_IP_RULE_DESCRIPTION = "Allow orchestrator IP";

    @Autowired
    private OpenStackApiProvider osClientProvider;
    @Autowired
    private IOpenStackTenantRepository tenantRepository;
    @Autowired
    private IOpenStackRegionRepository regionRepository;
    @Autowired
    private ServerDbService instanceService;
    @Autowired
    private IOpenStackVLANService vlanService;
    @Autowired
    private IStaticIpService staticIpService;
    @Autowired
    private IOpenStackCustomSecurityConfigService securityConfigService;

    @Value("${openstack.metadata.service:169.254.169.254}")
    private String openStackMetadataServiceIP;

    @Override
    public SecurityGroup setupProjectSecurityGroup(OpenStackRegionConfig zone, OpenStackTenant project, boolean personalTenant) {
        IOpenStackApi api = osClientProvider.adminOpenStack(zone);

        String projectName = project.getTenantAlias();
        String securityGroupName = String.join("-", projectName.toLowerCase(), "default");
        String description = String.format("Default security group for project %s", projectName.toLowerCase());

        // create empty SG
        SecurityGroup securityGroup;
        String projectExternalId = project.getNativeId();
        try {
            securityGroup = createEmptySecurityGroup(project.getTenantAlias(), projectExternalId, api, securityGroupName, description);
        } catch (Exception e) {
            throw new M3PrivateAgentException(e.getMessage(), e);
        }

        String securityGroupId = securityGroup.getId();
        try {
            if (personalTenant) {
                setupPersonalTenantRules(projectExternalId, securityGroupId, api);
            } else {
                setupLookBackRules(projectExternalId, securityGroupId, api);
                CreateSecurityGroupRule openStackMetadataRule = getOpenStackMetadataRule(projectExternalId, securityGroupId);
                createSecurityGroupRule(openStackMetadataRule, api);
            }
        } catch (Exception e) {
            rollbackSecurityGroup(securityGroup, api);
            throw e;
        }
        project.setSecurityGroupId(securityGroupId);
        project.setSecurityGroupName(securityGroupName);
        return securityGroup;
    }

    @Override
    public void createOrUpdateAdminSecurityGroup(OpenStackRegionConfig zone, SecurityGroupType securityGroupType) {
        IOpenStackApi api = osClientProvider.adminOpenStack(zone);

        OpenStackSecurityGroupInfo adminSecurityGroupInfo = zone.getAdminSecurityGroupId(securityGroupType);
        SecurityGroup createdSecurityGroup = null;
        String adminSecurityGroupId = null;
        if (adminSecurityGroupInfo == null || StringUtils.isBlank(adminSecurityGroupInfo.getNativeId())) {
            String securityGroupName = securityGroupType.name().toLowerCase() + "-group";
            String description = String.format("Security group for mode %s", securityGroupType);

            // create empty SG
            createdSecurityGroup = createEmptySecurityGroup(zone.getAdminProjectMeta().getProjectName(), zone.getAdminProjectMeta().getProjectId(), api, securityGroupName, description);
            adminSecurityGroupId = createdSecurityGroup.getId();
        }

        try {
            setupAdminSecurityGroupRules(zone, zone.getAdminProjectMeta().getProjectId(), adminSecurityGroupId, api, securityGroupType);
        } catch (Exception e) {
            rollbackSecurityGroup(createdSecurityGroup, api);
            throw e;
        }

        if (createdSecurityGroup != null) {
            zone.addAdminSecurityGroup(securityGroupType,
                new OpenStackSecurityGroupInfo(createdSecurityGroup.getId(), createdSecurityGroup.getName()));
            regionRepository.save(zone);
        }
    }

    private SecurityGroup createEmptySecurityGroup(OpenStackTenant project, IOpenStackApi api, String name, String description) {
        return createEmptySecurityGroup(project.getTenantAlias(), project.getNativeId(), api,
            name, description);
    }


    @Override
    public SecurityGroup createEmptySecurityGroup(String openStackTenantName, String openStackTenantId, IOpenStackApi api,
                                                  String sgName, String description) {
        CreateSecurityGroup defaultSecurityGroupConfig = CreateSecurityGroup.securityGroup()
            .withName(sgName)
            .withDescription(description)
            .forProject(openStackTenantId)
            .get();

        SecurityGroup securityGroup;
        try {
            securityGroup = api.networking().securityGroups().create(defaultSecurityGroupConfig);
        } catch (OSClientException e) {
            throw new ReadableAgentException(
                "Failed to create security group " + sgName + " for project " + openStackTenantName + ". Reason: " + e.getMessage(), e);
        }

        try {
            cleanUpRules(securityGroup, api);
        } catch (Exception e) {
            rollbackSecurityGroup(securityGroup, api);
            throw e;
        }

        return securityGroup;
    }

    private void cleanUpRules(SecurityGroup securityGroup, IOpenStackApi api) {
        try {
            List<SecurityGroupRule> rules = securityGroup.getRules();
            if (CollectionUtils.isEmpty(rules)) {
                return;
            }

            for (SecurityGroupRule rule : rules) {
                api.networking().securityGroupRules().delete(rule.getId());
            }
        } catch (OSClientException e) {
            throw new ReadableAgentException(String.format("Failed to clean up %s security group rules. Reason: %s",
                securityGroup.getName(), e.getMessage()), e);
        }
    }

    private void createSecurityGroupRulesQuietly(Collection<CreateSecurityGroupRule> rules, IOpenStackApi api) {
        for (CreateSecurityGroupRule rule : rules) {
            try {
                createSecurityGroupRule(rule, api);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private void createSecurityGroupRule(CreateSecurityGroupRule rule, IOpenStackApi api) {
        try {
            api.networking().securityGroupRules().create(rule);
        } catch (OSClientException e) {
            throw new ReadableAgentException(
                "Failed to create security group rule. Reason: " + e.getMessage(), e);
        }
    }

    private void rollbackSecurityGroup(SecurityGroup securityGroup, IOpenStackApi api) {
        if (securityGroup == null) {
            return;
        }

        try {
            api.networking().securityGroups().delete(securityGroup.getId());
        } catch (Exception e) {
            LOG.debug(e);
        }
    }

    private void setupAdminSecurityGroupRules(OpenStackRegionConfig zone,
                                              String projectId,
                                              String securityGroupId,
                                              IOpenStackApi api,
                                              SecurityGroupType securityGroupType) {
        // get admin security group
        SecurityGroup adminSecurityGroup;
        try {
            adminSecurityGroup = api.networking().securityGroups().detail(securityGroupId);
        } catch (OSClientException e) {
            throw new ReadableAgentException(String.format("Failed to get %s security group details, zone %s. %s",
                securityGroupType, zone.getRegionAlias(), e.getMessage()));
        }

        // get desired rules
        Collection<CreateSecurityGroupRule> ingressDefaultRules = initIngressDefaultRules(zone, securityGroupId, projectId, securityGroupType);
        Collection<CreateSecurityGroupRule> egressDefaultRules = initEgressDefaultRules(zone, securityGroupId, projectId, securityGroupType);
        Collection<CreateSecurityGroupRule> defaultRules = Stream.of(ingressDefaultRules, egressDefaultRules)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        // check admin security group consistency
        checkRulesConsistency(adminSecurityGroup, defaultRules, api);
    }

    @Override
    public void updateSecurityType(OpenStackTenant project, OpenStackRegionConfig zone, Set<SecurityGroupType> newSecurityGroupTypes) {
        List<SecurityGroupType> types = getSecurityGroupTypeWithNullableId(zone, newSecurityGroupTypes);
        if (CollectionUtils.isNotEmpty(types)) {
            throw new ReadableAgentException(String.format("Security mode %s not configured for zone %s", types.get(0), zone.getRegionAlias()));
        }

        List<String> notStates = Arrays.asList(ServerStateEnum.TERMINATING, ServerStateEnum.TERMINATED, ServerStateEnum.UNKNOWN, ServerStateEnum.ERROR).stream()
            .map(ServerStateEnum::name)
            .collect(Collectors.toList());
        Collection<OpenStackServerConfig> instances = instanceService.findTenantServersNotInState(zone.getId(), project.getId(), notStates);
        if (CollectionUtils.isEmpty(instances)) {
            LOG.debug(String.format("Active instances not found for project %s zone %s", project.getTenantAlias(), zone.getRegionAlias()));
            return;
        }

        IOpenStackApi api = osClientProvider.adminOpenStack(zone);
        for (OpenStackServerConfig openStackInstance : instances) {
            boolean securityGroupsChanged = updateInstanceSecurityMode(openStackInstance, zone, project, project.getSecurityGroupTypes(), newSecurityGroupTypes, api);
            if (securityGroupsChanged) {
                instanceService.saveServerConfig(openStackInstance);
            }
        }
    }

    private boolean updateInstanceSecurityMode(OpenStackServerConfig instance,
                                               OpenStackRegionConfig zone,
                                               OpenStackTenant project,
                                               Set<SecurityGroupType> previousTypes,
                                               Set<SecurityGroupType> newTypes,
                                               IOpenStackApi api) {
        boolean securityGroupsChanged = false;
        for (SecurityGroupType newType : newTypes) {
            if (previousTypes.contains(newType)) {
                continue;
            }

            OpenStackSecurityGroupInfo adminSecurityGroupInfo = zone.getAdminSecurityGroupId(newType);
            if (adminSecurityGroupInfo != null && StringUtils.isNotBlank(adminSecurityGroupInfo.getNativeId())) {
                securityGroupsChanged |= attachSecurityGroup(instance, zone, project, api, newType, adminSecurityGroupInfo.getNativeId());
            } else {
                LOG.warn(String.format("%s admin security group not configured for %s zone", newType, zone.getRegionAlias()));
            }
        }

        for (SecurityGroupType previousType : previousTypes) {
            if (newTypes.contains(previousType)) {
                continue;
            }

            OpenStackSecurityGroupInfo prevGroupInfo = zone.getAdminSecurityGroupId(previousType);
            if (prevGroupInfo != null && StringUtils.isNotBlank(prevGroupInfo.getNativeId())) {
                securityGroupsChanged |= detachSecurityGroup(instance, zone, project, api, previousType, prevGroupInfo.getNativeId());
            } else {
                LOG.warn(String.format("%s admin security group not configured for %s zone", previousType, zone.getRegionAlias()));
            }
        }

        return securityGroupsChanged;
    }

    private boolean detachSecurityGroup(OpenStackServerConfig instance, OpenStackRegionConfig zone,
                                        OpenStackTenant project, IOpenStackApi api, SecurityGroupType previousType,
                                        String groupToRemove) {
        if (!instance.getSecurityGroups().contains(groupToRemove)) {
            LOG.info(String.format("Security group type %s already detach from instance %s", previousType.name().toLowerCase(), instance.getNameAlias()));
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

    private boolean attachSecurityGroup(OpenStackServerConfig instance, OpenStackRegionConfig zone,
                                        OpenStackTenant project, IOpenStackApi api, SecurityGroupType newType,
                                        String groupToAdd) {
        if (instance.getSecurityGroups().contains(groupToAdd)) {
            LOG.info(String.format("Security group type %s already attach to instance %s", newType.name().toLowerCase(), instance.getNameAlias()));
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

    private void attachSecurityGroupSilently(OpenStackServerConfig instance, IOpenStackApi api, String groupToAdd) {
        if (StringUtils.isBlank(groupToAdd)) {
            return;
        }

        try {
            api.compute().servers().addSecurityGroup(instance.getNativeId(), groupToAdd);
            instance.getSecurityGroups().add(groupToAdd);
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to attach security group %s to instance %s. Reason: %s",
                groupToAdd, instance.getNativeId(), e.getMessage()), e);
        }
    }

    @Override
    public void attachAdminSecurityGroup(OpenStackTenant project, OpenStackRegionConfig zone, String instanceId) {
        Set<SecurityGroupType> securityGroupTypes = project.getSecurityGroupTypes();

        OpenStackServerConfig instance = instanceService.findServerByNativeId(zone.getId(), project.getId(), instanceId);
        if (instance == null) {
            LOG.warn(String.format("Instance %s not found in project %s, zone %s", instanceId, project.getTenantAlias(), zone.getRegionAlias()));
            return;
        }

        IOpenStackApi api = osClientProvider.adminOpenStack(zone);
        boolean groupAttached = false;
        for (SecurityGroupType securityGroupType : securityGroupTypes) {
            OpenStackSecurityGroupInfo securityGroupInfo = zone.getAdminSecurityGroupId(securityGroupType);
            if (securityGroupInfo != null && StringUtils.isNotBlank(securityGroupInfo.getNativeId())) {
                groupAttached |= attachSecurityGroup(instance, zone, project, api, securityGroupType, securityGroupInfo.getNativeId());
            } else {
                LOG.warn(String.format("Admin security group not initialize for mode %s, zone %s", securityGroupType, zone.getRegionAlias()));
            }
        }
        if (groupAttached) {
            instanceService.saveServerConfig(instance);
        }
    }

    @Override
    public void changeSecurityGroupAfterInstanceMovedToAnotherProject(OpenStackServerConfig instance,
                                                                      OpenStackTenant project,
                                                                      OpenStackRegionConfig zone) {
        IOpenStackApi api = osClientProvider.adminOpenStack(zone);

        // attach admin sg
        Set<SecurityGroupType> currentSecurityModes = project.getSecurityGroupTypes();
        for (SecurityGroupType currentSecurityMode : currentSecurityModes) {
            OpenStackSecurityGroupInfo securityGroupInfo = zone.getAdminSecurityGroupId(currentSecurityMode);
            if (securityGroupInfo != null && StringUtils.isNotBlank(securityGroupInfo.getNativeId())) {
                attachSecurityGroup(instance, zone, project, api, currentSecurityMode, securityGroupInfo.getNativeId());
            } else {
                LOG.warn(String.format("%s admin security group not configured for %s zone", currentSecurityMode, zone.getRegionAlias()));
            }
        }

    }

    @Override
    public void updateProjectDefaultSecurityGroup(OpenStackTenant project, Collection<ProjectSource> sourcesToAdd, Collection<String> sourcesToRemove) {
        if (project.getSecurityGroupTypes().contains(SecurityGroupType.PRIVATE) || CollectionUtils.isEmpty(sourcesToAdd) && CollectionUtils.isEmpty(sourcesToRemove)) {
            return;
        }

        OpenStackRegionConfig zone = regionRepository.findByIdInCloud(project.getRegionId());
        if (zone == null) {
            return;
        }

        IOpenStackApi api = osClientProvider.openStack(project, zone);

        removeProjectSources(zone, project, sourcesToRemove, api);
        addProjectSources(project, sourcesToAdd, api);
    }

    private void removeProjectSources(OpenStackRegionConfig zone, OpenStackTenant project, Collection<String> sourcesToRemove, IOpenStackApi api) {
        if (CollectionUtils.isEmpty(sourcesToRemove)) {
            return;
        }

        SecurityGroup projectSecurityGroup;
        try {
            projectSecurityGroup = getDefaultProjectSecurityGroup(project, zone, api);
        } catch (Exception e) {
            LOG.error(String.format("Failed to get project %s default security group. Reason: %s", project.getTenantAlias(), e.getMessage()));
            return;
        }

        List<SecurityGroupRule> rules = projectSecurityGroup.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        for (SecurityGroupRule rule : rules) {
            String remoteIpPrefix = rule.getRemoteIpPrefix();
            if (StringUtils.isBlank(remoteIpPrefix)) {
                continue;
            }

            boolean needsRemoval = sourcesToRemove.stream()
                .map(this::normalizeIp)
                .anyMatch(remoteIpPrefix::equalsIgnoreCase);
            if (needsRemoval) {
                deleteSecurityGroupRuleSilently(api, rule);
            }
        }
    }

    private void addProjectSources(OpenStackTenant project, Collection<ProjectSource> sourcesToAdd, IOpenStackApi api) {
        if (CollectionUtils.isEmpty(sourcesToAdd)) {
            return;
        }

        Collection<Direction> directions = getDirections(project);
        if (CollectionUtils.isEmpty(directions)) {
            return;
        }

        Collection<CreateSecurityGroupRule> rules = new ArrayList<>();
        for (ProjectSource source : sourcesToAdd) {
            for (Direction direction : directions) {
                CreateSecurityGroupRule securityGroupRule = getSecurityGroupRule(project.getNativeId(),
                    project.getSecurityGroupId(), direction, normalizeIp(source.getAddress()), source.getDescription());
                rules.add(securityGroupRule);
            }
        }

        createSecurityGroupRulesQuietly(rules, api);
    }

    private void checkRulesConsistency(SecurityGroup projectSecurityGroup, Collection<CreateSecurityGroupRule> desiredRules, IOpenStackApi api) {
        List<SecurityGroupRule> rules = projectSecurityGroup.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            createSecurityGroupRulesQuietly(desiredRules, api);
            return;
        }

        Iterator<SecurityGroupRule> securityGroupRuleIterator = rules.iterator();
        while (securityGroupRuleIterator.hasNext()) {
            SecurityGroupRule securityGroupRule = securityGroupRuleIterator.next();
            boolean isDesireRule = desiredRules.removeIf(desiredRule -> isDesireRule(securityGroupRule, desiredRule));
            if (isDesireRule) {
                securityGroupRuleIterator.remove();
            }
        }

        // removing extra rules
        for (SecurityGroupRule rule : rules) {
            deleteSecurityGroupRuleSilently(api, rule);
        }

        // add missing rules
        createSecurityGroupRulesQuietly(desiredRules, api);
    }

    private void deleteSecurityGroupRuleSilently(IOpenStackApi api, SecurityGroupRule rule) {
        try {
            api.networking().securityGroupRules().delete(rule.getId());
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to delete security group rule %s. Reason: %s", rule.getId(), e.getMessage()));
        }
    }

    private boolean isDesireRule(SecurityGroupRule securityGroupRule, CreateSecurityGroupRule desiredRule) {
        if (!StringUtils.equalsIgnoreCase(securityGroupRule.getDirection(), desiredRule.getDirection())) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(securityGroupRule.getRemoteIpPrefix(), desiredRule.getRemoteIpPrefix())) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(securityGroupRule.getRemoteGroupId(), desiredRule.getRemoteGroupId())) {
            return false;
        }
        if (!haveSameProtocol(securityGroupRule, desiredRule)) {
            return false;
        }

        return haveSamePortRange(securityGroupRule, desiredRule);
    }

    private boolean haveSameProtocol(SecurityGroupRule securityGroupRule, CreateSecurityGroupRule desiredRule) {
        String openStackRuleProtocol = Optional.ofNullable(securityGroupRule.getProtocol()).orElse(ANY_PROTOCOL_VALUES.get(0));
        String desiredRuleProtocol = Optional.ofNullable(desiredRule.getProtocol()).orElse(ANY_PROTOCOL_VALUES.get(0));
        if (StringUtils.equalsIgnoreCase(openStackRuleProtocol, desiredRuleProtocol)) {
            return true;
        }
        if (ANY_PROTOCOL_VALUES.contains(openStackRuleProtocol) && ANY_PROTOCOL_VALUES.contains(desiredRuleProtocol)) {
            return true;
        }

        IpProtocol openStackIpProtocol = resolveIpProtocol(openStackRuleProtocol);
        IpProtocol desiredIpProtocol = resolveIpProtocol(desiredRuleProtocol);
        return openStackIpProtocol != null && desiredIpProtocol != null && openStackIpProtocol.equals(desiredIpProtocol);
    }

    private IpProtocol resolveIpProtocol(String protocol) {
        if (StringUtils.isBlank(protocol)) {
            return null;
        }

        IpProtocol ipProtocol = IpProtocol.fromName(protocol);
        if (ipProtocol != null) {
            return ipProtocol;
        }

        return IpProtocol.fromNumber(protocol);
    }

    private boolean haveSamePortRange(SecurityGroupRule securityGroupRule, CreateSecurityGroupRule desiredRule) {
        return equalPorts(securityGroupRule.getPortRangeMin(), desiredRule.getPortRangeMin(), MIN_PORT)
            && equalPorts(securityGroupRule.getPortRangeMax(), desiredRule.getPortRangeMax(), MAX_PORT);
    }

    private boolean equalPorts(Integer openStackPort, Integer desiredPort, Integer defaultValue) {
        return Optional.ofNullable(openStackPort).orElse(defaultValue).equals(Optional.ofNullable(desiredPort).orElse(defaultValue));
    }

    private SecurityGroup getDefaultProjectSecurityGroup(OpenStackTenant project, OpenStackRegionConfig zone, IOpenStackApi api) {
        String defaultSecurityGroupId = project.getSecurityGroupId();
        if (StringUtils.isBlank(defaultSecurityGroupId)) {
            throw new ReadableAgentException(String.format("Got project without default security group. Project: %s, zone: %s",
                project.getTenantAlias(), zone.getRegionAlias()));
        }

        try {
            return api.networking().securityGroups().detail(defaultSecurityGroupId);
        } catch (OSClientException e) {
            throw new ReadableAgentException("Failed to describe security group details. Reason: " + e.getMessage(), e);
        }
    }

    private Collection<CreateSecurityGroupRule> initIngressDefaultRules(OpenStackRegionConfig zone,
                                                                        String securityGroupId,
                                                                        String projectId,
                                                                        SecurityGroupType securityGroupType) {
        List<CreateSecurityGroupRule> result = new ArrayList<>();
        Collection<CreateSecurityGroupRule> zoneExtensions = getZoneExtensionRules(zone, securityGroupId, projectId, securityGroupType, Direction.INGRESS);
        if (CollectionUtils.isNotEmpty(zoneExtensions)) {
            result.addAll(zoneExtensions);
        }

        switch (securityGroupType) {
            case PUBLIC:
                CreateSecurityGroupRule allowAllRule = getSecurityGroupRule(projectId, securityGroupId, Direction.INGRESS,
                    ALL_IP_RANGE, ALLOW_ALL_RULE_DESCRIPTION);
                result.add(allowAllRule);
                break;
            case LIMITED:
                List<CreateSecurityGroupRule> secureConnectionProtocolRules =
                    getSecureConnectionProtocolRules(projectId, securityGroupId);
                result.addAll(secureConnectionProtocolRules);
                break;
            default:
                break;
        }

        return result;
    }

    private String normalizeIp(String ip) {
        if (StringUtils.isNotBlank(ip) && !ip.endsWith("/32")) {
            return ip + "/32";
        }

        return ip;
    }

    private Collection<CreateSecurityGroupRule> getZoneExtensionRules(OpenStackRegionConfig zone,
                                                                      String securityGroupId,
                                                                      String projectId,
                                                                      SecurityGroupType securityGroupType,
                                                                      Direction direction) {
        List<SecurityGroupExtension> extensions = securityConfigService.findByZoneIds(Collections.singleton(zone.getId()), securityGroupType, direction);
        if (CollectionUtils.isEmpty(extensions)) {
            return Collections.emptyList();
        }

        return extensions.stream()
            .map(extension -> convert(projectId, securityGroupId, direction, extension))
            .collect(Collectors.toList());
    }

    private Collection<CreateSecurityGroupRule> initEgressDefaultRules(OpenStackRegionConfig zone,
                                                                       String securityGroupId,
                                                                       String projectId,
                                                                       SecurityGroupType securityGroupType) {
        List<CreateSecurityGroupRule> result = new ArrayList<>();
        Collection<CreateSecurityGroupRule> zoneExtensions = getZoneExtensionRules(zone, securityGroupId, projectId, securityGroupType, Direction.EGRESS);
        if (CollectionUtils.isNotEmpty(zoneExtensions)) {
            result.addAll(zoneExtensions);
        }

        CreateSecurityGroupRule allowAllRule = getSecurityGroupRule(projectId, securityGroupId, Direction.EGRESS,
            ALL_IP_RANGE, ALLOW_ALL_RULE_DESCRIPTION);
        switch (securityGroupType) {
            case PUBLIC:
                result.add(allowAllRule);
                break;
            case LIMITED:
                if (CollectionUtils.isEmpty(zoneExtensions)) {
                    result.add(allowAllRule);
                    break;
                } //else - fall through
            case PROTECTED:
            case PRIVATE:
                CreateSecurityGroupRule openStackMetadata = getOpenStackMetadataRule(projectId, securityGroupId);
                result.add(openStackMetadata);
                break;
            default:
                break;
        }

        return result;
    }

    private void setupLookBackRules(String projectId, String securityGroupId, IOpenStackApi api) {
        Collection<CreateSecurityGroupRule> lookBackRules = getLookBackRules(projectId, securityGroupId);
        for (CreateSecurityGroupRule lookBackRule : lookBackRules) {
            createSecurityGroupRule(lookBackRule, api);
        }
    }

    private Collection<CreateSecurityGroupRule> getLookBackRules(String projectId, String securityGroupId) {
        return Stream.of(Direction.values())
            .map(direction -> CreateSecurityGroupRule.rule()
                .project(projectId)
                .securityGroup(securityGroupId)
                .etherType(IP_V4)
                .remoteGroup(securityGroupId)
                .description(PROJECT_SOURCES_RULE_DESCRIPTION)
                .direction(direction.getInLowerCase())
                .get())
            .collect(Collectors.toList());
    }

    private void setupPersonalTenantRules(String projectId, String securityGroupId, IOpenStackApi api) {
        CreateSecurityGroupRule egressRule = getSecurityGroupRule(projectId, securityGroupId, Direction.EGRESS,
            ALL_IP_RANGE, ALLOW_ALL_RULE_DESCRIPTION);
        createSecurityGroupRule(egressRule, api);

        CreateSecurityGroupRule ingressRule = getSecurityGroupRule(projectId, securityGroupId, Direction.INGRESS,
            ALL_IP_RANGE, ALLOW_ALL_RULE_DESCRIPTION);
        createSecurityGroupRule(ingressRule, api);
    }

    private CreateSecurityGroupRule getOpenStackMetadataRule(String projectId,
                                                             String securityGroupId) {
        return CreateSecurityGroupRule.rule()
            .direction(Direction.EGRESS.getInLowerCase())
            .securityGroup(securityGroupId)
            .project(projectId)
            .protocol(IpProtocol.TCP.name())
            .etherType(IP_V4)
            .remoteIpPrefix(normalizeIp(openStackMetadataServiceIP))
            .portRangeMin(80)
            .portRangeMax(80)
            .description(OPEN_STACK_METADATA_RULE_DESCRIPTION)
            .get();
    }

    private CreateSecurityGroupRule getSecurityGroupRule(String projectId,
                                                         String securityGroupId,
                                                         Direction direction,
                                                         String ipPrefix,
                                                         String description) {
        return CreateSecurityGroupRule.rule()
            .direction(direction.getInLowerCase())
            .securityGroup(securityGroupId)
            .project(projectId)
            .etherType(IP_V4)
            .remoteIpPrefix(ipPrefix)
            .description(description)
            .get();
    }

    private List<CreateSecurityGroupRule> getSecureConnectionProtocolRules(String projectId,
                                                                           String securityGroupId) {
        List<CreateSecurityGroupRule> result = new ArrayList<>();

        for (Integer port : SECURE_CONNECTION_PORTS) {
            CreateSecurityGroupRule rule = CreateSecurityGroupRule.rule()
                .direction(Direction.INGRESS.getInLowerCase())
                .securityGroup(securityGroupId)
                .project(projectId)
                .protocol(IpProtocol.TCP.name())
                .etherType(IP_V4)
                .remoteIpPrefix(ALL_IP_RANGE)
                .portRangeMin(port)
                .portRangeMax(port)
                .description(SECURE_PROTOCOL_RULE_DESCRIPTION)
                .get();

            result.add(rule);
        }

        return result;
    }

    private CreateSecurityGroupRule convert(String projectId,
                                            String securityGroupId,
                                            Direction direction,
                                            SecurityGroupExtension extension) {
        CreateSecurityGroupRule.Builder builder = CreateSecurityGroupRule.rule()
            .direction(direction.getInLowerCase())
            .securityGroup(securityGroupId)
            .project(projectId)
            .etherType(IP_V4)
            .remoteIpPrefix(extension.getIpRange())
            .description(extension.getDescription());

        if (isAllProtocolsAllPorts(extension.getProtocol())) {
            return builder.get();
        }

        return builder
            .protocol(extension.getProtocol())
            .portRangeMin(extension.getFromPort())
            .portRangeMax(extension.getToPort())
            .get();
    }

    private List<SecurityGroupType> getSecurityGroupTypeWithNullableId(OpenStackRegionConfig zone, Set<SecurityGroupType> securityGroupTypes) {
        return securityGroupTypes.stream()
            .filter(type -> zone.getAdminSecurityGroupId(type) == null)
            .collect(Collectors.toList());
    }

    private Set<Direction> getDirections(OpenStackTenant project) {
        Set<Direction> directions = new HashSet<>();
        Set<SecurityGroupType> securityGroupTypes = project.getSecurityGroupTypes();
        if (CollectionUtils.isNotEmpty(securityGroupTypes)) {
            for (SecurityGroupType securityGroupType : securityGroupTypes) {
                Collection<Direction> securityGroupTypeDirections = securityGroupType.getDirections();
                directions.addAll(securityGroupTypeDirections);
            }
        }
        return directions;
    }

    private boolean isAllProtocolsAllPorts(String protocol) {
        return ALL_PROTOCOLS_ALL_PORTS.equalsIgnoreCase(protocol);
    }
}
