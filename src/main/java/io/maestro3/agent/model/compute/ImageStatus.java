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

import com.google.gson.annotations.SerializedName;


public enum ImageStatus {

    /**
     * The image identifier has been reserved for an image in the Glance registry.
     * No image data has been uploaded to Glance and the image size was not explicitly set to zero on creation.
     */
    @SerializedName("queued")
    QUEUED("queued"),

    /**
     * Denotes that an image’s raw data is currently being uploaded to Glance.
     */
    @SerializedName("saving")
    SAVING("saving"),

    /**
     * Denotes an image that is fully available in Glance.
     */
    @SerializedName("active")
    ACTIVE("active"),

    /**
     * Denotes that an error occurred during the uploading of an image’s data, and that the image is not readable.
     */
    @SerializedName("killed")
    KILLED("killed"),

    /**
     * Glance has retained the information about the image, but it is no longer available to use.
     * An image in this state will be removed automatically at a later date.
     */
    @SerializedName("deleted")
    DELETED("deleted"),

    /**
     * This is similar to deleted, however, Glance has not yet removed the image data.
     * An image in this state is recoverable.
     */
    @SerializedName("pending_deleted")
    PENDING_DELETED("pending deleted"),

    /**
     * Unknown state. Synthetic state that means we have got some unexpected image status from Glance.
     * Image should never have this kind of state.
     */
    UNKNOWN("unknown");

    private String name;

    ImageStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
