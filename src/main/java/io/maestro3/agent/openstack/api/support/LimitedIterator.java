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

package io.maestro3.agent.openstack.api.support;

import io.maestro3.agent.model.OpenStackResource;
import io.maestro3.agent.openstack.exception.OSClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public abstract class LimitedIterator<T extends OpenStackResource> implements Iterator<T> {

    private static final Logger LOG = LoggerFactory.getLogger(LimitedIterator.class);

    private final int limit;

    private List<T> resources;
    private String marker;
    private int cursor;

    public LimitedIterator(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean hasNext() {
        if (marker == null || resources.size() == cursor) {
            try {
                cursor = 0;
                resources = retrieveResourcesPage(buildPath());
                boolean notEmptyServers = CollectionUtils.isNotEmpty(resources);
                if (notEmptyServers) {
                    marker = resources.get(resources.size() - 1).getId();
                }
                return notEmptyServers;
            } catch (OSClientException e) {
                LOG.error("Failed to list servers. Reason: {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    private String buildPath() {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(getBasePath()).append("?limit=").append(limit);
        if (marker != null) {
            pathBuilder.append("&marker=").append(marker);
        }
        return pathBuilder.toString();
    }

    protected abstract List<T> retrieveResourcesPage(String path) throws OSClientException;

    protected abstract String getBasePath();

    @Override
    public T next() throws NoSuchElementException {
        int nextIndex = cursor++;
        if (resources == null || nextIndex >= resources.size()) {
            throw new NoSuchElementException(String.format("Element[%s] is not found. ", nextIndex));
        }
        return resources.get(nextIndex);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removing is not supported by this iterator.");
    }
}
