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

package io.maestro3.agent.model.network.impl.ip;

import org.apache.commons.lang3.StringUtils;


public enum IPState {
    READY("ready"),
    ALLOCATING("allocating"),
    RELEASING("releasing"),
    ASSOCIATING("associating"),
    DISASSOCIATING("disassociating");

    private String state;

    IPState(String state) {
        this.state = state;
    }

    public static IPState getState(String state) {
        for (IPState serviceInstanceState : IPState.values()) {
            if (StringUtils.equalsIgnoreCase(serviceInstanceState.state, state)) {
                return serviceInstanceState;
            }
        }
        return null;
    }

    public String getState() {
        return state;
    }

    public boolean is(IPState ipState) {
        return this == ipState;
    }

    public boolean in(IPState... ipStates) {
        for (IPState state : ipStates) {
            if (this == state) {
                return true;
            }
        }
        return false;
    }

    public boolean nin(IPState... ipStates) {
        return !in(ipStates);
    }
}
