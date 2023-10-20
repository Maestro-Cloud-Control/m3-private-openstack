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

package io.maestro3.agent.service.proccessor.step.tenant;

import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import org.springframework.stereotype.Service;

@Service
public class OpenstackPrivateActivateTenantThirdStepProcessor extends AbstractOSActivateTenantStepProcessor {

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {

        return SdkPrivateStep.builder()
            .withTitle("Tenant networking setup")
            .withDescription("#wizard.manage-private-cloud-wizard.tenant.forth-step.help")
            .withId(3)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        String username = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(2, wizard.getStep()), OpenstackConfigurationWizardConstant.USERNAME_ITEM);
        String projectName = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(2, wizard.getStep()), OpenstackConfigurationWizardConstant.PROJECT_ITEM);
        String network = PrivateWizardUtils.getSelectedOptionItem(
            PrivateWizardUtils.getStepById(3, wizard.getStep()).getData().getSelect(),
            OpenstackConfigurationWizardConstant.NETWORK_SELECT_ITEM).getValue();
        String secGroup = PrivateWizardUtils.getSelectedOptionItem(
            PrivateWizardUtils.getStepById(3, wizard.getStep()).getData().getSelect(),
            OpenstackConfigurationWizardConstant.SECURITY_GROUP_SELECT_ITEM).getValue();
        String region = PrivateWizardUtils.getSelectedOptionItem(
            PrivateWizardUtils.getStepById(1, wizard.getStep()).getData().getSelect(),
            OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM).getValue();
        String tenantName = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(1, wizard.getStep()), OpenstackConfigurationWizardConstant.TENANT_NAME);
        wizard.getMeta().put(SdkPrivateWizard.USERNAME, username);
        wizard.getMeta().put(SdkPrivateWizard.PROJECT_NAME, projectName);
        wizard.getMeta().put(SdkPrivateWizard.SEC_GROUP, secGroup);
        wizard.getMeta().put(SdkPrivateWizard.NETWORK, network);
        wizard.getMeta().put(SdkPrivateWizard.TENANT_NAME, tenantName);
        wizard.getMeta().put(SdkPrivateWizard.REGION_NAME, region);
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
    }
}
