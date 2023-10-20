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

package io.maestro3.agent.openstack.api.networking.extension.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.api.networking.extension.ISecurityGroupExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.api.networking.request.CreateSecurityGroup;
import io.maestro3.agent.openstack.api.networking.request.UpdateSecurityGroup;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class SecurityGroupExtension extends BasicNetworkingService implements ISecurityGroupExtension {

    public SecurityGroupExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<SecurityGroup> list() throws OSClientException {
        BasicOSRequest<SecurityGroups> list = builder(SecurityGroups.class, endpoint())
                .path("/security-groups")
                .create();

        SecurityGroups entity = client.execute(list).getEntity();
        return entity == null ? null : entity.securityGroupList;
    }

    @Override
    public List<SecurityGroup> listByTenantId(String tenantId) throws OSClientException {
        BasicOSRequest<SecurityGroups> list = builder(SecurityGroups.class, endpoint())
                .path("/security-groups?tenant_id=" + tenantId)
                .create();

        SecurityGroups entity = client.execute(list).getEntity();
        return entity == null ? null : entity.securityGroupList;
    }

    @Override
    public SecurityGroup create(CreateSecurityGroup configuration) throws OSClientException {
        IOSRequest<SecurityGroupWrapper> create = BasicOSRequest.builder(SecurityGroupWrapper.class, endpoint())
                .path("/security-groups")
                .post(configuration)
                .create();

        SecurityGroupWrapper result = client.execute(create).getEntity();
        return result == null ? null : result.securityGroup;
    }

    @Override
    public SecurityGroup detail(String securityGroupId) throws OSClientException {
        IOSRequest<SecurityGroupWrapper> detail = BasicOSRequest.builder(SecurityGroupWrapper.class, endpoint())
                .path("/security-groups/%s", securityGroupId)
                .create();

        SecurityGroupWrapper result = client.execute(detail).getEntity();
        return result == null ? null : result.securityGroup;
    }

    @Override
    public SecurityGroup update(String securityGroupId, UpdateSecurityGroup configuration) throws OSClientException {
        IOSRequest<SecurityGroupWrapper> detail = BasicOSRequest.builder(SecurityGroupWrapper.class, endpoint())
                .path("/security-groups/%s", securityGroupId)
                .put(configuration)
                .create();

        SecurityGroupWrapper result = client.execute(detail).getEntity();
        return result == null ? null : result.securityGroup;
    }

    @Override
    public void delete(String securityGroupId) throws OSClientException {
        IOSRequest<Void> detail = BasicOSRequest.builder(Void.class, endpoint())
                .path("/security-groups/%s", securityGroupId)
                .delete()
                .create();
        client.execute(detail).getEntity();
    }

    private static class SecurityGroups {
        @SerializedName("security_groups")
        private List<SecurityGroup> securityGroupList;
    }

    private static class SecurityGroupWrapper {
        @SerializedName("security_group")
        private SecurityGroup securityGroup;
    }
}
