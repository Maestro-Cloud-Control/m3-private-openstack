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

package io.maestro3.agent.openstack.api.compute.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.IPortInterfaceExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


class PortInterfaceExtension extends BasicComputeService implements IPortInterfaceExtension {

    PortInterfaceExtension(IOSClient client) {
        super(client);
    }

    @Override
    public void attach(String server, String port) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/os-interface", server)
                .post(new InterfaceAttachmentWrapper(port))
                .create();
        client.execute(request);
    }

    @Override
    public void attach(String server, String networkId, String ipAddress) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/os-interface", server)
                .post(new InterfaceAttachmentWrapper(networkId, ipAddress))
                .create();
        client.execute(request);
    }

    @Override
    public void detach(String server, String port) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/os-interface/%s", server, port)
                .delete().create();
        client.execute(request);
    }

    private static class InterfaceAttachmentWrapper {
        private final InterfaceAttachment interfaceAttachment;

        private InterfaceAttachmentWrapper(String portId) {
            interfaceAttachment = new InterfaceAttachment(portId);
        }

        private InterfaceAttachmentWrapper(String networkId, String ipAddress) {
            interfaceAttachment = new InterfaceAttachment(networkId, ipAddress);
        }
    }

    private static class InterfaceAttachment {
        @SerializedName("port_id")
        private final String portId;
        @SerializedName("net_id")
        private final String networkId;
        @SerializedName("fixed_ips")
        private final List<FixedIpAttachment> fixedIps;

        private InterfaceAttachment(String portId) {
            this.portId = portId;
            this.networkId = null;
            this.fixedIps = null;
        }

        private InterfaceAttachment(String networkId, String ipAddress) {
            this.portId = null;
            this.networkId = networkId;
            if (StringUtils.isNotBlank(ipAddress)) {
                this.fixedIps = Collections.singletonList(new FixedIpAttachment(ipAddress));
            } else {
                this.fixedIps = null;
            }
        }
    }

    private static class FixedIpAttachment {
        @SerializedName("ip_address")
        private final String ipAddress;

        private FixedIpAttachment(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
}
