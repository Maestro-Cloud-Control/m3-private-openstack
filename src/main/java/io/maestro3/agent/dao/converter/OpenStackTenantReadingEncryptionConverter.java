
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

package io.maestro3.agent.dao.converter;

import io.maestro3.agent.model.OpenStackUserInfo;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.util.CryptoUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.bson.Document;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class OpenStackTenantReadingEncryptionConverter extends AbstractMongoConverter<Document, OpenStackTenant> {
    @Override
    public OpenStackTenant convert(Document document) {
        OpenStackTenant openStackTenant = fromDocument(document, OpenStackTenant.class);
        OpenStackUserInfo adminUserCredentials = openStackTenant.getUserInfo();
        if (adminUserCredentials != null) {
            String password = adminUserCredentials.getPassword();
            if (StringUtils.isNotBlank(password)) {
                adminUserCredentials.setPassword(CryptoUtils.decrypt(password, adminUserCredentials.getNativeId()));
            }
        }
        return openStackTenant;
    }
}
