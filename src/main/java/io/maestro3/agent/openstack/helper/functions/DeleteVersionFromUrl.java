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

package io.maestro3.agent.openstack.helper.functions;

import com.google.common.base.Function;


class DeleteVersionFromUrl implements Function<String, String> {

    public static final DeleteVersionFromUrl INSTANCE = new DeleteVersionFromUrl();

    private static final String VERSION_REGEX = "/v[0-9]+(\\.[0-9])*";

    @Override
    public String apply(String url) {
        String result = url.replaceFirst(VERSION_REGEX, "");
        if (result.endsWith("/"))
            return result.substring(0, result.length() - 1);
        return result;
    }
}

