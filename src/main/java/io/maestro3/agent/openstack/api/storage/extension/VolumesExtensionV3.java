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

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.ActionResponse;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.api.storage.bean.VolumeAction;
import io.maestro3.agent.openstack.api.storage.bean.VolumeActions;
import io.maestro3.agent.openstack.api.support.LimitedIterator;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.client.IOSClientOption;
import io.maestro3.agent.openstack.client.OSClientOption;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class VolumesExtensionV3 extends BasicService implements IVolumesExtension {

    private static final IOSClientOption WITH_NULL_SERIALIZER = OSClientOption.builder().withNullSerializer().build();
    public VolumesExtensionV3(IOSClient client) {
        super(ServiceType.VOLUME_V3, client);
    }

    @Override
    public CinderVolume get(String volumeId) throws OSClientException {
        BasicOSRequest<VolumeSpec> getVolumeRequest = BasicOSRequest.builder(VolumeSpec.class, endpoint())
            .path("/volumes/%s", volumeId)
            .create();

        VolumeSpec volumeSpec = client.execute(getVolumeRequest).getEntity();
        if (volumeSpec == null) {
            return null;
        }
        return volumeSpec.volume;
    }

    @Override
    public List<CinderVolume> list() throws OSClientException {
        BasicOSRequest<Volumes> listVolumesRequest = BasicOSRequest.builder(Volumes.class, endpoint())
            .path("/volumes/detail")
            .create();

        Volumes volumes = client.execute(listVolumesRequest).getEntity();
        if (volumes == null) {
            return null;
        }
        return volumes.cinderVolumes;
    }

    @Override
    public List<CinderVolume> list(String projectId) throws OSClientException {
        BasicOSRequest<Volumes> listVolumesRequest = BasicOSRequest.builder(Volumes.class, endpoint())
            .path("/volumes/detail", projectId)
            .create();

        Volumes volumes = client.execute(listVolumesRequest).getEntity();
        if (volumes == null) {
            return Collections.emptyList();
        }
        return volumes.cinderVolumes;
    }

    @Override
    public Iterator<CinderVolume> listLimited(int limit) throws OSClientException {
        return new LimitedVolumesIterator(limit);
    }

    @Override
    public CinderVolume create(CreateCinderVolumeParameters parameters) throws OSClientException {
        BasicOSRequest<VolumeSpec> createVolumeRequest = BasicOSRequest.builder(VolumeSpec.class, endpoint())
            .path("/volumes")
            .post(getCreateVolumeSpec(parameters))
            .create();

        IOSResponse<VolumeSpec> response = client.execute(createVolumeRequest);
        if (response.getEntity() == null) {
            return null;
        }
        return response.getEntity().volume;
    }

    private CreateVolumeSpec getCreateVolumeSpec(CreateCinderVolumeParameters parameters) {
        CreatedVolume volume = new CreatedVolume();
        volume.setName(parameters.getName());
        if (parameters.getSizeGb() != null) {
            volume.setSize(parameters.getSizeGb());
        }
        if (StringUtils.isNotBlank(parameters.getAvailabilityZone())) {
            volume.setAvailabilityZone(parameters.getAvailabilityZone());
        }
        volume.setMetadata(parameters.getMetadata());
        volume.setVolumeId(parameters.getVolumeId());
        return new CreateVolumeSpec(volume);
    }

    @Override
    public void delete(String volumeId) throws OSClientException {
        BasicOSRequest<Void> deleteVolumeRequest = BasicOSRequest.builder(Void.class, endpoint())
            .path("/volumes/%s", volumeId)
            .delete()
            .create();
        client.execute(deleteVolumeRequest);
    }

    @Override
    public void extendVolume(String volumeId, int sizeGB) throws OSClientException {
        Assert.hasText(volumeId, "volume id can not be null or empty");
        Assert.isTrue(sizeGB > 0, "sizeGB must be a positive integer");
        invokeAction(volumeId, VolumeActions.extendVolume(sizeGB));
    }

    @Override
    public CinderVolume inspect(String volumeId) throws OSClientException {
        BasicOSRequest<VolumeSpec> inspectVolumeRequest = BasicOSRequest.builder(VolumeSpec.class, endpoint())
            .path("/volumes/%s", volumeId)
            .create();

        IOSResponse<VolumeSpec> response = client.execute(inspectVolumeRequest);
        if (response.getEntity() == null) {
            return null;
        }
        return response.getEntity().volume;
    }

    @Override
    public void updateMetadata(String volumeId, Map<String, String> metadata) throws OSClientException {
        BasicOSRequest<Void> updateMetadata = BasicOSRequest.builder(Void.class, endpoint())
            .path("/volumes/%s/metadata", volumeId)
            .put(new VolumeMetadata(metadata))
            .create();

        client.execute(updateMetadata);
    }

    private class LimitedVolumesIterator extends LimitedIterator<CinderVolume> {
        private LimitedVolumesIterator(int limit) {
            super(limit);
        }

        @Override
        protected String getBasePath() {
            return "/volumes/detail";
        }

        @Override
        protected List<CinderVolume> retrieveResourcesPage(String path) throws OSClientException {
            String fullPath = path + "&sort=created_at:asc";
            BasicOSRequest<Volumes> request = BasicOSRequest.builder(Volumes.class, endpoint())
                .path(fullPath)
                .create();

            Volumes volumes = client.execute(request).getEntity();
            if (volumes == null) {
                return null;
            }
            return volumes.cinderVolumes;
        }
    }

    private ActionResponse invokeAction(String volumeId, VolumeAction action) throws OSClientException {
        Assert.hasText(volumeId, "volume id can not be null or empty");
        BasicOSRequest<String> request = BasicOSRequest.builder(String.class, endpoint())
            .path("/volumes/%s/action", volumeId)
            .post(action)
            .create();

        String error = client.execute(request, WITH_NULL_SERIALIZER).getEntity();
        if (StringUtils.isNotBlank(error)) {
            return ActionResponse.failure(error);
        }
        return ActionResponse.success();
    }

    private static class Volumes {
        @SerializedName("volumes")
        private List<CinderVolume> cinderVolumes;
    }

    private static class VolumeSpec {
        private CinderVolume volume;
    }

    private static class CreateVolumeSpec {
        private final CreatedVolume volume;

        private CreateVolumeSpec(CreatedVolume volume) {
            this.volume = volume;
        }
    }

    private static class CreatedVolume {
        private String name;
        @SerializedName("source_volid")
        private String volumeId;
        private Integer size;
        private String status;
        @SerializedName("availability_zone")
        private String availabilityZone;
        private Map<String, String> metadata;

        public void setName(String name) {
            this.name = name;
        }

        public void setVolumeId(String volumeId) {
            this.volumeId = volumeId;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setAvailabilityZone(String availabilityZone) {
            this.availabilityZone = availabilityZone;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    private static final class VolumeMetadata {
        private final Map<String, String> metadata;

        private VolumeMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }
}
