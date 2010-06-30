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

package org.apache.poi.hwpf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDocument;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * This class holds much of the core of a Word document, but
 *  without some of the table structure information.
 * You generally want to work with one of
 *  {@link HWPFDocument} or {@link HWPFOldDocument} 
 */
public abstract class HWPFDocumentCore extends POIDocument
{
  /** The FIB */
  protected FileInformationBlock _fib;

  /** main document stream buffer*/
  protected byte[] _mainStream;

  protected HWPFDocumentCore()
  {
     super(null, null);
  }

  /**
   * Takens an InputStream, verifies that it's not RTF, builds a
   *  POIFSFileSystem from it, and returns that.
   */
  public static POIFSFileSystem verifyAndBuildPOIFS(InputStream istream) throws IOException {
	// Open a PushbackInputStream, so we can peek at the first few bytes
	PushbackInputStream pis = new PushbackInputStream(istream,6);
	byte[] first6 = new byte[6];
	pis.read(first6);

	// Does it start with {\rtf ? If so, it's really RTF
	if(first6[0] == '{' && first6[1] == '\\' && first6[2] == 'r'
		&& first6[3] == 't' && first6[4] == 'f') {
		throw new IllegalArgumentException("The document is really a RTF file");
	}

	// OK, so it's not RTF
	// Open a POIFSFileSystem on the (pushed back) stream
	pis.unread(first6);
	return new POIFSFileSystem(pis);
  }

  /**
   * This constructor loads a Word document from an InputStream.
   *
   * @param istream The InputStream that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in InputStream.
   */
  public HWPFDocumentCore(InputStream istream) throws IOException
  {
    //do Ole stuff
    this( verifyAndBuildPOIFS(istream) );
  }

  /**
   * This constructor loads a Word document from a POIFSFileSystem
   *
   * @param pfilesystem The POIFSFileSystem that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocumentCore(POIFSFileSystem pfilesystem) throws IOException
  {
	this(pfilesystem.getRoot(), pfilesystem);
  }

  /**
   * This constructor loads a Word document from a specific point
   *  in a POIFSFileSystem, probably not the default.
   * Used typically to open embeded documents.
   *
   * @param pfilesystem The POIFSFileSystem that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocumentCore(DirectoryNode directory, POIFSFileSystem pfilesystem) throws IOException
  {
    // Sort out the hpsf properties
	super(directory, pfilesystem);

    // read in the main stream.
    DocumentEntry documentProps = (DocumentEntry)
       directory.getEntry("WordDocument");
    _mainStream = new byte[documentProps.getSize()];

    directory.createDocumentInputStream("WordDocument").read(_mainStream);

    // Create our FIB, and check for the doc being encrypted
    _fib = new FileInformationBlock(_mainStream);
    if(_fib.isFEncrypted()) {
    	throw new EncryptedDocumentException("Cannot process encrypted word files!");
    }
  }

  public FileInformationBlock getFileInformationBlock()
  {
    return _fib;
  }
}
