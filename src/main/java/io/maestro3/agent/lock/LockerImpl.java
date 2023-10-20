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

package io.maestro3.agent.lock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.maestro3.agent.model.lock.ValuableOperation;
import io.maestro3.agent.model.lock.VoidOperation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LockerImpl implements Locker {

    private static final Logger LOG = LoggerFactory.getLogger(LockerImpl.class);

    private final String type;
    private final LoadingCache<LockKey, Lock> lockCache;

    public LockerImpl(String type) {
        Assert.hasLength(type, "Empty type of Locker specified.");
        this.type = type;

        lockCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(lockLoader());
    }

    @Override
    public <E extends Exception> void executeOperation(String projectId, VoidOperation<E> operation) throws E {
        execute(projectId, ValuableOperation.wrapVoid(operation));
    }

    @Override
    public <RES, E extends Exception> RES executeValuableOperation(String projectId,
                                                                   ValuableOperation<RES, E> operation) throws E {
        return execute(projectId, operation);
    }

    private <T, E extends Exception> T execute(String projectId,
                                               ValuableOperation<T, E> operation) throws E {
        String lockType = type + "-" + projectId;

        boolean grabLock = false;
        Lock lock = retrieveLockFromCacheInternal(lockType);
        if (Objects.isNull(lock)) return operation.execute(); // error while load new Lock, execute operation anyway

        for (int i = 0; i < LOCK_ATTEMPTS_COUNT; i++) {
            try {
                grabLock = lock.tryLock(WAIT_FOR_LOCK_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                // ignore
                Thread.currentThread().interrupt();
            }

            if (grabLock) {
                break;
            }
        }

        if (!grabLock) {
            LOG.warn("Could not lock {} with {} attempts. Will execute operation anyway.", lockType, LOCK_ATTEMPTS_COUNT);
        }

        try {
            return operation.execute();
        } finally {
            if (grabLock) {
                lock.unlock();
            }
        }
    }

    private Lock retrieveLockFromCacheInternal(String lockType) {
        try {
            return lockCache.get(LockKey.buildKey(lockType));
        } catch (ExecutionException e) {
            LOG.error("Attention(!), cache value loading for key " + lockType
                    + " failed with reason: " + e.getMessage(), e);
            return null;
        }
    }

    private CacheLoader<LockKey, Lock> lockLoader() {
        return new CacheLoader<LockKey, Lock>() {
            @Override
            public Lock load(LockKey key) {
                return new ReentrantLock();
            }
        };
    }

    static class LockKey {

        private final String id;

        private LockKey(String id) {
            this.id = id;
        }

        static LockKey buildKey(String id) {
            return new LockKey(id);
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            LockKey lockKey = (LockKey) o;

            return new EqualsBuilder()
                    .append(id, lockKey.id)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .toHashCode();
        }
    }
}
