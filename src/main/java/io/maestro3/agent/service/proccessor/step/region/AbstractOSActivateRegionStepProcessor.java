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

import io.maestro3.agent.admin.AdminCommandPipeline;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.openstack.provider.OpenStackApiRequest;
import io.maestro3.agent.service.proccessor.OpenstackConfigurationWizardConstant;
import io.maestro3.agent.service.step.IPrivateStepProcessor;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTextItem;

import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractOSActivateRegionStepProcessor implements IPrivateStepProcessor {
    private static final int DEFAULT_TIMEOUT_SEC = 30;

    @Override
    public String getWizardType() {
        return AdminCommandPipeline.CONFIGURE_OPEN_STACK_REGION.getName();
    }

    protected IOpenStackApi buildClientNullable(OpenStackApiProvider apiProvider, SdkPrivateWizard wizard,
                                                SdkPrivateStep step, int timeout) {
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
        requestBuilder.setTimeout(timeout);
        return apiProvider.openStackNoCache(requestBuilder.build());
    }

    protected IOpenStackApi buildClientNullable(OpenStackApiProvider apiProvider, SdkPrivateWizard wizard, SdkPrivateStep step) {
        return buildClientNullable(apiProvider, wizard, step, DEFAULT_TIMEOUT_SEC);
    }
}
