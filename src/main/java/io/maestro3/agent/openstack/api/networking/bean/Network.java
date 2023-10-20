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


public class Network {

    private String id;
    private String name;
    private NetworkStatus status;
    private List<String> subnets;
    @SerializedName("provider:physical_network")
    private String physicalNetwork;
    @SerializedName("provider:network_type")
    private NetworkType networkType;
    @SerializedName("admin_state_up")
    private boolean adminStateUp;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("router:external")
    private boolean external;
    private boolean shared;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NetworkStatus getStatus() {
        return status;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public String getPhysicalNetwork() {
        return physicalNetwork;
    }

    /**
     * Specify true if the network should be in the up state when created.
     * By default the network is down.
     */
    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    public String getTenantId() {
        return tenantId;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isShared() {
        return shared;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Network{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", status=").append(status);
        sb.append(", subnets=").append(subnets);
        sb.append(", physicalNetwork='").append(physicalNetwork).append('\'');
        sb.append(", adminStateUp=").append(adminStateUp);
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", networkType=").append(networkType);
        sb.append(", external=").append(external);
        sb.append(", shared=").append(shared);
        sb.append('}');
        return sb.toString();
    }
}
