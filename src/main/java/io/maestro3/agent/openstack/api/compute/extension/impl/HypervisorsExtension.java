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
import io.maestro3.agent.openstack.api.compute.bean.Hypervisor;
import io.maestro3.agent.openstack.api.compute.extension.IHypervisorsExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;

import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class HypervisorsExtension extends BasicComputeService implements IHypervisorsExtension {

    public HypervisorsExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<Hypervisor> list() throws OSClientException {
        BasicOSRequest<HypervisorsHolder> request = builder(HypervisorsHolder.class, endpoint())
                .path("/os-hypervisors/detail")
                .create();

        HypervisorsHolder hypervisorsHolder = client.execute(request).getEntity();
        return hypervisorsHolder == null ? null : hypervisorsHolder.hypervisors;
    }

    private static class HypervisorsHolder {
        private List<Hypervisor> hypervisors;
    }
}
