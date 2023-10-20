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

import io.maestro3.agent.model.base.VLAN;

import java.util.Set;


public class OpenStackVLAN extends VLAN {

    private String openStackNetworkId;
    private boolean securityGroupDisabled;
    private String gatewayExternalIp;
    private boolean sdn;
    private Set<String> cidrs;

    public String getOpenStackNetworkId() {
        return openStackNetworkId;
    }

    public void setOpenStackNetworkId(String openStackNetworkId) {
        this.openStackNetworkId = openStackNetworkId;
    }

    public boolean isSecurityGroupDisabled() {
        return securityGroupDisabled;
    }

    public void setSecurityGroupDisabled(boolean securityGroupDisabled) {
        this.securityGroupDisabled = securityGroupDisabled;
    }

    public String getGatewayExternalIp() {
        return gatewayExternalIp;
    }

    public void setGatewayExternalIp(String gatewayExternalIp) {
        this.gatewayExternalIp = gatewayExternalIp;
    }

    public boolean isSdn() {
        return sdn;
    }

    public void setSdn(boolean sdn) {
        this.sdn = sdn;
    }

    public Set<String> getCidrs() {
        return cidrs;
    }

    public void setCidrs(Set<String> cidrs) {
        this.cidrs = cidrs;
    }

    @Override
    public String toString() {
        return "OpenStackVLAN{" +
            "openStackNetworkId='" + openStackNetworkId + '\'' +
            ", securityGroupDisabled=" + securityGroupDisabled +
            ", gatewayExternalIp='" + gatewayExternalIp + '\'' +
            ", sdn=" + sdn +
            ", cidrs=" + cidrs +
            '}';
    }
}
