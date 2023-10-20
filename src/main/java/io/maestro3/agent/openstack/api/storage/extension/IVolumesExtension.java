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

package io.maestro3.agent.openstack.api.storage.extension;

import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.exception.OSClientException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public interface IVolumesExtension {

    CinderVolume get(String volumeId) throws OSClientException;

    List<CinderVolume> list(String projectId) throws OSClientException;

    List<CinderVolume> list() throws OSClientException;

    Iterator<CinderVolume> listLimited(int limit) throws OSClientException;

    CinderVolume create(CreateCinderVolumeParameters parameters) throws OSClientException;

    void delete(String volumeId) throws OSClientException;

    CinderVolume inspect(String volumeId) throws OSClientException;

    void updateMetadata(String volumeId, Map<String, String> metadata) throws OSClientException;

    void extendVolume(String volumeId, int sizeGB) throws OSClientException;

}
