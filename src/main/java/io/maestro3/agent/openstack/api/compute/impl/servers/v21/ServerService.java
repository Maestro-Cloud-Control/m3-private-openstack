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

package io.maestro3.agent.openstack.api.compute.impl.servers.v21;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.compute.bean.BlockDeviceMappingV2;
import io.maestro3.agent.openstack.api.compute.bean.NetworkInfo;
import io.maestro3.agent.openstack.api.compute.bean.Personality;
import io.maestro3.agent.openstack.api.compute.bean.SecurityInfo;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.compute.impl.servers.BaseServerService;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.model.compute.DiskConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerService extends BaseServerService {

    public ServerService(IOSClient client) {
        super(client);
    }

    @Override
    protected Object getBootServerRequest(ServerBootInfo bootInfo) {
        V21ServerBootInfo info = new V21ServerBootInfo();
        info.name = bootInfo.getName();
        info.configDrive = bootInfo.isConfigDrive();
        info.diskConfig = bootInfo.getDiskConfig();
        info.flavorRef = bootInfo.getFlavorRef();
        info.imageRef = bootInfo.getImageRef();
        info.keyName = bootInfo.getKeyName();
        info.metadata = bootInfo.getMetadata();
        info.networks = new ArrayList<>(bootInfo.getNetworks());
        info.personality = bootInfo.getPersonality();
        info.securityGroups = bootInfo.getSecurityGroups();
        info.port = bootInfo.getPort();
        info.userData = bootInfo.getUserData();
        info.adminPass = bootInfo.getAdminPass();
        info.availabilityZone = bootInfo.getAvailabilityZone();
        if (bootInfo.getDeviceMapping() != null) {
            info.deviceMapping = new BlockDeviceMappingV2[]{bootInfo.getDeviceMapping()};
        }
        return info;
    }

    private static class V21ServerBootInfo {
        private String name;
        private String imageRef;
        private String flavorRef;
        private String port;
        private List<NetworkInfo> networks = new ArrayList<>();
        @SerializedName("OS-DCF:diskConfig")
        private DiskConfig diskConfig;
        private Map<String, String> metadata = new HashMap<>();
        @SerializedName("key_name")
        private String keyName;
        private List<Personality> personality;
        @SerializedName("config_drive")
        private boolean configDrive;
        @SerializedName("user_data")
        private String userData;
        @SerializedName("security_groups")
        private List<SecurityInfo> securityGroups = new ArrayList<>();
        private String adminPass;
        @SerializedName("availability_zone")
        private String availabilityZone;
        @SerializedName("block_device_mapping_v2")
        private BlockDeviceMappingV2[] deviceMapping;
    }
}
