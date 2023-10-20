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

import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.InstanceProvisioningProgress;
import io.maestro3.agent.model.network.NetworkingType;
import io.maestro3.agent.model.network.StartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.AutoStartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.network.impl.ip.EnsureIpAddressInfo;
import io.maestro3.agent.model.network.impl.ip.MoveIpAddressInfo;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateOpenStackStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.IVirtOpenStackBasedOnPortsNetworkService;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.MetadataKeys;
import io.maestro3.agent.util.PortUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;


class AutoModeNetworkingProvider extends AbstractNetworkingProvider {

    private static final Logger LOG = LogManager.getLogger(AutoModeNetworkingProvider.class);

    private final IVirtOpenStackBasedOnPortsNetworkService networkService;
    private boolean floatingIpPreferable;

    public AutoModeNetworkingProvider(OpenStackRegionConfig zone, IOpenStackStaticIpService staticIpService, ServerDbService instanceService,
                                      IOpenStackVLANService vlanService, IOpenStackClientProvider openStackClientProvider,
                                      IVirtOpenStackBasedOnPortsNetworkService networkService,
                                      boolean floatingIpPreferable) {
        super(zone, staticIpService, instanceService, vlanService, openStackClientProvider);
        this.floatingIpPreferable = floatingIpPreferable;
        this.networkService = networkService;
    }

    @Override
    public void initializeWhileStartup(ServerBootInfo.Builder bootBuilder, InstanceProvisioningProgress progress,
                                       StartupNetworkingConfiguration startupNetworkingConfiguration) {

        AutoStartupNetworkingConfiguration networkingConfiguration = (AutoStartupNetworkingConfiguration) startupNetworkingConfiguration;
        if (CollectionUtils.isNotEmpty(networkingConfiguration.getNetworkIds()) && StringUtils.isNotBlank(networkingConfiguration.getPortId())) {
            throw new ReadableAgentException("Both networks and port parameters specified.");
        }

        if (StringUtils.isNotBlank(networkingConfiguration.getPortId())) {
            initializeWithPort(bootBuilder, progress, networkingConfiguration);
        } else {
            initializeWithNetworkIds(bootBuilder, progress, networkingConfiguration);
        }
    }

    private void initializeWithPort(ServerBootInfo.Builder bootBuilder, InstanceProvisioningProgress progress,
                                    AutoStartupNetworkingConfiguration networkingConfiguration) {
        synchronized (networkingConfiguration.getPortId().intern()) {
            progress.setPortId(networkingConfiguration.getPortId());
            setPortReservedBy(progress, progress.getInstanceName());
            String portId = networkingConfiguration.getPortId();
            if (StringUtils.isNotBlank(portId)) {
                evaluateStaticIp(progress, networkingConfiguration, portId);

                bootBuilder.withPort(portId);
                progress.setPortId(portId);
                if (!networkingConfiguration.isStatic()) {
                    progress.setPredefinedIpAddress(networkingConfiguration.getIpAddress());
                }
            }
        }
    }

    private void evaluateStaticIp(InstanceProvisioningProgress progress, AutoStartupNetworkingConfiguration networkingConfiguration, String portId) {
        if (!networkingConfiguration.isStatic()) {
            return;
        }

        OpenStackRegionConfig zone = progress.getZone();
        OpenStackPort port = staticIpService.findPortById(zone.getId(), progress.getProject().getId(), portId);
        if (port == null) {
            throw new ReadableAgentException("Port with ID " + portId + " does not exist");
        }

        String reservedBy = (port.getInstanceId() != null) ? port.getInstanceId() : port.getReservedBy();
        if (reservedBy != null && !reservedBy.equals(progress.getInstanceName())) {
            throw new ReadableAgentException("Static IP " + port.getIpAddress() + " is associated with another instance " + reservedBy);
        }
    }

    private void initializeWithNetworkIds(ServerBootInfo.Builder bootBuilder, InstanceProvisioningProgress progress,
                                          StartupNetworkingConfiguration networkingConfiguration) {

        setPortReservedBy(progress, progress.getInstanceName());
        bootBuilder.inNetworks(validateNetworks(progress.getProject(), networkingConfiguration.getNetworkIds()));
    }

    @Override
    public void commit(InstanceProvisioningProgress progress) {
        if (StringUtils.isNotBlank(progress.getPortId())) {
            networkService.refreshPort(progress.getProject(), progress.getPortId());
        }
    }

    @Override
    public void rollback(InstanceProvisioningProgress progress) {
        deleteDnsRecordQuietly(progress);
        setPortReservedBy(progress, null);
    }

    @Override
    public void processIpAddressesOnInstanceTerminate(OpenStackTenant project, OpenStackServerConfig instance) {
        disassociateStaticIpAddresses(project, instance);
        deleteManuallyCreatedPorts(project, instance);
    }

    private void disassociateStaticIpAddresses(OpenStackTenant project, OpenStackServerConfig instance) {
        List<StaticIpAddress> staticIpAddresses = staticIpService.findStaticIpAddressByInstanceId(zone.getId(), project.getId(),
            instance.getNativeId());

        if (CollectionUtils.isNotEmpty(staticIpAddresses)) {
            for (StaticIpAddress staticIpAddress : staticIpAddresses) {
                DisassociateStaticIpAddressParameters parameters = DisassociateOpenStackStaticIpAddressParameters.builder()
                    .staticIp(staticIpAddress.getIpAddress())
                    .build();
                networkServiceForStaticIpsProcessing().disassociateStaticIp(project, parameters);
            }
        }
    }

    @Override
    public void processStaticIpOnInstanceMoveToProject(OpenStackTenant project, OpenStackServerConfig instance) {
        List<StaticIpAddress> staticIpAddresses = staticIpService.findStaticIpAddressByInstanceId(zone.getId(), project.getId(),
            instance.getNativeId());

        if (CollectionUtils.isNotEmpty(staticIpAddresses)) {
            throw new ReadableAgentException("Cannot move instance with static IP associated.");
        }
    }

    @Override
    public MoveIpAddressInfo moveIpAddressToAnotherProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance) {
        IOpenStackClient fromClient = openStackClientProvider.getClient(zone, fromProject);
        MoveIpAddressInfo moveIpAddressInfo = new MoveIpAddressInfo();
        List<Port> ports = retrieveInstancePorts(fromProject, toProject, instance);

        Port port = ports.get(0);
        deletePort(fromClient, port);
        String ipAddress = PortUtils.getIpAddress(port);
        LOG.info("[{}] Will create port with mac address {} on project {}", instance.getNativeId(), port.getMacAddress(), toProject.getTenantAlias());

        CreatePortRequest.Builder builder = CreatePortRequest.build()
            .withSecurityGroupId(toProject.getSecurityGroupId());
        if (StringUtils.isNotBlank(ipAddress)) {
            builder.withIpAddress(ipAddress);
            builder.withNetworkId(port.getNetworkId());
            builder.withMacAddress(port.getMacAddress());
        }

        Port createdPort = createPort(toProject, builder.get());
        if (createdPort != null) {
            moveIpAddressInfo.withPortId(createdPort.getId());
        }
        moveIpAddressInfo.withNetworkId(port.getNetworkId());
        return moveIpAddressInfo;
    }

    @Override
    public void assertInstanceNetworkAvailableInTargetProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance) {
        List<Port> ports = retrieveInstancePorts(fromProject, toProject, instance);
        Port instancePort = ports.get(0);
        String instanceNetworkId = instancePort.getNetworkId();
        if (StringUtils.isEqualsIgnoreCase(toProject.getNetworkId(), instanceNetworkId)) {
            return;
        }

        List<OpenStackVLAN> openStackVLANS = vlanService.findByOpenStackNetworkId(zone.getId(), instanceNetworkId);
        boolean isVLANAvailableForProject = openStackVLANS.stream()
            .anyMatch(openStackVLAN -> isVLANAvailableForProject(toProject, openStackVLAN));

        if (!isVLANAvailableForProject) {
            throw new ReadableAgentException(
                "Move instance to project without access to current instance VLAN iы unavailable.");
        }
    }

    private List<Port> retrieveInstancePorts(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance) {
        List<Port> ports = listInstancePorts(zone, fromProject, instance);
        if (portIsNotConfiguredProperly(ports)) {
            throw new ReadableAgentException("Port is not configured properly yet.");
        }
        return ports;
    }

    private boolean isVLANAvailableForProject(OpenStackTenant toProject, OpenStackVLAN openStackVLAN) {
        return StringUtils.isEqualsIgnoreCase(openStackVLAN.getTenantId(), toProject.getId()) || openStackVLAN.getTenantId() == null;
    }

    private boolean portIsNotConfiguredProperly(List<Port> ports) {
        return CollectionUtils.isEmpty(ports) || StringUtils.isBlank(ports.get(0).getMacAddress())
            || StringUtils.isBlank(PortUtils.getIpAddress(ports.get(0)));
    }

    @Override
    public EnsureIpAddressInfo ensureIpAddressForInstanceCameFromAnotherProject(OpenStackTenant project, OpenStackServerConfig instance, Map<String, String> metadata) {
        String portId = null;
        String macAddress = null;
        String networkId = null;
        if (MapUtils.isNotEmpty(metadata)) {
            portId = metadata.get(MetadataKeys.EO_IP_ADDRESS_ID_TO_ASSIGN);
            macAddress = metadata.get(MetadataKeys.EO_PREVIOUS_MAC_ADDRESS);
            networkId = metadata.get(MetadataKeys.EO_PREVIOUS_NETWORK_ID);
        }

        Port port = getOrCreatePort(project, portId, macAddress, networkId);
        if (port == null) {
            return null;
        }

        IOpenStackClient client = openStackClientProvider.getClient(zone, project);
        try {
            client.attachPortToServer(instance.getNativeId(), port.getId());
            return new EnsureIpAddressInfo().withPrivateIp(PortUtils.getIpAddress(port));
        } catch (OSClientException e) {
            LOG.error("Failed to attach port {} to server {}", port.getId(), instance.getNativeId());
        }

        return null;
    }

    @Override
    public StartupNetworkingConfiguration getNetworkConfiguration(OpenStackTenant project,
                                                                  List<String> networks, String ipAddress) {
        if (CollectionUtils.isNotEmpty(networks) && StringUtils.isNotBlank(ipAddress)) {
            throw new ReadableAgentException("Both networks and port parameters specified.");
        }

        AutoStartupNetworkingConfiguration networkingConfiguration = new AutoStartupNetworkingConfiguration();
        if (CollectionUtils.isNotEmpty(networks)) {
            networkingConfiguration.setNetworkIds(validateNetworks(project, networks));
        }
        if (StringUtils.isNotBlank(ipAddress)) {
            String portId = getPortByIpAddress(project, ipAddress);
            networkingConfiguration.setPortId(portId);
            networkingConfiguration.setStatic(true);
        }

        return networkingConfiguration;
    }

    @Override
    public String ensureStaticIpAssociated(OpenStackTenant project, OpenStackServerConfig existing, OpenStackServerConfig incoming) {
        return incoming.getNetworkInterfaceInfo().getPrivateIP();
    }

    private Port getOrCreatePort(OpenStackTenant project, String portId, String macAddress, String networkId) {
        Port port = null;
        if (StringUtils.isNotBlank(portId)) {
            port = getPort(project, portId);
        }
        if (port == null) {
            port = createPort(project, macAddress, networkId);
        }

        return port;
    }

    private Port getPort(OpenStackTenant project, String portId) {
        try {
            return openStackClientProvider.getClient(zone, project).getPort(portId);
        } catch (OSClientException e) {
            LOG.error("Failed to get port by ID={}", portId);
        }

        return null;
    }

    @Override
    protected IVirtOpenStackNetworkService networkService() {
        if (floatingIpPreferable || zone.getNetworkingPolicy().getNetworkingType() == NetworkingType.MANUAL) {
            return networkServiceForStaticIpsProcessing();
        }
        return networkService;
    }

    protected IVirtOpenStackNetworkService networkServiceForStaticIpsProcessing() {
        return networkService;
    }

    private String getPortByIpAddress(OpenStackTenant project, String ipAddress) {
        OpenStackPort port = (OpenStackPort) staticIpService.findStaticIpAddress(zone.getId(), project.getId(), ipAddress);
        if (port == null) {
            throw new ReadableAgentException("Static IP " + ipAddress + " does not exist");
        }
        if (port.getInstanceId() != null) {
            throw new ReadableAgentException("Static IP " + port.getIpAddress() + " is associated with another instance " + port.getInstanceId());
        }
        return port.getPortId();
    }

    private OpenStackPort setPortReservedBy(InstanceProvisioningProgress progress, String instanceId) {
        Assert.notNull(progress.getProject(), "progress.project cannot be null.");
        String portId = progress.getPortId();
        if (StringUtils.isNotBlank(portId)) {
            OpenStackPort openStackPort = staticIpService.findPortById(zone.getId(), progress.getProject().getId(), portId);
            if (openStackPort == null) {
                return null;
            }

            String portInstanceId = openStackPort.getInstanceId();
            String instanceReservingPort = openStackPort.getReservedBy();
            // do not set reservedBy property if port is already reserved
            if ((StringUtils.isBlank(portInstanceId) && StringUtils.isBlank(instanceReservingPort)) || instanceId == null) {
                openStackPort.setReservedBy(instanceId);
                staticIpService.update(openStackPort);
            }
            return openStackPort;
        }
        return null;
    }
}
