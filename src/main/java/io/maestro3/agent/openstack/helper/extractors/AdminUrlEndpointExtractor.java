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

package io.maestro3.agent.openstack.helper.extractors;

import io.maestro3.agent.model.identity.Endpoint;

import java.net.URL;


public class AdminUrlEndpointExtractor implements EndpointExtractor {

    private static final EndpointExtractor DEFAULT_ENDPOINT_EXTRACTOR = new DefaultEndpointExtractor();

    private final String version;

    private AdminUrlEndpointExtractor(String version) {
        this.version = version;
    }

    public static AdminUrlEndpointExtractor ifVersionIs(String version) {
        return new AdminUrlEndpointExtractor(version);
    }

    @Override
    public URL extract(Endpoint endpoint) {
        URL url = DEFAULT_ENDPOINT_EXTRACTOR.extract(endpoint);
        if (url == null) {
            return null;
        }

        URL adminURL = endpoint.getAdminURL();
        if (adminURL == null) {
            return url;
        }

        String path = url.getPath();
        if (path.startsWith(version) || path.startsWith("/".concat(version))) {
            return adminURL;
        }
        return url;
    }
}
