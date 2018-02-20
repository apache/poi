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

package dom.common;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DomUtils
{


    /**
     * @return true if sets are the same
     */
    public static boolean compareNamedNodeMaps(NamedNodeMap atrSet1,
        NamedNodeMap atrSet2)
    {
        if (atrSet1 == null)
            if (atrSet2 == null)
                return true;
            else
                return false;
        else if (atrSet2 == null) return false;

        int nLen1 = atrSet1.getLength();
        Attr at1,
            at2;

        if (nLen1 != atrSet2.getLength())
            return false;
        for (int i = 0; i < nLen1; i++)
        {
            at1 = (Attr) atrSet1.item(i);
            at2 = (Attr) atrSet2.getNamedItem(at1.getName());
            if (at2 == null)
                return false;
            else if (!
                (at1.getNodeName().equals(at2.getNodeName())) &&
                (at1.getNamespaceURI().equals(at2.getNamespaceURI())) &&
                (at1.getValue().equals(at2.getValue()))
            )
                return false;
            else if (at1 == at2) return false;
        }
        return true;
    }

    /**
     * node is a copy
     * children are the same obj
     */
    public static boolean compareNodesShallow(Node n1, Node n2)
    {
        String pre1 = n1.getPrefix();
        String pre2 = n2.getPrefix();
        String uri1 = n1.getNamespaceURI();
        String uri2 = n2.getNamespaceURI();

        boolean prefixUriOK =
           ( pre1 == null && null == pre2 && uri1 == null && uri2 == null )
            ||  uri1.equals( uri2 ) &&
            pre1.equals( pre2 );

        return (
            n1.getNodeName().equals(n2.getNodeName()) &&
            prefixUriOK &&
            compareNamedNodeMaps(n1.getAttributes(), n2.getAttributes())
            // cloned nodes never have children
            //  && compareNodeTreePtr(n1.getChildNodes(),n2.getChildNodes())
            );
    }

    //equates "" to null
    private static boolean compareNull(String pre1, String pre2)
    {
        if (pre1 == null && pre2 == null) return true;
        if (pre1 == null && pre2.equals("")) return true;
        if (pre1.equals("") && pre2 == null)
            return true;
        else
            return pre1.equals(pre2);


    }

    /**
     * node is a copy
     * children copies
     */
    public static boolean compareNodesDeep(Node n1, Node n2)
        throws IllegalStateException
    {
        if (n1 == n2 && n1 == null) return true;
        if (n1 == null && n2 != null || n2 == null && n1 != null) return false;
        if (!(n1.getNodeName().equals(n2.getNodeName()) &&
            compareNull(n1.getNamespaceURI(), n2.getNamespaceURI()) &&
            compareNull(n1.getPrefix(), n2.getPrefix()))
        )
            throw new IllegalStateException("Diff QNames " +
                n1.getNamespaceURI() +
                " * " +
                n1.getPrefix() +
                ":" +
                n1.getNodeName() +
                "AND " + n2.getNamespaceURI() + " * " +
                n2.getPrefix() +
                ":" +
                n2.getNodeName());
        else if (!compareNamedNodeMaps(n1.getAttributes(), n2.getAttributes()))
            throw new IllegalStateException("Diff Attrs " +
                n1.getAttributes().getLength() +
                "AND " + n2.getAttributes().getLength());

        else if (n1.hasChildNodes()     //Xerces attr quirk
            && n2.hasChildNodes())
        {
            if (!compareNodeTreeValue(n1.getChildNodes(), n2.getChildNodes()))
                throw new IllegalStateException("Diff Children " +
                    n1.getNodeName() +
                    "AND " + n2.getNodeName());
        }
        else if (n1.hasChildNodes()     //Xerces attr quirk
            ^ n2.hasChildNodes())
            throw new IllegalStateException("One node has Children, other one not ");


        return true;
    }

    /**
     * the two node lists really are the same list
     */
    public static boolean compareNodeTreePtr(NodeList lst1, NodeList lst2)
    {
        if (lst1.getLength() != lst2.getLength())
            return false;

        for (int i = 0; i < lst1.getLength(); i++)
        {
            if (lst1.item(i) != lst2.item(i))
                return false;
        }
        return true;
    }

    public static boolean compareNodeTreeValue(NodeList lst1, NodeList lst2)
    {
        if (lst1.getLength() != lst2.getLength())
            return false;
        boolean result = true;
        for (int i = 0; i < lst1.getLength(); i++)
        {
            result = result && compareNodesDeep(lst1.item(i),
                lst2.item(i));

        }
        return result;
    }

}
