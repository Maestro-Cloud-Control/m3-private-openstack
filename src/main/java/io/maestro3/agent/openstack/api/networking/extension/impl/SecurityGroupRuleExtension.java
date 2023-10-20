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
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroupRule;
import io.maestro3.agent.openstack.api.networking.extension.ISecurityGroupRuleExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.api.networking.request.CreateSecurityGroupRule;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class SecurityGroupRuleExtension extends BasicNetworkingService implements ISecurityGroupRuleExtension {

    public SecurityGroupRuleExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<SecurityGroupRule> list() throws OSClientException {
        BasicOSRequest<SecurityGroupRules> list = builder(SecurityGroupRules.class,
                endpoint()).
                path("/security-group-rules").
                create();

        SecurityGroupRules entity = client.execute(list).getEntity();
        return entity == null ? null : entity.securityGroupRule;
    }

    @Override
    public SecurityGroupRule create(CreateSecurityGroupRule configuration) throws OSClientException {
        IOSRequest<SecurityGroupRuleWrapper> create = BasicOSRequest.builder(SecurityGroupRuleWrapper.class, endpoint())
                .path("/security-group-rules")
                .post(configuration)
                .create();

        SecurityGroupRuleWrapper result = client.execute(create).getEntity();
        return result == null ? null : result.securityGroupRule;
    }

    @Override
    public SecurityGroupRule get(String securityGroupRuleId) throws OSClientException {
        IOSRequest<SecurityGroupRuleWrapper> detail = BasicOSRequest.builder(SecurityGroupRuleWrapper.class, endpoint())
                .path("/security-group-rules/%s", securityGroupRuleId)
                .create();
        SecurityGroupRuleWrapper result = client.execute(detail).getEntity();
        return result == null ? null : result.securityGroupRule;
    }

    @Override
    public void delete(String securityGroupRuleId) throws OSClientException {
        IOSRequest<Void> delete = BasicOSRequest.builder(Void.class, endpoint())
                .path("/security-group-rules/%s", securityGroupRuleId)
                .delete()
                .create();
        client.execute(delete);
    }

    private static class SecurityGroupRules {
        @SerializedName("security_group_rules")
        private List<SecurityGroupRule> securityGroupRule;
    }

    private static class SecurityGroupRuleWrapper {
        @SerializedName("security_group_rule")
        private SecurityGroupRule securityGroupRule;
    }
}
