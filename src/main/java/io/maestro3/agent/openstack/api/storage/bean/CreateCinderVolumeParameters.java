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

package io.maestro3.agent.openstack.api.storage.bean;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class CreateCinderVolumeParameters {
    private String name;
    private Integer sizeGb;
    private String snapshotId;
    private String volumeId;
    private String availabilityZone;
    private Map<String, String> metadata;

    private CreateCinderVolumeParameters() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public Integer getSizeGb() {
        return sizeGb;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static class Builder {
        private String name;
        private Integer sizeGb;
        private String snapshotId;
        private String volumeId;
        private String availabilityZone;
        private Map<String, String> metadata = new HashMap<>();

        private Builder() {
        }

        public Builder name(String name) {
            Assert.hasText(name, "name cannot be null or empty.");
            this.name = name;
            return this;
        }

        public Builder sizeGb(int sizeGb) {
            this.sizeGb = sizeGb;
            return this;
        }

        public Builder snapshotId(String snapshotId) {
            this.snapshotId = snapshotId;
            return this;
        }

        public Builder volumeId(String volumeId) {
            this.volumeId = volumeId;
            return this;
        }

        public Builder availabilityZone(String availabilityZone) {
            this.availabilityZone = availabilityZone;
            return this;
        }

        public Builder metadata(String key, String value) {
            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                metadata.put(key, value);
            }
            return this;
        }

        public CreateCinderVolumeParameters build() {
            Assert.hasText(name, "name cannot be null or empty.");

            CreateCinderVolumeParameters parameters = new CreateCinderVolumeParameters();
            parameters.name = name;
            parameters.sizeGb = sizeGb;
            parameters.snapshotId = snapshotId;
            parameters.volumeId = volumeId;
            parameters.availabilityZone = availabilityZone;
            parameters.metadata = MapUtils.isNotEmpty(metadata) ? Collections.unmodifiableMap(metadata) : null;
            return parameters;
        }
    }
}
