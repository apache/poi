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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Removal;

/**
 * Escher array properties are the most weird construction ever invented
 * with all sorts of special cases.  I'm hopeful I've got them all.
 */
public final class EscherArrayProperty extends EscherComplexProperty implements Iterable<byte[]> {

    // arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    /**
     * The size of the header that goes at the start of the array, before the data
     */
    private static final int FIXED_SIZE = 3 * 2;
    /**
     * Normally, the size recorded in the simple data (for the complex data) includes the size of the header.
     * There are a few cases when it doesn't though...
     */
    private boolean sizeIncludesHeaderSize = true;

    /**
     * When reading a property from data stream remember if the complex part is empty and set this flag.
     */
    private final boolean emptyComplexPart;

    /**
     * Create an instance of an escher array property.
     * This constructor can be used to create emptyComplexParts with a complexSize = 0.
     * Preferably use {@link #EscherArrayProperty(EscherPropertyTypes, boolean, int)} with {@link #setComplexData(byte[])}.
     *
     * @param id          The id consists of the property number, a flag indicating whether this is a blip id and a flag
     *                    indicating that this is a complex property.
     * @param complexSize the data size
     */
    @Internal
    public EscherArrayProperty(short id, int complexSize) {
        // this is called by EscherPropertyFactory which happens to call it with empty parts
        // if a part is initial empty, don't allow it to contain something again
        super(id, complexSize);
        emptyComplexPart = (complexSize == 0);
    }

    /**
     * Create an instance of an escher array property.
     * This constructor defaults to a 6 bytes header if the complexData is null or byte[0].
     *
     * @param propertyNumber the property number part of the property id
     * @param isBlipId {@code true}, if it references a blip
     * @param complexData the data
     *
     * @deprecated use {@link #EscherArrayProperty(EscherPropertyTypes, boolean, int)} and {@link #setComplexData(byte[])}
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public EscherArrayProperty(short propertyNumber, boolean isBlipId, byte[] complexData) {
        // this is called by user code, if the complexData is empty/null, allocate a space for a valid header
        // be aware, that there are complex data areas with less than 6 bytes
        this((short)(propertyNumber | (isBlipId ? IS_BLIP : 0)), safeSize(complexData == null ? 0 : complexData.length));
        setComplexData(complexData);
    }

    /**
     * Create an instance of an escher array property.
     * This constructor defaults to a 6 bytes header if the complexSize is 0.
     *
     * @param type the property type of the property id
     * @param isBlipId {@code true}, if it references a blip
     * @param complexSize the data size
     */
    public EscherArrayProperty(EscherPropertyTypes type, boolean isBlipId, int complexSize) {
        this((short)(type.propNumber | (isBlipId ? IS_BLIP : 0)), safeSize(complexSize));
    }

    private static int safeSize(int complexSize) {
        // when called by user code, fix the size to be valid for the header
        return complexSize == 0 ? 6 : complexSize;
    }

    public int getNumberOfElementsInArray() {
        return (emptyComplexPart) ? 0 : LittleEndian.getUShort(getComplexData(), 0);
    }

    public void setNumberOfElementsInArray(int numberOfElements) {
        if (emptyComplexPart) {
            return;
        }
        rewriteArray(numberOfElements, false);
        LittleEndian.putShort(getComplexData(), 0, (short) numberOfElements);
    }

    private void rewriteArray(int numberOfElements, boolean copyToNewLen) {
        int expectedArraySize = numberOfElements * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        resizeComplexData(expectedArraySize, copyToNewLen ? expectedArraySize : getComplexData().length);
    }

    public int getNumberOfElementsInMemory() {
        return (emptyComplexPart) ? 0 : LittleEndian.getUShort(getComplexData(), 2);
    }

    public void setNumberOfElementsInMemory(int numberOfElements) {
        if (emptyComplexPart) {
            return;
        }
        rewriteArray(numberOfElements, true);
        LittleEndian.putShort(getComplexData(), 2, (short) numberOfElements);
    }

    public short getSizeOfElements() {
        return (emptyComplexPart) ? 0 : LittleEndian.getShort( getComplexData(), 4 );
    }

    public void setSizeOfElements(int sizeOfElements) {
        if (emptyComplexPart) {
            return;
        }
        LittleEndian.putShort( getComplexData(), 4, (short) sizeOfElements );

        int expectedArraySize = getNumberOfElementsInArray() * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        // Keep just the first 6 bytes.  The rest is no good to us anyway.
        resizeComplexData(expectedArraySize, 6);
    }

    public byte[] getElement(int index) {
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        return IOUtils.safelyClone(getComplexData(), FIXED_SIZE + index * actualSize, actualSize, MAX_RECORD_LENGTH);
    }

    public void setElement(int index, byte[] element) {
        if (emptyComplexPart) {
            return;
        }
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        System.arraycopy( element, 0, getComplexData(), FIXED_SIZE + index * actualSize, actualSize);
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
        if (emptyComplexPart) {
            resizeComplexData(0);
        } else {
            short numElements = LittleEndian.getShort(data, offset);
            // LittleEndian.getShort(data, offset + 2); // numReserved
            short sizeOfElements = LittleEndian.getShort(data, offset + 4);

            // the code here seems to depend on complexData already being
            // sized correctly via the constructor
            int cdLen = getComplexData().length;
            int arraySize = getActualSizeOfElements(sizeOfElements) * numElements;
            if (arraySize == cdLen) {
                // The stored data size in the simple block excludes the header size
                resizeComplexData(arraySize + 6, 0);
                sizeIncludesHeaderSize = false;
            }
            setComplexData(data, offset);
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
        if (!sizeIncludesHeaderSize) {
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

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "numElements", this::getNumberOfElementsInArray,
            "numElementsInMemory", this::getNumberOfElementsInMemory,
            "sizeOfElements", this::getSizeOfElements,
            "elements", () -> StreamSupport.stream(spliterator(), false).collect(Collectors.toList())
        );
    }
}
