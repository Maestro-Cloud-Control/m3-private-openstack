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

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.ExternalGateway;
import io.maestro3.agent.openstack.api.networking.bean.FixedIp;


public class CreateRouter {

    private RouterConfiguration router = new RouterConfiguration();

    private CreateRouter() {
    }

    public static Builder router() {
        return new Builder();
    }

    public static class Builder {

        private CreateRouter request = new CreateRouter();

        public Builder withName(String name) {
            request.router.name = name;
            return this;
        }

        public Builder forProject(String tenantId) {
            request.router.tenantId = tenantId;
            return this;
        }

        public Builder highlyAvailable(boolean highlyAvailable) {
            request.router.highlyAvailable = highlyAvailable;
            return this;
        }

        public Builder down() {
            request.router.adminStateUp = false;
            return this;
        }

        public Builder withExternalGateway(String networkId) {
            return withExternalGateway(networkId, null);
        }

        public Builder withExternalGateway(String networkId, String subnetId, String ipAddress) {
            request.router.externalGateway.setNetworkId(networkId);
            request.router.externalGateway.setEnableSnat(null);
            request.router.externalGateway.setFixedIps(
                Lists.newArrayList(new FixedIp(subnetId, ipAddress)));
            return this;
        }

        public Builder withExternalGateway(String networkId, Boolean enableSnat) {
            request.router.externalGateway.setNetworkId(networkId);
            request.router.externalGateway.setEnableSnat(enableSnat);
            return this;
        }

        public Builder withSnatEnabled(Boolean enableSnat) {
            request.router.externalGateway.setEnableSnat(enableSnat);
            return this;
        }

        public CreateRouter get() {
            return request;
        }
    }

    private static class RouterConfiguration {
        private String name;
        @SerializedName("tenant_id")
        private String tenantId;
        @SerializedName("admin_state_up")
        private boolean adminStateUp = true;
        @SerializedName("ha")
        private boolean highlyAvailable = false;
        @SerializedName("external_gateway_info")
        private ExternalGateway externalGateway = new ExternalGateway();
    }
}
