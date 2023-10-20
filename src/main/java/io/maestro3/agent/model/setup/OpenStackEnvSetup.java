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

package io.maestro3.agent.model.setup;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackEnvSetup {

    private List<OpenStackRegionSetup> regionsSetup;

    public OpenStackEnvSetup() {
        //default for deserialization
    }

    public OpenStackEnvSetup(List<OpenStackRegionSetup> regionsSetup) {
        this.regionsSetup = regionsSetup;
    }

    public List<OpenStackRegionSetup> getRegionsSetup() {
        return regionsSetup;
    }

    public void setRegionsSetup(List<OpenStackRegionSetup> regionsSetup) {
        this.regionsSetup = regionsSetup;
    }

    @Override
    public String toString() {
        return "EnvSetup{" +
                "regionsSetup=" + regionsSetup +
                '}';
    }
}
