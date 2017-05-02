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

package org.apache.poi.hpsf.wellknown;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.util.Internal;

/**
 * <p>Maps section format IDs to {@link PropertyIDMap}s. It is
 * initialized with two well-known section format IDs: those of the
 * <tt>\005SummaryInformation</tt> stream and the
 * <tt>\005DocumentSummaryInformation</tt> stream.</p>
 *
 * <p>If you have a section format ID you can use it as a key to query
 * this map.  If you get a {@link PropertyIDMap} returned your section
 * is well-known and you can query the {@link PropertyIDMap} for PID
 * strings. If you get back <code>null</code> you are on your own.</p>
 *
 * <p>This {@link java.util.Map} expects the byte arrays of section format IDs
 * as keys. A key maps to a {@link PropertyIDMap} describing the
 * property IDs in sections with the specified section format ID.</p>
 */
@Internal
public class SectionIDMap {

    /**
     * The default section ID map. It maps section format IDs to {@link PropertyIDMap PropertyIDMaps}
     */
    private static ThreadLocal<Map<ClassID,PropertyIDMap>> defaultMap =
        new ThreadLocal<Map<ClassID,PropertyIDMap>>();
    
    /**
     * <p>The SummaryInformation's section's format ID.</p>
     */
    public static final ClassID SUMMARY_INFORMATION_ID =
        new ClassID("{F29F85E0-4FF9-1068-AB91-08002B27B3D9}");

    /**
     * The DocumentSummaryInformation's first and second sections' format ID.
     */
    private static final ClassID DOC_SUMMARY_INFORMATION =
        new ClassID("{D5CDD502-2E9C-101B-9397-08002B2CF9AE}");    
    private static final ClassID USER_DEFINED_PROPERTIES =
        new ClassID("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}");
    
    public static final ClassID[] DOCUMENT_SUMMARY_INFORMATION_ID = {
        DOC_SUMMARY_INFORMATION, USER_DEFINED_PROPERTIES
    };

    /**
     * A property without a known name is described by this string.
     */
    public static final String UNDEFINED = "[undefined]";

    /**
     * <p>Returns the singleton instance of the default {@link
     * SectionIDMap}.</p>
     *
     * @return The instance value
     */
    public static SectionIDMap getInstance() {
        Map<ClassID,PropertyIDMap> m = defaultMap.get();
        if (m == null) {
            m = new HashMap<ClassID,PropertyIDMap>();
            m.put(SUMMARY_INFORMATION_ID, PropertyIDMap.getSummaryInformationProperties());
            m.put(DOCUMENT_SUMMARY_INFORMATION_ID[0], PropertyIDMap.getDocumentSummaryInformationProperties());
            defaultMap.set(m);
        }
        return new SectionIDMap();
    }



    /**
     * <p>Returns the property ID string that is associated with a
     * given property ID in a section format ID's namespace.</p>
     *
     * @param sectionFormatID Each section format ID has its own name
     * space of property ID strings and thus must be specified.
     * @param  pid The property ID
     * @return The well-known property ID string associated with the
     * property ID <var>pid</var> in the name space spanned by <var>
     * sectionFormatID</var> . If the <var>pid</var>
     * /<var>sectionFormatID </var> combination is not well-known, the
     * string "[undefined]" is returned.
     */
    public static String getPIDString(ClassID sectionFormatID, long pid) {
        final PropertyIDMap m = getInstance().get(sectionFormatID);
        if (m == null) {
            return UNDEFINED;
        }
        final String s = (String) m.get(pid);
        if (s == null) {
            return UNDEFINED;
        }
        return s;
    }



    /**
     * <p>Returns the {@link PropertyIDMap} for a given section format
     * ID.</p>
     *
     * @param sectionFormatID the section format ID
     * @return the property ID map
     */
    public PropertyIDMap get(final ClassID sectionFormatID) {
        return getInstance().get(sectionFormatID);
    }

    /**
     * Associates a section format ID with a {@link PropertyIDMap}.
     *
     * @param sectionFormatID the section format ID
     * @param propertyIDMap the property ID map
     * @return as defined by {@link java.util.Map#put}
     */
    public PropertyIDMap put(ClassID sectionFormatID, PropertyIDMap propertyIDMap) {
        return getInstance().put(sectionFormatID, propertyIDMap);
    }

    /**
     * Associates the string representation of a section format ID with a {@link PropertyIDMap}
     * 
     * @param key the key of the PropertyIDMap
     * @param value the PropertyIDMap itself
     * 
     * @return the previous PropertyIDMap stored under this key, or {@code null} if there wasn't one
     */
    protected PropertyIDMap put(String key, PropertyIDMap value) {
        return put(new ClassID(key), value);
    }
}
