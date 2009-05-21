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

package org.apache.poi.hdf.model.hdftypes;


/**
 * common data structure in a Word file. Contains an array of 4 byte ints in
 * the front that relate to an array of abitrary data structures in the back.
 *
 * This class acts more like a pointer. In the sense that it doesn't store any
 * data. It only provides convenience methods for accessing a particular
 * PlexOfCps
 *
 * @author Ryan Ackley
 */
public final class PlexOfCps
{
    private int _count;
    private int _offset;
    private int _sizeOfStruct;


    /**
     * Constructor
     *
     * @param size The size in bytes of this PlexOfCps
     * @param sizeOfStruct The size of the data structure type stored in
     *        this PlexOfCps.
     */
    public PlexOfCps(int size, int sizeOfStruct)
    {
        _count = (size - 4)/(4 + sizeOfStruct);
        _sizeOfStruct = sizeOfStruct;
    }
    public int getIntOffset(int index)
    {
      return index * 4;
    }
    /**
     * returns the number of data structures in this PlexOfCps.
     *
     * @return The number of data structures in this PlexOfCps
     */
    public int length()
    {
        return _count;
    }
    /**
     * Returns the offset, in bytes, from the beginning if this PlexOfCps to
     * the data structure at index.
     *
     * @param index The index of the data structure.
     *
     * @return The offset, in bytes, from the beginning if this PlexOfCps to
     *         the data structure at index.
     */
    public int getStructOffset(int index)
    {
        return (4 * (_count + 1)) + (_sizeOfStruct * index);
    }
}
