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

package org.apache.poi.hpsf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Variant} types as defined by Microsoft's COM.
 *
 * In the variant types descriptions the following shortcuts are used:
 * <ul>
 * <li>[V] - may appear in a VARIANT
 * <li>[T] - may appear in a TYPEDESC
 * <li>[P] - may appear in an OLE property set
 * <li>[S] - may appear in a Safe Array
 * </ul>
 */
public class Variant
{

    /**
     * [V][P] Nothing, i.e. not a single byte of data.
     */
    public static final int VT_EMPTY = 0;

    /**
     * [V][P] SQL style Null.
     */
    public static final int VT_NULL = 1;

    /**
     * [V][T][P][S] 2 byte signed int.
     */
    public static final int VT_I2 = 2;

    /**
     * [V][T][P][S] 4 byte signed int.
     */
    public static final int VT_I4 = 3;

    /**
     * [V][T][P][S] 4 byte real.
     */
    public static final int VT_R4 = 4;

    /**
     * [V][T][P][S] 8 byte real.
     */
    public static final int VT_R8 = 5;

    /**
     * [V][T][P][S] currency.
     */
    public static final int VT_CY = 6;

    /**
     * [V][T][P][S] date.
     */
    public static final int VT_DATE = 7;

    /**
     * [V][T][P][S] OLE Automation string.
     */
    public static final int VT_BSTR = 8;

    /**
     * [V][T][P][S] IDispatch
     */
    public static final int VT_DISPATCH = 9;

    /**
     * [V][T][S] SCODE
     */
    public static final int VT_ERROR = 10;

    /**
     * [V][T][P][S] True=-1, False=0.
     */
    public static final int VT_BOOL = 11;

    /**
     * [V][T][P][S] VARIANT
     */
    public static final int VT_VARIANT = 12;

    /**
     * [V][T][S] IUnknown
     */
    public static final int VT_UNKNOWN = 13;

    /**
     * [V][T][S] 16 byte fixed point.
     */
    public static final int VT_DECIMAL = 14;

    /**
     * [T] signed char.
     */
    public static final int VT_I1 = 16;

    /**
     * [V][T][P][S] unsigned char.
     */
    public static final int VT_UI1 = 17;

    /**
     * [T][P] unsigned short.
     */
    public static final int VT_UI2 = 18;

    /**
     * [T][P] unsigned int.
     */
    public static final int VT_UI4 = 19;

    /**
     * [T][P] signed 64-bit int.
     */
    public static final int VT_I8 = 20;

    /**
     * [T][P] unsigned 64-bit int.
     */
    public static final int VT_UI8 = 21;

    /**
     * [T] signed machine int.
     */
    public static final int VT_INT = 22;

    /**
     * [T] unsigned machine int.
     */
    public static final int VT_UINT = 23;

    /**
     * [T] C style void.
     */
    public static final int VT_VOID = 24;

    /**
     * [T] Standard return type.
     */
    public static final int VT_HRESULT = 25;

    /**
     * [T] pointer type.
     */
    public static final int VT_PTR = 26;

    /**
     * [T] (use VT_ARRAY in VARIANT).
     */
    public static final int VT_SAFEARRAY = 27;

    /**
     * [T] C style array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span>
     */
    public static final int VT_CARRAY = 28;

    /**
     * [T] user defined type.
     */
    public static final int VT_USERDEFINED = 29;

    /**
     * [T][P] null terminated string.
     */
    public static final int VT_LPSTR = 30;

    /**
     * [T][P] wide (Unicode) null terminated string.
     */
    public static final int VT_LPWSTR = 31;

    /**
     * [P] FILETIME. The FILETIME structure holds a date and time
     * associated with a file. The structure identifies a 64-bit
     * integer specifying the number of 100-nanosecond intervals which
     * have passed since January 1, 1601. This 64-bit value is split
     * into the two dwords stored in the structure.
     */
    public static final int VT_FILETIME = 64;

    /**
     * [P] Length prefixed bytes.
     */
    public static final int VT_BLOB = 65;

    /**
     * [P] Name of the stream follows.
     */
    public static final int VT_STREAM = 66;

    /**
     * [P] Name of the storage follows.
     */
    public static final int VT_STORAGE = 67;

    /**
     * [P] Stream contains an object.
     */
    public static final int VT_STREAMED_OBJECT = 68;

    /**
     * [P] Storage contains an object.
     */
    public static final int VT_STORED_OBJECT = 69;

    /**
     * [P] Blob contains an object.
     */
    public static final int VT_BLOB_OBJECT = 70;

    /**
     * [P] Clipboard format.
     */
    public static final int VT_CF = 71;

    /**
     * [P] A Class ID.<p>
     *
     * It consists of a 32 bit unsigned integer indicating the size
     * of the structure, a 32 bit signed integer indicating (Clipboard
     * Format Tag) indicating the type of data that it contains, and
     * then a byte array containing the data.<p>
     *
     * The valid Clipboard Format Tags are:
     *
     * <ul>
     *  <li>{@link Thumbnail#CFTAG_WINDOWS}
     *  <li>{@link Thumbnail#CFTAG_MACINTOSH}
     *  <li>{@link Thumbnail#CFTAG_NODATA}
     *  <li>{@link Thumbnail#CFTAG_FMTID}
     * </ul>
     *
     * <pre>{@code
     * typedef struct tagCLIPDATA {
     * // cbSize is the size of the buffer pointed to
     * // by pClipData, plus sizeof(ulClipFmt)
     * ULONG              cbSize;
     * long               ulClipFmt;
     * BYTE*              pClipData;
     * } CLIPDATA;}</pre>
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa380072(v=vs.85).aspx">PROPVARIANT structure</a>
     */
    public static final int VT_CLSID = 72;

    /**
     * "MUST be a VersionedStream. The storage representing the (non-simple)
     * property set MUST have a stream element with the name in the StreamName
     * field." -- [MS-OLEPS] -- v20110920; Object Linking and Embedding (OLE)
     * Property Set Data Structures; page 24 / 63
     */
    public static final int VT_VERSIONED_STREAM = 0x0049;

    /**
     * [P] simple counted array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span>
     */
    public static final int VT_VECTOR = 0x1000;

    /**
     * [V] SAFEARRAY*. <span style="background-color: #ffff00">How
     * long is this? How is it to be interpreted?</span>
     */
    public static final int VT_ARRAY = 0x2000;

    /**
     * [V] void* for local use. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span>
     */
    public static final int VT_BYREF = 0x4000;

    public static final int VT_RESERVED = 0x8000;

    public static final int VT_ILLEGAL = 0xFFFF;

    public static final int VT_ILLEGALMASKED = 0xFFF;

    public static final int VT_TYPEMASK = 0xFFF;



    /**
     * Maps the numbers denoting the variant types to their corresponding
     * variant type names.
     */
    private static final Map<Long,String> numberToName;

    private static final Map<Long,Integer> numberToLength;

    /**
     * Denotes a variant type with a length that is unknown to HPSF yet.
     */
    public static final Integer LENGTH_UNKNOWN = -2;

    /**
     * Denotes a variant type with a variable length.
     */
    public static final Integer LENGTH_VARIABLE = -1;

    /**
     * Denotes a variant type with a length of 0 bytes.
     */
    public static final Integer LENGTH_0 = 0;

    /**
     * Denotes a variant type with a length of 2 bytes.
     */
    public static final Integer LENGTH_2 = 2;

    /**
     * Denotes a variant type with a length of 4 bytes.
     */
    public static final Integer LENGTH_4 = 4;

    /**
     * Denotes a variant type with a length of 8 bytes.
     */
    public static final Integer LENGTH_8 = 8;

    private static final Object[][] NUMBER_TO_NAME_LIST = {
            {0L, "VT_EMPTY", LENGTH_0},
            {1L, "VT_NULL", LENGTH_UNKNOWN},
            {2L, "VT_I2", LENGTH_2},
            {3L, "VT_I4", LENGTH_4},
            {4L, "VT_R4", LENGTH_4},
            {5L, "VT_R8", LENGTH_8},
            {6L, "VT_CY", LENGTH_UNKNOWN},
            {7L, "VT_DATE", LENGTH_UNKNOWN},
            {8L, "VT_BSTR", LENGTH_UNKNOWN},
            {9L, "VT_DISPATCH", LENGTH_UNKNOWN},
            {10L, "VT_ERROR", LENGTH_UNKNOWN},
            {11L, "VT_BOOL", LENGTH_UNKNOWN},
            {12L, "VT_VARIANT", LENGTH_UNKNOWN},
            {13L, "VT_UNKNOWN", LENGTH_UNKNOWN},
            {14L, "VT_DECIMAL", LENGTH_UNKNOWN},
            {16L, "VT_I1", LENGTH_UNKNOWN},
            {17L, "VT_UI1", LENGTH_UNKNOWN},
            {18L, "VT_UI2", LENGTH_UNKNOWN},
            {19L, "VT_UI4", LENGTH_UNKNOWN},
            {20L, "VT_I8", LENGTH_UNKNOWN},
            {21L, "VT_UI8", LENGTH_UNKNOWN},
            {22L, "VT_INT", LENGTH_UNKNOWN},
            {23L, "VT_UINT", LENGTH_UNKNOWN},
            {24L, "VT_VOID", LENGTH_UNKNOWN},
            {25L, "VT_HRESULT", LENGTH_UNKNOWN},
            {26L, "VT_PTR", LENGTH_UNKNOWN},
            {27L, "VT_SAFEARRAY", LENGTH_UNKNOWN},
            {28L, "VT_CARRAY", LENGTH_UNKNOWN},
            {29L, "VT_USERDEFINED", LENGTH_UNKNOWN},
            {30L, "VT_LPSTR", LENGTH_VARIABLE},
            {31L, "VT_LPWSTR", LENGTH_UNKNOWN},
            {64L, "VT_FILETIME", LENGTH_8},
            {65L, "VT_BLOB", LENGTH_UNKNOWN},
            {66L, "VT_STREAM", LENGTH_UNKNOWN},
            {67L, "VT_STORAGE", LENGTH_UNKNOWN},
            {68L, "VT_STREAMED_OBJECT", LENGTH_UNKNOWN},
            {69L, "VT_STORED_OBJECT", LENGTH_UNKNOWN},
            {70L, "VT_BLOB_OBJECT", LENGTH_UNKNOWN},
            {71L, "VT_CF", LENGTH_UNKNOWN},
            {72L, "VT_CLSID", LENGTH_UNKNOWN}
    };

    /* Initialize the number-to-name and number-to-length map: */
    static {
        Map<Long,String> number2Name = new HashMap<>(NUMBER_TO_NAME_LIST.length, 1.0F);
        Map<Long,Integer> number2Len = new HashMap<>(NUMBER_TO_NAME_LIST.length, 1.0F);

        for (Object[] nn : NUMBER_TO_NAME_LIST) {
            number2Name.put((Long)nn[0], (String)nn[1]);
            number2Len.put((Long)nn[0], (Integer)nn[2]);
        }
        numberToName = Collections.unmodifiableMap(number2Name);
        numberToLength = Collections.unmodifiableMap(number2Len);
    }

    /**
     * Returns the variant type name associated with a variant type
     * number.
     *
     * @param variantType The variant type number
     * @return The variant type name or the string "unknown variant type"
     */
    public static String getVariantName(final long variantType) {
        long vt = variantType;
        String name = "";
        if ((vt & VT_VECTOR) != 0) {
            name = "Vector of ";
            vt -= VT_VECTOR;
        } else if ((vt & VT_ARRAY) != 0) {
            name = "Array of ";
            vt -= VT_ARRAY;
        } else if ((vt & VT_BYREF) != 0) {
            name = "ByRef of ";
            vt -= VT_BYREF;
        }
        
        name += numberToName.get(vt);
        return !name.isEmpty() ? name : "unknown variant type";
    }

    /**
     * Returns a variant type's length.
     *
     * @param variantType The variant type number
     * @return The length of the variant type's data in bytes. If the length is
     * variable, i.e. the length of a string, -1 is returned. If HPSF does not
     * know the length, -2 is returned. The latter usually indicates an
     * unsupported variant type.
     */
    public static int getVariantLength(final long variantType) {
        final Integer length = numberToLength.get(variantType);
        return (length != null) ? length : LENGTH_UNKNOWN;
    }

}
