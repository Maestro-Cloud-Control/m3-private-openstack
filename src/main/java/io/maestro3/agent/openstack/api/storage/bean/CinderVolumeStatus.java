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


public enum CinderVolumeStatus {

    CREATING("creating"),
    DOWNLOADING("downloading"),
    AVAILABLE("available"),
    ATTACHING("attaching"),
    IN_USE("in-use"),
    DELETING("deleting"),
    DETACHING("detaching"),
    ERROR("error"),
    ERROR_RESTORING("error_restoring"),
    ERROR_EXTENDING("error_extending");

    private static final CinderVolumeStatus[] STATUSES = values();

    private final String name;

    CinderVolumeStatus(String name) {
        this.name = name;
    }

    public static CinderVolumeStatus from(String statusString) {
        for (CinderVolumeStatus status : STATUSES) {
            if (status.name.equalsIgnoreCase(statusString)) {
                return status;
            }
        }
        return null;
    }

    public static boolean inError(CinderVolumeStatus status) {
        return status != null && (status == ERROR || status == ERROR_EXTENDING || status == ERROR_RESTORING);
    }

    public boolean ne(CinderVolumeStatus status) {
        return this != status;
    }

    public boolean is(CinderVolumeStatus status) {
        return this == status;
    }
}
