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

package io.maestro3.agent.service;

import io.maestro3.agent.model.base.VLAN;

import java.util.List;
import java.util.Set;

public interface IVLANService {

    void save(VLAN vlan);

    void update(VLAN vlan);

    VLAN getVLANByName(String vlanName, String tenantId, String regionId);

    VLAN getRegionVLANByName(String vlanName, String regionId);

    List<VLAN> getVLANSByName(List<String> names);

    VLAN getVLANByID(String vlanID);

    List<VLAN> getAvailableForTenant(String tenantId, String regionId, boolean includeNonTenant);

    VLAN findForTenantByName(String vlanName, String tenantId, String regionId);

    List<VLAN> getDmzAvailableForRegion(String regionId);

    void delete(VLAN vlan);

    Set<String> extractVlanNames(List<VLAN> existingVlans);
}
