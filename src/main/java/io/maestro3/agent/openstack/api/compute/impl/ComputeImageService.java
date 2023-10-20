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

package io.maestro3.agent.openstack.api.compute.impl;

import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.IComputeImageService;
import io.maestro3.agent.openstack.api.compute.bean.action.ServerActions;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Map;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;



class ComputeImageService extends BasicComputeService implements IComputeImageService {

    ComputeImageService(IOSClient client) {
        super(client);
    }

    @Override
    public String create(String serverId, String imageName) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText(imageName, "imageName cannot be null or empty.");

        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/action", serverId)
                .post(ServerActions.createImage(imageName))
                .headers(BasicOSRequest.Headers.LOCATION)
                .create();
        IOSResponse<Void> response = client.execute(request);
        return extractImageIdFromLocationHeader(response);
    }

    private String extractImageIdFromLocationHeader(IOSResponse<Void> response) {
        Map<String, String> headers = response.getHeaders();
        if (MapUtils.isEmpty(headers)) {
            return null;
        }

        // Location header may look like: http://controller:9292/images/a8eca55a-f2e3-4132-9786-5730dc738c99
        String locationUrl = headers.get(BasicOSRequest.Headers.LOCATION);
        if (StringUtils.isBlank(locationUrl)) {
            return null;
        }
        String[] split = locationUrl.split("/");
        if (split.length != 0) {
            return split[split.length - 1];
        }
        return null;
    }
}
