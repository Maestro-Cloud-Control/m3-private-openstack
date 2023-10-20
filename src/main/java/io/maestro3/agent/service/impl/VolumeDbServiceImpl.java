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

package io.maestro3.agent.service.impl;

import io.maestro3.agent.dao.VolumeDao;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.service.VolumeDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class VolumeDbServiceImpl implements VolumeDbService {

    private VolumeDao volumeDao;

    @Autowired
    public VolumeDbServiceImpl(VolumeDao volumeDao) {
        this.volumeDao = volumeDao;
    }

    @Override
    public void save(CinderVolume cinderVolume) {
        volumeDao.save(cinderVolume);
    }

    @Override
    public CinderVolume findById(String volumeId) {
        return volumeDao.findById(volumeId);
    }

    @Override
    public List<CinderVolume> findByTenantAndRegion(String tenantName, String regionName) {
        return volumeDao.findByTenantAndRegion(tenantName, regionName);
    }

    @Override
    public void updateVolumes(List<CinderVolume> volumesToRemove, List<CinderVolume> volumesToUpdate) {
        volumeDao.updateVolumes(volumesToRemove, volumesToUpdate);
    }

    @Override
    public List<CinderVolume> findByIds(List<String> volumeIds) {
        return volumeDao.findByIds(volumeIds);
    }
}
