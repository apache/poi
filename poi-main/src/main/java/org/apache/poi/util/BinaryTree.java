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

import java.util.*;

/**
 * Red-Black tree-based implementation of Map. This class guarantees
 * that the map will be in both ascending key order and ascending
 * value order, sorted according to the natural order for the key's
 * and value's classes.<p>
 *
 * This Map is intended for applications that need to be able to look
 * up a key-value pairing by either key or value, and need to do so
 * with equal efficiency.<p>
 *
 * While that goal could be accomplished by taking a pair of TreeMaps
 * and redirecting requests to the appropriate TreeMap (e.g.,
 * containsKey would be directed to the TreeMap that maps values to
 * keys, containsValue would be directed to the TreeMap that maps keys
 * to values), there are problems with that implementation,
 * particularly when trying to keep the two TreeMaps synchronized with
 * each other. And if the data contained in the TreeMaps is large, the
 * cost of redundant storage becomes significant.<p>
 *
 * This solution keeps the data properly synchronized and minimizes
 * the data storage. The red-black algorithm is based on TreeMap's,
 * but has been modified to simultaneously map a tree node by key and
 * by value. This doubles the cost of put operations (but so does
 * using two TreeMaps), and nearly doubles the cost of remove
 * operations (there is a savings in that the lookup of the node to be
 * removed only has to be performed once). And since only one node
 * contains the key and value, storage is significantly less than that
 * required by two TreeMaps.<p>
 *
 * There are some limitations placed on data kept in this Map. The
 * biggest one is this:<p>
 *
 * When performing a put operation, neither the key nor the value may
 * already exist in the Map. In the java.util Map implementations
 * (HashMap, TreeMap), you can perform a put with an already mapped
 * key, and neither cares about duplicate values at all ... but this
 * implementation's put method with throw an IllegalArgumentException
 * if either the key or the value is already in the Map.<p>
 *
 * Obviously, that same restriction (and consequence of failing to
 * heed that restriction) applies to the putAll method.<p>
 *
 * The Map.Entry instances returned by the appropriate methods will
 * not allow setValue() and will throw an
 * UnsupportedOperationException on attempts to call that method.<p>
 *
 * New methods are added to take advantage of the fact that values are
 * kept sorted independently of their keys:<p>
 *
 * Object getKeyForValue(Object value) is the opposite of get; it
 * takes a value and returns its key, if any.<p>
 *
 * Object removeValue(Object value) finds and removes the specified
 * value and returns the now un-used key.<p>
 *
 * Set entrySetByValue() returns the Map.Entry's in a Set whose
 * iterator will iterate over the Map.Entry's in ascending order by
 * their corresponding values.<p>
 *
 * Set keySetByValue() returns the keys in a Set whose iterator will
 * iterate over the keys in ascending order by their corresponding
 * values.<p>
 *
 * Collection valuesByValue() returns the values in a Collection whose
 * iterator will iterate over the values in ascending order.<p>
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
//for performance
public class BinaryTree extends AbstractMap {
    final Node[] _root;
    int _size = 0;
    int _modifications = 0;
    private final Set[] _key_set = new Set[] { null, null };
    private final Set[] _entry_set = new Set[] { null, null };
    private final Collection[] _value_collection = new Collection[] { null, null };
    static int      _KEY              = 0;
    static int      _VALUE            = 1;
    private static int      _INDEX_SUM        = _KEY + _VALUE;
    private static int      _MINIMUM_INDEX    = 0;
    private static int      _INDEX_COUNT      = 2;
    private static String[] _data_name        = new String[]
    {
        "key", "value"
    };

    /**
     * Construct a new BinaryTree
     */
    public BinaryTree() {
        _root = new Node[]{ null, null, };
    }

    /**
     * Constructs a new BinaryTree from an existing Map, with keys and
     * values sorted
     *
     * @param map the map whose mappings are to be placed in this map.
     *
     * @exception ClassCastException if the keys in the map are not
     *                               Comparable, or are not mutually
     *                               comparable; also if the values in
     *                               the map are not Comparable, or
     *                               are not mutually Comparable
     * @exception NullPointerException if any key or value in the map
     *                                 is null
     * @exception IllegalArgumentException if there are duplicate keys
     *                                     or duplicate values in the
     *                                     map
     */
    public BinaryTree(Map map)
        throws ClassCastException, NullPointerException,
                IllegalArgumentException
    {
    	this();
        putAll(map);
    }

    /**
     * Returns the key to which this map maps the specified value.
     * Returns null if the map contains no mapping for this value.
     *
     * @param value value whose associated key is to be returned.
     *
     * @return the key to which this map maps the specified value, or
     *         null if the map contains no mapping for this value.
     *
     * @exception ClassCastException if the value is of an
     *                               inappropriate type for this map.
     * @exception NullPointerException if the value is null
     */
    public Object getKeyForValue(Object value)
        throws ClassCastException, NullPointerException
    {
        return doGet(( Comparable ) value, _VALUE);
    }

    /**
     * Removes the mapping for this value from this map if present
     *
     * @param value value whose mapping is to be removed from the map.
     *
     * @return previous key associated with specified value, or null
     *         if there was no mapping for value.
     */
    public Object removeValue(Object value)
    {
        return doRemove(( Comparable ) value, _VALUE);
    }

    /**
     * Returns a set view of the mappings contained in this map. Each
     * element in the returned set is a Map.Entry. The set is backed
     * by the map, so changes to the map are reflected in the set, and
     * vice-versa.  If the map is modified while an iteration over the
     * set is in progress, the results of the iteration are
     * undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the Iterator.remove,
     * Set.remove, removeAll, retainAll and clear operations.  It does
     * not support the add or addAll operations.<p>
     *
     * The difference between this method and entrySet is that
     * entrySet's iterator() method returns an iterator that iterates
     * over the mappings in ascending order by key. This method's
     * iterator method iterates over the mappings in ascending order
     * by value.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set entrySetByValue()
    {
        if (_entry_set[ _VALUE ] == null)
        {
            _entry_set[ _VALUE ] = new AbstractSet()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_VALUE)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node;
                        }
                    };
                }

                public boolean contains(Object o)
                {
                    if (!(o instanceof Map.Entry))
                    {
                        return false;
                    }
                    Map.Entry entry = ( Map.Entry ) o;
                    Object    key   = entry.getKey();
                    Node      node  = lookup(( Comparable ) entry.getValue(),
                                             _VALUE);

                    return (node != null) && node.getData(_KEY).equals(key);
                }

                public boolean remove(Object o)
                {
                    if (!(o instanceof Map.Entry))
                    {
                        return false;
                    }
                    Map.Entry entry = ( Map.Entry ) o;
                    Object    key   = entry.getKey();
                    Node      node  = lookup(( Comparable ) entry.getValue(),
                                             _VALUE);

                    if ((node != null) && node.getData(_KEY).equals(key))
                    {
                        doRedBlackDelete(node);
                        return true;
                    }
                    return false;
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _entry_set[ _VALUE ];
    }

    /**
     * Returns a set view of the keys contained in this map.  The set
     * is backed by the map, so changes to the map are reflected in
     * the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress, the results of the
     * iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * Iterator.remove, Set.remove, removeAll, retainAll, and clear
     * operations. It does not support the add or addAll
     * operations.<p>
     *
     * The difference between this method and keySet is that keySet's
     * iterator() method returns an iterator that iterates over the
     * keys in ascending order by key. This method's iterator method
     * iterates over the keys in ascending order by value.
     *
     * @return a set view of the keys contained in this map.
     */

    public Set keySetByValue()
    {
        if (_key_set[ _VALUE ] == null)
        {
            _key_set[ _VALUE ] = new AbstractSet()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_VALUE)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node.getData(_KEY);
                        }
                    };
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public boolean contains(Object o)
                {
                    return containsKey(o);
                }

                public boolean remove(Object o)
                {
                    int old_size = _size;

                    BinaryTree.this.remove(o);
                    return _size != old_size;
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _key_set[ _VALUE ];
    }

    /**
     * Returns a collection view of the values contained in this
     * map. The collection is backed by the map, so changes to the map
     * are reflected in the collection, and vice-versa. If the map is
     * modified while an iteration over the collection is in progress,
     * the results of the iteration are undefined. The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the Iterator.remove,
     * Collection.remove, removeAll, retainAll and clear operations.
     * It does not support the add or addAll operations.<p>
     *
     * The difference between this method and values is that values's
     * iterator() method returns an iterator that iterates over the
     * values in ascending order by key. This method's iterator method
     * iterates over the values in ascending order by key.
     *
     * @return a collection view of the values contained in this map.
     */

    public Collection valuesByValue()
    {
        if (_value_collection[ _VALUE ] == null)
        {
            _value_collection[ _VALUE ] = new AbstractCollection()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_VALUE)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node.getData(_VALUE);
                        }
                    };
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public boolean contains(Object o)
                {
                    return containsValue(o);
                }

                public boolean remove(Object o)
                {
                    int old_size = _size;

                    removeValue(o);
                    return _size != old_size;
                }

                public boolean removeAll(Collection c)
                {
                    boolean  modified = false;
                    Iterator iter     = c.iterator();

                    while (iter.hasNext())
                    {
                        if (removeValue(iter.next()) != null)
                        {
                            modified = true;
                        }
                    }
                    return modified;
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _value_collection[ _VALUE ];
    }

    /**
     * common remove logic (remove by key or remove by value)
     *
     * @param o the key, or value, that we're looking for
     * @param index _KEY or _VALUE
     *
     * @return the key, if remove by value, or the value, if remove by
     *         key. null if the specified key or value could not be
     *         found
     */
    private Object doRemove(Comparable o, int index)
    {
        Node   node = lookup(o, index);
        Object rval = null;

        if (node != null)
        {
            rval = node.getData(oppositeIndex(index));
            doRedBlackDelete(node);
        }
        return rval;
    }

    /**
     * common get logic, used to get by key or get by value
     *
     * @param o the key or value that we're looking for
     * @param index _KEY or _VALUE
     *
     * @return the key (if the value was mapped) or the value (if the
     *         key was mapped); null if we couldn't find the specified
     *         object
     */
    private Object doGet(Comparable o, int index)
    {
        checkNonNullComparable(o, index);
        Node node = lookup(o, index);

        return ((node == null) ? null
                               : node.getData(oppositeIndex(index)));
    }

    /**
     * Get the opposite index of the specified index
     *
     * @param index _KEY or _VALUE
     *
     * @return _VALUE (if _KEY was specified), else _KEY
     */
    private int oppositeIndex(int index)
    {

        // old trick ... to find the opposite of a value, m or n,
        // subtract the value from the sum of the two possible
        // values. (m + n) - m = n; (m + n) - n = m
        return _INDEX_SUM - index;
    }

    /**
     * do the actual lookup of a piece of data
     *
     * @param data the key or value to be looked up
     * @param index _KEY or _VALUE
     *
     * @return the desired Node, or null if there is no mapping of the
     *         specified data
     */
    public Node lookup(Comparable data, int index)
    {
        Node rval = null;
        Node node = _root[ index ];

        while (node != null)
        {
            int cmp = compare(data, node.getData(index));

            if (cmp == 0)
            {
                rval = node;
                break;
            }
            node = (cmp < 0) ? node.getLeft(index)
                             : node.getRight(index);
        }
        return rval;
    }

    /**
     * Compare two objects
     *
     * @param o1 the first object
     * @param o2 the second object
     *
     * @return negative value if o1 < o2; 0 if o1 == o2; positive
     *         value if o1 > o2
     */
    private static int compare(Comparable o1, Comparable o2)
    {
        return o1.compareTo(o2);
    }

    /**
     * find the least node from a given node. very useful for starting
     * a sorting iterator ...
     *
     * @param node the node from which we will start searching
     * @param index _KEY or _VALUE
     *
     * @return the smallest node, from the specified node, in the
     *         specified mapping
     */
    static Node leastNode(Node node, int index)
    {
        Node rval = node;

        if (rval != null)
        {
            while (rval.getLeft(index) != null)
            {
                rval = rval.getLeft(index);
            }
        }
        return rval;
    }

    /**
     * get the next larger node from the specified node
     *
     * @param node the node to be searched from
     * @param index _KEY or _VALUE
     *
     * @return the specified node
     */
    static Node nextGreater(Node node, int index)
    {
        Node rval = null;

        if (node == null)
        {
            rval = null;
        }
        else if (node.getRight(index) != null)
        {

            // everything to the node's right is larger. The least of
            // the right node's descendents is the next larger node
            rval = leastNode(node.getRight(index), index);
        }
        else
        {

            // traverse up our ancestry until we find an ancestor that
            // is null or one whose left child is our ancestor. If we
            // find a null, then this node IS the largest node in the
            // tree, and there is no greater node. Otherwise, we are
            // the largest node in the subtree on that ancestor's left
            // ... and that ancestor is the next greatest node
            Node parent = node.getParent(index);
            Node child  = node;

            while ((parent != null) && (child == parent.getRight(index)))
            {
                child  = parent;
                parent = parent.getParent(index);
            }
            rval = parent;
        }
        return rval;
    }

    /**
     * copy the color from one node to another, dealing with the fact
     * that one or both nodes may, in fact, be null
     *
     * @param from the node whose color we're copying; may be null
     * @param to the node whose color we're changing; may be null
     * @param index _KEY or _VALUE
     */
    private static void copyColor(Node from, Node to, int index)
    {
        if (to != null)
        {
            if (from == null)
            {

                // by default, make it black
                to.setBlack(index);
            }
            else
            {
                to.copyColor(from, index);
            }
        }
    }

    /**
     * is the specified node red? if the node does not exist, no, it's
     * black, thank you
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static boolean isRed(Node node, int index)
    {
        return node == null ? false : node.isRed(index);
    }

    /**
     * is the specified black red? if the node does not exist, sure,
     * it's black, thank you
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static boolean isBlack(Node node, int index)
    {
        return node == null ? true : node.isBlack(index);
    }

    /**
     * force a node (if it exists) red
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static void makeRed(Node node, int index)
    {
        if (node != null)
        {
            node.setRed(index);
        }
    }

    /**
     * force a node (if it exists) black
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static void makeBlack(Node node, int index)
    {
        if (node != null)
        {
            node.setBlack(index);
        }
    }

    /**
     * get a node's grandparent. mind you, the node, its parent, or
     * its grandparent may not exist. no problem
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static Node getGrandParent(Node node, int index)
    {
        return getParent(getParent(node, index), index);
    }

    /**
     * get a node's parent. mind you, the node, or its parent, may not
     * exist. no problem
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static Node getParent(Node node, int index)
    {
        return ((node == null) ? null
                               : node.getParent(index));
    }

    /**
     * get a node's right child. mind you, the node may not exist. no
     * problem
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static Node getRightChild(Node node, int index)
    {
        return (node == null) ? null
                              : node.getRight(index);
    }

    /**
     * get a node's left child. mind you, the node may not exist. no
     * problem
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static Node getLeftChild(Node node, int index)
    {
        return (node == null) ? null
                              : node.getLeft(index);
    }

    /**
     * is this node its parent's left child? mind you, the node, or
     * its parent, may not exist. no problem. if the node doesn't
     * exist ... it's its non-existent parent's left child. If the
     * node does exist but has no parent ... no, we're not the
     * non-existent parent's left child. Otherwise (both the specified
     * node AND its parent exist), check.
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static boolean isLeftChild(Node node, int index) {
        if (node == null) {
            return true;
        }
        if (node.getParent(index) == null) {
            return false;
        }
        return node == node.getParent(index).getLeft(index);
    }

    /**
     * is this node its parent's right child? mind you, the node, or
     * its parent, may not exist. no problem. if the node doesn't
     * exist ... it's its non-existent parent's right child. If the
     * node does exist but has no parent ... no, we're not the
     * non-existent parent's right child. Otherwise (both the
     * specified node AND its parent exist), check.
     *
     * @param node the node (may be null) in question
     * @param index _KEY or _VALUE
     */
    private static boolean isRightChild(Node node, int index)
    {
        if (node == null) {
            return true;
        }
        if (node.getParent(index) == null) {
            return false;
        }
        return node == node.getParent(index).getRight(index);
    }

    /**
     * do a rotate left. standard fare in the world of balanced trees
     *
     * @param node the node to be rotated
     * @param index _KEY or _VALUE
     */
    private void rotateLeft(Node node, int index)
    {
        Node right_child = node.getRight(index);

        node.setRight(right_child.getLeft(index), index);
        if (right_child.getLeft(index) != null)
        {
            right_child.getLeft(index).setParent(node, index);
        }
        right_child.setParent(node.getParent(index), index);
        if (node.getParent(index) == null)
        {

            // node was the root ... now its right child is the root
            _root[ index ] = right_child;
        }
        else if (node.getParent(index).getLeft(index) == node)
        {
            node.getParent(index).setLeft(right_child, index);
        }
        else
        {
            node.getParent(index).setRight(right_child, index);
        }
        right_child.setLeft(node, index);
        node.setParent(right_child, index);
    }

    /**
     * do a rotate right. standard fare in the world of balanced trees
     *
     * @param node the node to be rotated
     * @param index _KEY or _VALUE
     */
    private void rotateRight(Node node, int index)
    {
        Node left_child = node.getLeft(index);

        node.setLeft(left_child.getRight(index), index);
        if (left_child.getRight(index) != null)
        {
            left_child.getRight(index).setParent(node, index);
        }
        left_child.setParent(node.getParent(index), index);
        if (node.getParent(index) == null)
        {

            // node was the root ... now its left child is the root
            _root[ index ] = left_child;
        }
        else if (node.getParent(index).getRight(index) == node)
        {
            node.getParent(index).setRight(left_child, index);
        }
        else
        {
            node.getParent(index).setLeft(left_child, index);
        }
        left_child.setRight(node, index);
        node.setParent(left_child, index);
    }

    /**
     * complicated red-black insert stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizeable any more
     *
     * @param inserted_node the node to be inserted
     * @param index _KEY or _VALUE
     */
    private void doRedBlackInsert(Node inserted_node, int index)
    {
        Node current_node = inserted_node;

        makeRed(current_node, index);
        while ((current_node != null) && (current_node != _root[ index ])
                && (isRed(current_node.getParent(index), index)))
        {
            if (isLeftChild(getParent(current_node, index), index))
            {
                Node y = getRightChild(getGrandParent(current_node, index),
                                       index);

                if (isRed(y, index))
                {
                    makeBlack(getParent(current_node, index), index);
                    makeBlack(y, index);
                    makeRed(getGrandParent(current_node, index), index);
                    current_node = getGrandParent(current_node, index);
                }
                else
                {
                    if (isRightChild(current_node, index))
                    {
                        current_node = getParent(current_node, index);
                        rotateLeft(current_node, index);
                    }
                    makeBlack(getParent(current_node, index), index);
                    makeRed(getGrandParent(current_node, index), index);
                    if (getGrandParent(current_node, index) != null)
                    {
                        rotateRight(getGrandParent(current_node, index),
                                    index);
                    }
                }
            }
            else
            {

                // just like clause above, except swap left for right
                Node y = getLeftChild(getGrandParent(current_node, index),
                                      index);

                if (isRed(y, index))
                {
                    makeBlack(getParent(current_node, index), index);
                    makeBlack(y, index);
                    makeRed(getGrandParent(current_node, index), index);
                    current_node = getGrandParent(current_node, index);
                }
                else
                {
                    if (isLeftChild(current_node, index))
                    {
                        current_node = getParent(current_node, index);
                        rotateRight(current_node, index);
                    }
                    makeBlack(getParent(current_node, index), index);
                    makeRed(getGrandParent(current_node, index), index);
                    if (getGrandParent(current_node, index) != null)
                    {
                        rotateLeft(getGrandParent(current_node, index),
                                   index);
                    }
                }
            }
        }
        makeBlack(_root[ index ], index);
    }

    /**
     * complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizeable any more
     *
     * @param deleted_node the node to be deleted
     */
    void doRedBlackDelete(Node deleted_node)
    {
        for (int index = _MINIMUM_INDEX; index < _INDEX_COUNT; index++)
        {

            // if deleted node has both left and children, swap with
            // the next greater node
            if ((deleted_node.getLeft(index) != null)
                    && (deleted_node.getRight(index) != null))
            {
                swapPosition(nextGreater(deleted_node, index), deleted_node,
                             index);
            }
            Node replacement = ((deleted_node.getLeft(index) != null)
                                ? deleted_node.getLeft(index)
                                : deleted_node.getRight(index));

            if (replacement != null)
            {
                replacement.setParent(deleted_node.getParent(index), index);
                if (deleted_node.getParent(index) == null)
                {
                    _root[ index ] = replacement;
                }
                else if (deleted_node
                         == deleted_node.getParent(index).getLeft(index))
                {
                    deleted_node.getParent(index).setLeft(replacement, index);
                }
                else
                {
                    deleted_node.getParent(index).setRight(replacement,
                                           index);
                }
                deleted_node.setLeft(null, index);
                deleted_node.setRight(null, index);
                deleted_node.setParent(null, index);
                if (isBlack(deleted_node, index))
                {
                    doRedBlackDeleteFixup(replacement, index);
                }
            }
            else
            {

                // replacement is null
                if (deleted_node.getParent(index) == null)
                {

                    // empty tree
                    _root[ index ] = null;
                }
                else
                {

                    // deleted node had no children
                    if (isBlack(deleted_node, index))
                    {
                        doRedBlackDeleteFixup(deleted_node, index);
                    }
                    if (deleted_node.getParent(index) != null)
                    {
                        if (deleted_node
                                == deleted_node.getParent(index)
                                    .getLeft(index))
                        {
                            deleted_node.getParent(index).setLeft(null,
                                                   index);
                        }
                        else
                        {
                            deleted_node.getParent(index).setRight(null,
                                                   index);
                        }
                        deleted_node.setParent(null, index);
                    }
                }
            }
        }
        shrink();
    }

    /**
     * complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizeable any more. This
     * rebalances the tree (somewhat, as red-black trees are not
     * perfectly balanced -- perfect balancing takes longer)
     *
     * @param replacement_node  the node being replaced
     * @param index _KEY or _VALUE
     */
    private void doRedBlackDeleteFixup(Node replacement_node,
                                       int index)
    {
        Node current_node = replacement_node;

        while ((current_node != _root[ index ])
                && (isBlack(current_node, index)))
        {
            if (isLeftChild(current_node, index))
            {
                Node sibling_node =
                    getRightChild(getParent(current_node, index), index);

                if (isRed(sibling_node, index))
                {
                    makeBlack(sibling_node, index);
                    makeRed(getParent(current_node, index), index);
                    rotateLeft(getParent(current_node, index), index);
                    sibling_node =
                        getRightChild(getParent(current_node, index), index);
                }
                if (isBlack(getLeftChild(sibling_node, index), index)
                        && isBlack(getRightChild(sibling_node, index), index))
                {
                    makeRed(sibling_node, index);
                    current_node = getParent(current_node, index);
                }
                else
                {
                    if (isBlack(getRightChild(sibling_node, index), index))
                    {
                        makeBlack(getLeftChild(sibling_node, index), index);
                        makeRed(sibling_node, index);
                        rotateRight(sibling_node, index);
                        sibling_node =
                            getRightChild(getParent(current_node, index),
                                          index);
                    }
                    copyColor(getParent(current_node, index), sibling_node,
                              index);
                    makeBlack(getParent(current_node, index), index);
                    makeBlack(getRightChild(sibling_node, index), index);
                    rotateLeft(getParent(current_node, index), index);
                    current_node = _root[ index ];
                }
            }
            else
            {
                Node sibling_node =
                    getLeftChild(getParent(current_node, index), index);

                if (isRed(sibling_node, index))
                {
                    makeBlack(sibling_node, index);
                    makeRed(getParent(current_node, index), index);
                    rotateRight(getParent(current_node, index), index);
                    sibling_node =
                        getLeftChild(getParent(current_node, index), index);
                }
                if (isBlack(getRightChild(sibling_node, index), index)
                        && isBlack(getLeftChild(sibling_node, index), index))
                {
                    makeRed(sibling_node, index);
                    current_node = getParent(current_node, index);
                }
                else
                {
                    if (isBlack(getLeftChild(sibling_node, index), index))
                    {
                        makeBlack(getRightChild(sibling_node, index), index);
                        makeRed(sibling_node, index);
                        rotateLeft(sibling_node, index);
                        sibling_node =
                            getLeftChild(getParent(current_node, index),
                                         index);
                    }
                    copyColor(getParent(current_node, index), sibling_node,
                              index);
                    makeBlack(getParent(current_node, index), index);
                    makeBlack(getLeftChild(sibling_node, index), index);
                    rotateRight(getParent(current_node, index), index);
                    current_node = _root[ index ];
                }
            }
        }
        makeBlack(current_node, index);
    }

    /**
     * swap two nodes (except for their content), taking care of
     * special cases where one is the other's parent ... hey, it
     * happens.
     *
     * @param x one node
     * @param y another node
     * @param index _KEY or _VALUE
     */
    private void swapPosition(Node x, Node y, int index)
    {

        // Save initial values.
        Node    x_old_parent      = x.getParent(index);
        Node    x_old_left_child  = x.getLeft(index);
        Node    x_old_right_child = x.getRight(index);
        Node    y_old_parent      = y.getParent(index);
        Node    y_old_left_child  = y.getLeft(index);
        Node    y_old_right_child = y.getRight(index);
        boolean x_was_left_child  =
            (x.getParent(index) != null)
            && (x == x.getParent(index).getLeft(index));
        boolean y_was_left_child  =
            (y.getParent(index) != null)
            && (y == y.getParent(index).getLeft(index));

        // Swap, handling special cases of one being the other's parent.
        if (x == y_old_parent)
        {   // x was y's parent
            x.setParent(y, index);
            if (y_was_left_child)
            {
                y.setLeft(x, index);
                y.setRight(x_old_right_child, index);
            }
            else
            {
                y.setRight(x, index);
                y.setLeft(x_old_left_child, index);
            }
        }
        else
        {
            x.setParent(y_old_parent, index);
            if (y_old_parent != null)
            {
                if (y_was_left_child)
                {
                    y_old_parent.setLeft(x, index);
                }
                else
                {
                    y_old_parent.setRight(x, index);
                }
            }
            y.setLeft(x_old_left_child, index);
            y.setRight(x_old_right_child, index);
        }
        if (y == x_old_parent)
        {   // y was x's parent
            y.setParent(x, index);
            if (x_was_left_child)
            {
                x.setLeft(y, index);
                x.setRight(y_old_right_child, index);
            }
            else
            {
                x.setRight(y, index);
                x.setLeft(y_old_left_child, index);
            }
        }
        else
        {
            y.setParent(x_old_parent, index);
            if (x_old_parent != null)
            {
                if (x_was_left_child)
                {
                    x_old_parent.setLeft(y, index);
                }
                else
                {
                    x_old_parent.setRight(y, index);
                }
            }
            x.setLeft(y_old_left_child, index);
            x.setRight(y_old_right_child, index);
        }

        // Fix children's parent pointers
        if (x.getLeft(index) != null)
        {
            x.getLeft(index).setParent(x, index);
        }
        if (x.getRight(index) != null)
        {
            x.getRight(index).setParent(x, index);
        }
        if (y.getLeft(index) != null)
        {
            y.getLeft(index).setParent(y, index);
        }
        if (y.getRight(index) != null)
        {
            y.getRight(index).setParent(y, index);
        }
        x.swapColors(y, index);

        // Check if _root changed
        if (_root[ index ] == x)
        {
            _root[ index ] = y;
        }
        else if (_root[ index ] == y)
        {
            _root[ index ] = x;
        }
    }

    /**
     * check if an object is fit to be proper input ... has to be
     * Comparable and non-null
     *
     * @param o the object being checked
     * @param index _KEY or _VALUE (used to put the right word in the
     *              exception message)
     *
     * @exception NullPointerException if o is null
     * @exception ClassCastException if o is not Comparable
     */
    private static void checkNonNullComparable(Object o,
                                               int index)
    {
        if (o == null)
        {
            throw new NullPointerException(_data_name[ index ]
                                           + " cannot be null");
        }
        if (!(o instanceof Comparable))
        {
            throw new ClassCastException(_data_name[ index ]
                                         + " must be Comparable");
        }
    }

    /**
     * check a key for validity (non-null and implements Comparable)
     *
     * @param key the key to be checked
     *
     * @exception NullPointerException if key is null
     * @exception ClassCastException if key is not Comparable
     */
    private static void checkKey(Object key)
    {
        checkNonNullComparable(key, _KEY);
    }

    /**
     * check a value for validity (non-null and implements Comparable)
     *
     * @param value the value to be checked
     *
     * @exception NullPointerException if value is null
     * @exception ClassCastException if value is not Comparable
     */
    private static void checkValue(Object value)
    {
        checkNonNullComparable(value, _VALUE);
    }

    /**
     * check a key and a value for validity (non-null and implements
     * Comparable)
     *
     * @param key the key to be checked
     * @param value the value to be checked
     *
     * @exception NullPointerException if key or value is null
     * @exception ClassCastException if key or value is not Comparable
     */
    private static void checkKeyAndValue(Object key, Object value)
    {
        checkKey(key);
        checkValue(value);
    }

    /**
     * increment the modification count -- used to check for
     * concurrent modification of the map through the map and through
     * an Iterator from one of its Set or Collection views
     */
    private void modify()
    {
        _modifications++;
    }

    /**
     * bump up the size and note that the map has changed
     */
    private void grow()
    {
        modify();
        _size++;
    }

    /**
     * decrement the size and note that the map has changed
     */
    private void shrink()
    {
        modify();
        _size--;
    }

    /**
     * insert a node by its value
     *
     * @param newNode the node to be inserted
     *
     * @exception IllegalArgumentException if the node already exists
     *                                     in the value mapping
     */
    private void insertValue(Node newNode)
        throws IllegalArgumentException
    {
        Node node = _root[ _VALUE ];

        while (true)
        {
            int cmp = compare(newNode.getData(_VALUE), node.getData(_VALUE));

            if (cmp == 0)
            {
                throw new IllegalArgumentException(
                    "Cannot store a duplicate value (\""
                    + newNode.getData(_VALUE) + "\") in this Map");
            }
            else if (cmp < 0)
            {
                if (node.getLeft(_VALUE) != null)
                {
                    node = node.getLeft(_VALUE);
                }
                else
                {
                    node.setLeft(newNode, _VALUE);
                    newNode.setParent(node, _VALUE);
                    doRedBlackInsert(newNode, _VALUE);
                    break;
                }
            }
            else
            {   // cmp > 0
                if (node.getRight(_VALUE) != null)
                {
                    node = node.getRight(_VALUE);
                }
                else
                {
                    node.setRight(newNode, _VALUE);
                    newNode.setParent(node, _VALUE);
                    doRedBlackInsert(newNode, _VALUE);
                    break;
                }
            }
        }
    }

    /* ********** START implementation of Map ********** */

    /**
     * Returns the number of key-value mappings in this map. If the
     * map contains more than Integer.MAX_VALUE elements, returns
     * Integer.MAX_VALUE.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        return _size;
    }

    /**
     * Returns true if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     *
     * @return true if this map contains a mapping for the specified
     *         key.
     *
     * @exception ClassCastException if the key is of an inappropriate
     *                               type for this map.
     * @exception NullPointerException if the key is null
     */
    public boolean containsKey(Object key)
        throws ClassCastException, NullPointerException
    {
        checkKey(key);
        return lookup(( Comparable ) key, _KEY) != null;
    }

    /**
     * Returns true if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     *
     * @return true if this map maps one or more keys to the specified
     *         value.
     */
    public boolean containsValue(Object value)
    {
        checkValue(value);
        return lookup(( Comparable ) value, _VALUE) != null;
    }

    /**
     * Returns the value to which this map maps the specified
     * key. Returns null if the map contains no mapping for this key.
     *
     * @param key key whose associated value is to be returned.
     *
     * @return the value to which this map maps the specified key, or
     *         null if the map contains no mapping for this key.
     *
     * @exception ClassCastException if the key is of an inappropriate
     *                               type for this map.
     * @exception NullPointerException if the key is null
     */
    public Object get(Object key)
        throws ClassCastException, NullPointerException
    {
        return doGet(( Comparable ) key, _KEY);
    }

    /**
     * Associates the specified value with the specified key in this
     * map.
     *
     * @param key key with which the specified value is to be
     *            associated.
     * @param value value to be associated with the specified key.
     *
     * @return null
     *
     * @exception ClassCastException if the class of the specified key
     *                               or value prevents it from being
     *                               stored in this map.
     * @exception NullPointerException if the specified key or value
     *                                 is null
     * @exception IllegalArgumentException if the key duplicates an
     *                                     existing key, or if the
     *                                     value duplicates an
     *                                     existing value
     */
    public Object put(Object key, Object value)
        throws ClassCastException, NullPointerException,
                IllegalArgumentException
    {
        checkKeyAndValue(key, value);
        Node node = _root[ _KEY ];

        if (node == null)
        {
            Node root = new Node(( Comparable ) key, ( Comparable ) value);

            _root[ _KEY ]   = root;
            _root[ _VALUE ] = root;
            grow();
        }
        else
        {
            while (true)
            {
                int cmp = compare(( Comparable ) key, node.getData(_KEY));

                if (cmp == 0)
                {
                    throw new IllegalArgumentException(
                        "Cannot store a duplicate key (\"" + key
                        + "\") in this Map");
                }
                else if (cmp < 0)
                {
                    if (node.getLeft(_KEY) != null)
                    {
                        node = node.getLeft(_KEY);
                    }
                    else
                    {
                        Node newNode = new Node(( Comparable ) key,
                                                ( Comparable ) value);

                        insertValue(newNode);
                        node.setLeft(newNode, _KEY);
                        newNode.setParent(node, _KEY);
                        doRedBlackInsert(newNode, _KEY);
                        grow();
                        break;
                    }
                }
                else
                {   // cmp > 0
                    if (node.getRight(_KEY) != null)
                    {
                        node = node.getRight(_KEY);
                    }
                    else
                    {
                        Node newNode = new Node(( Comparable ) key,
                                                ( Comparable ) value);

                        insertValue(newNode);
                        node.setRight(newNode, _KEY);
                        newNode.setParent(node, _KEY);
                        doRedBlackInsert(newNode, _KEY);
                        grow();
                        break;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Removes the mapping for this key from this map if present
     *
     * @param key key whose mapping is to be removed from the map.
     *
     * @return previous value associated with specified key, or null
     *         if there was no mapping for key.
     */
    public Object remove(Object key)
    {
        return doRemove(( Comparable ) key, _KEY);
    }

    /**
     * Removes all mappings from this map
     */
    public void clear()
    {
        modify();
        _size           = 0;
        _root[ _KEY ]   = null;
        _root[ _VALUE ] = null;
    }

    /**
     * Returns a set view of the keys contained in this map.  The set
     * is backed by the map, so changes to the map are reflected in
     * the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress, the results of the
     * iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * Iterator.remove, Set.remove, removeAll, retainAll, and clear
     * operations.  It does not support the add or addAll operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set keySet()
    {
        if (_key_set[ _KEY ] == null)
        {
            _key_set[ _KEY ] = new AbstractSet()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_KEY)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node.getData(_KEY);
                        }
                    };
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public boolean contains(Object o)
                {
                    return containsKey(o);
                }

                public boolean remove(Object o)
                {
                    int old_size = _size;

                    BinaryTree.this.remove(o);
                    return _size != old_size;
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _key_set[ _KEY ];
    }

    /**
     * Returns a collection view of the values contained in this
     * map. The collection is backed by the map, so changes to the map
     * are reflected in the collection, and vice-versa. If the map is
     * modified while an iteration over the collection is in progress,
     * the results of the iteration are undefined. The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the Iterator.remove,
     * Collection.remove, removeAll, retainAll and clear operations.
     * It does not support the add or addAll operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values()
    {
        if (_value_collection[ _KEY ] == null)
        {
            _value_collection[ _KEY ] = new AbstractCollection()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_KEY)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node.getData(_VALUE);
                        }
                    };
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public boolean contains(Object o)
                {
                    return containsValue(o);
                }

                public boolean remove(Object o)
                {
                    int old_size = _size;

                    removeValue(o);
                    return _size != old_size;
                }

                public boolean removeAll(Collection c)
                {
                    boolean  modified = false;
                    Iterator iter     = c.iterator();

                    while (iter.hasNext())
                    {
                        if (removeValue(iter.next()) != null)
                        {
                            modified = true;
                        }
                    }
                    return modified;
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _value_collection[ _KEY ];
    }

    /**
     * Returns a set view of the mappings contained in this map. Each
     * element in the returned set is a Map.Entry. The set is backed
     * by the map, so changes to the map are reflected in the set, and
     * vice-versa.  If the map is modified while an iteration over the
     * set is in progress, the results of the iteration are
     * undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the Iterator.remove,
     * Set.remove, removeAll, retainAll and clear operations.  It does
     * not support the add or addAll operations.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set entrySet()
    {
        if (_entry_set[ _KEY ] == null)
        {
            _entry_set[ _KEY ] = new AbstractSet()
            {
                public Iterator iterator()
                {
                    return new BinaryTreeIterator(_KEY)
                    {
                        protected Object doGetNext()
                        {
                            return _last_returned_node;
                        }
                    };
                }

                public boolean contains(Object o)
                {
                    if (!(o instanceof Map.Entry))
                    {
                        return false;
                    }
                    Map.Entry entry = ( Map.Entry ) o;
                    Object    value = entry.getValue();
                    Node      node  = lookup(( Comparable ) entry.getKey(),
                                             _KEY);

                    return (node != null)
                           && node.getData(_VALUE).equals(value);
                }

                public boolean remove(Object o)
                {
                    if (!(o instanceof Map.Entry))
                    {
                        return false;
                    }
                    Map.Entry entry = ( Map.Entry ) o;
                    Object    value = entry.getValue();
                    Node      node  = lookup(( Comparable ) entry.getKey(),
                                             _KEY);

                    if ((node != null) && node.getData(_VALUE).equals(value))
                    {
                        doRedBlackDelete(node);
                        return true;
                    }
                    return false;
                }

                public int size()
                {
                    return BinaryTree.this.size();
                }

                public void clear()
                {
                    BinaryTree.this.clear();
                }
            };
        }
        return _entry_set[ _KEY ];
    }

    /* **********  END  implementation of Map ********** */
    private abstract class BinaryTreeIterator
        implements Iterator
    {
        private int    _expected_modifications;
        protected Node _last_returned_node;
        private Node   _next_node;
        private int    _type;

        /**
         * Constructor
         *
         * @param type
         */
        BinaryTreeIterator(int type)
        {
            _type                   = type;
            _expected_modifications = BinaryTree.this._modifications;
            _last_returned_node     = null;
            _next_node              = leastNode(_root[ _type ], _type);
        }

        /**
         * @return 'next', whatever that means for a given kind of
         *         BinaryTreeIterator
         */

        protected abstract Object doGetNext();

        /* ********** START implementation of Iterator ********** */

        /**
         * @return true if the iterator has more elements.
         */

        public boolean hasNext()
        {
            return _next_node != null;
        }

        /**
         * @return the next element in the iteration.
         *
         * @exception NoSuchElementException if iteration has no more
         *                                   elements.
         * @exception ConcurrentModificationException if the
         *                                            BinaryTree is
         *                                            modified behind
         *                                            the iterator's
         *                                            back
         */

        public Object next()
            throws NoSuchElementException, ConcurrentModificationException
        {
            if (_next_node == null)
            {
                throw new NoSuchElementException();
            }
            if (_modifications != _expected_modifications)
            {
                throw new ConcurrentModificationException();
            }
            _last_returned_node = _next_node;
            _next_node          = nextGreater(_next_node, _type);
            return doGetNext();
        }

        /**
         * Removes from the underlying collection the last element
         * returned by the iterator. This method can be called only
         * once per call to next. The behavior of an iterator is
         * unspecified if the underlying collection is modified while
         * the iteration is in progress in any way other than by
         * calling this method.
         *
         * @exception IllegalStateException if the next method has not
         *                                  yet been called, or the
         *                                  remove method has already
         *                                  been called after the last
         *                                  call to the next method.
         * @exception ConcurrentModificationException if the
         *                                            BinaryTree is
         *                                            modified behind
         *                                            the iterator's
         *                                            back
         */

        public void remove()
            throws IllegalStateException, ConcurrentModificationException
        {
            if (_last_returned_node == null)
            {
                throw new IllegalStateException();
            }
            if (_modifications != _expected_modifications)
            {
                throw new ConcurrentModificationException();
            }
            doRedBlackDelete(_last_returned_node);
            _expected_modifications++;
            _last_returned_node = null;
        }

        /* **********  END  implementation of Iterator ********** */
    }   // end private abstract class BinaryTreeIterator

    // for performance
    private static final class Node
        implements Map.Entry
    {
        private Comparable[] _data;
        private Node[]       _left;
        private Node[]       _right;
        private Node[]       _parent;
        private boolean[]    _black;
        private int          _hashcode;
        private boolean      _calculated_hashcode;

        /**
         * Make a new cell with given key and value, and with null
         * links, and black (true) colors.
         *
         * @param key
         * @param value
         */

        Node(Comparable key, Comparable value)
        {
            _data                = new Comparable[]
            {
                key, value
            };
            _left                = new Node[]
            {
                null, null
            };
            _right               = new Node[]
            {
                null, null
            };
            _parent              = new Node[]
            {
                null, null
            };
            _black               = new boolean[]
            {
                true, true
            };
            _calculated_hashcode = false;
        }

        /**
         * get the specified data
         *
         * @param index _KEY or _VALUE
         *
         * @return the key or value
         */
        public Comparable getData(int index)
        {
            return _data[ index ];
        }

        /**
         * Set this node's left node
         *
         * @param node the new left node
         * @param index _KEY or _VALUE
         */
        public void setLeft(Node node, int index)
        {
            _left[ index ] = node;
        }

        /**
         * get the left node
         *
         * @param index _KEY or _VALUE
         *
         * @return the left node -- may be null
         */

        public Node getLeft(int index)
        {
            return _left[ index ];
        }

        /**
         * Set this node's right node
         *
         * @param node the new right node
         * @param index _KEY or _VALUE
         */
        public void setRight(Node node, int index)
        {
            _right[ index ] = node;
        }

        /**
         * get the right node
         *
         * @param index _KEY or _VALUE
         *
         * @return the right node -- may be null
         */

        public Node getRight(int index)
        {
            return _right[ index ];
        }

        /**
         * Set this node's parent node
         *
         * @param node the new parent node
         * @param index _KEY or _VALUE
         */
        public void setParent(Node node, int index)
        {
            _parent[ index ] = node;
        }

        /**
         * get the parent node
         *
         * @param index _KEY or _VALUE
         *
         * @return the parent node -- may be null
         */
        public Node getParent(int index)
        {
            return _parent[ index ];
        }

        /**
         * exchange colors with another node
         *
         * @param node the node to swap with
         * @param index _KEY or _VALUE
         */
        public void swapColors(Node node, int index)
        {

            // Swap colors -- old hacker's trick
            _black[ index ]      ^= node._black[ index ];
            node._black[ index ] ^= _black[ index ];
            _black[ index ]      ^= node._black[ index ];
        }

        /**
         * is this node black?
         *
         * @param index _KEY or _VALUE
         *
         * @return true if black (which is represented as a true boolean)
         */
        public boolean isBlack(int index)
        {
            return _black[ index ];
        }

        /**
         * is this node red?
         *
         * @param index _KEY or _VALUE
         *
         * @return true if non-black
         */
        public boolean isRed(int index)
        {
            return !_black[ index ];
        }

        /**
         * make this node black
         *
         * @param index _KEY or _VALUE
         */
        public void setBlack(int index)
        {
            _black[ index ] = true;
        }

        /**
         * make this node red
         *
         * @param index _KEY or _VALUE
         */
        public void setRed(int index)
        {
            _black[ index ] = false;
        }

        /**
         * make this node the same color as another
         *
         * @param node the node whose color we're adopting
         * @param index _KEY or _VALUE
         */
        public void copyColor(Node node, int index)
        {
            _black[ index ] = node._black[ index ];
        }

        /* ********** START implementation of Map.Entry ********** */

        /**
         * @return the key corresponding to this entry.
         */
        public Object getKey()
        {
            return _data[ _KEY ];
        }

        /**
         * @return the value corresponding to this entry.
         */
        public Object getValue()
        {
            return _data[ _VALUE ];
        }

        /**
         * Optional operation that is not permitted in this
         * implementation
         *
         * @param ignored
         *
         * @return does not return
         */
        public Object setValue(Object ignored)
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException(
                "Map.Entry.setValue is not supported");
        }

        /**
         * Compares the specified object with this entry for equality.
         * Returns true if the given object is also a map entry and
         * the two entries represent the same mapping.
         *
         * @param o object to be compared for equality with this map
         *          entry.
         * @return true if the specified object is equal to this map
         *         entry.
         */
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry e = ( Map.Entry ) o;

            return _data[ _KEY ].equals(e.getKey())
                   && _data[ _VALUE ].equals(e.getValue());
        }

        /**
         * @return the hash code value for this map entry.
         */

        public int hashCode()
        {
            if (!_calculated_hashcode)
            {
                _hashcode            = _data[ _KEY ].hashCode()
                                       ^ _data[ _VALUE ].hashCode();
                _calculated_hashcode = true;
            }
            return _hashcode;
        }

        /* **********  END  implementation of Map.Entry ********** */
    }
}
