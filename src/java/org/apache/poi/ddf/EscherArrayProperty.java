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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Escher array properties are the most weird construction ever invented
 * with all sorts of special cases.  I'm hopeful I've got them all.
 */
public final class EscherArrayProperty extends EscherComplexProperty implements Iterable<byte[]> {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    /**
     * The size of the header that goes at the
     *  start of the array, before the data
     */
    private static final int FIXED_SIZE = 3 * 2;
    /**
     * Normally, the size recorded in the simple data (for the complex
     *  data) includes the size of the header.
     * There are a few cases when it doesn't though...
     */
    private boolean sizeIncludesHeaderSize = true;

    /**
     * When reading a property from data stream remember if the complex part is empty and set this flag.
     */
    private boolean emptyComplexPart;

    public EscherArrayProperty(short id, byte[] complexData) {
        super(id, checkComplexData(complexData));
        emptyComplexPart = (complexData == null || complexData.length == 0);
    }

    public EscherArrayProperty(short propertyNumber, boolean isBlipId, byte[] complexData) {
        super(propertyNumber, isBlipId, checkComplexData(complexData));
    }

    private static byte[] checkComplexData(byte[] complexData) {
        if (complexData == null || complexData.length == 0) {
            return new byte[6];
        }

        return complexData;
    }

    public int getNumberOfElementsInArray() {
        return (emptyComplexPart) ? 0 : LittleEndian.getUShort(getComplexData(), 0);
    }

    public void setNumberOfElementsInArray(int numberOfElements) {
        int expectedArraySize = numberOfElements * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if (expectedArraySize != getComplexData().length) {
            byte[] newArray = IOUtils.safelyAllocate(expectedArraySize, MAX_RECORD_LENGTH);
            System.arraycopy(getComplexData(), 0, newArray, 0, getComplexData().length);
            setComplexData(newArray);
        }
        LittleEndian.putShort(getComplexData(), 0, (short) numberOfElements);
    }

    public int getNumberOfElementsInMemory() {
        return (emptyComplexPart) ? 0 : LittleEndian.getUShort(getComplexData(), 2);
    }

    public void setNumberOfElementsInMemory(int numberOfElements) {
        int expectedArraySize = numberOfElements * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if (expectedArraySize != getComplexData().length) {
            byte[] newArray = IOUtils.safelyAllocate(expectedArraySize, MAX_RECORD_LENGTH);
            System.arraycopy(getComplexData(), 0, newArray, 0, expectedArraySize);
            setComplexData(newArray);
        }
        LittleEndian.putShort(getComplexData(), 2, (short) numberOfElements);
    }

    public short getSizeOfElements() {
        return (emptyComplexPart) ? 0 : LittleEndian.getShort( getComplexData(), 4 );
    }

    public void setSizeOfElements(int sizeOfElements) {
        LittleEndian.putShort( getComplexData(), 4, (short) sizeOfElements );

        int expectedArraySize = getNumberOfElementsInArray() * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if (expectedArraySize != getComplexData().length) {
            // Keep just the first 6 bytes.  The rest is no good to us anyway.
            byte[] newArray = IOUtils.safelyAllocate(expectedArraySize, MAX_RECORD_LENGTH);
            System.arraycopy( getComplexData(), 0, newArray, 0, 6 );
            setComplexData(newArray);
        }
    }

    public byte[] getElement(int index) {
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        byte[] result = IOUtils.safelyAllocate(actualSize, MAX_RECORD_LENGTH);
        System.arraycopy(getComplexData(), FIXED_SIZE + index * actualSize, result, 0, result.length );
        return result;
    }

    public void setElement(int index, byte[] element) {
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        System.arraycopy( element, 0, getComplexData(), FIXED_SIZE + index * actualSize, actualSize);
    }

    @Override
    public String toString() {
        StringBuilder results = new StringBuilder();
        results.append("propNum: ").append(getPropertyNumber());
        results.append(", propName: ").append(EscherProperties.getPropertyName( getPropertyNumber() ));
        results.append(", complex: ").append(isComplex());
        results.append(", blipId: ").append(isBlipId());
        results.append(", data: \n");
        results.append("    {EscherArrayProperty:" + '\n');
        results.append("     Num Elements: ").append(getNumberOfElementsInArray()).append('\n');
        results.append("     Num Elements In Memory: ").append(getNumberOfElementsInMemory()).append('\n');
        results.append("     Size of elements: ").append(getSizeOfElements()).append('\n');
        for (int i = 0; i < getNumberOfElementsInArray(); i++) {
            results.append("     Element ").append(i).append(": ").append(HexDump.toHex(getElement(i))).append('\n');
        }
        results.append("}" + '\n');

        return results.toString();
    }

    @Override
    public String toXml(String tab){
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append("<").append(getClass().getSimpleName()).append(" id=\"0x").append(HexDump.toHex(getId()))
                .append("\" name=\"").append(getName()).append("\" blipId=\"")
                .append(isBlipId()).append("\">\n");
        for (int i = 0; i < getNumberOfElementsInArray(); i++) {
            builder.append("\t").append(tab).append("<Element>").append(HexDump.toHex(getElement(i))).append("</Element>\n");
        }
        builder.append(tab).append("</").append(getClass().getSimpleName()).append(">");
        return builder.toString();
    }

    /**
     * We have this method because the way in which arrays in escher works
     * is screwed for seemly arbitrary reasons.  While most properties are
     * fairly consistent and have a predictable array size, escher arrays
     * have special cases.
     *
     * @param data      The data array containing the escher array information
     * @param offset    The offset into the array to start reading from.
     * @return  the number of bytes used by this complex property.
     */
    public int setArrayData(byte[] data, int offset) {
        if (emptyComplexPart){
            setComplexData(new byte[0]);
        } else {
            short numElements = LittleEndian.getShort(data, offset);
            // LittleEndian.getShort(data, offset + 2); // numReserved
            short sizeOfElements = LittleEndian.getShort(data, offset + 4);

            // the code here seems to depend on complexData already being
            // sized correctly via the constructor
            int arraySize = getActualSizeOfElements(sizeOfElements) * numElements;
            if (arraySize == getComplexData().length) {
                // The stored data size in the simple block excludes the header size
                setComplexData(new byte[arraySize + 6]);
                sizeIncludesHeaderSize = false;
            }
            System.arraycopy(data, offset, getComplexData(), 0, getComplexData().length );
        }
        return getComplexData().length;
    }

    /**
     * Serializes the simple part of this property.  ie the first 6 bytes.
     *
     * Needs special code to handle the case when the size doesn't
     *  include the size of the header block
     */
    @Override
    public int serializeSimplePart(byte[] data, int pos) {
        LittleEndian.putShort(data, pos, getId());
        int recordSize = getComplexData().length;
        if(!sizeIncludesHeaderSize) {
            recordSize -= 6;
        }
        LittleEndian.putInt(data, pos + 2, recordSize);
        return 6;
    }

    /**
     * Sometimes the element size is stored as a negative number.  We
     * negate it and shift it to get the real value.
     */
    private static int getActualSizeOfElements(short sizeOfElements) {
        if (sizeOfElements < 0) {
            return (short) ( ( -sizeOfElements ) >> 2 );
        }
        return sizeOfElements;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Iterator<byte[]>(){
            int idx;
            @Override
            public boolean hasNext() {
                return (idx < getNumberOfElementsInArray());
            }
            
            @Override
            public byte[] next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return getElement(idx++);
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("not yet implemented");
            }
        };
    }
    
    
}
