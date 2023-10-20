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

import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenstackPrivateFirstStepProcessor extends AbstractOSActivateRegionStepProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenstackPrivateFirstStepProcessor.class);

    private OpenStackApiProvider apiProvider;

    @Autowired
    public OpenstackPrivateFirstStepProcessor(OpenStackApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        SdkPrivateStepData data = SdkPrivateStepData.builder()
            .withText(Arrays.asList(
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.AUTH_ITEM, "Auth URL", "Keystone auth URL",
                    "Url for authorization endpoint", "1", OpenstackConfigurationWizardConstant.URL_REGEX,
                    "Parameter should be correct url"),
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.USERNAME_ITEM, "Username", "Username",
                    "Username from OpenStack user", "2",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
                PrivateWizardUtils.securedTextItem(OpenstackConfigurationWizardConstant.PASSWORD_ITEM, "Password",
                    "Password", "Password from OpenStack user", "3",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.PROJECT_ITEM, "Project name",
                    "Project native name", "OpenStack project name", "4",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.USER_DOMAIN_ITEM, "User domain",
                    "User domain", "OpenStack user domain", "5",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.REGION_NATIVE_NAME_ITEM, "Region native name",
                    "Region native name", "OpenStack region native name", "6",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
                PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.PROJECT_DOMAIN_ITEM, "Project domain ID",
                    "Project domain id", "OpenStack project domain ID", "7",
                    OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR))
            )
            .withSelect(Collections.singletonList(
                PrivateWizardUtils.selectItem(OpenstackConfigurationWizardConstant.OS_VERSION_ITEM, "OpenStack Version", "OpenStack version", "8",
                    Arrays.stream(OpenStackVersion.values())
                        .filter(OpenStackVersion::isSelectable)
                        .sorted(Comparator.comparing(OpenStackVersion::name))
                        .map(v -> PrivateWizardUtils.getOptionItem(v.name(), false))
                        .collect(Collectors.toList()))
            ))
            .build();
        return SdkPrivateStep.builder()
            .withTitle("Credentials setup")
            .withDescription("#wizard.manage-private-cloud-wizard.second-step.help")
            .withId(1)
            .withData(data)
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        List<SdkPrivateStep> steps = wizard.getStep();
        SdkPrivateStep step = PrivateWizardUtils.getStepById(2, steps);
        SdkPrivateStepData stepData = step.getData();
        stepData.setText(Arrays.asList(
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.REGION_NAME_ITEM, "Maestro region name",
                "Maestro region name", "Name that will be used for this project in maestro", "0",
                OpenstackConfigurationWizardConstant.REGION_TENANT_NAME_REGEX, OpenstackConfigurationWizardConstant.REGION_TENANT_NAME_REGEX_ERROR),
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.SERVER_NAME_PREFIX_ITEM, "Server prefix",
                "Server prefix", "Prefix for virtual vm that will be run in this region", "1",
                OpenstackConfigurationWizardConstant.PREFIX_REGEX, OpenstackConfigurationWizardConstant.PREFIX_REGEX_ERROR)));
        stepData.getSelect().add(OpenstackWizardItems.buildEnableManagementSelectItem());
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        int clientTimeout = 15;
        IOpenStackApi client = buildClientNullable(apiProvider, wizard, firstStep, clientTimeout);
        if (!wizard.isValid() || client == null) {
            wizard.invalidate();
            return;
        }
        boolean success;
        try {
            client.compute().flavors().list();
            success = true;
        } catch (OSClientException e) {
            LOG.error("Failed to list flavors with specified params", e);
            success = false;
        }
        if (!success) {
            firstStep.getData().getMessage().add(PrivateWizardUtils.messageWarningItem("errorAuth",
                "Failed to execute auth request with the specified parameters. Please review them and try submitting them again.", "0"));
            wizard.invalidate();
        }
    }
}
