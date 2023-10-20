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

package io.maestro3.agent.openstack.api.telemetry;

import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.telemetry.Meter;
import io.maestro3.agent.model.telemetry.Statistic;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;


public interface ITelemetryService {

    /**
     * Get meters list for given resource.
     *
     * @param resourceId the id or instance, for example
     * @return a list of available meters for the resource for all time (with additional identifier if present)
     * @throws OSClientException Open Stack client exception
     */
    List<Pair<Meter, String>> listMeters(String resourceId) throws OSClientException;

    /**
     * Get aggregated statistics on given meter for given resource.
     *
     * @param meterName   the name of the meter
     * @param resourceId  the id of instance, for example
     * @param granularity samples (measures) will get aggregated within this amount of time (in milliseconds)
     * @param startTime   samples (measures) will be ignored outside this period of time
     * @return a sorted list of retrieved data
     * @throws OSClientException Open Stack client exception
     */
    List<Statistic> listStatistics(String meterName, String resourceId, long granularity, long startTime) throws OSClientException;

}
