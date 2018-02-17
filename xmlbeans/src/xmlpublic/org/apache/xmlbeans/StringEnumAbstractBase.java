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

package org.apache.xmlbeans;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * The base class for code-generated string enumeration value classes.
 * <p>
 * Subclasses are intended to be final types with a finite set of
 * singleton instances.  Each instance has a string value, which
 * it returns via {@link #toString}, and an int value for the purpose
 * of switching in case statements, returned via {@link #intValue}.
 * <p>
 * Each subclass manages an instance of {@link StringEnumAbstractBase.Table},
 * which holds all the singleton instances for the subclass. A Table
 * can return a singleton instance given a String or an integer code.
 */ 
public class StringEnumAbstractBase implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String _string;
    private int _int;

    /**
     * Singleton instances should only be created by subclasses.
     */ 
    protected StringEnumAbstractBase(String s, int i)
        { _string = s; _int = i; }

    /** Returns the underlying string value */
    public final String toString()
        { return _string; }
    /** Returns an int code that can be used for switch statements */
    public final int intValue()
        { return _int; }
    /** Returns the hash code of the underlying string */
    public final int hashCode()
        { return _string.hashCode(); }

    /**
     * Used to manage singleton instances of enumerations.
     * Each subclass of StringEnumAbstractBase has an instance
     * of a table to hold the singleton instances.
     */ 
    public static final class Table
    {
        private Map _map;
        private List _list;
        public Table(StringEnumAbstractBase[] array)
        {
            _map = new HashMap(array.length);
            _list = new ArrayList(array.length + 1);
            for (int i = 0; i < array.length; i++)
            {
                _map.put(array[i].toString(), array[i]);
                int j = array[i].intValue();
                while (_list.size() <= j)
                    _list.add(null);
                _list.set(j, array[i]);
            }
        }
        
        /** Returns the singleton for a {@link String}, or null if none. */
        public StringEnumAbstractBase forString(String s)
        {
            return (StringEnumAbstractBase)_map.get(s);
        }
        /** Returns the singleton for an int code, or null if none. */
        public StringEnumAbstractBase forInt(int i)
        {
            if (i < 0 || i > _list.size())
                return null;
            return (StringEnumAbstractBase)_list.get(i);
        }
        /** Returns the last valid int code (the first is 1; zero is not used). */
        public int lastInt()
        {
            return _list.size() - 1;
        }
    }
}
