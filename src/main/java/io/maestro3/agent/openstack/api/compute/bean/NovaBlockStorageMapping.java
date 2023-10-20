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
import io.maestro3.agent.model.compute.BlockStorageMapping;


public class NovaBlockStorageMapping implements BlockStorageMapping {

    @SerializedName("snapshot_id")
    private String snapshotId;
    @SerializedName("volume_size")
    private long volumeSize;
    @SerializedName("source_type")
    private String sourceType;
    @SerializedName("device_type")
    private String deviceType;
    @SerializedName("volume_id")
    private String volumeId;
    @SerializedName("destination_type")
    private String destinationType;

    @Override
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public long getVolumeSize() {
        return volumeSize;
    }

    @Override
    public String getSourceType() {
        return sourceType;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getVolumeId() {
        return volumeId;
    }

    @Override
    public String getDestinationType() {
        return destinationType;
    }

    @Override
    public String toString() {
        return "NovaImage{" + "snapshotId='" + snapshotId + '\'' +
                ", volumeSize=" + volumeSize +
                ", sourceType='" + sourceType + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", volumeId='" + volumeId + '\'' +
                ", destinationType=" + destinationType +
                '}';
    }
}
