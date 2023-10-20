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

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.service.MachineImageDbService;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.image.SdkImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class OpenStackImageResolver implements IImageResolver {

    private MachineImageDbService machineImageDbService;

    @Autowired
    public OpenStackImageResolver(MachineImageDbService machineImageDbService) {
        this.machineImageDbService = machineImageDbService;
    }

    @Override
    public List<SdkImage> toSdkImages(IRegion region) {
        List<OpenStackMachineImage> imageList = machineImageDbService.findByRegionId(region.getId());
        List<SdkImage> images = new ArrayList<>();
        for (OpenStackMachineImage machineImage : imageList) {
            if (StringUtils.isNotBlank(machineImage.getNameAlias())) {
                SdkImage sdkImage = new SdkImage();
                sdkImage.setRegion(region.getRegionAlias());
                sdkImage.setAlias(machineImage.getNameAlias());
                sdkImage.setName(machineImage.getNativeName());
                sdkImage.setDescription(machineImage.getNameAlias());
                sdkImage.setOsType(machineImage.getPlatformType().getFullLabel());
                sdkImage.setImageType("PUBLIC");
                sdkImage.setCloud(SdkCloud.OPEN_STACK);
                images.add(sdkImage);
            }
        }
        return images;
    }

    @Override
    public PrivateCloudType getCloud() {
        return PrivateCloudType.OPEN_STACK;
    }
}
