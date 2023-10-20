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


public class AddRouterInterface {

    @SerializedName("subnet_id")
    private String subnetId;

    @SerializedName("portId")
    private String portId;

    private AddRouterInterface() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private AddRouterInterface configuration = new AddRouterInterface();

        public Builder subnet(String subnetId) {
            if (configuration.portId != null) {
                throw new IllegalArgumentException("cannot specify both subnet and port");
            }
            configuration.subnetId = subnetId;
            return this;
        }

        public Builder port(String portId) {
            if (configuration.subnetId != null) {
                throw new IllegalArgumentException("cannot specify both subnet and port");
            }
            configuration.portId = portId;
            return this;
        }

        public AddRouterInterface get() {
            return configuration;
        }
    }

}
