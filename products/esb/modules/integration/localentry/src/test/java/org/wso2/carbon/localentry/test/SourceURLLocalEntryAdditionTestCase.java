/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.localentry.test;

import org.testng.annotations.Test;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminServiceStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SourceURLLocalEntryAdditionTestCase extends ESBIntegrationTestCase{

    private static final String ENTRY_NAME = "SourceURLTestEntry";

    public SourceURLLocalEntryAdditionTestCase() {
        super("LocalEntryAdmin");
    }

    @Test(groups = "wso2.esb", description = "Test adding a Source url Local Entry")
    public void testEntryAddition() throws RemoteException, LocalEntryAdminException {
        LocalEntryAdminServiceStub stub = new LocalEntryAdminServiceStub(getAdminServiceURL());
        authenticate(stub);

        String entryNames = stub.getEntryNamesString();
        //If an Entry by the name ENTRY_NAME already exists
        if (entryNames != null && entryNames.contains(ENTRY_NAME)) {
            //Remove the Entry
            assertTrue(stub.deleteEntry(ENTRY_NAME));
        }

        int before = stub.getEntryDataCount();
        assertTrue(stub.addEntry("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" key=\"" + ENTRY_NAME +
                "\" src=\"file:repository/samples/resources/transform/transform.xslt\"/>"));
        int after = stub.getEntryDataCount();
        assertEquals(1, after - before);

        entryNames = stub.getEntryNamesString();
        //The Entry should be added to the entries list
        assertTrue(entryNames.contains(ENTRY_NAME));
    }

}
