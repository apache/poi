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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class provides methods to write a DocumentEntry managed by a
 * {@link NPOIFSFileSystem} instance.
 */
public final class NDocumentOutputStream extends OutputStream {
	/** the Document's size */
	private int _document_size;

	/** have we been closed? */
	private boolean _closed;

	/** the actual Document */
	private NPOIFSDocument _document;
	
	/**
	 * Create an OutputStream from the specified DocumentEntry.
	 * The specified entry will be emptied.
	 * 
	 * @param document the DocumentEntry to be written
	 */
	public NDocumentOutputStream(DocumentEntry document) throws IOException {
		if (!(document instanceof DocumentNode)) {
			throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
		}
		_document_size = 0;
		_closed = false;
		
		_document = new NPOIFSDocument((DocumentNode)document);
		_document.free();
	}
	
	/**
	 * Create an OutputStream to create the specified new Entry
	 * 
	 * @param parent Where to create the Entry
	 * @param name Name of the new entry
	 */
	public NDocumentOutputStream(DirectoryEntry parent, String name) throws IOException {
        if (!(parent instanceof DirectoryNode)) {
            throw new IOException("Cannot open internal directory storage, " + parent + " not a Directory Node");
        }
        _document_size = 0;
        _closed = false;

        // Have an empty one created for now
        DocumentEntry doc = parent.createDocument(name, new ByteArrayInputStream(new byte[0]));
        _document = new NPOIFSDocument((DocumentNode)doc);
	}

    public void write(int b) throws IOException {
        // TODO
    }

    public void write(byte[] b) throws IOException {
        // TODO
    }

    public void write(byte[] b, int off, int len) throws IOException {
        // TODO
    }

    public void close() throws IOException {
        // TODO
    }
}
