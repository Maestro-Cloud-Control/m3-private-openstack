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

package io.maestro3.agent.openstack.api.networking.extension.impl;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.networking.bean.FixedIp;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.api.networking.extension.IPortExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PortExtension extends BasicNetworkingService implements IPortExtension {

    public PortExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<Port> list() throws OSClientException {
        return listWithFilter(null);
    }

    @Override
    public List<Port> listByTenantId(String tenantId) throws OSClientException {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("tenant_id", Collections.singletonList(tenantId));
        return listWithFilter(filter);
    }

    @Override
    public List<Port> listByDeviceId(String deviceId) throws OSClientException {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("device_id", Collections.singletonList(deviceId));
        return listWithFilter(filter);
    }

    @Override
    public void clearDns(String portId) throws OSClientException {
        BasicOSRequest<Void> clearDns = BasicOSRequest.builder(Void.class, endpoint())
                .path("/ports/%s", portId)
                .put(PortToUpdate.clearDns())
                .create();
        client.execute(clearDns);
    }

    @Override
    public void updateSecurityGroups(String portId, List<String> securityGroups) throws OSClientException {
        BasicOSRequest<Void> clearDns = BasicOSRequest.builder(Void.class, endpoint())
                .path("/ports/%s", portId)
                .put(PortToUpdate.updateSecurityGroups(securityGroups))
                .create();
        client.execute(clearDns);
    }

    @Override
    public void updateMacAddress(String portId, String macAddress) throws OSClientException {
        BasicOSRequest<Void> updateMacAddress = BasicOSRequest.builder(Void.class, endpoint())
                .path("/ports/%s", portId)
                .put(PortToUpdate.updateMacAddress(macAddress))
                .create();
        client.execute(updateMacAddress);
    }

    @Override
    public Port get(String portId) throws OSClientException {
        BasicOSRequest<PortWrapper> getPort = BasicOSRequest.builder(PortWrapper.class, endpoint())
                .path("/ports/%s", portId)
                .create();
        PortWrapper portWrapper = client.execute(getPort).getEntity();
        if (portWrapper != null) {
            return portWrapper.port;
        }
        return null;
    }

    @Override
    public void delete(String portId) throws OSClientException {
        BasicOSRequest<Void> deletePort = BasicOSRequest.builder(Void.class, endpoint())
                .path("/ports/%s", portId)
                .delete()
                .create();
        client.execute(deletePort);
    }

    @Override
    public Port create(String networkId, String securityGroupId, String name) throws OSClientException {
        BasicOSRequest<PortWrapper> createPort = BasicOSRequest.builder(PortWrapper.class, endpoint())
                .path("/ports")
                .post(new PortNameWrapper(networkId, securityGroupId, name))
                .create();
        PortWrapper entity = client.execute(createPort).getEntity();
        return (entity == null) ? null : entity.port;
    }

    @Override
    public Port create(String networkId, String securityGroupId, String name, String ipAddress) throws OSClientException {
        BasicOSRequest<PortWrapper> createPort = BasicOSRequest.builder(PortWrapper.class, endpoint())
                .path("/ports")
                .post(new PortNameWrapper(networkId, securityGroupId, name, ipAddress, null))
                .create();
        PortWrapper entity = client.execute(createPort).getEntity();
        return (entity == null) ? null : entity.port;
    }

    @Override
    public Port create(CreatePortRequest request) throws OSClientException {
        PortNameWrapper wrapper = new PortNameWrapper(
                request.getNetworkId(),
                request.getSecurityGroupId(),
                request.getPortName(),
                request.getIpAddress(),
                request.getMacAddress()
        );

        BasicOSRequest<PortWrapper> createPort = BasicOSRequest.builder(PortWrapper.class, endpoint())
                .path("/ports")
                .post(wrapper)
                .create();
        PortWrapper entity = client.execute(createPort).getEntity();
        return (entity == null) ? null : entity.port;
    }

    private List<Port> listWithFilter(Map<String, List<String>> filter) throws OSClientException {
        BasicOSRequest.BasicOSRequestBuilder<Ports> list = BasicOSRequest
                .builder(Ports.class, endpoint())
                .path(pathWithFilter("/ports", filter));

        Ports result = client.execute(list.create()).getEntity();
        return result == null ? null : result.portList;
    }

    private static class Ports {
        @SerializedName("ports")
        private List<Port> portList;
    }

    private static class PortWrapper {
        private Port port;
    }

    private static class PortNameWrapper {
        private final PortName port;

        private PortNameWrapper(String networkId, String securityGroupId, String name) {
            this.port = new PortName(networkId, securityGroupId, name, null, null);
        }

        private PortNameWrapper(String networkId, String securityGroupId, String name, String ipAddress, String macAddress) {
            this.port = new PortName(networkId, securityGroupId, name, ipAddress, macAddress);
        }
    }

    private static class PortName {
        private final String name;
        @SerializedName("fixed_ips")
        private final List<FixedIp> fixedIps;
        @SerializedName("network_id")
        private final String networkId;
        @SerializedName("security_groups")
        private final List<String> securityGroups;
        @SerializedName("mac_address")
        private final String macAddress;

        private PortName(String networkId, String securityGroupId, String name, String ipAddress, String macAddress) {
            this.name = name;
            this.networkId = networkId;
            this.macAddress = macAddress;
            this.fixedIps = StringUtils.isNotBlank(ipAddress) ? Collections.singletonList(new FixedIp(ipAddress)) : null;
            securityGroups = Collections.singletonList(securityGroupId);
        }
    }

    private static class PortToUpdate {
        private final UpdatePortRequest port;

        private PortToUpdate(UpdatePortRequest port) {
            this.port = port;
        }

        private static PortToUpdate clearDns() {
            return new PortToUpdate(UpdatePortRequest.builder()
                    .dnsName("")
                    .macAddress(null)
                    .build());
        }

        private static PortToUpdate updateMacAddress(String macAddress) {
            return new PortToUpdate(UpdatePortRequest.builder()
                    .dnsName(null)
                    .macAddress(macAddress)
                    .build());
        }

        private static PortToUpdate updateSecurityGroups(List<String> securityGroups) {
            return new PortToUpdate(UpdatePortRequest.builder()
                    .dnsName(null)
                    .macAddress(null)
                    .securityGroups(securityGroups)
                    .build());
        }
    }

    private static class UpdatePortRequest {
        @SerializedName("dns_name")
        private String dnsName;
        @SerializedName("mac_address")
        private String macAddress;
        @SerializedName("security_groups")
        private List<String> securityGroups;

        private UpdatePortRequest() {
        }

        private static UpdatePortRequestBuilder builder() {
            return new UpdatePortRequestBuilder();
        }

        private static class UpdatePortRequestBuilder {
            private String dnsName;
            private String macAddress;
            private List<String> securityGroups;

            private UpdatePortRequestBuilder() {
                securityGroups = Lists.newArrayList();
            }

            private UpdatePortRequestBuilder dnsName(String dnsName) {
                this.dnsName = dnsName;
                return this;
            }

            private UpdatePortRequestBuilder macAddress(String macAddress) {
                this.macAddress = macAddress;
                return this;
            }

            private UpdatePortRequestBuilder securityGroups(List<String> securityGroups) {
                this.securityGroups.addAll(securityGroups);
                return this;
            }

            private UpdatePortRequest build() {
                UpdatePortRequest updatePortRequest = new UpdatePortRequest();
                updatePortRequest.dnsName = dnsName;
                updatePortRequest.macAddress = macAddress;
                if (CollectionUtils.isNotEmpty(securityGroups)) {
                    updatePortRequest.securityGroups = securityGroups;
                }

                return updatePortRequest;
            }
        }

    }
}
