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

package org.apache.poi.hsmf.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.exceptions.DirectoryChunkNotFoundException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.BlockWritable;

/**
 * Provides a HashMap with the ability to parse a PIOFS object and provide
 * an 'easy to access' hashmap structure for the document chunks inside it.
 *
 * @author Travis Ferguson
 */
public final class POIFSChunkParser {

	public POIFSChunkParser(POIFSFileSystem fs) throws IOException {
		this.setFileSystem(fs);
	}


	/**
	 * Set the POIFileSystem object that this object is using.
	 * @param fs
	 */
	public void setFileSystem(POIFSFileSystem fs) throws IOException {
		this.fs = fs;
		this.reparseFileSystem();
	}

	/**
	 * Get a reference to the FileSystem object that this object is currently using.
	 */
	public POIFSFileSystem getFileSystem() {
		return this.fs;
	}

	/**
	 * Reparse the FileSystem object, resetting all the chunks stored in this object
	 *
	 */
	public void reparseFileSystem() throws IOException {
		// first clear this object of all chunks
		DirectoryEntry root = this.fs.getRoot();
		Iterator iter = root.getEntries();

		this.directoryMap = this.processPOIIterator(iter);
	}

	/**
	 * Returns a list of the standard chunk types, as
	 *  appropriate for the chunks we find in the file.
	 */
	public Chunks identifyChunks() {
		return Chunks.getInstance(this.isNewChunkVersion(this.directoryMap));
	}

	/**
	 * Returns a list of the standard chunk types, as
	 *  appropriate for the chunks we find in the file attachment.
	 */
	private AttachmentChunks identifyAttachmentChunks(Map attachmentMap) {
		return AttachmentChunks.getInstance(this.isNewChunkVersion(attachmentMap));
	}

	/**
	 * Return chunk version of the map in parameter
	 */
	private boolean isNewChunkVersion(Map map) {
		// Are they of the old or new type of strings?
		boolean hasOldStrings = false;
		boolean hasNewStrings = false;
		String oldStringEnd = Types.asFileEnding(Types.OLD_STRING);
		String newStringEnd = Types.asFileEnding(Types.NEW_STRING);

		for(Iterator i = map.keySet().iterator(); i.hasNext();) {
			String entry = (String)i.next();

			if(entry.endsWith( oldStringEnd )) {
				hasOldStrings = true;
			}
			if(entry.endsWith( newStringEnd )) {
				hasNewStrings = true;
			}
		}

		if(hasOldStrings && hasNewStrings) {
			throw new IllegalStateException("Your file contains string chunks of both the old and new types. Giving up");
		} else if(hasNewStrings) {
			return true;
		}
		return false;
	}

	/**
	 * Pull the chunk data that's stored in this object's hashmap out and return it as a HashMap.
	 * @param entryName
	 */
	public Object getChunk(HashMap dirMap, String entryName) {
		if(dirMap == null) {
			return null;
		}
		return dirMap.get(entryName);
	}

	/**
	 * Pull a directory/hashmap out of this hashmap and return it
	 * @param directoryName
	 * @return HashMap containing the chunks stored in the named directoryChunk
	 * @throws DirectoryChunkNotFoundException This is thrown should the directoryMap HashMap on this object be null
	 * or for some reason the directory is not found, is equal to null, or is for some reason not a HashMap/aka Directory Node.
	 */
	public HashMap getDirectoryChunk(String directoryName) throws DirectoryChunkNotFoundException {
		DirectoryChunkNotFoundException excep = new DirectoryChunkNotFoundException(directoryName);
		Object obj = getChunk(this.directoryMap, directoryName);
		if(obj == null || !(obj instanceof HashMap)) throw excep;

		return (HashMap)obj;
	}

	/**
	 * Pulls a ByteArrayOutputStream from this objects HashMap, this can be used to read a byte array of the contents of the given chunk.
	 * @param dirNode
	 * @param chunk
	 * @throws ChunkNotFoundException
	 */
	public Chunk getDocumentNode(HashMap dirNode, Chunk chunk) throws ChunkNotFoundException {
		String entryName = chunk.getEntryName();
		ChunkNotFoundException excep = new ChunkNotFoundException(entryName);
		Object obj = getChunk(dirNode, entryName);
		if(obj == null || !(obj instanceof ByteArrayOutputStream)) throw excep;

		chunk.setValue((ByteArrayOutputStream)obj);

		return chunk;
	}

	/**
	 * Pulls a Chunk out of this objects root Node tree.
	 * @param chunk
	 * @throws ChunkNotFoundException
	 */
	public Chunk getDocumentNode(Chunk chunk) throws ChunkNotFoundException {
		return getDocumentNode(this.directoryMap, chunk);
	}

	/**
	 *
	 * @return a map containing attachment name (String) and data (ByteArrayInputStream)
	 */
	public Map getAttachmentList() {
		Map attachments = new HashMap();
		List attachmentList = new ArrayList();
		for(Iterator i = directoryMap.keySet().iterator(); i.hasNext();) {
			String entry = (String)i.next();

			if(entry.startsWith(AttachmentChunks.namePrefix)) {
				String attachmentIdString = entry.replace(AttachmentChunks.namePrefix, "");
				try {
					int attachmentId = Integer.parseInt(attachmentIdString);
					attachmentList.add(directoryMap.get(entry));
				} catch (NumberFormatException nfe) {
					System.err.println("Invalid attachment id");
				}
			}
		}
		for (Iterator iterator = attachmentList.iterator(); iterator.hasNext();) {
			HashMap AttachmentChunkMap = (HashMap) iterator.next();
			AttachmentChunks attachmentChunks = this.identifyAttachmentChunks(AttachmentChunkMap);
			try {
				Chunk fileName = this.getDocumentNode(AttachmentChunkMap, attachmentChunks.attachLongFileName);
				Chunk content = this.getDocumentNode(AttachmentChunkMap, attachmentChunks.attachData);
				attachments.put(fileName.toString(), new ByteArrayInputStream(content.getValueByteArray().toByteArray()));
			} catch (ChunkNotFoundException e) {
				System.err.println("Invalid attachment chunk");
			}
		}
		return attachments;
	}

	/**
	 * Processes an iterator returned by a POIFS call to getRoot().getEntries()
	 * @param iter
	 * @return
	 * @throws IOException
	 */
	private HashMap processPOIIterator(Iterator iter) throws IOException {
		HashMap currentNode = new HashMap();

		while(iter.hasNext()) {
			Object obj = iter.next();
			if(obj instanceof DocumentNode) {
				this.processDocumentNode((DocumentNode)obj, currentNode);
			} else if(obj instanceof DirectoryNode) {
				String blockName = ((DirectoryNode)obj).getName();
				Iterator viewIt = null;
				if( ((DirectoryNode)obj).preferArray()) {
					Object[] arr = ((DirectoryNode)obj).getViewableArray();
					ArrayList viewList = new ArrayList(arr.length);

					for(int i = 0; i < arr.length; i++) {
						viewList.add(arr[i]);
					}
					viewIt = viewList.iterator();
				} else {
						viewIt = ((DirectoryNode)obj).getViewableIterator();
				}
				//store the next node on the hashmap
				currentNode.put(blockName, processPOIIterator(viewIt));
			} else if(obj instanceof DirectoryProperty) {
				//don't do anything with the directory property chunk...
			} else {
					System.err.println("Unknown node: " + obj.toString());
			}
		}
		return currentNode;
	}

	/**
	 * Processes a document node and adds it to the current directory HashMap
	 * @param obj
	 * @throws java.io.IOException
	 */
	private void processDocumentNode(DocumentNode obj, HashMap currentObj) throws IOException {
		String blockName = obj.getName();

		Iterator viewIt = null;
		if( obj.preferArray()) {
			Object[] arr = obj.getViewableArray();
			ArrayList viewList = new ArrayList(arr.length);

			for(int i = 0; i < arr.length; i++) {
					viewList.add(arr[i]);
			}
			viewIt = viewList.iterator();
		} else {
				viewIt = obj.getViewableIterator();
		}

		while(viewIt.hasNext()) {
			Object view = viewIt.next();

			if(view instanceof DocumentProperty) {
					//we don't care about the properties
			} else if(view instanceof POIFSDocument) {
					//check if our node has blocks or if it can just be read raw.
					int blockCount = ((POIFSDocument)view).countBlocks();
					//System.out.println("Block Name: " + blockName);
					if(blockCount <= 0) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();

						BlockWritable[] bws = ((POIFSDocument)view).getSmallBlocks();
						for(int i = 0; i < bws.length; i++) {
								bws[i].writeBlocks(out);
						}
						currentObj.put(blockName, out);
					} else {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						((POIFSDocument)view).writeBlocks(out);
						currentObj.put(blockName, out);
					}
			} else {
				System.err.println("Unknown View Type: " + view.toString());
			}
		}
	}

	/* private instance variables */
	private static final long serialVersionUID = 1L;
	private POIFSFileSystem fs;
	private HashMap directoryMap;
}
