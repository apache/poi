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

package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.hsmf.datatypes.PropertyValue.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndian.BufferUnderrunException;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * A Chunk which holds fixed-length properties, and pointer
 *  to the variable length ones (which get their own chunk).
 * There are two kinds of PropertiesChunks, which differ only in 
 *  their headers.
 */
public abstract class PropertiesChunk extends Chunk {
   public static final String NAME = "__properties_version1.0";
   
   /** For logging problems we spot with the file */
   private POILogger logger = POILogFactory.getLogger(PropertiesChunk.class);

   
   /**
    * Holds properties, indexed by type. Properties can be multi-valued
    */
   private Map<MAPIProperty, List<PropertyValue>> properties = 
         new HashMap<MAPIProperty, List<PropertyValue>>();

	/**
	 * Creates a Properties Chunk.
	 */
	protected PropertiesChunk() {
		super(NAME, -1, Types.UNKNOWN);
	}

	@Override
   public String getEntryName() {
	   return NAME;
   }
	
	/**
	 * Returns all the properties in the chunk
	 */
	public Map<MAPIProperty, List<PropertyValue>> getProperties() {
	   return properties;
	}
	
	/**
	 * Returns all values for the given property, of null if none exist
	 */
	public List<PropertyValue> getValues(MAPIProperty property) {
	   return properties.get(property);
	}
	
	/**
	 * Returns the (first/only) value for the given property, or
	 *  null if none exist
	 */
	public PropertyValue getValue(MAPIProperty property) {
	   List<PropertyValue> values = properties.get(property);
	   if (values != null && values.size() > 0) {
	      return values.get(0);
	   }
	   return null;
	}

   protected void readProperties(InputStream value) throws IOException {
      boolean going = true;
      while (going) {
         try {
            // Read in the header
            int typeID = LittleEndian.readUShort(value);
            int id     = LittleEndian.readUShort(value);
            long flags = LittleEndian.readUInt(value);
            
            // Turn the Type and ID into helper objects
            MAPIType type = Types.getById(typeID);
            MAPIProperty prop = MAPIProperty.get(id);
            if (prop.usualType != type) {
               // Oh dear, something has gone wrong...
               logger.log(POILogger.WARN, "Type mismatch, expected ", type, " but got ", prop.usualType);
               going = false;
               break;
            }
            
            // Work out how long the "data" is
            // This might be the actual data, or just a pointer
            //  to another chunk which holds the data itself
            boolean isPointer = false;
            int length = type.getLength();
            if (! type.isFixedLength()) {
               isPointer = true;
               length = 8;
            }
            
            // Grab the data block
            byte[] data = new byte[length];
            IOUtils.readFully(value, data);
            
            // Skip over any padding
            if (length < 8) {
               byte[] padding = new byte[8-length];
               IOUtils.readFully(value, padding);
            }
            
            // Wrap and store
            PropertyValue propVal = null;
            if (isPointer) {
               // TODO Pointer type which can do lookup
            }
            else if (type == Types.LONG_LONG) {
               propVal = new LongLongPropertyValue(prop, flags, data);
            }
            else if (type == Types.TIME) {
               propVal = new TimePropertyValue(prop, flags, data);
            }
            // TODO Add in the rest of the type
            else {
               propVal = new PropertyValue(prop, flags, data);
            }
            
            if (properties.get(prop) == null) {
               properties.put(prop, new ArrayList<PropertyValue>());
            }
            properties.get(prop).add(propVal);
         } catch (BufferUnderrunException e) {
            // Invalid property, ended short
            going = false;
         }
      }
	}
	
	protected void writeProperties(OutputStream out) throws IOException {
	   // TODO
	}
}
