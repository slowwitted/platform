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
package org.wso2.carbon.dashboard.deployer;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.DashboardContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * This is the deployer for .dar archives, which contain gadget.xml files and
 * their associated resources such as javascript, images etc.
 */
public class GadgetDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(GadgetDeployer.class);

    // The Registry path to store gadgets
    private static String REGISTRY_GADGET_STORAGE_PATH = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT + DashboardConstants.GADGET_PATH;
    private static String GADGET_CONF_FILE = "gadget-conf.xml";
    private int tenantID = 0 ;

    public void init(ConfigurationContext configurationContext) {

        tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();

        String artifactPath = CarbonUtils.getCarbonRepository() + "gadgets";
        File artifactsDir = new File(artifactPath);

        //checking whether its the gadget server is so, setting the gadget path
        String serverName = ServerConfiguration.getInstance().getFirstProperty("Name").trim();

        if (serverName.contains(DashboardConstants.PRODUCT_SERVER_NAME) || serverName.contains(DashboardConstants.SERVICE_SERVER_NAME)) {
            REGISTRY_GADGET_STORAGE_PATH = DashboardConstants.GS_REGISTRY_ROOT + DashboardConstants.GADGET_PATH;
        }

        if (!artifactsDir.exists()) {
            // If the directory is not there create it
            boolean created = artifactsDir.mkdir();
            if (!created) {
                log.debug("Directory could not be created at : " + artifactPath);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Initializing Gadget Deployer..");
        }
    }

    /**
     * Deploys a .gar archive to the Registry path in REGISTRY_GADGET_STORAGE_PATH
     *
     * @param deploymentFileData - info about the deployed file
     * @throws DeploymentException - error while deploying .gar archive
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {

//            int tenantId;
//            try {
//                tenantId = MultitenantUtils.getTenantId(DashboardContext.getConfigContext());
//            } catch (Exception e) {
//                throw new DeploymentException(e);
//            }

            UserRegistry registry = getRegistry(tenantID);

            // Extracting archive
            String extractedArchiveDir = extractGarArchive(deploymentFileData.getAbsolutePath());

            // Set permission for anonymous read. We do it here because it should happen always in order
            // to support mounting a remote registry.

            if (registry != null) {
                AuthorizationManager accessControlAdmin =
                        registry.getUserRealm().getAuthorizationManager();

                if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        REGISTRY_GADGET_STORAGE_PATH, ActionConstants.GET)) {
                    accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                            REGISTRY_GADGET_STORAGE_PATH, ActionConstants.GET);
                }

                File gadgetsDir = new File(extractedArchiveDir);
                if (gadgetsDir.exists()) {
                    beginFileTansfer(gadgetsDir, tenantID);

                    log.info("Successfully populated gadgets from archive ." + deploymentFileData.getAbsolutePath() + " to the registry.");
                } else {
                    log.info("Couldn't find contents at '" + extractedArchiveDir +
                            "'. Giving up.");

                }
            }

        } catch (RegistryException e) {
            throw new DeploymentException("An error occured while deploying gadget archive", e);
        } catch (CarbonException e) {
            throw new DeploymentException("An error occured while deploying gadget archive", e);
        } catch (UserStoreException e) {
            throw new DeploymentException("An error occured while deploying gadget archive", e);
        }

    }

    public void undeploy(String filePath) throws DeploymentException {

    }

    public static void beginFileTansfer(File rootDirectory, int tenantId) throws RegistryException {

        // Storing the root path for future reference
        String rootPath = rootDirectory.getAbsolutePath();

        UserRegistry registry = getRegistry(tenantId);

        // Creating the default gadget collection resource
        Collection defaultGadgetCollection = registry.newCollection();
        try {
            registry.beginTransaction();
            registry.put(REGISTRY_GADGET_STORAGE_PATH, defaultGadgetCollection);

            transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, tenantId);
            registry.commitTransaction();
        } catch (Exception e) {
            registry.rollbackTransaction();
            log.error(e.getMessage(), e);
        }

    }

    private static void transferDirectoryContentToRegistry(File rootDirectory, Registry registry,
                                                           String rootPath, int tenantId)
            throws Exception {

        try {


            File[] filesAndDirs = rootDirectory.listFiles();
            List<File> filesDirs = Arrays.asList(filesAndDirs);

            for (File file : filesDirs) {

                if (!file.isFile()) {
                    // This is a Directory add a new collection
                    // This path is used to store the file resource under registry
                    String directoryRegistryPath =
                            REGISTRY_GADGET_STORAGE_PATH + file.getAbsolutePath()
                                    .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");

                    // If the collection exists no need to create it. If not, create.
                    if (!registry.resourceExists(directoryRegistryPath)) {
                        Collection newCollection = registry.newCollection();
                        registry.put(directoryRegistryPath, newCollection);
                    }

                    // Set permission for anonymous read. We do it here because it should happen always in order
                    // to support mounting a remote registry.
                    UserRegistry userRegistry = getRegistry(tenantId);
                    AuthorizationManager accessControlAdmin =
                            userRegistry.getUserRealm().getAuthorizationManager();

                    if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                            REGISTRY_GADGET_STORAGE_PATH, ActionConstants.GET)) {
                        accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                REGISTRY_GADGET_STORAGE_PATH, ActionConstants.GET);
                    }

                    // recurse
                    transferDirectoryContentToRegistry(file, registry, rootPath, tenantId);
                } else {
                    //Adding gadget to the gadget browser: gadget conf.xml need to be present
                    if (file.getName().equals(GADGET_CONF_FILE)) {
                        FileInputStream fis = new FileInputStream(file);
                        XMLInputFactory xif = XMLInputFactory.newInstance();
                        XMLStreamReader reader = xif.createXMLStreamReader(fis);

                        StAXOMBuilder builder = new StAXOMBuilder(reader);
                        OMElement omEle = builder.getDocumentElement();

                        String gadgetName = omEle.getFirstChildWithName(
                                new QName("name")).getText();
                        String gadgetPath = omEle.getFirstChildWithName(
                                new QName("path")).getText();
                        String gadgetDesc = omEle.getFirstChildWithName(
                                new QName("description")).getText();


                        Resource res = registry.newResource();
                        res.setProperty(DashboardConstants.GADGET_NAME, gadgetName);
                        res.setProperty(DashboardConstants.GADGET_DESC, gadgetDesc);
                        res.setProperty(DashboardConstants.GADGET_URL, gadgetPath);

                        registry.put(DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT + DashboardConstants.GADGETS_COL + "/" + gadgetName, res);


                    } else {
                        // Add this to registry
                        addToRegistry(rootPath, file, tenantId);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }

    }

    private static void addToRegistry(String rootPath, File file, int tenantId) {
        try {
            Registry registry = getRegistry(tenantId);

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    REGISTRY_GADGET_STORAGE_PATH + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry
            Resource fileResource = registry.newResource();
            fileResource.setMediaType("application/vnd.wso2-gadget+xml");
            fileResource.setContentStream(new FileInputStream(file));
            registry.put(fileRegistryPath, fileResource);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }


    public String extractGarArchive(String garPath) throws CarbonException {
        garPath = AppDeployerUtils.formatPath(garPath);
        String fileName = garPath.substring(garPath.lastIndexOf('/') + 1);
        String destinationDir = AppDeployerUtils.APP_UNZIP_DIR + File.separator + System.currentTimeMillis() +
                fileName + File.separator;
        AppDeployerUtils.createDir(destinationDir);

        try {
            extract(garPath, destinationDir);
        } catch (IOException e) {
            throw new CarbonException("Error while extracting cApp artifact : " + fileName, e);
        }

        return destinationDir;
    }

    private static void extract(String sourcePath, String destPath) throws IOException {
        Enumeration entries;
        ZipFile zipFile;

        zipFile = new ZipFile(sourcePath);
        entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            // if the entry is a directory, create a new dir
            if (entry.isDirectory()) {
                AppDeployerUtils.createDir(destPath + entry.getName());
                continue;
            }
            // if the entry is a file, write the file
            copyInputStream(zipFile.getInputStream(entry),
                    new BufferedOutputStream(new FileOutputStream(destPath + entry.getName())));
        }
        zipFile.close();
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[40960];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

    /**
     * Getting the service either from the Registry service or other means.
     *
     * @return UserRegistry - The registry instance.
     */
    private static UserRegistry getRegistry(int tenantId) {
        UserRegistry registry = null;
        try {
            // registry = (UserRegistry) DashboardContext.getRegistry(tenantId);
            // int tID = PrivilegedCarbonContext.getCurrentContext().getTenantId();
            registry = (UserRegistry) DashboardContext.getRegistry(tenantId);

        } catch (Exception e) {
            // During startup, we can't get a registry instance from declarative services.
            // But the deployer gets called. Using instance stored in CarbonContext in that case.            
            registry = (UserRegistry) CarbonContext.getCurrentContext().getRegistry(
                    RegistryType.SYSTEM_CONFIGURATION);
        }
        return registry;
    }


    public void setDirectory(String s) {

    }

    public void setExtension(String s) {

    }

}
