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

package io.maestro3.agent.model.glance;

import io.maestro3.agent.model.common.EventType;
import io.maestro3.agent.model.common.OpenStackEvent;
import io.maestro3.agent.model.compute.Image;
import org.springframework.util.Assert;


public class OpenStackImageEvent extends OpenStackEvent {

    private final String imageId;
    private final Image image;

    private OpenStackImageEvent(String imageId, Image image, EventType eventType) {
        super(eventType);
        this.imageId = imageId;
        this.image = image;
    }

    public static Builder build() {
        return new Builder();
    }

    public String getImageId() {
        return imageId;
    }

    public Image getImage() {
        return image;
    }

    public static class Builder {
        private String imageId;
        private Image image;
        private EventType eventType;

        public Builder withImageId(String imageId) {
            Assert.hasText(imageId, "imageId cannot be null or empty.");
            this.imageId = imageId;
            return this;
        }

        public Builder withImage(Image image) {
            this.image = image;
            return this;
        }

        public Builder withEventType(EventType eventType) {
            Assert.notNull(eventType, "eventType cannot be null.");
            this.eventType = eventType;
            return this;
        }

        public OpenStackImageEvent get() {
            Assert.hasText(imageId, "imageId cannot be null or empty.");
            Assert.notNull(eventType, "eventType cannot be null.");

            return new OpenStackImageEvent(imageId, image, eventType);
        }
    }
}
