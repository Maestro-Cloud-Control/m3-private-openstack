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
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.UpdateVolumeQuotaRequest;
import io.maestro3.agent.openstack.api.storage.bean.VolumeQuota;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.springframework.util.Assert;


public class QuotasExtension extends BasicService implements IQuotasExtension {

    public QuotasExtension(IOSClient client) {
        super(ServiceType.VOLUME, client);
    }

    @Override
    public VolumeQuota get(String tenantId) throws OSClientException {
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");
        BasicOSRequest<VolumeQuotaSet> getVolumesQuota = BasicOSRequest.builder(VolumeQuotaSet.class, endpoint())
                .path("/os-quota-sets/%s", tenantId)
                .create();

        VolumeQuotaSet quotaSet = client.execute(getVolumesQuota).getEntity();
        return (quotaSet != null) ? quotaSet.quota : null;
    }

    @Override
    public VolumeQuota update(String tenantId, UpdateVolumeQuotaRequest request) throws OSClientException {
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");
        BasicOSRequest<VolumeQuotaSet> getVolumesQuota = BasicOSRequest.builder(VolumeQuotaSet.class, endpoint())
                .path("/os-quota-sets/%s", tenantId)
                .put(new UpdateVolumeQuotaSet(request))
                .create();

        VolumeQuotaSet quotaSet = client.execute(getVolumesQuota).getEntity();
        return (quotaSet != null) ? quotaSet.quota : null;
    }

    private static class VolumeQuotaSet {
        @SerializedName("quota_set")
        private VolumeQuota quota;
    }

    private static class UpdateVolumeQuotaSet {
        @SerializedName("quota_set")
        private final UpdateVolumeQuotaRequest quota;

        private UpdateVolumeQuotaSet(UpdateVolumeQuotaRequest quota) {
            this.quota = quota;
        }
    }
}
