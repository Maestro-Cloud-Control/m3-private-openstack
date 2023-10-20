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
import org.apache.commons.lang3.StringUtils;


public enum EventType { // this info came from a document, not from usage experience, so there may be other notifications

    // ----------- INSTANCE EVENTS --------------

    @SerializedName(Constants.COMPUTE_INSTANCE_CREATE_START)
    INSTANCE_CREATE_START(Constants.COMPUTE_INSTANCE_CREATE_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_CREATE_ERROR)
    INSTANCE_CREATE_ERROR(Constants.COMPUTE_INSTANCE_CREATE_ERROR, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_CREATE_END)
    INSTANCE_CREATE_END(Constants.COMPUTE_INSTANCE_CREATE_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_DELETE_START)
    INSTANCE_DELETE_START(Constants.COMPUTE_INSTANCE_DELETE_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_DELETE_END)
    INSTANCE_DELETE_END(Constants.COMPUTE_INSTANCE_DELETE_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_EXISTS)
    INSTANCE_EXISTS(Constants.COMPUTE_INSTANCE_EXISTS, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_POWER_OFF_START)
    INSTANCE_POWEROFF_START(Constants.COMPUTE_INSTANCE_POWER_OFF_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_POWER_OFF_END)
    INSTANCE_POWEROFF_END(Constants.COMPUTE_INSTANCE_POWER_OFF_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_POWER_ON_START)
    INSTANCE_POWERON_START(Constants.COMPUTE_INSTANCE_POWER_ON_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_POWER_ON_END)
    INSTANCE_POWERON_END(Constants.COMPUTE_INSTANCE_POWER_ON_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_REBUILD_START)
    INSTANCE_REBUILD_START(Constants.COMPUTE_INSTANCE_REBUILD_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_REBUILD_END)
    INSTANCE_REBUILD_END(Constants.COMPUTE_INSTANCE_REBUILD_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_REBOOT_START)
    INSTANCE_REBOOT_START(Constants.COMPUTE_INSTANCE_REBOOT_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_REBOOT_END)
    INSTANCE_REBOOT_END(Constants.COMPUTE_INSTANCE_REBOOT_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_SUSPEND_START)
    INSTANCE_SUSPEND_START(Constants.COMPUTE_INSTANCE_SUSPEND_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_SUSPEND_END)
    INSTANCE_SUSPEND_END(Constants.COMPUTE_INSTANCE_SUSPEND_END, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESUME_START)
    INSTANCE_RESUME_START(Constants.COMPUTE_INSTANCE_RESUME_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESUME_END)
    INSTANCE_RESUME_END(Constants.COMPUTE_INSTANCE_RESUME_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_START)
    INSTANCE_RESIZE_START(Constants.COMPUTE_INSTANCE_RESIZE_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_END)
    INSTANCE_RESIZE_END(Constants.COMPUTE_INSTANCE_RESIZE_END, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_PREP_START)
    INSTANCE_RESIZE_PREP_START(Constants.COMPUTE_INSTANCE_RESIZE_PREP_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_PREP_END)
    INSTANCE_RESIZE_PREP_END(Constants.COMPUTE_INSTANCE_RESIZE_PREP_END, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_CONFIRM_START)
    INSTANCE_RESIZE_CONFIRM_START(Constants.COMPUTE_INSTANCE_RESIZE_CONFIRM_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_CONFIRM_END)
    INSTANCE_RESIZE_CONFIRM_END(Constants.COMPUTE_INSTANCE_RESIZE_CONFIRM_END, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_REVERT_START)
    INSTANCE_RESIZE_REVERT_START(Constants.COMPUTE_INSTANCE_RESIZE_REVERT_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_RESIZE_REVERT_END)
    INSTANCE_RESIZE_REVERT_END(Constants.COMPUTE_INSTANCE_RESIZE_REVERT_END, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_FINISH_RESIZE_START)
    INSTANCE_RESIZE_FINISH_START(Constants.COMPUTE_INSTANCE_FINISH_RESIZE_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_FINISH_RESIZE_END)
    INSTANCE_RESIZE_FINISH_END(Constants.COMPUTE_INSTANCE_FINISH_RESIZE_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_SHUTDOWN_START)
    INSTANCE_SHUTDOWN_START(Constants.COMPUTE_INSTANCE_SHUTDOWN_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_SHUTDOWN_END)
    INSTANCE_SHUTDOWN_END(Constants.COMPUTE_INSTANCE_SHUTDOWN_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_SNAPSHOT_START)
    INSTANCE_SNAPSHOT_START(Constants.COMPUTE_INSTANCE_SNAPSHOT_START, Resource.INSTANCE),
    @SerializedName(Constants.COMPUTE_INSTANCE_SNAPSHOT_END)
    INSTANCE_SNAPSHOT_END(Constants.COMPUTE_INSTANCE_SNAPSHOT_END, Resource.INSTANCE),

    @SerializedName(Constants.COMPUTE_INSTANCE_UPDATE)
    INSTANCE_UPDATE(Constants.COMPUTE_INSTANCE_UPDATE, Resource.INSTANCE),

    // ------------- VOLUME EVENTS ----------------

    @SerializedName(Constants.VOLUME_DELETE_START)
    VOLUME_DELETE_START(Constants.VOLUME_DELETE_START, Resource.VOLUME),
    @SerializedName(Constants.VOLUME_DELETE_END)
    VOLUME_DELETE_END(Constants.VOLUME_DELETE_END, Resource.VOLUME),

    @SerializedName(Constants.VOLUME_CREATE_START)
    VOLUME_CREATE_START(Constants.VOLUME_CREATE_START, Resource.VOLUME),
    @SerializedName(Constants.VOLUME_CREATE_END)
    VOLUME_CREATE_END(Constants.VOLUME_CREATE_END, Resource.VOLUME),

    @SerializedName(Constants.VOLUME_ATTACH_END)
    VOLUME_ATTACH_END(Constants.VOLUME_ATTACH_END, Resource.VOLUME),

    @SerializedName(Constants.VOLUME_DETACH_END)
    VOLUME_DETACH_END(Constants.VOLUME_DETACH_END, Resource.VOLUME),

    @SerializedName(Constants.VOLUME_USAGE)
    VOLUME_USAGE(Constants.VOLUME_USAGE, Resource.VOLUME),

    // --------------------FLOATING IPS--------------------------

    @SerializedName(Constants.FLOATING_IP_CREATE_START)
    FLOATING_IP_CREATE_START(Constants.FLOATING_IP_CREATE_START, Resource.FLOATING_IP),
    @SerializedName(Constants.FLOATING_IP_CREATE_END)
    FLOATING_IP_CREATE_END(Constants.FLOATING_IP_CREATE_END, Resource.FLOATING_IP),

    // -------------- IMAGE EVENTS ------------------

    @SerializedName(Constants.IMAGE_ACTIVATE)
    IMAGE_ACTIVATE(Constants.IMAGE_ACTIVATE, Resource.MACHINE_IMAGE),
    @SerializedName(Constants.IMAGE_DELETE)
    IMAGE_DELETE(Constants.IMAGE_DELETE, Resource.MACHINE_IMAGE),
    @SerializedName(Constants.IMAGE_CREATE)
    IMAGE_CREATE(Constants.IMAGE_CREATE, Resource.MACHINE_IMAGE),
    @SerializedName(Constants.IMAGE_PREPARE)
    IMAGE_PREPARE(Constants.IMAGE_PREPARE, Resource.MACHINE_IMAGE),
    @SerializedName(Constants.IMAGE_UPDATE)
    IMAGE_UPDATE(Constants.IMAGE_UPDATE, Resource.MACHINE_IMAGE),
    @SerializedName(Constants.IMAGE_UPLOAD)
    IMAGE_UPLOAD(Constants.IMAGE_UPLOAD, Resource.MACHINE_IMAGE),

    // -------------- STACK EVENTS ------------------

    @SerializedName(Constants.ORCHESTRATION_STACK_CREATE_START)
    STACK_CREATE_START(Constants.ORCHESTRATION_STACK_CREATE_START, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_CREATE_ERROR)
    STACK_CREATE_ERROR(Constants.ORCHESTRATION_STACK_CREATE_ERROR, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_CREATE_END)
    STACK_CREATE_END(Constants.ORCHESTRATION_STACK_CREATE_END, Resource.STACK),

    @SerializedName(Constants.ORCHESTRATION_STACK_DELETE_START)
    STACK_DELETE_START(Constants.ORCHESTRATION_STACK_DELETE_START, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_DELETE_ERROR)
    STACK_DELETE_ERROR(Constants.ORCHESTRATION_STACK_DELETE_ERROR, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_DELETE_END)
    STACK_DELETE_END(Constants.ORCHESTRATION_STACK_DELETE_END, Resource.STACK),

    @SerializedName(Constants.ORCHESTRATION_STACK_RESUME_START)
    STACK_RESUME_START(Constants.ORCHESTRATION_STACK_RESUME_START, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_RESUME_ERROR)
    STACK_RESUME_ERROR(Constants.ORCHESTRATION_STACK_RESUME_ERROR, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_RESUME_END)
    STACK_RESUME_END(Constants.ORCHESTRATION_STACK_RESUME_END, Resource.STACK),

    @SerializedName(Constants.ORCHESTRATION_STACK_SUSPEND_START)
    STACK_SUSPEND_START(Constants.ORCHESTRATION_STACK_SUSPEND_START, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_SUSPEND_ERROR)
    STACK_SUSPEND_ERROR(Constants.ORCHESTRATION_STACK_SUSPEND_ERROR, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_SUSPEND_END)
    STACK_SUSPEND_END(Constants.ORCHESTRATION_STACK_SUSPEND_END, Resource.STACK),

    @SerializedName(Constants.ORCHESTRATION_STACK_UPDATE_START)
    STACK_UPDATE_START(Constants.ORCHESTRATION_STACK_UPDATE_START, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_UPDATE_ERROR)
    STACK_UPDATE_ERROR(Constants.ORCHESTRATION_STACK_UPDATE_ERROR, Resource.STACK),
    @SerializedName(Constants.ORCHESTRATION_STACK_UPDATE_END)
    STACK_UPDATE_END(Constants.ORCHESTRATION_STACK_UPDATE_END, Resource.STACK),

    // ---------------- AUTOSCALING EVENTS ---------------

    @SerializedName(Constants.ORCHESTRATION_AUTOSCALING_START)
    AUTOSCALING_START(Constants.ORCHESTRATION_AUTOSCALING_START, Resource.AUTO_SCALING),
    @SerializedName(Constants.ORCHESTRATION_AUTOSCALING_ERROR)
    AUTOSCALING_ERROR(Constants.ORCHESTRATION_AUTOSCALING_ERROR, Resource.AUTO_SCALING),
    @SerializedName(Constants.ORCHESTRATION_AUTOSCALING_END)
    AUTOSCALING_END(Constants.ORCHESTRATION_AUTOSCALING_END, Resource.AUTO_SCALING);

    private static final EventType[] VALUES = values();

    private final String name;
    private final Resource resource;

    EventType(String name, Resource resource) {
        this.name = name;
        this.resource = resource;
    }

    public static EventType getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (EventType type : VALUES) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public boolean in(EventType... eventTypes) {
        if (eventTypes == null) {
            return false;
        }

        for (EventType eventType : eventTypes) {
            if (this == eventType) {
                return true;
            }
        }
        return false;
    }

    public Resource getResource() {
        return resource;
    }

    public String getName() {
        return name;
    }

    private static class Constants {
        private static final String COMPUTE_INSTANCE_CREATE_START = "compute.instance.create.start";
        private static final String COMPUTE_INSTANCE_CREATE_ERROR = "compute.instance.create.error";
        private static final String COMPUTE_INSTANCE_CREATE_END = "compute.instance.create.end";
        private static final String COMPUTE_INSTANCE_DELETE_START = "compute.instance.delete.start";
        private static final String COMPUTE_INSTANCE_DELETE_END = "compute.instance.delete.end";
        private static final String COMPUTE_INSTANCE_EXISTS = "compute.instance.exists";
        private static final String COMPUTE_INSTANCE_POWER_OFF_START = "compute.instance.power_off.start";
        private static final String COMPUTE_INSTANCE_POWER_OFF_END = "compute.instance.power_off.end";
        private static final String COMPUTE_INSTANCE_POWER_ON_START = "compute.instance.power_on.start";
        private static final String COMPUTE_INSTANCE_POWER_ON_END = "compute.instance.power_on.end";
        private static final String COMPUTE_INSTANCE_REBUILD_START = "compute.instance.rebuild.start";
        private static final String COMPUTE_INSTANCE_REBUILD_END = "compute.instance.rebuild.end";
        private static final String COMPUTE_INSTANCE_REBOOT_START = "compute.instance.reboot.start";
        private static final String COMPUTE_INSTANCE_REBOOT_END = "compute.instance.reboot.end";
        private static final String COMPUTE_INSTANCE_SUSPEND_START = "compute.instance.suspend.start";
        private static final String COMPUTE_INSTANCE_SUSPEND_END = "compute.instance.suspend.end";
        private static final String COMPUTE_INSTANCE_RESUME_START = "compute.instance.resume.start";
        private static final String COMPUTE_INSTANCE_RESUME_END = "compute.instance.resume.end";
        private static final String COMPUTE_INSTANCE_RESIZE_START = "compute.instance.resize.start";
        private static final String COMPUTE_INSTANCE_RESIZE_END = "compute.instance.resize.end";
        private static final String COMPUTE_INSTANCE_RESIZE_PREP_START = "compute.instance.resize.prep.start";
        private static final String COMPUTE_INSTANCE_RESIZE_PREP_END = "compute.instance.resize.prep.end";
        private static final String COMPUTE_INSTANCE_RESIZE_CONFIRM_START = "compute.instance.resize.confirm.start";
        private static final String COMPUTE_INSTANCE_RESIZE_CONFIRM_END = "compute.instance.resize.confirm.end";
        private static final String COMPUTE_INSTANCE_RESIZE_REVERT_START = "compute.instance.resize.revert.start";
        private static final String COMPUTE_INSTANCE_RESIZE_REVERT_END = "compute.instance.resize.revert.end";
        private static final String COMPUTE_INSTANCE_FINISH_RESIZE_START = "compute.instance.finish_resize.start";
        private static final String COMPUTE_INSTANCE_FINISH_RESIZE_END = "compute.instance.finish_resize.end";
        private static final String COMPUTE_INSTANCE_SHUTDOWN_START = "compute.instance.shutdown.start";
        private static final String COMPUTE_INSTANCE_SHUTDOWN_END = "compute.instance.shutdown.end";
        private static final String COMPUTE_INSTANCE_SNAPSHOT_START = "compute.instance.snapshot.start";
        private static final String COMPUTE_INSTANCE_SNAPSHOT_END = "compute.instance.snapshot.end";
        private static final String COMPUTE_INSTANCE_UPDATE = "compute.instance.update";
        private static final String VOLUME_DELETE_START = "volume.delete.start";
        private static final String VOLUME_DELETE_END = "volume.delete.end";
        private static final String VOLUME_CREATE_START = "volume.create.start";
        private static final String VOLUME_CREATE_END = "volume.create.end";
        private static final String VOLUME_ATTACH_END = "volume.attach.end";
        private static final String VOLUME_DETACH_END = "volume.detach.end";
        private static final String VOLUME_USAGE = "volume.usage";
        private static final String IMAGE_ACTIVATE = "image.activate";
        private static final String IMAGE_DELETE = "image.delete";
        private static final String ORCHESTRATION_STACK_CREATE_START = "orchestration.stack.create.start";
        private static final String ORCHESTRATION_STACK_CREATE_ERROR = "orchestration.stack.create.error";
        private static final String ORCHESTRATION_STACK_CREATE_END = "orchestration.stack.create.end";
        private static final String ORCHESTRATION_STACK_DELETE_START = "orchestration.stack.delete.start";
        private static final String ORCHESTRATION_STACK_DELETE_ERROR = "orchestration.stack.delete.error";
        private static final String ORCHESTRATION_STACK_DELETE_END = "orchestration.stack.delete.end";
        private static final String ORCHESTRATION_STACK_RESUME_START = "orchestration.stack.resume.start";
        private static final String ORCHESTRATION_STACK_RESUME_ERROR = "orchestration.stack.resume.error";
        private static final String ORCHESTRATION_STACK_RESUME_END = "orchestration.stack.resume.end";
        private static final String ORCHESTRATION_STACK_SUSPEND_START = "orchestration.stack.suspend.start";
        private static final String ORCHESTRATION_STACK_SUSPEND_ERROR = "orchestration.stack.suspend.error";
        private static final String ORCHESTRATION_STACK_SUSPEND_END = "orchestration.stack.suspend.end";
        private static final String ORCHESTRATION_STACK_UPDATE_START = "orchestration.stack.update.start";
        private static final String ORCHESTRATION_STACK_UPDATE_ERROR = "orchestration.stack.update.error";
        private static final String ORCHESTRATION_STACK_UPDATE_END = "orchestration.stack.update.end";
        private static final String ORCHESTRATION_AUTOSCALING_START = "orchestration.autoscaling.start";
        private static final String ORCHESTRATION_AUTOSCALING_ERROR = "orchestration.autoscaling.error";
        private static final String ORCHESTRATION_AUTOSCALING_END = "orchestration.autoscaling.end";

        private static final String FLOATING_IP_CREATE_START = "floatingip.create.start";
        private static final String FLOATING_IP_CREATE_END = "floatingip.create.end";

        private static final String IMAGE_CREATE = "image.create";
        private static final String IMAGE_UPDATE = "image.update";
        private static final String IMAGE_PREPARE = "image.prepare";
        private static final String IMAGE_UPLOAD = "image.upload";

    }
}
