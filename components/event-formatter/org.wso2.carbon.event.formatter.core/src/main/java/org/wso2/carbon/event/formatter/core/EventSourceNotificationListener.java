package org.wso2.carbon.event.formatter.core;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;

public class EventSourceNotificationListener {

    private static final Log log = LogFactory.getLog(EventSourceNotificationListener.class);

    public void addedNewEventStream(int tenantId, String streamNameWithVersion) {

        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.reDeployEventFormatterConfigurationFile(tenantId, streamNameWithVersion);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while trying to deploy the Event formatter configuration files");
        }

    }

    public void removeEventStream(int tenantId, String streamNameWithVersion) {
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.unDeployEventFormatterConfigurationFile(tenantId, streamNameWithVersion);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event formatter configuration files");
        }

    }


}
