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
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackFloatingIp;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.AllocateFloatingIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.AllocateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.api.networking.bean.Router;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.exception.OpenStackConflictException;
import io.maestro3.agent.openstack.filter.impl.SubnetApiFilter;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IVLANService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.OpenStackConversionUtils;
import io.maestro3.agent.util.OpenStackNetworkUtils;
import io.maestro3.agent.util.SubnetUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class OpenStackBasedOnFloatingIpsNetworkService extends OpenStackNetworkService {

    private static final Logger LOG = LogManager.getLogger(OpenStackBasedOnFloatingIpsNetworkService.class);


    public OpenStackBasedOnFloatingIpsNetworkService(IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                                     IOpenStackStaticIpService staticIpService, OpenStackUpdateStaticIpsService updateStaticIpsService,
                                                     IOpenStackTenantRepository projectService, OpenStackRegionConfig zone, IVLANService vlanService,
                                                     IOpenStackSecurityGroupService openStackSecurityGroupService) {
        super(clientProvider, instanceService, staticIpService, updateStaticIpsService, projectService, zone, vlanService, openStackSecurityGroupService);
    }

    @Override
    public StaticIpAddress allocateStaticIp(OpenStackTenant project, AllocateStaticIpAddressParameters parameters) {
        checkAllocationLimit(project, UNUSED_STATIC_IPS_MAX_COUNT);
        OpenStackFloatingIp openStackFloatingIp = null;
        try {
            IOpenStackClient client = clientProvider.getAdminClient(zone, project);
            AllocateFloatingIpAddressParameters floatingIpAddressParameters;
            if (parameters instanceof AllocateFloatingIpAddressParameters) {
                floatingIpAddressParameters = (AllocateFloatingIpAddressParameters) parameters;
            } else {
                floatingIpAddressParameters = convert(parameters);
            }

            openStackFloatingIp = new OpenStackFloatingIp();

            if (parameters != null && StringUtils.isNotBlank(parameters.getFixedIp())) {
                checkAlreadyAllocated(zone, project, parameters.getFixedIp());
                openStackFloatingIp.setFixedIp(parameters.getFixedIp());
                openStackFloatingIp.setFixed(true);
                openStackFloatingIp.setIpAddress(parameters.getFixedIp() != null ? parameters.getFixedIp() : ANY_IP_ADDRESS);
            } else {
                FloatingIp floatingIp = getFloatingIp(client, floatingIpAddressParameters, project);
                if (floatingIp == null) {
                    throw new ReadableAgentException("Failed to allocate static IP.");
                }
                openStackFloatingIp.setExternalId(floatingIp.getId());
                openStackFloatingIp.setIpAddress(floatingIp.getFloatingIpAddress());
            }

            openStackFloatingIp.setPortId(null);
            openStackFloatingIp.setInstanceId(null);
            openStackFloatingIp.setTenantId(project.getId());
            openStackFloatingIp.setTenantName(project.getTenantAlias());
            openStackFloatingIp.setZoneId(this.zone.getId());
            openStackFloatingIp.setRegionName(this.zone.getRegionAlias());
            openStackFloatingIp.setPublic(true);
            openStackFloatingIp.setIpState(IPState.READY);

            // Reserves static IP by instance specified
            if (floatingIpAddressParameters != null && StringUtils.isNotBlank(floatingIpAddressParameters.getReservedInstanceId())) {
                openStackFloatingIp.setReservedBy(floatingIpAddressParameters.getReservedInstanceId());
            }
            onStaticIpAllocated(project, openStackFloatingIp);
        } catch (OpenStackConflictException e) {
            throw new ReadableAgentException("Floating IPs limit exceeded.");
        } catch (OSClientException error) {
            handle(error);
        }
        return openStackFloatingIp;
    }

    private void checkAllocationLimit(OpenStackTenant project, int unusedStaticIpsMaxCount) {
        Collection<OpenStackStaticIpAddress> unusedStaticIps = staticIpService.findNotAssociatedAndNotReservedStaticIps(zone.getId(), project.getId(), null);
        if (unusedStaticIps.size() >= unusedStaticIpsMaxCount) {
            throw new ReadableAgentException("Unused static IPs limit exceeded.");
        }
    }

    private AllocateFloatingIpAddressParameters convert(AllocateStaticIpAddressParameters parameters) {
        if (parameters == null) {
            return null;
        }

        return AllocateFloatingIpAddressParameters.builder()
                .fixedIp(parameters.getFixedIp())
                .networkId(parameters.getNetworkId())
                .build();
    }

    private FloatingIp getFloatingIp(IOpenStackClient client, AllocateFloatingIpAddressParameters floatingIpAddressParameters,
                                     OpenStackTenant project) throws OSClientException {
        FloatingIp floatingIp;
        String networkId = resolveNetworkId(floatingIpAddressParameters, project, client);
        if (floatingIpAddressParameters != null && StringUtils.isNotBlank(floatingIpAddressParameters.getPortId())) {
            floatingIp = client.allocateFloatingIp(networkId, floatingIpAddressParameters.getPortId(),
                    floatingIpAddressParameters.getFloatingIp());
        } else {
            floatingIp = client.allocateFloatingIp(networkId, null);
        }
        return floatingIp;
    }

    private String resolveNetworkId(AllocateFloatingIpAddressParameters parameters, OpenStackTenant project,
                                    IOpenStackClient client) throws OSClientException {
        String networkId;
        if (parameters == null || StringUtils.isBlank(parameters.getNetworkId())) {
            networkId = project.getNetworkId();
        } else {
            networkId = parameters.getNetworkId();
        }

        Network network = client.getNetwork(networkId);
        if (network == null) {
            throw new ReadableAgentException(String.format("Vlan with ID %s is not found on OpenStack for project %s", networkId, project.getTenantAlias()));
        }

        if (network.isExternal()) {
            return networkId;
        }

        return getExternalNetworkId(network, client);
    }

    public String findAnyInstancePort(OpenStackTenant project, OpenStackServerConfig instance) {
        IOpenStackClient client = clientProvider.getClient(zone, project);
        try {
            return getPort(instance, client);
        } catch (OSClientException e) {
            LOG.warn(e.getMessage());
            throw new ReadableAgentException("Instance " + instance.getNativeId() + " has no ports yet");
        }
    }

    @Override
    protected void assertCanAssociateStaticIpAddress(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, IOpenStackClient client) {
        super.assertCanAssociateStaticIpAddress(project, instance, ipAddress, client);

        //check that ip address is reachable from vm network
        assertIpAddressReachableFromVMNetwork(project, instance, ipAddress, client);
    }

    private void assertIpAddressReachableFromVMNetwork(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, IOpenStackClient client) {
        String vmNetworkId = instance.getNetworkInterfaceInfo().getNetworkId();
        try {
            Network network = client.getNetwork(vmNetworkId);
            if (network == null) {
                throw new ReadableAgentException(String.format("Vlan with ID %s is not found on OpenStack for project %s", vmNetworkId, project.getTenantAlias()));
            }

            if (network.isExternal()) {
                throw new ReadableAgentException(
                        String.format("IpAddress %s is not reachable from VM %s network", ipAddress.getIpAddress(), instance.getNameAlias()));
            }

            String externalNetworkId = getExternalNetworkId(network, client);
            SubnetApiFilter filter = new SubnetApiFilter().inNetwork(externalNetworkId);
            List<NovaSubnet> subnets = client.toApi().networking().subnets().list(filter);
            Set<String> cidrs = subnets.stream()
                    .map(NovaSubnet::getCidr)
                    .collect(Collectors.toSet());
            boolean reachable = SubnetUtils.isInSubnets(ipAddress.getIpAddress(), cidrs);
            if (!reachable) {
                throw new ReadableAgentException(
                        String.format("IpAddress %s is not reachable from VM %s network", ipAddress.getIpAddress(), instance.getNameAlias()));
            }
        } catch (OSClientException e) {
            throw new ReadableAgentException("OpenStack error occurred: " + e.getMessage(), e);
        }
    }

    private String getExternalNetworkId(Network network, IOpenStackClient client) throws OSClientException {
        String routerName = OpenStackNetworkUtils.generateRouterName(network.getName());
        List<Router> routers = client.toApi().networking().routers().listByName(routerName);
        if (CollectionUtils.isEmpty(routers)) {
            throw new ReadableAgentException("Failed to get network router");
        }

        String externalNetworkId = routers.get(0).getExternalGateway().getNetworkId();
        if (StringUtils.isBlank(externalNetworkId)) {
            throw new ReadableAgentException("Failed to get external network ID");
        }

        return externalNetworkId;
    }

    @Override
    protected OpenStackStaticIpAddress getIncomingStaticIpAddress(OpenStackTenant project, OpenStackStaticIpAddress staticIpAddress) {
        IOpenStackClient client = clientProvider.getClient(zone, project);
        OpenStackFloatingIp existing = (OpenStackFloatingIp) staticIpAddress;
        try {
            FloatingIp incoming = client.getFloatingIp(existing.getExternalId());
            if (incoming != null) {
                return OpenStackConversionUtils.toOpenStackFloatingIp(incoming, project, zone);
            }
        } catch (OSClientException e) {
            LOG.error("Failed to list floating IPs by ID={}", existing.getExternalId());
        }
        return null;
    }

    @Override
    protected String associateStaticIp(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, IOpenStackClient client) throws OSClientException {
        OpenStackFloatingIp floatingIp = getOpenStackFloatingIp(ipAddress);
        String portId = getPort(instance, client);
        client.associateFloatingIp(floatingIp.getExternalId(), portId); // floating ip is never 'null' here
        return portId;
    }

    @Override
    protected void disassociateStaticIp(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, DisassociateStaticIpAddressParameters parameters) throws OSClientException {
        OpenStackFloatingIp floatingIp = getOpenStackFloatingIp(ipAddress);

        IOpenStackClient client = clientProvider.getClient(zone, project);
        client.disassociateFloatingIp(floatingIp.getExternalId());
    }

    @Override
    protected void releaseStaticIp(OpenStackTenant project, OpenStackStaticIpAddress ipAddress) throws OSClientException {
        OpenStackFloatingIp floatingIp = getOpenStackFloatingIp(ipAddress);

        IOpenStackClient client = clientProvider.getClient(zone, project);
        client.releaseFloatingIp(floatingIp.getExternalId());
    }

    @Override
    protected void processStaticIpAddressAfterDisassociate(OpenStackStaticIpAddress staticIpAddress) {
        staticIpAddress.setPortId(null);
    }

    private OpenStackFloatingIp getOpenStackFloatingIp(OpenStackStaticIpAddress ipAddress) {
        if (!(ipAddress instanceof OpenStackFloatingIp)) {
            throw new IllegalStateException("ipAddress must be an instance of OpenStackFloatingIp.");
        }
        return (OpenStackFloatingIp) ipAddress;
    }

    private String getPort(OpenStackServerConfig instance, IOpenStackClient client) throws OSClientException {
        List<Port> ports = client.listPortsByDeviceId(instance.getNativeId()); // device can have several ports, choose any
        if (CollectionUtils.isNotEmpty(ports)) {
            return ports.get(0).getId();
        }
        throw new OSClientException("Oops! It's turned out that instance " + instance.getNativeId() + " has no ports, so we couldn't attach floating IP");
    }
}
