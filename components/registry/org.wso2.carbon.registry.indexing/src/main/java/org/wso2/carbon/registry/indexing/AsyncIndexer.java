/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.indexing;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The run() method of this class takes files from a blocking queue and indexes them.
 * An instance of this class should be executed with a ScheduledExecutorService so that run() method
 * runs periodically.
 */
public class AsyncIndexer implements Runnable {

    private static Log log = LogFactory.getLog(AsyncIndexer.class);
    private final SolrClient client;
    private LinkedBlockingQueue<File2Index> queue = new LinkedBlockingQueue<File2Index>();
    private boolean canAcceptFiles = true;
    int poolSize = 50;

    @SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
    public static class File2Index {
        public byte[] data;
        public String mediaType;
        public String path;
        public String lcName;
        public String lcState;

        public int tenantId;

        public File2Index(byte[] data, String mediaType, String path, int tenantId) {
            this.data = data;
            this.mediaType = mediaType;
            this.path = path;
            this.tenantId = tenantId;
        }

        public File2Index(byte[] data, String mediaType, String path, int tenantId, String lcName, String lcState) {
            this.data = data;
            this.mediaType = mediaType;
            this.path = path;
            this.tenantId = tenantId;
            this.lcName = lcName;
            this.lcState = lcState;
        }
    }

    public void addFile(File2Index file2Index) {
        if (canAcceptFiles) {
            queue.offer(file2Index);
        } else {
            log.warn("Can't accept resource for indexing. Shutdown in progress: path=" +
                    file2Index.path);
        }
    }

    protected AsyncIndexer() throws RegistryException {
        try {
            client = SolrClient.getInstance();
            Utils.setWaitBeforeShutdownObserver(new WaitBeforeShutdownObserver() {
                public void startingShutdown() {
                    canAcceptFiles = false;
                    do {
                        indexFile();
                    } while (queue.size() != 0);
                }

                public boolean isTaskComplete() {
                    // if the queue is not empty task is not complete.
                    return !(queue.size() > 0);
                }
            });
        } catch (IndexerException e) {
            throw new RegistryException("Error initializing Async Indexer " + e.getMessage(), e);
        }
    }

    public SolrClient getClient() {
        return client;
    }

    /**
     * This method retrieves resources submitted for indexing from a blocking queue and indexed them.
     * This handles interrupts properly so that it is compatible with the Executor framework.
     */
    public void run() {
      indexFile();
    }

    private boolean indexFile() {
        try {
            if(!canAcceptFiles){
                return false;
            }

            long batchSize = IndexingManager.getInstance().getBatchSize();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            long i =0;
            List<IndexingTask> taskList = new ArrayList<IndexingTask>();
            while (queue.size() > 0 && i <= batchSize) {
                ++i;
                IndexingTask indexingTask = new IndexingTask(MultitenantConstants.SUPER_TENANT_NAME,
                        MultitenantConstants.SUPER_TENANT_ID, queue.take());
                taskList.add(indexingTask);

            }
            if (taskList.size() > 0) {
                uploadFiles(taskList);
            }else {
                return true;
            }

        } catch (Throwable e) { // Throwable is caught to prevent the executor termination
            if (e instanceof InterruptedException) {
                return false; // to be compatible with executor framework. No need of logging anything
            } else {
                log.error("Error while indexing.", e);
            }
        }   finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

    protected void uploadFiles(List<IndexingTask> tasks) throws RegistryException {

        poolSize = IndexingManager.getInstance().getIndexerPoolSize();
        if (poolSize <= 0) {
            for (IndexingTask task : tasks) {
                task.run();
            }
        } else {
            ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
            try {
                for (IndexingTask task : tasks) {
                    executorService.submit(task);
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to submit indexing task ", e);
                }
            } finally {
                executorService.shutdown();
            }
        }
    }

    protected static class IndexingTask implements Runnable {
        private String userId;
        private int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        private File2Index fileData;

        protected IndexingTask(String userId, int tenantId, File2Index fileData) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.fileData = fileData;
        }

        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        setTenantDomain(MultitenantUtils.getTenantDomain(userId));
                doWork(fileData);

            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        private boolean doWork(File2Index file2Index) {
            AsyncIndexer asyncIndexer;
            try {
                Indexer indexer = IndexingManager.getInstance().getIndexerForMediaType(
                        file2Index.mediaType);
                try {
                    asyncIndexer = new AsyncIndexer();
                    asyncIndexer.getClient().indexDocument(file2Index, indexer);
                } catch (Exception e) {
                    log.warn("Could not index the resource: path=" + file2Index.path +
                            ", media type=" + file2Index.mediaType); // to ease debugging
                }

            } catch (Throwable e) { // Throwable is caught to prevent the executor termination
                log.error("Error while indexing.", e);
            }
            return true;
        }
    }
}
