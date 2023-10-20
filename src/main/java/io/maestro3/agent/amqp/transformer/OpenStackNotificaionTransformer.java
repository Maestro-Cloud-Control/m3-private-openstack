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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import io.maestro3.agent.amqp.IntegrationChannels;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.agent.model.notification.NullPayload;
import io.maestro3.agent.model.notification.OsloMessage;
import io.maestro3.sdk.v3.core.M3ApiAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;


@Component
public class OpenStackNotificaionTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenStackNotificaionTransformer.class);

    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .registerTypeAdapter(
                    EventType.class,
                    (JsonDeserializer<EventType>) (json, typeOfT, context) -> getEventTypeInternal(json))
            .create();

    /**
     * Convert message payload from {@link M3ApiAction} to .json before sending to Outbound.ZIP integration channel.
     *
     * @param message message with {@link M3ApiAction} as payload
     * @return message with .json format payload
     */
    @Transformer(inputChannel = IntegrationChannels.Inbound.OS_BYTES,
            outputChannel = IntegrationChannels.Inbound.OS_NOTIFICATIONS)
    public Message<?> transform(Message<byte[]> message,
                                @Header(value = "amqp_contentEncoding", required = false) String encoding,
                                @Header(value = "amqp_contentType", required = false) String contentType) {
        byte[] bytePayload = message.getPayload();
        String stringPayload = null;
        try {
            stringPayload = new String(bytePayload, encoding);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
        }

        String notificationJson;
        OsloMessage osloMessage = gson.fromJson(stringPayload, OsloMessage.class);
        if (osloMessage != null && osloMessage.getMessage() != null) {
            notificationJson = osloMessage.getMessage();
        } else {
            notificationJson = stringPayload;
        }

        LOG.debug("Received os notification: {}", notificationJson);

        Notification notification;
        if (StringUtils.isNotBlank(notificationJson)) {
            notification = gson.fromJson(notificationJson, Notification.class);
            if (notification != null) {
                notification.setOriginalJson(stringPayload);
                Map<String, Object> notificationMap = gson.fromJson(stringPayload, mapType);
                notification.setOriginalNotification(notificationMap);
                return MessageBuilder.withPayload(notification).copyHeaders(message.getHeaders()).build();
            }
        }
        LOG.error("Got empty payload or this payload has illegal structure: {}", notificationJson);
        return MessageBuilder.withPayload(NullPayload.getInstance()).build();
    }

    @Filter(inputChannel = IntegrationChannels.Inbound.OS_NOTIFICATIONS,
            outputChannel = IntegrationChannels.Inbound.OS_NOTIFICATIONS_ENCODED)
    public boolean accept(Message<?> message,
                          @Header(value = "amqp_contentEncoding", required = false) String encoding) {
        boolean accept = true;
        if (StringUtils.isBlank(encoding)) {
            accept = false;
            LOG.error("[OS_CONTENT_ENCODING_FILTER] Rejecting message with empty 'amqp_contentEncoding' header. " +
                    "Enable 'debug' level to see the message");
            if (LOG.isDebugEnabled()) {
                LOG.debug("[OS_CONTENT_ENCODING_FILTER] Rejected message -> {}", message);
            }
        }
        return accept;
    }

    private EventType getEventTypeInternal(JsonElement json) {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected a string, got " + json);
        }
        JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
        if (!jsonPrimitive.isString()) {
            throw new JsonParseException("Expected a string, got " + json);
        }
        String eventName = jsonPrimitive.getAsString();
        EventType eventType = EventType.getByName(eventName);
        if (eventType == null) {
            LOG.warn("Got unknown type of event -> {}", eventName);
        }
        return eventType;
    }
}
