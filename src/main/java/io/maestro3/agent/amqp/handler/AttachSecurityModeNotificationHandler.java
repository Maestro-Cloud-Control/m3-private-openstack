package io.maestro3.agent.amqp.handler;

import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.ServerDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttachSecurityModeNotificationHandler implements IOsNotificationHandler {

    private final IOpenStackTenantRepository tenantRepository;
    private final IOpenStackRegionRepository regionRepository;
    private final ServerDbService serverDbService;
    private final IOpenStackSecurityGroupService securityGroupService;

    @Autowired
    public AttachSecurityModeNotificationHandler(IOpenStackTenantRepository tenantRepository,
                                                 IOpenStackRegionRepository regionRepository,
                                                 ServerDbService serverDbService,
                                                 IOpenStackSecurityGroupService securityGroupService) {
        this.tenantRepository = tenantRepository;
        this.regionRepository = regionRepository;
        this.serverDbService = serverDbService;
        this.securityGroupService = securityGroupService;
    }

    @Override
    public void handle(Notification notification) {
        if (EventType.INSTANCE_CREATE_END != notification.getEventType()) {
            return;
        }

        String tenantId = (String) notification.getPayload().get("tenant_id");
        String instanceId = (String) notification.getPayload().get("instance_id");
        OpenStackTenant tenantConfig = tenantRepository.findByNativeId(tenantId);
        if (tenantConfig == null) {
            return;
        }
        OpenStackRegionConfig regionConfig = regionRepository.findByIdInCloud(tenantConfig.getRegionId());
        if (regionConfig == null) {
            return;
        }
        OpenStackServerConfig server = serverDbService.findServerByNativeId(tenantConfig.getRegionId(), tenantConfig.getId(), instanceId);
        if (server == null) {
            return;
        }
        securityGroupService.attachAdminSecurityGroup(tenantConfig, regionConfig, server);
    }
}
