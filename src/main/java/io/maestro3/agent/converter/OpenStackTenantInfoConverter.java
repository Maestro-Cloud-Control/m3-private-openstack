package io.maestro3.agent.converter;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.util.conversion.tenant.BaseTenantInfoConverter;
import io.maestro3.agent.util.conversion.tenant.TenantInfoConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OpenStackTenantInfoConverter implements TenantInfoConverter<OpenStackTenant> {

    private static final String SECURITY_MODE = "securityMode";

    @Override
    public OpenStackTenant convert(ITenant iTenant) {
        if (iTenant instanceof OpenStackTenant) {
            return (OpenStackTenant) iTenant;
        }
        throw new IllegalStateException("Malformed tenant: " + iTenant.getId());
    }

    @Override
    public Map<String, Object> getInfo(OpenStackTenant openStackTenant) {
        Map<String, Object> baseInfo = new BaseTenantInfoConverter().getTenantInfo(openStackTenant);
        baseInfo.put(SECURITY_MODE, openStackTenant.getSecurityMode());
        return baseInfo;
    }

    @Override
    public PrivateCloudType getPrivateCloudType() {
        return PrivateCloudType.OPEN_STACK;
    }
}
