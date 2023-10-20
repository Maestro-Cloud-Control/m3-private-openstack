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


public class AllocateFloatingIpAddressParameters extends AllocateStaticIpAddressParameters {
    private String portId;
    private String reservedInstanceId;
    private String floatingIp;

    private AllocateFloatingIpAddressParameters(AllocateFloatingIpAddressParametersBuilder builder) {
        super(builder);
        this.portId = builder.portId;
        this.reservedInstanceId = builder.reservedInstanceId;
        this.floatingIp = builder.floatingIp;
    }

    public static AllocateFloatingIpAddressParametersBuilder builder() {
        return new AllocateFloatingIpAddressParametersBuilder();
    }

    public String getPortId() {
        return portId;
    }

    public String getReservedInstanceId() {
        return reservedInstanceId;
    }

    public String getFloatingIp() {
        return floatingIp;
    }

    public static class AllocateFloatingIpAddressParametersBuilder extends Builder {
        private String portId;
        private String reservedInstanceId;
        private String floatingIp;

        private AllocateFloatingIpAddressParametersBuilder() {
            super();
        }

        public AllocateFloatingIpAddressParametersBuilder portId(String portId) {
            this.portId = portId;
            return this;
        }

        public AllocateFloatingIpAddressParametersBuilder reservedInstanceId(String reservedInstanceId) {
            this.reservedInstanceId = reservedInstanceId;
            return this;
        }

        public AllocateFloatingIpAddressParametersBuilder floatingIp(String floatingIp) {
            this.floatingIp = floatingIp;
            return this;
        }

        @Override
        public AllocateFloatingIpAddressParametersBuilder domainType(DomainType domainType) {
            super.domainType(domainType);
            return this;
        }

        @Override
        public AllocateFloatingIpAddressParametersBuilder fixedIp(String fixedIp) {
            super.fixedIp(fixedIp);
            return this;
        }

        @Override
        public AllocateFloatingIpAddressParametersBuilder networkId(String networkId) {
            super.networkId(networkId);
            return this;
        }

        @Override
        public AllocateFloatingIpAddressParameters build() {
            return new AllocateFloatingIpAddressParameters(this);
        }
    }
}
