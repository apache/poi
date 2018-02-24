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

import org.apache.xmlbeans.xml.stream.XMLName;
import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;

public class XMLNameHelper
{
    public static QName getQName(XMLName xmlName)
    {
        if (xmlName == null)
            return null;
        
        return QNameHelper.forLNS( xmlName.getLocalName(), xmlName.getNamespaceUri() );
    }
    
    public static XMLName forLNS(String localname, String uri)
    {
        if (uri == null)
            uri = "";
        return new XmlNameImpl(uri, localname);
    }

    public static XMLName forLN(String localname)
    {
        return new XmlNameImpl("", localname);
    }

    public static XMLName forPretty(String pretty, int offset)
    {
        int at = pretty.indexOf('@', offset);
        if (at < 0)
            return new XmlNameImpl("", pretty.substring(offset));
        return new XmlNameImpl(pretty.substring(at + 1), pretty.substring(offset, at));
    }

    public static String pretty(XMLName name)
    {
        if (name == null)
            return "null";

        if (name.getNamespaceUri() == null || name.getNamespaceUri().length() == 0)
            return name.getLocalName();
        
        return name.getLocalName() + "@" + name.getNamespaceUri();
    }

    private static final char[] hexdigits = new char[]
        {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private static boolean isSafe(int c)
    {
        if (c >= 'a' && c <= 'z')
            return true;
        if (c >= 'A' && c <= 'Z')
            return true;
        if (c >= '0' && c <= '9')
            return true;
        return false;
    }

    public static String hexsafe(String s)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            if (isSafe(ch))
            {
                result.append(ch);
            }
            else
            {
                byte[] utf8 = null;
                try
                {
                    utf8 = s.substring(i, i + 1).getBytes("UTF-8");
                for (int j = 0; j < utf8.length; j++)
                {
                    result.append('_');
                    result.append(hexdigits[(utf8[j] >> 4) & 0xF]);
                    result.append(hexdigits[utf8[j] & 0xF]);
                    }
                }
                catch(UnsupportedEncodingException uee)
                {
                    // should never happen - UTF-8 is always supported
                    result.append("_BAD_UTF8_CHAR");
                }
            }
        }
        return result.toString();
    }

    public static String hexsafedir(XMLName name)
    {
        if (name.getNamespaceUri() == null || name.getNamespaceUri().length() == 0)
            return "_nons/" + hexsafe(name.getLocalName());
        return hexsafe(name.getNamespaceUri()) + "/" + hexsafe(name.getLocalName());
    }
}
