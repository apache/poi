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

package org.apache.poi.hsmf.datatypes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The types list and details are available from
 * http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertytype%28v=EXCHG.140%29.aspx
 */
public final class Types {
    private static final Map<Integer, MAPIType> builtInTypes = new HashMap<>();
    private static final Map<Integer, MAPIType> customTypes = new HashMap<>();

    /** Unspecified */
    public static final MAPIType UNSPECIFIED = new MAPIType(0x0000,
            "Unspecified", -1);
    /** Unknown */
    public static final MAPIType UNKNOWN = new MAPIType(-1, "Unknown", -1);

    /** Null - NULL property value */
    public static final MAPIType NULL = new MAPIType(0x0001, "Null", 0);
    /** I2 - signed 16-bit value */
    public static final MAPIType SHORT = new MAPIType(0x0002, "Short", 2);
    /** Long - signed 32-bit value */
    public static final MAPIType LONG = new MAPIType(0x0003, "Long", 4);
    /** R4 - 4-byte floating point value */
    public static final MAPIType FLOAT = new MAPIType(0x0004, "Float", 4);
    /** Double - floating point double */
    public static final MAPIType DOUBLE = new MAPIType(0x0005, "Double", 8);
    /**
     * Currency - signed 64-bit integer that represents a base ten decimal with
     * four digits to the right of the decimal point
     */
    public static final MAPIType CURRENCY = new MAPIType(0x0006, "Currency", 8);
    /** AppTime - application time value */
    public static final MAPIType APP_TIME = new MAPIType(0x0007, "Application Time", 8);
    /** Error - 32-bit error value */
    public static final MAPIType ERROR = new MAPIType(0x000A, "Error", 4);
    /** Boolean - 16-bit Boolean value. '0' is false. Non-zero is true */
    public static final MAPIType BOOLEAN = new MAPIType(0x000B, "Boolean", 2);
    /** Object/Directory - embedded object in a property */
    public static final MAPIType DIRECTORY = new MAPIType(0x000D, "Directory", -1);
    /** I8 - 8-byte signed integer */
    public static final MAPIType LONG_LONG = new MAPIType(0x0014, "Long Long", 8);
    /**
     * SysTime - FILETIME 64-bit integer specifying the number of 100ns periods
     * since Jan 1, 1601
     */
    public static final MAPIType TIME = new MAPIType(0x0040, "Time", 8);
    /** ClassId - OLE GUID */
    public static final MAPIType CLS_ID = new MAPIType(0x0048, "CLS ID GUID", 16);

    /** Binary - counted byte array */
    public static final MAPIType BINARY = new MAPIType(0x0102, "Binary", -1);

    /**
     * An 8-bit string, probably in CP1252, but don't quote us... Normally used
     * for everything before Outlook 3.0, and some fields in Outlook 3.0.
     */
    public static final MAPIType ASCII_STRING = new MAPIType(0x001E, "ASCII String", -1);
    /** A string, from Outlook 3.0 onwards. Normally unicode */
    public static final MAPIType UNICODE_STRING = new MAPIType(0x001F, "Unicode String", -1);

    /** MultiValued - Value part contains multiple values */
    public static final int MULTIVALUED_FLAG = 0x1000;

    public static final class MAPIType {
        private final int id;
        private final String name;
        private final int length;

        /**
         * Creates a standard, built-in type
         */
        private MAPIType(int id, String name, int length) {
            this.id = id;
            this.name = name;
            this.length = length;
            builtInTypes.put(id, this);
        }

        /**
         * Creates a custom type
         */
        private MAPIType(int id, int length) {
            this.id = id;
            this.name = asCustomName(id);
            this.length = length;
            customTypes.put(id, this);
        }

        /**
         * Returns the length, in bytes, of values of this type, or -1 if it is
         * a variable length type.
         */
        public int getLength() {
            return length;
        }

        /**
         * Is this type a fixed-length type, or a variable-length one?
         */
        public boolean isFixedLength() {
            return ((length != -1) && (length <= 8)) || (id == Types.CLS_ID.id);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return id + " / 0x" + asFileEnding() + " - " + name + " @ " + length;
        }

        /**
         * Return the 4 character hex encoded version, as used in file endings
         */
        public String asFileEnding() {
            return Types.asFileEnding(id);
        }
    }

    public static MAPIType getById(int typeId) {
        return builtInTypes.get(typeId);
    }

    public static String asFileEnding(int type) {
        String str = Integer.toHexString(type).toUpperCase(Locale.ROOT);
        while (str.length() < 4) {
            str = "0" + str;
        }
        return str;
    }

    public static String asName(int typeId) {
        MAPIType type = builtInTypes.get(typeId);
        if (type != null) {
            return type.name;
        }
        return asCustomName(typeId);
    }

    private static String asCustomName(int typeId) {
        return "0x" + Integer.toHexString(typeId);
    }

    public static MAPIType createCustom(int typeId) {
        // Check they're not being silly, and asking for a built-in one...
        if (getById(typeId) != null) {
            return getById(typeId);
        }

        // Try to get an existing definition of this
        MAPIType type = customTypes.get(typeId);

        // If none, do a thread-safe creation
        if (type == null) {
            synchronized (customTypes) {
                type = customTypes.get(typeId);
                if (type == null) {
                    type = new MAPIType(typeId, -1);
                }
            }
        }

        return type;
    }
}
