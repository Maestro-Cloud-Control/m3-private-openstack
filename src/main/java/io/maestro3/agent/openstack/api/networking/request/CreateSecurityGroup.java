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


public class CreateSecurityGroup {

    @SerializedName("security_group")
    private SecurityGroupConfiguration securityGroup = new SecurityGroupConfiguration();

    private CreateSecurityGroup() {
    }

    public static Builder securityGroup() {
        return new Builder();
    }

    public static class Builder {

        private CreateSecurityGroup request = new CreateSecurityGroup();

        public Builder withName(String name) {
            request.securityGroup.name = name;
            return this;
        }

        public Builder forProject(String tenantId) {
            request.securityGroup.tenantId = tenantId;
            return this;
        }

        public Builder withDescription(String description) {
            request.securityGroup.description = description;
            return this;
        }

        public CreateSecurityGroup get() {
            Asserts.notBlank(request.securityGroup.name, "Security group name is required!");
            return request;
        }
    }

    private static class SecurityGroupConfiguration {
        @SerializedName("name")
        private String name;
        @SerializedName("tenant_id")
        private String tenantId;
        @SerializedName("description")
        private String description;

    }
}
