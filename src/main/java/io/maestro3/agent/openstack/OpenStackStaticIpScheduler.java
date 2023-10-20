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

import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.scheduler.AbstractScheduler;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class OpenStackStaticIpScheduler extends AbstractScheduler {

    private final IServiceFactory<IOpenStackNetworkingProvider> networkingServiceFactory;
    private final IOpenStackRegionRepository regionRepository;

    @Value("${flag.enable.images.static.ip.describer:true}")
    private boolean enabledImagesScheduledDescriber;

    @Autowired
    public OpenStackStaticIpScheduler(IServiceFactory<IOpenStackNetworkingProvider> networkingServiceFactory,
                                      IOpenStackRegionRepository regionRepository) {
        super(PrivateCloudType.OPEN_STACK, true);
        this.networkingServiceFactory = networkingServiceFactory;
        this.regionRepository = regionRepository;
    }

    @Override
    public String getScheduleTitle() {
        return "OpenStack static updating";
    }

    @Scheduled(cron = "${cron.update.openstack.images: 0 0/15 * * * ?}")

    public void execute() {
        start("Updating OS static ips");
        if (enabledImagesScheduledDescriber) {
            List<OpenStackRegionConfig> regions = regionRepository.findAllOSRegionsAvailableForDescribers();
            for (OpenStackRegionConfig region : regions) {
                LOG.info("Processing static ips for {} region", region.getRegionAlias());
                IOpenStackNetworkingProvider networkingProvider = networkingServiceFactory.get(region);
                networkingProvider.networkingService().updateStaticIps();
            }
        }
        end("Updating OS static ips finished");
    }
}
