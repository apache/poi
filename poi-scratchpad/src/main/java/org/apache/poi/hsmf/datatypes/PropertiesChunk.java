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

import org.apache.poi.hsmf.datatypes.PropertyValue.LongLongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.ShortPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.TimePropertyValue;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
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
    * The ChunkGroup that these properties apply to. Used when
    *  matching chunks to variable sized properties
    */
   private ChunkGroup parentGroup;
   
   /**
    * Creates a Properties Chunk.
    */
   protected PropertiesChunk(ChunkGroup parentGroup) {
       super(NAME, -1, Types.UNKNOWN);
       this.parentGroup = parentGroup;
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
	
   /**
    * Called once the parent ChunkGroup has been populated, to match
    *  up the Chunks in it with our Variable Sized Properties.
    */
   protected void matchVariableSizedPropertiesToChunks() {
      // Index the Parent Group chunks for easy lookup
      // TODO Is this the right way?
      Map<Integer,Chunk> chunks = new HashMap<Integer, Chunk>();
      for (Chunk chunk : parentGroup.getChunks()) {
         chunks.put(chunk.chunkId, chunk);
      }
      
      // Loop over our values, looking for chunk based ones
      for (List<PropertyValue> vals : properties.values()) {
         if (vals != null) {
            for (PropertyValue val : vals) {
               if (val instanceof ChunkBasedPropertyValue) {
                  ChunkBasedPropertyValue cVal = (ChunkBasedPropertyValue)val;
                  Chunk chunk = chunks.get(cVal.getProperty().id);
//System.err.println(cVal + " -> " + HexDump.toHex(cVal.data));                  
                  
                  // TODO Make sense of the raw offset value
                  
                  if (chunk != null) {
                     cVal.setValue(chunk);
                  } else {
                     logger.log(POILogger.WARN, "No chunk found matching Property " + cVal);
                  }
               }
            }
         }
      }
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
            
            // Wrap properties we don't know about as custom ones
            if (prop == MAPIProperty.UNKNOWN) {
                prop = MAPIProperty.createCustom(id, type, "Unknown " + id);
            }
            if (type == null) {
                logger.log(POILogger.WARN, "Invalid type found, expected ", prop.usualType, 
                        " but got ", typeID, " for property ", prop);
                going = false;
                break;
            }
            
            // Sanity check the property's type against the value's type
            if (prop.usualType != type) {
                // Is it an allowed substitution?
                if (type == Types.ASCII_STRING && prop.usualType == Types.UNICODE_STRING ||
                    type == Types.UNICODE_STRING && prop.usualType == Types.ASCII_STRING) {
                    // It's fine to go with the specified instead of the normal
                } else if (prop.usualType == Types.UNKNOWN) {
                    // We don't know what this property normally is, but it has come
                    // through with a valid type, so use that
                    logger.log(POILogger.INFO, "Property definition for ", prop, 
                            " is missing a type definition, found a value with type ", type);
                } else {
                   // Oh dear, something has gone wrong...
                   logger.log(POILogger.WARN, "Type mismatch, expected ", prop.usualType, 
                              " but got ", type, " for property ", prop);
                   going = false;
                   break;
                }
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
               // We'll match up the chunk later
               propVal = new ChunkBasedPropertyValue(prop, flags, data);
            }
            else if (type == Types.SHORT) {
                propVal = new ShortPropertyValue(prop, flags, data);
             }
            else if (type == Types.LONG) {
                propVal = new LongPropertyValue(prop, flags, data);
             }
            else if (type == Types.LONG_LONG) {
               propVal = new LongLongPropertyValue(prop, flags, data);
            }
            else if (type == Types.TIME) {
               propVal = new TimePropertyValue(prop, flags, data);
            }
            // TODO Add in the rest of the types
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
