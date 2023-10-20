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


public class OpenStackServersImport {

    private String regionAlias;
    private Collection<OpenStackTenantServersImport> tenantServersImport;

    public String getRegionAlias() {
        return regionAlias;
    }

    public void setRegionAlias(String regionAlias) {
        this.regionAlias = regionAlias;
    }

    public Collection<OpenStackTenantServersImport> getTenantServersImport() {
        return tenantServersImport;
    }

    public void setTenantServersImport(Collection<OpenStackTenantServersImport> tenantServersImport) {
        this.tenantServersImport = tenantServersImport;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OpenStackServersImport.class.getSimpleName() + "[", "]")
                .add("regionAlias='" + regionAlias + "'")
                .add("tenantServersImport=" + tenantServersImport)
                .toString();
    }
}
