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

package io.maestro3.agent.model.identity;


public interface ProjectModel {

    /**
     * @return The ID of the domain for the project
     */
    String getDomainId();

    /**
     * @return the id of the project
     */
    String getId();

    /**
     * @return the name of the project
     */
    String getName();

    /**
     * @return the description of the project
     */
    String getDescription();

    /**
     * @return if the project is enabled
     */
    boolean isEnabled();

    /**
     * @return (Since v3.6) Indicates whether the project also acts as a domain.
     */
    boolean isDomain();


}
