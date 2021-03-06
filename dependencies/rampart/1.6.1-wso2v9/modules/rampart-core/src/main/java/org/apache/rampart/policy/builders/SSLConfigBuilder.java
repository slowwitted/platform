/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.rampart.policy.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.rampart.policy.model.SSLConfig;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Properties;

public class SSLConfigBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        
    	SSLConfig sslCofig = new SSLConfig();       	      
		Properties properties = new Properties();
        OMElement childElement;
        OMAttribute name;
        String value;     
        
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {            

            childElement = (OMElement) iterator.next();

            QName prop = new QName(RampartConfig.NS, SSLConfig.PROPERTY_LN);
            
            if (prop.equals(childElement.getQName())) {
                name = childElement.getAttribute(new QName(SSLConfig.PROPERTY_NAME_ATTR));
                value = childElement.getText();
                
                //setting the jsse properties to the vm
                System.setProperty(name.getAttributeValue(), value);

                properties.put(name.getAttributeValue(), value.trim());
            }

        }            
        sslCofig.setProp(properties);
              
        return sslCofig;
    }

    public QName[] getKnownElements() {
    	return new QName[] {new QName(RampartConfig.NS, SSLConfig.SSL_LN)};
    }

}
