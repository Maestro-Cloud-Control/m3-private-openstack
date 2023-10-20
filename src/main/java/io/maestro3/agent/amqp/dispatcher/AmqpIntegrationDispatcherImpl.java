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

package io.maestro3.agent.amqp.dispatcher;

import io.maestro3.agent.amqp.IntegrationChannels;
import io.maestro3.agent.amqp.handler.OsNotificationReactor;
import io.maestro3.agent.model.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;


@Component
public class AmqpIntegrationDispatcherImpl implements AmqpIntegrationDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpIntegrationDispatcherImpl.class);

    private final OsNotificationReactor osNotificationReactor;

    @Autowired
    public AmqpIntegrationDispatcherImpl(OsNotificationReactor osNotificationReactor) {
        this.osNotificationReactor = osNotificationReactor;
    }

    @Override
    @ServiceActivator(inputChannel = IntegrationChannels.Inbound.OS_NOTIFICATIONS_ENCODED, autoStartup = "true")
    public void osNotificationDispatch(Notification notification) {
        try {
            osNotificationReactor.reactOnNotification(notification);
        } catch (Exception e) {
            LOG.error("OpenStack event '{}' processing failed", notification.getEventType(), e);
        }
    }

}
