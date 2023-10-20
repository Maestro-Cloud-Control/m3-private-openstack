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

import io.maestro3.agent.http.client.handler.RequestHandler;
import io.maestro3.agent.openstack.transport.HeaderConstants;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;


public class OSRequestInterceptor implements RequestHandler {

    @Override
    public void process(HttpRequest request, HttpContext ignore) {
        request.removeHeaders(HeaderConstants.CONTENT_LANGUAGE);
        request.removeHeaders(HeaderConstants.CONTENT_ENCODING);
        request.addHeader(HeaderConstants.CONTENT_TYPE, HeaderConstants.CONTENT_TYPE_JSON);
    }
}
