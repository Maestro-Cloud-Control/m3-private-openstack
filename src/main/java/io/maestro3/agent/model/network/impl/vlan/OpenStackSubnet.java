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

package io.maestro3.agent.model.network.impl.vlan;


public class OpenStackSubnet extends SubnetModel {

    private String tenantId;
    private String name;
    private int ipVersion;
    private String gatewayIp;
    private Boolean dhcpEnabled;
    private Boolean dmz;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public Boolean isDhcpEnabled() {
        return dhcpEnabled;
    }

    public void setDhcpEnabled(Boolean dhcpEnabled) {
        this.dhcpEnabled = dhcpEnabled;
    }

    public Boolean isDmz() {
        return dmz;
    }

    public void setDmz(Boolean dmz) {
        this.dmz = dmz;
    }

    @Override
    public String toString() {
        return "OpenStackSubnet{" +
                "name='" + name + '\'' +
                "tenantId='" + tenantId + '\'' +
                ", gatewayIp='" + gatewayIp + '\'' +
                ", dmz='" + dmz + '\'' +
                ", dhcpEnabled=" + dhcpEnabled +
                "} " + super.toString();
    }
}
