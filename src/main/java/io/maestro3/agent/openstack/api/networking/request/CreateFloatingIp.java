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

package io.maestro3.agent.openstack.api.networking.request;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import org.apache.http.util.Asserts;


public class CreateFloatingIp {

    @SerializedName("floatingip")
    private FloatingIp floatingIp = new FloatingIp();

    private CreateFloatingIp() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CreateFloatingIp request = new CreateFloatingIp();

        public Builder tenant(String tenantId) {
            request.floatingIp.setTenantId(tenantId);
            return this;
        }

        public Builder inNetwork(String floatingNetworkId) {
            request.floatingIp.setFloatingNetworkId(floatingNetworkId);
            return this;
        }

        public Builder withAddress(String floatingIpAddress) {
            request.floatingIp.setFloatingIpAddress(floatingIpAddress);
            return this;
        }

        public Builder portId(String portId) {
            request.floatingIp.setPortId(portId);
            return this;
        }

        public CreateFloatingIp get() {
            Asserts.notBlank(request.floatingIp.getFloatingNetworkId(), "floating network id cannot be blank");
            return request;
        }
    }
}
