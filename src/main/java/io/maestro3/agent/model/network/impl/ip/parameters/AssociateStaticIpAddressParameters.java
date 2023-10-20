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

package io.maestro3.agent.model.network.impl.ip.parameters;

import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;


public class AssociateStaticIpAddressParameters {
    private String instanceId;
    private StaticIpAddress staticIpAddress; //null to associate any static ip

    private AssociateStaticIpAddressParameters(Builder builder) {
        this.instanceId = builder.instanceId;
        this.staticIpAddress = builder.staticIpAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getInstanceId() {
        return instanceId;
    }

    public StaticIpAddress getStaticIpAddress() {
        return staticIpAddress;
    }

    public static class Builder {
        private String instanceId;
        private StaticIpAddress staticIpAddress;

        private Builder() {
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder staticIpAddress(StaticIpAddress staticIpAddress) {
            this.staticIpAddress = staticIpAddress;
            return this;
        }

        public AssociateStaticIpAddressParameters build() {
            return new AssociateStaticIpAddressParameters(this);
        }
    }
}
