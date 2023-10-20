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

package io.maestro3.agent.cadf.openstack.converter;

import io.maestro3.agent.cadf.openstack.OpenStackEventTypeActionMapping;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventCadfConverter implements IQualifiedCadfConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventCadfConverter.class);

    protected static final String NOTIFICATION_TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS";

    @Override
    public CadfAuditEvent convert(Notification notification) {
        ICadfAction action = OpenStackEventTypeActionMapping.getActionByEventType(notification.getEventType());

        if (action == null) {
            LOG.warn("Action with type {} is not supported", notification.getEventType());
            return null;
        }

        try {
            return doConvert(action, notification);
        } catch (Exception ex) {
            LOG.error("Failed to convert notification", ex);
            return null;
        }
    }

    @Override
    public Notification unconvert(CadfAuditEvent cadfAuditEvent) {
        throw new UnsupportedOperationException("Unconvertion is not supported");
    }

    protected abstract CadfAuditEvent doConvert(ICadfAction action, Notification notification);
}
