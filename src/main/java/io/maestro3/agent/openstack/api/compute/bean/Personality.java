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

package io.maestro3.agent.openstack.api.compute.bean;


public class Personality {

    private String path;

    private String contents;

    protected Personality() {
    }

    /**
     * Creates new personalty item.
     *
     * @param path     file path
     * @param contents content of the file
     */
    public Personality(String path, String contents) {
        this.path = path;
        this.contents = contents;
    }

    /**
     * Gets file path. The maximum size of the file path data is 255 bytes
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets file content.
     * The maximum limit refers to the number of bytes in the decoded data and not the number of characters in the encoded data.
     *
     * @return content of a file
     */
    public String getContents() {
        return contents;
    }
}
