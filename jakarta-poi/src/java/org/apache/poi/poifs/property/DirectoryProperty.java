
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.poifs.property;

import java.util.*;

import java.io.IOException;

import org.apache.poi.poifs.storage.SmallDocumentBlock;

/**
 * Directory property
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class DirectoryProperty
    extends Property
    implements Parent
{

    // List of Property instances
    private List _children;

    // set of children's names
    private Set  _children_names;

    /**
     * Default constructor
     *
     * @param name the name of the directory
     */

    public DirectoryProperty(String name)
    {
        super();
        _children       = new ArrayList();
        _children_names = new HashSet();
        setName(name);
        setSize(0);
        setPropertyType(PropertyConstants.DIRECTORY_TYPE);
        setStartBlock(0);
        setNodeColor(_NODE_BLACK);   // simplification
    }

    /**
     * reader constructor
     *
     * @param index index number
     * @param array byte data
     * @param offset offset into byte data
     */

    protected DirectoryProperty(final int index, final byte [] array,
                                final int offset)
    {
        super(index, array, offset);
        _children       = new ArrayList();
        _children_names = new HashSet();
    }

    /**
     * Change a Property's name
     *
     * @param property the Property whose name is being changed
     * @param newName the new name for the Property
     *
     * @return true if the name change could be made, else false
     */

    public boolean changeName(final Property property, final String newName)
    {
        boolean result;
        String  oldName = property.getName();

        property.setName(newName);
        String cleanNewName = property.getName();

        if (_children_names.contains(cleanNewName))
        {

            // revert the change
            property.setName(oldName);
            result = false;
        }
        else
        {
            _children_names.add(cleanNewName);
            _children_names.remove(oldName);
            result = true;
        }
        return result;
    }

    /**
     * Delete a Property
     *
     * @param property the Property being deleted
     *
     * @return true if the Property could be deleted, else false
     */

    public boolean deleteChild(final Property property)
    {
        boolean result = _children.remove(property);

        if (result)
        {
            _children_names.remove(property.getName());
        }
        return result;
    }

    private class PropertyComparator
        implements Comparator
    {

        /**
         * Object equality, implemented as object identity
         *
         * @param o Object we're being compared to
         *
         * @return true if identical, else false
         */

        public boolean equals(Object o)
        {
            return this == o;
        }

        /**
         * compare method. Assumes both parameters are non-null
         * instances of Property. One property is less than another if
         * its name is shorter than the other property's name. If the
         * names are the same length, the property whose name comes
         * before the other property's name, alphabetically, is less
         * than the other property.
         *
         * @param o1 first object to compare, better be a Property
         * @param o2 second object to compare, better be a Property
         *
         * @return negative value if o1 <  o2,
         *         zero           if o1 == o2,
         *         positive value if o1 >  o2.
         */

        public int compare(Object o1, Object o2)
        {
            String name1  = (( Property ) o1).getName();
            String name2  = (( Property ) o2).getName();
            int    result = name1.length() - name2.length();

            if (result == 0)
            {
                result = name1.compareTo(name2);
            }
            return result;
        }
    }   // end private class PropertyComparator

    /* ********** START extension of Property ********** */

    /**
     * @return true if a directory type Property
     */

    public boolean isDirectory()
    {
        return true;
    }

    /**
     * Perform whatever activities need to be performed prior to
     * writing
     */

    protected void preWrite()
    {
        if (_children.size() > 0)
        {
            Property[] children =
                ( Property [] ) _children.toArray(new Property[ 0 ]);

            Arrays.sort(children, new PropertyComparator());
            int midpoint = children.length / 2;

            setChildProperty(children[ midpoint ].getIndex());
            children[ 0 ].setPreviousChild(null);
            children[ 0 ].setNextChild(null);
            for (int j = 1; j < midpoint; j++)
            {
                children[ j ].setPreviousChild(children[ j - 1 ]);
                children[ j ].setNextChild(null);
            }
            if (midpoint != 0)
            {
                children[ midpoint ]
                    .setPreviousChild(children[ midpoint - 1 ]);
            }
            if (midpoint != (children.length - 1))
            {
                children[ midpoint ].setNextChild(children[ midpoint + 1 ]);
                for (int j = midpoint + 1; j < children.length - 1; j++)
                {
                    children[ j ].setPreviousChild(null);
                    children[ j ].setNextChild(children[ j + 1 ]);
                }
                children[ children.length - 1 ].setPreviousChild(null);
                children[ children.length - 1 ].setNextChild(null);
            }
            else
            {
                children[ midpoint ].setNextChild(null);
            }
        }
    }

    /* **********  END  extension of Property ********** */
    /* ********** START implementation of Parent ********** */

    /**
     * Get an iterator over the children of this Parent; all elements
     * are instances of Property.
     *
     * @return Iterator of children; may refer to an empty collection
     */

    public Iterator getChildren()
    {
        return _children.iterator();
    }

    /**
     * Add a new child to the collection of children
     *
     * @param property the new child to be added; must not be null
     *
     * @exception IOException if we already have a child with the same
     *                        name
     */

    public void addChild(final Property property)
        throws IOException
    {
        String name = property.getName();

        if (_children_names.contains(name))
        {
            throw new IOException("Duplicate name \"" + name + "\"");
        }
        _children_names.add(name);
        _children.add(property);
    }

    /* **********  END  implementation of Parent ********** */
}   // end public class DirectoryProperty

