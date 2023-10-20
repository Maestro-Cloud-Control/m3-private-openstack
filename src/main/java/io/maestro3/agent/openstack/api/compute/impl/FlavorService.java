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

package io.maestro3.agent.openstack.api.compute.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.Flavor;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.IFlavorService;
import io.maestro3.agent.openstack.api.compute.bean.CreateFlavorParameters;
import io.maestro3.agent.openstack.api.compute.bean.NovaFlavor;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


class FlavorService extends BasicComputeService implements IFlavorService {

    FlavorService(IOSClient client) {
        super(client);
    }

    @Override
    public Flavor get(String id) throws OSClientException {
        Assert.notNull(id, "id cannot be null or empty.");
        BasicOSRequest<FlavorWrapper> request = builder(FlavorWrapper.class, endpoint()).path("/flavors/%s", id).create();
        FlavorWrapper wrapper = client.execute(request).getEntity();
        return wrapper == null ? null : wrapper.flavor;
    }

    @Override
    public List<Flavor> list() throws OSClientException {
        BasicOSRequest<FlavorsWrapper> request = builder(FlavorsWrapper.class, endpoint())
                .path("/flavors/detail")
                .create();
        FlavorsWrapper wrapper = client.execute(request).getEntity();
        return wrapper == null ? null : wrapper.getFlavors();
    }

    @Override
    public Flavor create(CreateFlavorParameters parameters) throws OSClientException {
        BasicOSRequest<FlavorWrapper> request = builder(FlavorWrapper.class, endpoint())
                .path("/flavors")
                .post(new CreateFlavorWrapper(parameters))
                .create();
        FlavorWrapper wrapper = client.execute(request).getEntity();
        return wrapper == null ? null : wrapper.flavor;
    }

    @Override
    public void delete(String id) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/flavors/" + id)
                .delete()
                .create();
        client.execute(request).getEntity();
    }

    @Override
    public Map<String, String> listExtraSpecs(String flavorId) throws OSClientException {
        BasicOSRequest<ExtraSpecs> request = builder(ExtraSpecs.class, endpoint())
                .path("/flavors/" + flavorId + "/os-extra_specs")
                .create();
        ExtraSpecs extraSpecs = client.execute(request).getEntity();
        Map<String, String> extraSpecsMap = null;
        if (extraSpecs != null) {
            extraSpecsMap = extraSpecs.extraSpecsMap;
        }
        if (MapUtils.isEmpty(extraSpecsMap)) {
            return new HashMap<>();
        }
        return extraSpecsMap;
    }

    @Override
    public void createExtraSpec(String flavorId, Map<String, String> extraSpec) throws OSClientException {
        Assert.notEmpty(extraSpec, "extraSpec cannot be null or empty.");

        ExtraSpecs extraSpecs = new ExtraSpecs(extraSpec);
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/flavors/" + flavorId + "/os-extra_specs")
                .post(extraSpecs)
                .create();
        client.execute(request);
    }

    private static class ExtraSpecs {
        @SerializedName("extra_specs")
        private final Map<String, String> extraSpecsMap;

        private ExtraSpecs(Map<String, String> extraSpecs) {
            this.extraSpecsMap = extraSpecs;
        }
    }

    private static class FlavorWrapper {
        NovaFlavor flavor;
    }

    private static class CreateFlavorWrapper {
        private final CreateFlavorParameters flavor;

        private CreateFlavorWrapper(CreateFlavorParameters flavor) {
            this.flavor = flavor;
        }
    }

    private static class FlavorsWrapper {
        private List<NovaFlavor> flavors;

        private List<Flavor> getFlavors() {
            List<Flavor> flavorsList = new ArrayList<>();
            if (CollectionUtils.isEmpty(flavors)) {
                return flavorsList;
            }
            flavorsList.addAll(flavors);
            return flavorsList;
        }
    }
}
