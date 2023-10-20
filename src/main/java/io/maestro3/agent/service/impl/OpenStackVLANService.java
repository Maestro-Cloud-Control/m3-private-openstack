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
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.util.ConversionUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;


@Service
@Qualifier("openStackVLANService")
public class OpenStackVLANService extends VLANService implements IOpenStackVLANService {

    @Override
    public List<OpenStackVLAN> findByOpenStackNetworkId(String regionId, String networkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(networkId, "networkId should not be null or empty.");

        return ConversionUtils.castCollection(vlanDao.findByOpenStackNetworkId(regionId, networkId), OpenStackVLAN.class);
    }

    @Override
    public List<OpenStackVLAN> findByOpenStackNetworkId(String regionId, String tenantId, String networkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(networkId, "networkId should not be null or empty.");

        List<VLAN> tenantByOpenStackNetworkId = vlanDao.findTenantByOpenStackNetworkId(regionId, tenantId, networkId);
        if (CollectionUtils.isEmpty(tenantByOpenStackNetworkId)) {
            tenantByOpenStackNetworkId = vlanDao.findByOpenStackNetworkId(regionId, networkId);
        }

        return ConversionUtils.castCollection(tenantByOpenStackNetworkId, OpenStackVLAN.class);
    }

    @Override
    public List<OpenStackVLAN> findByRegionId(String regionId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        List<VLAN> vlans = vlanDao.findByRegionId(regionId);
        return ConversionUtils.castCollection(vlans, OpenStackVLAN.class);
    }


    @Override
    public List<OpenStackVLAN> findByRegionAndTenantId(String regionId, String tenant) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(tenant, "tenant should not be null or empty.");
        List<VLAN> vlans = vlanDao.findForTenant(tenant, regionId);
        return ConversionUtils.castCollection(vlans, OpenStackVLAN.class);
    }

    @Override
    public void updateByOpenStackNetworkId(String regionId, String oldNetworkId, String newNetworkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(oldNetworkId, "oldNetworkId should not be null or empty.");
        Assert.hasText(newNetworkId, "newNetworkId should not be null or empty.");

        vlanDao.updateByOpenStackNetworkId(regionId, oldNetworkId, newNetworkId);
    }

    @Override
    public Collection<OpenStackVLAN> findTenantVLANs(Collection<OpenStackTenant> tenants) {
        Assert.notEmpty(tenants, "tenant can not be null");
        Collection<VLAN> tenantVLANs = vlanDao.findTenantVLANs(tenants);
        tenantVLANs.removeIf(vlan -> !(vlan instanceof OpenStackVLAN));
        return ConversionUtils.castCollection(tenantVLANs, OpenStackVLAN.class);
    }
}
