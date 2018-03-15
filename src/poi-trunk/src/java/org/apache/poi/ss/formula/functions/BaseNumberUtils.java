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
package org.apache.poi.ss.formula.functions;

/**
 * <p>Some utils for converting from and to any base<p>
 *
 * @author cedric dot walter @ gmail dot com
 */
public class BaseNumberUtils {


    public static double convertToDecimal(String value, int base, int maxNumberOfPlaces) throws IllegalArgumentException {
        if (value == null || value.length() == 0) {
            return 0.0;
        }

        long stringLength = value.length();
        if (stringLength > maxNumberOfPlaces) {
            throw new IllegalArgumentException();
        }

        double decimalValue = 0.0;

        long signedDigit = 0;
        boolean hasSignedDigit = true;
        char[] characters = value.toCharArray();
        for (char character : characters) {
            long digit;

            if ('0' <= character && character <= '9') {
                digit = character - '0';
            } else if ('A' <= character && character <= 'Z') {
                digit = 10 + (character - 'A');
            } else if ('a' <= character && character <= 'z') {
                digit = 10 + (character - 'a');
            } else {
                digit = base;
            }

            if (digit < base) {
                if (hasSignedDigit) {
                    hasSignedDigit = false;
                    signedDigit = digit;
                }
                decimalValue = decimalValue * base + digit;
            } else {
                throw new IllegalArgumentException("character not allowed");
            }
        }

        boolean isNegative = (!hasSignedDigit && stringLength == maxNumberOfPlaces  && (signedDigit >= base / 2));
        if (isNegative) {
            decimalValue = getTwoComplement(base, maxNumberOfPlaces, decimalValue);
            decimalValue = decimalValue * -1.0;
        }

        return decimalValue;
    }

    private static double getTwoComplement(double base, double maxNumberOfPlaces, double decimalValue) {
        return (Math.pow(base, maxNumberOfPlaces) - decimalValue);
    }
}
