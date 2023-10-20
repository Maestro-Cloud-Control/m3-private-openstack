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

import com.google.gson.annotations.SerializedName;


public class VolumeActions {

    public static VolumeAction extendVolume(Integer newSize) {
        return new ExtendVolumeAction(newSize);
    }

    private static class ExtendVolumeAction implements VolumeAction {

        @SerializedName("os-extend")
        Extend extend;

        public ExtendVolumeAction(Integer newSize) {
            extend = new Extend();
            extend.newSize = newSize;
        }

        static class Extend {
            @SerializedName("new_size")
            Integer newSize;
        }
    }
}
