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

package io.maestro3.agent.openstack.api.compute;


import io.maestro3.agent.openstack.api.compute.bean.CreateFlavorParameters;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.compute.Flavor;

import java.util.List;
import java.util.Map;



public interface IFlavorService {

    Flavor get(String id) throws OSClientException;

    List<Flavor> list() throws OSClientException;

    Flavor create(CreateFlavorParameters parameters) throws OSClientException;

    void delete(String id) throws OSClientException;

    Map<String, String> listExtraSpecs(String flavorId) throws OSClientException;

    void createExtraSpec(String flavorId, Map<String, String> extraSpec) throws OSClientException;
}
