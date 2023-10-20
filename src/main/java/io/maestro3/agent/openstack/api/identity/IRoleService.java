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

package io.maestro3.agent.openstack.api.identity;

import io.maestro3.agent.openstack.api.identity.bean.Role;
import io.maestro3.agent.openstack.exception.OSClientException;

import java.util.List;


public interface IRoleService {

    void grantRole(String tenantId, String userId, String roleId) throws OSClientException;

    List<Role> list() throws OSClientException;

    Role getRoleByName(String name) throws OSClientException;
}
