package io.maestro3.agent.amqp.handler;

import io.maestro3.agent.model.notification.Notification;

public interface IOsNotificationHandler {

    void handle(Notification notification);
}
