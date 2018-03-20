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
package tools.xml;

import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

public class XmlComparator
{
    public static class Diagnostic
    {
        private StringBuffer message = null;

        private void add(String s)
        {
            if (message==null)
                 message = new StringBuffer();

            message.append(s).append("\n");
        }

        public boolean hasMessage()
        {
            return message!=null;
        }

        public String toString()
        {
            return (message==null ? null : message.toString());
        }

        public static void add(Diagnostic diag, String s)
        {
            if ( diag!=null)
                diag.add(s);
        }
    }

    public static boolean wsCollapseEqual(String s1, String s2)
    {
        String s1c = XmlWhitespace.collapse(s1);
        String s2c = XmlWhitespace.collapse(s2);
        return (s1c.equals(s2c));
    }

    public static boolean compareNamesAndAttributes(XmlCursor cur1, XmlCursor cur2, Diagnostic diag)
    {
        if (!cur1.getName().equals(cur2.getName()))
        {
            Diagnostic.add(diag, "Element names " + QNameHelper.pretty(cur1.getName()) + " and " + QNameHelper.pretty(cur2.getName()) + " do not match");
            return false;
        }

        boolean more = cur1.toFirstAttribute();
        if (more)
        {
            for (; more; more = cur1.toNextAttribute())
            {
                String text1 = cur1.getTextValue();
                String text2 = cur2.getAttributeText(cur1.getName());
                if (text2 == null)
                {
                    Diagnostic.add(diag, "Attribute " + QNameHelper.pretty(cur1.getName()) + " not present");
                    return false;
                }

                if (!wsCollapseEqual(text1, text2))
                {
                    Diagnostic.add(diag, "Attribute values for " + QNameHelper.pretty(cur1.getName()) + " do not match");
                    return false;
                }
            }
            cur1.toParent();
        }

        more = cur2.toFirstAttribute();
        if (more)
        {
            for (; more; more = cur2.toNextAttribute())
            {
                String text1 = cur1.getAttributeText(cur2.getName());
                if (text1 == null)
                {
                    Diagnostic.add(diag, "Attribute " + QNameHelper.pretty(cur2.getName()) + " not present");
                    return false;
                }
            }
            cur2.toParent();
        }

        return true;
    }

    public static Diagnostic lenientlyCompareTwoXmlStrings(String actual, String expect)
        throws XmlException
    {
        Diagnostic diag = new Diagnostic();
        lenientlyCompareTwoXmlStrings(actual, expect, diag);
        return diag;
    }

    /**
     * Provides an utility to compare the xml inside the two strings
     * @return true if the xml inside the two strings is leniently the same
     *   otherwise false
     */
    public static boolean lenientlyCompareTwoXmlStrings(String actual, String expect, Diagnostic diag)
        throws XmlException
    {
        XmlObject xobj1 = XmlObject.Factory.parse(actual);
        XmlObject xobj2 = XmlObject.Factory.parse(expect);

        XmlCursor cur1 = xobj1.newCursor();
        XmlCursor cur2 = xobj2.newCursor();

        cur1.toFirstChild();
        cur2.toFirstChild();

        return lenientlyCompareTwoXmlStrings(cur1,  cur2, diag);
    }

    /**
     * Provides an utility to compare the xml inside the two cursors
     * @return true if the xml inside the two strings is leniently the same
     *   otherwise false
     */
    public static boolean lenientlyCompareTwoXmlStrings(XmlCursor cur1, XmlCursor cur2, Diagnostic diag)
    {
        boolean match = true;
        int depth = 0;
        while (cur1.currentTokenType() != XmlCursor.TokenType.STARTDOC)
        {
            if (!compareNamesAndAttributes(cur1, cur2, diag))
            {
                match = false;
            }

            boolean hasChildren1 = cur1.toFirstChild();
            boolean hasChildren2 = cur2.toFirstChild();
            depth++;
            if (hasChildren1 != hasChildren2)
            {
                Diagnostic.add(diag, "Topology differs: one document has children where the other does not (" + QNameHelper.pretty(cur1.getName()) + ", " + QNameHelper.pretty(cur2.getName()) + ")"); // TODO: where?
                match = false;
                if (hasChildren1)
                {
                    cur1.toParent();
                    hasChildren1 = false;
                }
                if (hasChildren2)
                {
                    cur2.toParent();
                    hasChildren2 = false;
                }
            }
            else if (hasChildren1 == false)
            {
                if (!wsCollapseEqual(cur1.getTextValue(), cur2.getTextValue()))
                {
                    Diagnostic.add(diag, "Value " + cur1.getTextValue() + " differs from value " + cur2.getTextValue());
                    match = false;
                }
            }

            if (hasChildren1)
                continue;

            for (;;)
            {
                boolean hasSibling1 = cur1.toNextSibling();
                boolean hasSibling2 = cur2.toNextSibling();

                if (hasSibling1 != hasSibling2)
                {
                    Diagnostic.add(diag, "Topology differs: one document has siblings where the other does not"); // TODO: where?
                    hasSibling1 = false;
                    hasSibling2 = false;
                }

                if (hasSibling1)
                    break;

                cur1.toParent();
                cur2.toParent();
                depth--;

                if (cur1.currentTokenType() == XmlCursor.TokenType.STARTDOC || depth<=0)
                    break;
            }
        }
        return match;
    }
}
