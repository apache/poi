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

package org.apache.poi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This holds the common functionality for all POI
 *  Document classes.
 * Currently, this relates to Document Information Properties 
 * 
 * @author Nick Burch
 */
public abstract class POIDocument {
	/** Holds metadata on our document */
	protected SummaryInformation sInf;
	/** Holds further metadata on our document */
	protected DocumentSummaryInformation dsInf;
	/** The open POIFS FileSystem that contains our document */
	protected POIFSFileSystem filesystem;
	
	/** For our own logging use */
	protected POILogger logger = POILogFactory.getLogger(this.getClass());

	
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
			logger.log(POILogger.WARN, "Error getting property set with name " + setName + "\n" + ie);
			return null;
		}

		try {
			// Create the Property Set
			PropertySet set = PropertySetFactory.create(dis);
			return set;
		} catch(IOException ie) {
			// Must be corrupt or something like that
			logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + ie);
		} catch(org.apache.poi.hpsf.HPSFException he) {
			// Oh well, doesn't exist
			logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + he);
		}
		return null;
	}
	
	/**
	 * Writes out the standard Documment Information Properties (HPSF)
	 * @param outFS the POIFSFileSystem to write the properties into
	 */
	protected void writeProperties(POIFSFileSystem outFS) throws IOException {
		writeProperties(outFS, null);
	}
	/**
	 * Writes out the standard Documment Information Properties (HPSF)
	 * @param outFS the POIFSFileSystem to write the properties into
	 * @param writtenEntries a list of POIFS entries to add the property names too
	 */
	protected void writeProperties(POIFSFileSystem outFS, List writtenEntries) throws IOException {
		if(sInf != null) {
			writePropertySet(SummaryInformation.DEFAULT_STREAM_NAME,sInf,outFS);
			if(writtenEntries != null) {
				writtenEntries.add(SummaryInformation.DEFAULT_STREAM_NAME);
			}
		}
		if(dsInf != null) {
			writePropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME,dsInf,outFS);
			if(writtenEntries != null) {
				writtenEntries.add(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			}
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

			logger.log(POILogger.INFO, "Wrote property set " + name + " of size " + data.length);
		} catch(org.apache.poi.hpsf.WritingNotSupportedException wnse) {
			System.err.println("Couldn't write property set with name " + name + " as not supported by HPSF yet");
		}
	}

	/**
	 * Copies nodes from one POIFS to the other minus the excepts
	 * @param source is the source POIFS to copy from
	 * @param target is the target POIFS to copy to
	 * @param excepts is a list of Strings specifying what nodes NOT to copy
	 */
	protected void copyNodes(POIFSFileSystem source, POIFSFileSystem target,
	                          List excepts) throws IOException {
		//System.err.println("CopyNodes called");

		DirectoryEntry root = source.getRoot();
		DirectoryEntry newRoot = target.getRoot();

		Iterator entries = root.getEntries();

		while (entries.hasNext()) {
			Entry entry = (Entry)entries.next();
			if (!isInList(entry.getName(), excepts)) {
				copyNodeRecursively(entry,newRoot);
			}
		}
	}
		
	/**
	 * Checks to see if the String is in the list, used when copying
	 *  nodes between one POIFS and another
	 */
	private boolean isInList(String entry, List list) {
		for (int k = 0; k < list.size(); k++) {
			if (list.get(k).equals(entry)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Copies an Entry into a target POIFS directory, recursively
	 */
	private void copyNodeRecursively(Entry entry, DirectoryEntry target)
	throws IOException {
		//System.err.println("copyNodeRecursively called with "+entry.getName()+
		//                   ","+target.getName());
		DirectoryEntry newTarget = null;
		if (entry.isDirectoryEntry()) {
			newTarget = target.createDirectory(entry.getName());
			Iterator entries = ((DirectoryEntry)entry).getEntries();

			while (entries.hasNext()) {
				copyNodeRecursively((Entry)entries.next(),newTarget);
			}
		} else {
			DocumentEntry dentry = (DocumentEntry)entry;
			DocumentInputStream dstream = new DocumentInputStream(dentry);
			target.createDocument(dentry.getName(),dstream);
			dstream.close();
		}
	}
}
