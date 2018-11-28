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

package org.apache.poi.poifs.property;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.BATManaged;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSStream;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class embodies the Property Table for a {@link POIFSFileSystem};
 * this is basically the directory for all of the documents in the
 * filesystem and looks up entries in the filesystem to their
 * chain of blocks.
 */
public final class PropertyTable implements BATManaged {
    private static final POILogger _logger =
       POILogFactory.getLogger(PropertyTable.class);

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private final HeaderBlock    _header_block;
    private final List<Property> _properties = new ArrayList<>();
    private final POIFSBigBlockSize _bigBigBlockSize;

    public PropertyTable(HeaderBlock headerBlock)
    {
        _header_block = headerBlock;
        _bigBigBlockSize = headerBlock.getBigBlockSize();
        addProperty(new RootProperty());
    }

    /**
     * reading constructor (used when we've read in a file and we want
     * to extract the property table from it). Populates the
     * properties thoroughly
     *
     * @param headerBlock the header block of the file
     * @param filesystem the filesystem to read from
     *
     * @exception IOException if anything goes wrong (which should be
     *            a result of the input being NFG)
     */
    public PropertyTable(final HeaderBlock headerBlock, final POIFSFileSystem filesystem)
    throws IOException {
        this(
              headerBlock,
              new POIFSStream(filesystem, headerBlock.getPropertyStart())
        );
    }

    /* only invoked locally and from the junit tests */
    PropertyTable(final HeaderBlock headerBlock, final Iterable<ByteBuffer> dataSource)
    throws IOException {
        _header_block = headerBlock;
        _bigBigBlockSize = headerBlock.getBigBlockSize();

        for (ByteBuffer bb : dataSource) {
            // Turn it into an array
            byte[] data;
            if (bb.hasArray() && bb.arrayOffset() == 0 &&
                    bb.array().length == _bigBigBlockSize.getBigBlockSize()) {
                data = bb.array();
            } else {
                data = IOUtils.safelyAllocate(_bigBigBlockSize.getBigBlockSize(), MAX_RECORD_LENGTH);

                int toRead = data.length;
                if (bb.remaining() < _bigBigBlockSize.getBigBlockSize()) {
                    // Looks to be a truncated block
                    // This isn't allowed, but some third party created files
                    //  sometimes do this, and we can normally read anyway
                    _logger.log(POILogger.WARN, "Short Property Block, ", bb.remaining(),
                            " bytes instead of the expected " + _bigBigBlockSize.getBigBlockSize());
                    toRead = bb.remaining();
                }

                bb.get(data, 0, toRead);
            }

            PropertyFactory.convertToProperties(data, _properties);
        }

        populatePropertyTree( (DirectoryProperty)_properties.get(0));
    }


    /**
     * Add a property to the list of properties we manage
     *
     * @param property the new Property to manage
     */
    public void addProperty(Property property) {
        _properties.add(property);
    }

    /**
     * Remove a property from the list of properties we manage
     *
     * @param property the Property to be removed
     */
    public void removeProperty(final Property property) {
        _properties.remove(property);
    }

    /**
     * Get the root property
     *
     * @return the root property
     */
    public RootProperty getRoot() {
        // it's always the first element in the List
        return ( RootProperty ) _properties.get(0);
    }

    /**
     * Get the start block for the property table
     *
     * @return start block index
     */
    public int getStartBlock() {
        return _header_block.getPropertyStart();
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of BigBlock instances making
     *              up the the filesystem
     */
    public void setStartBlock(final int index) {
        _header_block.setPropertyStart(index);
    }



    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */
    public int countBlocks() {
       long rawSize = _properties.size() * (long)POIFSConstants.PROPERTY_SIZE;
       int blkSize = _bigBigBlockSize.getBigBlockSize();
       int numBlocks = (int)(rawSize / blkSize);
       if ((rawSize % blkSize) != 0) {
           numBlocks++;
       }
       return numBlocks;
    }
 
    /**
     * Prepare to be written
     */
    public void preWrite() {
        List<Property> pList = new ArrayList<>();
        // give each property its index
        int i=0;
        for (Property p : _properties) {
            // only handle non-null properties 
            if (p == null) continue;
            p.setIndex(i++);
            pList.add(p);
        }

        // prepare each property for writing
        for (Property p : pList) p.preWrite();
    }    
    
    /**
     * Writes the properties out into the given low-level stream
     */
    public void write(POIFSStream stream) throws IOException {
       OutputStream os = stream.getOutputStream();
       for(Property property : _properties) {
          if(property != null) {
             property.writeData(os);
          }
       }
       os.close();
       
       // Update the start position if needed
       if(getStartBlock() != stream.getStartBlock()) {
          setStartBlock(stream.getStartBlock());
       }
    }

    private void populatePropertyTree(DirectoryProperty root) throws IOException {
        int index = root.getChildIndex();

        if (!Property.isValidIndex(index)) {
            // property has no children
            return;
        }

        final Stack<Property> children = new Stack<>();
        children.push(_properties.get(index));
        while (!children.empty()) {
            Property property = children.pop();
            if (property == null) {
                // unknown / unsupported / corrupted property, skip
                continue;
            }

            root.addChild(property);
            if (property.isDirectory()) {
                populatePropertyTree(( DirectoryProperty ) property);
            }
            index = property.getPreviousChildIndex();
            if (isValidIndex(index)) {
                children.push(_properties.get(index));
            }
            index = property.getNextChildIndex();
            if (isValidIndex(index)) {
                children.push(_properties.get(index));
            }
        }
    }

    private boolean isValidIndex(int index) {
        if (! Property.isValidIndex(index))
            return false;
        if (index < 0 || index >= _properties.size()) {
            _logger.log(POILogger.WARN, "Property index " + index +
                    "outside the valid range 0.."+_properties.size());
            return false;
        }
        return true;
    }
}
