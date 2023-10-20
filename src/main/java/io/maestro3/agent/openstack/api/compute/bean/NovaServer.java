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

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.PowerState;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.compute.ServerStatus;
import io.maestro3.agent.model.compute.TaskState;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class NovaServer implements Server {

    private String id;
    private ServerStatus status;
    @SerializedName("OS-EXT-STS:power_state")
    private String powerState;
    @SerializedName("addresses")
    private Map<String, List<NovaAddress>> addressesMap = Maps.newHashMap();
    private NovaAddresses novaAddresses;
    private String name;
    @SerializedName("OS-EXT-STS:task_state")
    private String taskState;
    private Flavor flavor;
    private Object image;
    private Date created;
    private Map<String, String> metadata;
    @SerializedName("key_name")
    private String keyName;
    private List<Personality> personality;
    @SerializedName("security_groups")
    private List<SecurityInfo> securityGroups = new ArrayList<>();
    @SerializedName("tenant_id")
    private String tenantId;
    private Fault fault;
    @SerializedName("OS-EXT-SRV-ATTR:host")
    private String host;
    @SerializedName("OS-EXT-AZ:availability_zone")
    private String availabilityZone;
    @SerializedName("os-extended-volumes:volumes_attached")
    private List<CinderVolume> volumes = new ArrayList<>();



    @Override
    public String getId() {
        return id;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    @Override
    public PowerState getPowerState() {
        return PowerState.forValue(powerState);
    }

    @Override
    public NovaAddresses getAddresses() {
        if (novaAddresses != null) {
            return novaAddresses;
        }
        novaAddresses = NovaAddresses.wrap(addressesMap);
        return novaAddresses;
    }

    @Override
    public Set<String> getNetworkNames() {
        if (MapUtils.isEmpty(addressesMap)) {
            return Collections.emptySet();
        }
        return addressesMap.keySet();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TaskState getTaskState() {
        return TaskState.forValue(taskState);
    }

    @Override
    public String getFlavorId() {
        return flavor == null ? null : flavor.id;
    }

    @Override
    public String getImageId() {
        // Image can be object or a string
        if (image == null) {
            return null;
        }
        if (image instanceof Map<?, ?>) {
            Map<?, ?> imageMap = (Map<?, ?>) image;
            Object id = imageMap.get("id");
            if (id != null) {
                return id.toString();
            }
        } else if (image instanceof String) {
            return (String) image;
        }
        return null;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public List<Personality> getPersonality() {
        return personality;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public Fault getFault() {
        return fault;
    }

    @Override
    public String getHost() {
        return host;
    }

    public List<SecurityInfo> getSecurityGroups() {
        return securityGroups;
    }

    public static class Flavor {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    public void setAddressesMap(Map<String, List<NovaAddress>> addressesMap) {
        this.addressesMap = addressesMap;
    }

    public void setNovaAddresses(NovaAddresses novaAddresses) {
        this.novaAddresses = novaAddresses;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    public void setImage(Object image) {
        this.image = image;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public List<CinderVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<CinderVolume> volumes) {
        this.volumes = volumes;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public void setPersonality(List<Personality> personality) {
        this.personality = personality;
    }

    public void setSecurityGroups(List<SecurityInfo> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    @Override
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("status", status)
                .append("powerState", powerState)
                .append("addressesMap", addressesMap)
                .append("novaAddresses", novaAddresses)
                .append("name", name)
                .append("taskState", taskState)
                .append("flavor", flavor)
                .append("image", image)
                .append("created", created)
                .append("metadata", metadata)
                .append("keyName", keyName)
                .append("personality", personality)
                .append("securityGroups", securityGroups)
                .append("host", host)
                .append("availabilityZone", availabilityZone)
                .toString();
    }
}
