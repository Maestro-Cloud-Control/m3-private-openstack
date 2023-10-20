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

import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.internal.util.Assert;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableRowItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class OpenstackPrivateThirdStepProcessor extends AbstractOSActivateRegionStepProcessor {

    private OpenStackApiProvider apiProvider;
    private int listImagesPeriodMonth;

    @Autowired
    public OpenstackPrivateThirdStepProcessor(OpenStackApiProvider apiProvider,
                                              @Value("${configuration.wizard.os.image.list.period.month:1}") int listImagesPeriodMonth) {
        this.apiProvider = apiProvider;
        this.listImagesPeriodMonth = listImagesPeriodMonth;
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        return SdkPrivateStep.builder()
            .withTitle("Region flavors configuration")
            .withDescription("#wizard.manage-private-cloud-wizard.forth-step.help")
            .withId(3)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkPrivateStep forthStep = PrivateWizardUtils.getStepById(4, wizard.getStep());
        IOpenStackApi client = buildClientNullable(apiProvider, wizard, firstStep);
        List<Image> images;
        try {
            Assert.notNull(client, "client should not be null");
            //find images that were update in past 'listImagesPeriodMonth' month
            images = client.images().image().listPublic(Date.from(ZonedDateTime.now().minusMonths(listImagesPeriodMonth).toInstant()));
        } catch (Exception e) {
            throw new ReadableAgentException("Failed to describe images for region configuration", e);
        }

        forthStep.getData().getTable().add(OpenstackWizardItems.buildImagesTable(images));
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep thirdStep = PrivateWizardUtils.getStepById(3, wizard.getStep());
        SdkTableItem flavorsTable = PrivateWizardUtils.findItem(thirdStep.getData().getTable(), OpenstackConfigurationWizardConstant.FLAVOR_TABLE, SdkTableItem::getName);
        List<SdkTableRowItem> selectedFlavors = PrivateWizardUtils.getSelectedRowForTable(flavorsTable);
        if (selectedFlavors.size() == 0) {
            flavorsTable.setServerError("At least one flavor should be specified");
            wizard.invalidate();
        }
    }

}
