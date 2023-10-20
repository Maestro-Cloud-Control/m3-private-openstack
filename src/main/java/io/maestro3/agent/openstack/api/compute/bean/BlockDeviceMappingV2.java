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


public class BlockDeviceMappingV2 {
    @SerializedName("boot_index")
    private String bootIndex;
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("source_type")
    private String sourceType;
    @SerializedName("volume_size")
    private String volumeSize;
    @SerializedName("destination_type")
    private String destinationType;
    @SerializedName("delete_on_termination")
    private boolean deleteOnTermination;
    @SerializedName("disk_bus")
    private String diskBus;

    public BlockDeviceMappingV2(String bootIndex, String uuid, String sourceType, String volumeSize,
                                String destinationType, boolean deleteOnTermination, String diskBus) {
        this.bootIndex = bootIndex;
        this.uuid = uuid;
        this.sourceType = sourceType;
        this.volumeSize = volumeSize;
        this.destinationType = destinationType;
        this.deleteOnTermination = deleteOnTermination;
        this.diskBus = diskBus;
    }

    public String getBootIndex() {
        return bootIndex;
    }

    public void setBootIndex(String bootIndex) {
        this.bootIndex = bootIndex;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(String volumeSize) {
        this.volumeSize = volumeSize;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public boolean isDeleteOnTermination() {
        return deleteOnTermination;
    }

    public void setDeleteOnTermination(boolean deleteOnTermination) {
        this.deleteOnTermination = deleteOnTermination;
    }

    public String getDiskBus() {
        return diskBus;
    }

    public void setDiskBus(String diskBus) {
        this.diskBus = diskBus;
    }

    public static BlockDeviceMappingV2 fromImageToVolume(long diskSize, String imageId){
        return new BlockDeviceMappingV2("0", imageId, "image",
            String.valueOf(diskSize), "volume", true,
            "scsi");
    }
}
