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

package io.maestro3.agent.amqp;


public interface Headers {

    /**
     * Message eventGroup header. Used to dedicate right handler for events.
     */
    String EVENT_GROUP = "eventGroup";

    /**
     * Message eventGroups header. Used to dedicate right handler for events.
     */
    String EVENT_GROUPS = "eventGroups";

    /**
     * Message event type. Used to classify event.
     */
    String EVENT_TYPE = "eventType";

    /**
     * Header, that mark message as with zipped payload. Used to send zipped messages
     * to {@link io.maestro3.agent.amqp.integration.transformer.GzipTransformer}.
     */
    String ZIPPED = "zipped";

    /**
     * Header, that used to name a callback queue
     */
    String REPLY_TO = "amqp_replyTo";
    /**
     * Header, that used to correlate responses with requests
     */
    String CORRELATION_ID = "correlationId";


    String ROUTING_KEY = "routingKey";

}
