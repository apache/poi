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

import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.Internal;
import org.apache.poi.util.RandomSingleton;
import org.apache.poi.util.StringUtil;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

@Internal(since="3.15 beta 3")
public final class XSSFPasswordHelper {
    private XSSFPasswordHelper() {
        // no instances of this static class
    }

    /**
     * Sets the XORed or hashed password 
     *
     * @param xobj the xmlbeans object which contains the password attributes
     * @param password the password, if null, the password attributes will be removed
     * @param hashAlgo the hash algorithm, if null the password will be XORed
     * @param prefix the prefix of the password attributes, may be null
     */
    public static void setPassword(XmlObject xobj, String password, HashAlgorithm hashAlgo, String prefix) {
        try (final XmlCursor cur = xobj.newCursor()) {
            if (password == null) {
                cur.removeAttribute(getAttrName(prefix, "password"));
                cur.removeAttribute(getAttrName(prefix, "algorithmName"));
                cur.removeAttribute(getAttrName(prefix, "hashValue"));
                cur.removeAttribute(getAttrName(prefix, "saltValue"));
                cur.removeAttribute(getAttrName(prefix, "spinCount"));
                return;
            }

            cur.toFirstContentToken();
            if (hashAlgo == null) {
                int hash = CryptoFunctions.createXorVerifier1(password);
                cur.insertAttributeWithValue(getAttrName(prefix, "password"),
                        String.format(Locale.ROOT, "%04X", hash).toUpperCase(Locale.ROOT));
            } else {
                byte[] salt = RandomSingleton.getInstance().generateSeed(16);

                // Iterations specifies the number of times the hashing function shall be iteratively run (using each
                // iteration's result as the input for the next iteration).
                int spinCount = 100000;

                // Implementation Notes List:
                // --> In this third stage, the reversed byte order legacy hash from the second stage shall
                //     be converted to Unicode hex string representation
                byte[] hash = CryptoFunctions.hashPassword(password, hashAlgo, salt, spinCount, false);

                Base64.Encoder enc64 = Base64.getEncoder();

                cur.insertAttributeWithValue(getAttrName(prefix, "algorithmName"), hashAlgo.jceId);
                cur.insertAttributeWithValue(getAttrName(prefix, "hashValue"), enc64.encodeToString(hash));
                cur.insertAttributeWithValue(getAttrName(prefix, "saltValue"), enc64.encodeToString(salt));
                cur.insertAttributeWithValue(getAttrName(prefix, "spinCount"), ""+spinCount);
            }
        }
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
        // TODO: is "velvetSweatshop" the default password?
        if (password == null) return false;

        try (final XmlCursor cur = xobj.newCursor()) {
            String xorHashVal = cur.getAttributeText(getAttrName(prefix, "password"));
            String algoName = cur.getAttributeText(getAttrName(prefix, "algorithmName"));
            String hashVal = cur.getAttributeText(getAttrName(prefix, "hashValue"));
            String saltVal = cur.getAttributeText(getAttrName(prefix, "saltValue"));
            String spinCount = cur.getAttributeText(getAttrName(prefix, "spinCount"));
            if (xorHashVal != null) {
                int hash1 = Integer.parseInt(xorHashVal, 16);
                int hash2 = CryptoFunctions.createXorVerifier1(password);
                return hash1 == hash2;
            } else {
                if (hashVal == null || algoName == null || saltVal == null || spinCount == null) {
                    return false;
                }

                Base64.Decoder dec64 = Base64.getDecoder();

                byte[] hash1 = dec64.decode(hashVal);
                HashAlgorithm hashAlgo = HashAlgorithm.fromString(algoName);
                byte[] salt = dec64.decode(saltVal);
                int spinCnt = Integer.parseInt(spinCount);
                byte[] hash2 = CryptoFunctions.hashPassword(password, hashAlgo, salt, spinCnt, false);
                return Arrays.equals(hash1, hash2);
            }
        }
    }
    
    
    private static QName getAttrName(String prefix, String name) {
        if (prefix == null || prefix.isEmpty()) {
            return new QName(name);
        } else {
            return new QName(prefix + StringUtil.toUpperCase(name.charAt(0)) + name.substring(1));
        }
    }
}
