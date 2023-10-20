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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public enum ImageStateMapping {

    IN_PROGRESS("In progress", Arrays.asList("saving", "pending deleted", "queued")),
    AVAILABLE("Available", Collections.singletonList("active")),
    UNKNOWN("Unknown", Collections.EMPTY_LIST);

    private String name;
    private List<String> availableStates;

    private static final ImageStateMapping[] VALUES = values();

    ImageStateMapping(String name, List<String> availableStates) {
        this.name = name;
        this.availableStates = availableStates;
    }

    public static ImageStateMapping fromValue(String status) {
        if (status != null) {
            String lowerCaseStatus = status.toLowerCase(Locale.US);
            for (ImageStateMapping imageState : VALUES) {
                if (imageState.availableStates.contains(lowerCaseStatus)) {
                    return imageState;
                }
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }
}
