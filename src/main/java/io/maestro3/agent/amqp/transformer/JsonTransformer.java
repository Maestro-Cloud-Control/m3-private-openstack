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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.maestro3.agent.amqp.IntegrationChannels;
import io.maestro3.sdk.v3.core.M3ApiAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class JsonTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(JsonTransformer.class);
    private static final String PAYLOAD_TYPE = "payloadType";

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert message payload from .json to {@link M3ApiAction} before sending to Inbound.PRIVATE_CLOUD integration channel.
     *
     * @param message message with .json format payload
     * @return message with {@link M3ApiAction} payload
     */
    @Transformer(inputChannel = IntegrationChannels.Inbound.JSON, outputChannel = IntegrationChannels.Inbound.PRIVATE_CLOUD)
    public Message<?> fromJson(Message<?> message) {
        LOG.debug("Got INGOING message to convert with the following headers: {}", message.getHeaders());
        try {
            String payload = (String) message.getPayload();

            M3ApiAction m3ApiAction = objectMapper.readValue(payload, M3ApiAction.class);

            LOG.debug("Message headers : {}", message.getHeaders());
            return MessageBuilder.
                withPayload(m3ApiAction)
                .copyHeaders(message.getHeaders())
                .removeHeader(PAYLOAD_TYPE)
                .build();
        } catch (Throwable e) {
            LOG.error("Unable to deserialize payload: {}", message.getPayload());
            throw new MessageTransformationException("Unable to deserialize payload.", e);
        }
    }

    /**
     * Convert message payload from {@link M3ApiAction} to .json before sending to Outbound.ZIP integration channel.
     *
     * @param message message with {@link M3ApiAction} as payload
     * @return message with .json format payload
     */
    @Transformer(inputChannel = IntegrationChannels.Outbound.JSON, outputChannel = IntegrationChannels.Outbound.ZIP)
    public Message<?> toJson(Message<?> message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got OUTGOING message to convert with the following headers: {}", message.getHeaders());
        }
        try {
            String stringPayload = null;
            Object payload = message.getPayload();

            if (payload != null) {
                stringPayload = objectMapper.writeValueAsString(payload);
            }
            return MessageBuilder.
                withPayload(Optional.ofNullable(stringPayload)
                    .orElseThrow(IllegalArgumentException::new)).
                copyHeaders(message.getHeaders()).
                build();
        } catch (Throwable e) {
            LOG.error("Unable to serialize payload: {}", message.getPayload());
            throw new MessageTransformationException("Unable to serialize payload.", e);
        }
    }
}
