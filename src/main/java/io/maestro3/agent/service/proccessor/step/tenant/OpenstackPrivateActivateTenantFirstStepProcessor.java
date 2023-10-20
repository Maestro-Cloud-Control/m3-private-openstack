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

import com.google.common.cache.Cache;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.proccessor.OpenstackWizardItems;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkSelectItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTextItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class OpenstackPrivateActivateTenantFirstStepProcessor extends AbstractOSActivateTenantStepProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenstackPrivateActivateTenantFirstStepProcessor.class);

    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private Cache<String, Map<String, Object>> paramsCache;

    @Autowired
    public OpenstackPrivateActivateTenantFirstStepProcessor(IOpenStackRegionRepository regionRepository,
                                                            IOpenStackTenantRepository tenantRepository,
                                                            Cache<String, Map<String, Object>> paramsCache) {
        this.tenantRepository = tenantRepository;
        this.regionRepository = regionRepository;
        this.paramsCache = paramsCache;
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        List<OpenStackRegionConfig> osRegions = regionRepository.findAllRegionsForCloud();
        SdkPrivateStepData.Builder builder = SdkPrivateStepData.builder();
        if (osRegions.size() == 0) {
            builder.withMessage(PrivateWizardUtils.messageWarningItem("regionMessage", "There are no activated regions on the selected private agent", "0"));
        } else {
            String regionName = null;
            try {
                if (wizard != null) {
                    Map<String, Object> regionConfigData = paramsCache.get(wizard.getId(), HashMap::new);
                    wizard = (SdkPrivateWizard) regionConfigData.get(OpenstackConfigurationWizardConstant.WIZARD_CACHE_PARAM);
                    if (wizard != null) {
                        SdkPrivateStep secondStep = PrivateWizardUtils.getStepById(2, wizard.getStep());
                        regionName = PrivateWizardUtils.getTextValue(secondStep, OpenstackConfigurationWizardConstant.REGION_NAME_ITEM);
                    }
                }
                Map<String, Object> wizardParams = paramsCache.get(currentWizardId, HashMap::new);
                wizardParams.put(OpenstackConfigurationWizardConstant.WIZARD_CACHE_PARAM, wizard);
            } catch (ExecutionException e) {
                LOG.error("Cache error", e);
            }
            builder.withSelect(OpenstackWizardItems.buildSelectRegions(osRegions, regionName))
                .withText(PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.TENANT_NAME, "Tenant name",
                    "Tenant name", "Name for tenant on private agent", "1",
                    OpenstackConfigurationWizardConstant.REGION_TENANT_NAME_REGEX, OpenstackConfigurationWizardConstant.REGION_TENANT_NAME_REGEX_ERROR));
        }
        return SdkPrivateStep.builder()
            .withTitle("Tenant setup")
            .withDescription("#wizard.manage-private-cloud-wizard.tenant.second-step.help")
            .withId(1)
            .withData(builder.build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        SdkPrivateStep secondStep = PrivateWizardUtils.getStepById(2, wizard.getStep());
        Map<String, Object> regionConfigData = paramsCache.getIfPresent(wizard.getId());
        SdkPrivateWizard regionConfigWizard = regionConfigData != null
            ? (SdkPrivateWizard) regionConfigData.get(OpenstackConfigurationWizardConstant.WIZARD_CACHE_PARAM)
            : null;
        secondStep.getData().addText(Arrays.asList(
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.USERNAME_ITEM, "Username", "Username",
                "Username from openstack user", "2",
                OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
            PrivateWizardUtils.securedTextItem(OpenstackConfigurationWizardConstant.PASSWORD_ITEM, "Password",
                "Password", "Password from openstack user", "3",
                OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.PROJECT_ITEM, "Project name",
                "Project native name", "Openstack project name", "4",
                OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.USER_DOMAIN_ITEM, "User domain",
                "User domain", "Openstack user domain", "7",
                OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR),
            PrivateWizardUtils.textItem(OpenstackConfigurationWizardConstant.PROJECT_DOMAIN_ITEM, "Project domain id",
                "Project domain id", "Openstack project domain id", "8",
                OpenstackConfigurationWizardConstant.DEFAULT_REGEX, OpenstackConfigurationWizardConstant.DEFAULT_REGEX_ERROR))
        );
        fillWithParamsFromRegionWizard(wizard, secondStep.getData().getText(), regionConfigWizard != null
            ? PrivateWizardUtils.getStepById(1, regionConfigWizard.getStep()).getData().getText()
            : null);
    }

    private void fillWithParamsFromRegionWizard(SdkPrivateWizard wizard, List<SdkTextItem> currentItems, List<SdkTextItem> regionItems) {
        SdkPrivateStep firstStep = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkOptionItem regionSelect = PrivateWizardUtils.getSelectedOptionItem(firstStep.getData().getSelect(),
            OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM);
        List<SdkSelectItem> select = regionSelect.getSelect();
        if (select.isEmpty()){
            return;
        }
        SdkSelectItem fillParamsSelect = select.get(0);
        Boolean isSelected = fillParamsSelect.getOption().get(0).getSelected();
        if (!Boolean.TRUE.equals(isSelected)){
            return;
        }
        if (regionItems == null) {
            return;
        }
        Map<String, String> values = regionItems.stream()
            .collect(Collectors.toMap(SdkTextItem::getName, SdkTextItem::getValue));
        currentItems.forEach(item -> item.setValue(values.get(item.getName())));
    }

    @Override
    public void validate(SdkPrivateWizard wizard) {
        SdkPrivateStep stepById = PrivateWizardUtils.getStepById(1, wizard.getStep());
        SdkTextItem tenantNameItem = PrivateWizardUtils.findItemNullable(stepById.getData().getText(),
            OpenstackConfigurationWizardConstant.TENANT_NAME, SdkTextItem::getName);
        if (tenantNameItem == null) {
            wizard.invalidate();
            return;
        }
        SdkOptionItem regionOption = PrivateWizardUtils.getSelectedOptionItem(stepById.getData().getSelect(), OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM);
        String regionName = regionOption.getValue();
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(regionName);
        ITenant tenant = tenantRepository.findByTenantAliasAndRegionId(tenantNameItem.getValue(), region.getId());
        if (tenant != null) {
            tenantNameItem.setServerError("Tenant with specified name already exist");
            wizard.invalidate();
        }
    }
}
