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

import org.apache.xmlbeans.SchemaType;

public class XmlWhitespace
{
    public static final int WS_UNSPECIFIED = SchemaType.WS_UNSPECIFIED;
    public static final int WS_PRESERVE = SchemaType.WS_PRESERVE;
    public static final int WS_REPLACE = SchemaType.WS_REPLACE;
    public static final int WS_COLLAPSE = SchemaType.WS_COLLAPSE;


    public static boolean isSpace(char ch)
    {
        switch (ch)
        {
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                return true;
        }
        return false;
    }

    public static boolean isAllSpace(String v)
    {
        for (int i = 0, len = v.length(); i < len; i++)
        {
            if (!isSpace(v.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean isAllSpace(CharSequence v)
    {
        for (int i = 0, len = v.length(); i < len; i++)
        {
            if (!isSpace(v.charAt(i)))
                return false;
        }
        return true;
    }

    public static String collapse(String v)
    {
        return collapse(v, XmlWhitespace.WS_COLLAPSE);
    }

    /**
     * The algorithm used by apply_wscanon: sometimes used in impls.
     */
    public static String collapse(String v, int wsr)
    {
        if (wsr == SchemaType.WS_PRESERVE || wsr == SchemaType.WS_UNSPECIFIED)
            return v;

        if (v.indexOf('\n') >= 0)
            v = v.replace('\n', ' ');
        if (v.indexOf('\t') >= 0)
            v = v.replace('\t', ' ');
        if (v.indexOf('\r') >= 0)
            v = v.replace('\r', ' ');

        if (wsr == SchemaType.WS_REPLACE)
            return v;

        int j = 0;
        int len = v.length();
        if (len == 0)
            return v;

        int i;

        /* a trick: examine every other character looking for pairs of spaces */
        if (v.charAt(0) != ' ')
        {
            examine: {
                for (j = 2; j < len; j += 2)
                {
                    if (v.charAt(j) == ' ')
                    {
                        if (v.charAt(j - 1) == ' ')
                            break examine;
                        if (j == len - 1)
                            break examine;
                        j++;
                        if (v.charAt(j) == ' ')
                            break examine;
                    }
                }
                if (j == len && v.charAt(j - 1) == ' ')
                    break examine;
                return v;
            }
            /* j is pointing at the first ws to be removed, or past end */
            i = j;
        }
        else
        {
            /**
             * j is pointing at the last whitespace in the initial run
             */
            while (j + 1 < v.length() && v.charAt(j + 1) == ' ')
                j += 1;
            i = 0;
        }

        char[] ch = v.toCharArray();

        shifter: for (;;)
        {
            for (;;)
            {
                /* j was ws or past end */
                j++;
                if (j >= len)
                    break shifter;
                if (v.charAt(j) != ' ')
                    break;
            }
            for (;;)
            {
                /* j was nonws */
                ch[i++] = ch[j++];
                if (j >= len)
                    break shifter;
                if (ch[j] == ' ')
                {
                    ch[i++] = ch[j++];
                    if (j >= len)
                        break shifter;
                    if (ch[j] == ' ')
                        break;
                }
            }
        }

        return new String(ch, 0, (i == 0 || ch[i - 1] != ' ') ? i : i - 1);
    }
}
