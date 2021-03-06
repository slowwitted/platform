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
package org.wso2.carbon.identity.entitlement;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.wso2.balana.*;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.DateAttribute;
import org.wso2.balana.attr.DateTimeAttribute;
import org.wso2.balana.attr.DoubleAttribute;
import org.wso2.balana.attr.HexBinaryAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.TimeAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.balana.attr.AttributeValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicablePolicyAlg;
import org.wso2.balana.combine.xacml2.OnlyOneApplicablePolicyAlg;
import org.wso2.balana.combine.xacml3.*;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.xacml2.RequestCtx;
import org.wso2.balana.ctx.xacml2.Subject;
import org.wso2.balana.xacml3.Attributes;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.cache.EntitlementBaseCache;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheEntry;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheKey;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementExtensionBuilder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyStoreManageModule;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

/**
 *
 * Provides utility functionalities used across different classes.
 *
 */
public class EntitlementUtil {

	private static Log log = LogFactory.getLog(EntitlementUtil.class);


    /**
     * Return an instance of a named cache that is common to all tenants.
     *
     * @param name the name of the cache.
     *
     * @return the named cache instance.
     */
    public static EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry> getCommonCache(String name) {
        // TODO Should verify the cache creation done per tenant or as below

        // We create a single cache for all tenants. It is not a good choice to create per-tenant
        // caches in this case. We qualify tenants by adding the tenant identifier in the cache key.
//	    PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getCurrentContext();
//	    PrivilegedCarbonContext.startTenantFlow();
//		try {
//			currentContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//			return CacheManager.getInstance().getCache(name);
//		} finally {
//		    PrivilegedCarbonContext.endTenantFlow();
//		}

        return new EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry>(name);
    }
    /**
     * Return the Attribute Value Object for given string value and data type
     * @param value attribute value as a String object
     * @param type  attribute data type name as String object
     * @return Attribute Value Object
     * @throws EntitlementException throws
     */
	public static AttributeValue getAttributeValue(final String value, String type)
                        throws EntitlementException {

        try {
            if (StringAttribute.identifier.equals(type)) {
                return new StringAttribute(value);
            }
            if (IntegerAttribute.identifier.equals(type)) {
                return new IntegerAttribute(Long.parseLong(value));
            }
            if (BooleanAttribute.identifier.equals(type)) {
                return BooleanAttribute.getInstance(value);
            }
            if (DoubleAttribute.identifier.equals(type)) {
                return new DoubleAttribute(Double.parseDouble(value));
            }
            if (DateAttribute.identifier.equals(type)) {
                return new DateAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (DateTimeAttribute.identifier.equals(type)) {
                return new DateTimeAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (TimeAttribute.identifier.equals(type)) {
                return TimeAttribute.getInstance(value);
            }
            if (HexBinaryAttribute.identifier.equals(type)) {
                return new HexBinaryAttribute(value.getBytes());
            }

            return new AttributeValue(new URI(type)) {
                @Override
                public String encode() {
                    return value;
                }
            };

        } catch (ParsingException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        } catch (ParseException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        } catch (URISyntaxException e) {
            throw new EntitlementException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        }
	}

    /**
     * This creates the XACML 3.0 Request context from AttributeDTO object model
     * @param attributeDTOs  AttributeDTO objects as List
     * @return DOM element as XACML request
     * @throws EntitlementException  throws, if fails
     */
    public static AbstractRequestCtx createRequestContext(List<AttributeDTO> attributeDTOs){

        Set<Attributes>  attributesSet = new HashSet<Attributes>();

        for(AttributeDTO DTO : attributeDTOs){
            Attributes attributes = getAttributes(DTO);
            if(attributes != null){
                attributesSet.add(attributes);
            }
        }
        return new org.wso2.balana.ctx.xacml3.RequestCtx(attributesSet, null);
    }

    /**
     * Validates the given policy XML files against the standard XACML policies.
     * 
     * @param policy Policy to validate
     * @return return false, If validation failed or XML parsing failed or any IOException occurs 
     */
    public static boolean validatePolicy(PolicyDTO policy) {
        try {

            if (!"true".equalsIgnoreCase((String) EntitlementServiceComponent.getEntitlementConfig()
                    .getEngineProperties().get(EntitlementExtensionBuilder.PDP_SCHEMA_VALIDATION))) {
                return true;
            }

            // there may be cases where you only updated the policy meta data in PolicyDTO not the
            // actual XACML policy String
            if(policy.getPolicy() == null || policy.getPolicy().trim().length() < 1){
                return true;
            }

	        //get policy version
            String policyXMLNS = getPolicyVersion(policy.getPolicy());

            Map<String, Schema> schemaMap = EntitlementServiceComponent.
                                                    getEntitlementConfig().getPolicySchemaMap();
            //load correct schema by version
            Schema schema = schemaMap.get(policyXMLNS);

            if(schema != null){
                //build XML document
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                InputStream stream = new ByteArrayInputStream(policy.getPolicy().getBytes());
                Document doc = documentBuilder.parse(stream);
                //Do the DOM validation
                DOMSource domSource = new DOMSource(doc);
                DOMResult domResult = new DOMResult();
                Validator validator = schema.newValidator();
                validator.validate(domSource,domResult);
                if(log.isDebugEnabled()){
                    log.debug("XACML Policy validation succeeded with the Schema");
                }
                return true;
            } else {
                log.error("Invalid Namespace in policy");
            }
        } catch (SAXException e) {
            log.error("XACML policy is not valid according to the schema :" + e.getMessage());
        } catch (IOException e) {
            //ignore
        } catch (ParserConfigurationException e) {
            //ignore
        }
        return false;
    }


    public static String getPolicyVersion(String policy)  {

        try{
            //build XML document
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(policy.getBytes());
            Document doc = documentBuilder.parse(stream);

            //get policy version
            Element policyElement = doc.getDocumentElement();
            return policyElement.getNamespaceURI();
        } catch (Exception e) {
            // ignore exception as default value is used
            log.warn("Policy version can not be identified. Default XACML 3.0 version is used");
            return XACMLConstants.XACML_3_0_IDENTIFIER;
        }
    }


    public static Attributes getAttributes(AttributeDTO attributeDataDTO){

        try {
            AttributeValue value = Balana.getInstance().getAttributeFactory().
                                    createValue(new URI(attributeDataDTO.getAttributeDataType()),
                                            attributeDataDTO.getAttributeValue());
            Attribute attribute = new Attribute(new URI(attributeDataDTO.getAttributeId()),
                                            null, null, value, XACMLConstants.XACML_VERSION_3_0);
            Set<Attribute> set = new HashSet<Attribute>();
            set.add(attribute);
            return new Attributes(new URI(attributeDataDTO.getCategory()), set);
        } catch (Exception e) {
            //ignore and return null;
        }

        return null;
    }

	/**
	 * Creates PolicyCombiningAlgorithm object based on policy combining url
	 *
	 * @param uri policy combining url as String
	 * @return PolicyCombiningAlgorithm object
	 * @throws EntitlementException throws if unsupported algorithm
	 */
	public static PolicyCombiningAlgorithm getPolicyCombiningAlgorithm(String uri)
			throws EntitlementException {

		if (FirstApplicablePolicyAlg.algId.equals(uri)) {
			return new FirstApplicablePolicyAlg();
		} else if (DenyOverridesPolicyAlg.algId.equals(uri)) {
			return new DenyOverridesPolicyAlg();
		} else if (PermitOverridesPolicyAlg.algId.equals(uri)) {
			return new PermitOverridesPolicyAlg();
		} else if (OnlyOneApplicablePolicyAlg.algId.equals(uri)) {
			return new OnlyOneApplicablePolicyAlg();
		} else if (OrderedDenyOverridesPolicyAlg.algId.equals(uri)) {
			return new OrderedDenyOverridesPolicyAlg();
		} else if (OrderedPermitOverridesPolicyAlg.algId.equals(uri)) {
			return new OrderedPermitOverridesPolicyAlg();
		} else if (DenyUnlessPermitPolicyAlg.algId.equals(uri)){
            return new DenyUnlessPermitPolicyAlg();
        } else if(PermitUnlessDenyPolicyAlg.algId.equals(uri)){
            return new PermitUnlessDenyPolicyAlg();
        }

		throw new EntitlementException("Unsupported policy algorithm " + uri);
	}

	/**
	 * Creates Simple XACML request using given attribute value.Here category, attribute ids and datatypes are
     * taken as default values.
	 *
	 * @param subject user or role
	 * @param resource resource name
	 * @param action action name
	 * @return String XACML request as String
	 */
	public static String createSimpleXACMLRequest(String subject, String resource, String action)  {

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
        "ReturnPolicyIdList=\"false\" CombinedDecision=\"false\"><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" >" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"+ subject +
        "</AttributeValue></Attribute></Attributes><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource +
        "</AttributeValue></Attribute></Attributes><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>" +
        "</Attribute></Attributes></Request>";
	}

    public static void addSamplePolicies(Registry registry) {

        File policyFolder = new File(CarbonUtils.getCarbonHome() + File.separator
                + "repository" + File.separator + "resources" + File.separator
                + "security" + File.separator + "policies" + File.separator + "xacml"
                + File.separator + "default");

        if (policyFolder.exists()) {
            for (File policyFile : policyFolder.listFiles()) {
                if (policyFile.isFile()) {
                    PolicyDTO policyDTO = new PolicyDTO();
                    try{
                        policyDTO.setPolicy(FileUtils.readFileToString(policyFile));
                        EntitlementUtil.addFilesystemPolicy(policyDTO, registry, false);
                    } catch (Exception e){
                        // log and ignore
                        log.error("Error while adding sample XACML policies" ,e);
                    }
                }
            }
        }
    }

    /**
     * This method persists a new XACML policy, which was read from filesystem,
     * in the registry
     * 
     * @param policyDTO PolicyDTO object
     * @param registry Registry
     * @param promote where policy must be promote PDP or not
     * @return returns whether True/False
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException
     *             throws if policy with same id is exist
     */
    public static boolean addFilesystemPolicy(PolicyDTO policyDTO,
                                              Registry registry, boolean promote)
            throws EntitlementException {

        PAPPolicyStoreManager policyAdmin;
        AbstractPolicy policyObj;

        if (policyDTO.getPolicy() != null) {
            policyDTO.setPolicy(policyDTO.getPolicy().replaceAll(">\\s+<", "><"));
        }

        policyObj = getPolicy(policyDTO.getPolicy());

        if (policyObj != null) {
            PAPPolicyStore policyStore = new PAPPolicyStore(registry);
            policyAdmin = new PAPPolicyStoreManager();
            policyDTO.setPolicyId(policyObj.getId().toASCIIString());
            policyDTO.setActive(true);

            if (getPolicy(policyDTO.getPolicyId(), registry) != null) {
                throw new EntitlementException(
                        "An Entitlement Policy with the given ID already exists");
            }

            policyDTO.setPromote(true);
            policyAdmin.addOrUpdatePolicy(policyDTO);

            PAPPolicyStoreReader reader = new PAPPolicyStoreReader(policyStore);
            policyDTO = reader.readPolicyDTO(policyDTO.getPolicyId());

            PolicyStoreDTO policyStoreDTO = new PolicyStoreDTO();
            policyStoreDTO.setPolicyId(policyDTO.getPolicyId());
            policyStoreDTO.setPolicy(policyDTO.getPolicy());
            policyStoreDTO.setPolicyOrder(policyDTO.getPolicyOrder());
            policyStoreDTO.setAttributeDTOs(policyDTO.getAttributeDTOs());

            if(promote){
                addPolicyToPDP(policyStoreDTO);
            }
            
            policyAdmin.addOrUpdatePolicy(policyDTO);
            
            return true;
        } else {
            throw new EntitlementException("Invalid Entitlement Policy");
        }
    }


    public static AbstractPolicy getPolicy(String policy) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        InputStream stream = null;
        // now use the factory to create the document builder
        try {
            builder = factory.newDocumentBuilder();
            stream = new ByteArrayInputStream(policy.getBytes("UTF-8"));
            Document doc = builder.parse(stream);
            Element root = doc.getDocumentElement();
            String name = root.getTagName();
            // see what type of policy this is
            if (name.equals("Policy")) {
                return Policy.getInstance(root);
            } else if (name.equals("PolicySet")) {
                return PolicySet.getInstance(root, null);
            } else {
                // this isn't a root type that we know how to handle
                throw new ParsingException("Unknown root document type: " + name);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while parsing start up policy" , e);
        } finally {
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("Error while closing input stream");
                }
            }
        }
    }
	
	/**
	 * Gets policy dto for a given policy id
	 * 
	 * @param policyId policy id
	 * @param registry Registry
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.entitlement.EntitlementException
	 */
    public static PolicyDTO getPolicy(String policyId, Registry registry) throws EntitlementException {
        PAPPolicyStoreReader policyReader = null;
        policyReader = new PAPPolicyStoreReader(new PAPPolicyStore(registry));
        return policyReader.readPolicyDTO(policyId);
    }
	
    /**
     * @param policyStoreDTO
     * @return
     */
    public static void addPolicyToPDP(PolicyStoreDTO policyStoreDTO) throws EntitlementException {

        Registry registry;
        String policyPath;
        Collection policyCollection;
        Resource resource;

        Map.Entry<PolicyStoreManageModule, Properties> entry = EntitlementServiceComponent
                .getEntitlementConfig().getPolicyStore().entrySet().iterator().next();
        String policyStorePath = entry.getValue().getProperty("policyStorePath");

        if (policyStorePath == null) {
            policyStorePath = "/repository/identity/Entitlement/actualStore/";
        }

        if (policyStoreDTO == null || policyStoreDTO.getPolicy() == null
                || policyStoreDTO.getPolicy().trim().length() == 0
                || policyStoreDTO.getPolicyId() == null
                || policyStoreDTO.getPolicyId().trim().length() == 0) {
            return;
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService()
                    .getGovernanceSystemRegistry();

            if (registry.resourceExists(policyStorePath)) {
                policyCollection = (Collection) registry.get(policyStorePath);
            } else {
                policyCollection = registry.newCollection();
            }

            registry.put(policyStorePath, policyCollection);
            policyPath = policyStorePath + policyStoreDTO.getPolicyId();

            if (registry.resourceExists(policyPath)) {
                resource = registry.get(policyPath);
            } else {
                resource = registry.newResource();
            }

            resource.setProperty("policyOrder", Integer.toString(policyStoreDTO.getPolicyOrder()));
            resource.setContent(policyStoreDTO.getPolicy());
            resource.setMediaType("application/xacml-policy+xml");
            AttributeDTO[] attributeDTOs = policyStoreDTO.getAttributeDTOs();
            if (attributeDTOs != null) {
                setAttributesAsProperties(attributeDTOs, resource);
            }
            registry.put(policyPath, resource);
        } catch (RegistryException e) {
            log.error(e);
            throw new EntitlementException("Error while adding policy to PDP" ,e);
        }
    }
    
    /**
     * This helper method creates properties object which contains the policy meta data.
     *
     * @param attributeDTOs List of AttributeDTO
     * @param resource registry resource
     */
    public static void setAttributesAsProperties(AttributeDTO[] attributeDTOs, Resource resource) {
        
        int attributeElementNo = 0;
        if(attributeDTOs != null){
            for(AttributeDTO attributeDTO : attributeDTOs){
                resource.setProperty("policyMetaData" + attributeElementNo,
                       attributeDTO.getCategory() + "," +
                       attributeDTO.getAttributeValue() + "," +
                       attributeDTO.getAttributeId() + "," +
                       attributeDTO.getAttributeDataType());
                attributeElementNo ++;
            }
        }
    }
}