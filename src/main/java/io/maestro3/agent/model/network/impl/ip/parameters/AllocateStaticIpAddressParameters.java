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

import io.maestro3.agent.model.network.impl.DomainType;


public class AllocateStaticIpAddressParameters {
    private DomainType domainType;
    private String fixedIp;
    private String networkId;

    protected AllocateStaticIpAddressParameters(Builder builder) {
        this.domainType = builder.domainType;
        this.fixedIp = builder.fixedIp;
        this.networkId = builder.networkId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public String getFixedIp() {
        return fixedIp;
    }

    public String getNetworkId() {
        return networkId;
    }

    public static class Builder {
        private DomainType domainType;
        private String fixedIp;
        private String networkId;

        protected Builder() {
        }

        public Builder domainType(DomainType domainType) {
            this.domainType = domainType;
            return this;
        }

        public Builder fixedIp(String fixedIp) {
            this.fixedIp = fixedIp;
            return this;
        }

        public Builder networkId(String networkId) {
            this.networkId = networkId;
            return this;
        }

        public AllocateStaticIpAddressParameters build() {
            return new AllocateStaticIpAddressParameters(this);
        }
    }
}
