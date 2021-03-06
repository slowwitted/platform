/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */
package org.wso2.xkms2.builder;

import org.apache.axiom.om.OMElement;
import org.wso2.xkms2.LocateRequest;
import org.wso2.xkms2.XKMS2Constants;
import org.wso2.xkms2.XKMSElement;
import org.wso2.xkms2.XKMSException;

import javax.xml.namespace.QName;
/*
 * 
 */

public class LocateRequestBuilder extends KISSRequestBuilder {

    private LocateRequest locateRequest;

    public LocateRequestBuilder() {
        this.locateRequest = new LocateRequest();
    }

    public XKMSElement buildElement(OMElement element) throws XKMSException {
        QName qName = element.getQName();
        if (!(qName.getLocalPart().equals("LocateRequest") &&
              qName.getNamespaceURI().equals(XKMS2Constants.XKMS2_NS))) {
            throw new XKMSException("LocateRequest is not the first element");

        }
        super.buildElement(element, locateRequest);
        return locateRequest;
    }
}
