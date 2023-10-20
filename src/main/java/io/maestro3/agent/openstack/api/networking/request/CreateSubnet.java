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

package io.maestro3.agent.openstack.api.networking.request;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.IpVersion;
import io.maestro3.agent.openstack.api.networking.bean.NovaAddressPool;
import org.apache.http.util.Asserts;


public class CreateSubnet {

    private SubnetConfiguration subnet;

    private CreateSubnet(SubnetConfiguration subnet) {
        this.subnet = subnet;
    }

    public static Builder subnet() {
        return new Builder();
    }

    public static class Builder {

        private SubnetConfiguration subnet = new SubnetConfiguration();

        public Builder withName(String name) {
            subnet.name = name;
            return this;
        }

        public Builder inNetwork(String networkId) {
            subnet.networkId = networkId;
            return this;
        }

        public Builder inProject(String tenantId) {
            subnet.tenantId = tenantId;
            return this;
        }

        public Builder withPool(NovaAddressPool... pool) {
            subnet.pool = pool;
            return this;
        }

        public Builder ofVersion(IpVersion ipVersion) {
            if (ipVersion != null) {
                subnet.ipVersion = ipVersion.getVersion();
            }
            return this;
        }

        public Builder withGateway(String gateway) {
            subnet.gateway = gateway;
            return this;
        }

        public Builder enableDHCP() {
            subnet.enableDHCP = true;
            return this;
        }

        public Builder withDNSService(String... dnsService) {
            subnet.dnsNames = dnsService;
            return this;
        }

        public Builder withCidr(String cidr) {
            subnet.cidr = cidr;
            return this;
        }

        public CreateSubnet get() {
            Asserts.notBlank(subnet.networkId, "Network ID is required");
            Asserts.notBlank(subnet.cidr, "CIDR is required");
            return new CreateSubnet(subnet);
        }
    }

    private static class SubnetConfiguration {
        private String name;
        @SerializedName("network_id")
        private String networkId;
        @SerializedName("tenant_id")
        private String tenantId;
        @SerializedName("allocation_pools")
        private NovaAddressPool[] pool;
        @SerializedName("ip_version")
        private int ipVersion;
        @SerializedName("enable_dhcp")
        private boolean enableDHCP;
        @SerializedName("dns_nameservers")
        private String[] dnsNames;
        @SerializedName("gateway_ip")
        private String gateway;
        private String cidr;
    }
}
