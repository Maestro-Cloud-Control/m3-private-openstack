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
import io.maestro3.agent.openstack.api.networking.bean.FixedIp;

import java.util.List;


public class ExternalGateway {

    @SerializedName("network_id")
    private String networkId;
    @SerializedName("enable_snat")
    private Boolean enableSnat; // Enable Source NAT (SNAT) attribute. Default is True.

    @SerializedName("external_fixed_ips")
    private List<FixedIp> fixedIps;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Boolean isEnableSnat() {
        return enableSnat;
    }

    public void setEnableSnat(Boolean enableSnat) {
        this.enableSnat = enableSnat;
    }

    public List<FixedIp> getFixedIps() {
        return fixedIps;
    }

    public void setFixedIps(List<FixedIp> fixedIps) {
        this.fixedIps = fixedIps;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExternalGatewayInfo{");
        sb.append("networkId='").append(networkId).append('\'');
        sb.append(", enableSnat=").append(enableSnat);
        sb.append('}');
        return sb.toString();
    }
}
