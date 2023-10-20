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


public enum PowerState {
    NO_STATE("0"), RUNNING("1"), PAUSED("3"), SHUT_DOWN("4"), UNRECOGNIZED("");

    private String state;

    PowerState(String state) {
        this.state = state;
    }

    public static PowerState forValue(String value) {
        if (value != null) {
            for (PowerState s : PowerState.values()) {
                if (s.state.equalsIgnoreCase(value))
                    return s;
            }
        }
        return PowerState.UNRECOGNIZED;
    }

    public boolean is(PowerState... states) {
        if (states == null || states.length == 0) {
            return false;
        }
        for (PowerState state : states) {
            if (state != null && state == this) {
                return true;
            }
        }
        return false;
    }
}
