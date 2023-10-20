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

package io.maestro3.agent.dao;

import io.maestro3.agent.model.general.MachineImage;

import java.util.List;

public interface MachineImageDao extends IImageRepository {

    MachineImage findByAliasForProject(String imageNameAlias, String tenantId, String regionId);

    <T extends MachineImage> void insert(List<T> imagesConfig);

    <T extends MachineImage>void save(T imageConfig);

    MachineImage findByNativeId(String nativeId);

    <T extends MachineImage> List<T> findByRegionId(String regionId);

    List<MachineImage> findByRegionIdAndTenantId(String regionId, String tenantId);

    void removeByNativeId(String nativeId);
}
