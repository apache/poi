
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

package org.apache.poi.poifs.storage;

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * A block of Property instances
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class PropertyBlock
    extends BigBlock
{
    private static final int _properties_per_block =
        POIFSConstants.BIG_BLOCK_SIZE / POIFSConstants.PROPERTY_SIZE;
    private Property[]       _properties;

    /**
     * Create a single instance initialized with default values
     *
     * @param properties the properties to be inserted
     * @param offset the offset into the properties array
     */

    private PropertyBlock(final Property [] properties, final int offset)
    {
        _properties = new Property[ _properties_per_block ];
        for (int j = 0; j < _properties_per_block; j++)
        {
            _properties[ j ] = properties[ j + offset ];
        }
    }

    /**
     * Create an array of PropertyBlocks from an array of Property
     * instances, creating empty Property instances to make up any
     * shortfall
     *
     * @param properties the Property instances to be converted into
     *                   PropertyBlocks, in a java List
     *
     * @return the array of newly created PropertyBlock instances
     */

    public static BlockWritable [] createPropertyBlockArray(
            final List properties)
    {
        int        block_count   =
            (properties.size() + _properties_per_block - 1)
            / _properties_per_block;
        Property[] to_be_written =
            new Property[ block_count * _properties_per_block ];

        System.arraycopy(properties.toArray(new Property[ 0 ]), 0,
                         to_be_written, 0, properties.size());
        for (int j = properties.size(); j < to_be_written.length; j++)
        {

            // create an instance of an anonymous inner class that
            // extends Property
            to_be_written[ j ] = new Property()
            {
                protected void preWrite()
                {
                }

                public boolean isDirectory()
                {
                    return false;
                }
            };
        }
        BlockWritable[] rvalue = new BlockWritable[ block_count ];

        for (int j = 0; j < block_count; j++)
        {
            rvalue[ j ] = new PropertyBlock(to_be_written,
                                            j * _properties_per_block);
        }
        return rvalue;
    }

    /* ********** START extension of BigBlock ********** */

    /**
     * Write the block's data to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    void writeData(final OutputStream stream)
        throws IOException
    {
        for (int j = 0; j < _properties_per_block; j++)
        {
            _properties[ j ].writeData(stream);
        }
    }

    /* **********  END  extension of BigBlock ********** */
}   // end public class PropertyBlock

