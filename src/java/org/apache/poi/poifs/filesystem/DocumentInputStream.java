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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.SuppressForbidden;

/**
 * This class provides methods to read a DocumentEntry managed by a
 *  {@link POIFSFileSystem} or {@link NPOIFSFileSystem} instance.
 * It creates the appropriate one, and delegates, allowing us to
 *  work transparently with the two.
 */
public class DocumentInputStream extends InputStream implements LittleEndianInput {
	/** returned by read operations if we're at end of document */
	protected static final int EOF = -1;

	protected static final int SIZE_SHORT = 2;
	protected static final int SIZE_INT = 4;
	protected static final int SIZE_LONG = 8;
	
	private DocumentInputStream delegate;
	
	/** For use by downstream implementations */
	protected DocumentInputStream() {}

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
	   DocumentNode documentNode = (DocumentNode)document;
	   DirectoryNode parentNode = (DirectoryNode)document.getParent();

	   if(documentNode.getDocument() != null) {
	      delegate = new ODocumentInputStream(document);
	   } else if(parentNode.getOFileSystem() != null) {
	      delegate = new ODocumentInputStream(document);
	   } else if(parentNode.getNFileSystem() != null) {
	      delegate = new NDocumentInputStream(document);
	   } else {
	      throw new IOException("No FileSystem bound on the parent, can't read contents");
	   }
	}

	/**
	 * Create an InputStream from the specified Document
	 * 
	 * @param document the Document to be read
	 */
	public DocumentInputStream(OPOIFSDocument document) {
	   delegate = new ODocumentInputStream(document);
	}

   /**
    * Create an InputStream from the specified Document
    * 
    * @param document the Document to be read
    */
   public DocumentInputStream(NPOIFSDocument document) {
      delegate = new NDocumentInputStream(document);
   }

    @Override
    @SuppressForbidden("just delegating")
	public int available() {
	   return delegate.available();
	}

    @Override
	public void close() {
	   delegate.close();
	}

    @Override
	public void mark(int ignoredReadlimit) {
		delegate.mark(ignoredReadlimit);
	}

	/**
	 * Tests if this input stream supports the mark and reset methods.
	 * 
	 * @return <code>true</code> always
	 */
    @Override
	public boolean markSupported() {
		return true;
	}

    @Override
	public int read() throws IOException {
	   return delegate.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

    @Override
	public int read(byte[] b, int off, int len) throws IOException {
	   return delegate.read(b, off, len);
	}

	/**
	 * Repositions this stream to the position at the time the mark() method was
	 * last called on this input stream. If mark() has not been called this
	 * method repositions the stream to its beginning.
	 */
    @Override
	public void reset() {
	   delegate.reset();
	}

    @Override
	public long skip(long n) throws IOException {
	   return delegate.skip(n);
	}

    @Override
	public byte readByte() {
	   return delegate.readByte();
	}

    @Override
	public double readDouble() {
	   return delegate.readDouble();
	}

    @Override
	public short readShort() {
		return (short) readUShort();
	}

    @Override
    public void readFully(byte[] buf) {
        readFully(buf, 0, buf.length);
    }

    @Override
	public void readFully(byte[] buf, int off, int len) {
	   delegate.readFully(buf, off, len);
	}

    @Override
	public long readLong() {
	   return delegate.readLong();
	}

    @Override
	public int readInt() {
	   return delegate.readInt();
	}

    @Override
	public int readUShort() {
	   return delegate.readUShort();
	}

    @Override
	public int readUByte() {
	   return delegate.readUByte();
	}
	
    public long readUInt() {
        int i = readInt();
        return i & 0xFFFFFFFFL;
    }

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }
}
