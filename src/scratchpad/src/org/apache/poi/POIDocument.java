/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This holds the common functionality for all POI
 *  Document classes.
 * Currently, this relates to Document Information Properties 
 * 
 * @author Nick Burch
 */
public abstract class POIDocument {
	// Holds metadata on our document
	protected SummaryInformation sInf;
	protected DocumentSummaryInformation dsInf;
	
	protected POIFSFileSystem filesystem;
	
	/** 
	 * Fetch the Document Summary Information of the document
	 */
	public DocumentSummaryInformation getDocumentSummaryInformation() { return dsInf; }

	/** 
	 * Fetch the Summary Information of the document
	 */
	public SummaryInformation getSummaryInformation() { return sInf; }

	/**
	 * Find, and create objects for, the standard
	 *  Documment Information Properties (HPSF)
	 */
	protected void readProperties() {
		// DocumentSummaryInformation
		dsInf = (DocumentSummaryInformation)getPropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

		// SummaryInformation
		sInf = (SummaryInformation)getPropertySet(SummaryInformation.DEFAULT_STREAM_NAME);
	}

	/** 
	 * For a given named property entry, either return it or null if
	 *  if it wasn't found
	 */
	protected PropertySet getPropertySet(String setName) {
		DocumentInputStream dis;
		try {
			// Find the entry, and get an input stream for it
			dis = filesystem.createDocumentInputStream(setName);
		} catch(IOException ie) {
			// Oh well, doesn't exist
			System.err.println("Error getting property set with name " + setName + "\n" + ie);
			return null;
		}

		try {
			// Create the Property Set
			PropertySet set = PropertySetFactory.create(dis);
			return set;
		} catch(IOException ie) {
			// Must be corrupt or something like that
			System.err.println("Error creating property set with name " + setName + "\n" + ie);
		} catch(org.apache.poi.hpsf.HPSFException he) {
			// Oh well, doesn't exist
			System.err.println("Error creating property set with name " + setName + "\n" + he);
		}
		return null;
	}
	
	/**
	 * Writes out the standard Documment Information Properties (HPSF)
	 * @param outFS the POIFSFileSystem to write the properties into
	 */
	protected void writeProperties(POIFSFileSystem outFS) throws IOException {
		if(sInf != null) {
			writePropertySet("\005SummaryInformation",sInf,outFS);
		}
		if(dsInf != null) {
			writePropertySet("\005DocumentSummaryInformation",dsInf,outFS);
		}
	}
	
	/**
	 * Writes out a given ProperySet
	 * @param name the (POIFS Level) name of the property to write
	 * @param set the PropertySet to write out 
	 * @param outFS the POIFSFileSystem to write the property into
	 */
	protected void writePropertySet(String name, PropertySet set, POIFSFileSystem outFS) throws IOException {
		try {
			MutablePropertySet mSet = new MutablePropertySet(set);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			mSet.write(bOut);
			byte[] data = bOut.toByteArray();
			ByteArrayInputStream bIn = new ByteArrayInputStream(data);
			outFS.createDocument(bIn,name);
			System.out.println("Wrote property set " + name + " of size " + data.length);
		} catch(org.apache.poi.hpsf.WritingNotSupportedException wnse) {
			System.err.println("Couldn't write property set with name " + name + " as not supported by HPSF yet");
		}
	}
}
