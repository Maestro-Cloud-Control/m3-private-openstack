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

package io.maestro3.agent.openstack.api.networking.extension;

import io.maestro3.agent.openstack.api.networking.bean.Router;
import io.maestro3.agent.openstack.api.networking.request.AddRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.CreateRouter;
import io.maestro3.agent.openstack.api.networking.request.RemoveRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.UpdateRouter;
import io.maestro3.agent.openstack.exception.OSClientException;

import java.util.List;


public interface IRouterExtension {

    List<Router> list() throws OSClientException;

    List<Router> listByName(String name) throws OSClientException;

    Router create(CreateRouter configuration) throws OSClientException;

    Router detail(String routerId) throws OSClientException;

    void update(String routerId, UpdateRouter configuration) throws OSClientException;

    void delete(String routerId) throws OSClientException;

    void addInterface(String routerId, AddRouterInterface configuration) throws OSClientException;

    void removeInterface(String routerId, RemoveRouterInterface configuration) throws OSClientException;

}
