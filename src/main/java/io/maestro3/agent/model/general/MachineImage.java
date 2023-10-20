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

package io.maestro3.agent.model.general;

import io.maestro3.agent.model.compute.ImageVisibility;
import io.maestro3.agent.model.base.PlatformType;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public abstract class MachineImage {

    @Id
    private String id;
    @NotBlank
    private String nameAlias;
    @NotNull
    private PlatformType platformType;
    private String tenantId;
    @NotBlank
    private String regionId;

    private double requiredMinMemoryGb;
    private int requiredMinStorageSizeGb;

    private ImageVisibility imageVisibility;

    private String imageStatus;

    public String getId() {
        return id;
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(PlatformType platformType) {
        this.platformType = platformType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(String imageStatus) {
        this.imageStatus = imageStatus;
    }

    public ImageVisibility getImageVisibility() {
        return imageVisibility;
    }

    public void setImageVisibility(ImageVisibility imageVisibility) {
        this.imageVisibility = imageVisibility;
    }

    public double getRequiredMinMemoryGb() {
        return requiredMinMemoryGb;
    }

    public void setRequiredMinMemoryGb(double requiredMinMemoryGb) {
        this.requiredMinMemoryGb = requiredMinMemoryGb;
    }

    public int getRequiredMinStorageSizeGb() {
        return requiredMinStorageSizeGb;
    }

    public void setRequiredMinStorageSizeGb(int requiredMinStorageSizeGb) {
        this.requiredMinStorageSizeGb = requiredMinStorageSizeGb;
    }

    @Override
    public String toString() {
        return "MachineImage{" +
                "id='" + id + '\'' +
                ", nameAlias='" + nameAlias + '\'' +
                ", platformType=" + platformType +
                ", tenantId='" + tenantId + '\'' +
                ", regionId='" + regionId + '\'' +
                ", imageVisibility=" + imageVisibility +
                ", imageStatus='" + imageStatus + '\'' +
                ", minStorageSizeGb=" + requiredMinStorageSizeGb +
                ", minMemoryGb='" + requiredMinMemoryGb + '\'' +
                '}';
    }
}
