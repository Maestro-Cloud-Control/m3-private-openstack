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

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import io.maestro3.agent.openstack.api.networking.extension.IFloatingIpExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.api.networking.request.CreateFloatingIp;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.client.IOSClientOption;
import io.maestro3.agent.openstack.client.OSClientOption;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.util.List;


public class FloatingIpExtension extends BasicNetworkingService implements IFloatingIpExtension {

    private static final IOSClientOption WITH_NULL_SERIALIZER = OSClientOption.builder().withNullSerializer().build();

    public FloatingIpExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<FloatingIp> listByTenantId(String externalId) throws OSClientException {
        IOSRequest<FloatingIps> request = BasicOSRequest.builder(FloatingIps.class, endpoint())
                .path("/floatingips?tenant_id=" + externalId)
                .create();
        FloatingIps result = client.execute(request).getEntity();
        return result == null ? null : result.floatingIpList;
    }

    @Override
    public FloatingIp create(CreateFloatingIp configuration) throws OSClientException {
        IOSRequest<FloatingIpWrapper> request = BasicOSRequest.builder(FloatingIpWrapper.class, endpoint())
                .path("/floatingips")
                .post(configuration)
                .create();
        FloatingIpWrapper result = client.execute(request).getEntity();
        return result == null ? null : result.floatingIp;
    }

    @Override
    public FloatingIp detail(String floatingIpId) throws OSClientException {
        IOSRequest<FloatingIpWrapper> request = BasicOSRequest.builder(FloatingIpWrapper.class, endpoint())
                .path("/floatingips/%s", floatingIpId)
                .create();
        FloatingIpWrapper result = client.execute(request).getEntity();
        return result == null ? null : result.floatingIp;
    }

    @Override
    public void delete(String floatingIpId) throws OSClientException {
        IOSRequest<Void> request = BasicOSRequest.builder(Void.class, endpoint())
                .path("/floatingips/%s", floatingIpId)
                .delete()
                .create();
        client.execute(request);
    }

    @Override
    public FloatingIp associate(String floatingIpId, String portId) throws OSClientException {
        IOSRequest<FloatingIpWrapper> request = BasicOSRequest.builder(FloatingIpWrapper.class, endpoint())
                .path("/floatingips/%s", floatingIpId)
                .put(AssociativeFloatingIp.withPort(portId))
                .create();
        FloatingIpWrapper result = client.execute(request).getEntity();
        return result == null ? null : result.floatingIp;
    }

    @Override
    public FloatingIp disassociate(String floatingIpId) throws OSClientException {
        IOSRequest<FloatingIpWrapper> request = BasicOSRequest.builder(FloatingIpWrapper.class, endpoint())
                .path("/floatingips/%s", floatingIpId)
                .put(AssociativeFloatingIp.empty())
                .create();
        FloatingIpWrapper result = client.execute(request, WITH_NULL_SERIALIZER).getEntity();
        return result == null ? null : result.floatingIp;
    }

    private static class FloatingIps {
        @SerializedName("floatingips")
        private List<FloatingIp> floatingIpList;
    }

    private static class FloatingIpWrapper {
        @SerializedName("floatingip")
        private FloatingIp floatingIp;
    }

    private static class AssociativeFloatingIp {
        @SerializedName("floatingip")
        private SimpleFloatingIp floatingIp;

        private AssociativeFloatingIp(SimpleFloatingIp floatingIp) {
            this.floatingIp = floatingIp;
        }

        private static AssociativeFloatingIp withPort(String portId) {
            return new AssociativeFloatingIp(new SimpleFloatingIp(portId));
        }

        private static AssociativeFloatingIp empty() {
            return new AssociativeFloatingIp(new SimpleFloatingIp());
        }
    }

    private static class SimpleFloatingIp {
        @SerializedName("port_id")
        private String portId;

        SimpleFloatingIp(String portId) {
            this.portId = portId;
        }

        SimpleFloatingIp() {
        }
    }
}
