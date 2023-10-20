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

package io.maestro3.agent.amqp.handler;

import io.maestro3.agent.cadf.CadfAuditEventSender;
import io.maestro3.agent.cadf.openstack.INotificationConverterFactory;
import io.maestro3.agent.cadf.openstack.converter.IQualifiedCadfConverter;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.cadf.model.CadfAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class OsNotificationReactorImpl implements OsNotificationReactor {

    private static final Logger LOG = LoggerFactory.getLogger(OsNotificationReactorImpl.class);

    private final CadfAuditEventSender eventSender;
    private final INotificationConverterFactory notificationConverterFactory;


    @Autowired
    public OsNotificationReactorImpl(CadfAuditEventSender eventSender,
                                     INotificationConverterFactory notificationConverterFactory) {
        this.eventSender = eventSender;
        this.notificationConverterFactory = notificationConverterFactory;
    }

    @Override
    public void reactOnNotification(Notification notification) {
        LOG.info(notification.getOriginalJson());
        IQualifiedCadfConverter cadfConverter = notificationConverterFactory.getConverter(notification);

        if (cadfConverter == null) {
            LOG.warn("Action with type {} is not supported", notification.getEventType());
            return;
        }

        CadfAuditEvent event = cadfConverter.convert(notification);
        List<AuditEventGroupType> outputEventGroups = cadfConverter.getOutputEventGroups();

        eventSender.sendCadfAuditEvent(event, outputEventGroups);
    }
}
