/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.autoscale.lbautoscale.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscalerTaskDSHolder;
import org.wso2.carbon.stratos.cloud.controller.stub.CloudControllerServiceStub;

/**
 * This is the client class this calls Autoscaler service.
 */
public class CloudControllerStubClient extends CloudControllerClient {

    private CloudControllerServiceStub stub;
    private static final String CLOUD_CONTROLLER_EPR = AutoscalerTaskDSHolder.getInstance().getLoadBalancerConfig().getAutoscalerServiceEpr();

    private static final Log log = LogFactory.getLog(CloudControllerStubClient.class);

    // public CloudControllerStubClient(String epr) throws AxisFault {
    //
    // try {
    //
    // stub = new CloudControllerServiceStub(epr);
    // stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(90000);
    //
    // } catch (AxisFault axisFault) {
    // String msg =
    // "Failed to initiate AutoscalerService client. " + axisFault.getMessage();
    // log.error(msg, axisFault);
    // throw new AxisFault(msg, axisFault);
    // }
    // }

    // public boolean init(boolean isSpi) throws Exception {
    //
    // return stub.initAutoscaler(isSpi);
    // }

    public String startInstance(String domainName, String subDomainName) throws Exception {

        return stub.startInstance(domainName, subDomainName);
    }

    public boolean terminateInstance(String domainName, String subDomainName) throws Exception {

        return stub.terminateInstance(domainName, subDomainName);
    }

    public boolean
        terminateLastlySpawnedInstance(String domainName, String subDomainName) throws Exception {

        return stub.terminateLastlySpawnedInstance(domainName, subDomainName);
    }

    public int getPendingInstanceCount(String domainName, String subDomainName) throws Exception {

        return stub.getPendingInstanceCount(domainName, subDomainName);
    }

    @Override
    public void init() {

        try {

            stub = new CloudControllerServiceStub(CLOUD_CONTROLLER_EPR);
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(300000);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate AutoscalerService client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RuntimeException(msg, axisFault);
        }
    }

}
