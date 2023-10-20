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

package io.maestro3.agent.openstack.api.telemetry.impl;

import com.google.common.reflect.TypeToken;
import io.maestro3.agent.model.telemetry.Meter;
import io.maestro3.agent.model.telemetry.Resource;
import io.maestro3.agent.model.telemetry.Statistic;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.telemetry.ITelemetryService;
import io.maestro3.agent.openstack.api.telemetry.bean.CeilometerMeter;
import io.maestro3.agent.openstack.api.telemetry.bean.CeilometerResource;
import io.maestro3.agent.openstack.api.telemetry.bean.CeilometerStatistic;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.sdk.internal.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class TelemetryService extends BasicService implements ITelemetryService {

    private static final String RESOURCES_REQUEST = "/v2/resources" +
        "?q.field=metadata.instance_id&q.op=eq&q.value=%s";

    private static final String METERS_REQUEST = "/v2/meters" +
        "?q.field=resource_id&q.op=eq&q.value=%s";

    private static final String STATISTICS_REQUEST = "/v2/meters/%s/statistics" +
        "?q.field=resource_id&q.op=eq&q.value=%s" +
        "&q.field=timestamp&q.op=gt&q.value=%s" +
        "&period=%s";

    public TelemetryService(IOSClient client) {
        super(ServiceType.TELEMETRY, client);
    }

    @Override
    public List<Pair<Meter, String>> listMeters(String resourceId) throws OSClientException {
        Assert.hasText(resourceId, "resourceId must not be null or empty");

        List<Pair<Meter, String>> meters = new LinkedList<>();

        meters.addAll(listMetersById(resourceId));
        meters.addAll(listNetworkMeters(resourceId));
        return meters;
    }

    private List<Pair<Meter, String>> listMetersById(String resourceId) throws OSClientException {
        // this code is really weird!
        Type type = new TypeToken<List<CeilometerMeter>>() {
        }.getType();
        BasicOSRequest<List<CeilometerMeter>> request = BasicOSRequest
            .<List<CeilometerMeter>>builder(type, endpoint())
            .path(METERS_REQUEST, resourceId)
            .create();
        List<CeilometerMeter> meters = client.execute(request).getEntity();
        if (meters == null) {
            return Collections.emptyList();
        }
        List<Pair<Meter, String>> metersWithIdentifiers = new LinkedList<>();
        for (CeilometerMeter meter : meters) {
            metersWithIdentifiers.add(Pair.of(meter, null));
        }
        return metersWithIdentifiers;
    }

    private List<Pair<Meter, String>> listNetworkMeters(String resourceId) throws OSClientException {
        Type type = new TypeToken<List<CeilometerResource>>() {
        }.getType();
        BasicOSRequest<List<CeilometerResource>> request = BasicOSRequest
            .<List<CeilometerResource>>builder(type, endpoint())
            .path(RESOURCES_REQUEST, resourceId)
            .create();

        List<CeilometerResource> resources = client.execute(request).getEntity();
        if (CollectionUtils.isEmpty(resources)) {
            return Collections.emptyList();
        }
        List<Pair<Meter, String>> meters = new LinkedList<>();
        for (CeilometerResource resource : resources) {
            if (StringUtils.isBlank(resource.getId())) {
                continue;
            }
            String identifier = getIdentifierForNetworkResource(resource);
            if (StringUtils.isNotBlank(identifier)) {
                for (Pair<Meter, String> meterPair : listMetersById(resource.getId())) {
                    meterPair.setValue(identifier);
                    meters.add(meterPair);
                }
            }
        }
        return meters;
    }

    private String getIdentifierForNetworkResource(Resource resource) {
        if (resource == null) {
            return null;
        }
        Map<String, String> metadata = resource.getMetadata();
        if (metadata == null) {
            return null;
        }
        String identifier;
        if (StringUtils.isNotBlank(identifier = metadata.get("name"))) {
            return identifier;
        }
        if (StringUtils.isNotBlank(identifier = metadata.get("mac"))) {
            return identifier;
        }
        return null;
    }

    @Override
    public List<Statistic> listStatistics(String meterName, String resourceId, long granularity, long startTime) throws OSClientException {
        Assert.hasText(meterName, "meterName must not be null or empty");
        Assert.hasText(resourceId, "resourceId must not be null or empty");

        Date startDate = new Date(startTime);
        // this code is also weird!
        Type type = new TypeToken<List<CeilometerStatistic>>() {
        }.getType();
        BasicOSRequest<List<CeilometerStatistic>> request = BasicOSRequest
            .<List<CeilometerStatistic>>builder(type, endpoint())
            .path(
                STATISTICS_REQUEST,
                meterName,
                resourceId,
                DateUtils.formatDate(startDate, DateUtils.FORMAT_TIME),
                String.valueOf(TimeUnit.MILLISECONDS.toSeconds(granularity))
            )
            .create();
        List<CeilometerStatistic> statistics = client.execute(request).getEntity();
        return statistics != null ? new ArrayList<>(statistics) : Collections.<Statistic>emptyList();
    }

}
