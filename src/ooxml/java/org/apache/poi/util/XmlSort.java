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

import java.util.Comparator;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public final class XmlSort {
    /**
     * Sorts the children of <code>element</code> according to the order indicated by the
     * comparator.
     * @param element the element whose content is to be sorted. Only element children are sorted,
     * attributes are not touched. When elements are reordered, all the text, comments and PIs
     * follow the element that they come immediately after.
     * @param comp a comparator that is to be used when comparing the <code>QName</code>s of two
     * elements. 
     * @throws IllegalArgumentException if the input <code>XmlObject</code> does not represent
     * an element
     */
    public static void sort(XmlObject element, Comparator<XmlCursor> comp) {
        XmlCursor headCursor = element.newCursor();
        if (!headCursor.isStart()) {
            throw new IllegalStateException("The element parameter must point to a STARTDOC");
        }
        // We use insertion sort to minimize the number of swaps, because each swap means
        // moving a part of the document
        /* headCursor points to the beginning of the list of the already sorted items and
           listCursor points to the beginning of the list of unsorted items
           At the beginning, headCursor points to the first element and listCursor points to the
           second element. The algorithm ends when listCursor cannot be moved to the "next"
           element in the unsorted list, i.e. the unsorted list becomes empty */
        boolean moved = headCursor.toFirstChild();
        if (!moved) {
            // Cursor was not moved, which means that the given element has no children and
            // therefore there is nothing to sort
            return;
        }
        XmlCursor listCursor = headCursor.newCursor();
        boolean moreElements = listCursor.toNextSibling();
        while (moreElements) {
            moved = false;
            // While we can move the head of the unsorted list, it means that there are still
            // items (elements) that need to be sorted
            while (headCursor.comparePosition(listCursor) < 0) {
                if (comp.compare(headCursor, listCursor) > 0) {
                    // We have found the position in the sorted list, insert the element and the
                    // text following the element in the current position
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
            if (!moved) {
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
}
 