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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Chunk which holds fixed-length properties, and pointer
 *  to the variable length ones (which get their own chunk).
 * There are two kinds of PropertiesChunks, which differ only in 
 *  their headers.
 */
public abstract class PropertiesChunk extends Chunk {
   public static final String NAME = "__properties_version1.0";
   
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
      // TODO
	}
	
	protected void writeProperties(OutputStream out) throws IOException {
	   // TODO
	}
}
