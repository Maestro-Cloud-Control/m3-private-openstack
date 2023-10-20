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

package io.maestro3.agent.openstack.api;

import com.google.common.base.Function;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.helper.extractors.EndpointExtractor;
import org.apache.commons.collections4.MapUtils;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;


public abstract class BasicService {

    protected IOSClient client;
    protected ServiceType[] types;

    protected BasicService(ServiceType type, IOSClient client) {
        this(client, type);
    }

    protected BasicService(IOSClient client, ServiceType... types) {
        this.types = types;
        this.client = client;
    }

    protected URL endpoint() throws OSClientException {
        return getEndpoint(null, null);
    }

    protected URL endpoint(EndpointExtractor endpointExtractor) throws OSClientException {
        return getEndpoint(endpointExtractor, null);
    }

    protected URL endpoint(Function<URL, URL> endpointInterceptor) throws OSClientException {
        return getEndpoint(null, endpointInterceptor);
    }

    private URL getEndpoint(EndpointExtractor endpointExtractor, Function<URL, URL> endpointInterceptor) throws OSClientException {
        URL endpoint = null;
        for (ServiceType type : types) {
            endpoint = endpointExtractor == null
                ? client.getNullableEndpoint(type)
                : client.getNullableEndpoint(type, endpointExtractor);
            if (endpoint != null) {
                break;
            }
        }
        URL resultEndpoint = endpoint != null ? endpoint : client.getDefaultEndpoint();
        return endpointInterceptor == null
            ? resultEndpoint
            : endpointInterceptor.apply(resultEndpoint);
    }

    protected String getVersion() throws OSClientException {
        URL endpoint = endpoint();
        if (endpoint != null) {
            return endpoint.getPath();
        }
        return null;
    }

    protected static String pathWithFilter(String path, Map<String, List<String>> filter) {
        if (MapUtils.isNotEmpty(filter)) {
            path = path.concat("?").concat(toUriParams(filter));
        }
        return path;
    }

    private static String toUriParams(Map<String, List<String>> filter) {
        StringBuilder params = new StringBuilder();
        String separator = "";
        for (String key : filter.keySet()) {
            for (String value : filter.get(key)) {
                params.append(separator).append(key).append("=").append(value);
                separator = "&";
            }
        }
        return params.toString();
    }
}
