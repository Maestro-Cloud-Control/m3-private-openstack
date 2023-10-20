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
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.AllocateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateOpenStackStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.exception.OpenStackConflictException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IVLANService;
import io.maestro3.agent.service.IVirtOpenStackBasedOnPortsNetworkService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.OpenStackConversionUtils;
import io.maestro3.agent.util.PortUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.util.List;


class OpenStackBasedOnPortsNetworkService extends OpenStackNetworkService implements IVirtOpenStackBasedOnPortsNetworkService {

    private static final Logger LOG = LogManager.getLogger(OpenStackBasedOnPortsNetworkService.class);

    public OpenStackBasedOnPortsNetworkService(IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                               IOpenStackStaticIpService staticIpService, OpenStackUpdateStaticIpsService updateStaticIpsService,
                                               IOpenStackTenantRepository projectService, OpenStackRegionConfig zone, IVLANService vlanService,
                                               IOpenStackSecurityGroupService openStackSecurityGroupService) {
        super(clientProvider, instanceService, staticIpService, updateStaticIpsService, projectService, zone, vlanService, openStackSecurityGroupService);
    }

    @Override
    public StaticIpAddress allocateStaticIp(OpenStackTenant project, AllocateStaticIpAddressParameters parameters) {
        OpenStackPort openStackPort = null;
        try {
            openStackPort = new OpenStackPort();
            if (parameters != null && StringUtils.isNotBlank(parameters.getFixedIp())) {
                checkAlreadyAllocated(zone, project, parameters.getFixedIp());
                openStackPort.setIpAddress(parameters.getFixedIp() != null ? parameters.getFixedIp() : ANY_IP_ADDRESS);
                openStackPort.setFixedIp(parameters.getFixedIp());
                openStackPort.setFixed(true);
            } else {
                Port port = createPort(project, PortUtils.generateStaticIpPortName());
                openStackPort.setPortId(port.getId());
                openStackPort.setIpAddress(PortUtils.getIpAddress(port));
            }
            openStackPort.setTenantId(project.getId());
            openStackPort.setTenantName(project.getTenantAlias());
            openStackPort.setZoneId(zone.getId());
            openStackPort.setRegionName(zone.getRegionAlias());
            openStackPort.setPublic(false);
            openStackPort.setIpState(IPState.READY);
            onStaticIpAllocated(project, openStackPort);
        } catch (OpenStackConflictException e) {
            throw new ReadableAgentException("Ports limit exceeded.");
        } catch (OSClientException error) {
            handle(error);
        }
        return openStackPort;
    }

    @Override
    public void refreshPort(final OpenStackTenant project, final String portId) {
        Assert.notNull(project, "project cannot be null.");
        Assert.hasText(portId, "portId cannot be null or empty.");

        OpenStackPort existing = staticIpService.findPortById(zone.getId(), project.getId(), portId);
        IOpenStackClient client = clientProvider.getAdminClient(zone, project);
        try {
            Port port = client.getPort(portId);
            OpenStackPort incoming = OpenStackConversionUtils.toOpenStackPort(port, project, zone);
            updateStaticIpsService.updateStaticIp(project, incoming, existing);
        } catch (OSClientException e) {
            LOG.error("Failed to refresh port {}. Reason: {}", portId, e.getMessage());
        }
    }

    @Override
    protected OpenStackStaticIpAddress getIncomingStaticIpAddress(OpenStackTenant project, OpenStackStaticIpAddress staticIpAddress) {
        IOpenStackClient client = clientProvider.getClient(zone, project);
        OpenStackPort existingPort = (OpenStackPort) staticIpAddress;
        try {
            Port port = client.getPort(existingPort.getPortId());
            return OpenStackConversionUtils.toOpenStackPort(port, project, zone);
        } catch (OSClientException e) {
            LOG.error("Failed to get port by ID={}. Reason: {}", existingPort.getPortId(), e.getMessage());
        }
        return null;
    }

    @Override
    protected void assertCanAssociateStaticIpAddress(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress, IOpenStackClient client) {
        super.assertCanAssociateStaticIpAddress(project, instance, ipAddress, client);

        if (ipAddress.isFixed()) {
            return;
        }
        try {
            List<Port> serverPorts = client.listPortsByDeviceId(instance.getNativeId());
            if (CollectionUtils.isNotEmpty(serverPorts)) {
                for (Port serverPort : serverPorts) {
                    assertInstanceAlreadyHasStaticIpAddress(serverPort, instance.getNativeId(), ipAddress.getIpAddress());
                }
            }
        } catch (OSClientException e) {
            LOG.debug(e);
            LOG.error("Failed to list ports by device. Reason: {}", e.getMessage());
        }
    }

    @Override
    protected String associateStaticIp(OpenStackTenant project, final OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress,
                                       IOpenStackClient client) throws OSClientException {
        OpenStackPort openStackPort = getOpenStackPort(ipAddress);
        Port staticPort = assertAndGetStaticPort(client, project, openStackPort);
        List<Port> serverPorts = client.listPortsByDeviceId(instance.getNativeId());
        if (CollectionUtils.isNotEmpty(serverPorts)) {
            Port existingPort = serverPorts.get(0);
            // Detach port from server
            client.detachPortFromServer(instance.getNativeId(), existingPort.getId());
            // Delete just detached port
            client.deletePort(existingPort.getId());
            // Update "static" port's MAC address
            client.updatePortMacAddress(staticPort.getId(), existingPort.getMacAddress());
        }

        // Attach just updated "static" port to server
        client.attachPortToServer(instance.getNativeId(), staticPort.getId());
        return openStackPort.getPortId();
    }

    private Port assertAndGetStaticPort(IOpenStackClient client, OpenStackTenant project, OpenStackPort port) throws OSClientException {
        Port staticPort = client.getPort(port.getPortId());
        if (staticPort != null) {
            if (StringUtils.isNotBlank(staticPort.getDeviceId())) {
                String portOwnerName = getPortOwnerName(project, staticPort);
                throw new ReadableAgentException("Static IP " + port.getIpAddress() + " is associated with another instance " + portOwnerName);
            }
        } else {
            throw new ReadableAgentException("Port with ID " + port.getPortId() + " does not exist");
        }
        return staticPort;
    }

    private void assertInstanceAlreadyHasStaticIpAddress(Port serverPort, String instanceOperationalSearchId, String ipAddressToAssociate) {
        if (serverPort.getName() != null && serverPort.getName().startsWith(PortUtils.EO_PORT_PREFIX)) {
            throw new ReadableAgentException("Failed to associate static IP, because instance already has it.");
        }
    }

    @Override
    protected void disassociateStaticIp(OpenStackTenant project, OpenStackServerConfig instance, OpenStackStaticIpAddress ipAddress,
                                        DisassociateStaticIpAddressParameters incomingParameters) throws OSClientException {

        DisassociateOpenStackStaticIpAddressParameters parameters = getActualParameters(incomingParameters);

        OpenStackPort openStackPort = getOpenStackPort(ipAddress);
        IOpenStackClient client = clientProvider.getClient(zone, project);

        if (instance != null) {
            client.detachPortFromServer(instance.getNativeId(), openStackPort.getPortId());

            Port staticPort = client.getPort(openStackPort.getPortId());
            client.deletePort(staticPort.getId());
            Port recreatedStaticPort = createPort(project, staticPort.getName(), openStackPort.getIpAddress(), null);
            ipAddress.setPortId(recreatedStaticPort.getId());
            staticIpService.update(ipAddress);

            if (parameters.isCreateNewInstead()) {
                Port newRandomPort = createPortWithMacAddress(project, PortUtils.generateManuallyCreatedPortName(), staticPort.getMacAddress());
                attachPortToServer(project, instance, newRandomPort.getId());
            }
        } else {
            LOG.warn("Could not perform 'detachPortFromServer' action because instance is null.");
        }
    }

    private DisassociateOpenStackStaticIpAddressParameters getActualParameters(DisassociateStaticIpAddressParameters parameters) {
        if (parameters instanceof DisassociateOpenStackStaticIpAddressParameters) {
            return (DisassociateOpenStackStaticIpAddressParameters) parameters;
        }
        return DisassociateOpenStackStaticIpAddressParameters.builder()
            .staticIp(parameters.getStaticIp())
            .refreshInstanceAfter(true)
            .createNewInstead(true)
            .build();
    }

    @Override
    protected void releaseStaticIp(OpenStackTenant project, OpenStackStaticIpAddress ipAddress) throws OSClientException {
        OpenStackPort openStackPort = getOpenStackPort(ipAddress);
        IOpenStackClient client = clientProvider.getClient(zone, project);

        client.deletePort(openStackPort.getPortId());
    }

    @Override
    protected void processStaticIpAddressAfterDisassociate(OpenStackStaticIpAddress staticIpAddress) {
        // do nothing
    }

    private void attachPortToServer(OpenStackTenant project, final OpenStackServerConfig instance, final String newPortId) throws OSClientException {
        final IOpenStackClient client = clientProvider.getClient(zone, project);
        try {
            client.attachPortToServer(instance.getNativeId(), newPortId);
        } catch (OSClientException e) {
            LOG.error("Error while attaching port to server.", e);
        }
    }

    private Port createPort(OpenStackTenant openStackProject, String portName) throws OSClientException {
        return createPort(openStackProject, portName, null, null);
    }

    private Port createPortWithMacAddress(OpenStackTenant openStackProject, String portName, String macAddress) throws OSClientException {
        return createPort(openStackProject, portName, null, macAddress);
    }

    private Port createPort(OpenStackTenant openStackProject, String portName, String ipAddress, String macAddress) throws OSClientException {
        IOpenStackClient client = clientProvider.getAdminClient(zone, openStackProject);
        String defaultSecurityGroupId = getDefaultSecurityGroupId(openStackProject);
        CreatePortRequest request = CreatePortRequest.build()
            .withNetworkId(openStackProject.getNetworkId())
            .withSecurityGroupId(defaultSecurityGroupId)
            .withIpAddress(ipAddress)
            .withMacAddress(macAddress)
            .withPortName(portName)
            .get();
        return client.createPort(request);
    }

    private String getPortOwnerName(OpenStackTenant project, Port port) {
        OpenStackServerConfig actualPortOwner = instanceService.findServerByNativeId(zone.getId(), project.getId(), port.getDeviceId());
        if (actualPortOwner != null) {
            return actualPortOwner.getNameAlias();
        }
        try {
            IOpenStackClient client = clientProvider.getClient(zone, project);
            Server server = client.getServer(project.getNativeId(), port.getDeviceId());
            if (server != null) {
                return server.getName();
            }
        } catch (OSClientException e) {
            LOG.error("Failed to get server by ID={}", port.getDeviceId());
        }
        return "(unknown)";
    }

    private String getDefaultSecurityGroupId(OpenStackTenant project) {
        if (project != null) {
            return project.getSecurityGroupId();
        }
        return null;
    }

    private OpenStackPort getOpenStackPort(OpenStackStaticIpAddress ipAddress) {
        if (!(ipAddress instanceof OpenStackPort)) {
            throw new IllegalStateException("ipAddress must be an instance of OpenStackPort.");
        }
        return (OpenStackPort) ipAddress;
    }
}
