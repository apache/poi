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

package org.apache.poi.util;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public final class XmlSort
{
    /**
     * Receives an XML element instance and sorts the children of this
     * element in lexicographical (by default) order.
     *
     * @param args An array in which the first item is a
     * path to the XML instance file and the second item (optional) is
     * an XPath inside the document identifying the element to be sorted
     */
    public static void main(String[] args)
    {
        if (args.length < 1 || args.length > 2)
        {
            System.out.println("    java XmlSort <XML_File> [<XPath>]");
            return;
        }
        File f = new File(args[0]);
        try
        {
            XmlObject docInstance = XmlObject.Factory.parse(f);
            XmlObject element = null;
            if (args.length > 1)
            {
                String xpath = args[1];
                XmlObject[] result = docInstance.selectPath(xpath);
                if (result.length == 0)
                {
                    System.out.println("ERROR: XPath \"" + xpath + "\" did not return any results");
                }
                else if (result.length > 1)
                {
                    System.out.println("ERROR: XPath \"" + xpath + "\" returned more than one " +
                        "node (" + result.length + ")");
                }
                else
                    element = result[0];
            }
            else
            {
                // Navigate to the root element
                XmlCursor c = docInstance.newCursor();
                c.toFirstChild();
                element = c.getObject();
                c.dispose();
            }
            if (element != null)
                sort(element, new QNameComparator(QNameComparator.ASCENDING));
            System.out.println(docInstance.xmlText());
        }
        catch (IOException ioe)
        {
            System.out.println("ERROR: Could not open file: \"" + args[0] + "\": " +
                ioe.getMessage());
        }
        catch (XmlException xe)
        {
            System.out.println("ERROR: Could not parse file: \"" + args[0] + "\": " +
                xe.getMessage());
        }
    }

    /**
     * Sorts the children of <code>element</code> according to the order indicated by the
     * comparator.
     * @param element the element whose content is to be sorted. Only element children are sorted,
     * attributes are not touched. When elements are reordered, all the text, comments and PIs
     * follow the element that they come immediately after.
     * @param comp a comparator that is to be used when comparing the <code>QName</code>s of two
     * elements. See {@link QNameComparator} for a simple
     * implementation that compares two elements based on the value of their QName, but more
     * complicated implementations are possible, for instance, ones that compare two elements based
     * on the value of a specifc attribute etc.
     * @throws IllegalArgumentException if the input <code>XmlObject</code> does not represent
     * an element
     */
    public static void sort(XmlObject element, Comparator<XmlCursor> comp)
    {
        XmlCursor headCursor = element.newCursor();
        if (!headCursor.isStart())
            throw new IllegalStateException("The element parameter must point to a STARTDOC");
        // We use insertion sort to minimize the number of swaps, because each swap means
        // moving a part of the document
        /* headCursor points to the beginning of the list of the already sorted items and
           listCursor points to the beginning of the list of unsorted items
           At the beginning, headCursor points to the first element and listCursor points to the
           second element. The algorithm ends when listCursor cannot be moved to the "next"
           element in the unsorted list, i.e. the unsorted list becomes empty */
        boolean moved = headCursor.toFirstChild();
        if (!moved)
        {
            // Cursor was not moved, which means that the given element has no children and
            // therefore there is nothing to sort
            return;
        }
        XmlCursor listCursor = headCursor.newCursor();
        boolean moreElements = listCursor.toNextSibling();
        while (moreElements)
        {
            moved = false;
            // While we can move the head of the unsorted list, it means that there are still
            // items (elements) that need to be sorted
            while (headCursor.comparePosition(listCursor) < 0)
            {
                if (comp.compare(headCursor, listCursor) > 0)
                {
                    // We have found the position in the sorted list, insert the element and the
                    // text following the element in the current position
                    /*
                     * Uncomment this code to cause the text before the element to move along
                     * with the element, rather than the text after the element. Notice that this
                     * is more difficult to do, because the cursor's "type" refers to the position
                     * to the right of the cursor, so to get the type of the token to the left, the
                     * cursor needs to be first moved to the left (previous token)
                     *
                    headCursor.toPrevToken();
                    while (headCursor.isComment() || headCursor.isProcinst() || headCursor.isText())
                        headCursor.toPrevToken();
                    headCursor.toNextToken();
                    listCursor.toPrevToken();
                    while (listCursor.isComment() || listCursor.isProcinst() || listCursor.isText())
                        listCursor.toPrevToken();
                    listCursor.toNextToken();
                    while (!listCursor.isStart())
                        listCursor.moveXml(headCursor);
                    listCursor.moveXml(headCursor);
                    */
                    // Move the element
                    listCursor.moveXml(headCursor);
                    // Move the text following the element
                    while (!listCursor.isStart() && !listCursor.isEnd())
                        listCursor.moveXml(headCursor);
                    moreElements = listCursor.isStart();
                    moved = true;
                    break;
                }
                headCursor.toNextSibling();
            }
            if (!moved)
            {
                // Because during the move of a fragment of XML, the listCursor is also moved, in
                // case we didn't need to move XML (the new element to be inserted happened to
                // be the last one in order), we need to move this cursor
                moreElements = listCursor.toNextSibling();
            }
            // Reposition the head of the sorted list
            headCursor.toParent();
            headCursor.toFirstChild();
        }
    }

    /**
     * Implements a <code>java.util.Comparator</code> for comparing <code>QName</code>values.
     * The namespace URIs are compared first and if they are equal, the local parts are compared.
     * <p/>
     * The constructor accepts an argument indicating whether the comparison order is the same as
     * the lexicographic order of the strings or the reverse.
     */
    public static final class QNameComparator implements Comparator
    {
        public static final int ASCENDING = 1;
        public static final int DESCENDING = 2;

        private int order;

        public QNameComparator(int order)
        {
            this.order = order;
            if (order != ASCENDING && order != DESCENDING)
                throw new IllegalArgumentException("Please specify one of ASCENDING or DESCENDING "+
                    "comparison orders");
        }

        public int compare(Object o, Object o1)
        {
            XmlCursor cursor1 = (XmlCursor) o;
            XmlCursor cursor2 = (XmlCursor) o1;
            QName qname1 = cursor1.getName();
            QName qname2 = cursor2.getName();
            int qnameComparisonRes = qname1.getNamespaceURI().compareTo(qname2.getNamespaceURI());
            if (qnameComparisonRes == 0)
                return order == ASCENDING ?
                    qname1.getLocalPart().compareTo(qname2.getLocalPart()) :
                    -qname1.getLocalPart().compareTo(qname2.getLocalPart());
            else
                return order == ASCENDING ? qnameComparisonRes : -qnameComparisonRes;
        }
    }
}
 