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

package io.maestro3.agent.http.client;

import io.maestro3.agent.http.TrackingHttpClientWrapper;
import io.maestro3.agent.http.client.exception.SimpleHttpClientException;
import io.maestro3.agent.http.client.exception.SimpleHttpResponseException;
import io.maestro3.agent.http.client.parameters.TypedNameValue;
import io.maestro3.agent.ssl.FakeSSLSocketFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Deprecated
public class SimpleHttpClientImpl implements SimpleHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClientImpl.class);

    private final HttpClient httpClient;
    private final HttpHost defaultHost;

    /**
     * @param addFakeFactory add FakeSSLSocketFactory with default port 443 if true
     */
    public SimpleHttpClientImpl(HttpClient httpClient, String defaultHost, boolean addFakeFactory) {
        Assert.notNull(httpClient, "httpClient can't be null.");
        this.httpClient = httpClient;

        if (StringUtils.isNotBlank(defaultHost)) {
            this.defaultHost = Utils.extractHost(defaultHost);
        } else {
            this.defaultHost = null;
        }

        if (addFakeFactory && !(httpClient instanceof TrackingHttpClientWrapper)) {
            try {
                Scheme scheme = new Scheme("https", 443, FakeSSLSocketFactory.getInstance());
                this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
            } catch (Throwable ex) {
                throw new RuntimeException("Unable register ssl factory", ex);
            }
        }
    }

    public SimpleHttpClientImpl(HttpClient httpClient, String defaultHost) {
        this(httpClient, defaultHost, true);
    }

    public SimpleHttpClientImpl(HttpClient httpClient) {
        this(httpClient, null);
    }

    public SimpleHttpClientImpl(HttpClient httpClient, boolean addFakeFactory) {
        this(httpClient, null, addFakeFactory);
    }

    @Override
    public <T> T execute(Request request) throws SimpleHttpClientException {
        if (defaultHost == null) {
            throw new SimpleHttpClientException("Default host is not set.");
        }
        return this.execute(this.defaultHost, request, null);
    }

    @Override
    public <T> T execute(String host, Request request) throws SimpleHttpClientException {
        Assert.hasText(host, "host can't be null or empty.");
        Assert.notNull(request, "request can't be null.");

        return this.execute(host, request, null);
    }

    @Override
    public <T> T execute(String host, Request request, HeadersAccumulator accumulator) throws SimpleHttpClientException {
        Assert.hasText(host, "host can't be null or empty.");
        Assert.notNull(request, "request can't be null.");

        return this.execute(Utils.extractHost(host), request, accumulator);
    }

    private <T> T execute(HttpHost host, Request request, HeadersAccumulator accumulator) throws SimpleHttpClientException {
        LOG.debug("Executing request to '{}'...", String.valueOf(request.getUri()));

        HttpContext context = new BasicHttpContext();

        HttpRequest httpRequest;
        try {
            httpRequest = this.buildHttpRequest(request);
            if (request.getRequestHandler() != null) {
                request.getRequestHandler().process(httpRequest, context);
            }
        } catch (Throwable e) {
            throw new SimpleHttpClientException("Unable to create request.", e);
        }

        HttpResponse response;
        try {
            response = httpClient.execute(host, httpRequest, context);
        } catch (Throwable e) {
            httpClient.getConnectionManager().closeIdleConnections(0L, TimeUnit.MILLISECONDS);
            throw new SimpleHttpClientException("Unable to execute request.", e);
        }

        LOG.debug("Response received. statusCode={}", response.getStatusLine().getStatusCode());

        try {
            if (request.getResponseHandler() != null) {
                request.getResponseHandler().process(response);
            }
            T readResponse = this.readResponse(response, request);
            accumulateHeaders(response, accumulator);
            return readResponse;
        } catch (SimpleHttpResponseException e) {
            throw e;
        } catch (Throwable e) {
            throw new SimpleHttpClientException("Unable to read response.", e);
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    private void accumulateHeaders(HttpResponse response, HeadersAccumulator accumulator) {
        if (accumulator != null) {
            for (String header : accumulator.headers()) {
                Header firstHeader = response.getFirstHeader(header);
                if (firstHeader != null) {
                    accumulator.add(header, firstHeader.getValue());
                }
            }
        }
    }

    private void putHeaders(HttpRequest httpRequest, List<TypedNameValue<String, String>> headers) {
        if (headers != null) {
            for (TypedNameValue<String, String> header : headers) {
                httpRequest.addHeader(header.getName(), header.getValue());
            }
        }
    }

    private <T> T readResponse(HttpResponse response, Request request) throws Exception {
        if (request.getDeserializer() != null) {
            return request.getDeserializer().deserialize(request.getResponseType(), response);
        } else {
            return null;
        }
    }

    private HttpRequest buildHttpRequest(Request request) throws Exception {
        HttpRequest httpRequest;

        URI uri;
        try {
            uri = this.buildUri(request);
        } catch (URISyntaxException e) {
            throw new SimpleHttpClientException("Unable to build URI", e);
        }

        switch (request.getMethod()) {
            case POST: {
                HttpPost post = new HttpPost(uri);
                if (request.getData() != null && request.getSerializer() != null) {
                    request.getSerializer().serialize(request.getData(), post);
                }
                httpRequest = post;
                break;
            }
            case PATCH:
                HttpPatch patch = new HttpPatch(uri);
                if (request.getData() != null && request.getSerializer() != null) {
                    request.getSerializer().serialize(request.getData(), patch);
                }
                httpRequest = patch;
                break;
            case PUT: {
                HttpPut put = new HttpPut(uri);
                if (request.getData() != null && request.getSerializer() != null) {
                    request.getSerializer().serialize(request.getData(), put);
                }
                httpRequest = put;
                break;
            }
            case GET: {
                httpRequest = new HttpGet(uri);
                break;
            }
            case DELETE: {
                httpRequest = new HttpDelete(uri);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid method: " + String.valueOf(request.getMethod()));
            }
        }

        putHeaders(httpRequest, request.getHeaders());

        return httpRequest;
    }

    private URI buildUri(Request request) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(request.getUri());
        if (!CollectionUtils.isEmpty(request.getQueryParams())) {
            for (TypedNameValue<String, String> nv : request.getQueryParams()) {
                builder.addParameter(nv.getName(), nv.getValue());
            }
        }
        return builder.build();
    }
}
