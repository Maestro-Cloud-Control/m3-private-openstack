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

package io.maestro3.agent.model.setup;

import java.util.Collection;
import java.util.StringJoiner;


public class OpenStackTenantServersImport {

    private String tenantAlias;
    private Collection<String> serverAliases;

    public String getTenantAlias() {
        return tenantAlias;
    }

    public void setTenantAlias(String tenantAlias) {
        this.tenantAlias = tenantAlias;
    }

    public Collection<String> getServerAliases() {
        return serverAliases;
    }

    public void setServerAliases(Collection<String> serverAliases) {
        this.serverAliases = serverAliases;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OpenStackTenantServersImport.class.getSimpleName() + "[", "]")
                .add("tenantAlias='" + tenantAlias + "'")
                .add("serverAliases=" + serverAliases)
                .toString();
    }
}
