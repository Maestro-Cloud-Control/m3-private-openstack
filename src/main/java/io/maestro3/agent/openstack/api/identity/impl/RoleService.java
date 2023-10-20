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

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.IRoleService;
import io.maestro3.agent.openstack.api.identity.bean.Role;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class RoleService extends BasicService implements IRoleService {

    public RoleService(IOSClient client) {
        super(ServiceType.IDENTITY, client);
    }

    @Override
    public void grantRole(String projectId, String userId, String roleId) throws OSClientException {
        Assert.notNull(userId, "userId can not be null");
        Assert.notNull(roleId, "roleId can not be null");
        Assert.notNull(projectId, "projectId can not be null");
        BasicOSRequest<Void> create = builder(Void.class, endpoint())
                .path("/projects/%s/users/%s/roles/%s", projectId, userId, roleId)
                .put(null)
                .create();
        client.execute(create).getEntity();
    }

    @Override
    public List<Role> list() throws OSClientException {
        BasicOSRequest<Roles> list = builder(Roles.class, endpoint())
                .path("/roles")
                .create();
        Roles response = client.execute(list).getEntity();
        return response == null ? null : response.roleList;
    }

    @Override
    public Role getRoleByName(String name) throws OSClientException { // will iterate here because of the bug in the API
        Assert.notNull(name, "name can not be null");
        List<Role> roles = list();
        for (Role role : roles) {
            if (StringUtils.equals(role.getName(), name)) {
                return role;
            }
        }
        return null;
    }

    private static class Roles {
        @SerializedName("roles")
        List<Role> roleList;
    }
}
