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

package io.maestro3.agent.model.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.maestro3.agent.model.network.impl.AutoNetworkingPolicy;
import io.maestro3.agent.model.network.impl.ManualNetworkingPolicy;
import io.maestro3.agent.model.network.impl.dns.RegionDnsConfiguration;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "networkingType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AutoNetworkingPolicy.class, name = "AUTO"),
    @JsonSubTypes.Type(value = ManualNetworkingPolicy.class, name = "MANUAL")})
public class NetworkingPolicy {
    private NetworkingType networkingType;
    private String networkId;
    private RegionDnsConfiguration regionDnsConfiguration = new RegionDnsConfiguration();

    public NetworkingPolicy() {
    }

    public NetworkingPolicy(NetworkingType networkingType) {
        this.networkingType = networkingType;
    }

    public NetworkingType getNetworkingType() {
        return networkingType;
    }

    public void setNetworkingType(NetworkingType networkingType) {
        this.networkingType = networkingType;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public RegionDnsConfiguration getRegionDnsConfiguration() {
        return regionDnsConfiguration;
    }

    public void setRegionDnsConfiguration(RegionDnsConfiguration regionDnsConfiguration) {
        this.regionDnsConfiguration = regionDnsConfiguration;
    }
}
