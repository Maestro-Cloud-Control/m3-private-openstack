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
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.InstanceProvisioningProgress;
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.network.NetworkingType;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.NetworkApiFilter;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.OpenStackNetworkUtils;
import io.maestro3.agent.util.PortUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


abstract class AbstractNetworkingProvider implements IOpenStackNetworkingProvider {

    private static final Logger LOG = LogManager.getLogger(AbstractNetworkingProvider.class);

    // supposed to postpone PTR-record creation 2 times if some errors have occurred
    private static final int MINUTES_TO_WAIT_FOR_DNS_PTR_RECORD_CREATION = 6;

//    private final IDnsNamesService dnsNamesService;

    final IOpenStackStaticIpService staticIpService;
    final ServerDbService instanceService;
    final IOpenStackVLANService vlanService;
    final IOpenStackClientProvider openStackClientProvider;
    final OpenStackRegionConfig zone;

    AbstractNetworkingProvider(OpenStackRegionConfig zone, IOpenStackStaticIpService staticIpService, ServerDbService instanceService,
                               IOpenStackVLANService vlanService, IOpenStackClientProvider openStackClientProvider) {
        this.staticIpService = staticIpService;
        this.instanceService = instanceService;
        this.vlanService = vlanService;
        this.zone = zone;
        this.openStackClientProvider = openStackClientProvider;
    }



    @Override
    public String getDefaultNetworkId(OpenStackTenant project) {
        if (project == null) {
            return null;
        }
        return project.getNetworkId();
    }

    @Override
    public String getDefaultSecurityGroupId(OpenStackTenant project) {
        if (project == null) {
            return null;
        }
        return project.getSecurityGroupId();
    }

    @Override
    public boolean isDefaultSecurityGroupRequired() {
        return true;
    }


    @Override
    public void deleteInstancePorts(OpenStackRegionConfig regionConfig, OpenStackTenant project, OpenStackServerConfig instance) {
        List<Port> ports = listInstancePorts(regionConfig, project, instance);
        if (CollectionUtils.isNotEmpty(ports)) {
            IOpenStackClient client = openStackClientProvider.getClient(regionConfig, project);
            for (Port port : ports) {
                deletePort(client, port);
            }
        }
    }

    @Override
    public IVirtOpenStackNetworkService networkingService() {
        return networkService();
    }

    protected abstract IVirtOpenStackNetworkService networkService();

    protected Set<String> validateNetworks(OpenStackTenant project, Collection<String> networks) {
        IOpenStackClient client = openStackClientProvider.getClient(zone, project);
        Set<String> networksToBootFrom = new HashSet<>();
        if (CollectionUtils.isEmpty(networks)) {
            String defaultNetworkId = getDefaultNetworkId(project);
            if (StringUtils.isNotBlank(defaultNetworkId)) {
                ensureCanBootInDefaultNetwork(defaultNetworkId, project.getTenantAlias(), client);
                networksToBootFrom.add(defaultNetworkId);
            }
        } else {
            ensureCanBootInNetworks(networks, project, client);
            networksToBootFrom.addAll(networks);
        }
        return networksToBootFrom;
    }

    private void ensureCanBootInDefaultNetwork(String networkId, String projectCode, IOpenStackClient client) {
        Network network = null;
        try {
            network = client.getNetwork(networkId);
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to get network by its ID=%s", networkId), e);
        }

        if (network == null) {
            throw new ReadableAgentException(
                "Default network with ID=" + networkId + " was removed at OpenStack for " + projectCode + " in " + zone.getRegionAlias());
        }
    }

    private void ensureCanBootInNetworks(Collection<String> networkIds, OpenStackTenant project, IOpenStackClient client) {
        String pmcCode = project.getTenantAlias().toUpperCase();
        NetworkApiFilter filter = new NetworkApiFilter().withIds(networkIds);
        List<Network> availableNetworks = null;
        String defaultNetworkId = zone.getNetworkingPolicy().getNetworkId();
        String defaultProjectId = null;
        try {
            availableNetworks = client.listNetworks(filter);
            Network defaultNetwork = client.getNetwork(defaultNetworkId);
            defaultProjectId = defaultNetwork.getTenantId();
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to list networks with ids=%s", networkIds), e);
        }

        if (CollectionUtils.isEmpty(availableNetworks)) {
            throw new ReadableAgentException("Network(s), specified by user, was not found at OpenStack for " + pmcCode + " in " + zone.getRegionAlias());
        }

        Map<String, Network> networksMappedOnUUID = mapNetworkOnUUID(availableNetworks);
        List<VLAN> VLANs = vlanService.getAvailableForTenant(project.getId(), zone.getId(), false);
        List<String> filteredNetworkIds = OpenStackNetworkUtils.filterNonDmzVLANAvailableInProject(
            VLANs, networksMappedOnUUID.keySet(), project.getId());
        for (String uuid : networkIds) {
            Network network = networksMappedOnUUID.get(uuid);
            boolean isFromNotAvailableVLAN = !StringUtils.isEqualsIgnoreCase(project.getNetworkId(), uuid)
                && !filteredNetworkIds.contains(uuid);
            ArrayList<String> availableTenantIds = Lists.newArrayList(project.getNativeId(), defaultProjectId);

            if (network == null || isFromNotAvailableVLAN || !availableTenantIds.contains(network.getTenantId())) {
                throw new ReadableAgentException("Network with UUID " + uuid + " was not allowed to runInstance action at "
                    + pmcCode + " in " + zone.getRegionAlias());
            }
        }
    }

    protected void deleteManuallyCreatedPorts(OpenStackTenant project, OpenStackServerConfig instance) {
        List<Port> ports = listInstancePorts(zone, project, instance);
        if (CollectionUtils.isNotEmpty(ports)) {
            IOpenStackClient client = openStackClientProvider.getClient(zone, project);
            for (Port port : ports) {
                if (port.getName().startsWith(PortUtils.MANUALLY_CREATED_PORT_PREFIX)) {
                    deletePort(client, port);
                }
            }
        }
    }

    protected void deleteDnsRecordQuietly(InstanceProvisioningProgress progress) {
        // not implemented
    }


    protected void deletePort(IOpenStackClient client, Port port) {
        try {
            client.deletePort(port.getId());
        } catch (OSClientException e) {
            LOG.error("Failed to delete port {}. Reason: {}", port.getId(), e.getMessage());
        }
    }

    protected Port createPort(OpenStackTenant project, String macAddress, String networkId) {
        CreatePortRequest request = CreatePortRequest.build()
            .withNetworkId(StringUtils.isNotBlank(networkId) ? networkId : project.getNetworkId())
            .withSecurityGroupId(project.getSecurityGroupId())
            .withMacAddress(macAddress)
            .get();

        return createPort(project, request);
    }

    protected Port createPort(OpenStackTenant project, CreatePortRequest request) {
        if (request == null) {
            request = CreatePortRequest.build()
                .withNetworkId(project.getNetworkId())
                .withSecurityGroupId(project.getSecurityGroupId())
                .withPortName(PortUtils.generateManuallyCreatedPortName())
                .get();
        }

        Port createdPort = null;
        IOpenStackClient client = openStackClientProvider.getClient(zone, project);
        try {
            createdPort = client.createPort(request);
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to create port with IP address %s.", request.getIpAddress()), e);
        }
        return createdPort;
    }

    protected List<Port> listInstancePorts(OpenStackRegionConfig regionConfig, OpenStackTenant project,
                                           OpenStackServerConfig instance) {
        IOpenStackClient client = openStackClientProvider.getClient(regionConfig, project);
        List<Port> ports = null;
        try {
            ports = client.listPortsByDeviceId(instance.getNativeId());
        } catch (OSClientException e) {
            LOG.error("Failed to list ports for instance {}. Reason: {}", instance.getNativeId(), e.getMessage());
        }
        return ports;
    }

    private Map<String, Network> mapNetworkOnUUID(List<Network> networks) {
        Map<String, Network> result = new HashMap<>();
        for (Network network : networks) {
            String uuid = network.getId();
            result.put(uuid, network);
        }
        return result;
    }
}
