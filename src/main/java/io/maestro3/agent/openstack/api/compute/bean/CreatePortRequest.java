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

package io.maestro3.agent.openstack.api.compute.bean;

import org.springframework.util.Assert;


public class CreatePortRequest {

    private String networkId;
    private String securityGroupId;
    private String macAddress;
    private String ipAddress;
    private String portName;

    public static Builder build() {
        return new Builder();
    }

    private CreatePortRequest() {
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPortName() {
        return portName;
    }

    public static class Builder {
        private String networkId;
        private String securityGroupId;
        private String macAddress;
        private String ipAddress;
        private String portName;

        private Builder() {
        }

        public Builder withNetworkId(String networkId) {
            Assert.hasText(networkId, "networkId cannot be null or empty.");
            this.networkId = networkId;
            return this;
        }

        public Builder withSecurityGroupId(String securityGroupId) {
            Assert.hasText(securityGroupId, "securityGroupId cannot be null or empty.");
            this.securityGroupId = securityGroupId;
            return this;
        }

        public Builder withPortName(String portName) {
            this.portName = portName;
            return this;
        }

        public Builder withMacAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public CreatePortRequest get() {
            Assert.hasText(networkId, "networkId cannot be null or empty.");
            Assert.hasText(securityGroupId, "securityGroupId cannot be null or empty.");

            CreatePortRequest request = new CreatePortRequest();
            request.networkId = networkId;
            request.securityGroupId = securityGroupId;
            request.macAddress = macAddress;
            request.portName = portName;
            request.ipAddress = ipAddress;
            return request;
        }
    }
}
