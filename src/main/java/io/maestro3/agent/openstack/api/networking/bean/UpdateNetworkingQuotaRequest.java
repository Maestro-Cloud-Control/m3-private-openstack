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


public class UpdateNetworkingQuotaRequest {

    @SerializedName("port")
    private Integer ports;
    @SerializedName("floatingip")
    private Integer floatingIps;

    public static UpdateNetworkingQuotaRequest build() {
        return new UpdateNetworkingQuotaRequest();
    }

    private UpdateNetworkingQuotaRequest() {
    }

    public UpdateNetworkingQuotaRequest withPorts(Integer ports) {
        this.ports = ports;
        return this;
    }

    public UpdateNetworkingQuotaRequest withFloatingIps(Integer floatingIps) {
        this.floatingIps = floatingIps;
        return this;
    }

    public UpdateNetworkingQuotaRequest unlimited() {
        this.ports = -1;
        this.floatingIps = -1;
        return this;
    }
}
