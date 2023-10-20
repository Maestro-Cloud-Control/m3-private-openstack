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

package io.maestro3.agent.openstack.api.networking.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class NovaSubnet {

    private String id;
    private String name;
    @SerializedName("enable_dhcp")
    private boolean enableDHCP;
    @SerializedName("network_id")
    private String networkId;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("dns_nameservers")
    private List<String> dnsNames;
    @SerializedName("ip_version")
    private int ipVersion;
    @SerializedName("allocation_pools")
    List<NovaAddressPool> allocationPools;
    @SerializedName("host_routes")
    private List<String> hostRoutes;
    @SerializedName("gateway_ip")
    private String gateway;
    private String cidr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnableDHCP() {
        return enableDHCP;
    }

    public void setEnableDHCP(boolean enableDHCP) {
        this.enableDHCP = enableDHCP;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getDnsNames() {
        return dnsNames;
    }

    public void setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
    }

    public IpVersion getIpVersion() {
        return IpVersion.encode(ipVersion);
    }

    public void setIpVersion(IpVersion ipVersion) {
        this.ipVersion = ipVersion.getVersion();
    }

    public List<NovaAddressPool> getAllocationPools() {
        return allocationPools;
    }

    public void setAllocationPools(List<NovaAddressPool> allocationPools) {
        this.allocationPools = allocationPools;
    }

    public List<String> getHostRoutes() {
        return hostRoutes;
    }

    public void setHostRoutes(List<String> hostRoutes) {
        this.hostRoutes = hostRoutes;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subnet{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", enableDHCP=").append(enableDHCP);
        sb.append(", networkId='").append(networkId).append('\'');
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", dnsNames=").append(dnsNames);
        sb.append(", ipVersion=").append(IpVersion.encode(ipVersion));
        sb.append(", allocationPools=").append(allocationPools);
        sb.append(", hostRoutes=").append(hostRoutes);
        sb.append(", gateway='").append(gateway).append('\'');
        sb.append(", cidr='").append(cidr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
