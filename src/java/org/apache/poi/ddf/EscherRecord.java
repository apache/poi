
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

package org.apache.poi.ddf;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The base abstract record from which all escher records are defined.  Subclasses will need
 * to define methods for serialization/deserialization and for determining the record size.
 */
public abstract class EscherRecord implements Cloneable {
    private static final BitField fInstance = BitFieldFactory.getInstance(0xfff0);
    private static final BitField fVersion = BitFieldFactory.getInstance(0x000f);

    private short _options;
    private short _recordId;

    /**
     * Create a new instance
     */
    public EscherRecord() {
        // fields uninitialised
    }

    /**
     * Delegates to fillFields(byte[], int, EscherRecordFactory)
     *
     * @param data they bytes to serialize from
     * @param f the escher record factory
     * @return The number of bytes written.
     *
     * @see #fillFields(byte[], int, org.apache.poi.ddf.EscherRecordFactory)
     */
    protected int fillFields( byte[] data, EscherRecordFactory f )
    {
        return fillFields( data, 0, f );
    }

    /**
     * The contract of this method is to deserialize an escher record including
     * it's children.
     *
     * @param data      The byte array containing the serialized escher
     *                  records.
     * @param offset    The offset into the byte array.
     * @param recordFactory     A factory for creating new escher records.
     * @return          The number of bytes written.
     */
    public abstract int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory );

    /**
     * Reads the 8 byte header information and populates the <code>options</code>
     * and <code>recordId</code> records.
     *
     * @param data      the byte array to read from
     * @param offset    the offset to start reading from
     * @return          the number of bytes remaining in this record.  This
     *                  may include the children if this is a container.
     */
    protected int readHeader( byte[] data, int offset ) {
        _options = LittleEndian.getShort( data, offset );
        _recordId = LittleEndian.getShort( data, offset + 2 );
        return LittleEndian.getInt( data, offset + 4 );
    }

    /**
     * Read the options field from header and return instance part of it.
     * @param data      the byte array to read from
     * @param offset    the offset to start reading from
     * @return          value of instance part of options field
     */
    protected static short readInstance(byte[] data, int offset ) {
        final short options = LittleEndian.getShort( data, offset );
        return fInstance.getShortValue( options );
    }

    /**
     * Determine whether this is a container record by inspecting the option field.
     *
     * @return  true is this is a container field.
     */
    public boolean isContainerRecord() {
        return getVersion() == (short)0x000f;
    }

    /**
     * Note that <code>options</code> is an internal field.
     * Use {@link #setInstance(short)} ()} and {@link #setVersion(short)} ()} to set the actual fields.
     *
     * @return The options field for this record. All records have one.
     */
    @Internal
    public short getOptions()
    {
        return _options;
    }

    /**
     * Set the options this this record. Container records should have the
     * last nibble set to 0xF.<p>
     *
     * Note that {@code options} is an internal field.
     * Use {@link #getInstance()} and {@link #getVersion()} to access actual fields.
     *
     * @param options the record options
     */
    @Internal
    public void setOptions( short options ) {
        // call to handle correct/incorrect values
        setVersion( fVersion.getShortValue( options ) );
        setInstance( fInstance.getShortValue( options ) );
        _options = options;
    }

    /**
     * Serializes to a new byte array.  This is done by delegating to
     * serialize(int, byte[]);
     *
     * @return  the serialized record.
     * @see #serialize(int, byte[])
     */
    public byte[] serialize()
    {
        byte[] retval = new byte[getRecordSize()];

        serialize( 0, retval );
        return retval;
    }

    /**
     * Serializes to an existing byte array without serialization listener.
     * This is done by delegating to serialize(int, byte[], EscherSerializationListener).
     *
     * @param offset    the offset within the data byte array.
     * @param data      the data array to serialize to.
     * @return          The number of bytes written.
     *
     * @see #serialize(int, byte[], org.apache.poi.ddf.EscherSerializationListener)
     */
    public int serialize( int offset, byte[] data)
    {
        return serialize( offset, data, new NullEscherSerializationListener() );
    }

    /**
     * Serializes the record to an existing byte array.
     *
     * @param offset    the offset within the byte array
     * @param data      the data array to serialize to
     * @param listener  a listener for begin and end serialization events.  This
     *                  is useful because the serialization is
     *                  hierarchical/recursive and sometimes you need to be able
     *                  break into that.
     * @return the number of bytes written.
     */
    public abstract int serialize( int offset, byte[] data, EscherSerializationListener listener );

    /**
     * Subclasses should effeciently return the number of bytes required to
     * serialize the record.
     *
     * @return  number of bytes
     */
    abstract public int getRecordSize();

    /**
     * Return the current record id.
     *
     * @return  The 16 bit record id.
     */
    public short getRecordId() {
        return _recordId;
    }

    /**
     * Sets the record id for this record.
     *
     * @param recordId the record id
     */
    public void setRecordId( short recordId ) {
        _recordId = recordId;
    }

    /**
     * @return  Returns the children of this record.  By default this will
     *          be an empty list.  EscherCotainerRecord is the only record
     *          that may contain children.
     *
     * @see EscherContainerRecord
     */
    public List<EscherRecord> getChildRecords() { return Collections.emptyList(); }

    /**
     * Sets the child records for this record.  By default this will throw
     * an exception as only EscherContainerRecords may have children.
     *
     * @param childRecords  Not used in base implementation.
     */
    public void setChildRecords(List<EscherRecord> childRecords) {
        throw new UnsupportedOperationException("This record does not support child records.");
    }

    /**
     * Escher records may need to be clonable in the future.
     *
     * @return the cloned object
     *
     * @throws CloneNotSupportedException if the subclass hasn't implemented {@link Cloneable}
     */
    @Override
    public EscherRecord clone() throws CloneNotSupportedException {
        return (EscherRecord)super.clone();
    }

    /**
     * Returns the indexed child record.
     *
     * @param index the index of the child within the child records
     * @return the indexed child record
     */
    public EscherRecord getChild( int index ) {
        return getChildRecords().get(index);
    }

    /**
     * The display methods allows escher variables to print the record names
     * according to their hierarchy.
     *
     * @param w         The print writer to output to.
     * @param indent    The current indent level.
     */
    public void display(PrintWriter w, int indent)
    {
        for (int i = 0; i < indent * 4; i++) {
            w.print(' ');
        }
        w.println(getRecordName());
    }

    /**
     * Subclasses should return the short name for this escher record.
     *
     * @return the short name for this escher record
     */
    public abstract String getRecordName();

    /**
     * Returns the instance part of the option record.
     *
     * @return The instance part of the record
     */
    public short getInstance()
    {
        return fInstance.getShortValue( _options );
    }

    /**
     * Sets the instance part of record
     *
     * @param value instance part value
     */
    public void setInstance( short value )
    {
        _options = fInstance.setShortValue( _options, value );
    }

    /**
     * Returns the version part of the option record.
     *
     * @return The version part of the option record
     */
    public short getVersion()
    {
        return fVersion.getShortValue( _options );
    }

    /**
     * Sets the version part of record
     *
     * @param value version part value
     */
    public void setVersion( short value )
    {
        _options = fVersion.setShortValue( _options, value );
    }

    public String toXml(){
        return toXml("");
    }

    /**
     * @param tab - each children must be indented right relative to its parent
     * @return xml representation of this record
     */
    public final String toXml(String tab){
        final String nl = System.getProperty( "line.separator" );
        String clsNm = getClass().getSimpleName();
        StringBuilder sb = new StringBuilder(1000);
        sb.append(tab).append("<").append(clsNm)
          .append(" recordId=\"0x").append(HexDump.toHex(getRecordId()))
          .append("\" version=\"0x").append(HexDump.toHex(getVersion()))
          .append("\" instance=\"0x").append(HexDump.toHex(getInstance()))
          .append("\" options=\"0x").append(HexDump.toHex(getOptions()))
          .append("\" recordSize=\"").append(getRecordSize());
        Object[][] attrList = getAttributeMap();
        if (attrList == null || attrList.length == 0) {
            sb.append("\" />").append(nl);
        } else {
            sb.append("\">").append(nl);
            String childTab = tab+"   ";
            for (Object[] attrs : attrList) {
                String tagName = capitalizeAndTrim((String)attrs[0]);
                boolean hasValue = false;
                boolean lastChildComplex = false;
                for (int i=0; i<attrs.length-1; i+=2) {
                    Object value = attrs[i+1];
                    if (value == null) {
                        // ignore null values
                        continue;
                    }
                    if (!hasValue) {
                        // only add tagname, when there was a value
                        sb.append(childTab).append("<").append(tagName).append(">");
                    }
                    // add names for optional attributes
                    String optName = capitalizeAndTrim((String)attrs[i+0]);
                    if (i>0) {
                        sb.append(nl).append(childTab).append("  <").append(optName).append(">");
                    }
                    lastChildComplex = appendValue(sb, value, true, childTab);
                    if (i>0) {
                        sb.append(nl).append(childTab).append("  </").append(optName).append(">");
                    }
                    hasValue = true;
                }
                if (hasValue) {
                    if (lastChildComplex) {
                        sb.append(nl).append(childTab);
                    }
                    sb.append("</").append(tagName).append(">").append(nl);
                }
            }
            sb.append(tab).append("</").append(clsNm).append(">");
        }
        return sb.toString();
    }

    @Override
    public final String toString() {
        final String nl = System.getProperty( "line.separator" );
        StringBuilder sb = new StringBuilder(1000);
        sb.append(getClass().getName()).append(" (").append(getRecordName()).append("):").append(nl)
          .append("  RecordId: 0x").append(HexDump.toHex( getRecordId() )).append(nl)
          .append("  Version: 0x").append(HexDump.toHex( getVersion() )).append(nl)
          .append("  Instance: 0x").append(HexDump.toHex( getInstance() )).append(nl)
          .append("  Options: 0x").append(HexDump.toHex( getOptions() )).append(nl)
          .append("  Record Size: ").append( getRecordSize() );

        Object[][] attrList = getAttributeMap();
        if (attrList != null && attrList.length > 0) {
            String childTab = "  ";
            for (Object[] attrs : attrList) {
                for (int i=0; i<attrs.length-1; i+=2) {
                    Object value = attrs[i+1];
                    if (value == null) {
                        // ignore null values
                        continue;
                    }
                    String name = (String)attrs[i+0];
                    sb.append(nl).append(childTab).append(name).append(": ");
                    appendValue(sb, value, false, childTab);
                }
            }
        }

        return sb.toString();
    }
    
    /**
     * @return true, if value was a complex record, false otherwise
     */
    private static boolean appendValue(StringBuilder sb, Object value, boolean toXML, String childTab) {
        final String nl = System.getProperty( "line.separator" );
        boolean isComplex = false;
        if (value instanceof String) {
            if (toXML) {
                escapeXML((String)value, sb);
            } else {
                sb.append((String)value);
            }
        } else if (value instanceof Byte) {
            sb.append("0x").append(HexDump.toHex((Byte)value));
        } else if (value instanceof Short) {
            sb.append("0x").append(HexDump.toHex((Short)value));
        } else if (value instanceof Integer) {
            sb.append("0x").append(HexDump.toHex((Integer)value));
        } else if (value instanceof byte[]) {
            sb.append(nl).append(HexDump.toHex((byte[])value, 32).replaceAll("(?m)^",childTab+"   "));
        } else if (value instanceof Boolean) {
            sb.append(((Boolean)value).booleanValue());
        } else if (value instanceof EscherRecord) {
            EscherRecord er = (EscherRecord)value;
            if (toXML) {
                sb.append(nl).append(er.toXml(childTab+"    "));
            } else {
                sb.append(er.toString().replaceAll("(?m)^",childTab));
            }
            isComplex = true;
        } else if (value instanceof EscherProperty) {
            EscherProperty ep = (EscherProperty)value;
            if (toXML) {
                sb.append(nl).append(ep.toXml(childTab+"  "));
            } else {
                sb.append(ep.toString().replaceAll("(?m)^",childTab));
            }
            isComplex = true;
        } else {
            throw new IllegalArgumentException("unknown attribute type "+value.getClass().getSimpleName());
        }
        return isComplex;
    }

    /**
     * For the purpose of providing toString() and toXml() a subclass can either override those methods
     * or provide a Object[][] array in the form {@code { { "Attribute Name (Header)", value, "optional attribute", value }, ... } }.<p>
     *
     * Null values won't be printed.<p>
     *
     * The attributes record, version, instance, options must not be returned.
     *
     * @return the attribute map
     * 
     * @since POI 3.17-beta2
     */
    @Internal
    protected abstract Object[][] getAttributeMap();

    private static String capitalizeAndTrim(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length());
        boolean capitalizeNext = true;
        for (char ch : str.toCharArray()) {
            if (!Character.isLetterOrDigit(ch)) {
                capitalizeNext = true;
                continue;
            }

            if (capitalizeNext) {
                if (!Character.isLetter(ch)) {
                    sb.append('_');
                } else {
                    ch = Character.toTitleCase(ch);
                }
                capitalizeNext = false;
            }
            sb.append(ch);
        }

        return sb.toString();
    }

    private static void escapeXML(String s, StringBuilder out) {
        if (s == null || s.isEmpty()) {
            return;
        }
        for (char c : s.toCharArray()) {
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
    }
}