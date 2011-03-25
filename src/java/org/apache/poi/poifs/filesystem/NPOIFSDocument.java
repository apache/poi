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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;

/**
 * This class manages a document in the NIO POIFS filesystem.
 * This is the {@link NPOIFSFileSystem} version.
 */
public final class NPOIFSDocument implements POIFSViewable {
   private DocumentProperty _property;

   private NPOIFSFileSystem _filesystem;
   private NPOIFSStream _stream;
   private int _block_size;
	
   /**
    * Constructor for an existing Document 
    */
   public NPOIFSDocument(DocumentProperty property, NPOIFSFileSystem filesystem) 
      throws IOException
   {
      this._property = property;
      this._filesystem = filesystem;

      if(property.getSize() < POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
         _stream = new NPOIFSStream(_filesystem.getMiniStore(), property.getStartBlock());
         _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
      } else {
         _stream = new NPOIFSStream(_filesystem, property.getStartBlock());
         _block_size = _filesystem.getBlockStoreBlockSize();
      }
   }

   /**
    * Constructor for a new Document
    *
    * @param name the name of the POIFSDocument
    * @param stream the InputStream we read data from
    */
   public NPOIFSDocument(String name, NPOIFSFileSystem filesystem, InputStream stream) 
      throws IOException 
   {
      this._filesystem = filesystem;

      // Buffer the contents into memory. This is a bit icky...
      // TODO Replace with a buffer up to the mini stream size, then streaming write
      byte[] contents;
      if(stream instanceof ByteArrayInputStream) {
         ByteArrayInputStream bais = (ByteArrayInputStream)stream;
         contents = new byte[bais.available()];
         bais.read(contents);
      } else {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         IOUtils.copy(stream, baos);
         contents = baos.toByteArray();
      }

      // Do we need to store as a mini stream or a full one?
      if(contents.length <= POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
         _stream = new NPOIFSStream(filesystem.getMiniStore());
         _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
      } else {
         _stream = new NPOIFSStream(filesystem);
         _block_size = _filesystem.getBlockStoreBlockSize();
      }

      // Store it
      _stream.updateContents(contents);

      // And build the property for it
      this._property = new DocumentProperty(name, contents.length);
      _property.setStartBlock(_stream.getStartBlock());     
   }
   
   int getDocumentBlockSize() {
      return _block_size;
   }
   
   Iterator<ByteBuffer> getBlockIterator() {
      if(getSize() > 0) {
         return _stream.getBlockIterator();
      } else {
         List<ByteBuffer> empty = Collections.emptyList();
         return empty.iterator();
      }
   }

   /**
    * @return size of the document
    */
   public int getSize() {
      return _property.getSize();
   }

   /**
    * @return the instance's DocumentProperty
    */
   DocumentProperty getDocumentProperty() {
      return _property;
   }

   /**
    * Get an array of objects, some of which may implement POIFSViewable
    *
    * @return an array of Object; may not be null, but may be empty
    */
   public Object[] getViewableArray() {
      Object[] results = new Object[1];
      String result;

      try {
         if(getSize() > 0) {
            // Get all the data into a single array
            byte[] data = new byte[getSize()];
            int offset = 0;
            for(ByteBuffer buffer : _stream) {
               int length = Math.min(_block_size, data.length-offset); 
               buffer.get(data, offset, length);
               offset += length;
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            HexDump.dump(data, 0, output, 0);
            result = output.toString();
         } else {
            result = "<NO DATA>";
         }
      } catch (IOException e) {
         result = e.getMessage();
      }
      results[0] = result;
      return results;
   }

   /**
    * Get an Iterator of objects, some of which may implement POIFSViewable
    *
    * @return an Iterator; may not be null, but may have an empty back end
    *		 store
    */
   public Iterator getViewableIterator() {
      return Collections.EMPTY_LIST.iterator();
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
      StringBuffer buffer = new StringBuffer();

      buffer.append("Document: \"").append(_property.getName()).append("\"");
      buffer.append(" size = ").append(getSize());
      return buffer.toString();
   }
}
