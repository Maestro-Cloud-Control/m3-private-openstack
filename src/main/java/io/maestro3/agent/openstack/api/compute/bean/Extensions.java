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

package io.maestro3.agent.openstack.api.compute.bean;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


public class Extensions {

    @SerializedName("extensions")
    private List<ExtensionValue> extensionsList;

    public boolean contains(String extensionName) {
        if (CollectionUtils.isEmpty(extensionsList)) {
            return false;
        }

        for (ExtensionValue ext : extensionsList) {
            if (ext.getName().equalsIgnoreCase(extensionName)) {
                return true;
            }
        }
        return false;
    }

    public ExtensionValue get(String extensionName) {
        if (CollectionUtils.isEmpty(extensionsList)) {
            return null;
        }

        for (ExtensionValue ext : extensionsList) {
            if (ext.getName().equalsIgnoreCase(extensionName)) {
                return ext;
            }
        }
        return null;
    }
}
