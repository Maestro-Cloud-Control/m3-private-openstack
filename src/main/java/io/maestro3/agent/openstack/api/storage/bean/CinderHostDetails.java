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


public class CinderHostDetails {
    private String host;
    private String project;
    @SerializedName("volume_count")
    private int volumeCount;
    @SerializedName("total_volume_gb")
    private int totalVolumeGb;
    @SerializedName("snapshot_count")
    private int snapshotCount;
    @SerializedName("total_snapshot_gb")
    private int totalSnapshotGb;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getVolumeCount() {
        return volumeCount;
    }

    public void setVolumeCount(int volumeCount) {
        this.volumeCount = volumeCount;
    }

    public int getTotalVolumeGb() {
        return totalVolumeGb;
    }

    public void setTotalVolumeGb(int totalVolumeGb) {
        this.totalVolumeGb = totalVolumeGb;
    }

    public int getSnapshotCount() {
        return snapshotCount;
    }

    public void setSnapshotCount(int snapshotCount) {
        this.snapshotCount = snapshotCount;
    }

    public int getTotalSnapshotGb() {
        return totalSnapshotGb;
    }

    public void setTotalSnapshotGb(int totalSnapshotGb) {
        this.totalSnapshotGb = totalSnapshotGb;
    }

    @Override
    public String toString() {
        return "CinderHostDetails{" + "host='" + host + '\'' +
                ", project='" + project + '\'' +
                ", volumeCount=" + volumeCount +
                ", totalVolumeGb=" + totalVolumeGb +
                ", snapshotCount=" + snapshotCount +
                ", totalSnapshotGb=" + totalSnapshotGb +
                '}';
    }
}
