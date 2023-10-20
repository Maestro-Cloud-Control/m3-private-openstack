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

package io.maestro3.agent.openstack.api.networking.request;

import com.google.gson.annotations.SerializedName;
import org.apache.http.util.Asserts;


public class CreateSecurityGroupRule {

    @SerializedName("security_group_rule")
    private SecurityGroupRuleConfiguration securityGroupRule = new SecurityGroupRuleConfiguration();

    private CreateSecurityGroupRule() {
    }

    public String getDirection() {
        return securityGroupRule.direction;
    }

    public String getProtocol() {
        return securityGroupRule.protocol;
    }

    public Integer getPortRangeMin() {
        return securityGroupRule.portRangeMin;
    }

    public Integer getPortRangeMax() {
        return securityGroupRule.portRangeMax;
    }

    public String getRemoteIpPrefix() {
        return securityGroupRule.remoteIpPrefix;
    }

    public String getRemoteGroupId() {
        return securityGroupRule.remoteGroupId;
    }

    public static Builder rule() {
        return new Builder();
    }

    public static class Builder {

        private CreateSecurityGroupRule request = new CreateSecurityGroupRule();
        private SecurityGroupRuleConfiguration securityGroupRule = request.securityGroupRule;

        public Builder description(String description) {
            securityGroupRule.description = description;
            return this;
        }

        public Builder direction(String direction) {
            securityGroupRule.direction = direction;
            return this;
        }

        public Builder project(String tenantId) {
            securityGroupRule.tenantId = tenantId;
            return this;
        }

        public Builder etherType(String etherType) {
            securityGroupRule.etherType = etherType;
            return this;
        }

        public Builder securityGroup(String securityGroupId) {
            securityGroupRule.securityGroupId = securityGroupId;
            return this;
        }

        public Builder portRangeMin(Integer portRangeMin) {
            securityGroupRule.portRangeMin = portRangeMin;
            return this;
        }

        public Builder portRangeMax(Integer portRangeMax) {
            securityGroupRule.portRangeMax = portRangeMax;
            return this;
        }

        public Builder protocol(String protocol) {
            securityGroupRule.protocol = protocol;
            return this;
        }

        public Builder remoteGroup(String remoteGroupId) {
            securityGroupRule.remoteGroupId = remoteGroupId;
            return this;
        }

        public Builder remoteIpPrefix(String remoteIpPrefix) {
            securityGroupRule.remoteIpPrefix = remoteIpPrefix;
            return this;
        }

        public CreateSecurityGroupRule get() {
            Asserts.notBlank(securityGroupRule.direction, "Required attribute 'direction' not specified");
            Asserts.notBlank(securityGroupRule.securityGroupId, "Required attribute 'security_group_id' not specified");
            return request;
        }
    }

    private static class SecurityGroupRuleConfiguration {
        @SerializedName("security_group_id")
        private String securityGroupId;
        @SerializedName("direction")
        private String direction;
        @SerializedName("ethertype")
        private String etherType;
        @SerializedName("port_range_max")
        private Integer portRangeMax;
        @SerializedName("port_range_min")
        private Integer portRangeMin;
        @SerializedName("protocol")
        private String protocol;
        @SerializedName("remote_ip_prefix")
        private String remoteIpPrefix;
        @SerializedName("remote_group_id")
        private String remoteGroupId;
        @SerializedName("tenant_id")
        private String tenantId;
        @SerializedName("description")
        private String description;
    }
}
