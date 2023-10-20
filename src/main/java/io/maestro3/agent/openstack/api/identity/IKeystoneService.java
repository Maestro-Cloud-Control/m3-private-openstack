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


public interface IKeystoneService {

    /**
     * @return tenant service to manage tenants
     */
    IProjectService projects();

    /**
     * @return user service to manage users
     */
    IUserService users();

    /**
     * @return role service to manage roles
     */
    IRoleService roles();

    /**
     * @return domain service to manage domains
     */
    IDomainService domains();
}
