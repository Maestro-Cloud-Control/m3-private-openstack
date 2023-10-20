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

package io.maestro3.agent.openstack.api.storage.impl;

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.IBlockStorageService;
import io.maestro3.agent.openstack.api.storage.extension.HostsExtension;
import io.maestro3.agent.openstack.api.storage.extension.IHostsExtension;
import io.maestro3.agent.openstack.api.storage.extension.IQuotasExtension;
import io.maestro3.agent.openstack.api.storage.extension.ISchedulerStatsExtension;
import io.maestro3.agent.openstack.api.storage.extension.ISnapshotExtension;
import io.maestro3.agent.openstack.api.storage.extension.IVolumesExtension;
import io.maestro3.agent.openstack.api.storage.extension.QuotasExtension;
import io.maestro3.agent.openstack.api.storage.extension.SchedulerStatsExtension;
import io.maestro3.agent.openstack.api.storage.extension.SnapshotExtension;
import io.maestro3.agent.openstack.api.storage.extension.VolumeExtensionDelegator;
import io.maestro3.agent.openstack.api.storage.extension.VolumesExtension;
import io.maestro3.agent.openstack.client.IOSClient;


public class BlockStorageService extends BasicService implements IBlockStorageService {
    private final IVolumesExtension volumesExtension;
    private final ISnapshotExtension snapshotExtension;
    private final IQuotasExtension quotasExtension;
    private final IHostsExtension hostsExtension;
    private final ISchedulerStatsExtension schedulerStatsExtension;

    public BlockStorageService(IOSClient client) {
        super(ServiceType.VOLUME, client);
        volumesExtension = new VolumeExtensionDelegator(client);
        snapshotExtension = new SnapshotExtension(client);
        quotasExtension = new QuotasExtension(client);
        hostsExtension = new HostsExtension(client);
        schedulerStatsExtension = new SchedulerStatsExtension(client);
    }

    @Override
    public IVolumesExtension volumes() {
        return volumesExtension;
    }

    @Override
    public ISnapshotExtension snapshots() {
        return snapshotExtension;
    }

    @Override
    public IQuotasExtension quotas() {
        return quotasExtension;
    }

    @Override
    public IHostsExtension hosts() {
        return hostsExtension;
    }

    @Override
    public ISchedulerStatsExtension schedulerStats() {
        return schedulerStatsExtension;
    }
}
