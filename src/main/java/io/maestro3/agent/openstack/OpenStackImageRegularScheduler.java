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

package io.maestro3.agent.openstack;

import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.scheduler.AbstractScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class OpenStackImageRegularScheduler extends AbstractScheduler {

    private OpenStackImagesUpdater openStackImagesUpdater;

    @Value("${flag.enable.images.schedule.describer}")
    private boolean enabledImagesScheduledDescriber;

    @Autowired
    public OpenStackImageRegularScheduler(OpenStackImagesUpdater openStackImagesUpdater) {
        super(PrivateCloudType.OPEN_STACK, true);
        this.openStackImagesUpdater = openStackImagesUpdater;
    }

    @Override
    public String getScheduleTitle() {
        return "OpenStack images updating";
    }

    @Scheduled(cron = "${cron.update.openstack.images: 0 0/2 * * * ?}")
    public void executeSchedule() {
        super.executeSchedule();
    }

    public void execute() {
        start("Updating OS images");
        if (enabledImagesScheduledDescriber) {
            openStackImagesUpdater.updateImages(false);
        }
        end("Updating OS images finished");
    }
}
