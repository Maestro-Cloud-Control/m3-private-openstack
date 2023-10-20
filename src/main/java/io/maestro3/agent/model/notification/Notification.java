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

package io.maestro3.agent.model.notification;

import com.google.gson.annotations.SerializedName;

import java.util.Map;


public class Notification {

    @SerializedName("event_type")
    private EventType eventType;

    private String timestamp;

    @SerializedName("_unique_id")
    private String uniqueId;

    private Map<String, Object> payload;

    private String originalJson;

    private Map<String, Object> originalNotification; // original json converted to map, so it contains all the fields

    public EventType getEventType() {
        return eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getOriginalJson() {
        return originalJson;
    }

    public Map<String, Object> getOriginalNotification() {
        return originalNotification;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setOriginalJson(String originalJson) {
        this.originalJson = originalJson;
    }

    public void setOriginalNotification(Map<String, Object> originalNotification) {
        this.originalNotification = originalNotification;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Notification{");
        sb.append("eventType=").append(eventType);
        sb.append(", timestamp='").append(timestamp).append('\'');
        sb.append(", payload=").append(payload);
        sb.append(", originalJson='").append(originalJson).append('\'');
        sb.append(", originalNotification=").append(originalNotification);
        sb.append('}');
        return sb.toString();
    }
}
