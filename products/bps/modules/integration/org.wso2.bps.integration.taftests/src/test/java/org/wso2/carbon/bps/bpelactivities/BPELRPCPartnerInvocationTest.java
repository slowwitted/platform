/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.bps.bpelactivities;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bps.BPSMasterTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BPELRPCPartnerInvocationTest extends BPSMasterTest{
    private static final Log log = LogFactory.getLog(BPELRPCPartnerInvocationTest.class);

    BpelPackageManagementClient bpelManager;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws Exception {
        uploadBpelForTest("RPCServiceProcess");
        uploadBpelForTest("RPCClientProcess");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "RPCClientProcessService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "invoke RPC client process")
    private void invokeRPCClientProcess()
            throws Exception {
        String payload = "<rpc:RPCClientProcessRequest xmlns:rpc=\"http://wso2.org/bps/rpcclient\">" +
                "<rpc:input>Hello</rpc:input></rpc:RPCClientProcessRequest>";
        String operation = "hello";
        String serviceName = "RPCClientProcessService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.sendRequest(serviceUrl + File.separator + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("RPCServiceProcess");
        bpelManager.undeployBPEL("RPCClientProcess");
        adminServiceAuthentication.logOut();
    }
}
