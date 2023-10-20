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
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.compute.ImageStatus;
import io.maestro3.agent.model.compute.ImageType;

import java.util.Date;
import java.util.List;


public class NovaImage implements Image {

    private String id;
    private ImageStatus status;
    private String name;
    private String description;
    private String owner;
    private long size;

    @SerializedName("base_image_ref")
    private String baseImageId;
    @SerializedName("created_at")
    private Date created;
    @SerializedName("updated_at")
    private Date updated;
    @SerializedName("image_type")
    private ImageType imageType;
    @SerializedName("min_disk")
    private int minDiskSize;
    @SerializedName("min_ram")
    private int minRamSize;

    // Custom Image properties
    @SerializedName("basedon")
    private String basedOnRepositoryUrl;    // URI/URL to image in public repository
    @SerializedName("revision")
    private String revision;                // some original id in public repository
    @SerializedName("builddate")
    private String buildImageDate;          // date of build/customization image for EPC

    private List<NovaBlockStorageMapping> blockStorageMappings;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getBaseImageId() {
        return baseImageId;
    }

    @Override
    public ImageStatus getStatus() {
        if (status == null) {
            return ImageStatus.UNKNOWN;
        }
        return status;
    }

    @Override
    public ImageType getType() {
        if (imageType == null) {
            return ImageType.UNKNOWN;
        }
        return imageType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getMinDiskSize() {
        return minDiskSize;
    }

    @Override
    public int getMinRamSize() {
        return minRamSize;
    }

    @Override
    public List<NovaBlockStorageMapping> getBlockStorageMapping() {
        return blockStorageMappings;
    }

    public void setBlockStorageMappings(List<NovaBlockStorageMapping> blockStorageMappings) {
        this.blockStorageMappings = blockStorageMappings;
    }

    public String getBasedOnRepositoryUrl() {
        return basedOnRepositoryUrl;
    }

    public void setBasedOnRepositoryUrl(String basedOnRepositoryUrl) {
        this.basedOnRepositoryUrl = basedOnRepositoryUrl;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getBuildImageDate() {
        return buildImageDate;
    }

    public void setBuildImageDate(String buildImageDate) {
        this.buildImageDate = buildImageDate;
    }

    @Override
    public String toString() {
        return "NovaImage{" + "id='" + id + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner='" + owner + '\'' +
                ", size=" + size +
                ", minDiskSize=" + minDiskSize +
                ", minRamSize=" + minRamSize +
                ", baseImageId='" + baseImageId + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", imageType=" + imageType +
                '}';
    }
}
