/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SimpleValue;

/**
 * A class to hold and compare a list of XmlObjects for use by keys
 * keyrefs during validation.
 */
public class XmlObjectList
{
    private final XmlObject[] _objects;

    /**
     * Construct a new empty object list of the given fixed size.
     */
    public XmlObjectList(int objectCount) {
        _objects = new XmlObject[objectCount];
    }

    /**
     * Set an object by index unless a value has been previously
     * set at that location.
     *
     * @return true if the value was set, false if the value has
     * already been set
     */
    public boolean set(XmlObject o, int index)
    {
        if (_objects[index] != null)
            return false;

        _objects[index] = o;
        return true;
    }

    /**
     * Tests that all values have been set. Needed for keys.
     */
    public boolean filled() {
        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] == null) return false;

        return true;
    }

    /**
     * Tests that all values have been set. Needed for keys.
     */
    public int unfilled()
    {
        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] == null) return i;

        return -1;
    }

    public boolean equals(Object o) {
        if (!( o instanceof XmlObjectList))
            return false;

        XmlObjectList other = (XmlObjectList)o;

        if (other._objects.length != this._objects.length)
            return false;

        for (int i = 0 ; i < _objects.length ; i++) {
            // Ignore missing values
            if (_objects[i] == null || other._objects[i] == null)
                return false;

            if (! _objects[i].valueEquals(other._objects[i]))
                return false;
        }

        return true;
    }

    public int hashCode()
    {
        int h = 0;

        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] != null)
                h = 31 * h + _objects[i].valueHashCode();

        return h;
    }
    
    private static String prettytrim(String s)
    {
        int end;
        for (end = s.length(); end > 0; end -= 1)
        {
            if (!XMLChar.isSpace(s.charAt(end - 1)))
                break;
        }
        int start;
        for (start = 0; start < end; start += 1)
        {
            if (!XMLChar.isSpace(s.charAt(start)))
                break;
        }
        return s.substring(start, end);
    }

    public String toString() {
        StringBuffer b = new StringBuffer();

        for (int i = 0 ; i < _objects.length ; i++)
        {
            if (i != 0) b.append(" ");
            b.append(prettytrim(((SimpleValue)_objects[i]).getStringValue()));
        }

        return b.toString();
    }
}
