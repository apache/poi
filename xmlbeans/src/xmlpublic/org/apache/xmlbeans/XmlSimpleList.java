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

package org.apache.xmlbeans;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * The immutable {@link List} returned for XML simple list values.
 * 
 * XmlSimpleList implements an equals() and hashCode() that compare list
 * contents, so two XmlSimpleLists are the same if they have the same
 * values in the same order.
 */ 
public class XmlSimpleList implements List, java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
	private List underlying;
    
    /**
     * Constructs an immutable XmlSimpleList that wraps (does not copy)
     * the given {@link List}.  All non-mutating methods delegate to
     * the underlying List instance.
     */ 
	public XmlSimpleList(List list)     { this.underlying = list; }
    
    /** Returns the number of elements in this list. */
    public int size() 		            { return underlying.size(); }
    /** True if the list is empty. */
    public boolean isEmpty() 	        { return underlying.isEmpty(); }
    /** True if the list is contains an object equal to o. */
    public boolean contains(Object o)   { return underlying.contains(o); }
    /** True if the list is contains all the objects in the given collection. */
    public boolean containsAll(Collection coll) { return underlying.containsAll(coll); }
    /** Copies the collection to an array. */
    public Object[] toArray() 	        { return underlying.toArray(); }
    /** Copies the collection to an array of a specified type. */
    public Object[] toArray(Object[] a) { return underlying.toArray(a); }
    
    /** Unsupported because this list is immutable. */
    public boolean add(Object o)        { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public boolean addAll(Collection coll) { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public boolean remove(Object o)     { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public boolean removeAll(Collection coll) { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public boolean retainAll(Collection coll) { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public void clear()                 { throw new UnsupportedOperationException(); }

    /** Returns the object at the specified position in this list. */
    public Object get(int index)        { return underlying.get(index); }
    /** Unsupported because this list is immutable. */
    public Object set(int index, Object element) { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public void add(int index, Object element) { throw new UnsupportedOperationException(); }
    /** Unsupported because this list is immutable. */
    public Object remove(int index)     { throw new UnsupportedOperationException(); }
    /** Returns index of the first occurance of an object equal to o. */
    public int indexOf(Object o)        { return underlying.indexOf(o); }
    /** Returns index of the last occurance of an object equal to o. */
    public int lastIndexOf(Object o)    { return underlying.lastIndexOf(o); }
    /** Unsupported because this list is immutable. */
    public boolean addAll(int index, Collection c) { throw new UnsupportedOperationException(); }
    /** Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive. */
    public List subList(int from, int to) { return new XmlSimpleList(underlying.subList(from, to)); }

    /** Returns an iterator over the elements in this list in proper sequence. */
    public Iterator iterator()
    { 
        return new Iterator()
        { 
            Iterator i = underlying.iterator();
            public boolean hasNext()    { return i.hasNext(); }
            public Object next() 	    { return i.next(); }
            public void remove()        { throw new UnsupportedOperationException(); }
        };
    }

    /** Returns a list iterator of the elements in this list in proper sequence. */
    public ListIterator listIterator() 	{ return listIterator(0); }
    /** Returns a list iterator of the elements in this list in proper sequence, starting at the specified position in this list. */
    public ListIterator listIterator(final int index)
    { 
        return new ListIterator()
        { 
            ListIterator i = underlying.listIterator(index);
    
            public boolean hasNext()    { return i.hasNext(); }
            public Object next()        { return i.next(); }
            public boolean hasPrevious(){ return i.hasPrevious(); }
            public Object previous()    { return i.previous(); }
            public int nextIndex()      { return i.nextIndex(); }
            public int previousIndex()  { return i.previousIndex(); }
    
            public void remove()        { throw new UnsupportedOperationException(); }
            public void set(Object o)   { throw new UnsupportedOperationException(); }
            public void add(Object o)   { throw new UnsupportedOperationException(); }
        };
    }
    
    private String stringValue(Object o)
    {
        if (o instanceof SimpleValue)
            return ((SimpleValue)o).stringValue();
        return o.toString();
    }
    
    /**
     * Returns a space-separated list of the string representations of all
     * the items in the list.  For most lists, this is a valid xml lexical
     * value for the list. (The notable exception is a list of QNames.)
     */ 
    public String toString()
    {
        int size = underlying.size();
        if (size == 0)
            return "";
        String first = stringValue(underlying.get(0));
        if (size == 1)
            return first;
        StringBuffer result = new StringBuffer(first);
        for (int i = 1; i < size; i++)
        {
            result.append(' ');
            result.append(stringValue(underlying.get(i)));
        }
        return result.toString();
    }
    
    /**
     * Two XmlSimpleLists are equal if all their items are equal.
     * (They must have the same number of items, and the items must be in
     * the same order.)
     */ 
    public boolean equals(Object o)
    { 
        if (this == o) return true;
        if (!(o instanceof XmlSimpleList)) return false;
        final XmlSimpleList xmlSimpleList = (XmlSimpleList)o;
        List underlying2 = xmlSimpleList.underlying;
        int size = underlying.size();
        if (size != underlying2.size())
            return false;
        for (int i = 0; i < size; i++)
        { 
            Object item = underlying.get(i);
            Object item2 = underlying2.get(i);
            if (item == null ? item2 != null : !item.equals(item2))
                return false;
        }
        return true;
    }
    
    /**
     * Combines the hash codes of all the list items.
     */ 
    public int hashCode()
    { 
        int size = underlying.size();
        int hash = 0;
        for (int i = 0; i < size; i++)
        { 
            Object item = underlying.get(i);
            hash *= 19;
            hash += item.hashCode();
        }
        return hash;
     }
}
