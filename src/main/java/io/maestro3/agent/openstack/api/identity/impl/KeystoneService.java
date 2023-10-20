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

package io.maestro3.agent.openstack.api.identity.impl;

import io.maestro3.agent.openstack.api.identity.IDomainService;
import io.maestro3.agent.openstack.api.identity.IKeystoneService;
import io.maestro3.agent.openstack.api.identity.IProjectService;
import io.maestro3.agent.openstack.api.identity.IRoleService;
import io.maestro3.agent.openstack.api.identity.IUserService;
import io.maestro3.agent.openstack.client.IOSClient;


public class KeystoneService implements IKeystoneService {

    private IProjectService projects;
    private IUserService users;
    private IRoleService roles;
    private IDomainService domains;

    public KeystoneService(IOSClient client) {
        projects = new ProjectService(client);
        users = new UserService(client);
        roles = new RoleService(client);
        domains = new DomainsService(client);
    }

    @Override
    public IProjectService projects() {
        return projects;
    }

    @Override
    public IUserService users() {
        return users;
    }

    @Override
    public IRoleService roles() {
        return roles;
    }

    @Override
    public IDomainService domains() {
        return domains;
    }
}
