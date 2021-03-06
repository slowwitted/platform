/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.api.selenium.apimanager.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AppFactoryDataHolder;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.api.selenium.appfactory.resources.ApiManagerHomePage;
import org.wso2.carbon.automation.api.selenium.appfactory.resources.ResourceOverviewPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;
import java.util.Set;

public class SubscriptionPage {
    private static final Log log = LogFactory.getLog(AppLogin.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public SubscriptionPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("subscriptions.jag"))) {
            throw new IllegalStateException("This is not the Api Manager subscription");
        }
    }


    /**
     * this method uses to Subscribe to Api Manager
     *
     * @throws IOException          input output exception
     * @throws InterruptedException for thread sleeps
     */
    public void generateKeys()
            throws IOException, InterruptedException {
        driver.manage().window().maximize();
        Thread.sleep(5000);
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.api.key.generate.link.text"))).click();
        //This thread waits until keys Get generated
        Thread.sleep(10000);
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.api.key.generate.link.text"))).click();
        Thread.sleep(10000);
        String sandBoxAndProductionDetails = driver.findElement(By.id(("appDetails0_super"))).getText();
        log.info("--------------------------------------------------------------------------");
        log.info(sandBoxAndProductionDetails);
        log.info("--------------------------------------------------------------------------");
        AppFactoryDataHolder.setSandBoxAndProductionDetails(sandBoxAndProductionDetails);
        log.info("Sand box And production details are added");
    }
    /**
     * navigates to Resource Overview Page
     *
     * @return ResourceOverviewPage
     * @throws IOException for input output exceptions
     */
    public ResourceOverviewPage gotoResourceOverviewPage() throws IOException {

        try {
            Set handles = driver.getWindowHandles();
            String current = driver.getWindowHandle();
            handles.remove(current);
            String newTab = (String) handles.iterator().next();
            driver.switchTo().window(newTab);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("shifted to the app factory tab");
        return new ResourceOverviewPage(driver);
    }

    /**
     * navigates to APi Manager Home Page
     *
     * @return ApiManagerHomePage
     * @throws IOException for input Output Exceptions
     */
    public ApiManagerHomePage gotoApiManagerHomePage() throws IOException {

        try {
            Set handles = driver.getWindowHandles();
            String current = driver.getWindowHandle();
            handles.remove(current);
            String newTab = (String) handles.iterator().next();
            driver.switchTo().window(newTab);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("shifted to the app factory tab");
        return new ApiManagerHomePage(driver);
    }
    /**
     * this method refreshes the page
     *
     * @throws InterruptedException for Thread Sleeps
     */
    public void refresh() throws InterruptedException {
        log.info("page is refreshing");
        //This Thread waits until the page get refreshed
        Thread.sleep(20000);
        driver.navigate().refresh();
    }
    /**
     * this method uses to select application
     *
     * @param appKey application key
     * @throws InterruptedException for thread sleeps
     * @throws IOException          for input output exceptions
     */
    public void selectApp(String appKey) throws InterruptedException, IOException {
        log.info("selecting the application");
        //This Thread waits until application gets select
        Thread.sleep(5000);
        String xpath1 = "/html/body/div[4]/div[7]/div/div[2]/div[2]/div/div/a";
        String applicationKey = driver.findElement(By.xpath(xpath1)).getText();
        if (appKey.equals(applicationKey)) {
            generateKeys();
        } else {
            String constructXpath1 = "/html/body/div[4]/div[7]/div/div[2]/div[2]/div[";
            String constructXpath2 = "]/div/a";
            for (int i = 2; i < 10; i++) {
                String actualXpath = constructXpath1 + i + constructXpath2;
                String applicationKeyValue = driver.findElement(By.xpath(actualXpath)).getText();
                log.info("val on app is -------> " + applicationKeyValue);
                log.info("Correct is    -------> " + appKey);

                if (appKey.equals(applicationKeyValue)) {
                    driver.findElement(By.xpath(actualXpath)).click();
                    Thread.sleep(2000);
                    String showKeysXpath1 = "/html/body/div[4]/div[7]/div/div[2]/div[2]/div[" + i + "]" +
                            "/div[2]/div/div/div/div[2]/div/div/div/a";
                    String showKeyXpath2 = "/html/body/div[4]/div[7]/div/div[2]/div[2]/div[" + i + "]" +
                            "/div[2]/div/div/div/div[2]/div/div[2]/div/a";
                    //This Threads waits until  keys gets  generated
                    Thread.sleep(2000);
                    driver.findElement(By.xpath(showKeysXpath1)).click();
                    Thread.sleep(2000);
                    driver.findElement(By.xpath(showKeyXpath2)).click();
                    Thread.sleep(2000);
                    String productionAndSandBoxDetails = "appDetails" + (i - 1) + "_super";
                    String allDetails = driver.findElement(By.id(productionAndSandBoxDetails)).getText();
                    log.info("--------------------------------------------------------------------------");
                    log.info(allDetails);
                    log.info("----------------------------------------------------------------------");
                    AppFactoryDataHolder.setSandBoxAndProductionDetails(productionAndSandBoxDetails);
                    log.info("Sand box And production details are added");
                    break;
                }
            }
            log.info("Application Not found");
        }
        driver.findElement(By.linkText((appKey))).click();
        log.info("application is selected");
    }
}
