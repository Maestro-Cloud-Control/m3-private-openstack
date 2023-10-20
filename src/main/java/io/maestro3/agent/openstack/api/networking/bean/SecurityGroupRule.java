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


public class SecurityGroupRule {

    @SerializedName("id")
    private String id;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("security_group_id")
    private String securityGroupId;
    @SerializedName("direction")
    private String direction;
    @SerializedName("ethertype")
    private String etherType;
    @SerializedName("port_range_max")
    private Integer portRangeMax;
    @SerializedName("port_range_min")
    private Integer portRangeMin;
    @SerializedName("protocol")
    private String protocol;
    @SerializedName("remote_ip_prefix")
    private String remoteIpPrefix;
    @SerializedName("remote_group_id")
    private String remoteGroupId;

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public String getDirection() {
        return direction;
    }

    public String getEtherType() {
        return etherType;
    }

    public Integer getPortRangeMax() {
        return portRangeMax;
    }

    public Integer getPortRangeMin() {
        return portRangeMin;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRemoteIpPrefix() {
        return remoteIpPrefix;
    }

    public String getRemoteGroupId() {
        return remoteGroupId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SecurityGroupRule{");
        sb.append("id='").append(id).append('\'');
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", securityGroupId='").append(securityGroupId).append('\'');
        sb.append(", direction='").append(direction).append('\'');
        sb.append(", etherType='").append(etherType).append('\'');
        sb.append(", portRangeMax=").append(portRangeMax);
        sb.append(", portRangeMin=").append(portRangeMin);
        sb.append(", protocol='").append(protocol).append('\'');
        sb.append(", remoteIpPrefix='").append(remoteIpPrefix).append('\'');
        sb.append(", remoteGroupId='").append(remoteGroupId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
