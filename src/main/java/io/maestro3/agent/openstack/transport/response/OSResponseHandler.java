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

package io.maestro3.agent.openstack.transport.response;

import io.maestro3.agent.http.client.exception.SimpleHttpClientException;
import io.maestro3.agent.http.client.handler.ResponseHandler;
import io.maestro3.agent.openstack.exception.OSResponseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OSResponseHandler implements ResponseHandler {

    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(".*message\":\\s\"([^\"]+)\".*");

    private static final Logger logger = LoggerFactory.getLogger(OSResponseHandler.class);

    @Override
    public void process(HttpResponse response) throws SimpleHttpClientException {
        int code = response.getStatusLine().getStatusCode();

        if (code == HttpStatus.SC_UNAUTHORIZED) {
            throw new OSResponseException(code, "Unauthorized");
        }
        if (code >= HttpStatus.SC_BAD_REQUEST) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String errorMsg = buildErrorMessage(entity);
                throw new OSResponseException(code, errorMsg);
            }
        }
    }

    private String buildErrorMessage(HttpEntity entity) {
        try {
            String message = "Unknown error";
            String json = EntityUtils.toString(entity);
            if (json != null && json.contains("message")) {
                Matcher m = ERROR_MESSAGE_PATTERN.matcher(json);
                if (m.matches()) {
                    message = m.group(1);
                }
            }
            return message;
        } catch (IOException e) {
            logger.error("Can not parse response. Error message: {}", e.getMessage());
            throw new RuntimeException("Can not parse response");
        }
    }
}
