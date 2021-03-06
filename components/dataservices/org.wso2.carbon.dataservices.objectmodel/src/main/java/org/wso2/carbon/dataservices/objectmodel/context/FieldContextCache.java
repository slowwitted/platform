/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.objectmodel.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a field context cache. 
 */
public class FieldContextCache { 
    
    private Map<String, CachedFieldContext> fieldContextCache = new HashMap<String, CachedFieldContext>(100);
    
    private Map<String, List<CachedFieldContext>> subFieldContextMapping = new HashMap<String, 
            List<CachedFieldContext>>();
    
    public void addToFieldCache(FieldContextPath path, CachedFieldContext fieldCtx) {
        this.fieldContextCache.put(path.getAbsolutePath(), fieldCtx);
        this.addToSubFieldContextMapping(path, fieldCtx);
    }
    
    private void addToSubFieldContextMapping(FieldContextPath path, CachedFieldContext fieldCtx) {
        FieldContextPath headPathCtx = path.getHeadPath();
        if (headPathCtx == null) {
            return;
        }
        String headPath = headPathCtx.getAbsolutePath();
        List<CachedFieldContext> subFields = this.subFieldContextMapping.get(headPath);
        if (subFields == null) {
            subFields = new ArrayList<CachedFieldContext>();
            this.subFieldContextMapping.put(headPath, subFields);
        }
        subFields.add(fieldCtx);
    }

    public CachedFieldContext getCachedField(String path) {
        return this.fieldContextCache.get(path);
    }
    
    public void clearCacheForHead(String path) throws FieldContextException {
        List<CachedFieldContext> subCtxs = this.subFieldContextMapping.get(path);
        if (subCtxs != null) {
            for (CachedFieldContext ctx : subCtxs) {
                this.fieldContextCache.remove(ctx.getPath());
                ctx.close();    
            }
        }
        this.subFieldContextMapping.remove(path);
    }
    
}
