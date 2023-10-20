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

package io.maestro3.agent.model.compute;

import io.maestro3.agent.model.common.EventType;
import io.maestro3.agent.model.common.OpenStackEvent;
import org.springframework.util.Assert;


public class OpenStackInstanceEvent extends OpenStackEvent {

    private final String serverId;
    private final Server server;

    private OpenStackInstanceEvent(String serverId, Server server, EventType eventType) {
        super(eventType);
        this.serverId = serverId;
        this.server = server;
    }

    public static Builder build() {
        return new Builder();
    }

    public String getServerId() {
        return serverId;
    }

    public Server getServer() {
        return server;
    }

    public static class Builder {
        private String serverId;
        private Server server;
        private EventType eventType;

        private Builder() {
        }

        public Builder withServerId(String serverId) {
            Assert.hasText(serverId, "serverId cannot be null or empty.");
            this.serverId = serverId;
            return this;
        }

        public Builder withServer(Server server) {
            this.server = server;
            return this;
        }

        public Builder withEventType(EventType eventType) {
            Assert.notNull(eventType, "eventType cannot be null.");
            this.eventType = eventType;
            return this;
        }

        public OpenStackInstanceEvent get() {
            Assert.hasText(serverId, "serverId cannot be null or empty.");
            Assert.notNull(eventType, "eventType cannot be null.");

            return new OpenStackInstanceEvent(serverId, server, eventType);
        }
    }
}
