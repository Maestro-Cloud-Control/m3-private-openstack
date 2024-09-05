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

import com.google.common.collect.Lists;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.network.NetworkingPolicy;
import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.AssociateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DescribeStaticIpAddressesParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateOpenStackStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.vlan.OpenStackSubnet;
import io.maestro3.agent.model.network.impl.vlan.SubnetModel;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.exception.OpenStackConflictException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IVLANService;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.ConversionUtils;
import io.maestro3.agent.util.OpenStackConversionUtils;
import io.maestro3.agent.util.OpenStackNetworkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


abstract class OpenStackNetworkService implements IVirtOpenStackNetworkService {
    private static final int ALLOWED_IP_OPERATIONS_MINUTES = 2;
    private static final Logger LOG = LogManager.getLogger(OpenStackNetworkService.class);

    protected static final int UNUSED_STATIC_IPS_MAX_COUNT = 5;
    protected static final String ANY_IP_ADDRESS = "0.0.0.0";

    protected final IOpenStackClientProvider clientProvider;
    protected final ServerDbService instanceService;
    protected final IOpenStackStaticIpService staticIpService;
    protected final OpenStackUpdateStaticIpsService updateStaticIpsService;
    protected final UpdateStaticIpsProcessor updateStaticIpsProcessor = new UpdateStaticIpsProcessor();
    protected final IOpenStackTenantRepository projectService;
    protected final OpenStackRegionConfig zone;
    protected final IVLANService vlanService;
    protected final IOpenStackSecurityGroupService openStackSecurityGroupService;

    public OpenStackNetworkService(IOpenStackClientProvider clientProvider, ServerDbService instanceService, IOpenStackStaticIpService staticIpService, OpenStackUpdateStaticIpsService updateStaticIpsService, IOpenStackTenantRepository projectService,
                                   OpenStackRegionConfig zone, IVLANService vlanService, IOpenStackSecurityGroupService openStackSecurityGroupService) {
        this.clientProvider = clientProvider;
        this.openStackSecurityGroupService = openStackSecurityGroupService;
        this.instanceService = instanceService;
        this.staticIpService = staticIpService;
        this.updateStaticIpsService = updateStaticIpsService;
        this.projectService = projectService;
        this.zone = zone;
        this.vlanService = vlanService;
    }

    @Override
    public List<SubnetModel> describeSubnets(List<String> subnetIds, OpenStackTenant project) {
        IOpenStackClient client = clientProvider.getClient(zone, project);
        List<NovaSubnet> openStackSubnets = null;
        String defaultNetworkId = zone.getNetworkingPolicy().getNetworkId();
        String defaultProjectId = null;
        try {
            openStackSubnets = client.listSubnets(subnetIds);
            Network defaultNetwork = client.getNetwork(defaultNetworkId);
            defaultProjectId = defaultNetwork.getTenantId();
        } catch (OSClientException e) {
            handle(e);
        }

        List<SubnetModel> ourSubnets = OpenStackConversionUtils.toOurSubnets(openStackSubnets);
        filterSubnetsFromZoneVLANs(project, ourSubnets, defaultProjectId);

        return ourSubnets;
    }

    private void filterSubnetsFromZoneVLANs(OpenStackTenant project, List<SubnetModel> ourSubnets, String defaultProjectId) {
        List<VLAN> zoneVLANs = vlanService.getAvailableForTenant(project.getId(), zone.getId(), false);
        List<String> subnetNetworkIds = ConversionUtils.convertCollection(ourSubnets, input -> input != null ? input.getNetworkId() : null);
        List<String> filteredNotDmzNetworkIds = OpenStackNetworkUtils.filterNonDmzVLANSubnets(zoneVLANs, subnetNetworkIds);
        List<String> filteredAvailableInProjectNetworkIds = OpenStackNetworkUtils.filterVLANSubnetsAvailableInProject(zoneVLANs, subnetNetworkIds, project.getId());
        Iterator<SubnetModel> iterator = ourSubnets.iterator();
        while (iterator.hasNext()) {
            OpenStackSubnet subnet = (OpenStackSubnet) iterator.next();
            String subnetNetworkId = subnet.getNetworkId();
            boolean isDefaultNetwork = StringUtils.equalsIgnoreCase(project.getNetworkId(), subnetNetworkId);
            if (!isDefaultNetwork && !filteredAvailableInProjectNetworkIds.contains(subnetNetworkId)) {
                iterator.remove();
                continue;
            }
            boolean isFromDmzVLAN = !isDefaultNetwork && !filteredNotDmzNetworkIds.contains(subnetNetworkId);
            subnet.setDmz(isFromDmzVLAN);
            ArrayList<String> availableTenantIds = Lists.newArrayList(project.getNativeId(), defaultProjectId);
            boolean isFromAnotherProject = !availableTenantIds.contains(subnet.getTenantId());
            if (isFromAnotherProject) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<StaticIpAddress> describeStaticIps(OpenStackTenant project, DescribeStaticIpAddressesParameters parameters) {
        return describeStaticIPs(project, parameters.getDomainType(), parameters.getInstanceIds(), parameters.getStaticIps(), zone);
    }

    @Override
    public StaticIpAddress associateStaticIp(OpenStackTenant project, AssociateStaticIpAddressParameters parameters) {
        return associate(project, parameters);
    }

    private StaticIpAddress associate(OpenStackTenant project, AssociateStaticIpAddressParameters parameters) {
        OpenStackServerConfig instance = instanceService.findServer(zone.getId(), project.getId(), parameters.getInstanceId());
        OpenStackStaticIpAddress ipAddress = findSpareOrAllocateStaticIp(project, parameters, instance);
        Assert.notNull(ipAddress, "No static IP found. Static ip is blank and search for any free static IP is disabled.");

//        assertInstanceIsNotMovingToAnotherProject(instance);
        assertIpOperationsAllowed(instance, ipAddress);

        IOpenStackClient client = clientProvider.getClient(zone, project);
        assertCanAssociateStaticIpAddress(project, instance, ipAddress, client);

        try {
            if (!ipAddress.isFixed()) {
                String portId = associateStaticIp(project, instance, ipAddress, client);
                updateInstance(instance, ipAddress);
                ipAddress.setPortId(portId);
            }
            ipAddress.setInstanceId(instance.getNativeId());
            ipAddress.setIpState(IPState.READY);
            staticIpService.update(ipAddress);
        } catch (OpenStackConflictException e) {
            LOG.error("Failed to associate static IP.", e);
            throw new ReadableAgentException("Failed to associate static IP, because instance already has it.");
        } catch (Exception e) {
            LOG.error("Failed to associate static IP.", e);
            handle(e);
        }
        return ipAddress;
    }

    private void updateInstance(OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress) {

        // do not set IP address to booting instance (for manual OS zones)
        if (instance.getState() != ServerStateEnum.STARTING) {
            instance.getNetworkInterfaceInfo().setPublicIp(ipAddress.getIpAddress());
        }
        instance.setLastIpOperationDate(System.currentTimeMillis());
        instanceService.saveServerConfig(instance);
    }

    protected void assertCanAssociateStaticIpAddress(OpenStackTenant project, OpenStackServerConfig instance,
                                                     OpenStackStaticIpAddress ipAddress, IOpenStackClient client) {
    }

    @Override
    public StaticIpAddress disassociateStaticIp(OpenStackTenant project, DisassociateStaticIpAddressParameters parameters) {
        LOG.info("DISASSOCIATE_STATIC_IP for " + zone.getRegionAlias() + " in " + project.getTenantAlias());
        return disassociate(project, parameters);
    }

    private StaticIpAddress disassociate(OpenStackTenant project, DisassociateStaticIpAddressParameters parameters) {
        OpenStackStaticIpAddress staticIpAddress = findStaticIpAddress(project, parameters.getStaticIp());

        ensureCanDissociateStaticIp(staticIpAddress);
        OpenStackServerConfig instance = null;
        String instanceId = staticIpAddress.getInstanceId();
        if (isNotBlank(instanceId)) {
            instance = instanceService.findServerByNativeId(zone.getId(), project.getId(), instanceId);
//            assertInstanceIsNotMovingToAnotherProject(instance);
        }
        assertIpOperationsAllowed(instance, staticIpAddress);
        try {
            if (!staticIpAddress.isFixed()) {
                disassociateStaticIp(project, instance, staticIpAddress, parameters);
            }
            staticIpAddress.setInstanceId(null);
            staticIpAddress.setReservedBy(null);
            processStaticIpAddressAfterDisassociate(staticIpAddress);
            staticIpService.update(staticIpAddress);
            if (instance != null && !staticIpAddress.isFixed()) {
                instance.setLastIpOperationDate(System.currentTimeMillis());
                instanceService.saveServerConfig(instance);
                if (needToRefreshInstance(parameters)) {
                    refreshInstance(project, instance);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to disassociate static IP.", e);
            handle(e);
        }
        return staticIpAddress;
    }

    private boolean needToRefreshInstance(DisassociateStaticIpAddressParameters parameters) {
        if (!(parameters instanceof DisassociateOpenStackStaticIpAddressParameters)) {
            // always refresh instance unless it is specified not to do so
            return true;
        }
        DisassociateOpenStackStaticIpAddressParameters disassociateOpenStackStaticIpAddressParameters =
                (DisassociateOpenStackStaticIpAddressParameters) parameters;
        return disassociateOpenStackStaticIpAddressParameters.isRefreshInstanceAfter();
    }

    @Override
    public boolean releaseStaticIp(OpenStackTenant project, String staticIp) {
        LOG.info("RELEASING_STATIC_IP for " + zone.getRegionAlias() + " in " + project.getTenantAlias());
        release(project, staticIp);
        return true;
    }

    private void release(OpenStackTenant project, String staticIp) {
        OpenStackStaticIpAddress openStackStaticIp = findStaticIpAddress(project, staticIp);
        ensureCanReleaseStaticIp(openStackStaticIp);
        try {
            if (openStackStaticIp.isFixed()) {
                staticIpService.delete(openStackStaticIp);
            } else {
                releaseStaticIp(project, openStackStaticIp);
            }
            staticIpService.delete(openStackStaticIp);
        } catch (OSClientException e) {
            LOG.error("Unable to release static IP.", e);
            handle(e);
        }
    }

    @Override
    public void refreshStaticIp(OpenStackTenant project, OpenStackStaticIpAddress staticIpAddress) {
        OpenStackStaticIpAddress incoming = getIncomingStaticIpAddress(project, staticIpAddress);
        if (incoming == null) {
            return;
        }
        updateStaticIpsService.updateStaticIp(project, incoming, staticIpAddress);
    }

    protected abstract OpenStackStaticIpAddress getIncomingStaticIpAddress(OpenStackTenant project, OpenStackStaticIpAddress staticIpAddress);

    @Override
    public void updateStaticIps() {
        List<OpenStackTenant> projects = projectService.findByRegionIdInCloud(zone.getId());
        projects.forEach(
                updateStaticIpsProcessor::process
        );
    }

    protected abstract String associateStaticIp(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, IOpenStackClient client) throws OSClientException;

    protected abstract void disassociateStaticIp(OpenStackTenant project, OpenStackServerConfig openStackInstance, OpenStackStaticIpAddress ipAddress, DisassociateStaticIpAddressParameters parameters) throws OSClientException;

    protected abstract void processStaticIpAddressAfterDisassociate(OpenStackStaticIpAddress staticIpAddress);

    protected abstract void releaseStaticIp(OpenStackTenant project, OpenStackStaticIpAddress ipAddress) throws OSClientException;

    protected String getNetworkId() {
        NetworkingPolicy networkingPolicy = zone.getNetworkingPolicy();
        return networkingPolicy.getNetworkId();
    }

    protected void handle(Exception e) {
        if (e instanceof OSClientException) {
            throw new ReadableAgentException("OpenStack exception occurred: " + e.getMessage());
        } else {
            throw new ReadableAgentException("Unexpected exception occurred: " + e.getMessage(), e);
        }
    }

    private OpenStackStaticIpAddress findSpareOrAllocateStaticIp(OpenStackTenant project, AssociateStaticIpAddressParameters parameters,
                                                                 OpenStackServerConfig instance) {
        if (parameters.getStaticIpAddress() != null) {
            return (OpenStackStaticIpAddress) parameters.getStaticIpAddress();
        }

        // no spare IP found, let's allocate new one
        return (OpenStackStaticIpAddress) allocateStaticIp(project, null);
    }

    private OpenStackStaticIpAddress findStaticIpAddress(OpenStackTenant project, String staticIp) {
        StaticIpAddress staticIpAddress = staticIpService.findStaticIpAddress(zone.getId(), project.getId(), staticIp);
        if (staticIpAddress == null) {
            throw new ReadableAgentException("Static IP " + staticIp + " does not exist");
        }
        return (OpenStackStaticIpAddress) staticIpAddress;
    }

    private void refreshInstance(final OpenStackTenant project, final OpenStackServerConfig instance) {
        try {
//            UpdateResourceContext updateResourceContext = new UpdateResourceContext(systemContext, (OpenStackProject) project, UpdateResourceInitiator.MANUAL);
//            instancesService.updateOneInstance(updateResourceContext, instance);
        } catch (Exception e) {
            LOG.error("Unable to refresh instance.", e);
        }
    }

    protected void onStaticIpAllocated(OpenStackTenant project, StaticIpAddress staticIpAddress) {
        staticIpService.save(staticIpAddress);
    }

    private class UpdateStaticIpsProcessor {

        public void process(OpenStackTenant project) {
            LOG.info("Processing ips for tenant " + project.getTenantAlias());
            try {
                updateStaticIpsService.updateStaticIps(project);
            } catch (Exception e) {
                LOG.error("Failed to update static IPs", e);
            }
        }
    }

    protected List<StaticIpAddress> describeStaticIPs(OpenStackTenant project, DomainType domainType, List<String> instanceIds,
                                                      List<String> staticIps, OpenStackRegionConfig zone) {
        return staticIpService.findStaticIpAddresses(zone.getId(), project.getId(), staticIps, domainType, instanceIds);
    }

    protected void assertIpOperationsAllowed(OpenStackServerConfig instance, OpenStackStaticIpAddress staticIpAddress) {
        if (staticIpAddress != null && staticIpAddress.isFixed()) {
            // Always allow for fixed IP addresses because they are fake
            return;
        }
        if (instance != null) {
            DateTime lastIpOperationDateTime = instance.getLastIpOperationDate() != null ? new DateTime(instance.getLastIpOperationDate()) : null;
            int allowedIpOperationsMinutes = zone.getAllowedIpOperationsMinutes() > 0 ? zone.getAllowedIpOperationsMinutes() : ALLOWED_IP_OPERATIONS_MINUTES;
            if (lastIpOperationDateTime != null && lastIpOperationDateTime.plusMinutes(allowedIpOperationsMinutes).isAfter(DateTime.now())) {
                throw new ReadableAgentException("IP operations are not allowed now.");
            }
        }
    }

    public void ensureCanDissociateStaticIp(StaticIpAddress staticIp) {
        if (staticIp.getInstanceId() == null) {
            throw new ReadableAgentException("Static IP " + staticIp.getIpAddress() + " is not associated with any instance");
        }
    }

    public void ensureCanReleaseStaticIp(StaticIpAddress staticIp) {
        if (staticIp.getInstanceId() != null) {
            throw new ReadableAgentException("Can not release associated static IP" + staticIp.getIpAddress());
        }
    }

    protected void checkAlreadyAllocated(OpenStackRegionConfig zone, OpenStackTenant project, String fixedIp) {
        StaticIpAddress address = staticIpService.findStaticIpAddress(zone.getId(), project.getId(), fixedIp);
        if (address != null) {
            throw new ReadableAgentException("Such ip address is already allocated.");
        }
    }
}
