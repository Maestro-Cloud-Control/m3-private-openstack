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

package io.maestro3.agent.openstack.client;

import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.IOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;


public interface IOSClient extends IEndpointProvider {

    /**
     * Executes Open Stack requests. By default OSClient serialize null fields.
     *
     * @param request request
     * @param <T>     type of response
     * @return response enclosing result entity
     * @throws OSClientException Open Stack client exception
     */
    <T> IOSResponse<T> execute(IOSRequest<T> request) throws OSClientException;


    /**
     * Executes Open Stack requests with custom options.
     *
     * @param <T>     type of response
     * @param request request
     * @return response enclosing result entity
     * @throws OSClientException Open Stack client exception
     */
    <T> IOSResponse<T> execute(IOSRequest<T> request, IOSClientOption option) throws OSClientException;

    /**
     * @return client metadata. May be used for caching purposes.
     */
    IClientMetadata getMetadata() throws OSClientException;
}
