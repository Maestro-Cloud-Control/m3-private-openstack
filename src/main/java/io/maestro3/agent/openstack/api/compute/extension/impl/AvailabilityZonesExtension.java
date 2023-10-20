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

package io.maestro3.agent.openstack.api.compute.extension.impl;

import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.bean.AvailabilityZone;
import io.maestro3.agent.openstack.api.compute.bean.AvailabilityZoneResponse;
import io.maestro3.agent.openstack.api.compute.extension.IAvailabilityZonesExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class AvailabilityZonesExtension extends BasicComputeService implements IAvailabilityZonesExtension {

    public AvailabilityZonesExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<AvailabilityZone> list() throws OSClientException {
        BasicOSRequest<AvailabilityZonesHolder> request = builder(AvailabilityZonesHolder.class, endpoint())
                .path("/os-availability-zone/detail")
                .create();

        AvailabilityZonesHolder availabilityZonesHolder = client.execute(request).getEntity();
        return availabilityZonesHolder == null ? null : convertToAvailabilityZones(availabilityZonesHolder.availabilityZoneInfo);
    }

    private List<AvailabilityZone> convertToAvailabilityZones(Collection<AvailabilityZoneResponse> availabilityZoneResponses) {
        List<AvailabilityZone> availabilityZones = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(availabilityZoneResponses)) {
            for (AvailabilityZoneResponse availabilityZoneResponse : availabilityZoneResponses) {
                availabilityZones.add(convertToAvailabilityZone(availabilityZoneResponse));
            }
        }
        return availabilityZones;
    }

    private AvailabilityZone convertToAvailabilityZone(AvailabilityZoneResponse availabilityZoneResponse) {
        AvailabilityZone availabilityZone = new AvailabilityZone();
        availabilityZone.setName(availabilityZoneResponse.getZoneName());
        availabilityZone.setAvailable(availabilityZoneResponse.getZoneState().isAvailable());
        if (MapUtils.isEmpty(availabilityZoneResponse.getHosts()) || CollectionUtils.isEmpty(availabilityZoneResponse.getHosts().keySet())) {
            availabilityZone.setHosts(Collections.emptyList());
        } else {
            availabilityZone.setHosts(new ArrayList<>(availabilityZoneResponse.getHosts().keySet()));
        }
        return availabilityZone;
    }

    @SuppressWarnings("unused")
    private static final class AvailabilityZonesHolder {
        private List<AvailabilityZoneResponse> availabilityZoneInfo;
    }
}
