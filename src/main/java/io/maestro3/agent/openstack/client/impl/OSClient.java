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

package io.maestro3.agent.openstack.client.impl;

import io.maestro3.agent.http.client.HeadersAccumulator;
import io.maestro3.agent.http.client.Request;
import io.maestro3.agent.http.client.RequestBuilder;
import io.maestro3.agent.http.client.SimpleHttpClient;
import io.maestro3.agent.http.client.SimpleHttpClientImpl;
import io.maestro3.agent.http.client.exception.SimpleHttpClientException;
import io.maestro3.agent.http.client.handler.RequestHandler;
import io.maestro3.agent.http.client.handler.ResponseHandler;
import io.maestro3.agent.http.client.serialization.Deserializer;
import io.maestro3.agent.http.client.serialization.Serializer;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.identity.Access;
import io.maestro3.agent.model.identity.Endpoint;
import io.maestro3.agent.model.identity.KeystoneDomainCredentials;
import io.maestro3.agent.model.identity.Service;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneCredentials;
import io.maestro3.agent.openstack.api.identity.bean.v2.Auth;
import io.maestro3.agent.openstack.api.identity.bean.v2.KeystoneAccess;
import io.maestro3.agent.openstack.api.identity.bean.v3.V3Auth;
import io.maestro3.agent.openstack.api.identity.bean.v3.V3KeystoneAccess;
import io.maestro3.agent.openstack.client.IClientMetadata;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.client.IOSClientOption;
import io.maestro3.agent.openstack.client.OSClientOption;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.exception.OSResponseException;
import io.maestro3.agent.openstack.exception.OpenStackConflictException;
import io.maestro3.agent.openstack.helper.extractors.DefaultEndpointExtractor;
import io.maestro3.agent.openstack.helper.extractors.EndpointExtractor;
import io.maestro3.agent.openstack.transport.HeaderConstants;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;
import io.maestro3.agent.openstack.transport.request.OSRequestInterceptor;
import io.maestro3.agent.openstack.transport.response.BasicOSResponse;
import io.maestro3.agent.openstack.transport.response.IOSResponse;
import io.maestro3.agent.openstack.transport.response.OSResponseHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OSClient implements IOSClient {

    private static final int CLIENT_ID_LENGTH = 10;
    private static final char[] CLIENT_ID_CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static final IOSClientOption DEFAULT_OS_CLIENT_OPTION = OSClientOption.builder().build();

    private static final DefaultEndpointExtractor ENDPOINT_EXTRACTOR = new DefaultEndpointExtractor();
    private static final String V3_TOKEN_HEADER = "X-Subject-Token";

    private String id; // for caching purposes
    private URL authUrl;
    private String regionName; // OpenStack region name
    private KeystoneDomainCredentials credentials;
    private Access access;
    private SimpleHttpClient client; //
    private Map<ServiceType, Endpoint> serviceEndpointsCached;
    private RequestHandler interceptor;
    private ResponseHandler responseHandler;
    private OpenStackVersion osVersion;
    private boolean v3Auth;

    private OSClient() {
    }

    @Override
    public IClientMetadata getMetadata() throws OSClientException {
        ensureAuthorized();
        return new ClientMetadata(id, access.getTokenUserId(), access.getTokenProjectId());
    }

    @Override
    public synchronized <T> IOSResponse<T> execute(IOSRequest<T> request) throws OSClientException {
        return execute(request, DEFAULT_OS_CLIENT_OPTION);
    }

    @Override
    public synchronized <T> IOSResponse<T> execute(IOSRequest<T> request, IOSClientOption option) throws OSClientException {
        ensureAuthorized();
        return execute(request, true, false, option);
    }

    /**
     * Executes Open Stack requests, handles unauthorized exception.
     *
     * @param request       request
     * @param retryAuth     if enabled will attempt to reauthorize in case SC_UNAUTHORIZED (401) status code received.
     *                      If authorization attempt fails just throw exception explaining failure reasons.
     * @param authorization shows if request is an authorization request
     * @param <T>           type of response
     * @return response enclosing result entity
     * @throws OSClientException Open Stack client exception
     */
    private <T> IOSResponse<T> execute(IOSRequest<T> request, boolean retryAuth, boolean authorization) throws OSClientException {
        return execute(request, retryAuth, authorization, DEFAULT_OS_CLIENT_OPTION);
    }

    private <T> IOSResponse<T> execute(IOSRequest<T> request, boolean retryAuth, boolean authorization, IOSClientOption option) throws OSClientException {
        IOSResponse<T> response = new BasicOSResponse<>();
        T entity;
        try {
            BasicOSResponse<T> basicResponse = (BasicOSResponse<T>) response;
            Request buildRequest = buildRequest(request, authorization, option);
            if (CollectionUtils.isNotEmpty(request.getHeadersToRetrieve())) {
                HeadersAccumulator accumulator = new HeadersAccumulator(request.getHeadersToRetrieve());
                entity = client.execute(request.getHost(), buildRequest, accumulator);
                basicResponse.setHeaders(accumulator.retrieve());
            } else {
                entity = client.execute(request.getHost(), buildRequest);
            }
            basicResponse.setEntity(entity);
        } catch (Exception e) {
            response = handle(e, request, retryAuth);
        }
        return response;
    }

    private <T> IOSResponse<T> handle(Exception e, IOSRequest<T> req, boolean retryAuth) throws OSClientException {
        if (e instanceof SimpleHttpClientException) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof OSResponseException) {
                return handleResponseException((OSResponseException) cause, req, retryAuth);
            } else {
                throw new OSClientException(cause);
            }
        }
        throw new OSClientException("Unable to execute request", e);
    }

    private <T> IOSResponse<T> handleResponseException(OSResponseException e, IOSRequest<T> req, boolean retryAuth) throws OSClientException {
        if (retryAuth && e.isNotAuthorized()) {
            authorize();
            return execute(req, false, true);
        } else if (e.notFound()) {
            return BasicOSResponse.emptyResponse();
        } else if (e.conflict()) {
            throw new OpenStackConflictException(e.getMessage());
        } else {
            throw new OSClientException(e.getMessage(), e.getCode());
        }
    }

    private <T> Request buildRequest(IOSRequest<T> request, boolean authorization, IOSClientOption option) throws OSClientException {
        RequestBuilder builder = new RequestBuilder();

        Serializer serializer = option.getSerializer();
        Deserializer deserializer = option.getDeserializer();

        builder.toUri(request.getPath()).
            as(request.getResponseType()).
            deserializer(deserializer).
            with(interceptor).
            with(responseHandler);

        switch (request.getMethod()) {
            case POST:
                builder.post(request.getContent()).serializer(serializer);
                break;
            case GET:
                builder.get();
                break;
            case DELETE:
                builder.delete();
                break;
            case PUT:
                builder.put(request.getContent()).serializer(serializer);
                break;
            case PATCH:
                builder.patch(request.getContent()).serializer(serializer);
                break;
            default:
                throw new OSClientException("Unsupported request method: " + request.getMethod());
        }

        if (!authorization) {
            builder.header(HeaderConstants.X_AUTH_TOKEN, access.getToken().getId());
        }

        return builder.create();
    }

    @Override
    public synchronized URL getNullableEndpoint(ServiceType type) throws OSClientException {
        return getNullableEndpoint(type, ENDPOINT_EXTRACTOR);
    }

    @Override
    public synchronized URL getNullableEndpoint(ServiceType type, EndpointExtractor endpointExtractor) throws OSClientException {
        ensureAuthorized();
        if (serviceEndpointsCached.containsKey(type)) {
            Endpoint endpoint = serviceEndpointsCached.get(type);
            return endpointExtractor.extract(endpoint);
        }
        List<Service> serviceCatalog = access.getServiceCatalog();
        for (Service service : serviceCatalog) {
            List<Endpoint> serviceEndpoints = service.getEndpoints();
            if (service.getServiceType() == type) {
                if (CollectionUtils.isNotEmpty(serviceEndpoints)) {
                    Endpoint endpoint = serviceEndpoints.get(0);
                    serviceEndpointsCached.put(service.getServiceType(), endpoint);
                    return endpointExtractor.extract(endpoint);
                }
            }
        }
        return null;
    }

    @Override
    public URL getDefaultEndpoint() {
        return authUrl;
    }

    private synchronized void ensureAuthorized() throws OSClientException {
        DateTime inTwoMinutes = new DateTime().plusMinutes(2);
        // access new token if existing expires in 2 minutes
        if (access == null || access.getToken().getExpires().before(inTwoMinutes.toDate())) {
            authorize();
        }
    }

    /**
     * Authorizes the client.
     * By calling this method you supply client with the authorization key that is required to perform any API requests.
     *
     * @return authorized client
     * @throws OSClientException Open Stack client exception
     */
    private synchronized IOSClient authorize() throws OSClientException {
        if (v3Auth) {
            IOSRequest<V3KeystoneAccess> authorize = BasicOSRequest.builder(V3KeystoneAccess.class, authUrl)
                .path("/auth/tokens")
                .post(new V3Auth(credentials.getUsername(), credentials.getPassword(), credentials.getTenantName(),
                    credentials.getUserDomainName(), credentials.getTenantDomainName(), osVersion))
                .headers(V3_TOKEN_HEADER)
                .create();
            IOSResponse<V3KeystoneAccess> response = execute(authorize, false, true);
            Map<String, String> headers = response.getHeaders();

            V3KeystoneAccess entity = response.getEntity();
            entity.setToken(headers.get(V3_TOKEN_HEADER));
            entity.setRegionId(regionName);
            access = entity;
            return this;
        } else {
            KeystoneCredentials credentials = new KeystoneCredentials(this.credentials.getUsername(), this.credentials.getPassword());
            credentials.setTenantName(this.credentials.getTenantName());
            IOSRequest<AccessWrapper> authorize = BasicOSRequest.builder(AccessWrapper.class, authUrl)
                .path("/tokens")
                .post(new Auth(credentials))
                .create();
            AccessWrapper entity = execute(authorize, false, true).getEntity();
            access = entity == null ? null : entity.access;
            return this;
        }
    }

    private static class AccessWrapper {
        private KeystoneAccess access;
    }

    public static Builder builder(HttpClient httpClient) {
        return new Builder(httpClient);
    }

    public static class Builder {
        private final HttpClient httpClient;
        private String authUrl;
        private String username;
        private String password;
        private String tenantName;
        private String userDomainName;
        private String tenantDomainName;
        private String regionName;
        private OpenStackVersion osVersion;

        private Builder(HttpClient httpClient) {
            Assert.notNull(httpClient, "httpClient cannot be null.");
            this.httpClient = httpClient;
        }

        public Builder authUrl(String authUrl) {
            Assert.hasText(authUrl, "authUrl cannot be null or empty.");
            this.authUrl = authUrl;
            return this;
        }

        public Builder osVersion(OpenStackVersion osVersion) {
            Assert.notNull(osVersion, "osVersion cannot be null or empty.");
            this.osVersion = osVersion;
            return this;
        }

        public Builder username(String username) {
            Assert.hasText(username, "username cannot be null or empty.");
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            Assert.hasText(password, "password cannot be null or empty.");
            this.password = password;
            return this;
        }

        public Builder tenantName(String tenantName) {
            Assert.hasText(tenantName, "tenantName cannot be null or empty.");
            this.tenantName = tenantName;
            return this;
        }

        public Builder userDomainName(String userDomainName) {
            this.userDomainName = userDomainName;
            return this;
        }

        public Builder tenantDomainName(String tenantDomainName) {
            this.tenantDomainName = tenantDomainName;
            return this;
        }

        public Builder regionName(String regionName) {
            if (StringUtils.isNotBlank(regionName)) {
                this.regionName = regionName;
            }
            return this;
        }

        public OSClient build() {
            Assert.hasText(authUrl, "authUrl cannot be null or empty.");
            Assert.hasText(username, "username cannot be null or empty.");
            Assert.hasText(password, "password cannot be null or empty.");
            Assert.hasText(tenantName, "tenantName cannot be null or empty.");

            OSClient client = new OSClient();
            client.authUrl = getAuthUrl();
            client.v3Auth = isV3AuthSupported(client.authUrl);
            client.credentials = new KeystoneDomainCredentials(username, password);
            client.credentials.setTenantName(tenantName);
            client.credentials.setUserDomainName(userDomainName);
            client.credentials.setTenantDomainName(tenantDomainName);
            client.client = new SimpleHttpClientImpl(httpClient, false);
            client.interceptor = new OSRequestInterceptor();
            client.serviceEndpointsCached = new HashMap<>();
            client.osVersion = osVersion;
            client.responseHandler = new OSResponseHandler();
            client.id = RandomStringUtils.random(CLIENT_ID_LENGTH, CLIENT_ID_CHARS);
            client.regionName = regionName;
            return client;
        }

        private boolean isV3AuthSupported(URL authUrl) {
            String authUrlPath = authUrl.getPath().replace("/", StringUtils.EMPTY);
            return authUrlPath.endsWith("v3");
        }

        private URL getAuthUrl() {
            try {
                return new URL(authUrl);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Malformed URL specified. ", e);
            }
        }
    }
}
