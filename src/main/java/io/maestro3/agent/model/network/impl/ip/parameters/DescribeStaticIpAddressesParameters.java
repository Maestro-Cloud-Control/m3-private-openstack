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

import java.util.List;


public class DescribeStaticIpAddressesParameters {
    private DomainType domainType;
    private List<String> instanceIds;
    private List<String> staticIps;

    private DescribeStaticIpAddressesParameters(Builder builder) {
        this.domainType = builder.domainType;
        this.instanceIds = builder.instanceIds;
        this.staticIps = builder.staticIps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public List<String> getInstanceIds() {
        return instanceIds;
    }

    public List<String> getStaticIps() {
        return staticIps;
    }

    public static class Builder {
        private DomainType domainType;
        private List<String> instanceIds;
        private List<String> staticIps;

        private Builder() {
        }

        public Builder domainType(DomainType domainType) {
            this.domainType = domainType;
            return this;
        }

        public Builder instanceIds(List<String> instanceIds) {
            this.instanceIds = instanceIds;
            return this;
        }

        public Builder staticIps(List<String> staticIps) {
            this.staticIps = staticIps;
            return this;
        }

        public DescribeStaticIpAddressesParameters build() {
            return new DescribeStaticIpAddressesParameters(this);
        }
    }
}
