/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.formatter.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.event.formatter.core.config.OutputMapping;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.internal.util.FormatterConfigurationBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class XMLFormatterConfigurationBuilder extends FormatterConfigurationBuilder {

    private XMLFormatterConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventFormatterValidationException, EventFormatterConfigurationException {

        XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();

        if (!validateXMLEventMapping(mappingElement)) {
            throw new EventFormatterConfigurationException("XML Mapping is not valid, check the output mapping");
        }

        OMElement innerMappingElement = mappingElement.getFirstChildWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_MAPPING_INLINE));
        if (innerMappingElement != null) {
            xmlOutputMapping.setRegistryResource(false);
        } else {
            innerMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_MAPPING_REGISTRY));
            if (innerMappingElement != null) {
                xmlOutputMapping.setRegistryResource(true);
                try {
                    AXIOMUtil.stringToOM(innerMappingElement.toString());
                } catch (XMLStreamException e) {
                    throw new EventFormatterConfigurationException("XML Mapping that provided is not valid");
                }

            } else {
                throw new EventFormatterConfigurationException("XML Mapping is not valid, Mapping should be inline or from registry");
            }
        }

        String xmlMappingText = innerMappingElement.toString();
        if (innerMappingElement != null) {
            int index1 = xmlMappingText.indexOf(">");
            int index2 = xmlMappingText.lastIndexOf("<");
            xmlMappingText = xmlMappingText.substring(index1 + 1, index2);
            xmlOutputMapping.setMappingXMLText(xmlMappingText);
        }

        return xmlOutputMapping;
    }

    private static boolean validateXMLEventMapping(OMElement omElement) {


        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            mappingIterator.next();
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) outputMapping;
        String xmlText = xmlOutputMapping.getMappingXMLText();

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventFormatterConstants.EF_ELE_MAPPING_PROPERTY));
        mappingOMElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventFormatterConstants.EF_ATTR_TYPE, EventFormatterConstants.EF_XML_MAPPING_TYPE, null);

        OMElement innerMappingElement = null;
        if (xmlOutputMapping.isRegistryResource()) {
            innerMappingElement = factory.createOMElement(new QName(
                    EventFormatterConstants.EF_ELE_MAPPING_REGISTRY));
            innerMappingElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
            mappingOMElement.addChild(innerMappingElement);
            innerMappingElement.setText(xmlText);
        } else {
            innerMappingElement = factory.createOMElement(new QName(
                    EventFormatterConstants.EF_ELE_MAPPING_INLINE));
            innerMappingElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
            try {
                AXIOMUtil.stringToOM(xmlText);
            } catch (XMLStreamException e) {
                throw new EventFormatterConfigurationException("XML Mapping that provided is not valid");
            }

            mappingOMElement.addChild(innerMappingElement);
            try {
                innerMappingElement.addChild(AXIOMUtil.stringToOM(xmlText));
            } catch (XMLStreamException e) {
                throw new EventFormatterConfigurationException("XML mapping is not in XML format :" + xmlText, e);
            }
        }

        return mappingOMElement;
    }


}




