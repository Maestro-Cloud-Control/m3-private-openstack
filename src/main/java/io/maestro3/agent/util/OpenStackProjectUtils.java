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

package io.maestro3.agent.util;

import io.maestro3.agent.model.region.OpenStackRegionConfig;

public class OpenStackProjectUtils {

    private static final String NAME_SEPARATOR = "::";

    public static String buildProjectName(String nodeName, OpenStackRegionConfig zone, String project, boolean useSimpleNames) {
        StringBuilder name = new StringBuilder();
        if (useSimpleNames) {
            name.append(project);
        } else {
            name.append(nodeName)
                .append(NAME_SEPARATOR)
                .append(zone.getRegionAlias())
                .append(NAME_SEPARATOR)
                .append(project);
        }
        return name.toString().toLowerCase();
    }

    public static String buildUserName(String nodeName, OpenStackRegionConfig zone, String project, boolean useSimpleNames) {
        StringBuilder name = new StringBuilder();
        if (useSimpleNames) {
            name.append(project);
        } else {
            name.append(project)
                .append(NAME_SEPARATOR)
                .append(nodeName)
                .append(NAME_SEPARATOR)
                .append(zone.getRegionAlias())
                .append(NAME_SEPARATOR)
                .append(project);
        }
        return name.toString().toLowerCase();
    }
}
