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

public enum NetworkType {

    DEFAULT("Default"),
    HYBRID("Hybrid"),
    SECURED("Secured");

    public static final NetworkType[] VALUES = values();
    private final String title;

    NetworkType(String title) {
        this.title = title;
    }

    public static NetworkType fromName(String name) {
        for (NetworkType networkType : VALUES) {
            if (networkType.name().equalsIgnoreCase(name)) {
                return networkType;
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }
}
