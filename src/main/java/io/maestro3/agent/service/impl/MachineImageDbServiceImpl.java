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

package io.maestro3.agent.service.impl;

import io.maestro3.agent.dao.MachineImageDao;
import io.maestro3.agent.model.general.MachineImage;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.service.MachineImageDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MachineImageDbServiceImpl implements MachineImageDbService {

    private MachineImageDao machineImageDao;

    public MachineImageDbServiceImpl(@Autowired MachineImageDao machineImageDao) {
        this.machineImageDao = machineImageDao;
    }

    @Override
    public OpenStackMachineImage findOpenStackImageByAliasForProject(String imageNameAlias, String tenantId, String regionId) {
        return (OpenStackMachineImage) machineImageDao.findByAliasForProject(imageNameAlias, tenantId, regionId);
    }

    @Override
    public OpenStackMachineImage findByNativeId(String nativeId) {
        return (OpenStackMachineImage) machineImageDao.findByNativeId(nativeId);
    }

    @Override
    public List<OpenStackMachineImage> findByRegionId(String regionId) {
        return machineImageDao.findByRegionId(regionId);
    }

    @Override
    public List<OpenStackMachineImage> findByRegionIdAndTenantId(String regionId, String tenantId) {
        List<MachineImage> machineImages = machineImageDao.findByRegionIdAndTenantId(regionId, tenantId);
        return convert(machineImages);
    }

    private List<OpenStackMachineImage> convert(List<MachineImage> machineImages) {
        return Optional.ofNullable(machineImages)
                .stream()
                .filter(OpenStackMachineImage.class::isInstance)
                .map(OpenStackMachineImage.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void insert(List<OpenStackMachineImage> imagesConfig) {
        Assert.notEmpty(imagesConfig, "imagesConfig cannot be null or empty");
        for (OpenStackMachineImage openStackMachineImage : imagesConfig) {
            assertParametersValid(openStackMachineImage);
        }
        machineImageDao.insert(imagesConfig);
    }

    @Override
    public void save(OpenStackMachineImage machineImage) {
        Assert.notNull(machineImage, "imagesConfig cannot be null or empty");
        assertParametersValid(machineImage);

        machineImageDao.save(machineImage);
    }

    @Override
    public void removeByNativeId(String nativeId) {
        Assert.notNull(nativeId, "nativeId cannot be null or empty");

        machineImageDao.removeByNativeId(nativeId);
    }

    private void assertParametersValid(OpenStackMachineImage image) {
        Assert.notNull(image, "Image cannot be null");
        Assert.hasText(image.getNativeId(), "Image.NativeId cannot be null or empty");
        Assert.hasText(image.getNativeName(), "Image.NativeName cannot be null or empty");
        Assert.hasText(image.getNameAlias(), "Image.NameAlias cannot be null or empty");
        Assert.hasText(image.getRegionId(), "Image.RegionId cannot be null or empty");
        Assert.notNull(image.getPlatformType(), "Image.PlatformType cannot be null");
    }
}
