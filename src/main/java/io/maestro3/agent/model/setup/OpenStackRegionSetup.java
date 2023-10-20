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
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackRegionSetup {
    private OpenStackRegionConfig regionConfig;
    private List<OpenStackTenant> tenantsConfig;
    private List<OpenStackFlavorConfig> flavorsConfig;
    private List<OpenStackMachineImage> imagesConfig;

    public OpenStackRegionSetup() {
        // default for deserialization
    }

    public OpenStackRegionSetup(OpenStackRegionConfig regionConfig,
                                List<OpenStackTenant> tenantsConfig,
                                List<OpenStackFlavorConfig> flavorsConfig,
                                List<OpenStackMachineImage> imagesConfig) {
        this.regionConfig = regionConfig;
        this.tenantsConfig = tenantsConfig;
        this.flavorsConfig = flavorsConfig;
        this.imagesConfig = imagesConfig;
    }

    public OpenStackRegionConfig getRegionConfig() {
        return regionConfig;
    }

    public void setRegionConfig(OpenStackRegionConfig regionConfig) {
        this.regionConfig = regionConfig;
    }

    public List<OpenStackTenant> getTenantsConfig() {
        return tenantsConfig;
    }

    public void setTenantsConfig(List<OpenStackTenant> tenantsConfig) {
        this.tenantsConfig = tenantsConfig;
    }

    public List<OpenStackFlavorConfig> getFlavorsConfig() {
        return flavorsConfig;
    }

    public void setFlavorsConfig(List<OpenStackFlavorConfig> flavorsConfig) {
        this.flavorsConfig = flavorsConfig;
    }

    public List<OpenStackMachineImage> getImagesConfig() {
        return imagesConfig;
    }

    public void setImagesConfig(List<OpenStackMachineImage> imagesConfig) {
        this.imagesConfig = imagesConfig;
    }

    @Override
    public String toString() {
        return "OpenStackRegionSetup{" +
                "regionConfig=" + regionConfig +
                ", tenantsConfig=" + tenantsConfig +
                ", flavorsConfig=" + flavorsConfig +
                ", imagesConfig=" + imagesConfig +
                '}';
    }
}
