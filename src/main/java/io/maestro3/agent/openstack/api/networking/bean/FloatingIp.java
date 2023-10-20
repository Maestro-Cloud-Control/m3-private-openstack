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


public class FloatingIp {

    @SerializedName("id")
    private String id;

    @SerializedName("router_id")
    private String routerId;

    @SerializedName("tenant_id")
    private String tenantId;

    @SerializedName("floating_network_id")
    private String floatingNetworkId;

    @SerializedName("floating_ip_address")
    private String floatingIpAddress;

    @SerializedName("fixed_ip_address")
    private String fixedIpAddress;

    @SerializedName("port_id")
    private String portId;

    @SerializedName("status")
    private String status;

    public String getFloatingNetworkId() {
        return floatingNetworkId;
    }

    public void setFloatingNetworkId(String floatingNetworkId) {
        this.floatingNetworkId = floatingNetworkId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFloatingIpAddress() {
        return floatingIpAddress;
    }

    public void setFloatingIpAddress(String floatingIpAddress) {
        this.floatingIpAddress = floatingIpAddress;
    }

    public String getFixedIpAddress() {
        return fixedIpAddress;
    }

    public void setFixedIpAddress(String fixedIpAddress) {
        this.fixedIpAddress = fixedIpAddress;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FloatingIp{" +
                "id='" + id + '\'' +
                ", routerId='" + routerId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", floatingNetworkId='" + floatingNetworkId + '\'' +
                ", floatingIpAddress='" + floatingIpAddress + '\'' +
                ", fixedIpAddress='" + fixedIpAddress + '\'' +
                ", portId='" + portId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
