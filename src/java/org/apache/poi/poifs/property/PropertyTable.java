
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.poifs.property;

import java.io.IOException;
import java.io.OutputStream;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.BATManaged;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.PropertyBlock;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.RawDataBlockList;

/**
 * This class embodies the Property Table for the filesystem; this is
 * basically the dsirectory for all of the documents in the
 * filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class PropertyTable
    implements BATManaged, BlockWritable
{
    private int             _start_block;
    private List            _properties;
    private BlockWritable[] _blocks;

    /**
     * Default constructor
     */

    public PropertyTable()
    {
        _start_block = POIFSConstants.END_OF_CHAIN;
        _properties  = new ArrayList();
        addProperty(new RootProperty());
        _blocks = null;
    }

    /**
     * reading constructor (used when we've read in a file and we want
     * to extract the property table from it). Populates the
     * properties thoroughly
     *
     * @param startBlock the first block of the property table
     * @param blockList the list of blocks
     *
     * @exception IOException if anything goes wrong (which should be
     *            a result of the input being NFG)
     */

    public PropertyTable(final int startBlock,
                         final RawDataBlockList blockList)
        throws IOException
    {
        _start_block = POIFSConstants.END_OF_CHAIN;
        _blocks      = null;
        _properties  =
            PropertyFactory
                .convertToProperties(blockList.fetchBlocks(startBlock));
        populatePropertyTree(( DirectoryProperty ) _properties.get(0));
    }

    /**
     * Add a property to the list of properties we manage
     *
     * @param property the new Property to manage
     */

    public void addProperty(final Property property)
    {
        _properties.add(property);
    }

    /**
     * Remove a property from the list of properties we manage
     *
     * @param property the Property to be removed
     */

    public void removeProperty(final Property property)
    {
        _properties.remove(property);
    }

    /**
     * Get the root property
     *
     * @return the root property
     */

    public RootProperty getRoot()
    {

        // it's always the first element in the List
        return ( RootProperty ) _properties.get(0);
    }

    /**
     * Prepare to be written
     */

    public void preWrite()
    {
        Property[] properties =
            ( Property [] ) _properties.toArray(new Property[ 0 ]);

        // give each property its index
        for (int k = 0; k < properties.length; k++)
        {
            properties[ k ].setIndex(k);
        }

        // allocate the blocks for the property table
        _blocks = PropertyBlock.createPropertyBlockArray(_properties);

        // prepare each property for writing
        for (int k = 0; k < properties.length; k++)
        {
            properties[ k ].preWrite();
        }
    }

    /**
     * Get the start block for the property table
     *
     * @return start block index
     */

    public int getStartBlock()
    {
        return _start_block;
    }

    private void populatePropertyTree(DirectoryProperty root)
        throws IOException
    {
        int index = root.getChildIndex();

        if (!Property.isValidIndex(index))
        {

            // property has no children
            return;
        }
        Stack children = new Stack();

        children.push(_properties.get(index));
        while (!children.empty())
        {
            Property property = ( Property ) children.pop();

            root.addChild(property);
            if (property.isDirectory())
            {
                populatePropertyTree(( DirectoryProperty ) property);
            }
            index = property.getPreviousChildIndex();
            if (Property.isValidIndex(index))
            {
                children.push(_properties.get(index));
            }
            index = property.getNextChildIndex();
            if (Property.isValidIndex(index))
            {
                children.push(_properties.get(index));
            }
        }
    }

    /* ********** START implementation of BATManaged ********** */

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */

    public int countBlocks()
    {
        return (_blocks == null) ? 0
                                 : _blocks.length;
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of BigBlock instances making
     *              up the the filesystem
     */

    public void setStartBlock(final int index)
    {
        _start_block = index;
    }

    /* **********  END  implementation of BATManaged ********** */
    /* ********** START implementation of BlockWritable ********** */

    /**
     * Write the storage to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    public void writeBlocks(final OutputStream stream)
        throws IOException
    {
        if (_blocks != null)
        {
            for (int j = 0; j < _blocks.length; j++)
            {
                _blocks[ j ].writeBlocks(stream);
            }
        }
    }

    /* **********  END  implementation of BlockWritable ********** */
}   // end public class PropertyTable

