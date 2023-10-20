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

package io.maestro3.agent.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackNetworkInterfaceInfo {

    private String networkId;
    private String privateIP;
    private String privateDns;
    private String publicIp;
    private String publicDns;
    private String vlanName;

    public String getVlanName() {
        return vlanName;
    }

    public OpenStackNetworkInterfaceInfo setVlanName(String vlanName) {
        this.vlanName = vlanName;
        return this;
    }

    private Collection<OpenStackSecurityGroupInfo> securityGroupInfos;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public String getPrivateDns() {
        return privateDns;
    }

    public void setPrivateDns(String privateDns) {
        this.privateDns = privateDns;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPublicDns() {
        return publicDns;
    }

    public void setPublicDns(String publicDns) {
        this.publicDns = publicDns;
    }

    public Collection<OpenStackSecurityGroupInfo> getSecurityGroupInfos() {
        return securityGroupInfos;
    }

    public void setSecurityGroupInfos(Collection<OpenStackSecurityGroupInfo> securityGroupInfos) {
        this.securityGroupInfos = securityGroupInfos;
    }
}
