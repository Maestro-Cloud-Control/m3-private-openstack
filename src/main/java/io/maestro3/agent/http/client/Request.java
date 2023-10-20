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
import java.util.List;


public class Request {
    private RequestMethod method;
    private String uri;
    private Object data;
    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;
    private Type responseType;
    private List<TypedNameValue<String, String>> headers;
    private List<TypedNameValue<String, String>> queryParams;
    private Deserializer deserializer;
    private Serializer serializer;

    public Request() {
        //json
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public Type getResponseType() {
        return responseType;
    }

    public void setResponseType(Type responseType) {
        this.responseType = responseType;
    }

    public List<TypedNameValue<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<TypedNameValue<String, String>> headers) {
        this.headers = headers;
    }

    public List<TypedNameValue<String, String>> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<TypedNameValue<String, String>> queryParams) {
        this.queryParams = queryParams;
    }

    public Deserializer getDeserializer() {
        return deserializer;
    }

    public void setDeserializer(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
