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

package io.maestro3.agent.converter;

import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackSecurityGroupInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;

import java.util.Collections;


public final class M3PrivateAgentModelConverter {

    private M3PrivateAgentModelConverter() {
        throw new UnsupportedOperationException();
    }

    public static OpenStackServerConfig toOpenStackServerConfig(OpenStackTenant tenant, Server server) {
        OpenStackServerConfig serverConfig = new OpenStackServerConfig();
        serverConfig.setNameAlias(server.getName());
        serverConfig.setNativeId(server.getId());
        serverConfig.setMetadata(server.getMetadata());
        serverConfig.setNativeName(server.getName());
        serverConfig.setFlavorId(server.getFlavorId());
        serverConfig.setImageId(server.getImageId());
        serverConfig.setTenantId(tenant.getId());
        serverConfig.setRegionId(tenant.getRegionId());
        serverConfig.setAvailabilityZone(server.getAvailabilityZone());
        serverConfig.setStartTime(server.getCreated().getTime());
        SdkInstanceState sdkServerState = OpenStackServerStateDetector.toM3SdkInstanceState(
                server.getStatus(), server.getPowerState(), server.getTaskState());
        serverConfig.setState(ServerStateEnum.forSdkServerState(sdkServerState));

        OpenStackNetworkInterfaceInfo networkInterfaceInfo = new OpenStackNetworkInterfaceInfo();
        networkInterfaceInfo.setNetworkId(tenant.getNetworkId());
        OpenStackSecurityGroupInfo securityGroupInfo = new OpenStackSecurityGroupInfo();
        securityGroupInfo.setNativeName(tenant.getSecurityGroupName());
        securityGroupInfo.setNativeId(tenant.getSecurityGroupId());
        networkInterfaceInfo.setSecurityGroupInfos(Collections.singletonList(securityGroupInfo));

        serverConfig.setNetworkInterfaceInfo(networkInterfaceInfo);

        return serverConfig;
    }
}
