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

package io.maestro3.agent.openstack.api.identity;

import io.maestro3.agent.openstack.api.identity.bean.TokenMeta;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.identity.User;

import java.util.List;


public interface IUserService {

    User create(User user) throws OSClientException;

    TokenMeta getCurrentUserInfo() throws OSClientException;

    void delete(String userId) throws OSClientException;

    User getUserByName(String name) throws OSClientException;

    List<User> list() throws OSClientException;
}
