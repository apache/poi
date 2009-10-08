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

package org.apache.poi.hdf.model.util;

import java.util.*;

import org.apache.poi.hdf.model.hdftypes.PropertyNode;

/*
 * A B-Tree like implementation of the java.util.Set inteface.  This is a modifiable set
 * and thus allows elements to be added and removed.  An instance of java.util.Comparator
 * must be provided at construction else all Objects added to the set must implement
 * java.util.Comparable and must be comparable to one another.  No duplicate elements
 * will be allowed in any BTreeSet in accordance with the specifications of the Set interface.
 * Any attempt to add a null element will result in an IllegalArgumentException being thrown.
 * The java.util.Iterator returned by the iterator method guarantees the elements returned
 * are in ascending order.  The Iterator.remove() method is supported.
 * Comment me
 *
 * @author Ryan Ackley
 *
*/

public final class BTreeSet extends AbstractSet
{

    /*
     * Instance Variables
    */
    public BTreeNode root;
    private Comparator comparator = null;
    private int order;
    int size = 0;

    /*
     *                             Constructors
     * A no-arg constructor is supported in accordance with the specifications of the
     * java.util.Collections interface.  If the order for the B-Tree is not specified
     * at construction it defaults to 32.
    */

    public BTreeSet()
    {
        this(6);           // Default order for a BTreeSet is 32
    }

    public BTreeSet(Collection c)
    {
        this(6);           // Default order for a BTreeSet is 32
        addAll(c);
    }

    public BTreeSet(int order)
    {
        this(order, null);
    }

    public BTreeSet(int order, Comparator comparator)
    {
        this.order = order;
        this.comparator = comparator;
        root = new BTreeNode(null);
    }


    /*
     * Public Methods
    */
    public boolean add(Object x) throws IllegalArgumentException
    {
        if (x == null) throw new IllegalArgumentException();
        return root.insert(x, -1);
    }

    public boolean contains(Object x)
    {
        return root.includes(x);
    }

    public boolean remove(Object x)
    {
        if (x == null) return false;
        return root.delete(x, -1);
    }

    public int size()
    {
        return size;
    }

    public void clear()
    {
        root = new BTreeNode(null);
        size = 0;
    }

    public java.util.Iterator iterator()
    {
        return new Iterator();
    }

    public static ArrayList findProperties(int start, int end, BTreeSet.BTreeNode root)
    {
      ArrayList results = new ArrayList();
      BTreeSet.Entry[] entries = root.entries;

      for(int x = 0; x < entries.length; x++)
      {
        if(entries[x] != null)
        {
          BTreeSet.BTreeNode child = entries[x].child;
          PropertyNode xNode = (PropertyNode)entries[x].element;
          if(xNode != null)
          {
            int xStart = xNode.getStart();
            int xEnd = xNode.getEnd();
            if(xStart < end)
            {
              if(xStart >= start)
              {
                if(child != null)
                {
                  ArrayList beforeItems = findProperties(start, end, child);
                  results.addAll(beforeItems);
                }
                results.add(xNode);
              }
              else if(start < xEnd)
              {
                results.add(xNode);
                //break;
              }
            }
            else
            {
              if(child != null)
              {
                ArrayList beforeItems = findProperties(start, end, child);
                results.addAll(beforeItems);
              }
              break;
            }
          }
          else if(child != null)
          {
            ArrayList afterItems = findProperties(start, end, child);
            results.addAll(afterItems);
          }
        }
        else
        {
          break;
        }
      }
      return results;
    }
    /*
     * Private methods
    */
    int compare(Object x, Object y)
    {
        return (comparator == null ? ((Comparable)x).compareTo(y) : comparator.compare(x, y));
    }



    /*
     * Inner Classes
    */

    /*
     * Guarantees that the Objects are returned in ascending order.  Due to the volatile
     * structure of a B-Tree (many splits, steals and merges can happen in a single call to remove)
     * this Iterator does not attempt to track any concurrent changes that are happening to
     * it's BTreeSet.  Therefore, after every call to BTreeSet.remove or BTreeSet.add a new
     * Iterator should be constructed.  If no new Iterator is constructed than there is a
     * chance of receiving a NullPointerException. The Iterator.delete method is supported.
    */

    private class Iterator implements java.util.Iterator
    {
        private int index = 0;
        private Stack parentIndex = new Stack(); // Contains all parentIndicies for currentNode
        private Object lastReturned = null;
        private Object next;
        private BTreeNode currentNode;

        Iterator()
        {
            currentNode = firstNode();
            next = nextElement();
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public Object next()
        {
            if (next == null) throw new NoSuchElementException();

            lastReturned = next;
            next = nextElement();
            return lastReturned;
        }

        public void remove()
        {
            if (lastReturned == null) throw new NoSuchElementException();

            BTreeSet.this.remove(lastReturned);
            lastReturned = null;
        }

        private BTreeNode firstNode()
        {
            BTreeNode temp = BTreeSet.this.root;

            while (temp.entries[0].child != null)
            {
                temp = temp.entries[0].child;
                parentIndex.push(Integer.valueOf(0));
            }

            return temp;
        }

        private Object nextElement()
        {
            if (currentNode.isLeaf())
            {
                if (index < currentNode.nrElements) return currentNode.entries[index++].element;

                else if (!parentIndex.empty())
                { //All elements have been returned, return successor of lastReturned if it exists
                    currentNode = currentNode.parent;
                    index = ((Integer)parentIndex.pop()).intValue();

                    while (index == currentNode.nrElements)
                    {
                        if (parentIndex.empty()) break;
                        currentNode = currentNode.parent;
                        index = ((Integer)parentIndex.pop()).intValue();
                    }

                    if (index == currentNode.nrElements) return null; //Reached root and he has no more children
                    return currentNode.entries[index++].element;
                }

                else
                { //Your a leaf and the root
                    if (index == currentNode.nrElements) return null;
                    return currentNode.entries[index++].element;
                }
            }

            // else - You're not a leaf so simply find and return the successor of lastReturned
            currentNode = currentNode.entries[index].child;
            parentIndex.push(Integer.valueOf(index));

            while (currentNode.entries[0].child != null)
            {
                currentNode = currentNode.entries[0].child;
                parentIndex.push(Integer.valueOf(0));
            }

            index = 1;
            return currentNode.entries[0].element;
        }
    }


    public static class Entry
    {

        public Object element;
        public BTreeNode child;
    }


    public class BTreeNode
    {

        public Entry[] entries;
        public BTreeNode parent;
        private int nrElements = 0;
        private final int MIN = (BTreeSet.this.order - 1) / 2;

        BTreeNode(BTreeNode parent)
        {
            this.parent = parent;
            entries = new Entry[BTreeSet.this.order];
            entries[0] = new Entry();
        }

        boolean insert(Object x, int parentIndex)
        {
            if (isFull())
            { // If full, you must split and promote splitNode before inserting
                Object splitNode = entries[nrElements / 2].element;
                BTreeNode rightSibling = split();

                if (isRoot())
                { // Grow a level
                    splitRoot(splitNode, this, rightSibling);
                    // Determine where to insert
                    if (BTreeSet.this.compare(x, BTreeSet.this.root.entries[0].element) < 0) insert(x, 0);
                    else rightSibling.insert(x, 1);
                }

                else
                { // Promote splitNode
                    parent.insertSplitNode(splitNode, this, rightSibling, parentIndex);
                    if (BTreeSet.this.compare(x, parent.entries[parentIndex].element) < 0) {
                        return insert(x, parentIndex);
                    }
                    return rightSibling.insert(x, parentIndex + 1);
                }
            }

            else if (isLeaf())
            { // If leaf, simply insert the non-duplicate element
                int insertAt = childToInsertAt(x, true);
                // Determine if the element already exists
                if (insertAt == -1) {
                    return false;
                }
                insertNewElement(x, insertAt);
                BTreeSet.this.size++;
                return true;
            }

            else
            { // If not full and not leaf recursively find correct node to insert at
                int insertAt = childToInsertAt(x, true);
                return (insertAt == -1 ? false : entries[insertAt].child.insert(x, insertAt));
            }
            return false;
        }

        boolean includes(Object x)
        {
            int index = childToInsertAt(x, true);
            if (index == -1) return true;
            if (entries[index] == null || entries[index].child == null) return false;
            return entries[index].child.includes(x);
        }

        boolean delete(Object x, int parentIndex)
        {
            int i = childToInsertAt(x, true);
            int priorParentIndex = parentIndex;
            BTreeNode temp = this;
            if (i != -1)
            {
                do
                {
                    if (temp.entries[i] == null || temp.entries[i].child == null) return false;
                    temp = temp.entries[i].child;
                    priorParentIndex = parentIndex;
                    parentIndex = i;
                    i = temp.childToInsertAt(x, true);
                } while (i != -1);
            } // Now temp contains element to delete and temp's parentIndex is parentIndex

            if (temp.isLeaf())
            { // If leaf and have more than MIN elements, simply delete
                if (temp.nrElements > MIN)
                {
                    temp.deleteElement(x);
                    BTreeSet.this.size--;
                    return true;
                }

                // else - If leaf and have less than MIN elements, than prepare the BTreeSet for deletion
                temp.prepareForDeletion(parentIndex);
                temp.deleteElement(x);
                BTreeSet.this.size--;
                temp.fixAfterDeletion(priorParentIndex);
                return true;
            }

            // else - Only delete at leaf so first switch with successor than delete
            temp.switchWithSuccessor(x);
            parentIndex = temp.childToInsertAt(x, false) + 1;
            return temp.entries[parentIndex].child.delete(x, parentIndex);

        }


        private boolean isFull() { return nrElements == (BTreeSet.this.order - 1); }

        private boolean isLeaf() { return entries[0].child == null; }

        private boolean isRoot() { return parent == null; }

        /*
         * Splits a BTreeNode into two BTreeNodes, removing the splitNode from the
         * calling BTreeNode.
        */
        private BTreeNode split()
        {
            BTreeNode rightSibling = new BTreeNode(parent);
            int index = nrElements / 2;
            entries[index++].element = null;

            for (int i = 0, nr = nrElements; index <= nr; i++, index++)
            {
                rightSibling.entries[i] = entries[index];
                if (rightSibling.entries[i] != null && rightSibling.entries[i].child != null)
                    rightSibling.entries[i].child.parent = rightSibling;
                entries[index] = null;
                nrElements--;
                rightSibling.nrElements++;
            }

            rightSibling.nrElements--; // Need to correct for copying the last Entry which has a null element and a child
            return rightSibling;
        }

        /*
         * Creates a new BTreeSet.root which contains only the splitNode and pointers
         * to it's left and right child.
        */
        private void splitRoot(Object splitNode, BTreeNode left, BTreeNode right)
        {
            BTreeNode newRoot = new BTreeNode(null);
            newRoot.entries[0].element = splitNode;
            newRoot.entries[0].child = left;
            newRoot.entries[1] = new Entry();
            newRoot.entries[1].child = right;
            newRoot.nrElements = 1;
            left.parent = right.parent = newRoot;
            BTreeSet.this.root = newRoot;
        }

        private void insertSplitNode(Object splitNode, BTreeNode left, BTreeNode right, int insertAt)
        {
            for (int i = nrElements; i >= insertAt; i--) entries[i + 1] = entries[i];

            entries[insertAt] = new Entry();
            entries[insertAt].element = splitNode;
            entries[insertAt].child = left;
            entries[insertAt + 1].child = right;

            nrElements++;
        }

        private void insertNewElement(Object x, int insertAt)
        {

            for (int i = nrElements; i > insertAt; i--) entries[i] = entries[i - 1];

            entries[insertAt] = new Entry();
            entries[insertAt].element = x;

            nrElements++;
        }

        /*
         * Possibly a deceptive name for a pretty cool method.  Uses binary search
         * to determine the postion in entries[] in which to traverse to find the correct
         * BTreeNode in which to insert a new element.  If the element exists in the calling
         * BTreeNode than -1 is returned.  When the parameter position is true and the element
         * is present in the calling BTreeNode -1 is returned, if position is false and the
         * element is contained in the calling BTreeNode than the position of the element
         * in entries[] is returned.
        */
        private int childToInsertAt(Object x, boolean position)
        {
            int index = nrElements / 2;

            if (entries[index] == null || entries[index].element == null) return index;

            int lo = 0, hi = nrElements - 1;
            while (lo <= hi)
            {
                if (BTreeSet.this.compare(x, entries[index].element) > 0)
                {
                    lo = index + 1;
                    index = (hi + lo) / 2;
                }
                else
                {
                    hi = index - 1;
                    index = (hi + lo) / 2;
                }
            }

            hi++;
            if (entries[hi] == null || entries[hi].element == null) return hi;
            return (!position ? hi : BTreeSet.this.compare(x, entries[hi].element) == 0 ? -1 : hi);
        }


        private void deleteElement(Object x)
        {
            int index = childToInsertAt(x, false);
            for (; index < (nrElements - 1); index++) entries[index] = entries[index + 1];

            if (nrElements == 1) entries[index] = new Entry(); // This is root and it is empty
            else entries[index] = null;

            nrElements--;
        }

        private void prepareForDeletion(int parentIndex)
        {
            if (isRoot()) return; // Don't attempt to steal or merge if your the root

            // If not root then try to steal left
            else if (parentIndex != 0 && parent.entries[parentIndex - 1].child.nrElements > MIN)
            {
                stealLeft(parentIndex);
                return;
            }

            // If not root and can't steal left try to steal right
            else if (parentIndex < entries.length && parent.entries[parentIndex + 1] != null && parent.entries[parentIndex + 1].child != null && parent.entries[parentIndex + 1].child.nrElements > MIN)
            {
                    stealRight(parentIndex);
                    return;
            }

            // If not root and can't steal left or right then try to merge left
            else if (parentIndex != 0) {
                mergeLeft(parentIndex);
                return;
            }

            // If not root and can't steal left or right and can't merge left you must be able to merge right
            else mergeRight(parentIndex);
        }

        private void fixAfterDeletion(int parentIndex)
        {
            if (isRoot() || parent.isRoot()) return; // No fixing needed

            if (parent.nrElements < MIN)
            { // If parent lost it's n/2 element repair it
                BTreeNode temp = parent;
                temp.prepareForDeletion(parentIndex);
                if (temp.parent == null) return; // Root changed
                if (!temp.parent.isRoot() && temp.parent.nrElements < MIN)
                { // If need be recurse
                    BTreeNode x = temp.parent.parent;
                    int i = 0;
                    // Find parent's parentIndex
                    for (; i < entries.length; i++) if (x.entries[i].child == temp.parent) break;
                    temp.parent.fixAfterDeletion(i);
                }
            }
        }

        private void switchWithSuccessor(Object x)
        {
            int index = childToInsertAt(x, false);
            BTreeNode temp = entries[index + 1].child;
            while (temp.entries[0] != null && temp.entries[0].child != null) temp = temp.entries[0].child;
            Object successor = temp.entries[0].element;
            temp.entries[0].element = entries[index].element;
            entries[index].element = successor;
        }

        /*
         * This method is called only when the BTreeNode has the minimum number of elements,
         * has a leftSibling, and the leftSibling has more than the minimum number of elements.
        */
        private void stealLeft(int parentIndex)
        {
            BTreeNode p = parent;
            BTreeNode ls = parent.entries[parentIndex - 1].child;

            if (isLeaf())
            { // When stealing from leaf to leaf don't worry about children
                int add = childToInsertAt(p.entries[parentIndex - 1].element, true);
                insertNewElement(p.entries[parentIndex - 1].element, add);
                p.entries[parentIndex - 1].element = ls.entries[ls.nrElements - 1].element;
                ls.entries[ls.nrElements - 1] = null;
                ls.nrElements--;
            }

            else
            { // Was called recursively to fix an undermanned parent
                entries[0].element = p.entries[parentIndex - 1].element;
                p.entries[parentIndex - 1].element = ls.entries[ls.nrElements - 1].element;
                entries[0].child = ls.entries[ls.nrElements].child;
                entries[0].child.parent = this;
                ls.entries[ls.nrElements] = null;
                ls.entries[ls.nrElements - 1].element = null;
                nrElements++;
                ls.nrElements--;
            }
        }

        /*
         * This method is called only when stealLeft can't be called, the BTreeNode
         * has the minimum number of elements, has a rightSibling, and the rightSibling
         * has more than the minimum number of elements.
        */
        private void stealRight(int parentIndex)
        {
            BTreeNode p = parent;
            BTreeNode rs = p.entries[parentIndex + 1].child;

            if (isLeaf())
            { // When stealing from leaf to leaf don't worry about children
                entries[nrElements] = new Entry();
                entries[nrElements].element = p.entries[parentIndex].element;
                p.entries[parentIndex].element = rs.entries[0].element;
                for (int i = 0; i < rs.nrElements; i++) rs.entries[i] = rs.entries[i + 1];
                rs.entries[rs.nrElements - 1] = null;
                nrElements++;
                rs.nrElements--;
            }

            else
            { // Was called recursively to fix an undermanned parent
                for (int i = 0; i <= nrElements; i++) entries[i] = entries[i + 1];
                entries[nrElements].element = p.entries[parentIndex].element;
                p.entries[parentIndex].element = rs.entries[0].element;
                entries[nrElements + 1] = new Entry();
                entries[nrElements + 1].child = rs.entries[0].child;
                entries[nrElements + 1].child.parent = this;
                for (int i = 0; i <= rs.nrElements; i++) rs.entries[i] = rs.entries[i + 1];
                rs.entries[rs.nrElements] = null;
                nrElements++;
                rs.nrElements--;
            }
        }

        /*
         * This method is called only when stealLeft and stealRight could not be called,
         * the BTreeNode has the minimum number of elements, has a leftSibling, and the
         * leftSibling has more than the minimum number of elements.  If after completion
         * parent has fewer than the minimum number of elements than the parents entries[0]
         * slot is left empty in anticipation of a recursive call to stealLeft, stealRight,
         * mergeLeft, or mergeRight to fix the parent. All of the before-mentioned methods
         * expect the parent to be in such a condition.
        */
        private void mergeLeft(int parentIndex)
        {
            BTreeNode p = parent;
            BTreeNode ls = p.entries[parentIndex - 1].child;

            if (isLeaf())
            { // Don't worry about children
                int add = childToInsertAt(p.entries[parentIndex - 1].element, true);
                insertNewElement(p.entries[parentIndex - 1].element, add); // Could have been a successor switch
                p.entries[parentIndex - 1].element = null;

                for (int i = nrElements - 1, nr = ls.nrElements; i >= 0; i--)
                    entries[i + nr] = entries[i];

                for (int i = ls.nrElements - 1; i >= 0; i--)
                {
                    entries[i] = ls.entries[i];
                    nrElements++;
                }

                if (p.nrElements == MIN && p != BTreeSet.this.root)
                {

                    for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x--, y--)
                        p.entries[x] = p.entries[y];
                    p.entries[0] = new Entry();
                    p.entries[0].child = ls; //So p doesn't think it's a leaf this will be deleted in the next recursive call
                }

                else
                {

                    for (int x = parentIndex - 1, y = parentIndex; y <= p.nrElements; x++, y++)
                        p.entries[x] = p.entries[y];
                    p.entries[p.nrElements] = null;
                }

                p.nrElements--;

                if (p.isRoot() && p.nrElements == 0)
                { // It's the root and it's empty
                    BTreeSet.this.root = this;
                    parent = null;
                }
            }

            else
            { // I'm not a leaf but fixing the tree structure
                entries[0].element = p.entries[parentIndex - 1].element;
                entries[0].child = ls.entries[ls.nrElements].child;
                nrElements++;

                for (int x = nrElements, nr = ls.nrElements; x >= 0; x--)
                    entries[x + nr] = entries[x];

                for (int x = ls.nrElements - 1; x >= 0; x--)
                {
                    entries[x] = ls.entries[x];
                    entries[x].child.parent = this;
                    nrElements++;
                }

                if (p.nrElements == MIN && p != BTreeSet.this.root)
                { // Push everything to the right
                    for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x++, y++)
                    {
                        System.out.println(x + " " + y);
                        p.entries[x] = p.entries[y];
                    }
                    p.entries[0] = new Entry();
                }

                else
                { // Either p.nrElements > MIN or p == BTreeSet.this.root so push everything to the left
                    for (int x = parentIndex - 1, y = parentIndex; y <= p.nrElements; x++, y++)
                        p.entries[x] = p.entries[y];
                    p.entries[p.nrElements] = null;
                }

                p.nrElements--;

                if (p.isRoot() && p.nrElements == 0)
                { // p == BTreeSet.this.root and it's empty
                    BTreeSet.this.root = this;
                    parent = null;
                }
            }
        }

        /*
         * This method is called only when stealLeft, stealRight, and mergeLeft could not be called,
         * the BTreeNode has the minimum number of elements, has a rightSibling, and the
         * rightSibling has more than the minimum number of elements.  If after completion
         * parent has fewer than the minimum number of elements than the parents entries[0]
         * slot is left empty in anticipation of a recursive call to stealLeft, stealRight,
         * mergeLeft, or mergeRight to fix the parent. All of the before-mentioned methods
         * expect the parent to be in such a condition.
        */
        private void mergeRight(int parentIndex)
        {
            BTreeNode p = parent;
            BTreeNode rs = p.entries[parentIndex + 1].child;

            if (isLeaf())
            { // Don't worry about children
                entries[nrElements] = new Entry();
                entries[nrElements].element = p.entries[parentIndex].element;
                nrElements++;
                for (int i = 0, nr = nrElements; i < rs.nrElements; i++, nr++)
                {
                    entries[nr] = rs.entries[i];
                    nrElements++;
                }
                p.entries[parentIndex].element = p.entries[parentIndex + 1].element;
                if (p.nrElements == MIN && p != BTreeSet.this.root)
                {
                    for (int x = parentIndex + 1, y = parentIndex; y >= 0; x--, y--)
                        p.entries[x] = p.entries[y];
                    p.entries[0] = new Entry();
                    p.entries[0].child = rs; // So it doesn't think it's a leaf, this child will be deleted in the next recursive call
                }

                else
                {
                    for (int x = parentIndex + 1, y = parentIndex + 2; y <= p.nrElements; x++, y++)
                        p.entries[x] = p.entries[y];
                    p.entries[p.nrElements] = null;
                }

                p.nrElements--;
                if (p.isRoot() && p.nrElements == 0)
                { // It's the root and it's empty
                    BTreeSet.this.root = this;
                    parent = null;
                }
           }

           else
           { // It's not a leaf

               entries[nrElements].element = p.entries[parentIndex].element;
               nrElements++;

               for (int x = nrElements + 1, y = 0; y <= rs.nrElements; x++, y++)
               {
                   entries[x] = rs.entries[y];
                   rs.entries[y].child.parent = this;
                   nrElements++;
               }
               nrElements--;

               p.entries[++parentIndex].child = this;

               if (p.nrElements == MIN && p != BTreeSet.this.root)
               {
                  for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x--, y--)
                      p.entries[x] = p.entries[y];
                  p.entries[0] = new Entry();
               }

               else
               {
                   for (int x = parentIndex - 1, y = parentIndex; y <= p.nrElements; x++, y++)
                       p.entries[x] = p.entries[y];
                   p.entries[p.nrElements] = null;
               }

               p.nrElements--;

               if (p.isRoot() && p.nrElements == 0)
               { // It's the root and it's empty
                   BTreeSet.this.root = this;
                   parent = null;
               }
            }
        }
  }

}

