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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public enum SecurityGroupType {

    /**
     * Ingress: ONLY project sources
     * Egress: ONLY project sources, OpenStack metadata
     */
    PRIVATE(false),

    /**
     * Ingress: ONLY project sources, Orchestrator node IPs
     * Egress: project sources, OpenStack metadata
     */
    PROTECTED(false),

    /**
     * Recommended
     * <p>
     * Ingress: project sources, SSH, RDP, HTTPS, Orchestrator node IPs
     * Egress: 0.0.0.0/0
     */
    LIMITED(false),

    /**
     * Ingress: 0.0.0.0/0
     * Egress: 0.0.0.0/0
     */
    PUBLIC(false),

    /**
     * Ingress: ONLY project sources
     * Egress: ONLY project sources
     */
    MANUAL(true),

    /**
     * Special type. This type can only be used with type MANUAL.
     */
    CORE_V(true);

    private boolean isMultipleModes;

    SecurityGroupType(boolean isMultipleModes) {
        this.isMultipleModes = isMultipleModes;
    }

    public boolean isMultipleModes() {
        return isMultipleModes;
    }

    public static SecurityGroupType fromName(String name) {
        return Stream.of(values())
            .filter(securityGroupType -> securityGroupType.name().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public Collection<Direction> getDirections() {
        switch (this) {
            case PUBLIC:
            case PRIVATE:
                return Collections.emptyList();
            case MANUAL:
            case CORE_V:
                return Collections.singletonList(Direction.EGRESS);
            default:
                return Arrays.asList(Direction.values());
        }
    }
}
