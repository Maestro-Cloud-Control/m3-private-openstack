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

package io.maestro3.agent.openstack.api.identity.bean;


import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.identity.ProjectModel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class KeystoneProject implements ProjectModel {

    private String id;
    private final String name;
    private String description;
    private boolean enabled = true;
    @SerializedName("domain_id")
    private String domainId;
    private boolean domain = false;

    public KeystoneProject(String name) {
        this.name = name;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isDomain() {
        return domain;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setDomain(boolean domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("enabled", enabled)
                .append("domainId", domainId)
                .append("domain", domain)
                .toString();
    }
}
