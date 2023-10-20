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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum TaskState {

    RESUMING("resuming"),
    DELETING("deleting"),
    POWERING_ON("powering-on"),
    POWERING_OFF("powering-off"),
    REBOOTING("rebooting"),
    REBOOTING_HARD("rebooting_hard"),
    REBOOT_STARTED("reboot_started"),
    REBOOT_STARTED_HARD("reboot_started_hard"),
    SUSPENDING("suspending"),
    SPAWNING("spawning"),
    NETWORKING("networking"),
    SCHEDULING("scheduling"),
    IMAGE_SNAPSHOT("image_snapshot"),
    IMAGE_UPLOADING("image_uploading"),
    IMAGE_PENDING_UPLOAD("image_pending_upload"),
    RESIZE_MIGRATING("resize_migrating"),
    RESIZE_MIGRATED("resize_migrated"),
    RESIZE_FINISH("resize_finish"),
    UNRECOGNIZED("");

    private static final Logger LOG = LoggerFactory.getLogger(TaskState.class);
    private String state;

    TaskState(String state) {
        this.state = state;
    }

    public static TaskState forValue(String value) {
        if (value == null) {
            return null;
        }

        for (TaskState state : TaskState.values()) {
            if (state.state.equalsIgnoreCase(value))
                return state;
        }
        LOG.warn("Cannot recognize state -> {}", value);
        return UNRECOGNIZED;
    }
}
