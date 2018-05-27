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
package org.apache.poi.extractor;

import java.io.Closeable;
import java.io.IOException;

/**
 * Common Parent for Text Extractors
 *  of POI Documents. 
 * You will typically find the implementation of
 *  a given format's text extractor under
 *  org.apache.poi.[format].extractor .
 *  
 * @see org.apache.poi.hssf.extractor.ExcelExtractor
 * @see org.apache.poi.hslf.extractor.PowerPointExtractor
 * @see org.apache.poi.hdgf.extractor.VisioTextExtractor
 * @see org.apache.poi.hwpf.extractor.WordExtractor
 */
public abstract class POITextExtractor implements Closeable {
    private Closeable fsToClose;
    
	/**
	 * Retrieves all the text from the document.
	 * How cells, paragraphs etc are separated in the text
	 *  is implementation specific - see the javadocs for
	 *  a specific project for details.
	 * @return All the text from the document
	 */
	public abstract String getText();
	
	/**
	 * Returns another text extractor, which is able to
	 *  output the textual content of the document
	 *  metadata / properties, such as author and title.
	 * 
	 * @return the metadata and text extractor
	 */
	public abstract POITextExtractor getMetadataTextExtractor();

	/**
	 * Used to ensure file handle cleanup.
	 * 
	 * @param fs filesystem to close
	 */
	public void setFilesystem(Closeable fs) {
	    fsToClose = fs;
	}
	
	/**
	 * Allows to free resources of the Extractor as soon as
	 * it is not needed any more. This may include closing
	 * open file handles and freeing memory.
	 * 
	 * The Extractor cannot be used after close has been called.
	 */
	@Override
    public void close() throws IOException {
		if(fsToClose != null) {
		    fsToClose.close();
		}
	}

	/**
	 * @return the processed document
	 */
	public abstract Object getDocument();
}
