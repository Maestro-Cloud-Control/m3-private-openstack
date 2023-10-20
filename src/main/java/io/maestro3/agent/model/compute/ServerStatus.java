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


public enum ServerStatus {
    /**
     * The server is active.
     */
    ACTIVE,
    /**
     * The server has not finished the original build process
     */
    BUILD,
    /**
     * The server is deleted.
     */
    DELETED,
    /**
     * The server is in error.
     */
    ERROR,
    /**
     * The server is hard rebooting.
     * This is equivalent to pulling the power plug on a physical server, plugging it back in, and rebooting it.
     */
    HARD_REBOOT,
    /**
     * The password is being reset on the server.
     */
    PASSWORD,
    /**
     * The server is in a soft reboot state. A reboot command was passed to the operating system.
     */
    REBOOT,
    /**
     * The server is currently being rebuilt from an image.
     */
    REBUILD,
    /**
     * The server is in rescue mode.
     */
    RESCUE,
    /**
     * Server is performing the differential copy of data that changed during its initial copy.
     * Server is down for this stage.
     */
    RESIZE,
    /**
     * The resize or migration of a server failed for some reason.
     * The destination server is being cleaned up and the original source server is restarting.
     */
    REVERT_RESIZE,
    /**
     * The virtual machine (VM) was powered down by the user, but not through the OpenStack Compute API.
     * For example, the user issued a shutdown -h command from within the server instance.
     * If the OpenStack Compute manager detects that the VM was powered down, it transitions the server instance to the SHUTOFF status.
     * If you use the OpenStack Compute API to restart the instance, the instance might be deleted first,
     * depending on the value in the shutdown_terminate database field on the Instance model.
     */
    SHUTOFF,
    /**
     * The server is suspended, either by request or necessity.
     */
    SUSPENDED,
    /**
     * The state of the server is unknown. Contact your cloud provider.
     */
    UNKNOWN,
    /**
     * The server is in paused mode.
     */
    PAUSED,
    /**
     * System is awaiting confirmation that the server is operational after a move or resize.
     */
    VERIFY_RESIZE,
    /**
     * The server is in stopped mode.
     */
    STOPPED,
    /**
     * The server is migrating. This is caused by a live migration (moving a server that is active) action.
     */
    MIGRATING,
    /**
     * Unrecognized state. Synthetic state that means we have got some unexpected server status from Nova.
     * Server should never have this kind of state.
     */
    UNRECOGNIZED;

    public static ServerStatus forValue(String value) {
        if (value != null) {
            for (ServerStatus s : ServerStatus.values()) {
                if (s.name().equalsIgnoreCase(value))
                    return s;
            }
        }
        return ServerStatus.UNRECOGNIZED;
    }

    public boolean is(ServerStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return false;
        }
        for (ServerStatus status : statuses) {
            if (status != null && status == this) {
                return true;
            }
        }
        return false;
    }
}
