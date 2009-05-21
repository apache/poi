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

package org.apache.poi.hdf.extractor;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class PropertySet
{
    private String _name;
    private int _type;
    private int _previous;
    private int _next;
    private int _dir;
    private int _sb;
    private int _size;
    private int _num;

    public PropertySet(String name, int type, int previous, int next, int dir,
                       int sb, int size, int num)
    {
        _name = name;
        _type = type;
        _previous = previous;
        _next = next;
        _dir = dir;
        _sb = sb;
        _size = size;
        _num = num;
    }
    public int getSize()
    {
        return _size;
    }
    public int getStartBlock()
    {
        return _sb;
    }
}
