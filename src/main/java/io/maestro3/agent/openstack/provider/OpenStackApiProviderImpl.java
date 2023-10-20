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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.factory.CloseableHttpClientFactory;
import io.maestro3.agent.http.tracker.IHttpRequestTracker;
import io.maestro3.agent.model.OpenStackUserInfo;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.region.OpenStackServiceTenantInfo;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.OpenStackApi;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.client.impl.OSClient;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


@Component
public class OpenStackApiProviderImpl implements OpenStackApiProvider {

    private static final int MAX_CACHE_SIZE = 20;

    /**
     * Cache of OSClient`s for OpenStack API.
     */
    private Cache<String, IOSClient> clientsCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    /**
     * Cache of HTTP clients for OpenStack API.
     */
    private final Cache<String, HttpClient> httpClientsCache = CacheBuilder.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .build();

    private final IHttpRequestTracker requestTracker;

    @Autowired
    public OpenStackApiProviderImpl(IHttpRequestTracker requestTracker) {
        this.requestTracker = requestTracker;
    }

    @Override
    public IOpenStackApi openStack(OpenStackTenant tenant, OpenStackRegionConfig region)
        throws M3PrivateAgentException {
        OpenStackUserInfo userInfo = tenant.getUserInfo();
        return new OpenStackApi(
            getClient(region.getKeystoneAuthUrl(),
                region.getId(),
                userInfo.getName(),
                userInfo.getPassword(),
                tenant.getNativeName(),
                region.getNativeRegionName(),
                userInfo.getDomainName(),
                tenant.getDomainName(),
                region.getOsVersion()));
    }

    @Override
    public IOpenStackApi openStackNoCache(OpenStackApiRequest request)
        throws M3PrivateAgentException {
        return new OpenStackApi(
            getClientNoCache(
                request.getAuthUrl(), request.getUser(), request.getPassword(), request.getTenant(), request.getRegionName(),
                request.getUserDomainName(), request.getTenantDomainName(), request.getVersion(), request.getTimeout()));
    }


    @Override
    public IOpenStackApi adminOpenStack(OpenStackRegionConfig region) throws M3PrivateAgentException {
        OpenStackUserInfo userInfo = region.getAdminUserCredentials();
        OpenStackServiceTenantInfo serviceTenantInfo = region.getServiceTenantInfo();
        return new OpenStackApi(
            getClient(region.getKeystoneAuthUrl(),
                region.getId(),
                userInfo.getName(),
                userInfo.getPassword(),
                serviceTenantInfo.getName(),
                region.getNativeRegionName(),
                userInfo.getDomainName(),
                "",
                region.getOsVersion()));
    }

    public IOSClient getClientNoCache(String authUrl, String user, String password, String tenant, String regionName,
                                      String userDomainName, String tenantDomainName, OpenStackVersion version, int timeout)
        throws M3PrivateAgentException {

        return OSClient.builder(CloseableHttpClientFactory.getHttpClient(timeout))
            .authUrl(authUrl)
            .username(user)
            .password(password)
            .tenantName(tenant)
            .regionName(regionName)
            .userDomainName(userDomainName)
            .tenantDomainName(tenantDomainName)
            .osVersion(version)
            .build();
    }

    private IOSClient getClient(String authUrl, String regionId, String user, String password, String tenant, String regionName,
                                String userDomainName, String tenantDomainName, OpenStackVersion version)
        throws M3PrivateAgentException {
        String clientHash = createClientHash(authUrl, user, password, tenant, regionName, userDomainName, tenantDomainName);
        IOSClient client = clientsCache.getIfPresent(clientHash);
        if (client != null) {
            return client;
        }

        synchronized (this) {
            client = clientsCache.getIfPresent(clientHash);
            if (client == null) {
                client = OSClient.builder(getHttpClient(authUrl, regionId))
                    .authUrl(authUrl)
                    .username(user)
                    .password(password)
                    .tenantName(tenant)
                    .regionName(regionName)
                    .userDomainName(userDomainName)
                    .tenantDomainName(tenantDomainName)
                    .osVersion(version)
                    .build();
                clientsCache.put(clientHash, client);
            }
            return client;
        }
    }

    private HttpClient getHttpClient(String authUrl, String regionId) {
        HttpClient httpClient = httpClientsCache.getIfPresent(authUrl);
        if (httpClient != null) {
            return httpClient;
        }

        synchronized (httpClientsCache) {
            httpClient = httpClientsCache.getIfPresent(authUrl);
            if (httpClient != null) {
                return httpClient;
            }
            httpClient = CloseableHttpClientFactory.getHttpClient(regionId, requestTracker);
            httpClientsCache.put(authUrl, httpClient);
            return httpClient;
        }
    }

    private String createClientHash(String authUrl, String user, String password, String tenant, String regionName,
                                    String userDomainName, String tenantDomainName)
        throws M3PrivateAgentException {
        String hash = authUrl + user + password + tenant + regionName + userDomainName + tenantDomainName;
        try {
            return new String(MessageDigest.getInstance("MD5").digest(hash.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new M3PrivateAgentException("Unexpected error." + e.getMessage());
        }
    }
}
