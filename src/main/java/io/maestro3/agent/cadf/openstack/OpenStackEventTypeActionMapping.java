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

import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum OpenStackEventTypeActionMapping {

    INSTANCE_CREATED(EventType.INSTANCE_CREATE_END, CadfActions.create(), SdkInstanceState.RUNNING, Collections.singletonList(ServerStateEnum.CREATING), "Instance created"),
    INSTANCE_STARTED(EventType.INSTANCE_POWERON_END, CadfActions.start(), SdkInstanceState.RUNNING, Arrays.asList(ServerStateEnum.STARTING, ServerStateEnum.STOPPED), "Instance started"),
    INSTANCE_REBOOTED(EventType.INSTANCE_REBOOT_END, CadfActions.start(), SdkInstanceState.RUNNING, Collections.singletonList(ServerStateEnum.REBOOTING), "Instance rebooted"),
    INSTANCE_STOPPED(EventType.INSTANCE_POWEROFF_END, CadfActions.stop(), SdkInstanceState.STOPPED, Arrays.asList(ServerStateEnum.STOPPING, ServerStateEnum.RUNNING), "Instance stopped"),
    INSTANCE_TERMINATED(EventType.INSTANCE_SHUTDOWN_END, CadfActions.delete(), SdkInstanceState.TERMINATED, Arrays.asList(ServerStateEnum.values()), "Instance terminated"),

    IMAGE_ACTIVATE(EventType.IMAGE_ACTIVATE, CadfActions.create(), SdkInstanceState.STOPPED, "Image activated"),
    IMAGE_DELETE(EventType.IMAGE_DELETE, CadfActions.delete(), SdkInstanceState.STOPPED, "Image deleted"),

    INSTANCE_FOUND(EventType.INSTANCE_EXISTS, CadfActions.discover(), null, "Instance found"),
    INSTANCE_CONFIGURATION_CHANGE(EventType.INSTANCE_UPDATE, CadfActions.configure(), null, "Instance configuration changed"),

    VOLUME_ATTACH_END(EventType.VOLUME_ATTACH_END, CadfActions.attach(), SdkInstanceState.RUNNING, "Volume attach finished");


    private EventType type;
    private ICadfAction cadfAction;
    private SdkInstanceState instanceState;
    private String description;
    private List<ServerStateEnum> serverStates;

    OpenStackEventTypeActionMapping(EventType type, ICadfAction cadfAction, SdkInstanceState instanceState,
                                    List<ServerStateEnum> serverStates, String description) {
        this.type = type;
        this.cadfAction = cadfAction;
        this.instanceState = instanceState;
        this.description = description;
        this.serverStates = serverStates;
    }

    OpenStackEventTypeActionMapping(EventType type, ICadfAction cadfAction, SdkInstanceState instanceState, String description) {
        this.type = type;
        this.cadfAction = cadfAction;
        this.instanceState = instanceState;
        this.description = description;
    }


    public static ICadfAction getActionByEventType(EventType type) {
        if (type == null) {
            return null;
        }
        for (OpenStackEventTypeActionMapping mapping : values()) {
            if (mapping.type.equals(type)) {
                return mapping.cadfAction;
            }
        }
        return null;
    }

    public static SdkInstanceState getStateByEventType(EventType type) {
        if (type == null) {
            return null;
        }
        for (OpenStackEventTypeActionMapping mapping : values()) {
            if (mapping.type.equals(type)) {
                return mapping.instanceState;
            }
        }
        return null;
    }

    public static ICadfAction getActionByState(SdkInstanceState state) {
        if (state == null) {
            return null;
        }
        for (OpenStackEventTypeActionMapping mapping : values()) {
            if (mapping.instanceState.equals(state)) {
                return mapping.cadfAction;
            }
        }
        return null;
    }

    public static ICadfAction getActionByPreviousState(ServerStateEnum state) {
        if (state == null) {
            return null;
        }
        for (OpenStackEventTypeActionMapping mapping : values()) {
            List<ServerStateEnum> serverStates = mapping.serverStates;
            if (CollectionUtils.isNotEmpty(serverStates)) {
                for (ServerStateEnum serverState : serverStates) {
                    if (serverState == state) {
                        return mapping.cadfAction;
                    }
                }
            }
        }
        return null;
    }

    public static String getDescriptionByCadfAction(ICadfAction cadfAction) {
        if (cadfAction == null) {
            return null;
        }
        for (OpenStackEventTypeActionMapping mapping : values()) {
            if (mapping.cadfAction.equals(cadfAction)) {
                return mapping.description;
            }
        }
        return null;
    }
}
