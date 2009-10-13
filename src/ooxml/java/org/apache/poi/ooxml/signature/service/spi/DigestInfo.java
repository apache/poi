
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

package org.apache.poi.ooxml.signature.service.spi;

import java.io.Serializable;

/**
 * Digest Information data transfer class.
 */
public class DigestInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Main constructor.
     * 
     * @param digestValue
     * @param digestAlgo
     * @param description
     */
    public DigestInfo(byte[] digestValue, String digestAlgo, String description) {
        this.digestValue = digestValue;
        this.digestAlgo = digestAlgo;
        this.description = description;
    }

    public final byte[] digestValue;

    public final String description;

    public final String digestAlgo;
}
