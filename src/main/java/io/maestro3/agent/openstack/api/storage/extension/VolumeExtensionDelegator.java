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

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class VolumeExtensionDelegator extends BasicService implements IVolumesExtension {
    private final AtomicReference<IVolumesExtension> extensionV2 = new AtomicReference<>();
    private final AtomicReference<IVolumesExtension> extensionV3 = new AtomicReference<>();

    public VolumeExtensionDelegator(IOSClient client) {
        super(client, ServiceType.VOLUME, ServiceType.VOLUME_V3);
    }

    @Override
    public CinderVolume get(String volumeId) throws OSClientException {
        return delegate(getVersion()).get(volumeId);
    }

    @Override
    public List<CinderVolume> list() throws OSClientException {
        return delegate(getVersion()).list();
    }

    @Override
    public List<CinderVolume> list(String projectId) throws OSClientException {
        return delegate(getVersion()).list(projectId);
    }

    @Override
    public Iterator<CinderVolume> listLimited(int limit) throws OSClientException {
        return delegate(getVersion()).listLimited(limit);
    }

    @Override
    public CinderVolume create(CreateCinderVolumeParameters parameters) throws OSClientException {
        return delegate(getVersion()).create(parameters);
    }

    @Override
    public void delete(String volumeId) throws OSClientException {
        delegate(getVersion()).delete(volumeId);
    }

    @Override
    public CinderVolume inspect(String volumeId) throws OSClientException {
        return delegate(getVersion()).inspect(volumeId);
    }

    @Override
    public void updateMetadata(String volumeId, Map<String, String> metadata) throws OSClientException {
        delegate(getVersion()).updateMetadata(volumeId, metadata);
    }

    @Override
    public void extendVolume(String volumeId, int sizeGB) throws OSClientException {
        delegate(getVersion()).extendVolume(volumeId, sizeGB);
    }


    private IVolumesExtension delegate(String version) {
        if (StringUtils.isNotBlank(version) && version.contains("v3")) {
            IVolumesExtension extensionV3Local = extensionV3.get();
            if (extensionV3Local == null) {
                synchronized (this) {
                    extensionV3Local = extensionV3.get();
                    if (extensionV3Local == null) {
                        extensionV3Local = new VolumesExtensionV3(client);
                        extensionV3.set(extensionV3Local);
                    }
                }
            }
            return extensionV3Local;
        }
        IVolumesExtension extensionV2Local = extensionV2.get();
        if (extensionV2Local == null) {
            synchronized (this) {
                extensionV2Local = extensionV2.get();
                if (extensionV2Local == null) {
                    extensionV2Local = new VolumesExtension(client);
                    extensionV2.set(extensionV2Local);
                }
            }
        }
        return extensionV2Local;
    }
}
