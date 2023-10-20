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

package io.maestro3.agent.model.network;

import io.maestro3.sdk.internal.util.StringUtils;


public enum SecurityConfigType {
    OPEN_STACK,
    OPEN_STACK_PROJECT_CUSTOM;

    public static SecurityConfigType fromString(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (SecurityConfigType iamUserType : values()) {
                if (iamUserType.name().equalsIgnoreCase(value)) {
                    return iamUserType;
                }
            }
        }
        return null;
    }
}

