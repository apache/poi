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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Plex of CPs stored in File (PLCF)
 * <p>
 * common data structure in a Word file. Contains an array of 4 byte ints in the
 * front that relate to an array of arbitrary data structures in the back.
 * <p>
 * See page 184 of official documentation for details
 */
public final class PlexOfCps {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 10_485_760;

    private int _iMac;
    private int _cbStruct;
    private List<GenericPropertyNode> _props;

    public PlexOfCps(int sizeOfStruct) {
        _props = new ArrayList<>();
        _cbStruct = sizeOfStruct;
    }

    /**
     * Constructor
     *
     * @param cb       The size of PLCF in bytes
     * @param cbStruct The size of the data structure type stored in this PlexOfCps.
     */
    public PlexOfCps(byte[] buf, int start, int cb, int cbStruct) {
        // Figure out the number we hold
        _iMac = (cb - 4) / (4 + cbStruct);

        _cbStruct = cbStruct;
        _props = new ArrayList<>(_iMac);

        for (int x = 0; x < _iMac; x++) {
            _props.add(getProperty(x, buf, start));
        }
    }

    @Internal
    void adjust(int startCp, int shift) {
        for (GenericPropertyNode node : _props) {
            if (node.getStart() > startCp) {
                if (node.getStart() + shift < startCp) {
                    node.setStart(startCp);
                } else {
                    node.setStart(node.getStart() + shift);
                }
            }
            if (node.getEnd() >= startCp) {
                if (node.getEnd() + shift < startCp) {
                    node.setEnd(startCp);
                } else {
                    node.setEnd(node.getEnd() + shift);
                }
            }
        }
    }

    public GenericPropertyNode getProperty(int index) {
        return _props.get(index);
    }

    public void addProperty(GenericPropertyNode node) {
        _props.add(node);
        _iMac++;
    }

    void remove(int index) {
        _props.remove(index);
        _iMac--;
    }

    public byte[] toByteArray() {
        int size = _props.size();
        int cpBufSize = ((size + 1) * LittleEndianConsts.INT_SIZE);
        int structBufSize = +(_cbStruct * size);
        int bufSize = cpBufSize + structBufSize;

        byte[] buf = IOUtils.safelyAllocate(bufSize, MAX_RECORD_LENGTH);

        int nodeEnd = 0;
        for (int x = 0; x < size; x++) {
            GenericPropertyNode node = _props.get(x);
            nodeEnd = node.getEnd();

            // put the starting offset of the property into the plcf.
            LittleEndian.putInt(buf, (LittleEndianConsts.INT_SIZE * x), node.getStart());

            // put the struct into the plcf
            System.arraycopy(node.getBytes(), 0, buf, cpBufSize + (x * _cbStruct), _cbStruct);
        }
        // put the ending offset of the last property into the plcf.
        LittleEndian.putInt(buf, LittleEndianConsts.INT_SIZE * size, nodeEnd);

        return buf;
    }

    private GenericPropertyNode getProperty(int index, byte[] buf, int offset) {
        int start = LittleEndian.getInt(buf, offset + getIntOffset(index));
        int end = LittleEndian.getInt(buf, offset + getIntOffset(index + 1));

        byte[] struct = IOUtils.safelyClone(buf, offset + getStructOffset(index), _cbStruct, MAX_RECORD_LENGTH);

        return new GenericPropertyNode(start, end, struct);
    }

    private int getIntOffset(int index) {
        return index * 4;
    }

    /**
     * returns the number of data structures in this PlexofCps.
     *
     * @return The number of data structures in this PlexofCps
     */
    public int length() {
        return _iMac;
    }

    /**
     * Returns the offset, in bytes, from the beginning if this PlexOfCps to the
     * data structure at index.
     *
     * @param index The index of the data structure.
     * @return The offset, in bytes, from the beginning if this PlexOfCps to the
     * data structure at index.
     */
    private int getStructOffset(int index) {
        return (4 * (_iMac + 1)) + (_cbStruct * index);
    }

    GenericPropertyNode[] toPropertiesArray() {
        if (_props == null || _props.isEmpty())
            return new GenericPropertyNode[0];

        return _props.toArray(new GenericPropertyNode[0]);
    }

    @Override
    public String toString() {
        return "PLCF (cbStruct: " + _cbStruct + "; iMac: " + _iMac + ")";
    }
}
