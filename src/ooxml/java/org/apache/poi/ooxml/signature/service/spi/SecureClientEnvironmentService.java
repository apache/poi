
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

import java.util.List;

/**
 * Interface for security environment service components. Can be used by the eID
 * Applet Service to check the client environment security requirements.
 */
public interface SecureClientEnvironmentService {

    /**
     * Checks whether the client environment is secure enough for this web
     * application.
     * 
     * @param javaVersion
     *            the version of the Java JRE on the client machine.
     * @param javaVendor
     *            the vendor of the Java JRE on the client machine.
     * @param osName
     *            the name of the operating system on the client machine.
     * @param osArch
     *            the architecture of the client machine.
     * @param osVersion
     *            the operating system version of the client machine.
     * @param userAgent
     *            the user agent, i.e. browser, used on the client machine.
     * @param navigatorAppName
     *            the optional navigator application name (browser)
     * @param navigatorAppVersion
     *            the optional navigator application version (browser version)
     * @param navigatorUserAgent
     *            the optional optional navigator user agent name.
     * @param remoteAddress
     *            the address of the client machine.
     * @param sslKeySize
     *            the key size of the SSL session used between server and
     *            client.
     * @param sslCipherSuite
     *            the cipher suite of the SSL session used between server and
     *            client.
     * @param readerList
     *            the list of smart card readers present on the client machine.
     * @throws InsecureClientEnvironmentException
     *             if the client env is found not to be secure enough.
     */
    void checkSecureClientEnvironment(String javaVersion, String javaVendor, String osName, String osArch, String osVersion, String userAgent,
                                    String navigatorAppName, String navigatorAppVersion, String navigatorUserAgent, String remoteAddress, int sslKeySize,
                                    String sslCipherSuite, List<String> readerList) throws InsecureClientEnvironmentException;
}
