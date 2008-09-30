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
package org.apache.poi;

/**
 * Represents a descriptor of a OOXML relation.
 *
 * @author Yegor Kozlov
 */
public class POIXMLRelation {

    protected String _type;
    protected String _relation;
    protected String _defaultName;

    /**
     * Instantiates a POIXMLRelation.
     */
    protected POIXMLRelation(String type, String rel, String defaultName) {
        _type = type;
        _relation = rel;
        _defaultName = defaultName;
    }

    public String getContentType() { return _type; }
    public String getRelation() { return _relation; }
    public String getDefaultFileName() { return _defaultName; }

    /**
     * Returns the filename for the nth one of these,
     *  eg /xl/comments4.xml
     */
    public String getFileName(int index) {
        if(_defaultName.indexOf("#") == -1) {
            // Generic filename in all cases
            return getDefaultFileName();
        }
        return _defaultName.replace("#", Integer.toString(index));
    }
}
