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

package io.maestro3.agent.model.network.impl.dns;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


public enum DnsRecordType {
    MX,
    A,
    NS,
    TXT,
    SOA,
    AAAA,
    PTR;

    public static List<DnsRecordType> SUPPORTED_TYPES = Lists.newArrayList(A, PTR);

    public static DnsRecordType forName(String typeName) {
        return SUPPORTED_TYPES.stream()
            .filter(recordType -> StringUtils.equalsIgnoreCase(recordType.name(), typeName))
            .findAny().orElse(null);
    }
}
