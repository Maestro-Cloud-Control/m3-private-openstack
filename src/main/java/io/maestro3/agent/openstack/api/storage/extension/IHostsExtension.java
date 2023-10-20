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

import io.maestro3.agent.openstack.api.storage.bean.CinderHost;
import io.maestro3.agent.openstack.api.storage.bean.CinderHostDetails;
import io.maestro3.agent.openstack.api.storage.request.ListHostsRequest;
import io.maestro3.agent.openstack.exception.OSClientException;

import java.util.List;


public interface IHostsExtension {

    List<CinderHost> list(ListHostsRequest request) throws OSClientException;

    CinderHostDetails details(String tenantId, String hostName) throws OSClientException;
}
