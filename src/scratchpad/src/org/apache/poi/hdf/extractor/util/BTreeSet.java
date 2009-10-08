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

package org.apache.poi.hdf.extractor.util;

import java.util.*;


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

public final class BTreeSet extends AbstractSet implements Set {

    /*
     * Instance Variables
    */
    public BTreeNode root;
    private Comparator comparator = null;
    int order;
    int size = 0;

    /*
     *                             Constructors
     * A no-arg constructor is supported in accordance with the specifications of the
     * java.util.Collections interface.  If the order for the B-Tree is not specified
     * at construction it defaults to 32.
    */

    public BTreeSet() {
        this(6);           // Default order for a BTreeSet is 32
    }

    public BTreeSet(Collection c) {
        this(6);           // Default order for a BTreeSet is 32
        addAll(c);
    }

    public BTreeSet(int order) {
        this(order, null);
    }

    public BTreeSet(int order, Comparator comparator) {
        this.order = order;
        this.comparator = comparator;
        root = new BTreeNode(null);
    }


    /*
     * Public Methods
    */
    public boolean add(Object x) throws IllegalArgumentException {
        if (x == null) throw new IllegalArgumentException();
        return root.insert(x, -1);
    }

    public boolean contains(Object x) {
        return root.includes(x);
    }

    public boolean remove(Object x) {
        if (x == null) return false;
        return root.delete(x, -1);
    }

    public int size() {
        return size;
    }

    public void clear() {
        root = new BTreeNode(null);
        size = 0;
    }

    public java.util.Iterator iterator() {
        return new BTIterator();
    }


    /*
     * Private methods
    */
    int compare(Object x, Object y) {
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

    private final class BTIterator implements java.util.Iterator {
        private int index = 0;
        Stack parentIndex = new Stack(); // Contains all parentIndicies for currentNode
        private Object lastReturned = null;
        private Object next;
        BTreeNode currentNode;

        BTIterator() {
            currentNode = firstNode();
            next = nextElement();
        }

        public boolean hasNext() {
            return next != null;
        }

        public Object next() {
            if (next == null) throw new NoSuchElementException();

            lastReturned = next;
            next = nextElement();
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null) throw new NoSuchElementException();

            BTreeSet.this.remove(lastReturned);
            lastReturned = null;
        }

        private BTreeNode firstNode() {
            BTreeNode temp = BTreeSet.this.root;

            while (temp._entries[0].child != null) {
                temp = temp._entries[0].child;
                parentIndex.push(Integer.valueOf(0));
            }

            return temp;
        }

        private Object nextElement() {
            if (currentNode.isLeaf()) {
                if (index < currentNode._nrElements) return currentNode._entries[index++].element;

                else if (!parentIndex.empty()) { //All elements have been returned, return successor of lastReturned if it exists
                    currentNode = currentNode._parent;
                    index = ((Integer)parentIndex.pop()).intValue();

                    while (index == currentNode._nrElements) {
                        if (parentIndex.empty()) break;
                        currentNode = currentNode._parent;
                        index = ((Integer)parentIndex.pop()).intValue();
                    }

                    if (index == currentNode._nrElements) return null; //Reached root and he has no more children
                    return currentNode._entries[index++].element;
                }

                else { //Your a leaf and the root
                    if (index == currentNode._nrElements) return null;
                    return currentNode._entries[index++].element;
                }
            }

            // else - You're not a leaf so simply find and return the successor of lastReturned
            currentNode = currentNode._entries[index].child;
            parentIndex.push(Integer.valueOf(index));

            while (currentNode._entries[0].child != null) {
                currentNode = currentNode._entries[0].child;
                parentIndex.push(Integer.valueOf(0));
            }

            index = 1;
            return currentNode._entries[0].element;
        }
    }


    public static class Entry {

        public Object element;
        public BTreeNode child;
    }


    public class BTreeNode {

        public Entry[] _entries;
        public BTreeNode _parent;
        int _nrElements = 0;
        private final int MIN = (BTreeSet.this.order - 1) / 2;

        BTreeNode(BTreeNode parent) {
            _parent = parent;
            _entries = new Entry[BTreeSet.this.order];
            _entries[0] = new Entry();
        }

        boolean insert(Object x, int parentIndex) {
            if (isFull()) { // If full, you must split and promote splitNode before inserting
                Object splitNode = _entries[_nrElements / 2].element;
                BTreeNode rightSibling = split();

                if (isRoot()) { // Grow a level
                    splitRoot(splitNode, this, rightSibling);
                    // Determine where to insert
                    if (BTreeSet.this.compare(x, BTreeSet.this.root._entries[0].element) < 0) insert(x, 0);
                    else rightSibling.insert(x, 1);
                }

                else { // Promote splitNode
                    _parent.insertSplitNode(splitNode, this, rightSibling, parentIndex);
                    if (BTreeSet.this.compare(x, _parent._entries[parentIndex].element) < 0) {
                        return insert(x, parentIndex);
                    }
                    return rightSibling.insert(x, parentIndex + 1);
                }
            }

            else if (isLeaf()) { // If leaf, simply insert the non-duplicate element
                int insertAt = childToInsertAt(x, true);
                if (insertAt == -1) {
                    return false; // Determine if the element already exists
                }
                insertNewElement(x, insertAt);
                BTreeSet.this.size++;
                return true;
            }

            else { // If not full and not leaf recursively find correct node to insert at
                int insertAt = childToInsertAt(x, true);
                return (insertAt == -1 ? false : _entries[insertAt].child.insert(x, insertAt));
            }
            return false;
        }

        boolean includes(Object x) {
            int index = childToInsertAt(x, true);
            if (index == -1) return true;
            if (_entries[index] == null || _entries[index].child == null) return false;
            return _entries[index].child.includes(x);
        }

        boolean delete(Object x, int parentIndex) {
            int i = childToInsertAt(x, true);
            int priorParentIndex = parentIndex;
            BTreeNode temp = this;
            if (i != -1) {
                do {
                    if (temp._entries[i] == null || temp._entries[i].child == null) return false;
                    temp = temp._entries[i].child;
                    priorParentIndex = parentIndex;
                    parentIndex = i;
                    i = temp.childToInsertAt(x, true);
                } while (i != -1);
            } // Now temp contains element to delete and temp's parentIndex is parentIndex

            if (temp.isLeaf()) { // If leaf and have more than MIN elements, simply delete
                if (temp._nrElements > MIN) {
                    temp.deleteElement(x);
                    BTreeSet.this.size--;
                    return true;
                }

                // else If leaf and have less than MIN elements, than prepare the BTreeSet for deletion
                temp.prepareForDeletion(parentIndex);
                temp.deleteElement(x);
                BTreeSet.this.size--;
                temp.fixAfterDeletion(priorParentIndex);
                return true;
            }

            // else Only delete at leaf so first switch with successor than delete
            temp.switchWithSuccessor(x);
            parentIndex = temp.childToInsertAt(x, false) + 1;
            return temp._entries[parentIndex].child.delete(x, parentIndex);
        }


        private boolean isFull() { return _nrElements == (BTreeSet.this.order - 1); }

        boolean isLeaf() { return _entries[0].child == null; }

        private boolean isRoot() { return _parent == null; }

        /*
         * Splits a BTreeNode into two BTreeNodes, removing the splitNode from the
         * calling BTreeNode.
        */
        private BTreeNode split() {
            BTreeNode rightSibling = new BTreeNode(_parent);
            int index = _nrElements / 2;
            _entries[index++].element = null;

            for (int i = 0, nr = _nrElements; index <= nr; i++, index++) {
                rightSibling._entries[i] = _entries[index];
                if (rightSibling._entries[i] != null && rightSibling._entries[i].child != null)
                    rightSibling._entries[i].child._parent = rightSibling;
                _entries[index] = null;
                _nrElements--;
                rightSibling._nrElements++;
            }

            rightSibling._nrElements--; // Need to correct for copying the last Entry which has a null element and a child
            return rightSibling;
        }

        /*
         * Creates a new BTreeSet.root which contains only the splitNode and pointers
         * to it's left and right child.
        */
        private void splitRoot(Object splitNode, BTreeNode left, BTreeNode right) {
            BTreeNode newRoot = new BTreeNode(null);
            newRoot._entries[0].element = splitNode;
            newRoot._entries[0].child = left;
            newRoot._entries[1] = new Entry();
            newRoot._entries[1].child = right;
            newRoot._nrElements = 1;
            left._parent = right._parent = newRoot;
            BTreeSet.this.root = newRoot;
        }

        private void insertSplitNode(Object splitNode, BTreeNode left, BTreeNode right, int insertAt) {
            for (int i = _nrElements; i >= insertAt; i--) _entries[i + 1] = _entries[i];

            _entries[insertAt] = new Entry();
            _entries[insertAt].element = splitNode;
            _entries[insertAt].child = left;
            _entries[insertAt + 1].child = right;

            _nrElements++;
        }

        private void insertNewElement(Object x, int insertAt) {

            for (int i = _nrElements; i > insertAt; i--) _entries[i] = _entries[i - 1];

            _entries[insertAt] = new Entry();
            _entries[insertAt].element = x;

            _nrElements++;
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
        private int childToInsertAt(Object x, boolean position) {
            int index = _nrElements / 2;

            if (_entries[index] == null || _entries[index].element == null) return index;

            int lo = 0, hi = _nrElements - 1;
            while (lo <= hi) {
                if (BTreeSet.this.compare(x, _entries[index].element) > 0) {
                    lo = index + 1;
                    index = (hi + lo) / 2;
                }
                else {
                    hi = index - 1;
                    index = (hi + lo) / 2;
                }
            }

            hi++;
            if (_entries[hi] == null || _entries[hi].element == null) return hi;
            return (!position ? hi : BTreeSet.this.compare(x, _entries[hi].element) == 0 ? -1 : hi);
        }


        private void deleteElement(Object x) {
            int index = childToInsertAt(x, false);
            for (; index < (_nrElements - 1); index++) _entries[index] = _entries[index + 1];

            if (_nrElements == 1) _entries[index] = new Entry(); // This is root and it is empty
            else _entries[index] = null;

            _nrElements--;
        }

        private void prepareForDeletion(int parentIndex) {
            if (isRoot()) return; // Don't attempt to steal or merge if your the root

            // If not root then try to steal left
            else if (parentIndex != 0 && _parent._entries[parentIndex - 1].child._nrElements > MIN) {
                stealLeft(parentIndex);
                return;
            }

            // If not root and can't steal left try to steal right
            else if (parentIndex < _entries.length && _parent._entries[parentIndex + 1] != null && _parent._entries[parentIndex + 1].child != null && _parent._entries[parentIndex + 1].child._nrElements > MIN) {
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

        private void fixAfterDeletion(int parentIndex) {
            if (isRoot() || _parent.isRoot()) return; // No fixing needed

            if (_parent._nrElements < MIN) { // If parent lost it's n/2 element repair it
                BTreeNode temp = _parent;
                temp.prepareForDeletion(parentIndex);
                if (temp._parent == null) return; // Root changed
                if (!temp._parent.isRoot() && temp._parent._nrElements < MIN) { // If need be recurse
                    BTreeNode x = temp._parent._parent;
                    int i = 0;
                    // Find parent's parentIndex
                    for (; i < _entries.length; i++) if (x._entries[i].child == temp._parent) break;
                    temp._parent.fixAfterDeletion(i);
                }
            }
        }

        private void switchWithSuccessor(Object x) {
            int index = childToInsertAt(x, false);
            BTreeNode temp = _entries[index + 1].child;
            while (temp._entries[0] != null && temp._entries[0].child != null) temp = temp._entries[0].child;
            Object successor = temp._entries[0].element;
            temp._entries[0].element = _entries[index].element;
            _entries[index].element = successor;
        }

        /*
         * This method is called only when the BTreeNode has the minimum number of elements,
         * has a leftSibling, and the leftSibling has more than the minimum number of elements.
        */
        private void stealLeft(int parentIndex) {
            BTreeNode p = _parent;
            BTreeNode ls = _parent._entries[parentIndex - 1].child;

            if (isLeaf()) { // When stealing from leaf to leaf don't worry about children
                int add = childToInsertAt(p._entries[parentIndex - 1].element, true);
                insertNewElement(p._entries[parentIndex - 1].element, add);
                p._entries[parentIndex - 1].element = ls._entries[ls._nrElements - 1].element;
                ls._entries[ls._nrElements - 1] = null;
                ls._nrElements--;
            }

            else { // Was called recursively to fix an undermanned parent
                _entries[0].element = p._entries[parentIndex - 1].element;
                p._entries[parentIndex - 1].element = ls._entries[ls._nrElements - 1].element;
                _entries[0].child = ls._entries[ls._nrElements].child;
                _entries[0].child._parent = this;
                ls._entries[ls._nrElements] = null;
                ls._entries[ls._nrElements - 1].element = null;
                _nrElements++;
                ls._nrElements--;
            }
        }

        /*
         * This method is called only when stealLeft can't be called, the BTreeNode
         * has the minimum number of elements, has a rightSibling, and the rightSibling
         * has more than the minimum number of elements.
        */
        private void stealRight(int parentIndex) {
            BTreeNode p = _parent;
            BTreeNode rs = p._entries[parentIndex + 1].child;

            if (isLeaf()) { // When stealing from leaf to leaf don't worry about children
                _entries[_nrElements] = new Entry();
                _entries[_nrElements].element = p._entries[parentIndex].element;
                p._entries[parentIndex].element = rs._entries[0].element;
                for (int i = 0; i < rs._nrElements; i++) rs._entries[i] = rs._entries[i + 1];
                rs._entries[rs._nrElements - 1] = null;
                _nrElements++;
                rs._nrElements--;
            }

            else { // Was called recursively to fix an undermanned parent
                for (int i = 0; i <= _nrElements; i++) _entries[i] = _entries[i + 1];
                _entries[_nrElements].element = p._entries[parentIndex].element;
                p._entries[parentIndex].element = rs._entries[0].element;
                _entries[_nrElements + 1] = new Entry();
                _entries[_nrElements + 1].child = rs._entries[0].child;
                _entries[_nrElements + 1].child._parent = this;
                for (int i = 0; i <= rs._nrElements; i++) rs._entries[i] = rs._entries[i + 1];
                rs._entries[rs._nrElements] = null;
                _nrElements++;
                rs._nrElements--;
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
        private void mergeLeft(int parentIndex) {
            BTreeNode p = _parent;
            BTreeNode ls = p._entries[parentIndex - 1].child;

            if (isLeaf()) { // Don't worry about children
                int add = childToInsertAt(p._entries[parentIndex - 1].element, true);
                insertNewElement(p._entries[parentIndex - 1].element, add); // Could have been a successor switch
                p._entries[parentIndex - 1].element = null;

                for (int i = _nrElements - 1, nr = ls._nrElements; i >= 0; i--)
                    _entries[i + nr] = _entries[i];

                for (int i = ls._nrElements - 1; i >= 0; i--) {
                    _entries[i] = ls._entries[i];
                    _nrElements++;
                }

                if (p._nrElements == MIN && p != BTreeSet.this.root) {

                    for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x--, y--)
                        p._entries[x] = p._entries[y];
                    p._entries[0] = new Entry();
                    p._entries[0].child = ls; //So p doesn't think it's a leaf this will be deleted in the next recursive call
                }

                else {

                    for (int x = parentIndex - 1, y = parentIndex; y <= p._nrElements; x++, y++)
                        p._entries[x] = p._entries[y];
                    p._entries[p._nrElements] = null;
                }

                p._nrElements--;

                if (p.isRoot() && p._nrElements == 0) { // It's the root and it's empty
                    BTreeSet.this.root = this;
                    _parent = null;
                }
            }

            else { // I'm not a leaf but fixing the tree structure
                _entries[0].element = p._entries[parentIndex - 1].element;
                _entries[0].child = ls._entries[ls._nrElements].child;
                _nrElements++;

                for (int x = _nrElements, nr = ls._nrElements; x >= 0; x--)
                    _entries[x + nr] = _entries[x];

                for (int x = ls._nrElements - 1; x >= 0; x--) {
                    _entries[x] = ls._entries[x];
                    _entries[x].child._parent = this;
                    _nrElements++;
                }

                if (p._nrElements == MIN && p != BTreeSet.this.root) { // Push everything to the right
                    for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x++, y++){
                        System.out.println(x + " " + y);
                        p._entries[x] = p._entries[y];}
                    p._entries[0] = new Entry();
                }

                else { // Either p.nrElements > MIN or p == BTreeSet.this.root so push everything to the left
                    for (int x = parentIndex - 1, y = parentIndex; y <= p._nrElements; x++, y++)
                        p._entries[x] = p._entries[y];
                    p._entries[p._nrElements] = null;
                }

                p._nrElements--;

                if (p.isRoot() && p._nrElements == 0) { // p == BTreeSet.this.root and it's empty
                    BTreeSet.this.root = this;
                    _parent = null;
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
        private void mergeRight(int parentIndex) {
            BTreeNode p = _parent;
            BTreeNode rs = p._entries[parentIndex + 1].child;

            if (isLeaf()) { // Don't worry about children
                _entries[_nrElements] = new Entry();
                _entries[_nrElements].element = p._entries[parentIndex].element;
                _nrElements++;
                for (int i = 0, nr = _nrElements; i < rs._nrElements; i++, nr++) {
                    _entries[nr] = rs._entries[i];
                    _nrElements++;
                }
                p._entries[parentIndex].element = p._entries[parentIndex + 1].element;
                if (p._nrElements == MIN && p != BTreeSet.this.root) {
                    for (int x = parentIndex + 1, y = parentIndex; y >= 0; x--, y--)
                        p._entries[x] = p._entries[y];
                    p._entries[0] = new Entry();
                    p._entries[0].child = rs; // So it doesn't think it's a leaf, this child will be deleted in the next recursive call
                }

                else {
                    for (int x = parentIndex + 1, y = parentIndex + 2; y <= p._nrElements; x++, y++)
                        p._entries[x] = p._entries[y];
                    p._entries[p._nrElements] = null;
                }

                p._nrElements--;
                if (p.isRoot() && p._nrElements == 0) { // It's the root and it's empty
                    BTreeSet.this.root = this;
                    _parent = null;
                }
           }

           else { // It's not a leaf

               _entries[_nrElements].element = p._entries[parentIndex].element;
               _nrElements++;

               for (int x = _nrElements + 1, y = 0; y <= rs._nrElements; x++, y++) {
                   _entries[x] = rs._entries[y];
                   rs._entries[y].child._parent = this;
                   _nrElements++;
               }
               _nrElements--;

               p._entries[++parentIndex].child = this;

               if (p._nrElements == MIN && p != BTreeSet.this.root) {
                  for (int x = parentIndex - 1, y = parentIndex - 2; y >= 0; x--, y--)
                      p._entries[x] = p._entries[y];
                  p._entries[0] = new Entry();
               }

               else {
                   for (int x = parentIndex - 1, y = parentIndex; y <= p._nrElements; x++, y++)
                       p._entries[x] = p._entries[y];
                   p._entries[p._nrElements] = null;
               }

               p._nrElements--;

               if (p.isRoot() && p._nrElements == 0) { // It's the root and it's empty
                   BTreeSet.this.root = this;
                   _parent = null;
               }
            }
        }
  }
}

