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

import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableRowItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenstackPrivateForthStepProcessor extends AbstractOSActivateRegionStepProcessor {

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        return SdkPrivateStep.builder()
            .withTitle("Region images configuration")
            .withDescription("#wizard.manage-private-cloud-wizard.fifth-step.help")
            .withId(4)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        SdkPrivateStep forthStep = PrivateWizardUtils.getStepById(4, wizard.getStep());
        SdkTableItem imagesTable = PrivateWizardUtils.findItem(forthStep.getData().getTable(), OpenstackConfigurationWizardConstant.IMAGE_TABLE, SdkTableItem::getName);
        List<SdkTableRowItem> selectedImages = PrivateWizardUtils.getSelectedRowForTable(imagesTable);
        Map<String, String> params = selectedImages.stream()
            .collect(Collectors.toMap(r -> r.getHiddenData().get("id"), r -> r.getHiddenData().get("name")));
        SdkTableItem imageToPlatformTable = OpenstackWizardItems.buildImageToPlatformTable(params);
        PrivateWizardUtils.getStepById(5, wizard.getStep()).getData().getTable().add(imageToPlatformTable);
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep forthStep = PrivateWizardUtils.getStepById(4, wizard.getStep());
        SdkTableItem imagesTable = PrivateWizardUtils.findItem(forthStep.getData().getTable(), OpenstackConfigurationWizardConstant.IMAGE_TABLE, SdkTableItem::getName);
        List<SdkTableRowItem> selectedImages = PrivateWizardUtils.getSelectedRowForTable(imagesTable);
        if (selectedImages.size() == 0) {
            imagesTable.setServerError("At least one image should be specified");
            wizard.invalidate();
        }
    }
}
