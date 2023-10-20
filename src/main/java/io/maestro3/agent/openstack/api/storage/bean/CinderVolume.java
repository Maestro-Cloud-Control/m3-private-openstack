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

package io.maestro3.agent.openstack.api.storage.bean;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.OpenStackResource;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;


public class CinderVolume implements OpenStackResource {
    public static final String ID_FIELD = "id";

    @Field(ID_FIELD)
    @SerializedName(ID_FIELD)
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("size")
    private int size;
    @SerializedName("status")
    private String status;
    @SerializedName("attachments")
    private List<CinderVolumeAttachment> attachments;
    @SerializedName("availability_zone")
    private String availabilityZone;
    @SerializedName("os-vol-host-attr:host")
    private String host;
    @SerializedName("volume_type")
    private String volumeType;
    @SerializedName("bootable")
    private boolean bootable;
    @SerializedName("tenant")
    private String tenant;
    @SerializedName("region")
    private String region;
    private Map<String, String> metadata;

    public boolean isBootable() {
        return bootable;
    }

    public void setBootable(boolean bootable) {
        this.bootable = bootable;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CinderVolumeAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CinderVolumeAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "CinderVolume{" + "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", status='" + status + '\'' +
                ", attachments=" + attachments +
                ", availabilityZone='" + availabilityZone + '\'' +
                ", host='" + host + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
