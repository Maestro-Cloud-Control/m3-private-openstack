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
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.network.StartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.ManualModeStartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.ip.EnsureIpAddressInfo;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.MoveIpAddressInfo;
import io.maestro3.agent.model.network.impl.ip.OpenStackFloatingIp;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.AllocateFloatingIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.AssociateStaticIpAddressParameters;
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
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.MetadataKeys;
import io.maestro3.agent.util.PortUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;


class ManualModeNetworkingProvider extends AbstractNetworkingProvider {

    private static final Logger LOG = LogManager.getLogger(ManualModeNetworkingProvider.class);

    private final OpenStackBasedOnFloatingIpsNetworkService networkService;

    public ManualModeNetworkingProvider(OpenStackRegionConfig zone, IOpenStackStaticIpService staticIpService,
                                        ServerDbService instanceService, IOpenStackVLANService vlanService,
                                        IOpenStackClientProvider openStackClientProvider,
                                        OpenStackBasedOnFloatingIpsNetworkService networkService) {
        super(zone, staticIpService, instanceService, vlanService, openStackClientProvider);
        this.networkService = networkService;
    }

    @Override
    public void rollback(InstanceProvisioningProgress progress) {
        releaseStaticIpQuietly(progress);
        deleteDnsRecordQuietly(progress);
    }

    @Override
    public String ensureStaticIpAssociated(OpenStackTenant project, OpenStackServerConfig existing, OpenStackServerConfig incoming) {
        if (incoming.getState().is(ServerStateEnum.STOPPED, ServerStateEnum.ERROR /*ServerStateEnum.SUSPENDED*/)) {
            return null;
        }
        if (StringUtils.isBlank(existing.getNetworkInterfaceInfo().getPublicIp()) && StringUtils.isBlank(incoming.getNetworkInterfaceInfo().getPublicIp())) {
            try {
                // If instance already has port then we can associate static IP to it
                String instancePort = networkService.findAnyInstancePort(project, existing);
                if (StringUtils.isNotBlank(instancePort)) {

                    LOG.info("Trying to associate static IP for instance {}", existing.getNameAlias());
                    AssociateStaticIpAddressParameters parameters = AssociateStaticIpAddressParameters.builder()
                        .instanceId(existing.getNativeId())
                        .build();
                    StaticIpAddress staticIpAddress = networkService.associateStaticIp(project, parameters);
                    incoming.getNetworkInterfaceInfo().setPublicIp(staticIpAddress.getIpAddress());

                    return staticIpAddress.getIpAddress();
                } else {
                    LOG.info("Instance {} port is yet empty.", existing.getNameAlias());
                }
            } catch (Exception e) {
                LOG.warn("Could not associate static IP. Reason: {}. Instance: {}, state: {}", e.getMessage(), existing.getNativeId(), existing.getState());
                return null;
            }
        }
        if (StringUtils.isNotBlank(existing.getNetworkInterfaceInfo().getPublicIp())) {
            return existing.getNetworkInterfaceInfo().getPublicIp();
        }

        return incoming.getNetworkInterfaceInfo().getPublicIp();
    }

    @Override
    public MoveIpAddressInfo moveIpAddressToAnotherProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance) {
        Assert.notNull(fromProject, "fromProject cannot be null.");
        Assert.notNull(toProject, "toProject cannot be null.");
        Assert.notNull(instance, "instance cannot be null.");

        IOpenStackClient fromClient = openStackClientProvider.getClient(zone, fromProject);

        OpenStackFloatingIp floatingIp = getFloatingIp(fromProject, instance, RefreshIpPolicy.REFRESH);
        if (floatingIp == null) {
            return getMoveIpAddressInfoWithMacAddress(instance, fromClient);
        }

        Port oldPort = getPort(fromClient, floatingIp.getPortId());
        if (oldPort == null) {
            return getMoveIpAddressInfoWithMacAddress(instance, fromClient);
        }

        disassociateAndReleaseStaticIp(fromProject, floatingIp);
        detachPortFromServer(instance, fromClient, oldPort);
        deletePort(fromClient, oldPort);

        // create new port with previous values of IP address and Mac Address
        CreatePortRequest request = buildCreatePortRequest(toProject, oldPort);
        Port newPort = createPort(toProject, request);
        if (newPort == null) {
            return null;
        }

        AllocateFloatingIpAddressParameters parameters
            = buildAllocateStaticIpParameters(instance, floatingIp.getIpAddress(), newPort);
        allocateStaticIpAddress(toProject, parameters);

        return new MoveIpAddressInfo().withPortId(newPort.getId());
    }

    @Override
    public void assertInstanceNetworkAvailableInTargetProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance) {
        // not implemented yet, fall through
    }

    private MoveIpAddressInfo getMoveIpAddressInfoWithMacAddress(OpenStackServerConfig instance, IOpenStackClient fromClient) {
        Port serverPort = getFirstServerPort(instance, fromClient);
        if (serverPort != null) {
            detachPortFromServer(instance, fromClient, serverPort);
            deletePort(fromClient, serverPort);
            // set at least mac address, it is needed for instance to be able to ping on another project
            return new MoveIpAddressInfo().withMacAddress(serverPort.getMacAddress());
        }
        LOG.warn("Server {} ({}) has no ports.", instance.getNameAlias(), instance.getNativeId());
        return null;
    }

    private Port getFirstServerPort(OpenStackServerConfig instance, IOpenStackClient client) {
        Port serverPort = null;
        try {
            List<Port> serverPorts = client.listPortsByDeviceId(instance.getNativeId());
            if (CollectionUtils.isNotEmpty(serverPorts)) {
                serverPort = serverPorts.get(0);

            }
        } catch (OSClientException e) {
            LOG.error("Failed to list server ports. Reason: {}", e.getMessage());
        }
        return serverPort;
    }

    private OpenStackFloatingIp getFloatingIp(OpenStackTenant project, OpenStackServerConfig instance, RefreshIpPolicy refreshIpPolicy) {
        List<StaticIpAddress> staticIpAddresses = staticIpService.findStaticIpAddressByInstanceId(zone.getId(), project.getId(), instance.getNativeId());
        if (CollectionUtils.isNotEmpty(staticIpAddresses)) {
            return (OpenStackFloatingIp) staticIpAddresses.get(0);
        }

        if (refreshIpPolicy == RefreshIpPolicy.NO_REFRESH) {
            return null;
        } else {
            LOG.info("Will try to find reserved IP and refresh it.");
            OpenStackStaticIpAddress reservedStaticIpAddress = staticIpService.findReservedByInstance(zone.getId(), project.getId(), instance.getNativeId());
            if (reservedStaticIpAddress != null) {
                networkService.refreshStaticIp(project, reservedStaticIpAddress);
                return getFloatingIp(project, instance, RefreshIpPolicy.NO_REFRESH);
            }
            return null;
        }
    }

    private OpenStackFloatingIp allocateStaticIpAddress(OpenStackTenant toProject, AllocateFloatingIpAddressParameters parameters) {
        try {
            StaticIpAddress staticIpAddress = networkService.allocateStaticIp(toProject, parameters);
            return (OpenStackFloatingIp) staticIpAddress;
        } catch (Exception e) {
            LOG.error(String.format("Failed to allocate static IP. Reason: %s", e.getMessage()), e);
        }
        return null;
    }

    private void detachPortFromServer(OpenStackServerConfig instance, IOpenStackClient fromClient, Port oldPort) {
        try {
            fromClient.detachPortFromServer(instance.getNativeId(), oldPort.getId());
        } catch (OSClientException e) {
            LOG.error("Failed to detach port {} from instance {} ({}). Reason: {}", oldPort.getId(), instance.getNativeId(), instance.getNativeId(), e.getMessage());
        }
    }

    private AllocateFloatingIpAddressParameters buildAllocateStaticIpParameters(OpenStackServerConfig instance, String ipAddress, Port newPort) {
        return AllocateFloatingIpAddressParameters.builder()
            .reservedInstanceId(instance.getNativeId())
            .portId(newPort.getId())
            .floatingIp(ipAddress)
            .build();
    }

    private CreatePortRequest buildCreatePortRequest(OpenStackTenant project, Port port) {
        CreatePortRequest.Builder builder = CreatePortRequest.build()
            .withNetworkId(project.getNetworkId())
            .withSecurityGroupId(project.getSecurityGroupId())
            .withPortName(PortUtils.generateManuallyCreatedPortName());

        String macAddress = port.getMacAddress();
        if (StringUtils.isNotBlank(macAddress)) {
            builder.withMacAddress(macAddress);
        }
        return builder.get();
    }

    private void disassociateAndReleaseStaticIp(OpenStackTenant fromProject, OpenStackFloatingIp floatingIp) {
        try {
            DisassociateOpenStackStaticIpAddressParameters parameters = DisassociateOpenStackStaticIpAddressParameters.builder()
                .staticIp(floatingIp.getIpAddress())
                .build();
            networkService.disassociateStaticIp(fromProject, parameters);
            networkService.releaseStaticIp(fromProject, floatingIp.getIpAddress());
        } catch (Exception e) {
            LOG.error(String.format("Failed to release static IP. Reason: %s", e.getMessage()), e);
        }
    }

    @Override
    public EnsureIpAddressInfo ensureIpAddressForInstanceCameFromAnotherProject(OpenStackTenant project, OpenStackServerConfig instance, Map<String, String> metadata) {
        String portId = metadata.get(MetadataKeys.EO_IP_ADDRESS_ID_TO_ASSIGN);
        IOpenStackClient client = openStackClientProvider.getClient(zone, project);
        if (StringUtils.isNotBlank(portId)) {
            attachPortToServer(instance, portId, client);
        } else {
            String macAddress = metadata.get(MetadataKeys.EO_PREVIOUS_MAC_ADDRESS);
            if (StringUtils.isBlank(macAddress)) {
                macAddress = null;
                LOG.warn("[{}] Will create port with random Mac address for instance came from another project.", instance.getNativeId());
            }

            CreatePortRequest request = buildCreatePortRequest(project, macAddress);
            Port port = getFirstServerPort(instance, client);
            if (port == null) {
                port = createPort(project, request);
                if (port != null) {
                    attachPortToServer(instance, port.getId(), client);
                }
            }
            if (port != null) {
                AllocateFloatingIpAddressParameters parameters = buildAllocateStaticIpParameters(instance, null, port);
                allocateStaticIpAddress(project, parameters);
            }
        }

        OpenStackFloatingIp floatingIp = getFloatingIp(project, instance, RefreshIpPolicy.REFRESH);
        if (floatingIp != null) {
            return new EnsureIpAddressInfo().withPublicIp(floatingIp.getIpAddress()).withPrivateIp(floatingIp.getFixedIp());
        }
        return null;
    }

    private CreatePortRequest buildCreatePortRequest(OpenStackTenant project, String macAddress) {
        CreatePortRequest.Builder builder = CreatePortRequest.build()
            .withNetworkId(project.getNetworkId())
            .withSecurityGroupId(project.getSecurityGroupId())
            .withPortName(PortUtils.generateManuallyCreatedPortName())
            .withMacAddress(macAddress);
        return builder.get();
    }

    @Override
    public StartupNetworkingConfiguration getNetworkConfiguration(OpenStackTenant project,
                                                                  List<String> networks, String ipAddress) {
        StartupNetworkingConfiguration networkingConfiguration = new ManualModeStartupNetworkingConfiguration();
        networkingConfiguration.setNetworkIds(validateNetworks(project, networks));
        return networkingConfiguration;
    }

    @Override
    public void initializeWhileStartup(ServerBootInfo.Builder bootBuilder, InstanceProvisioningProgress progress,
                                       StartupNetworkingConfiguration networkingConfiguration) {

        Assert.notNull(progress, "progress cannot be null.");

        AllocateFloatingIpAddressParameters parameters = AllocateFloatingIpAddressParameters.builder()
            .reservedInstanceId(progress.getInstanceName())
            .build();

        StaticIpAddress staticIpAddress = networkService.allocateStaticIp(progress.getProject(), parameters);
        if (staticIpAddress != null) {
            String ipAddress = staticIpAddress.getIpAddress();
            progress.setStaticIpAddress(staticIpAddress);
            // Only A-typed DNS-record for manual-type regions
//            DnsName dnsName = (DnsName) createDnsRecordQuietly(progress.getProject(), progress.getInstanceName(), ipAddress, DnsRecordType.A);
//            if (dnsName != null) {
//                progress.setDnsName(dnsName);
//            }
        }
        if (CollectionUtils.isNotEmpty(networkingConfiguration.getNetworkIds())) {
            bootBuilder.inNetworks(networkingConfiguration.getNetworkIds());
        }
    }

    @Override
    public void processIpAddressesOnInstanceTerminate(OpenStackTenant project, OpenStackServerConfig instance) {
        disassociateAndReleaseStaticIp(project, instance);
        deleteManuallyCreatedPorts(project, instance);
    }

    private void disassociateAndReleaseStaticIp(OpenStackTenant project, OpenStackServerConfig instance) {
        String staticIp = disassociateStaticIp(project, instance);
        releaseStaticIp(project, staticIp);
    }

    private Port getPort(IOpenStackClient client, String portId) {
        Port port = null;
        try {
            port = client.getPort(portId);
        } catch (OSClientException e) {
            LOG.error(String.format("Failed to get port by ID=%s", portId), e);
        }
        return port;
    }

    private void attachPortToServer(OpenStackServerConfig instance, String portId, IOpenStackClient client) {
        try {
            client.attachPortToServer(instance.getNativeId(), portId);
        } catch (OSClientException e) {
            LOG.error("Failed to attach port to server. Reason: {}", e.getMessage());
        }
    }

    private void releaseStaticIpQuietly(InstanceProvisioningProgress progress) {
        try {
            StaticIpAddress staticIpAddress = progress.getStaticIpAddress();
            if (staticIpAddress != null) {
                networkService.releaseStaticIp(progress.getProject(), staticIpAddress.getIpAddress());
            }
        } catch (Exception e) {
            LOG.error("Failed to automatically release static IP.", e);
        }
    }

    private String disassociateStaticIp(OpenStackTenant project, OpenStackServerConfig instance) {
        String publicIp = instance.getNetworkInterfaceInfo().getPublicIp();
        OpenStackFloatingIp floatingIp = null;
        if (StringUtils.isNotBlank(publicIp)) {
            floatingIp = (OpenStackFloatingIp) staticIpService.findStaticIpAddress(zone.getId(), project.getId(), publicIp);
        } else {
            OpenStackStaticIpAddress staticIpAddress = staticIpService.findReservedByInstance(zone.getId(), project.getId(), instance.getNativeId());
            if (staticIpAddress != null && staticIpAddress instanceof OpenStackFloatingIp) {
                floatingIp = (OpenStackFloatingIp) staticIpAddress;
            }
        }
        if (floatingIp != null && floatingIp.getIpState().nin(IPState.DISASSOCIATING, IPState.RELEASING)) {
            if (StringUtils.isNotBlank(floatingIp.getInstanceId())) {
                DisassociateStaticIpAddressParameters parameters = DisassociateOpenStackStaticIpAddressParameters.builder()
                    .staticIp(floatingIp.getIpAddress())
                    .build();
                networkService.disassociateStaticIp(project, parameters);
            }
            return floatingIp.getIpAddress();
        }
        return null;
    }

    private void releaseStaticIp(OpenStackTenant project, String floatingIp) {
        if (StringUtils.isNotBlank(floatingIp)) {
            networkService.releaseStaticIp(project, floatingIp);
        }
    }

    @Override
    public void processStaticIpOnInstanceMoveToProject(OpenStackTenant project, OpenStackServerConfig instance) {
        OpenStackFloatingIp floatingIp = getFloatingIp(project, instance, RefreshIpPolicy.REFRESH);
        if (floatingIp == null) {
            throw new ReadableAgentException("Instance does not have IP address yet.");
        }
    }

    @Override
    public void commit(InstanceProvisioningProgress progress) {
        // do nothing
    }

    @Override
    protected IVirtOpenStackNetworkService networkService() {
        return networkService;
    }

    private enum RefreshIpPolicy {
        REFRESH, NO_REFRESH
    }
}
