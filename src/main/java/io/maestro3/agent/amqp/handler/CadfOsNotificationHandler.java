package io.maestro3.agent.amqp.handler;

import io.maestro3.agent.cadf.CadfAuditEventSender;
import io.maestro3.agent.cadf.openstack.INotificationConverterFactory;
import io.maestro3.agent.cadf.openstack.converter.IQualifiedCadfConverter;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CadfOsNotificationHandler implements IOsNotificationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CadfOsNotificationHandler.class);

    private final CadfAuditEventSender eventSender;
    private final INotificationConverterFactory notificationConverterFactory;

    @Autowired
    public CadfOsNotificationHandler(CadfAuditEventSender eventSender,
                                     INotificationConverterFactory notificationConverterFactory) {
        this.eventSender = eventSender;
        this.notificationConverterFactory = notificationConverterFactory;
    }

    @Override
    public void handle(Notification notification) {
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
