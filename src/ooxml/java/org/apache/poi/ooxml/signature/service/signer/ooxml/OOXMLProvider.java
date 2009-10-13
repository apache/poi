
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


/*
 * Based on the eID Applet Project code.
 * Original Copyright (C) 2008-2009 FedICT.
 */

package org.apache.poi.ooxml.signature.service.signer.ooxml;

import java.security.Provider;
import java.security.Security;

/**
 * Security Provider for Office OpenXML.
 */
public class OOXMLProvider extends Provider {

    private static final long serialVersionUID = 1L;

    public static final String NAME = "OOXMLProvider";

    private OOXMLProvider() {
        super(NAME, 1.0, "OOXML Security Provider");
        put("TransformService." + RelationshipTransformService.TRANSFORM_URI, RelationshipTransformService.class.getName());
        put("TransformService." + RelationshipTransformService.TRANSFORM_URI + " MechanismType", "DOM");
    }

    /**
     * Installs this security provider.
     */
    public static void install() {
        Provider provider = Security.getProvider(NAME);
        if (null == provider) {
            Security.addProvider(new OOXMLProvider());
        }
    }
}
