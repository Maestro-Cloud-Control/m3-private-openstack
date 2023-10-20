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


public class UpdateNetwork {

    private NetworkConfiguration network;

    private UpdateNetwork(NetworkConfiguration network) {
        this.network = network;
    }

    public static Builder network() {
        return new Builder();
    }

    public static class Builder {

        private NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        public Builder name(String name) {
            networkConfiguration.name = name;
            return this;
        }

        public Builder up() {
            networkConfiguration.adminState = true;
            return this;
        }

        public Builder down() {
            networkConfiguration.adminState = false;
            return this;
        }

        public Builder shared() {
            networkConfiguration.shared = true;
            return this;
        }

        public UpdateNetwork get() {
            return new UpdateNetwork(networkConfiguration);
        }
    }

    private static class NetworkConfiguration {
        private String name;
        @SerializedName("admin_state_up")
        private Boolean adminState;
        private Boolean shared;
    }
}
