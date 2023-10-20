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


public class Router {

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("status")
    private RouterState status;
    @SerializedName("admin_state_up")
    private boolean adminStateUp;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("external_gateway_info")
    private ExternalGateway externalGateway;
    @SerializedName("routes")
    private List<HostRoute> routes;

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

    public RouterState getStatus() {
        return status;
    }

    public void setStatus(RouterState status) {
        this.status = status;
    }

    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    public void setAdminStateUp(boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public ExternalGateway getExternalGateway() {
        return externalGateway;
    }

    public void setExternalGateway(ExternalGateway externalGateway) {
        this.externalGateway = externalGateway;
    }

    public List<HostRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<HostRoute> routes) {
        this.routes = routes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Router{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", status=").append(status);
        sb.append(", adminStateUp=").append(adminStateUp);
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", externalGatewayInfo=").append(externalGateway);
        sb.append(", routes=").append(routes);
        sb.append('}');
        return sb.toString();
    }
}
