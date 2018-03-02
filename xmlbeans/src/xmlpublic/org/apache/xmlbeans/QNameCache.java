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

import javax.xml.namespace.QName;

/**
 * A cache that can be used to pool QName instances.  Each thread has one.
 */ 
public final class QNameCache
{
    private static final float DEFAULT_LOAD = 0.70f;
    private final float loadFactor;
    private int numEntries = 0;
    private int threshold;
    private int hashmask;
    private QName[] table;

    /**
     * Creates a QNameCache with the given initialCapacity and loadFactor.
     * 
     * @param initialCapacity the number of entries to initially make space for
     * @param loadFactor a number to control the density of the hashtable
     */ 
    public QNameCache(int initialCapacity, float loadFactor)
    {
        assert initialCapacity > 0;
        assert loadFactor > 0 && loadFactor < 1;

        // Find a power of 2 >= initialCapacity
        int capacity = 16;
        while (capacity < initialCapacity) 
            capacity <<= 1;
    
        this.loadFactor = loadFactor;
        this.hashmask = capacity - 1;
        threshold = (int)(capacity * loadFactor);
        table = new QName[capacity];
    }

    /**
     * Creates a QNameCache with the given initialCapacity.
     * 
     * @param initialCapacity the number of entries to initially make space for
     */ 
    public QNameCache(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD);
    }

    public QName getName(String uri, String localName)
    {
        return getName( uri, localName, "" );
    }
    
    /**
     * Fetches a QName with the given namespace and localname.
     * Creates one if one is not found in the cache.
     * 
     * @param uri the namespace
     * @param localName the localname
     * @param prefix the prefix
     * @return the cached QName
     */ 
    public QName getName(String uri, String localName, String prefix)
    {
        /*
        return new QName(uri, localName, prefix);
        */
        assert localName != null;
        
        if (uri == null) uri = "";
        if (prefix == null) prefix = "";

        int index = hash(uri, localName, prefix) & hashmask;
        while (true) {
            QName q = table[index];
            if (q == null)
            {
                numEntries++;
                if (numEntries >= threshold)
                    rehash();

                return table[index] = new QName(uri, localName, prefix);
            }
            else if (equals(q, uri, localName, prefix))
                return q;
            else 
                index = (index-1) & hashmask;
        }
    }

    private void rehash()
    {
        int newLength = table.length * 2;
        QName[] newTable = new QName[newLength];
        int newHashmask = newLength - 1;

        for (int i = 0 ; i < table.length ; i++)
        {
            QName q = table[i];
            if (q != null)
            {
                int newIndex =
                    hash( q.getNamespaceURI(), q.getLocalPart(), q.getPrefix() ) & newHashmask;
                
                while (newTable[newIndex] != null)
                    newIndex = (newIndex - 1) & newHashmask;
                
                newTable[newIndex] = q;
            }
        }

        table = newTable;
        hashmask = newHashmask;
        threshold = (int) (newLength * loadFactor);
    }
    
    private static int hash(String uri, String localName, String prefix)
    {
        int h = 0;

        h += prefix.hashCode() << 10;
        h += uri.hashCode() << 5;
        h += localName.hashCode();

        return h;
    }

    private static boolean equals(QName q, String uri, String localName, String prefix)
    {
        return
            q.getLocalPart().equals(localName) &&
                q.getNamespaceURI().equals(uri) &&
                    q.getPrefix().equals(prefix);
    }
}
