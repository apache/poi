
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

import org.apache.poi.poifs.filesystem.BATManaged;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.property.RootProperty;

import java.util.*;

import java.io.*;

/**
 * This class implements storage for writing the small blocks used by
 * small documents.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class SmallBlockTableWriter
    implements BlockWritable, BATManaged
{
    private BlockAllocationTableWriter _sbat;
    private List                       _small_blocks;
    private int                        _big_block_count;
    private RootProperty               _root;

    /**
     * Creates new SmallBlockTable
     *
     * @param documents a List of POIFSDocument instances
     * @param root the Filesystem's root property
     */

    public SmallBlockTableWriter(final List documents,
                                 final RootProperty root)
    {
        _sbat         = new BlockAllocationTableWriter();
        _small_blocks = new ArrayList();
        _root         = root;
        Iterator iter = documents.iterator();

        while (iter.hasNext())
        {
            POIFSDocument   doc    = ( POIFSDocument ) iter.next();
            BlockWritable[] blocks = doc.getSmallBlocks();

            if (blocks.length != 0)
            {
                doc.setStartBlock(_sbat.allocateSpace(blocks.length));
                for (int j = 0; j < blocks.length; j++)
                {
                    _small_blocks.add(blocks[ j ]);
                }
            }
        }
        _sbat.simpleCreateBlocks();
        _root.setSize(_small_blocks.size());
        _big_block_count = SmallDocumentBlock.fill(_small_blocks);
    }

    /**
     * Get the SBAT
     *
     * @return the Small Block Allocation Table
     */

    public BlockAllocationTableWriter getSBAT()
    {
        return _sbat;
    }

    /* ********** START implementation of BATManaged ********** */

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */

    public int countBlocks()
    {
        return _big_block_count;
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of BigBlock instances making
     *              up the the filesystem
     *
     * @param start_block
     */

    public void setStartBlock(int start_block)
    {
        _root.setStartBlock(start_block);
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
        Iterator iter = _small_blocks.iterator();

        while (iter.hasNext())
        {
            (( BlockWritable ) iter.next()).writeBlocks(stream);
        }
    }

    /* **********  END  implementation of BlockWritable ********** */
}
