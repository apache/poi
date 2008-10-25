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

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.storage.DataInputBlock;
import org.apache.poi.util.LittleEndianInput;

/**
 * This class provides methods to read a DocumentEntry managed by a
 * {@link POIFSFileSystem} instance.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class DocumentInputStream extends InputStream implements LittleEndianInput {
	/** returned by read operations if we're at end of document */
	private static final int EOF = -1;

	private static final int SIZE_SHORT = 2;
	private static final int SIZE_INT = 4;
	private static final int SIZE_LONG = 8;

	/** current offset into the Document */
	private int _current_offset;

	/** current marked offset into the Document (used by mark and reset) */
	private int _marked_offset;

	/** the Document's size */
	private int _document_size;

	/** have we been closed? */
	private boolean _closed;

	/** the actual Document */
	private POIFSDocument _document;

	/** the data block containing the current stream pointer */
	private DataInputBlock _currentBlock;

	/**
	 * Create an InputStream from the specified DocumentEntry
	 * 
	 * @param document the DocumentEntry to be read
	 * 
	 * @exception IOException if the DocumentEntry cannot be opened (like, maybe it has
	 *                been deleted?)
	 */
	public DocumentInputStream(DocumentEntry document) throws IOException {
		if (!(document instanceof DocumentNode)) {
			throw new IOException("Cannot open internal document storage");
		}
		_current_offset = 0;
		_marked_offset = 0;
		_document_size = document.getSize();
		_closed = false;
		_document = ((DocumentNode) document).getDocument();
		_currentBlock = getDataInputBlock(0);
	}

	/**
	 * Create an InputStream from the specified Document
	 * 
	 * @param document the Document to be read
	 */
	public DocumentInputStream(POIFSDocument document) {
		_current_offset = 0;
		_marked_offset = 0;
		_document_size = document.getSize();
		_closed = false;
		_document = document;
		_currentBlock = getDataInputBlock(0);
	}

	public int available() {
		if (_closed) {
			throw new IllegalStateException("cannot perform requested operation on a closed stream");
		}
		return _document_size - _current_offset;
	}

	public void close() {
		_closed = true;
	}

	public void mark(int ignoredReadlimit) {
		_marked_offset = _current_offset;
	}

	/**
	 * Tests if this input stream supports the mark and reset methods.
	 * 
	 * @return <code>true</code> always
	 */
	public boolean markSupported() {
		return true;
	}

	private DataInputBlock getDataInputBlock(int offset) {
		return _document.getDataInputBlock(offset);
	}

	public int read() throws IOException {
		dieIfClosed();
		if (atEOD()) {
			return EOF;
		}
		int result = _currentBlock.readUByte();
		_current_offset++;
		if (_currentBlock.available() < 1) {
			_currentBlock = getDataInputBlock(_current_offset);
		}
		return result;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		dieIfClosed();
		if (b == null) {
			throw new IllegalArgumentException("buffer must not be null");
		}
		if (off < 0 || len < 0 || b.length < off + len) {
			throw new IndexOutOfBoundsException("can't read past buffer boundaries");
		}
		if (len == 0) {
			return 0;
		}
		if (atEOD()) {
			return EOF;
		}
		int limit = Math.min(available(), len);
		readFully(b, off, limit);
		return limit;
	}

	/**
	 * Repositions this stream to the position at the time the mark() method was
	 * last called on this input stream. If mark() has not been called this
	 * method repositions the stream to its beginning.
	 */
	public void reset() {
		_current_offset = _marked_offset;
		_currentBlock = getDataInputBlock(_current_offset);
	}

	public long skip(long n) throws IOException {
		dieIfClosed();
		if (n < 0) {
			return 0;
		}
		int new_offset = _current_offset + (int) n;

		if (new_offset < _current_offset) {

			// wrap around in converting a VERY large long to an int
			new_offset = _document_size;
		} else if (new_offset > _document_size) {
			new_offset = _document_size;
		}
		long rval = new_offset - _current_offset;

		_current_offset = new_offset;
		_currentBlock = getDataInputBlock(_current_offset);
		return rval;
	}

	private void dieIfClosed() throws IOException {
		if (_closed) {
			throw new IOException("cannot perform requested operation on a closed stream");
		}
	}

	private boolean atEOD() {
		return _current_offset == _document_size;
	}

	private void checkAvaliable(int requestedSize) {
		if (_closed) {
			throw new IllegalStateException("cannot perform requested operation on a closed stream");
		}
		if (requestedSize > _document_size - _current_offset) {
			throw new RuntimeException("Buffer underrun - requested " + requestedSize
					+ " bytes but " + (_document_size - _current_offset) + " was available");
		}
	}

	public byte readByte() {
		return (byte) readUByte();
	}

	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	public short readShort() {
		return (short) readUShort();
	}

	public void readFully(byte[] buf, int off, int len) {
		checkAvaliable(len);
		int blockAvailable = _currentBlock.available();
		if (blockAvailable > len) {
			_currentBlock.readFully(buf, off, len);
			_current_offset += len;
			return;
		}
		// else read big amount in chunks
		int remaining = len;
		int writePos = off;
		while (remaining > 0) {
			boolean blockIsExpiring = remaining >= blockAvailable;
			int reqSize;
			if (blockIsExpiring) {
				reqSize = blockAvailable;
			} else {
				reqSize = remaining;
			}
			_currentBlock.readFully(buf, writePos, reqSize);
			remaining -= reqSize;
			writePos += reqSize;
			_current_offset += reqSize;
			if (blockIsExpiring) {
				if (_current_offset == _document_size) {
					if (remaining > 0) {
						throw new IllegalStateException(
								"reached end of document stream unexpectedly");
					}
					_currentBlock = null;
					break;
				}
				_currentBlock = getDataInputBlock(_current_offset);
				blockAvailable = _currentBlock.available();
			}
		}
	}

	public long readLong() {
		checkAvaliable(SIZE_LONG);
		int blockAvailable = _currentBlock.available();
		long result;
		if (blockAvailable > SIZE_LONG) {
			result = _currentBlock.readLongLE();
		} else {
			DataInputBlock nextBlock = getDataInputBlock(_current_offset + blockAvailable);
			if (blockAvailable == SIZE_LONG) {
				result = _currentBlock.readLongLE();
			} else {
				result = nextBlock.readLongLE(_currentBlock, blockAvailable);
			}
			_currentBlock = nextBlock;
		}
		_current_offset += SIZE_LONG;
		return result;
	}

	public int readInt() {
		checkAvaliable(SIZE_INT);
		int blockAvailable = _currentBlock.available();
		int result;
		if (blockAvailable > SIZE_INT) {
			result = _currentBlock.readIntLE();
		} else {
			DataInputBlock nextBlock = getDataInputBlock(_current_offset + blockAvailable);
			if (blockAvailable == SIZE_INT) {
				result = _currentBlock.readIntLE();
			} else {
				result = nextBlock.readIntLE(_currentBlock, blockAvailable);
			}
			_currentBlock = nextBlock;
		}
		_current_offset += SIZE_INT;
		return result;
	}

	public int readUShort() {
		checkAvaliable(SIZE_SHORT);
		int blockAvailable = _currentBlock.available();
		int result;
		if (blockAvailable > SIZE_SHORT) {
			result = _currentBlock.readUShortLE();
		} else {
			DataInputBlock nextBlock = getDataInputBlock(_current_offset + blockAvailable);
			if (blockAvailable == SIZE_SHORT) {
				result = _currentBlock.readUShortLE();
			} else {
				result = nextBlock.readUShortLE(_currentBlock);
			}
			_currentBlock = nextBlock;
		}
		_current_offset += SIZE_SHORT;
		return result;
	}

	public int readUByte() {
		checkAvaliable(1);
		int result = _currentBlock.readUByte();
		_current_offset++;
		if (_currentBlock.available() < 1) {
			_currentBlock = getDataInputBlock(_current_offset);
		}
		return result;
	}
}
