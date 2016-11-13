/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.xmlbeans.XmlObject;

/**
 * @deprecated POI 3.15 beta 3. Use {@link XSSFPasswordHelper} instead.
 */
@Internal(since="3.15 beta 3")
@Deprecated
@Removal(version="3.17")
public class XSSFPaswordHelper {
    /**
     * Sets the XORed or hashed password 
     *
     * @param xobj the xmlbeans object which contains the password attributes
     * @param password the password, if null, the password attributes will be removed
     * @param hashAlgo the hash algorithm, if null the password will be XORed
     * @param prefix the prefix of the password attributes, may be null
     */
    public static void setPassword(XmlObject xobj, String password, HashAlgorithm hashAlgo, String prefix) {
        XSSFPasswordHelper.setPassword(xobj, password, hashAlgo, prefix);
    }

    /**
     * Validates the password, i.e.
     * calculates the hash of the given password and compares it against the stored hash
     *
     * @param xobj the xmlbeans object which contains the password attributes
     * @param password the password, if null the method will always return false,
     *  even if there's no password set
     * @param prefix the prefix of the password attributes, may be null
     * 
     * @return true, if the hashes match
     */
    public static boolean validatePassword(XmlObject xobj, String password, String prefix) {
        return XSSFPasswordHelper.validatePassword(xobj, password, prefix);
    }
}
