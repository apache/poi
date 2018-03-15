
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
        

package org.apache.poi.poifs.storage;

import java.io.IOException;

/**
 * Interface for lists of blocks that are mapped by block allocation
 * tables
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public interface BlockList
{

    /**
     * remove the specified block from the list
     *
     * @param index the index of the specified block; if the index is
     *              out of range, that's ok
     */

    void zap(final int index);

    /**
     * remove and return the specified block from the list
     *
     * @param index the index of the specified block
     *
     * @return the specified block
     *
     * @exception IOException if the index is out of range or has
     *            already been removed
     */

     ListManagedBlock remove(final int index) throws IOException;

    /**
     * get the blocks making up a particular stream in the list. The
     * blocks are removed from the list.
     *
     * @param startBlock the index of the first block in the stream
     * @param headerPropertiesStartBlock the index of the first header block in the stream
     *
     * @return the stream as an array of correctly ordered blocks
     *
     * @exception IOException if blocks are missing
     */

     ListManagedBlock [] fetchBlocks(final int startBlock, final int headerPropertiesStartBlock)
        throws IOException;

    /**
     * set the associated BlockAllocationTable
     *
     * @param bat the associated BlockAllocationTable
     *
     * @exception IOException
     */

    void setBAT(final BlockAllocationTableReader bat) throws IOException;
    
    int blockCount();
}   // end public interface BlockList

