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

package io.maestro3.agent.converter;

import io.maestro3.agent.model.compute.PowerState;
import io.maestro3.agent.model.compute.ServerStatus;
import io.maestro3.agent.model.compute.TaskState;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class OpenStackServerStateDetector {

    private static final Logger LOG = LoggerFactory.getLogger(OpenStackServerStateDetector.class);

    private static final String CAN_NOT_DETECT_STATE_THREE_PLACEHOLDERS_MESSAGE = "Can not detect state out of serverStatus={}, powerState={}, task={}";
    private static final String CAN_NOT_DETECT_STATE_TWO_PLACEHOLDERS_MESSAGE = "Can not detect state out of serverStatus={} and powerState={}";

    private OpenStackServerStateDetector() {
        throw new UnsupportedOperationException();
    }

    public static SdkInstanceState toM3SdkInstanceState(ServerStatus status, PowerState powerState, TaskState task) {
        if (task != null && task != TaskState.UNRECOGNIZED) {
            switch (task) {
                case RESUMING:
                case POWERING_ON:
                case SPAWNING:
                case NETWORKING:
                case SCHEDULING:
                    return SdkInstanceState.STARTING;
                case IMAGE_UPLOADING:
                case IMAGE_PENDING_UPLOAD:
                case IMAGE_SNAPSHOT:
                    return SdkInstanceState.UNKNOWN;
                case DELETING:
                    return SdkInstanceState.TERMINATED;
                case SUSPENDING:
                    return SdkInstanceState.STOPPING;
                case REBOOTING:
                case REBOOT_STARTED:
                case REBOOT_STARTED_HARD:
                case REBOOTING_HARD:
                    return SdkInstanceState.REBOOTING;
                case POWERING_OFF:
                    return SdkInstanceState.STOPPING;
                default:
                    LOG.warn(CAN_NOT_DETECT_STATE_THREE_PLACEHOLDERS_MESSAGE, status, powerState, task);
                    return SdkInstanceState.UNKNOWN;
            }
        }

        // no tasks are currently performed under server (instance)
        if (status != null) {
            switch (status) {
                case STOPPED:
                case SHUTOFF:
                    return SdkInstanceState.STOPPED;
                case VERIFY_RESIZE:
                case RESIZE:
                case MIGRATING:
                    if (powerState.is(PowerState.SHUT_DOWN)) return SdkInstanceState.STOPPED;
                    else if (powerState.is(PowerState.RUNNING, PowerState.NO_STATE)) return SdkInstanceState.RUNNING;
                    else return SdkInstanceState.UNKNOWN;
                case ACTIVE:
                    return powerState.is(PowerState.RUNNING, PowerState.NO_STATE) ? SdkInstanceState.RUNNING : SdkInstanceState.UNKNOWN;
                case BUILD:
                    return SdkInstanceState.STARTING;
                case SUSPENDED:
                    return powerState.is(PowerState.SHUT_DOWN) ? SdkInstanceState.STOPPED : SdkInstanceState.UNKNOWN;
                case REBOOT:
                    return SdkInstanceState.REBOOTING;
                // if OpenStack returns instance with DELETED status, then instance exists and we cannot say that it is terminated
                // we will announce that instance terminated when OpenStack returns nothing
                case DELETED:
                    return SdkInstanceState.TERMINATED;
                case ERROR:
                    return SdkInstanceState.ERROR;
                default:
                    LOG.warn(CAN_NOT_DETECT_STATE_TWO_PLACEHOLDERS_MESSAGE, status, powerState);
                    return SdkInstanceState.UNKNOWN;
            }
        }
        return SdkInstanceState.UNKNOWN;
    }

    public static ServerStateEnum toServerState(ServerStatus status, PowerState powerState, TaskState task) {
        if (task != null && task != TaskState.UNRECOGNIZED) {
            switch (task) {
                case RESUMING:
                case POWERING_ON:
                    return ServerStateEnum.STARTING;
                case SPAWNING:
                case NETWORKING:
                case SCHEDULING:
                    return ServerStateEnum.CREATING;
                case IMAGE_UPLOADING:
                case IMAGE_PENDING_UPLOAD:
                case IMAGE_SNAPSHOT:
                    return ServerStateEnum.STOPPED;
                case DELETING:
                    return ServerStateEnum.TERMINATING;
                case SUSPENDING:
                case POWERING_OFF:
                    return ServerStateEnum.STOPPING;
                case REBOOTING:
                case REBOOT_STARTED:
                case REBOOT_STARTED_HARD:
                case REBOOTING_HARD:
                    return ServerStateEnum.REBOOTING;
                default:
                    LOG.warn(CAN_NOT_DETECT_STATE_THREE_PLACEHOLDERS_MESSAGE, status, powerState, task);
                    return ServerStateEnum.UNKNOWN;
            }
        }
        // no tasks are currently performed under server (instance)
        if (status != null) {
            switch (status) {
                case STOPPED:
                case SHUTOFF:
                    return ServerStateEnum.STOPPED;
                case VERIFY_RESIZE:
                case RESIZE:
                case MIGRATING:
                    if (powerState.is(PowerState.SHUT_DOWN)) return ServerStateEnum.STOPPED;
                    else if (powerState.is(PowerState.RUNNING, PowerState.NO_STATE)) return ServerStateEnum.RUNNING;
                    else return ServerStateEnum.UNKNOWN;
                case ACTIVE:
                    return powerState.is(PowerState.RUNNING, PowerState.NO_STATE) ? ServerStateEnum.RUNNING : ServerStateEnum.UNKNOWN;
                case BUILD:
                    return ServerStateEnum.STARTING;
                case SUSPENDED:
                    return powerState.is(PowerState.SHUT_DOWN) ? ServerStateEnum.STOPPED : ServerStateEnum.UNKNOWN;
                case REBOOT:
                    return ServerStateEnum.REBOOTING;
                case DELETED:
                    return ServerStateEnum.TERMINATED;
                case ERROR:
                    return ServerStateEnum.ERROR;
                default:
                    LOG.warn(CAN_NOT_DETECT_STATE_TWO_PLACEHOLDERS_MESSAGE, status, powerState);
                    return ServerStateEnum.UNKNOWN;
            }
        }
        return ServerStateEnum.UNKNOWN;
    }
}
