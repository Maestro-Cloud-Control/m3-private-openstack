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

package io.maestro3.agent.openstack.filter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class ApiFilter {

    private Map<String, Collection<String>> filter = new HashMap<>();

    protected void putFilter(String filterName, Collection<String> filterValues) {
        filter.put(filterName, filterValues);
    }

    public String apply(String path) {
        if (MapUtils.isNotEmpty(filter)) {
            path = path.concat("?").concat(toUriParams(filter));
        }
        return path;
    }

    private static String toUriParams(Map<String, Collection<String>> filter) {
        StringBuilder params = new StringBuilder();
        String separator = "";
        for (String key : filter.keySet()) {
            Collection<String> paramValues = filter.get(key);
            if (CollectionUtils.isNotEmpty(paramValues)) {
                for (String value : paramValues) {
                    params.append(separator).append(key).append("=").append(value);
                    separator = "&";
                }
            }
        }
        return params.toString();
    }
}
