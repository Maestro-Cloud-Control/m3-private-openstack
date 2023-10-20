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
import io.maestro3.agent.model.identity.User;


public class KeystoneUser implements User {

    private String id;
    private String username;
    private String name;
    private String password;
    private String tenantId;
    @SerializedName("default_project_id")
    private String defaultProjectId;
    private String email;
    private boolean enabled = true;
    @SerializedName("domain_id")
    private String domainId;

    public KeystoneUser() {
    }

    public KeystoneUser(String name, String password, String projectId) {
        // for API v2 compatibility
        username = name;
        this.name = name;
        this.password = password;
        tenantId = projectId;
        defaultProjectId = projectId;
    }

    public KeystoneUser(User user, String password) {
        id = user.getId();
        username = user.getName();
        name = user.getName();
        this.password = password;
        tenantId = user.getTenantId();
        defaultProjectId = user.getTenantId();
        email = user.getEmail();
        enabled = user.isEnabled();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String defaultProjectId() {
        return defaultProjectId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDefaultProjectId(String defaultProjectId) {
        this.defaultProjectId = defaultProjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String toString() {
        return "KeystoneUser{" + "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", defaultProjectId='" + defaultProjectId + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
