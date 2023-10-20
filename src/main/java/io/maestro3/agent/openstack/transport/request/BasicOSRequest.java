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

package io.maestro3.agent.openstack.transport.request;

import io.maestro3.agent.http.client.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public class BasicOSRequest<T> implements IOSRequest<T> {

    private String path;
    private String host;
    private Type responseType;
    private RequestMethod method = RequestMethod.GET;
    private Object content;
    private Set<String> headersToRetrieve = new LinkedHashSet<>();

    private BasicOSRequest() {
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public RequestMethod getMethod() {
        return method;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public Type getResponseType() {
        return responseType;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Set<String> getHeadersToRetrieve() {
        return headersToRetrieve;
    }

    public static <T> BasicOSRequestBuilder<T> builder(Class<T> responseType, URL endpoint) {
        return new BasicOSRequestBuilder<>(responseType, endpoint);
    }

    public static <T> BasicOSRequestBuilder<T> builder(Type responseType, URL endpoint) {
        return new BasicOSRequestBuilder<>(responseType, endpoint);
    }

    public static class BasicOSRequestBuilder<T> {

        private BasicOSRequest<T> request;
        private String endpointHost;
        private String endpointPath;

        BasicOSRequestBuilder(Type responseType, URL endpoint) {
            request = new BasicOSRequest<>();
            request.responseType = responseType;
            endpoint(endpoint);
        }

        public BasicOSRequest<T> create() {
            populateHostAndPath();
            return request;
        }

        private void endpoint(URL endpoint) {
            endpointHost = extractHttpHost(endpoint);
            endpointPath = endpoint.getPath();
        }

        public BasicOSRequestBuilder<T> path(String path, Object... params) {
            request.path = url(path, params);
            return this;
        }

        public BasicOSRequestBuilder<T> delete() {
            request.method = RequestMethod.DELETE;
            return this;
        }

        public BasicOSRequestBuilder<T> patch(Object content) {
            request.method = RequestMethod.PATCH;
            request.content = content;
            return this;
        }

        public BasicOSRequestBuilder<T> post(Object content) {
            request.method = RequestMethod.POST;
            request.content = content;
            return this;
        }

        public BasicOSRequestBuilder<T> put(Object content) {
            request.method = RequestMethod.PUT;
            request.content = content;
            return this;
        }

        public BasicOSRequestBuilder<T> headers(String... headers) {
            if (headers != null && headers.length != 0) {
                request.headersToRetrieve.addAll(Arrays.asList(headers));
            }
            return this;
        }

        private String extractHttpHost(URL url) {
            String protocol = url.getProtocol();
            String authority = url.getAuthority();
            return protocol + "://" + authority;
        }

        private void populateHostAndPath() {
            request.host = endpointHost;
            String finalPath = request.path;
            if (StringUtils.isNotEmpty(endpointPath)) {
                finalPath = endpointPath;
                if (StringUtils.isNotEmpty(request.path)) {
                    finalPath += request.path;
                }
            }
            request.path = finalPath;
        }

        private String url(String url, Object... params) {
            if (params == null || params.length == 0) {
                return url;
            }
            return String.format(url, params);
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "path='" + path + '\'' +
                ", host='" + host + '\'' +
                ", method=" + method +
                '}';
    }

    public interface Headers {
        String LOCATION = "Location";
    }
}
