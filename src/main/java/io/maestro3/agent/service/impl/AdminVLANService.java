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

import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IAdminVLANService;
import io.maestro3.agent.service.IVLANService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AdminVLANService implements IAdminVLANService {

    private final IVLANService vlanService;

    @Autowired
    public AdminVLANService(IVLANService vlanService) {
        this.vlanService = vlanService;
    }

    @Override
    public void addRegionVLAN(VLAN vlan, OpenStackRegionConfig region) {
        Assert.notNull(vlan, "vlan cannot be null");
        Assert.notNull(region, "region cannot be null");

        vlanService.save(vlan);
    }

    @Override
    public void addTenantVLAN(VLAN vlan, OpenStackRegionConfig region, OpenStackTenant tenant) {
        Assert.notNull(vlan, "vlan cannot be null");
        Assert.notNull(region, "region cannot be null");
        Assert.notNull(tenant, "tenant cannot be null");

        vlanService.save(vlan);
    }

    @Override
    public VLAN getTenantVLANByName(String vlanName, String tenantId, String regionId) {
        return vlanService.getVLANByName(vlanName, tenantId, regionId);
    }

    @Override
    public VLAN getRegionVLANByName(String vlanName, String regionId) {
        return vlanService.getRegionVLANByName(vlanName, regionId);
    }
}
