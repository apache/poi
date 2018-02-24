/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.common;

import java.util.HashMap;
import java.util.Iterator;
import java.nio.charset.Charset;

public class EncodingMap
{
    public static String getJava2IANAMapping ( String java )
    {
        String iana = (String) _java_to_iana.get( java.toUpperCase() );
        if (iana != null)
            return iana;
        // Try to use the information in the JDK to see if it is an encoding it supports
        if (Charset.isSupported( java ))
        {
            try
            {
                iana = Charset.forName( java ).name();
                return iana;
            }
            catch (IllegalArgumentException iae)
            {
                return null;
            }
        }
        return null;
    }
    
    public static String getIANA2JavaMapping ( String iana )
    {
        String java = (String) _iana_to_java.get( iana.toUpperCase() );
        if (java != null)
            return java;
        else if (Charset.isSupported( iana ))
            return iana;
        else
            return null;
    }

    private EncodingMap ( ) { }
    
    private final static HashMap _iana_to_java = new HashMap();
    private final static HashMap _java_to_iana = new HashMap();

    private final static void addMapping (
        String java, String iana, boolean isDefault )
    {
        assert !_iana_to_java.containsKey( iana );
        assert java.toUpperCase().equals( java );
        assert iana.toUpperCase().equals( iana );
        
        _iana_to_java.put( iana, java );

        if (isDefault)
        {
            assert !_java_to_iana.containsKey( java );
            _java_to_iana.put( java, iana );
        }
    }

    private final static boolean completeMappings ( )
    {
        HashMap m = new HashMap();

        for ( Iterator i = _iana_to_java.keySet().iterator() ; i.hasNext() ; )
            m.put( _iana_to_java.get( i.next() ), null );

        for ( Iterator i = m.keySet().iterator() ; i.hasNext() ; )
        {
            Object k = i.next();
            assert _java_to_iana.containsKey( k ): k;
        }

        return true;
    }

    static
    {
        addMapping( "ASCII",         "ANSI_X3.4-1986",         false );
        addMapping( "ASCII",         "ASCII",                  true  );
        addMapping( "ASCII",         "CP367",                  false );
        addMapping( "ASCII",         "CSASCII",                false );
        addMapping( "ASCII",         "IBM-367",                false );
        addMapping( "ASCII",         "IBM367",                 false );
        addMapping( "ASCII",         "ISO-IR-6",               false );
        addMapping( "ASCII",         "ISO646-US",              false );
        addMapping( "ASCII",         "ISO_646.IRV:1991",       false );
        addMapping( "ASCII",         "US",                     false );
        addMapping( "ASCII",         "US-ASCII",               false );
        addMapping( "BIG5",          "BIG5",                   true  );
        addMapping( "BIG5",          "CSBIG5",                 false );
        addMapping( "CP037",         "CP037",                  false );
        addMapping( "CP037",         "CSIBM037",               false );
        addMapping( "CP037",         "EBCDIC-CP-CA",           false );
        addMapping( "CP037",         "EBCDIC-CP-NL",           false );
        addMapping( "CP037",         "EBCDIC-CP-US",           true  );
        addMapping( "CP037",         "EBCDIC-CP-WT",           false );
        addMapping( "CP037",         "IBM-37",                 false );
        addMapping( "CP037",         "IBM037",                 false );
        addMapping( "CP1026",        "CP1026",                 false );
        addMapping( "CP1026",        "CSIBM1026",              false );
        addMapping( "CP1026",        "IBM-1026",               false );
        addMapping( "CP1026",        "IBM1026",                true  );
        addMapping( "CP1047",        "CP1047",                 false );
        addMapping( "CP1047",        "IBM-1047",               false );
        addMapping( "CP1047",        "IBM1047",                true  );
        addMapping( "CP1140",        "CCSID01140",             false );
        addMapping( "CP1140",        "CP01140",                false );
        addMapping( "CP1140",        "IBM-1140",               false );
        addMapping( "CP1140",        "IBM01140",               true  );
        addMapping( "CP1141",        "CCSID01141",             false );
        addMapping( "CP1141",        "CP01141",                false );
        addMapping( "CP1141",        "IBM-1141",               false );
        addMapping( "CP1141",        "IBM01141",               true  );
        addMapping( "CP1142",        "CCSID01142",             false );
        addMapping( "CP1142",        "CP01142",                false );
        addMapping( "CP1142",        "IBM-1142",               false );
        addMapping( "CP1142",        "IBM01142",               true  );
        addMapping( "CP1143",        "CCSID01143",             false );
        addMapping( "CP1143",        "CP01143",                false );
        addMapping( "CP1143",        "IBM-1143",               false );
        addMapping( "CP1143",        "IBM01143",               true  );
        addMapping( "CP1144",        "CCSID01144",             false );
        addMapping( "CP1144",        "CP01144",                false );
        addMapping( "CP1144",        "IBM-1144",               false );
        addMapping( "CP1144",        "IBM01144",               true  );
        addMapping( "CP1145",        "CCSID01145",             false );
        addMapping( "CP1145",        "CP01145",                false );
        addMapping( "CP1145",        "IBM-1145",               false );
        addMapping( "CP1145",        "IBM01145",               true  );
        addMapping( "CP1146",        "CCSID01146",             false );
        addMapping( "CP1146",        "CP01146",                false );
        addMapping( "CP1146",        "IBM-1146",               false );
        addMapping( "CP1146",        "IBM01146",               true  );
        addMapping( "CP1147",        "CCSID01147",             false );
        addMapping( "CP1147",        "CP01147",                false );
        addMapping( "CP1147",        "IBM-1147",               false );
        addMapping( "CP1147",        "IBM01147",               true  );
        addMapping( "CP1148",        "CCSID01148",             false );
        addMapping( "CP1148",        "CP01148",                false );
        addMapping( "CP1148",        "IBM-1148",               false );
        addMapping( "CP1148",        "IBM01148",               true  );
        addMapping( "CP1149",        "CCSID01149",             false );
        addMapping( "CP1149",        "CP01149",                false );
        addMapping( "CP1149",        "IBM-1149",               false );
        addMapping( "CP1149",        "IBM01149",               true  );
        addMapping( "CP1250",        "WINDOWS-1250",           true );
        addMapping( "CP1251",        "WINDOWS-1251",           true );
        addMapping( "CP1252",        "WINDOWS-1252",           true );
        addMapping( "CP1253",        "WINDOWS-1253",           true );
        addMapping( "CP1254",        "WINDOWS-1254",           true );
        addMapping( "CP1255",        "WINDOWS-1255",           true );
        addMapping( "CP1256",        "WINDOWS-1256",           true );
        addMapping( "CP1257",        "WINDOWS-1257",           true );
        addMapping( "CP1258",        "WINDOWS-1258",           true );
        addMapping( "CP273",         "CP273",                  false );
        addMapping( "CP273",         "CSIBM273",               false );
        addMapping( "CP273",         "IBM-273",                false );
        addMapping( "CP273",         "IBM273",                 true  );
        addMapping( "CP277",         "CP277",                  false );
        addMapping( "CP277",         "CSIBM277",               false );
        addMapping( "CP277",         "EBCDIC-CP-DK",           true  );
        addMapping( "CP277",         "EBCDIC-CP-NO",           false );
        addMapping( "CP277",         "IBM-277",                false );
        addMapping( "CP277",         "IBM277",                 false );
        addMapping( "CP278",         "CP278",                  false );
        addMapping( "CP278",         "CSIBM278",               false );
        addMapping( "CP278",         "EBCDIC-CP-FI",           true  );
        addMapping( "CP278",         "EBCDIC-CP-SE",           false );
        addMapping( "CP278",         "IBM-278",                false );
        addMapping( "CP278",         "IBM278",                 false );
        addMapping( "CP280",         "CP280",                  false );
        addMapping( "CP280",         "CSIBM280",               false );
        addMapping( "CP280",         "EBCDIC-CP-IT",           true  );
        addMapping( "CP280",         "IBM-280",                false );
        addMapping( "CP280",         "IBM280",                 false );
        addMapping( "CP284",         "CP284",                  false );
        addMapping( "CP284",         "CSIBM284",               false );
        addMapping( "CP284",         "EBCDIC-CP-ES",           true  );
        addMapping( "CP284",         "IBM-284",                false );
        addMapping( "CP284",         "IBM284",                 false );
        addMapping( "CP285",         "CP285",                  false );
        addMapping( "CP285",         "CSIBM285",               false );
        addMapping( "CP285",         "EBCDIC-CP-GB",           true  );
        addMapping( "CP285",         "IBM-285",                false );
        addMapping( "CP285",         "IBM285",                 false );
        addMapping( "CP290",         "CP290",                  false );
        addMapping( "CP290",         "CSIBM290",               false );
        addMapping( "CP290",         "EBCDIC-JP-KANA",         true  );
        addMapping( "CP290",         "IBM-290",                false );
        addMapping( "CP290",         "IBM290",                 false );
        addMapping( "CP297",         "CP297",                  false );
        addMapping( "CP297",         "CSIBM297",               false );
        addMapping( "CP297",         "EBCDIC-CP-FR",           true  );
        addMapping( "CP297",         "IBM-297",                false );
        addMapping( "CP297",         "IBM297",                 false );
        addMapping( "CP420",         "CP420",                  false );
        addMapping( "CP420",         "CSIBM420",               false );
        addMapping( "CP420",         "EBCDIC-CP-AR1",          true  );
        addMapping( "CP420",         "IBM-420",                false );
        addMapping( "CP420",         "IBM420",                 false );
        addMapping( "CP424",         "CP424",                  false );
        addMapping( "CP424",         "CSIBM424",               false );
        addMapping( "CP424",         "EBCDIC-CP-HE",           true  );
        addMapping( "CP424",         "IBM-424",                false );
        addMapping( "CP424",         "IBM424",                 false );
        addMapping( "CP437",         "437",                    false );
        addMapping( "CP437",         "CP437",                  false );
        addMapping( "CP437",         "CSPC8CODEPAGE437",       false );
        addMapping( "CP437",         "IBM-437",                false );
        addMapping( "CP437",         "IBM437",                 true  );
        addMapping( "CP500",         "CP500",                  false );
        addMapping( "CP500",         "CSIBM500",               false );
        addMapping( "CP500",         "EBCDIC-CP-BE",           false );
        addMapping( "CP500",         "EBCDIC-CP-CH",           true  );
        addMapping( "CP500",         "IBM-500",                false );
        addMapping( "CP500",         "IBM500",                 false );
        addMapping( "CP775",         "CP775",                  false );
        addMapping( "CP775",         "CSPC775BALTIC",          false );
        addMapping( "CP775",         "IBM-775",                false );
        addMapping( "CP775",         "IBM775",                 true  );
        addMapping( "CP850",         "850",                    false );
        addMapping( "CP850",         "CP850",                  false );
        addMapping( "CP850",         "CSPC850MULTILINGUAL",    false );
        addMapping( "CP850",         "IBM-850",                false );
        addMapping( "CP850",         "IBM850",                 true  );
        addMapping( "CP852",         "852",                    false );
        addMapping( "CP852",         "CP852",                  false );
        addMapping( "CP852",         "CSPCP852",               false );
        addMapping( "CP852",         "IBM-852",                false );
        addMapping( "CP852",         "IBM852",                 true  );
        addMapping( "CP855",         "855",                    false );
        addMapping( "CP855",         "CP855",                  false );
        addMapping( "CP855",         "CSIBM855",               false );
        addMapping( "CP855",         "IBM-855",                false );
        addMapping( "CP855",         "IBM855",                 true  );
        addMapping( "CP857",         "857",                    false );
        addMapping( "CP857",         "CP857",                  false );
        addMapping( "CP857",         "CSIBM857",               false );
        addMapping( "CP857",         "IBM-857",                false );
        addMapping( "CP857",         "IBM857",                 true  );
        addMapping( "CP858",         "CCSID00858",             false );
        addMapping( "CP858",         "CP00858",                false );
        addMapping( "CP858",         "IBM-858",                false );
        addMapping( "CP858",         "IBM00858",               true  );
        addMapping( "CP860",         "860",                    false );
        addMapping( "CP860",         "CP860",                  false );
        addMapping( "CP860",         "CSIBM860",               false );
        addMapping( "CP860",         "IBM-860",                false );
        addMapping( "CP860",         "IBM860",                 true  );
        addMapping( "CP861",         "861",                    false );
        addMapping( "CP861",         "CP-IS",                  false );
        addMapping( "CP861",         "CP861",                  false );
        addMapping( "CP861",         "CSIBM861",               false );
        addMapping( "CP861",         "IBM-861",                false );
        addMapping( "CP861",         "IBM861",                 true  );
        addMapping( "CP862",         "862",                    false );
        addMapping( "CP862",         "CP862",                  false );
        addMapping( "CP862",         "CSPC862LATINHEBREW",     false );
        addMapping( "CP862",         "IBM-862",                false );
        addMapping( "CP862",         "IBM862",                 true  );
        addMapping( "CP863",         "863",                    false );
        addMapping( "CP863",         "CP863",                  false );
        addMapping( "CP863",         "CSIBM863",               false );
        addMapping( "CP863",         "IBM-863",                false );
        addMapping( "CP863",         "IBM863",                 true  );
        addMapping( "CP864",         "CP864",                  false );
        addMapping( "CP864",         "CSIBM864",               false );
        addMapping( "CP864",         "IBM-864",                false );
        addMapping( "CP864",         "IBM864",                 true  );
        addMapping( "CP865",         "865",                    false );
        addMapping( "CP865",         "CP865",                  false );
        addMapping( "CP865",         "CSIBM865",               false );
        addMapping( "CP865",         "IBM-865",                false );
        addMapping( "CP865",         "IBM865",                 true  );
        addMapping( "CP866",         "866",                    false );
        addMapping( "CP866",         "CP866",                  false );
        addMapping( "CP866",         "CSIBM866",               false );
        addMapping( "CP866",         "IBM-866",                false );
        addMapping( "CP866",         "IBM866",                 true  );
        addMapping( "CP868",         "CP-AR",                  false );
        addMapping( "CP868",         "CP868",                  false );
        addMapping( "CP868",         "CSIBM868",               false );
        addMapping( "CP868",         "IBM-868",                false );
        addMapping( "CP868",         "IBM868",                 true  );
        addMapping( "CP869",         "CP-GR",                  false );
        addMapping( "CP869",         "CP869",                  false );
        addMapping( "CP869",         "CSIBM869",               false );
        addMapping( "CP869",         "IBM-869",                false );
        addMapping( "CP869",         "IBM869",                 true  );
        addMapping( "CP870",         "CP870",                  false );
        addMapping( "CP870",         "CSIBM870",               false );
        addMapping( "CP870",         "EBCDIC-CP-ROECE",        true  );
        addMapping( "CP870",         "EBCDIC-CP-YU",           false );
        addMapping( "CP870",         "IBM-870",                false );
        addMapping( "CP870",         "IBM870",                 false );
        addMapping( "CP871",         "CP871",                  false );
        addMapping( "CP871",         "CSIBM871",               false );
        addMapping( "CP871",         "EBCDIC-CP-IS",           true  );
        addMapping( "CP871",         "IBM-871",                false );
        addMapping( "CP871",         "IBM871",                 false );
        addMapping( "CP918",         "CP918",                  false );
        addMapping( "CP918",         "CSIBM918",               false );
        addMapping( "CP918",         "EBCDIC-CP-AR2",          true  );
        addMapping( "CP918",         "IBM-918",                false );
        addMapping( "CP918",         "IBM918",                 false );
        addMapping( "CP924",         "CCSID00924",             false );
        addMapping( "CP924",         "CP00924",                false );
        addMapping( "CP924",         "EBCDIC-LATIN9--EURO",    false );
        addMapping( "CP924",         "IBM-924",                false );
        addMapping( "CP924",         "IBM00924",               true  );
        addMapping( "CP936",         "GBK",                    true  );
        addMapping( "CP936",         "CP936",                  false );
        addMapping( "CP936",         "MS936",                  false );
        addMapping( "CP936",         "WINDOWS-936",            false );
        addMapping( "EUCJIS",        "CSEUCPKDFMTJAPANESE",    false );
        addMapping( "EUCJIS",        "EUC-JP",                 true  );
        addMapping( "EUCJIS",        "EXTENDED_UNIX_CODE_PACKED_FORMAT_FOR_JAPANESE", false );
        addMapping( "GB18030",       "GB18030",                true  );
        addMapping( "GB2312",        "CSGB2312",               false );
        addMapping( "GB2312",        "GB2312",                 true  );
        addMapping( "ISO2022CN",     "ISO-2022-CN",            true  );
        addMapping( "ISO2022KR",     "CSISO2022KR",            false );
        addMapping( "ISO2022KR",     "ISO-2022-KR",            true  );
        addMapping( "ISO8859_1",     "CP819",                  false );
        addMapping( "ISO8859_1",     "CSISOLATIN1",            false );
        addMapping( "ISO8859_1",     "IBM-819",                false );
        addMapping( "ISO8859_1",     "IBM819",                 false );
        addMapping( "ISO8859_1",     "ISO-8859-1",             true  );
        addMapping( "ISO8859_1",     "ISO-IR-100",             false );
        addMapping( "ISO8859_1",     "ISO_8859-1",             false );
        addMapping( "ISO8859_1",     "L1",                     false );
        addMapping( "ISO8859_1",     "LATIN1",                 false );
        addMapping( "ISO8859_2",     "CSISOLATIN2",            false );
        addMapping( "ISO8859_2",     "ISO-8859-2",             true  );
        addMapping( "ISO8859_2",     "ISO-IR-101",             false );
        addMapping( "ISO8859_2",     "ISO_8859-2",             false );
        addMapping( "ISO8859_2",     "L2",                     false );
        addMapping( "ISO8859_2",     "LATIN2",                 false );
        addMapping( "ISO8859_3",     "CSISOLATIN3",            false );
        addMapping( "ISO8859_3",     "ISO-8859-3",             true  );
        addMapping( "ISO8859_3",     "ISO-IR-109",             false );
        addMapping( "ISO8859_3",     "ISO_8859-3",             false );
        addMapping( "ISO8859_3",     "L3",                     false );
        addMapping( "ISO8859_3",     "LATIN3",                 false );
        addMapping( "ISO8859_4",     "CSISOLATIN4",            false );
        addMapping( "ISO8859_4",     "ISO-8859-4",             true  );
        addMapping( "ISO8859_4",     "ISO-IR-110",             false );
        addMapping( "ISO8859_4",     "ISO_8859-4",             false );
        addMapping( "ISO8859_4",     "L4",                     false );
        addMapping( "ISO8859_4",     "LATIN4",                 false );
        addMapping( "ISO8859_5",     "CSISOLATINCYRILLIC",     false );
        addMapping( "ISO8859_5",     "CYRILLIC",               false );
        addMapping( "ISO8859_5",     "ISO-8859-5",             true  );
        addMapping( "ISO8859_5",     "ISO-IR-144",             false );
        addMapping( "ISO8859_5",     "ISO_8859-5",             false );
        addMapping( "ISO8859_6",     "ARABIC",                 false );
        addMapping( "ISO8859_6",     "ASMO-708",               false );
        addMapping( "ISO8859_6",     "CSISOLATINARABIC",       false );
        addMapping( "ISO8859_6",     "ECMA-114",               false );
        addMapping( "ISO8859_6",     "ISO-8859-6",             true  );
        addMapping( "ISO8859_6",     "ISO-IR-127",             false );
        addMapping( "ISO8859_6",     "ISO_8859-6",             false );
        addMapping( "ISO8859_7",     "CSISOLATINGREEK",        false );
        addMapping( "ISO8859_7",     "ECMA-118",               false );
        addMapping( "ISO8859_7",     "ELOT_928",               false );
        addMapping( "ISO8859_7",     "GREEK",                  false );
        addMapping( "ISO8859_7",     "GREEK8",                 false );
        addMapping( "ISO8859_7",     "ISO-8859-7",             true  );
        addMapping( "ISO8859_7",     "ISO-IR-126",             false );
        addMapping( "ISO8859_7",     "ISO_8859-7",             false );
        addMapping( "ISO8859_8",     "CSISOLATINHEBREW",       false );
        addMapping( "ISO8859_8",     "HEBREW",                 false );
        addMapping( "ISO8859_8",     "ISO-8859-8",             true  );
        addMapping( "ISO8859_8",     "ISO-8859-8-I",           false );
        addMapping( "ISO8859_8",     "ISO-IR-138",             false );
        addMapping( "ISO8859_8",     "ISO_8859-8",             false );
        addMapping( "ISO8859_9",     "CSISOLATIN5",            false );
        addMapping( "ISO8859_9",     "ISO-8859-9",             true  );
        addMapping( "ISO8859_9",     "ISO-IR-148",             false );
        addMapping( "ISO8859_9",     "ISO_8859-9",             false );
        addMapping( "ISO8859_9",     "L5",                     false );
        addMapping( "ISO8859_9",     "LATIN5",                 false );
        addMapping( "JIS",           "CSISO2022JP",            false );
        addMapping( "JIS",           "ISO-2022-JP",            true  );
        addMapping( "JIS0201",       "CSISO13JISC6220JP",      false );
        addMapping( "JIS0201",       "X0201",                  true  );
        addMapping( "JIS0208",       "CSISO87JISX0208",        false );
        addMapping( "JIS0208",       "ISO-IR-87",              false );
        addMapping( "JIS0208",       "X0208",                  true  );
        addMapping( "JIS0208",       "X0208DBIJIS_X0208-1983", false );
        addMapping( "JIS0212",       "CSISO159JISX02121990",   false );
        addMapping( "JIS0212",       "ISO-IR-159",             true  );
        addMapping( "JIS0212",       "X0212",                  false );
        addMapping( "KOI8_R",        "CSKOI8R",                false );
        addMapping( "KOI8_R",        "KOI8-R",                 true  );
        addMapping( "KSC5601",       "EUC-KR",                 true  );
        addMapping( "MS932",         "CSWINDOWS31J",           false );
        addMapping( "MS932",         "WINDOWS-31J",            true  );
        addMapping( "SJIS",          "CSSHIFTJIS",             false );
        addMapping( "SJIS",          "MS_KANJI",               false );
        addMapping( "SJIS",          "SHIFT_JIS",              true  );
        addMapping( "TIS620",        "TIS-620",                true  );
        addMapping( "UNICODE",       "UTF-16",                 true  );
        addMapping( "UTF-16BE",      "UTF-16BE",               true  );
        addMapping( "UTF-16BE",      "UTF_16BE",               false );
        addMapping( "ISO-10646-UCS-2","ISO-10646-UCS-2",        true  );
        addMapping( "UTF-16LE",      "UTF-16LE",               true  );
        addMapping( "UTF-16LE",      "UTF_16LE",               false );
        addMapping( "UTF8",          "UTF-8",                  true  );

        assert completeMappings();
    };
}
