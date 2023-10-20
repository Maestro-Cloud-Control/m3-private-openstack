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

package io.maestro3.agent.openstack.api.compute.bean;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.DiskConfig;
import io.maestro3.agent.model.compute.Flavor;
import io.maestro3.agent.model.compute.Image;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.util.Asserts;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ServerBootInfo {

    private String name;
    private String imageRef;
    private String flavorRef;
    private String adminPass;
    private String port;
    private Set<NetworkInfo> networks = new HashSet<>();
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
    @SerializedName("availability_zone")
    private String availabilityZone;
    @SerializedName("os:scheduler_hints")
    private Map<String, String> schedulerHints;
    @SerializedName("block_device_mapping_v2")
    private BlockDeviceMappingV2 deviceMapping;

    public BlockDeviceMappingV2 getDeviceMapping() {
        return deviceMapping;
    }

    public String getName() {
        return name;
    }

    public String getImageRef() {
        return imageRef;
    }

    public String getFlavorRef() {
        return flavorRef;
    }

    public String getPort() {
        return port;
    }

    public Set<NetworkInfo> getNetworks() {
        return networks;
    }

    public DiskConfig getDiskConfig() {
        return diskConfig;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getKeyName() {
        return keyName;
    }

    public List<Personality> getPersonality() {
        return personality;
    }

    public boolean isConfigDrive() {
        return configDrive;
    }

    public String getUserData() {
        return userData;
    }

    public List<SecurityInfo> getSecurityGroups() {
        return securityGroups;
    }

    public String getAdminPass() {
        return adminPass;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * Get server boot info builder
     *
     * @return boot info builder
     */
    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private ServerBootInfo server = new ServerBootInfo();
        private Map<String, String> schedulerHints = new LinkedHashMap<>();

        /**
         * Set server name
         *
         * @param name server name
         * @return server boot info
         */
        public Builder server(String name) {
            server.name = name;
            return this;
        }

        /**
         * Set server image
         *
         * @param image server image
         * @return server boot info
         */
        public Builder ofImage(Image image) {
            server.imageRef = image.getId();
            return this;
        }

        public Builder ofImage(String id) {
            server.imageRef = id;
            return this;
        }

        /**
         * Set server flavor
         *
         * @param flavor flavor of a server
         * @return server boot info
         */
        public Builder withFlavor(Flavor flavor) {
            server.flavorRef = flavor.getId();
            return this;
        }

        public Builder withDeviceMapping(BlockDeviceMappingV2 mapping) {
            server.deviceMapping = mapping;
            return this;
        }

        /**
         * Set server flavor id
         *
         * @param id flavor id
         * @return server boot info
         */
        public Builder withFlavor(String id) {
            server.flavorRef = id;
            return this;
        }

        /**
         * Associate server with networks.
         */
        public Builder inNetworks(Set<String> networkIds) {
            Asserts.notNull(networkIds, "networkIds can not be empty");

            for (String uuid : networkIds) {
                server.networks.add(NetworkInfo.uuid(uuid));
            }
            return this;
        }

        public Builder inNetwork(String networkId) {
            Asserts.notNull(networkId, "networkId can not be empty");
            server.networks.add(NetworkInfo.uuid(networkId));
            return this;
        }

        public Builder withPort(String portId) {
            Asserts.notNull(portId, "portId can not be empty");
            server.networks.add(NetworkInfo.port(portId));
            return this;
        }

        /**
         * One or more security groups. Specify the name of the security group in the <b>name</b> attribute.
         * If you omit this attribute, the server is created in the <b>default</b> security group.
         */
        public Builder inSecurityGroups(Set<String> securityGroupNames) {
            Asserts.notNull(securityGroupNames, "networkIds can not be empty");

            for (String name : securityGroupNames) {
                server.securityGroups.add(new SecurityInfo(name));
            }
            return this;
        }

        /**
         * Appends SSH key name.
         *
         * @param keyName SSH key name
         * @return server boot info
         */
        public Builder withKey(String keyName) {
            server.keyName = keyName;
            return this;
        }

        /**
         * Add server personality.
         *
         * @param personality server personality
         * @return server boot info
         * @see Personality
         */
        public Builder withPersonalty(Personality personality) {
            if (personality != null) {
                server.personality = Collections.singletonList(personality);
            }
            return this;
        }

        /**
         * Add disk config.
         *
         * @param diskConfig disk config
         * @return server boot info
         * @see DiskConfig
         */
        public Builder withDiskConfig(DiskConfig diskConfig) {
            if (diskConfig != null) {
                server.diskConfig = diskConfig;
            }
            return this;
        }

        /**
         * Inject meta information into the server.
         *
         * @param key   meta key
         * @param value meta value
         * @return server boot info
         */
        public Builder withMeta(String key, String value) {
            server.metadata.put(key, value);
            return this;
        }

        /**
         * <b>Config Drive</b> is special configuration drive that will be attached to the instance when it boots.<br/>
         * The instance can retrieve any information that would normally be available through the <b>metadata service</b> by mounting this disk and reading files from it.
         *
         * @return server boot info
         */
        public Builder useConfigDrive() {
            server.configDrive = true;
            return this;
        }

        /**
         * Configuration information or scripts to use upon launch. Must be Base64 encoded.
         *
         * @param userData user data
         * @return server boot info
         */
        public Builder withUserData(String userData) {
            server.userData = userData;
            return this;
        }

        public Builder port(String portId) {
            server.port = portId;
            return this;
        }

        public Builder withAdminPass(String adminPass) {
            server.adminPass = adminPass;
            return this;
        }

        public Builder withAvailabilityZone(String availabilityZone) {
            server.availabilityZone = availabilityZone;
            return this;
        }

        public Builder withSchedulerHint(String key, String value) {
            Assert.notNull(key, "key cannot be null or empty.");
            Assert.notNull(value, "value cannot be null or empty.");
            schedulerHints.put(key, value);
            return this;
        }

        /**
         * Creates new previously configured server boot info.
         *
         * @return server boot info to create new server
         */
        public ServerBootInfo create() {
            if (MapUtils.isNotEmpty(schedulerHints)) {
                server.schedulerHints = schedulerHints;
            }
            return server;
        }
    }
}
