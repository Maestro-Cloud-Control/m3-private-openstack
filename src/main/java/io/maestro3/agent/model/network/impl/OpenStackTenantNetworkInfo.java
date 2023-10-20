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

package io.maestro3.agent.model.network.impl;

public class OpenStackTenantNetworkInfo {

    private String networkName;
    private String networkId;
    private String subnetName;
    private String subnetCidr;
    private String gatewayNetworkId;
    private String gatewayExternalIp;

    private OpenStackTenantNetworkInfo() {}

    public String getNetworkName() {
        return networkName;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public String getGatewayNetworkId() {
        return gatewayNetworkId;
    }

    public String getGatewayExternalIp() {
        return gatewayExternalIp;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        OpenStackTenantNetworkInfo networkInfo = new OpenStackTenantNetworkInfo();

        public Builder networkId(String networkId) {
            networkInfo.networkId = networkId;
            return this;
        }

        public Builder networkName(String networkName) {
            networkInfo.networkName = networkName;
            return this;
        }

        public Builder subnetName(String subnetName) {
            networkInfo.subnetName = subnetName;
            return this;
        }

        public Builder subnetCidr(String subnetCidr) {
            networkInfo.subnetCidr = subnetCidr;
            return this;
        }

        public Builder gatewayNetworkId(String gatewayNetworkId) {
            networkInfo.gatewayNetworkId = gatewayNetworkId;
            return this;
        }

        public Builder gatewayExternalIp(String ipAddress) {
            networkInfo.gatewayExternalIp = ipAddress;
            return this;
        }

        public OpenStackTenantNetworkInfo get() {
            return networkInfo;
        }
    }
}
