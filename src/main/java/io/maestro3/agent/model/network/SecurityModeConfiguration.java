package io.maestro3.agent.model.network;

public class SecurityModeConfiguration {

    private String name;
    private String description;
    private String adminSecurityGroupId;
    private boolean defaultMode;

    public SecurityModeConfiguration(String name, String description, String adminSecurityGroupId, boolean defaultMode) {
        this.name = name;
        this.description = description;
        this.adminSecurityGroupId = adminSecurityGroupId;
        this.defaultMode = defaultMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminSecurityGroupId() {
        return adminSecurityGroupId;
    }

    public void setAdminSecurityGroupId(String adminSecurityGroupId) {
        this.adminSecurityGroupId = adminSecurityGroupId;
    }

    public boolean isDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(boolean defaultMode) {
        this.defaultMode = defaultMode;
    }
}
