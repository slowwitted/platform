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
package org.wso2.xkms2;

public class ValidReason {

    public static final ValidReason ISSUER_TRUST =
            new ValidReason(ReasonOpenEnum.ISSUER_TRUST);
    public static final ValidReason REVOCATION_STATUS =
            new ValidReason(ReasonOpenEnum.REVOCATION_STATUS);
    public static final ValidReason VALIDITY_INTERVAL =
            new ValidReason(ReasonOpenEnum.VALIDITY_INTERVAL);
    public static final ValidReason SIGNATURE =
            new ValidReason(ReasonOpenEnum.SIGNATURE);

    private String anyURI;

    private ValidReason(String anyURI) {
        this.anyURI = anyURI;
    }

    public String getAnyURI() {
        return anyURI;
    }

    public static ValidReason validate(String anyURI) throws XKMSException {
        return new ValidReason(ReasonOpenEnum.validate(anyURI));
    }
}
