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

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.identity.Access;
import io.maestro3.agent.model.identity.Endpoint;
import io.maestro3.agent.model.identity.Service;
import io.maestro3.agent.model.identity.Token;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneEndpoint;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class V3KeystoneAccess implements Access {

    private V3Token token;

    private String regionId;
    private String tokenId;

    private List<Service> services;

    @Override
    public Token getToken() {
        KeystoneToken keystoneToken = new KeystoneToken();
        keystoneToken.setExpires(token.expires);
        keystoneToken.setId(tokenId);
        return keystoneToken;
    }

    @Override
    public String getTokenUserId() {
        return token.user.id;
    }

    @Override
    public String getTokenProjectId() {
        return token.project.id;
    }

    public void setToken(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public List<Service> getServiceCatalog() {
        List<Service> servicesLocal = services;
        if (servicesLocal != null) {
            return servicesLocal;
        }

        synchronized (this) {
            servicesLocal = services;
            if (servicesLocal == null) {
                List<V3Catalog> catalogs = token.catalogs;
                if (CollectionUtils.isEmpty(catalogs)) {
                    return null;
                }

                List<Service> servicesList = new ArrayList<>();
                for (V3Catalog catalog : catalogs) {
                    if (CollectionUtils.isEmpty(catalog.endpoints)) {
                        continue;
                    }

                    V3Service service = new V3Service();
                    URL publicUrl = null;
                    URL adminURL = null;

                    for (V3Endpoint endpoint : catalog.endpoints) {
                        if (StringUtils.isNotBlank(regionId) && !regionId.equalsIgnoreCase(endpoint.regionId)) {
                            continue;
                        }
                        EndpointInterface endpointInterface = endpoint.endpointInterface;
                        String url = endpoint.url;
                        if (endpointInterface == EndpointInterface.ADMIN) {
                            adminURL = getUrl(url);
                        }
                        if (endpointInterface == EndpointInterface.PUBLIC) {
                            publicUrl = getUrl(url);
                        }
                    }
                    Endpoint keystoneEndpoint = new KeystoneEndpoint(publicUrl, adminURL);
                    service.setEndpoints(Collections.singletonList(keystoneEndpoint));
                    if (keystoneEndpoint.getPublicURL() != null) {
                        service.setServiceType(ServiceType.encode(catalog.name, keystoneEndpoint.getPublicURL().toString()));
                        servicesList.add(service);
                    } else if (keystoneEndpoint.getAdminURL() != null) {
                        service.setServiceType(ServiceType.encode(catalog.name, keystoneEndpoint.getAdminURL().toString()));
                        servicesList.add(service);
                    }
                }
                services = servicesLocal = servicesList;
            }
        }
        return servicesLocal;
    }

    private URL getUrl(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static class V3Token {
        @SerializedName("expires_at")
        private Date expires;
        private AuthEntity user;
        private AuthEntity project;
        @SerializedName("catalog")
        private List<V3Catalog> catalogs;
    }

    private static class V3Catalog {
        private List<V3Endpoint> endpoints;
        private String name;
    }

    private static class AuthEntity {
        private String id;
        private String name;
    }

    private static class V3Endpoint {
        @SerializedName("interface")
        private EndpointInterface endpointInterface;
        private String url;
        @SerializedName("region_id")
        private String regionId;
    }

    private enum EndpointInterface {
        @SerializedName("public")
        PUBLIC,
        @SerializedName("admin")
        ADMIN,
        @SerializedName("internal")
        INTERNAL
    }
}
