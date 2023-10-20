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

package io.maestro3.agent.dao;

import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.tenant.OpenStackTenant;

import java.util.Collection;
import java.util.List;

public interface IVLANDao {

    void save(VLAN vlan);

    void update(VLAN vlan);

    void delete(String vlanId);

    VLAN findByName(String vlanName, String tenantId, String regionId);

    VLAN findByNameForRegion(String vlanName, String regionId);

    VLAN findByID(String vlanID);

    List<VLAN> findForTenant(String tenantId, String regionId);

    List<VLAN> findOnlyForTenant(String tenantId, String regionId);

    List<VLAN> findDmzForRegion(String regionId);

    List<VLAN> findAll();

    List<VLAN> findByName(List<String> names);

    VLAN findForTenantByName(String vlanName, String tenantId, String regionId);

    List<VLAN> findByOpenStackNetworkId(String regionId, String networkId);

    List<VLAN> findTenantByOpenStackNetworkId(String regionId, String tenantId, String networkId);

    List<VLAN> findByRegionId(String regionId);

    void updateByOpenStackNetworkId(String regionId, String oldNetworkId, String newNetworkId);

    Collection<VLAN> findTenantVLANs(Collection<OpenStackTenant> tenants);
}
