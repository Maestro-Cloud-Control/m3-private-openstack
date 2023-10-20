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

import io.maestro3.agent.model.identity.Credentials;


public class KeystoneCredentials implements Credentials {

    private PasswordCredentials passwordCredentials = new PasswordCredentials();

    private String tenantName;

    public KeystoneCredentials(String username, String password) {
        passwordCredentials.setCredentials(username, password);
    }

    @Override
    public String getUsername() {
        return passwordCredentials.username;
    }

    @Override
    public String getPassword() {
        return passwordCredentials.password;
    }

    @Override
    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    private static final class PasswordCredentials {
        private String username;
        private String password;

        public void setCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
