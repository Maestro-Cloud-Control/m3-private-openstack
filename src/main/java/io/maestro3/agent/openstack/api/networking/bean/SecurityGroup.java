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

package io.maestro3.agent.openstack.api.networking.bean;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;


public class SecurityGroup {

    @SerializedName("id")
    private String id;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("description")
    private String description;
    @SerializedName("name")
    private String name;
    @SerializedName("security_group_rules")
    private List<SecurityGroupRule> rules;

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<SecurityGroupRule> getRules() {
        return rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof SecurityGroup)) return false;

        SecurityGroup that = (SecurityGroup) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(tenantId, that.tenantId)
                .append(description, that.description)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(tenantId)
                .append(description)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SecurityGroup{");
        sb.append("id='").append(id).append('\'');
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", rules=").append(rules);
        sb.append('}');
        return sb.toString();
    }
}
