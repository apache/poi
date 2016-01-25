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
package org.apache.poi.hssf.eventusermodel.old;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public final class StringValue implements IRecordValue {

    private final String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public Date asDate(DateFormat df) throws ParseException {
        return df.parse(value);
    }

    @Override
    public double asDouble() {
        return Double.parseDouble(normalizeNumericValue(value));
    }

    @Override
    public long asLong() {
        return Long.parseLong(normalizeNumericValue(value));
    }

    @Override
    public String asString() {
        return value;
    }

    private static String normalizeNumericValue(String value) {
        StringBuilder buff = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ',') {
                if (buff == null) {
                    buff = new StringBuilder();
                    buff.append(value, 0, i);
                    buff.append('.');
                }
            } else if (Character.isWhitespace(c)) {
                if (buff == null) {
                    buff = new StringBuilder();
                    buff.append(value, 0, i);
                    buff.append('.');
                }
            } else {
                if (buff != null) {
                    buff.append(c);
                }
            }
        }
        if (buff != null) {
            return buff.toString();
        } else {
            return value;
        }
    }

    @Override
    public String toString() {
        return "StringValue(" + value + ')';
    }
}
