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


public class Port { // port has much more fields, feel free to add some

    @SerializedName("id")
    private String id;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("name")
    private String name;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("fixed_ips")
    private List<FixedIp> fixedIps;
    @SerializedName("status")
    private String status;
    @SerializedName("mac_address")
    private String macAddress;
    @SerializedName("network_id")
    private String networkId;
    @SerializedName("security_groups")
    private List<String> securityGroupIds;

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public List<FixedIp> getFixedIps() {
        return fixedIps;
    }

    public String getStatus() {
        return status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getNetworkId() {
        return networkId;
    }

    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Port{");
        sb.append("id='").append(id).append('\'');
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", fixedIps=").append(fixedIps);
        sb.append(", status='").append(status).append('\'');
        sb.append(", macAddress='").append(macAddress).append('\'');
        sb.append(", networkId='").append(networkId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
