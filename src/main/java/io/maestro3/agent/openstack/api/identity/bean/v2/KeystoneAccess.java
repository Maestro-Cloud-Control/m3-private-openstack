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

package io.maestro3.agent.openstack.api.identity.bean.v2;


import io.maestro3.agent.model.identity.Access;
import io.maestro3.agent.model.identity.Endpoint;
import io.maestro3.agent.model.identity.Service;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneEndpoint;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneToken;

import java.util.ArrayList;
import java.util.List;


public class KeystoneAccess implements Access {

    private KeystoneToken token;
    private List<AccessService> serviceCatalog;
    private AuthEntity user;

    @Override
    public KeystoneToken getToken() {
        return token;
    }

    @Override
    public String getTokenUserId() {
        return user.id;
    }

    @Override
    public String getTokenProjectId() {
        return token.getTenant().id;
    }

    @Override
    public List<Service> getServiceCatalog() {
        return new ArrayList<Service>(serviceCatalog);
    }

    private static final class AccessService implements Service {
        private String name;
        private List<KeystoneEndpoint> endpoints;
        private ServiceType serviceType;

        @Override
        public List<Endpoint> getEndpoints() {
            return new ArrayList<Endpoint>(endpoints);
        }

        @Override
        public ServiceType getServiceType() {
            if (serviceType == null) {
                serviceType = ServiceType.encode(name, endpoints.get(0).getPublicURL().toString());
            }
            return serviceType;
        }
    }

    public static class AuthEntity {
        private String id;
        private String name;
    }
}
