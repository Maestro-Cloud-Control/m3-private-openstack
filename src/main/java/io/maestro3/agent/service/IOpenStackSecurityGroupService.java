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

import io.maestro3.agent.model.network.SecurityGroupType;
import io.maestro3.agent.model.network.impl.ProjectSource;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;

import java.util.Collection;
import java.util.Set;


public interface IOpenStackSecurityGroupService {

    SecurityGroup setupProjectSecurityGroup(OpenStackRegionConfig zone, OpenStackTenant project, boolean personalTenant);

    void createOrUpdateAdminSecurityGroup(OpenStackRegionConfig zone, SecurityGroupType securityGroupType);


    SecurityGroup createEmptySecurityGroup(String OpenStackTenantName, String OpenStackTenantId, IOpenStackApi api, String sgName, String description);

    void updateSecurityType(OpenStackTenant project, OpenStackRegionConfig zone, Set<SecurityGroupType> newSecurityGroupTypes);

    void attachAdminSecurityGroup(OpenStackTenant project, OpenStackRegionConfig zone, String instanceId);

    void changeSecurityGroupAfterInstanceMovedToAnotherProject(OpenStackServerConfig instance,
                                                               OpenStackTenant project,
                                                               OpenStackRegionConfig zone);

    void updateProjectDefaultSecurityGroup(OpenStackTenant project, Collection<ProjectSource> sourcesToAdd, Collection<String> sourcesToRemove);
}
