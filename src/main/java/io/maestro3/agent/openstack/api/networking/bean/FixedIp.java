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


public class FixedIp {

    @SerializedName("subnet_id")
    private String subnetId;
    @SerializedName("ip_address")
    private String ipAddress;

    public FixedIp() {
    }

    public FixedIp(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public FixedIp(String subnetId, String ipAddress) {
        this.subnetId = subnetId;
        this.ipAddress = ipAddress;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FixedIp{");
        sb.append("subnetId='").append(subnetId).append('\'');
        sb.append(", ipAddress='").append(ipAddress).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
