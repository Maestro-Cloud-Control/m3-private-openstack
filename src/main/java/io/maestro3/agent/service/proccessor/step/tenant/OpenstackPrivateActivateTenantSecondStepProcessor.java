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

import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.identity.bean.TokenMeta;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class OpenstackPrivateActivateTenantSecondStepProcessor extends AbstractOSActivateTenantStepProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenstackPrivateActivateTenantSecondStepProcessor.class);

    private IOpenStackRegionRepository regionRepository;
    private OpenStackApiProvider apiProvider;

    @Autowired
    public OpenstackPrivateActivateTenantSecondStepProcessor(IOpenStackRegionRepository regionRepository,
                                                             OpenStackApiProvider apiProvider) {
        this.regionRepository = regionRepository;
        this.apiProvider = apiProvider;
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {

        return SdkPrivateStep.builder()
            .withTitle("Tenant credentials setup")
            .withDescription("#wizard.manage-private-cloud-wizard.tenant.third-step.help")
            .withId(2)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkPrivateStep secondStep = PrivateWizardUtils.getStepById(2, wizard.getStep());
        SdkOptionItem regionOption = PrivateWizardUtils.getSelectedOptionItem(firstStep.getData().getSelect(), OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM);
        String regionName = regionOption.getValue();
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(regionName);
        IOpenStackApi client = buildClientNullable(apiProvider, wizard, secondStep, region);
        SdkPrivateStep thirdStep = PrivateWizardUtils.getStepById(3, wizard.getStep());
        try {
            List<Network> networks = client.networking().networks().list();
            List<SecurityGroup> securityGroups = client.networking().securityGroups().list();
            thirdStep.getData()
                .setSelect(Arrays.asList(
                    OpenstackWizardItems.buildNetworkSelect(networks),
                    OpenstackWizardItems.buildSecurityGroupSelect(securityGroups),
                    OpenstackWizardItems.buildDescriberModeSelect(),
                    OpenstackWizardItems.buildEnableManagementSelectItem()
                ));
        } catch (OSClientException e) {
            thirdStep.getData().getMessage().add(PrivateWizardUtils.messageWarningItem("errorAuth", "Failed to describe network configuration request with specified parameters.", "0"));
            wizard.invalidate();
        }
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep secondStep = PrivateWizardUtils.getStepById(2, wizard.getStep());
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkOptionItem regionOption = PrivateWizardUtils.getSelectedOptionItem(firstStep.getData().getSelect(), OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM);
        String regionName = regionOption.getValue();
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(regionName);
        IOpenStackApi client = buildClientNullable(apiProvider, wizard, secondStep, region);
        if (!wizard.isValid() || client == null) {
            wizard.invalidate();
            return;
        }
        boolean success;
        try {
            TokenMeta currentUserInfo = client.keystone().users().getCurrentUserInfo();
            secondStep.getData().addText(Arrays.asList(
                PrivateWizardUtils.hiddenTextItem(OpenstackConfigurationWizardConstant.PROJECT_ID_ITEM, currentUserInfo.getProjectId(), "10"),
                PrivateWizardUtils.hiddenTextItem(OpenstackConfigurationWizardConstant.USER_ID, currentUserInfo.getUserId(), "11")
            ));
            success = true;
        } catch (OSClientException e) {
            LOG.error("Failed to list flavors with specified params", e);
            success = false;
        }
        if (!success) {
            secondStep.getData().getMessage().add(PrivateWizardUtils.messageWarningItem("errorAuth", "Failed to execute auth request with specified parameters.", "0"));
            wizard.invalidate();
        }
    }
}
