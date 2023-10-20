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

import io.maestro3.agent.dao.IVLANDao;
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.service.IVLANService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Primary
public class VLANService implements IVLANService {

    @Autowired
    protected IVLANDao vlanDao;

    @Override
    public void save(VLAN vlan) {
        Assert.notNull(vlan, "VLAN can not be null");
        vlanDao.save(vlan);
    }

    @Override
    public void update(VLAN vlan) {
        Assert.notNull(vlan, "VLAN can not be null");
        Assert.hasText(vlan.getId(), "vlan.id can not be null or empty");
        vlanDao.update(vlan);
    }

    @Override
    public VLAN getVLANByName(String vlanName, String tenantId, String regionId) {
        Assert.hasText(vlanName, "VLAN name should not be null or empty.");
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return vlanDao.findByName(vlanName, tenantId, regionId);
    }

    @Override
    public VLAN getRegionVLANByName(String vlanName, String regionId) {
        Assert.hasText(vlanName, "VLAN name should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return vlanDao.findByNameForRegion(vlanName, regionId);
    }

    @Override
    public List<VLAN> getVLANSByName(List<String> names) {
        Assert.notEmpty(names, "should be provided at least one VLAN name");
        return vlanDao.findByName(names);
    }

    @Override
    public VLAN getVLANByID(String vlanID) {
        Assert.hasText(vlanID, "VLAN ID should not be null or empty.");
        return vlanDao.findByID(vlanID);
    }

    @Override
    public List<VLAN> getAvailableForTenant(String tenantId, String regionId, boolean includeNonTenant) {
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        if (includeNonTenant) {
            return vlanDao.findForTenant(tenantId, regionId);
        }

        return vlanDao.findOnlyForTenant(tenantId, regionId);
    }

    @Override
    public VLAN findForTenantByName(String vlanName, String tenantId, String regionId) {
        return vlanDao.findForTenantByName(vlanName, tenantId, regionId);
    }

    @Override
    public List<VLAN> getDmzAvailableForRegion(String regionId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return vlanDao.findDmzForRegion(regionId);
    }

    @Override
    public void delete(VLAN vlan) {
        Assert.notNull(vlan, "vlan can not be null");
        Assert.hasText(vlan.getId(), "vlanId can not be null or empty");
        vlanDao.delete(vlan.getId());
    }

    @Override
    public Set<String> extractVlanNames(List<VLAN> existingVlans) {
        Set<String> names = new HashSet<>();
        for (VLAN current : existingVlans) {
            names.add(current.getName());
        }
        return names;
    }
}
