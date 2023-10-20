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


public class UpdateVolumeQuotaRequest {
    private Integer volumes;
    @SerializedName("gigabytes")
    private Integer volumesGb;
    private Integer snapshots;

    public static UpdateVolumeQuotaRequest build() {
        return new UpdateVolumeQuotaRequest();
    }

    private UpdateVolumeQuotaRequest() {
    }

    public Integer getVolumes() {
        return volumes;
    }

    public UpdateVolumeQuotaRequest withVolumes(Integer volumes) {
        this.volumes = volumes;
        return this;
    }

    public Integer getSnapshots() {
        return snapshots;
    }

    public UpdateVolumeQuotaRequest withSnapshots(Integer snapshots) {
        this.snapshots = snapshots;
        return this;
    }

    public Integer getVolumesGb() {
        return volumesGb;
    }

    public UpdateVolumeQuotaRequest withVolumesGb(Integer volumesGb) {
        this.volumesGb = volumesGb;
        return this;
    }

    public UpdateVolumeQuotaRequest unlimited() {
        snapshots = -1;
        volumes = -1;
        volumesGb = -1;
        return this;
    }
}
