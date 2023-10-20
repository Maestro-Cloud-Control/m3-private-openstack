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

public class TenantNetworkCreationInputParameters {

    private String tenantName;
    private String networkName;
    private String cidr;

    private String gatewayNetworkId;
    private String subnetId;
    private String ipAddress;
    private boolean highlyAvailable;
    private boolean disableSnat;

    public String getTenantName() {
        return tenantName;
    }

    public TenantNetworkCreationInputParameters setTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getCidr() {
        return cidr;
    }

    public TenantNetworkCreationInputParameters setCidr(String cidr) {
        this.cidr = cidr;
        return this;
    }

    public String getGatewayNetworkId() {
        return gatewayNetworkId;
    }

    public TenantNetworkCreationInputParameters setGatewayNetworkId(String gatewayNetworkId) {
        this.gatewayNetworkId = gatewayNetworkId;
        return this;
    }

    public boolean isHighlyAvailable() {
        return highlyAvailable;
    }

    public TenantNetworkCreationInputParameters setHighlyAvailable(boolean highlyAvailable) {
        this.highlyAvailable = highlyAvailable;
        return this;
    }

    public boolean isDisableSnat() {
        return disableSnat;
    }

    public TenantNetworkCreationInputParameters setDisableSnat(boolean disableSnat) {
        this.disableSnat = disableSnat;
        return this;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public TenantNetworkCreationInputParameters setSubnetId(String subnetId) {
        this.subnetId = subnetId;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public TenantNetworkCreationInputParameters setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public String getNetworkName() {
        return networkName;
    }

    public TenantNetworkCreationInputParameters setNetworkName(String networkName) {
        this.networkName = networkName;
        return this;
    }
}
