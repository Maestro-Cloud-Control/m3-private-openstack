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

package io.maestro3.agent.cadf.openstack;

import io.maestro3.agent.cadf.openstack.converter.IQualifiedCadfConverter;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationConverterFactory implements INotificationConverterFactory {

    private Map<EventType, IQualifiedCadfConverter> mappedConverters;

    @Autowired
    public NotificationConverterFactory(List<IQualifiedCadfConverter> converters) {
        mappedConverters = new HashMap<>();
        if (converters != null) {
            for (IQualifiedCadfConverter converter : converters) {
                for (EventType supportedEventType : converter.getSupportedEventTypes()) {
                    if (mappedConverters.get(supportedEventType) != null) {
                        throw new IllegalArgumentException("The only one converter allowed for metric: " + supportedEventType);
                    } else {
                        mappedConverters.put(supportedEventType, converter);
                    }
                }
            }
        }
    }

    @Override
    public IQualifiedCadfConverter getConverter(Notification notification) {
        return mappedConverters.get(notification.getEventType());
    }
}
