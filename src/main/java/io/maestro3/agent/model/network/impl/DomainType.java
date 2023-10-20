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

package io.maestro3.agent.model.network.impl;

import com.google.common.collect.Lists;
import io.maestro3.sdk.internal.util.StringUtils;

import java.util.List;


public enum DomainType {

    VPC("vpc"),
    STANDARD("standard");

    private static List<DomainType> values = Lists.newArrayList(DomainType.values());

    private String label;

    DomainType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DomainType fromName(String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        for (DomainType domainType : values) {
            if (label.equalsIgnoreCase(domainType.label)) {
                return domainType;
            }
        }
        return null;
    }

}
