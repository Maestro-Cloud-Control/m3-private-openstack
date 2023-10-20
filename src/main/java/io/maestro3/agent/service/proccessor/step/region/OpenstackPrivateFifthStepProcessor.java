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

import com.google.common.cache.Cache;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class OpenstackPrivateFifthStepProcessor extends AbstractOSActivateRegionStepProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(OpenstackPrivateFifthStepProcessor.class);

    private Cache<String, Map<String, Object>> paramsCache;

    @Autowired
    public OpenstackPrivateFifthStepProcessor(Cache<String, Map<String, Object>> paramsCache) {
        this.paramsCache = paramsCache;
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public SdkPrivateStep buildFirstStepData(String currentWizardId, SdkPrivateWizard wizard) {
        return SdkPrivateStep.builder()
            .withTitle("Region images configuration")
            .withDescription("#wizard.manage-private-cloud-wizard.sixth-step.help")
            .withId(5)
            .withData(SdkPrivateStepData.builder().build())
            .build();
    }

    @Override
    public void prepareNextStep(SdkPrivateWizard wizard) {
        try {
            Map<String, Object> stringObjectMap = paramsCache.get(wizard.getId(), HashMap::new);
            stringObjectMap.put(OpenstackConfigurationWizardConstant.WIZARD_CACHE_PARAM, wizard);
        } catch (ExecutionException e) {
            LOG.error("Cache error", e);
        }
        String auth = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(1, wizard.getStep()), OpenstackConfigurationWizardConstant.AUTH_ITEM);
        String username = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(1, wizard.getStep()), OpenstackConfigurationWizardConstant.USERNAME_ITEM);
        String regionNativeName = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(1, wizard.getStep()), OpenstackConfigurationWizardConstant.REGION_NATIVE_NAME_ITEM);
        String maestroName = PrivateWizardUtils.getTextValue(PrivateWizardUtils.getStepById(2, wizard.getStep()), OpenstackConfigurationWizardConstant.REGION_NAME_ITEM);
        wizard.getMeta().put(SdkPrivateWizard.REGION_NAME, maestroName);
        wizard.getMeta().put(SdkPrivateWizard.AUTH_URL, auth);
        wizard.getMeta().put(SdkPrivateWizard.REGION_NATIVE_NAME, regionNativeName);
        wizard.getMeta().put(SdkPrivateWizard.USERNAME, username);
    }


    @Override
    public void validate(SdkPrivateWizard wizard) {
        //do nothing
    }
}
