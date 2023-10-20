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

package io.maestro3.agent.service.proccessor;

import io.maestro3.agent.admin.AdminCommandPipeline;
import io.maestro3.agent.service.AbstractWizardProcessor;
import io.maestro3.agent.service.step.IPrivateStepProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenstackPrivateActivateRegionWizardProcessor extends AbstractWizardProcessor<IPrivateStepProcessor> {

    @Autowired
    public OpenstackPrivateActivateRegionWizardProcessor(List<IPrivateStepProcessor> stepProcessors) {
        super(stepProcessors, AdminCommandPipeline.CONFIGURE_OPEN_STACK_REGION.getName());
    }
}