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

package io.maestro3.agent.amqp.transformer;

import io.maestro3.agent.amqp.IntegrationChannels;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@Component
public class GzipTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(GzipTransformer.class);

    private static final String ZIPPED_HEADER_NAME = "zipped";
    private static final String ZIPPED_HEADER_VALUE = "zipped";

    /**
     * Zip message before sending to Outbound.PLAIN integration channel.
     *
     * @param message message with .json format payload
     * @return message with zipped format payload
     */
    @Transformer(inputChannel = IntegrationChannels.Outbound.ZIP, outputChannel = IntegrationChannels.Outbound.PLAIN)
    public Message<?> zipMessagePayload(Message<?> message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got OUTGOING message to zip with the following headers: {}", message.getHeaders());
        }

        Object payload = message.getPayload();
        if (!(payload instanceof String)) {
            LOG.info("Failed to zip  message payload of type {}", payload.getClass().getSimpleName());
            return message;
        }

        byte[] zippedPayload = zipPayload((String) payload);
        if (zippedPayload == null) {
            return message;
        }
        return MessageBuilder
                .withPayload(zippedPayload)
                .copyHeaders(message.getHeaders())
                .setHeader(ZIPPED_HEADER_NAME, ZIPPED_HEADER_VALUE)
                .build();
    }

    /**
     * Unzip message before sending to Inbound.DISPATCHER integration channel.
     *
     * @param message message with zipped format payload
     * @return message with .json format payload
     */
    @Transformer(inputChannel = IntegrationChannels.Inbound.ZIP, outputChannel = IntegrationChannels.Inbound.JSON)
    public Message<?> unzipMessagePayload(Message<?> message,
                                          @Header(value = ZIPPED_HEADER_NAME, required = false) String zipped) {
        LOG.debug("Got INGOING message to unzip with the following headers: {}", message.getHeaders());

        if (!ZIPPED_HEADER_VALUE.equals(zipped)) {
            return message;
        }
        Object payload = message.getPayload();
        String unzippedPayload;
        try {
            if (payload instanceof byte[]) {
                unzippedPayload = unzip((byte[]) payload);
            } else {
                LOG.error("Can't unzip payload type: {}", payload.getClass().getSimpleName());
                return message;
            }
        } catch (IOException e) {
            LOG.error("Failed to unzip message payload - {}", e.getMessage());
            return message;
        }

        return MessageBuilder
                .withPayload(unzippedPayload)
                .copyHeaders(message.getHeaders())
                .build();
    }

    private byte[] zipPayload(String payload) {
        GZIPOutputStream gzipStream = null;
        byte[] zippedPayload = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gzipStream = new GZIPOutputStream(out);
            gzipStream.write(payload.getBytes());
            gzipStream.finish();
            zippedPayload = out.toByteArray();

        } catch (IOException e) {
            LOG.error("Failed to gzip message payload - {}", e.getMessage());
        } finally {
            IOUtils.closeQuietly(gzipStream);
        }
        return zippedPayload;
    }

    private String unzip(byte[] payload) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(payload));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream));
        StringBuilder resultBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            resultBuilder.append(line);
        }
        return resultBuilder.toString();
    }

}
