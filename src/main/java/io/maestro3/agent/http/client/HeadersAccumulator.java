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

package io.maestro3.agent.http.client;

import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class HeadersAccumulator {

    private Set<String> headerNames;
    private Map<String, String> headers = new LinkedHashMap<>();

    public HeadersAccumulator(Set<String> headerNames) {
        Assert.notEmpty(headerNames, "headerNames cannot be null or empty.");
        this.headerNames = new LinkedHashSet<>(headerNames);
    }

    public void add(String key, String value) {
        headers.put(key, value);
    }

    public Map<String, String> retrieve() {
        return new LinkedHashMap<>(headers);
    }

    public Set<String> headers() {
        return new LinkedHashSet<>(headerNames);
    }
}
