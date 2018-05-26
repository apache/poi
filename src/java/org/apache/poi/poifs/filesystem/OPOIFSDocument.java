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

package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.DataInputBlock;
import org.apache.poi.poifs.storage.DocumentBlock;
import org.apache.poi.poifs.storage.ListManagedBlock;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.SmallDocumentBlock;
import org.apache.poi.util.HexDump;

/**
 * This class manages a document in a old-style 
 *  OPOIFS filesystem.
 */
public final class OPOIFSDocument implements BATManaged, BlockWritable, POIFSViewable  {
	private static final DocumentBlock[] EMPTY_BIG_BLOCK_ARRAY = { };
	private static final SmallDocumentBlock[] EMPTY_SMALL_BLOCK_ARRAY = { };
	private DocumentProperty _property;
	private int _size;
	
   private final POIFSBigBlockSize _bigBigBlockSize;

	// one of these stores will be valid
	private SmallBlockStore  _small_store;
	private BigBlockStore	 _big_store;
	
		/**
	 * Constructor from large blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the big blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
	public OPOIFSDocument(String name, RawDataBlock[] blocks, int length) throws IOException {
		_size = length;
		if(blocks.length == 0) {
		   _bigBigBlockSize = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
		} else {
		   _bigBigBlockSize = (blocks[0].getBigBlockSize() == POIFSConstants.SMALLER_BIG_BLOCK_SIZE ?
		         POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS : 
		         POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS
		   );
		}
		
		_big_store = new BigBlockStore(_bigBigBlockSize, convertRawBlocksToBigBlocks(blocks));
		_property = new DocumentProperty(name, _size);
		_small_store = new SmallBlockStore(_bigBigBlockSize, EMPTY_SMALL_BLOCK_ARRAY);
		_property.setDocument(this);
	}

	// TODO - awkward typing going on here
	private static DocumentBlock[] convertRawBlocksToBigBlocks(ListManagedBlock[] blocks) throws IOException {
		DocumentBlock[] result = new DocumentBlock[blocks.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new DocumentBlock((RawDataBlock)blocks[i]);
		}
		return result;
	}
	private static SmallDocumentBlock[] convertRawBlocksToSmallBlocks(ListManagedBlock[] blocks) {
		if (blocks instanceof SmallDocumentBlock[]) {
			return (SmallDocumentBlock[]) blocks;
		}
		SmallDocumentBlock[] result = new SmallDocumentBlock[blocks.length];
		System.arraycopy(blocks, 0, result, 0, blocks.length);
		return result;
	}

	/**
	 * Constructor from small blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the small blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
	public OPOIFSDocument(String name, SmallDocumentBlock[] blocks, int length) {
		_size = length;
		
		if(blocks.length == 0) {
		   _bigBigBlockSize = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
		} else {
		   _bigBigBlockSize = blocks[0].getBigBlockSize();
		}

		_big_store = new BigBlockStore(_bigBigBlockSize, EMPTY_BIG_BLOCK_ARRAY);
		_property = new DocumentProperty(name, _size);
		_small_store = new SmallBlockStore(_bigBigBlockSize, blocks);
		_property.setDocument(this);
	}

	/**
	 * Constructor from small blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the small blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
	public OPOIFSDocument(String name, POIFSBigBlockSize bigBlockSize, ListManagedBlock[] blocks, int length) throws IOException {
		_size = length;
		_bigBigBlockSize = bigBlockSize;
		_property = new DocumentProperty(name, _size);
		_property.setDocument(this);
		if (Property.isSmall(_size)) {
			_big_store = new BigBlockStore(bigBlockSize,EMPTY_BIG_BLOCK_ARRAY);
			_small_store = new SmallBlockStore(bigBlockSize,convertRawBlocksToSmallBlocks(blocks));
		} else {
			_big_store = new BigBlockStore(bigBlockSize,convertRawBlocksToBigBlocks(blocks));
			_small_store = new SmallBlockStore(bigBlockSize,EMPTY_SMALL_BLOCK_ARRAY);
		}
	}
	public OPOIFSDocument(String name, ListManagedBlock[] blocks, int length) throws IOException {
	   this(name, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, blocks, length);
	}

	/**
	 * Constructor
	 *
	 * @param name the name of the POIFSDocument
	 * @param stream the InputStream we read data from
	 */
	public OPOIFSDocument(String name, POIFSBigBlockSize bigBlockSize, InputStream stream) throws IOException {
		List<DocumentBlock> blocks = new ArrayList<>();

		_size = 0;
		_bigBigBlockSize = bigBlockSize;
		while (true) {
			DocumentBlock block = new DocumentBlock(stream, bigBlockSize);
			int blockSize = block.size();

			if (blockSize > 0) {
				blocks.add(block);
				_size += blockSize;
			}
			if (block.partiallyRead()) {
				break;
			}
		}
		DocumentBlock[] bigBlocks = blocks.toArray(new DocumentBlock[blocks.size()]);

		_big_store = new BigBlockStore(bigBlockSize,bigBlocks);
		_property = new DocumentProperty(name, _size);
		_property.setDocument(this);
		if (_property.shouldUseSmallBlocks()) {
			_small_store = new SmallBlockStore(bigBlockSize,SmallDocumentBlock.convert(bigBlockSize,bigBlocks, _size));
			_big_store = new BigBlockStore(bigBlockSize,new DocumentBlock[0]);
		} else {
			_small_store = new SmallBlockStore(bigBlockSize,EMPTY_SMALL_BLOCK_ARRAY);
		}
	}
	public OPOIFSDocument(String name, InputStream stream) throws IOException {
	   this(name, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, stream);
	}

	/**
	 * Constructor
	 *
	 * @param name the name of the POIFSDocument
	 * @param size the length of the POIFSDocument
	 * @param path the path of the POIFSDocument
	 * @param writer the writer who will eventually write the document contents
	 */
	public OPOIFSDocument(String name, int size, POIFSBigBlockSize bigBlockSize, POIFSDocumentPath path, POIFSWriterListener writer) {
		_size = size;
		_bigBigBlockSize = bigBlockSize;
		_property = new DocumentProperty(name, _size);
		_property.setDocument(this);
		if (_property.shouldUseSmallBlocks()) {
			_small_store = new SmallBlockStore(_bigBigBlockSize, path, name, size, writer);
			_big_store = new BigBlockStore(_bigBigBlockSize, EMPTY_BIG_BLOCK_ARRAY);
		} else {
			_small_store = new SmallBlockStore(_bigBigBlockSize, EMPTY_SMALL_BLOCK_ARRAY);
			_big_store = new BigBlockStore(_bigBigBlockSize, path, name, size, writer);
		}
	}
	public OPOIFSDocument(String name, int size, POIFSDocumentPath path, POIFSWriterListener writer) {
	   this(name, size, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, path, writer);
	}

	/**
	 * @return array of SmallDocumentBlocks; may be empty, cannot be null
	 */
	public SmallDocumentBlock[] getSmallBlocks() {
		return _small_store.getBlocks();
	}

	/**
	 * @return size of the document
	 */
	public int getSize() {
		return _size;
	}

	/**
	 * read data from the internal stores
	 *
	 * @param buffer the buffer to write to
	 * @param offset the offset into our storage to read from
	 * This method is currently (Oct 2008) only used by test code. Perhaps it can be deleted
	 */
	void read(byte[] buffer, int offset) {
		ODocumentInputStream.readFullyInternal(buffer, 0, buffer.length, offset, _size, this::getDataInputBlock);
	}

	/**
	 * @return <code>null</code> if <tt>offset</tt> points to the end of the document stream
	 */
	DataInputBlock getDataInputBlock(int offset) {
		if (offset >= _size) {
			if (offset > _size) {
				throw new RuntimeException("Request for Offset " + offset + " doc size is " + _size);
			}
			return null;
		}
		if (_property.shouldUseSmallBlocks()) {
			return SmallDocumentBlock.getDataInputBlock(_small_store.getBlocks(), offset);
		}
		return DocumentBlock.getDataInputBlock(_big_store.getBlocks(), offset);
	}

	/**
	 * @return the instance's DocumentProperty
	 */

	DocumentProperty getDocumentProperty() {
		return _property;
	}

	/* ********** START implementation of BlockWritable ********** */

	/**
	 * Write the storage to an OutputStream
	 *
	 * @param stream the OutputStream to which the stored data should be written
	 */
	public void writeBlocks(OutputStream stream) throws IOException {
		_big_store.writeBlocks(stream);
	}

	/* **********  END  implementation of BlockWritable ********** */
	/* ********** START implementation of BATManaged ********** */

	/**
	 * Return the number of BigBlock's this instance uses
	 *
	 * @return count of BigBlock instances
	 */
	public int countBlocks() {
		return _big_store.countBlocks();
	}

	/**
	 * Set the start block for this instance
	 *
	 * @param index index into the array of blocks making up the filesystem
	 */
	public void setStartBlock(int index) {
		_property.setStartBlock(index);
	}

	/* **********  END  implementation of BATManaged ********** */
	/* ********** START begin implementation of POIFSViewable ********** */

	/**
	 * Get an array of objects, some of which may implement POIFSViewable
	 *
	 * @return an array of Object; may not be null, but may be empty
	 */
	public Object[] getViewableArray() {
		String result = "<NO DATA>";

		try {
			BlockWritable[] blocks = null;

			if (_big_store.isValid()) {
				blocks = _big_store.getBlocks();
			} else if (_small_store.isValid()) {
				blocks = _small_store.getBlocks();
			}
			if (blocks != null) {
	            ByteArrayOutputStream output = new ByteArrayOutputStream();
				for (BlockWritable bw : blocks) {
					bw.writeBlocks(output);
				}
				int length = Math.min(output.size(), _property.getSize());
				result = HexDump.dump(output.toByteArray(), 0, 0, length);
			}
		} catch (IOException e) {
			result = e.getMessage();
		}
		return new String[]{ result };
	}

	/**
	 * Get an Iterator of objects, some of which may implement POIFSViewable
	 *
	 * @return an Iterator; may not be null, but may have an empty back end
	 *		 store
	 */
	public Iterator<Object> getViewableIterator() {
		return Collections.emptyList().iterator();
	}

	/**
	 * Give viewers a hint as to whether to call getViewableArray or
	 * getViewableIterator
	 *
	 * @return <code>true</code> if a viewer should call getViewableArray,
	 *		 <code>false</code> if a viewer should call getViewableIterator
	 */
	public boolean preferArray() {
		return true;
	}

	/**
	 * Provides a short description of the object, to be used when a
	 * POIFSViewable object has not provided its contents.
	 *
	 * @return short description
	 */
	public String getShortDescription() {
		return "Document: \"" + _property.getName() + "\"" +
				" size = " + getSize();
	}

	/* **********  END  begin implementation of POIFSViewable ********** */
	private static final class SmallBlockStore {
		private SmallDocumentBlock[] _smallBlocks;
		private final POIFSDocumentPath _path;
		private final String _name;
		private final int _size;
		private final POIFSWriterListener _writer;
		private final POIFSBigBlockSize _bigBlockSize;

		/**
		 * Constructor
		 *
		 * @param blocks blocks to construct the store from
		 */
		SmallBlockStore(POIFSBigBlockSize bigBlockSize, SmallDocumentBlock[] blocks) {
		   _bigBlockSize = bigBlockSize;
			_smallBlocks = blocks.clone();
			this._path = null;
			this._name = null;
			this._size = -1;
			this._writer = null;
		}

		/**
		 * Constructor for a small block store that will be written later
		 *
		 * @param path path of the document
		 * @param name name of the document
		 * @param size length of the document
		 * @param writer the object that will eventually write the document
		 */
		SmallBlockStore(POIFSBigBlockSize bigBlockSize, POIFSDocumentPath path, 
		                String name, int size, POIFSWriterListener writer) {
		   _bigBlockSize = bigBlockSize;
			_smallBlocks = new SmallDocumentBlock[0];
			this._path = path;
			this._name = name;
			this._size = size;
			this._writer = writer;
		}

		/**
		 * @return <code>true</code> if this store is a valid source of data
		 */
		boolean isValid() {
			return _smallBlocks.length > 0 || _writer != null;
		}

		/**
		 * @return the SmallDocumentBlocks
		 */
		SmallDocumentBlock[] getBlocks() {
			if (isValid() && _writer != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream(_size);
				DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);

				_writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
				_smallBlocks = SmallDocumentBlock.convert(_bigBlockSize, stream.toByteArray(), _size);
			}
			return _smallBlocks;
		}
	} // end private class SmallBlockStore

	private static final class BigBlockStore {
		private DocumentBlock[] bigBlocks;
		private final POIFSDocumentPath _path;
		private final String _name;
		private final int _size;
		private final POIFSWriterListener _writer;
      private final POIFSBigBlockSize _bigBlockSize;

		/**
		 * Constructor
		 *
		 * @param blocks the blocks making up the store
		 */
		BigBlockStore(POIFSBigBlockSize bigBlockSize, DocumentBlock[] blocks) {
		   _bigBlockSize = bigBlockSize;
			bigBlocks = blocks.clone();
			_path = null;
			_name = null;
			_size = -1;
			_writer = null;
		}

		/**
		 * Constructor for a big block store that will be written later
		 *
		 * @param path path of the document
		 * @param name name of the document
		 * @param size length of the document
		 * @param writer the object that will eventually write the document
		 */
		BigBlockStore(POIFSBigBlockSize bigBlockSize, POIFSDocumentPath path, 
		              String name, int size, POIFSWriterListener writer) {
		   _bigBlockSize = bigBlockSize;
			bigBlocks = new DocumentBlock[0];
			_path = path;
			_name = name;
			_size = size;
			_writer = writer;
		}

		/**
		 * @return <code>true</code> if this store is a valid source of data
		 */
		boolean isValid() {
			return bigBlocks.length > 0 || _writer != null;
		}

		/**
		 * @return the DocumentBlocks
		 */
		DocumentBlock[] getBlocks() {
			if (isValid() && _writer != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream(_size);
				DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);

				_writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
				bigBlocks = DocumentBlock.convert(_bigBlockSize, stream.toByteArray(), _size);
			}
			return bigBlocks;
		}

		/**
		 * write the blocks to a stream
		 *
		 * @param stream the stream to which the data is to be written
		 */
		void writeBlocks(OutputStream stream) throws IOException {
			if (isValid()) {
				if (_writer != null) {
					DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);

					_writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
					dstream.writeFiller(countBlocks() * _bigBlockSize.getBigBlockSize(),
							DocumentBlock.getFillByte());
				} else {
					for (DocumentBlock bigBlock : bigBlocks) {
						bigBlock.writeBlocks(stream);
					}
				}
			}
		}

		/**
		 * @return number of big blocks making up this document
		 */
		int countBlocks() {

			if (isValid()) {
				if (_writer == null) {
					return bigBlocks.length;
				}
				return (_size + _bigBlockSize.getBigBlockSize() - 1)
							/ _bigBlockSize.getBigBlockSize();
			}
			return 0;
		}
	} // end private class BigBlockStore
}
