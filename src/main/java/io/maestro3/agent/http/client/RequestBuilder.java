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

import io.maestro3.agent.http.client.handler.RequestHandler;
import io.maestro3.agent.http.client.handler.ResponseHandler;
import io.maestro3.agent.http.client.parameters.TypedNameValue;
import io.maestro3.agent.http.client.serialization.Deserializer;
import io.maestro3.agent.http.client.serialization.Serializer;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class RequestBuilder {

    private final Request request = new Request();

    public RequestBuilder() {
        //json
    }

    public RequestBuilder toUri(String uri) {
        this.request.setUri(uri);
        return this;
    }

    public RequestBuilder with(RequestHandler handler) {
        this.request.setRequestHandler(handler);
        return this;
    }

    public RequestBuilder with(ResponseHandler handler) {
        this.request.setResponseHandler(handler);
        return this;
    }

    public RequestBuilder withQueryParams(Map<String, String> inputParams) {
        if (inputParams == null || inputParams.size() < 1) {
            return this;
        }
        List<TypedNameValue<String, String>> queryParams = request.getQueryParams();
        if (queryParams == null) {
            queryParams = new LinkedList<>();
        }
        for (Map.Entry<String, String> entry : inputParams.entrySet()) {
            queryParams.add(new TypedNameValue<>(entry.getKey(), entry.getValue()));
        }
        request.setQueryParams(queryParams);
        return this;
    }

    public RequestBuilder serializer(Serializer serializer) {
        this.request.setSerializer(serializer);
        return this;
    }

    public RequestBuilder deserializer(Deserializer deserializer) {
        this.request.setDeserializer(deserializer);
        return this;
    }

    public RequestBuilder post(Object data) {
        this.request.setData(data);
        this.request.setMethod(RequestMethod.POST);
        return this;
    }

    public RequestBuilder get() {
        this.request.setMethod(RequestMethod.GET);
        return this;
    }

    public RequestBuilder as(Type responseType) {
        this.request.setResponseType(responseType);
        return this;
    }

    public RequestBuilder header(String name, String value) {
        return this.header(new TypedNameValue<>(name, value));
    }

    public RequestBuilder header(TypedNameValue<String, String> header) {
        List<TypedNameValue<String, String>> headers = request.getHeaders();
        if (headers == null) {
            headers = new LinkedList<>();
            request.setHeaders(headers);
        }
        headers.add(header);
        return this;
    }

    public RequestBuilder put(Object data) {
        this.request.setMethod(RequestMethod.PUT);
        this.request.setData(data);
        return this;
    }

    public RequestBuilder patch(Object data) {
        this.request.setMethod(RequestMethod.PATCH);
        this.request.setData(data);
        return this;
    }

    public RequestBuilder delete() {
        this.request.setMethod(RequestMethod.DELETE);
        return this;
    }

    public RequestBuilder method(RequestMethod requestMethod) {
        this.request.setMethod(requestMethod);
        return this;
    }

    public RequestBuilder data(Object data) {
        this.request.setData(data);
        return this;
    }

    public RequestBuilder queryParam(TypedNameValue<String, String> param) {
        List<TypedNameValue<String, String>> params = request.getQueryParams();
        if (params == null) {
            params = new LinkedList<>();
        }
        params.add(param);
        request.setQueryParams(params);
        return this;
    }

    public RequestBuilder queryParam(String name, String value) {
        return this.queryParam(new TypedNameValue<>(name, value));
    }

    public Request create() {
        return this.request;
    }
}
