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

package io.maestro3.agent.openstack.api.storage.extension;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolumeSnapshot;
import io.maestro3.agent.openstack.api.storage.bean.VolumeSnapshot;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SnapshotExtension extends BasicService implements ISnapshotExtension {

    public SnapshotExtension(IOSClient client) {
        super(ServiceType.VOLUME, client);
    }

    @Override
    public List<VolumeSnapshot> list() throws OSClientException {
        BasicOSRequest<Snapshots> listSnapshots = BasicOSRequest.builder(Snapshots.class, endpoint())
                .path("/snapshots/detail")
                .create();

        Snapshots snapshots = client.execute(listSnapshots).getEntity();
        return snapshots != null ? new ArrayList<VolumeSnapshot>(snapshots.cinderVolumeSnapshots) : null;
    }

    @Override
    public VolumeSnapshot details(String snapshotId) throws OSClientException {
        Assert.hasText(snapshotId, "snapshotId cannot be null or empty.");
        BasicOSRequest<SnapshotHolder> snapshotDetails = BasicOSRequest.builder(SnapshotHolder.class, endpoint())
                .path("/snapshots/" + snapshotId)
                .create();

        SnapshotHolder snapshotHolder = client.execute(snapshotDetails).getEntity();
        return snapshotHolder != null ? snapshotHolder.snapshot : null;
    }

    @Override
    public VolumeSnapshot create(String volumeId, Map<String, String> metadata) throws OSClientException {
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");
        BasicOSRequest<SnapshotHolder> createSnapshot = BasicOSRequest.builder(SnapshotHolder.class, endpoint())
                .path("/snapshots")
                .post(new SnapshotSpec(volumeId, metadata))
                .create();

        SnapshotHolder snapshotHolder = client.execute(createSnapshot).getEntity();
        return snapshotHolder != null ? snapshotHolder.snapshot : null;
    }

    @Override
    public void delete(String snapshotId) throws OSClientException {
        Assert.hasText(snapshotId, "snapshotId cannot be null or empty.");
        BasicOSRequest<Void> deleteSnapshot = BasicOSRequest.builder(Void.class, endpoint())
                .path("/snapshots/" + snapshotId)
                .delete()
                .create();

        client.execute(deleteSnapshot);
    }

    private static class Snapshots {
        @SerializedName("snapshots")
        private List<CinderVolumeSnapshot> cinderVolumeSnapshots;
    }

    private static class SnapshotSpec {
        private final InnerSnapshot snapshot;

        private SnapshotSpec(String volumeId) {
            this(volumeId, null);
        }

        private SnapshotSpec(String volumeId, Map<String, String> metadata) {
            this.snapshot = new InnerSnapshot(volumeId, metadata);
        }
    }

    private static class InnerSnapshot {
        @SerializedName("volume_id")
        private final String volumeId;
        private final Map<String, String> metadata;
        private boolean force = true;

        private InnerSnapshot(String volumeId, Map<String, String> metadata) {
            this.volumeId = volumeId;
            this.metadata = metadata;
        }
    }

    private static class SnapshotHolder {
        private CinderVolumeSnapshot snapshot;
    }

    private static class Backup {
        @SerializedName("volume_id")
        private final String volumeId;
        private final boolean force = true;

        Backup(String volumeId) {
            this.volumeId = volumeId;
        }
    }

    private static class BackupHolder {
        private final Backup backup;

        private BackupHolder(String volumeId) {
            this.backup = new Backup(volumeId);
        }
    }
}
