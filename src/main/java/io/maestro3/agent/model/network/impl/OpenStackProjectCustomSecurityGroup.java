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

package io.maestro3.agent.model.network.impl;

import io.maestro3.agent.model.network.CustomSecurityConfig;
import io.maestro3.agent.model.network.SecurityConfigType;
import io.maestro3.sdk.internal.util.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Set;


public class OpenStackProjectCustomSecurityGroup extends CustomSecurityConfig {

    @NotBlank
    private String projectId;
    @NotBlank
    private String name;
    @NotBlank
    private String openStackId;
    private String description;
    private Set<String> instanceIds;
    private boolean all;

    public OpenStackProjectCustomSecurityGroup() {
        super(SecurityConfigType.OPEN_STACK_PROJECT_CUSTOM);
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name.toLowerCase();
        }
    }

    public String getOpenStackId() {
        return openStackId;
    }

    public void setOpenStackId(String openStackId) {
        this.openStackId = openStackId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(Set<String> instanceIds) {
        this.instanceIds = instanceIds;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }
}
