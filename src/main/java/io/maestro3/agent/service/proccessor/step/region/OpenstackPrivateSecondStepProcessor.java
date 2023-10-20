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

package io.maestro3.agent.service.proccessor.step.region;

import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.compute.Flavor;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.openstack.provider.OpenStackApiRequest;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.exception.M3SdkException;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTextItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
public class OpenstackPrivateSecondStepProcessor extends AbstractOSActivateRegionStepProcessor {

    private OpenStackApiProvider apiProvider;
    private IOpenStackRegionRepository regionRepository;
    private int defaultStorageSizeForUnknownFlavors;

    @Autowired
    public OpenstackPrivateSecondStepProcessor(OpenStackApiProvider apiProvider, IOpenStackRegionRepository regionRepository,
                                               @Value("${configuration.wizard.os.flavor.default.disk}") int defaultStorageSizeForUnknownFlavors) {
        this.apiProvider = apiProvider;
        this.regionRepository = regionRepository;
        this.defaultStorageSizeForUnknownFlavors = defaultStorageSizeForUnknownFlavors;
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        return SdkPrivateStep.builder()
            .withTitle("Region parameters setup")
            .withDescription("#wizard.manage-private-cloud-wizard.third-step.help")
            .withId(2)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkPrivateStep thirdStep = PrivateWizardUtils.getStepById(3, wizard.getStep());
        IOpenStackApi client = buildClientNullable(wizard, firstStep);
        List<Flavor> flavors;
        try {
            if (client == null) {
                throw new M3SdkException("client should not be null");
            }
            flavors = client.compute().flavors().list();
        } catch (Exception e) {
            throw new ReadableAgentException("Failed to describe flavors for region configuration", e);
        }
        thirdStep.getData().getMessage().add(PrivateWizardUtils.messageInfoItem("messageInfo",
            String.format("Please note that for flavors with 'UNKNOWN' storage the default value (%sMb) will be applied", defaultStorageSizeForUnknownFlavors), "0"));
        thirdStep.getData().getTable().add(OpenstackWizardItems.buildFlavorTable(flavors));
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep secondStep = PrivateWizardUtils.getStepById(2, wizard.getStep());
        SdkTextItem tenantNameItem = PrivateWizardUtils.getTextItem(secondStep, OpenstackConfigurationWizardConstant.REGION_NAME_ITEM);
        String tenantName = tenantNameItem.getValue();
        OpenStackRegionConfig existingRegion = regionRepository.findByAliasInCloud(tenantName);
        if (existingRegion != null) {
            tenantNameItem.setServerError("Region with specified name already exist");
            wizard.invalidate();
        }
    }

    private IOpenStackApi buildClientNullable(SdkPrivateWizard wizard, SdkPrivateStep step) {
        OpenStackApiRequest.OpenStackApiRequestBuilder requestBuilder = new OpenStackApiRequest.OpenStackApiRequestBuilder();
        for (Map.Entry<String, BiFunction<OpenStackApiRequest.OpenStackApiRequestBuilder, String, OpenStackApiRequest.OpenStackApiRequestBuilder>> entry :
            OpenstackConfigurationWizardConstant.REGION_REQUEST_FILLERS.entrySet()) {
            SdkTextItem item = PrivateWizardUtils.getTextItem(step, entry.getKey());
            String parameterValue = item.getValue();
            if (!StringUtils.isBlank(parameterValue)) {
                entry.getValue().apply(requestBuilder, parameterValue.trim());
            } else {
                item.setServerError("Parameter should not be empty");
                wizard.invalidate();
                return null;
            }
        }
        SdkOptionItem osVersionSelect = PrivateWizardUtils.getSelectedOptionItem(step.getData().getSelect(), "osVersionSelect");
        requestBuilder.setVersion(OpenStackVersion.valueOf(osVersionSelect.getValue()));
        return apiProvider.openStackNoCache(requestBuilder.build());
    }
}
