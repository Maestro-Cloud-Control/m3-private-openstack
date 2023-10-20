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

package io.maestro3.agent.openstack.api.identity.bean.v3;

import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.sdk.internal.util.StringUtils;

import java.util.Collections;
import java.util.List;


public class V3Auth {

    private static final String DEFAULT_AUTH_METHOD = "password";
    private static final String DEFAULT_DOMAIN_NAME = "default";

    private final AuthWrapper auth;

    public V3Auth(String username, String password, String tenantName, String userDomainName, String tenantDomainName,
                  OpenStackVersion version) {
        auth = new AuthWrapper(
            new Identity(new Password(new User(username, password, getAuthDomain(userDomainName, version)))),
            new Scope(new Project(tenantName, getAuthDomain(tenantDomainName, version)))
        );
    }

    private static class AuthWrapper {
        private final Identity identity;
        private Scope scope;

        private AuthWrapper(Identity identity) {
            this.identity = identity;
        }

        private AuthWrapper(Identity identity, Scope scope) {
            this(identity);
            this.scope = scope;
        }
    }

    private final class Scope {
        private final Project project;

        private Scope(Project project) {
            this.project = project;
        }
    }

    private final class Project {
        private final String name;
        private final Object domain;

        private Project(String name, Object domain) {
            this.name = name;
            this.domain = domain;
        }
    }

    private static class Identity {
        final List<String> methods = Collections.singletonList(DEFAULT_AUTH_METHOD);
        final Password password;

        private Identity(Password password) {
            this.password = password;
        }
    }

    private static class Password {
        final User user;

        private Password(User user) {
            this.user = user;
        }
    }

    private static class User {
        final String name;
        final String password;
        final Object domain;

        private User(String name, String password, Object domain) {
            this.name = name;
            this.password = password;
            this.domain = domain;
        }
    }

    private static class OcataDomain {
        final String id;

        private OcataDomain(String name) {
            this.id = StringUtils.isBlank(name)
                ? DEFAULT_DOMAIN_NAME
                : name;
        }
    }

    private static class DefaultDomain {
        final String name;

        private DefaultDomain(String name) {
            this.name = StringUtils.isBlank(name)
                ? DEFAULT_DOMAIN_NAME
                : name;
        }
    }

    private static Object getAuthDomain(String name, OpenStackVersion version){
        if (version == OpenStackVersion.OCATA){
            return new OcataDomain(name);
        }
        return new DefaultDomain(name);
    }
}
