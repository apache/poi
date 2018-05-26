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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hsmf.datatypes.PropertyValue.BooleanPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.CurrencyPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.DoublePropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.FloatPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongLongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.NullPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.ShortPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.TimePropertyValue;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndian.BufferUnderrunException;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * <p>
 * A Chunk which holds (single) fixed-length properties, and pointer to the
 * variable length ones / multi-valued ones (which get their own chunk).
 * <p>
 * There are two kinds of PropertiesChunks, which differ only in their headers.
 */
public abstract class PropertiesChunk extends Chunk {
    public static final String NAME = "__properties_version1.0";

    // arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    // standard prefix, defined in the spec
    public static final String VARIABLE_LENGTH_PROPERTY_PREFIX = "__substg1.0_";

    // standard property flags, defined in the spec
    public static final int PROPERTIES_FLAG_READABLE = 2;
    public static final int PROPERTIES_FLAG_WRITEABLE = 4;

    /** For logging problems we spot with the file */
    private POILogger logger = POILogFactory.getLogger(PropertiesChunk.class);

    /**
     * Holds properties, indexed by type. If a property is multi-valued, or
     * variable length, it will be held via a {@link ChunkBasedPropertyValue}.
     */
    private Map<MAPIProperty, PropertyValue> properties = new HashMap<>();

    /**
     * The ChunkGroup that these properties apply to. Used when matching chunks
     * to variable sized and multi-valued properties
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
     * Returns all the properties in the chunk, without looking up any
     * chunk-based values
     */
    public Map<MAPIProperty, PropertyValue> getRawProperties() {
        return properties;
    }

    /**
     * <p>
     * Returns all the properties in the chunk, along with their values.
     * <p>
     * Any chunk-based values will be looked up and returned as such
     */
    public Map<MAPIProperty, List<PropertyValue>> getProperties() {
        Map<MAPIProperty, List<PropertyValue>> props =
                new HashMap<>(properties.size());
        for (MAPIProperty prop : properties.keySet()) {
            props.put(prop, getValues(prop));
        }
        return props;
    }

    /**
     * Defines a property. Multi-valued properties are not yet supported.
     */
    public void setProperty(PropertyValue value) {
        properties.put(value.getProperty(), value);
    }

    /**
     * Returns all values for the given property, looking up chunk based ones as
     * required, of null if none exist
     */
    public List<PropertyValue> getValues(MAPIProperty property) {
        PropertyValue val = properties.get(property);
        if (val == null) {
            return null;
        }
        if (val instanceof ChunkBasedPropertyValue) {
            // ChunkBasedPropertyValue cval = (ChunkBasedPropertyValue)val;
            // TODO Lookup
            return Collections.emptyList();
        } else {
            return Collections.singletonList(val);
        }
    }

    /**
     * Returns the value / pointer to the value chunk of the property, or null
     * if none exists
     */
    public PropertyValue getRawValue(MAPIProperty property) {
        return properties.get(property);
    }

    /**
     * Called once the parent ChunkGroup has been populated, to match up the
     * Chunks in it with our Variable Sized Properties.
     */
    protected void matchVariableSizedPropertiesToChunks() {
        // Index the Parent Group chunks for easy lookup
        // TODO Is this the right way?
        Map<Integer, Chunk> chunks = new HashMap<>();
        for (Chunk chunk : parentGroup.getChunks()) {
            chunks.put(chunk.getChunkId(), chunk);
        }

        // Loop over our values, looking for chunk based ones
        for (PropertyValue val : properties.values()) {
            if (val instanceof ChunkBasedPropertyValue) {
                ChunkBasedPropertyValue cVal = (ChunkBasedPropertyValue) val;
                Chunk chunk = chunks.get(cVal.getProperty().id);
                // System.err.println(cVal.getProperty() + " = " + cVal + " -> "
                // + HexDump.toHex(cVal.data));

                // TODO Make sense of the raw offset value

                if (chunk != null) {
                    cVal.setValue(chunk);
                } else {
                    logger.log(POILogger.WARN, "No chunk found matching Property " + cVal);
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
                int id = LittleEndian.readUShort(value);
                long flags = LittleEndian.readUInt(value);

                // Turn the Type and ID into helper objects
                MAPIType type = Types.getById(typeID);
                MAPIProperty prop = MAPIProperty.get(id);

                // Wrap properties we don't know about as custom ones
                if (prop == MAPIProperty.UNKNOWN) {
                    prop = MAPIProperty.createCustom(id, type, "Unknown " + id);
                }
                if (type == null) {
                    logger.log(POILogger.WARN, "Invalid type found, expected ",
                            prop.usualType, " but got ", typeID,
                            " for property ", prop);
                    going = false;
                    break;
                }

                // Sanity check the property's type against the value's type
                if (prop.usualType != type) {
                    // Is it an allowed substitution?
                    if (type == Types.ASCII_STRING
                        && prop.usualType == Types.UNICODE_STRING
                        || type == Types.UNICODE_STRING
                        && prop.usualType == Types.ASCII_STRING) {
                        // It's fine to go with the specified instead of the
                        // normal
                    } else if (prop.usualType == Types.UNKNOWN) {
                        // We don't know what this property normally is, but it
                        // has come
                        // through with a valid type, so use that
                        logger.log(POILogger.INFO, "Property definition for ", prop,
                            " is missing a type definition, found a value with type ", type);
                    } else {
                        // Oh dear, something has gone wrong...
                        logger.log(POILogger.WARN, "Type mismatch, expected ",
                            prop.usualType, " but got ", type, " for property ", prop);
                        going = false;
                        break;
                    }
                }

                // TODO Detect if it is multi-valued, since if it is
                // then even fixed-length strings store their multiple
                // values in another chunk (much as variable length ones)

                // Work out how long the "data" is
                // This might be the actual data, or just a pointer
                // to another chunk which holds the data itself
                boolean isPointer = false;
                int length = type.getLength();
                if (!type.isFixedLength()) {
                    isPointer = true;
                    length = 8;
                }

                // Grab the data block
                byte[] data = IOUtils.safelyAllocate(length, MAX_RECORD_LENGTH);
                IOUtils.readFully(value, data);

                // Skip over any padding
                if (length < 8) {
                    byte[] padding = new byte[8 - length];
                    IOUtils.readFully(value, padding);
                }

                // Wrap and store
                PropertyValue propVal = null;
                if (isPointer) {
                    // We'll match up the chunk later
                    propVal = new ChunkBasedPropertyValue(prop, flags, data, type);
                } else if (type == Types.NULL) {
                    propVal = new NullPropertyValue(prop, flags, data);
                } else if (type == Types.BOOLEAN) {
                    propVal = new BooleanPropertyValue(prop, flags, data);
                } else if (type == Types.SHORT) {
                    propVal = new ShortPropertyValue(prop, flags, data);
                } else if (type == Types.LONG) {
                    propVal = new LongPropertyValue(prop, flags, data);
                } else if (type == Types.LONG_LONG) {
                    propVal = new LongLongPropertyValue(prop, flags, data);
                } else if (type == Types.FLOAT) {
                    propVal = new FloatPropertyValue(prop, flags, data);
                } else if (type == Types.DOUBLE) {
                    propVal = new DoublePropertyValue(prop, flags, data);
                } else if (type == Types.CURRENCY) {
                    propVal = new CurrencyPropertyValue(prop, flags, data);
                } else if (type == Types.TIME) {
                    propVal = new TimePropertyValue(prop, flags, data);
                }
                // TODO Add in the rest of the types
                else {
                    propVal = new PropertyValue(prop, flags, data, type);
                }

                if (properties.get(prop) != null) {
                    logger.log(POILogger.WARN,
                            "Duplicate values found for " + prop);
                }
                properties.put(prop, propVal);
            } catch (BufferUnderrunException e) {
                // Invalid property, ended short
                going = false;
            }
        }
    }

    /**
     * Writes this chunk in the specified {@code DirectoryEntry}.
     * 
     * @param directory
     *        The directory.
     * @throws IOException
     *         If an I/O error occurs.
     */
    public void writeProperties(DirectoryEntry directory) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<PropertyValue> values = writeProperties(baos);
        baos.close();

        // write the header data with the properties declaration
        directory.createDocument(org.apache.poi.hsmf.datatypes.PropertiesChunk.NAME,
            new ByteArrayInputStream(baos.toByteArray()));

        // write the property values
        writeNodeData(directory, values);
    }
    
    /**
     * Write the nodes for variable-length data. Those properties are returned by
     * {@link #writeProperties(java.io.OutputStream)}.
     * 
     * @param directory
     *        The directory.
     * @param values
     *        The values.
     * @throws IOException
     *         If an I/O error occurs.
     */
    protected void writeNodeData(DirectoryEntry directory, List<PropertyValue> values) throws IOException {
        for (PropertyValue value : values) {
            byte[] bytes = value.getRawValue();
            String nodeName = VARIABLE_LENGTH_PROPERTY_PREFIX + getFileName(value.getProperty(), value.getActualType());
            directory.createDocument(nodeName, new ByteArrayInputStream(bytes));
        }
    }

    /**
     * Writes the header of the properties.
     * 
     * @param out
     *          The {@code OutputStream}.
     * @return The variable-length properties that need to be written in another
     *         node.
     * @throws IOException
     *           If an I/O error occurs.
     */
    protected List<PropertyValue> writeProperties(OutputStream out) throws IOException {
        List<PropertyValue> variableLengthProperties = new ArrayList<>();
        for (Entry<MAPIProperty, PropertyValue> entry : properties.entrySet()) {
            MAPIProperty property = entry.getKey();
            PropertyValue value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (property.id < 0) {
                continue;
            }
            // generic header
            // page 23, point 2.4.2
  	        // tag is the property id and its type
            long tag = Long.parseLong(getFileName(property, value.getActualType()), 16);
            LittleEndian.putUInt(tag, out);
            LittleEndian.putUInt(value.getFlags(), out); // readable + writable

            MAPIType type = getTypeMapping(value.getActualType());
            if (type.isFixedLength()) {
                // page 11, point 2.1.2
                writeFixedLengthValueHeader(out, property, type, value);
            } else {
                // page 12, point 2.1.3
                writeVariableLengthValueHeader(out, property, type, value);
                variableLengthProperties.add(value);
            }
        }
        return variableLengthProperties;
    }

    private void writeFixedLengthValueHeader(OutputStream out, MAPIProperty property, MAPIType type, PropertyValue value) throws IOException {
        // fixed type header
        // page 24, point 2.4.2.1.1
        byte[] bytes = value.getRawValue();
        int length = bytes != null ? bytes.length : 0;
        if (bytes != null) {
            out.write(bytes);
        }
        out.write(new byte[8 - length]);
    }

    private void writeVariableLengthValueHeader(OutputStream out, MAPIProperty propertyEx, MAPIType type,
        PropertyValue value) throws IOException {
        // variable length header
        // page 24, point 2.4.2.2
        byte[] bytes = value.getRawValue();
        int length = bytes != null ? bytes.length : 0;
        // alter the length, as specified in page 25
        if (type == Types.UNICODE_STRING) {
            length += 2;
        } else if (type == Types.ASCII_STRING) {
            length += 1;
        }
        LittleEndian.putUInt(length, out);
        // specified in page 25
        LittleEndian.putUInt(0, out);
    }

    private String getFileName(MAPIProperty property, MAPIType actualType) {
        String str = Integer.toHexString(property.id).toUpperCase(Locale.ROOT);
        while (str.length() < 4) {
            str = "0" + str;
        }
        MAPIType type = getTypeMapping(actualType);
        return str + type.asFileEnding();
    }

    private MAPIType getTypeMapping(MAPIType type) {
        return type == Types.ASCII_STRING ? Types.UNICODE_STRING : type;
    }
}
