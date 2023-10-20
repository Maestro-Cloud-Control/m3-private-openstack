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

package io.maestro3.agent.model.storage;

import io.maestro3.agent.model.common.EventType;
import io.maestro3.agent.model.common.OpenStackEvent;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import org.springframework.util.Assert;


public class OpenStackVolumeEvent extends OpenStackEvent {

    private final String volumeId;
    private final CinderVolume cinderVolume;

    private OpenStackVolumeEvent(String volumeId, CinderVolume cinderVolume, EventType eventType) {
        super(eventType);
        this.volumeId = volumeId;
        this.cinderVolume = cinderVolume;
    }

    public static Builder build() {
        return new Builder();
    }

    public String getVolumeId() {
        return volumeId;
    }

    public CinderVolume getCinderVolume() {
        return cinderVolume;
    }

    public static class Builder {
        private String volumeId;
        private CinderVolume cinderVolume;
        private EventType eventType;

        private Builder() {
        }

        public Builder withVolumeId(String volumeId) {
            Assert.hasText(volumeId, "volumeId cannot be null or empty.");
            this.volumeId = volumeId;
            return this;
        }

        public Builder withCinderVolume(CinderVolume cinderVolume) {
            this.cinderVolume = cinderVolume;
            return this;
        }

        public Builder withEventType(EventType eventType) {
            Assert.notNull(eventType, "eventType cannot be null.");
            this.eventType = eventType;
            return this;
        }

        public OpenStackVolumeEvent get() {
            Assert.hasText(volumeId, "volumeId cannot be null or empty.");
            Assert.notNull(eventType, "eventType cannot be null.");

            return new OpenStackVolumeEvent(volumeId, cinderVolume, eventType);
        }
    }
}
