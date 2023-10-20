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


public class CreateNetwork {

    private NetworkConfiguration network = new NetworkConfiguration();

    private CreateNetwork() {
    }

    public static Builder network() {
        return new Builder();
    }

    public static class Builder {

        private CreateNetwork request = new CreateNetwork();

        public Builder withName(String name) {
            request.network.name = name;
            return this;
        }

        public Builder forProject(String tenantId) {
            request.network.tenantId = tenantId;
            return this;
        }

        public Builder down() {
            request.network.adminStateUp = false;
            return this;
        }

        public Builder shared() {
            request.network.shared = true;
            return this;
        }

        public CreateNetwork get() {
            return request;
        }
    }

    private static class NetworkConfiguration {
        private String name;
        @SerializedName("tenant_id")
        private String tenantId;
        @SerializedName("admin_state_up")
        private boolean adminStateUp = true;
        private boolean shared;
    }
}
