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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;


public class EnforceVersionToUrl implements Function<URL, URL> {

    private static final Pattern VERSION_REGEX = Pattern.compile("/v[0-9]+(\\.[0-9])*");

    private String version;
    private boolean onlyIfAbsent;

    private EnforceVersionToUrl(String version, boolean onlyIfAbsent) {
        this.version = version;
        this.onlyIfAbsent = onlyIfAbsent;
    }

    /**
     * @param version api version that will be forcibly added to the endpoint url depending on 'onlyIfAbsent' option
     *                (e.g. /v2.0)
     */
    public static EnforceVersionToUrl to(String version, boolean onlyIfAbsent) {
        return new EnforceVersionToUrl(version, onlyIfAbsent);
    }

    /**
     * @param version api version that will be forcibly added to the endpoint.
     *                (e.g. /v2.0)
     */
    public static EnforceVersionToUrl to(String version) {
        return new EnforceVersionToUrl(version, false);
    }

    @Override
    @SuppressWarnings("all")
    public URL apply(URL url) {
        URL result = url;
        if (url != null) {
            if (onlyIfAbsent && VERSION_REGEX.matcher(url.toString()).find()) {
                result = url;
            } else {
                try {
                    result = new URL(DeleteVersionFromUrl.INSTANCE.apply(url.toString()).concat(version));
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Failed to enforce URL " + url, e);
                }
            }
        }
        return result;
    }
}


