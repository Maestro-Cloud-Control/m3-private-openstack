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


public class VolumeQuota {
    private int volumes;
    private int snapshots;
    private int gigabytes;
    @SerializedName("per_volume_gigabytes")
    private int perVolumeGigabytes;

    public int getVolumes() {
        return volumes;
    }

    public void setVolumes(int volumes) {
        this.volumes = volumes;
    }

    public int getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(int snapshots) {
        this.snapshots = snapshots;
    }

    public int getGigabytes() {
        return gigabytes;
    }

    public void setGigabytes(int gigabytes) {
        this.gigabytes = gigabytes;
    }

    public int getPerVolumeGigabytes() {
        return perVolumeGigabytes;
    }

    public void setPerVolumeGigabytes(int perVolumeGigabytes) {
        this.perVolumeGigabytes = perVolumeGigabytes;
    }

    @Override
    public String toString() {
        return "VolumeQuota{" + "volumes=" + volumes +
                ", snapshots=" + snapshots +
                ", gigabytes=" + gigabytes +
                ", perVolumeGigabytes=" + perVolumeGigabytes +
                '}';
    }
}
