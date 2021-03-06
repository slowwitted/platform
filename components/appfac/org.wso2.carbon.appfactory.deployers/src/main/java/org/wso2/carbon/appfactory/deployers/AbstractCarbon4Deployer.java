package org.wso2.carbon.appfactory.deployers;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.clients.ArtifactUploadClient;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

import java.net.URL;
import java.util.Map;

public abstract class AbstractCarbon4Deployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(AbstractCarbon4Deployer.class);


    public void deployCarbonApp(UploadedFileItem[] uploadedFileItems, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);

        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(
                        serviceUrl);
                if (artifactUploadClient.authenticate(
                        getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient.uploadCarbonApp(uploadedFileItems);
                    log.debug(uploadedFileItems[0].getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + uploadedFileItems[0].getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + uploadedFileItems[0].getFileName() +
                        " of application:" + applicationId + " to deployment location:" +
                        serviceUrl;
                handleException(msg,e);
            }
        }
    }

    public void uploadWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);


        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(
                        serviceUrl);
                if (artifactUploadClient.authenticate(
                        getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient
                            .uploadWebApp(webappUploadDatas);
                    log.debug(webappUploadDatas[0].getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + webappUploadDatas[0].getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + webappUploadDatas[0].getFileName() +
                        " of application:" + applicationId + " to deployment location:" +
                        serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void uploadJaxWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);


        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(
                        serviceUrl);
                if (artifactUploadClient.authenticate(
                        getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient
                            .uploadJaxWebApp(webappUploadDatas);
                    log.debug(webappUploadDatas[0].getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + webappUploadDatas[0].getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + webappUploadDatas[0].getFileName() +
                        " of application:" + applicationId + " to deployment location:" +
                        serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void uploadJaggeryApp(org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);


        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(
                        serviceUrl);
                if (artifactUploadClient.authenticate(
                        getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient
                            .uploadJaggeryApp(webappUploadDatas);
                    log.debug(webappUploadDatas[0].getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + webappUploadDatas[0].getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + webappUploadDatas[0].getFileName() +
                        " of application:" + applicationId + " to deployment location:" +
                        serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void uploadBPEL(org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] uploadedFileItem, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);


        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(
                        serviceUrl);
                if (artifactUploadClient.authenticate(
                        getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient
                            .uploadBpel(uploadedFileItem);
                    log.debug(uploadedFileItem[0].getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + uploadedFileItem[0].getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + uploadedFileItem[0].getFileName() +
                        " of application:" + applicationId + " to deployment location:" +
                        serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void uploadDBSApp(UploadItem[] uploadData, Map metadata) throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);
        UploadItem uploadItem = null;

        for (UploadItem item : uploadData) {
            if(item.getFileName().endsWith("dbs")){
                uploadItem = item;
            }
        }

        if(uploadItem == null){
            return;
        }

        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(serviceUrl);
                if (artifactUploadClient.authenticate(getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient.uploadDBSApp(uploadItem.getFileName(), uploadItem.getDataHandler());
                    log.debug(uploadItem.getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + uploadItem.getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + uploadItem.getFileName() + " of application:" +
                        applicationId + " to deployment location:" + serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void uploadPHP(UploadItem[] uploadData, Map metadata) {
        log.warn("PHP app is not supported by Stratos 1.6");
    }

    @Override
    public void uploadESBApp(ExtendedUploadItem[] uploadData, Map metadata)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);

//        We only expect one upload item.
        UploadItem uploadItem =  uploadData[0];

//        TODO:this was ESBDEPLOYMENT_SERVER_URLS
        String[] deploymentServerUrls = getParameterValues(metadata, AppFactoryConstants.DEPLOYMENT_SERVER_URLS);

        for (String serviceUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = new URL(serviceUrl).getHost();
                ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(serviceUrl);
                if (artifactUploadClient.authenticate(getAdminUsername(applicationId),
                        getServerAdminPassword(), deploymentServerIp)) {

                    artifactUploadClient.uploadESBApp(uploadItem.getFileName(), uploadItem.getDataHandler());
                    log.debug(uploadItem.getFileName() + " is successfully uploaded.");

                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to deploy the artifact:" + uploadItem.getFileName());
                }
            } catch (Exception e) {
                String msg="Failed to upload the artifact:" + uploadItem.getFileName() + " of application:" +
                        applicationId + " to deployment location:" + serviceUrl;
                handleException(msg,e);
            }
        }
    }

    @Override
    public void unDeployArtifact(Map<String, String[]> requestParameters) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
