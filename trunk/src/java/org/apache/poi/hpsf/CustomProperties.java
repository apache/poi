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

package org.apache.poi.hpsf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * Maintains the instances of {@link CustomProperty} that belong to a
 * {@link DocumentSummaryInformation}. The class maintains the names of the
 * custom properties in a dictionary. It implements the {@link Map} interface
 * and by this provides a simplified view on custom properties: A property's
 * name is the key that maps to a typed value. This implementation hides
 * property IDs from the developer and regards the property names as keys to
 * typed values.<p>
 *
 * While this class provides a simple API to custom properties, it ignores
 * the fact that not names, but IDs are the real keys to properties. Under the
 * hood this class maintains a 1:1 relationship between IDs and names. Therefore
 * you should not use this class to process property sets with several IDs
 * mapping to the same name or with properties without a name: the result will
 * contain only a subset of the original properties. If you really need to deal
 * such property sets, use HPSF's low-level access methods.<p>
 *
 * An application can call the {@link #isPure} method to check whether a
 * property set parsed by {@link CustomProperties} is still pure (i.e.
 * unmodified) or whether one or more properties have been dropped.<p>
 *
 * This class is not thread-safe; concurrent access to instances of this
 * class must be synchronized.<p>
 *
 * While this class is roughly HashMap&lt;Long,CustomProperty&gt;, that's the
 * internal representation. To external calls, it should appear as
 * HashMap&lt;String,Object&gt; mapping between Names and Custom Property Values.
 */
@SuppressWarnings("serial")
public class CustomProperties extends HashMap<Long,CustomProperty> {

    /**
     * Maps property IDs to property names and vice versa.
     */
    private final TreeBidiMap<Long,String> dictionary = new TreeBidiMap<Long,String>();

    /**
     * Tells whether this object is pure or not.
     */
    private boolean isPure = true;


    /**
     * Puts a {@link CustomProperty} into this map. It is assumed that the
     * {@link CustomProperty} already has a valid ID. Otherwise use
     * {@link #put(CustomProperty)}.
     * 
     * @param name the property name
     * @param cp the property
     * 
     * @return the previous property stored under this name
     */
    public CustomProperty put(final String name, final CustomProperty cp) {
        if (name == null) {
            /* Ignoring a property without a name. */
            isPure = false;
            return null;
        }
        
        if (!name.equals(cp.getName())) {
            throw new IllegalArgumentException("Parameter \"name\" (" + name +
                    ") and custom property's name (" + cp.getName() +
                    ") do not match.");
        }

        /* Register name and ID in the dictionary. Mapping in both directions is possible. If there is already a  */
        super.remove(dictionary.getKey(name));
        dictionary.put(cp.getID(), name);

        /* Put the custom property into this map. */
        return super.put(cp.getID(), cp);
    }



    /**
     * Puts a {@link CustomProperty} that has not yet a valid ID into this
     * map. The method will allocate a suitable ID for the custom property:
     *
     * <ul>
     * <li>If there is already a property with the same name, take the ID
     * of that property.
     *
     * <li>Otherwise find the highest ID and use its value plus one.
     * </ul>
     *
     * @param customProperty
     * @return If there was already a property with the same name, the old property
     * @throws ClassCastException
     */
    private Object put(final CustomProperty customProperty) throws ClassCastException {
        final String name = customProperty.getName();

        /* Check whether a property with this name is in the map already. */
        final Long oldId = (name == null) ? null :  dictionary.getKey(name);
        if (oldId != null) {
            customProperty.setID(oldId);
        } else {
            long lastKey = (dictionary.isEmpty()) ? 0 : dictionary.lastKey();
            customProperty.setID(Math.max(lastKey,PropertyIDMap.PID_MAX) + 1);
        }
        return this.put(name, customProperty);
    }



    /**
     * Removes a custom property.
     * @param name The name of the custom property to remove
     * @return The removed property or {@code null} if the specified property was not found.
     *
     * @see java.util.HashSet#remove(java.lang.Object)
     */
    public Object remove(final String name) {
        final Long id = dictionary.removeValue(name);
        return super.remove(id);
    }

    /**
     * Adds a named string property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final String value) {
        final Property p = new Property(-1, Variant.VT_LPWSTR, value);
        return put(new CustomProperty(p, name));
    }

    /**
     * Adds a named long property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final Long value) {
        final Property p = new Property(-1, Variant.VT_I8, value);
        return put(new CustomProperty(p, name));
    }

    /**
     * Adds a named double property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final Double value) {
        final Property p = new Property(-1, Variant.VT_R8, value);
        return put(new CustomProperty(p, name));
    }

    /**
     * Adds a named integer property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final Integer value) {
        final Property p = new Property(-1, Variant.VT_I4, value);
        return put(new CustomProperty(p, name));
    }

    /**
     * Adds a named boolean property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final Boolean value) {
        final Property p = new Property(-1, Variant.VT_BOOL, value);
        return put(new CustomProperty(p, name));
    }


    /**
     * Gets a named value from the custom properties.
     *
     * @param name the name of the value to get
     * @return the value or {@code null} if a value with the specified
     *         name is not found in the custom properties.
     */
    public Object get(final String name) {
        final Long id = dictionary.getKey(name);
        final CustomProperty cp = super.get(id);
        return cp != null ? cp.getValue() : null;
    }



    /**
     * Adds a named date property.
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    public Object put(final String name, final Date value) {
        final Property p = new Property(-1, Variant.VT_FILETIME, value);
        return put(new CustomProperty(p, name));
    }

    /**
     * Returns a set of all the names of our custom properties.
     * Equivalent to {@link #nameSet()}
     * 
     * @return a set of all the names of our custom properties
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set keySet() {
        return dictionary.values();
    }

    /**
     * Returns a set of all the names of our custom properties
     * 
     * @return a set of all the names of our custom properties
     */
    public Set<String> nameSet() {
        return dictionary.values();
    }

    /**
     * Returns a set of all the IDs of our custom properties
     * 
     * @return a set of all the IDs of our custom properties
     */
    public Set<String> idSet() {
        return dictionary.values();
    }


    /**
     * Sets the codepage.
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage) {
        Property p = new Property(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2, codepage);
        put(new CustomProperty(p));
    }



    /**
     * <p>Gets the dictionary which contains IDs and names of the named custom
     * properties.
     *
     * @return the dictionary.
     */
    Map<Long,String> getDictionary() {
        return dictionary;
    }


    /**
     * Checks against both String Name and Long ID
     */
    @Override
    public boolean containsKey(Object key) {
        return ((key instanceof Long && dictionary.containsKey(key)) || dictionary.containsValue(key));
    }

    /**
     * Checks against both the property, and its values. 
     */
    @Override
    public boolean containsValue(Object value) {
        if(value instanceof CustomProperty) {
            return super.containsValue(value);
        }
      
        for(CustomProperty cp : super.values()) {
            if(cp.getValue() == value) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the codepage.
     *
     * @return the codepage or -1 if the codepage is undefined.
     */
    public int getCodepage() {
        CustomProperty cp = get(PropertyIDMap.PID_CODEPAGE);
        return (cp == null) ? -1 : (Integer)cp.getValue();
    }

    /**
     * Tells whether this {@link CustomProperties} instance is pure or one or
     * more properties of the underlying low-level property set has been
     * dropped.
     *
     * @return {@code true} if the {@link CustomProperties} is pure, else
     *         {@code false}.
     */
    public boolean isPure() {
        return isPure;
    }

    /**
     * Sets the purity of the custom property set.
     *
     * @param isPure the purity
     */
    public void setPure(final boolean isPure) {
        this.isPure = isPure;
    }
}
