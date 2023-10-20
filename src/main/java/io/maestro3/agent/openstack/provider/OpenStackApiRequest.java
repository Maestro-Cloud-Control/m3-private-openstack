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

package io.maestro3.agent.openstack.provider;

import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.sdk.internal.util.Assert;

public class OpenStackApiRequest {
    private String authUrl;
    private String user;
    private String password;
    private String tenant;
    private String regionName;
    private String userDomainName;
    private String tenantDomainName;
    private OpenStackVersion version;
    private int timeout;

    private OpenStackApiRequest() {
    }

    private OpenStackApiRequest(String authUrl, String user, String password, String tenant, String regionName,
                                String userDomainName, String tenantDomainName, OpenStackVersion version, int timeout) {
        this.authUrl = authUrl;
        this.user = user;
        this.password = password;
        this.tenant = tenant;
        this.regionName = regionName;
        this.userDomainName = userDomainName;
        this.tenantDomainName = tenantDomainName;
        this.version = version;
        this.timeout = timeout;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getTenant() {
        return tenant;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getUserDomainName() {
        return userDomainName;
    }

    public String getTenantDomainName() {
        return tenantDomainName;
    }

    public OpenStackVersion getVersion() {
        return version;
    }

    public static class OpenStackApiRequestBuilder {
        private String authUrl;
        private String user;
        private String password;
        private String tenant;
        private String regionName;
        private String userDomainName;
        private String tenantDomainName;
        private OpenStackVersion version;
        private int timeout;

        public OpenStackApiRequestBuilder setAuthUrl(String authUrl) {
            this.authUrl = authUrl;
            return this;
        }

        public OpenStackApiRequestBuilder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public OpenStackApiRequestBuilder setUser(String user) {
            this.user = user;
            return this;
        }

        public OpenStackApiRequestBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public OpenStackApiRequestBuilder setTenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public OpenStackApiRequestBuilder setRegionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public OpenStackApiRequestBuilder setUserDomainName(String userDomainName) {
            this.userDomainName = userDomainName;
            return this;
        }

        public OpenStackApiRequestBuilder setTenantDomainName(String tenantDomainName) {
            this.tenantDomainName = tenantDomainName;
            return this;
        }

        public OpenStackApiRequestBuilder setVersion(OpenStackVersion version) {
            this.version = version;
            return this;
        }

        public OpenStackApiRequest build() {
            Assert.hasText(authUrl, "auth url");
            Assert.hasText(user, "user");
            Assert.hasText(password, "password");
            Assert.hasText(tenant, "tenant");
            Assert.hasText(regionName, "regionName");
            Assert.hasText(userDomainName, "userDomainName");
            Assert.hasText(tenantDomainName, "tenantDomainName");
            Assert.notNull(version, "version");

            return new OpenStackApiRequest(authUrl, user, password, tenant, regionName, userDomainName,
                tenantDomainName, version, timeout);
        }
    }
}
