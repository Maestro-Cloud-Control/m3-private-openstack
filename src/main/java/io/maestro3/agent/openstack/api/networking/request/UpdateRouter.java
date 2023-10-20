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
import io.maestro3.agent.openstack.api.networking.bean.ExternalGateway;


public class UpdateRouter {

    private RouterConfiguration router;

    private UpdateRouter(RouterConfiguration router) {
        this.router = router;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private RouterConfiguration routerconfiguration = new RouterConfiguration();

        public Builder name(String name) {
            routerconfiguration.name = name;
            return this;
        }

        public Builder up() {
            routerconfiguration.adminState = true;
            return this;
        }

        public Builder down() {
            routerconfiguration.adminState = false;
            return this;
        }

        public Builder externalGateway(String networkId) {
            return externalGateway(networkId, null);
        }

        public Builder externalGateway(String networkId, Boolean snatEnabled) {
            routerconfiguration.externalGateway.setNetworkId(networkId);
            routerconfiguration.externalGateway.setEnableSnat(snatEnabled);
            return this;
        }

        public UpdateRouter get() {
            return new UpdateRouter(routerconfiguration);
        }
    }

    private static class RouterConfiguration {
        private String name;
        @SerializedName("external_gateway_info")
        private ExternalGateway externalGateway = new ExternalGateway();
        @SerializedName("admin_state_up")
        private Boolean adminState;
    }

}
