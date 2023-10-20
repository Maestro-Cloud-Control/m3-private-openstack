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

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.CinderBackendStoragePool;
import io.maestro3.agent.openstack.api.storage.bean.CinderBackendStoragePoolCapabilities;
import io.maestro3.agent.openstack.api.storage.bean.CinderBackendStoragePoolInfo;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class SchedulerStatsExtension extends BasicService implements ISchedulerStatsExtension {

    public SchedulerStatsExtension(IOSClient client) {
        super(ServiceType.VOLUME, client);
    }

    @Override
    public List<CinderBackendStoragePoolInfo> listBackendStoragePools() throws OSClientException {
        BasicOSRequest<CinderBackendStoragePools> listSnapshots = BasicOSRequest.builder(CinderBackendStoragePools.class, endpoint())
                .path("/scheduler-stats/get_pools?detail=true")
                .create();

        CinderBackendStoragePools storagePools = client.execute(listSnapshots).getEntity();
        if (storagePools == null) {
            return Collections.emptyList();
        }
        return convertToStorageInfo(storagePools.pools);
    }

    private List<CinderBackendStoragePoolInfo> convertToStorageInfo(Collection<CinderBackendStoragePool> pools) {
        if (CollectionUtils.isEmpty(pools)) {
            return Collections.emptyList();
        }
        List<CinderBackendStoragePoolInfo> storagePoolInfoList = new ArrayList<>();
        for (CinderBackendStoragePool pool : pools) {
            CinderBackendStoragePoolInfo storagePoolInfo = new CinderBackendStoragePoolInfo();
            storagePoolInfo.setHostName(pool.getName());
            CinderBackendStoragePoolCapabilities capabilities = pool.getCapabilities();
            if (capabilities != null) {
                storagePoolInfo.setFreeCapacityGb(getDoubleFromString(capabilities.getFreeCapacityGb()));
                storagePoolInfo.setTotalCapacityGb(getDoubleFromString(capabilities.getTotalCapacityGb()));
                storagePoolInfo.setTotalVolumes(NumberUtils.toInt(capabilities.getTotalVolumes(), -1));
            }
            storagePoolInfoList.add(storagePoolInfo);
        }
        return storagePoolInfoList;
    }

    private double getDoubleFromString(String value) {
        if (StringUtils.isBlank(value)) {
            return -1;
        }
        if (value.equalsIgnoreCase("unknown")) {
            return -1;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final class CinderBackendStoragePools {
        private List<CinderBackendStoragePool> pools;
    }
}
