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

import io.maestro3.agent.openstack.provider.OpenStackApiRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class OpenstackConfigurationWizardConstant {
    public static final Map<String, BiFunction<OpenStackApiRequest.OpenStackApiRequestBuilder, String, OpenStackApiRequest.OpenStackApiRequestBuilder>> REGION_REQUEST_FILLERS = new LinkedHashMap<>();
    public static final Map<String, BiFunction<OpenStackApiRequest.OpenStackApiRequestBuilder, String, OpenStackApiRequest.OpenStackApiRequestBuilder>> TENANT_REQUEST_FILLERS = new LinkedHashMap<>();
    public static final String AUTH_ITEM = "authText";
    public static final String USERNAME_ITEM = "usernameText";
    public static final String USER_ID = "userIdItem";
    public static final String PASSWORD_ITEM = "passwordText";
    public static final String PROJECT_ITEM = "projectText";
    public static final String PROJECT_ID_ITEM = "projectIdText";
    public static final String REGION_NATIVE_NAME_ITEM = "regionNativeNameText";
    public static final String USER_DOMAIN_ITEM = "userDomainText";
    public static final String PROJECT_DOMAIN_ITEM = "projectDomainText";
    public static final String OS_VERSION_ITEM = "osVersionSelect";
    public static final String ENABLE_SCHEDULE_ITEM = "enableSchedule";
    public static final String ENABLE_MANAGEMENT_ITEM = "enableManagement";
    public static final String FILLED_PARAMS_ITEM = "filledParameters";
    public static final String REGION_NAME_ITEM = "regionName";
    public static final String SERVER_NAME_PREFIX_ITEM = "serverPrefix";
    public static final String TENANT_NAME = "tenantNameText";
    public static final String REGION_SELECT_ITEM = "regionSelect";
    public static final String NETWORK_SELECT_ITEM = "networkSelect";
    public static final String SECURITY_GROUP_SELECT_ITEM = "securityGroupSelect";
    public static final String DESCRIBER_SELECT_ITEM = "describerSelect";
    public static final String ALL_MODE = "ALL";
    public static final String MAESTRO_MODE = "MAESTRO";

    public static final String IMAGE_TO_PLATFORM_TABLE = "imageToPlatformTable";
    public static final String FLAVOR_TABLE = "flavorsTable";
    public static final String IMAGE_TABLE = "imageTable";

    public static final String URL_REGEX = "^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)$";
    public static final String DEFAULT_REGEX = "^\\S{4,36}$";
    public static final String DEFAULT_REGEX_ERROR = "Value should be between 4 and 36 symbols and should not contain whitespaces";

    public static final String REGION_TENANT_NAME_REGEX = "^[\\.\\-_A-Z0-9]{6,25}$";
    public static final String REGION_TENANT_NAME_REGEX_ERROR = "Name may contain alphabetic symbols in upper case, " +
        "numbers, special symbols (.-_) and should be between 6 and 25 symbols";
    public static final String PREFIX_REGEX = "^[a-z]{1,4}$";
    public static final String PREFIX_REGEX_ERROR = "Server prefix may contain only alphabetic symbols in lower case and should be between 1 and 4 symbols";

    public static final String WIZARD_CACHE_PARAM = "wizard";

    static {
        REGION_REQUEST_FILLERS.put(AUTH_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setAuthUrl);
        REGION_REQUEST_FILLERS.put(USERNAME_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setUser);
        REGION_REQUEST_FILLERS.put(PASSWORD_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setPassword);
        REGION_REQUEST_FILLERS.put(PROJECT_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setTenant);
        REGION_REQUEST_FILLERS.put(REGION_NATIVE_NAME_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setRegionName);
        REGION_REQUEST_FILLERS.put(USER_DOMAIN_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setUserDomainName);
        REGION_REQUEST_FILLERS.put(PROJECT_DOMAIN_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setTenantDomainName);

        TENANT_REQUEST_FILLERS.put(USERNAME_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setUser);
        TENANT_REQUEST_FILLERS.put(PASSWORD_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setPassword);
        TENANT_REQUEST_FILLERS.put(PROJECT_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setTenant);
        TENANT_REQUEST_FILLERS.put(USER_DOMAIN_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setUserDomainName);
        TENANT_REQUEST_FILLERS.put(PROJECT_DOMAIN_ITEM, OpenStackApiRequest.OpenStackApiRequestBuilder::setTenantDomainName);
    }

    private OpenstackConfigurationWizardConstant() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }
}
