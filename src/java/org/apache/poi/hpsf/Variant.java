/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hpsf;

/**
 * <p>The <em>Variant</em> types as defined by Microsoft's COM. I
 * found this information in <a
 * href="http://www.marin.clara.net/COM/variant_type_definitions.htm">
 * http://www.marin.clara.net/COM/variant_type_definitions.htm</a>.</p>
 *
 * <p>In the variant types descriptions the following shortcuts are
 * used: <strong> [V]</strong> - may appear in a VARIANT,
 * <strong>[T]</strong> - may appear in a TYPEDESC,
 * <strong>[P]</strong> - may appear in an OLE property set,
 * <strong>[S]</strong> - may appear in a Safe Array.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class Variant
{

    /**
     * <p>[V][P] Nothing, i.e. not a single byte of data.</p>
     */
    public static final int VT_EMPTY = 0;

    /**
     * <p>[V][P] SQL style Null.</p>
     */
    public static final int VT_NULL = 1;

    /**
     * <p>[V][T][P][S] 2 byte signed int.</p>
     */
    public static final int VT_I2 = 2;

    /**
     * <p>[V][T][P][S] 4 byte signed int.</p>
     */
    public static final int VT_I4 = 3;

    /**
     * <p>[V][T][P][S] 4 byte real.</p>
     */
    public static final int VT_R4 = 4;

    /**
     * <p>[V][T][P][S] 8 byte real.</p>
     */
    public static final int VT_R8 = 5;

    /**
     * <p>[V][T][P][S] currency. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CY = 6;

    /**
     * <p>[V][T][P][S] date. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_DATE = 7;

    /**
     * <p>[V][T][P][S] OLE Automation string. <span
     * style="background-color: #ffff00">How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_BSTR = 8;

    /**
     * <p>[V][T][P][S] IDispatch *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_DISPATCH = 9;

    /**
     * <p>[V][T][S] SCODE. <span style="background-color: #ffff00">How
     * long is this? How is it to be interpreted?</span></p>
     */
    public static final int VT_ERROR = 10;

    /**
     * <p>[V][T][P][S] True=-1, False=0.</p>
     */
    public static final int VT_BOOL = 11;

    /**
     * <p>[V][T][P][S] VARIANT *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_VARIANT = 12;

    /**
     * <p>[V][T][S] IUnknown *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_UNKNOWN = 13;

    /**
     * <p>[V][T][S] 16 byte fixed point.</p>
     */
    public static final int VT_DECIMAL = 14;

    /**
     * <p>[T] signed char.</p>
     */
    public static final int VT_I1 = 16;

    /**
     * <p>[V][T][P][S] unsigned char.</p>
     */
    public static final int VT_UI1 = 17;

    /**
     * <p>[T][P] unsigned short.</p>
     */
    public static final int VT_UI2 = 18;

    /**
     * <p>[T][P] unsigned int.</p>
     */
    public static final int VT_UI4 = 19;

    /**
     * <p>[T][P] signed 64-bit int.</p>
     */
    public static final int VT_I8 = 20;

    /**
     * <p>[T][P] unsigned 64-bit int.</p>
     */
    public static final int VT_UI8 = 21;

    /**
     * <p>[T] signed machine int.</p>
     */
    public static final int VT_INT = 22;

    /**
     * <p>[T] unsigned machine int.</p>
     */
    public static final int VT_UINT = 23;

    /**
     * <p>[T] C style void.</p>
     */
    public static final int VT_VOID = 24;

    /**
     * <p>[T] Standard return type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_HRESULT = 25;

    /**
     * <p>[T] pointer type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_PTR = 26;

    /**
     * <p>[T] (use VT_ARRAY in VARIANT).</p>
     */
    public static final int VT_SAFEARRAY = 27;

    /**
     * <p>[T] C style array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CARRAY = 28;

    /**
     * <p>[T] user defined type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_USERDEFINED = 29;

    /**
     * <p>[T][P] null terminated string.</p>
     */
    public static final int VT_LPSTR = 30;

    /**
     * <p>[T][P] wide (Unicode) null terminated string.</p>
     */
    public static final int VT_LPWSTR = 31;

    /**
     * <p>[P] FILETIME. The FILETIME structure holds a date and time
     * associated with a file. The structure identifies a 64-bit
     * integer specifying the number of 100-nanosecond intervals which
     * have passed since January 1, 1601. This 64-bit value is split
     * into the two dwords stored in the structure.</p>
     */
    public static final int VT_FILETIME = 64;

    /**
     * <p>[P] Length prefixed bytes.</p>
     */
    public static final int VT_BLOB = 65;

    /**
     * <p>[P] Name of the stream follows.</p>
     */
    public static final int VT_STREAM = 66;

    /**
     * <p>[P] Name of the storage follows.</p>
     */
    public static final int VT_STORAGE = 67;

    /**
     * <p>[P] Stream contains an object. <span
     * style="background-color: #ffff00"> How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_STREAMED_OBJECT = 68;

    /**
     * <p>[P] Storage contains an object. <span
     * style="background-color: #ffff00"> How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_STORED_OBJECT = 69;

    /**
     * <p>[P] Blob contains an object. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_BLOB_OBJECT = 70;

    /**
     * <p>[P] Clipboard format. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CF = 71;

    /**
     * <p>[P] A Class ID.</p>
     *
     * <p>It consists of a 32 bit unsigned integer indicating the size
     * of the structure, a 32 bit signed integer indicating (Clipboard
     * Format Tag) indicating the type of data that it contains, and
     * then a byte array containing the data.</p>
     *
     * <p>The valid Clipboard Format Tags are:</p>
     *
     * <ul>
     *  <li>{@link Thumbnail#CFTAG_WINDOWS}</li>
     *  <li>{@link Thumbnail#CFTAG_MACINTOSH}</li>
     *  <li>{@link Thumbnail#CFTAG_NODATA}</li>
     *  <li>{@link Thumbnail#CFTAG_FMTID}</li>
     * </ul>
     *
     * <pre>typedef struct tagCLIPDATA {
     * // cbSize is the size of the buffer pointed to
     * // by pClipData, plus sizeof(ulClipFmt)
     * ULONG              cbSize;
     * long               ulClipFmt;
     * BYTE*              pClipData;
     * } CLIPDATA;</pre>
     *
     * <p>See <a
     * href="msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp"
     * target="_blank">
     * msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp</a>.</p>
     */
    public static final int VT_CLSID = 72;

    /**
     * <p>[P] simple counted array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_VECTOR = 0x1000;

    /**
     * <p>[V] SAFEARRAY*. <span style="background-color: #ffff00">How
     * long is this? How is it to be interpreted?</span></p>
     */
    public static final int VT_ARRAY = 0x2000;

    /**
     * <p>[V] void* for local use. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_BYREF = 0x4000;

    /**
     * <p>FIXME: Document this!</p>
     */
    public static final int VT_RESERVED = 0x8000;

    /**
     * <p>FIXME: Document this!</p>
     */
    public static final int VT_ILLEGAL = 0xFFFF;

    /**
     * <p>FIXME: Document this!</p>
     */
    public static final int VT_ILLEGALMASKED = 0xFFF;

    /**
     * <p>FIXME: Document this!</p>
     */
    public static final int VT_TYPEMASK = 0xFFF;

}