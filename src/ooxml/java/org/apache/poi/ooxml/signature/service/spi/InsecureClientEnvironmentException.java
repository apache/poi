
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

/**
 * Insecure Client Environment Exception.
 */
public class InsecureClientEnvironmentException extends Exception {

    private static final long serialVersionUID = 1L;

    private final boolean warnOnly;

    /**
     * Default constructor.
     */
    public InsecureClientEnvironmentException() {
        this(false);
    }

    /**
     * Main constructor.
     * 
     * @param warnOnly
     *            only makes that the citizen is warned about a possible
     *            insecure enviroment.
     */
    public InsecureClientEnvironmentException(boolean warnOnly) {
        this.warnOnly = warnOnly;
    }

    /**
     * If set the eID Applet will only give a warning on case the server-side
     * marks the client environment as being insecure. Else the eID Applet will
     * abort the requested eID operation.
     * 
     * @return
     */
    public boolean isWarnOnly() {
        return this.warnOnly;
    }
}
