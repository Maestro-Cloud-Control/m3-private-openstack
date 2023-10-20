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

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.IProjectService;
import io.maestro3.agent.openstack.api.identity.bean.KeystoneProject;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.identity.ProjectModel;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class ProjectService extends BasicService implements IProjectService {

    public ProjectService(IOSClient client) {
        super(ServiceType.IDENTITY, client);
    }

    @Override
    public ProjectModel create(ProjectModel project) throws OSClientException {
        Assert.notNull(project, "project can not be null");
        Assert.hasText(project.getName(), "You should provide name of a project.");
        BasicOSRequest<ProjectWrapper> create = builder(ProjectWrapper.class, endpoint())
                .path("/projects")
                .post(new ProjectRequestWrapper(project))
                .create();
        ProjectWrapper entity = client.execute(create).getEntity();
        return entity == null ? null : entity.project;
    }

    @Override
    public void delete(String projectId) throws OSClientException {
        IOSRequest<Void> deleteTenant = BasicOSRequest.builder(Void.class, endpoint())
                .path("/projects/%s", projectId)
                .delete()
                .create();
        client.execute(deleteTenant);
    }

    @Override
    public List<ProjectModel> list() throws OSClientException {
        IOSRequest<ProjectsWrapper> listProjects = BasicOSRequest.builder(ProjectsWrapper.class, endpoint())
                .path("/projects")
                .create();
        ProjectsWrapper projectsWrapper = client.execute(listProjects).getEntity();
        return projectsWrapper != null ? projectsWrapper.getProjects() : null;
    }

    @Override
    public List<ProjectModel> listByName(String name) throws OSClientException {
        IOSRequest<ProjectsWrapper> listProjects = BasicOSRequest.builder(ProjectsWrapper.class, endpoint())
                .path("/projects?name=%s", name)
                .create();
        ProjectsWrapper projectsWrapper = client.execute(listProjects).getEntity();
        return projectsWrapper != null ? projectsWrapper.getProjects() : null;
    }

    private static class ProjectRequestWrapper {
        private ProjectModel project;

        private ProjectRequestWrapper(ProjectModel project) {
            this.project = project;
        }
    }

    private static class ProjectWrapper {
        private KeystoneProject project;
    }

    private static class ProjectsWrapper {
        private List<KeystoneProject> projects;

        private List<ProjectModel> getProjects() {
            List<ProjectModel> osProjects = new ArrayList<>();
            if (projects == null) {
                return osProjects;
            }
            for (KeystoneProject project : projects) {
                osProjects.add(project);
            }
            return osProjects;
        }
    }
}
