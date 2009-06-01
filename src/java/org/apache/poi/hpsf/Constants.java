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

/**
 * <p>Defines constants of general use.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class Constants
{
    /** <p>Codepage 037, a special case</p> */
    public static final int CP_037 = 37;

    /** <p>Codepage for SJIS</p> */
    public static final int CP_SJIS = 932;

    /** <p>Codepage for GBK, aka MS936</p> */
    public static final int CP_GBK = 936;

    /** <p>Codepage for MS949</p> */
    public static final int CP_MS949 = 949;

    /** <p>Codepage for UTF-16</p> */
    public static final int CP_UTF16 = 1200;

    /** <p>Codepage for UTF-16 big-endian</p> */
    public static final int CP_UTF16_BE = 1201;

    /** <p>Codepage for Windows 1250</p> */
    public static final int CP_WINDOWS_1250 = 1250;

    /** <p>Codepage for Windows 1251</p> */
    public static final int CP_WINDOWS_1251 = 1251;

    /** <p>Codepage for Windows 1252</p> */
    public static final int CP_WINDOWS_1252 = 1252;

    /** <p>Codepage for Windows 1253</p> */
    public static final int CP_WINDOWS_1253 = 1253;

    /** <p>Codepage for Windows 1254</p> */
    public static final int CP_WINDOWS_1254 = 1254;

    /** <p>Codepage for Windows 1255</p> */
    public static final int CP_WINDOWS_1255 = 1255;

    /** <p>Codepage for Windows 1256</p> */
    public static final int CP_WINDOWS_1256 = 1256;

    /** <p>Codepage for Windows 1257</p> */
    public static final int CP_WINDOWS_1257 = 1257;

    /** <p>Codepage for Windows 1258</p> */
    public static final int CP_WINDOWS_1258 = 1258;

    /** <p>Codepage for Johab</p> */
    public static final int CP_JOHAB = 1361;

    /** <p>Codepage for Macintosh Roman (Java: MacRoman)</p> */
    public static final int CP_MAC_ROMAN = 10000;

    /** <p>Codepage for Macintosh Japan (Java: unknown - use SJIS, cp942 or
     * cp943)</p> */
    public static final int CP_MAC_JAPAN = 10001;

    /** <p>Codepage for Macintosh Chinese Traditional (Java: unknown - use Big5,
     * MS950, or cp937)</p> */
    public static final int CP_MAC_CHINESE_TRADITIONAL = 10002;

    /** <p>Codepage for Macintosh Korean (Java: unknown - use EUC_KR or
     * cp949)</p> */
    public static final int CP_MAC_KOREAN = 10003;

    /** <p>Codepage for Macintosh Arabic (Java: MacArabic)</p> */
    public static final int CP_MAC_ARABIC = 10004;

    /** <p>Codepage for Macintosh Hebrew (Java: MacHebrew)</p> */
    public static final int CP_MAC_HEBREW = 10005;

    /** <p>Codepage for Macintosh Greek (Java: MacGreek)</p> */
    public static final int CP_MAC_GREEK = 10006;

    /** <p>Codepage for Macintosh Cyrillic (Java: MacCyrillic)</p> */
    public static final int CP_MAC_CYRILLIC = 10007;

    /** <p>Codepage for Macintosh Chinese Simplified (Java: unknown - use
     * EUC_CN, ISO2022_CN_GB, MS936 or cp935)</p> */
    public static final int CP_MAC_CHINESE_SIMPLE = 10008;

    /** <p>Codepage for Macintosh Romanian (Java: MacRomania)</p> */
    public static final int CP_MAC_ROMANIA = 10010;

    /** <p>Codepage for Macintosh Ukrainian (Java: MacUkraine)</p> */
    public static final int CP_MAC_UKRAINE = 10017;

    /** <p>Codepage for Macintosh Thai (Java: MacThai)</p> */
    public static final int CP_MAC_THAI = 10021;

    /** <p>Codepage for Macintosh Central Europe (Latin-2)
     * (Java: MacCentralEurope)</p> */
    public static final int CP_MAC_CENTRAL_EUROPE = 10029;

    /** <p>Codepage for Macintosh Iceland (Java: MacIceland)</p> */
    public static final int CP_MAC_ICELAND = 10079;

    /** <p>Codepage for Macintosh Turkish (Java: MacTurkish)</p> */
    public static final int CP_MAC_TURKISH = 10081;

    /** <p>Codepage for Macintosh Croatian (Java: MacCroatian)</p> */
    public static final int CP_MAC_CROATIAN = 10082;

    /** <p>Codepage for US-ASCII</p> */
    public static final int CP_US_ACSII = 20127;

    /** <p>Codepage for KOI8-R</p> */
    public static final int CP_KOI8_R = 20866;

    /** <p>Codepage for ISO-8859-1</p> */
    public static final int CP_ISO_8859_1 = 28591;

    /** <p>Codepage for ISO-8859-2</p> */
    public static final int CP_ISO_8859_2 = 28592;

    /** <p>Codepage for ISO-8859-3</p> */
    public static final int CP_ISO_8859_3 = 28593;

    /** <p>Codepage for ISO-8859-4</p> */
    public static final int CP_ISO_8859_4 = 28594;

    /** <p>Codepage for ISO-8859-5</p> */
    public static final int CP_ISO_8859_5 = 28595;

    /** <p>Codepage for ISO-8859-6</p> */
    public static final int CP_ISO_8859_6 = 28596;

    /** <p>Codepage for ISO-8859-7</p> */
    public static final int CP_ISO_8859_7 = 28597;

    /** <p>Codepage for ISO-8859-8</p> */
    public static final int CP_ISO_8859_8 = 28598;

    /** <p>Codepage for ISO-8859-9</p> */
    public static final int CP_ISO_8859_9 = 28599;

    /** <p>Codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP1 = 50220;

    /** <p>Another codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP2 = 50221;

    /** <p>Yet another codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP3 = 50222;

    /** <p>Codepage for ISO-2022-KR</p> */
    public static final int CP_ISO_2022_KR = 50225;

    /** <p>Codepage for EUC-JP</p> */
    public static final int CP_EUC_JP = 51932;

    /** <p>Codepage for EUC-KR</p> */
    public static final int CP_EUC_KR = 51949;

    /** <p>Codepage for GB2312</p> */
    public static final int CP_GB2312 = 52936;

    /** <p>Codepage for GB18030</p> */
    public static final int CP_GB18030 = 54936;

    /** <p>Another codepage for US-ASCII</p> */
    public static final int CP_US_ASCII2 = 65000;

    /** <p>Codepage for UTF-8</p> */
    public static final int CP_UTF8 = 65001;

    /** <p>Codepage for Unicode</p> */
    public static final int CP_UNICODE = CP_UTF16;
}
