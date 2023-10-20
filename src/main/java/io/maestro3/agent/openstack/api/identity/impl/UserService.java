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

package io.maestro3.agent.openstack.api.identity.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.identity.User;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.IUserService;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneUser;
import io.maestro3.agent.openstack.api.identity.bean.TokenMeta;
import io.maestro3.agent.openstack.client.IClientMetadata;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.helper.extractors.AdminUrlEndpointExtractor;
import io.maestro3.agent.openstack.helper.extractors.EndpointExtractor;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;
import org.springframework.util.Assert;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class UserService extends BasicService implements IUserService {

    private static final EndpointExtractor ADMIN_EXTRACTOR = AdminUrlEndpointExtractor.ifVersionIs("v2.0");

    UserService(IOSClient client) {
        super(ServiceType.IDENTITY, client);
    }

    @Override
    public User create(User user) throws OSClientException {
        Assert.notNull(user, "user can not be null");
        Assert.isNull(user.getId(), "user id should be null");
        BasicOSRequest<UserResponseWrapper> create = builder(UserResponseWrapper.class, endpoint())
                .path("/users")
                .post(new UserRequestWrapper(user))
                .create();
        UserResponseWrapper entity = client.execute(create).getEntity();
        return extractUser(entity);
    }

    @Override
    public List<User> list() throws OSClientException {
        URL endpoint = endpoint(ADMIN_EXTRACTOR);
        BasicOSRequest<Users> list = builder(Users.class, endpoint)
                .path("/users")
                .create();

        IOSResponse<Users> usersResponse = client.execute(list);
        Users users = (usersResponse != null) ? usersResponse.getEntity() : null;
        return toUsersList(users);
    }

    @Override
    public TokenMeta getCurrentUserInfo() throws OSClientException {
        IClientMetadata metadata = client.getMetadata();
        return new TokenMeta(metadata.getUserId(), metadata.getProjectId());
    }

    @Override
    public void delete(String userId) throws OSClientException {
        Assert.hasText(userId, "shape can not be null");
        IOSRequest<Void> deleteUser = BasicOSRequest.builder(Void.class, endpoint())
                .path("/users/%s", userId)
                .delete()
                .create();
        client.execute(deleteUser);
    }

    @Override
    public User getUserByName(String name) throws OSClientException {
        Assert.hasText(name, "name can not be blank");

        Map<String, List<String>> filter = new HashMap<>();
        filter.put("name", Collections.singletonList(name));

        URL endpoint = endpoint(ADMIN_EXTRACTOR);
        BasicOSRequest.BasicOSRequestBuilder<UserResponseWrapper> list = BasicOSRequest
                .builder(UserResponseWrapper.class, endpoint)
                .path(pathWithFilter("/users", filter));
        UserResponseWrapper result = client.execute(list.create()).getEntity();
        return extractUser(result);
    }

    private User extractUser(UserResponseWrapper result) {
        if (result == null) {
            return null;
        }
        if (result.user != null) {
            return result.user;
        }
        if (result.users != null && result.users.size() > 0) {
            return result.users.get(0);
        }
        return null;
    }

    private List<User> toUsersList(Users users) {
        if (users == null) {
            return Collections.emptyList();
        }
        if (users.keystoneUsers == null) {
            return Collections.emptyList();
        }

        List<User> usersList = new ArrayList<>();
        for (KeystoneUser keystoneUser : users.keystoneUsers) {
            usersList.add(keystoneUser);
        }
        return usersList;
    }

    private static class UserRequestWrapper {
        private User user;

        private UserRequestWrapper(User user) {
            this.user = user;
        }
    }

    private static class UserResponseWrapper {
        private ArrayList<KeystoneUser> users;
        private KeystoneUser user;
    }

    private static class Users {
        @SerializedName("users")
        private List<KeystoneUser> keystoneUsers;
    }
}
