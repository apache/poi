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

package org.apache.poi.hsmf.datatypes;

import java.util.List;
import java.util.Map;

/**
 * A group of chunks which is indexable by {@link MAPIProperty} entries.
 */
public interface ChunkGroupWithProperties extends ChunkGroup {
    /**
     * Returns all the Properties contained in the Chunk, along with their
     * Values. Normally, each property will have one value, sometimes none, and
     * rarely multiple (normally for Unknown etc). For fixed sized properties,
     * the value can be fetched straight from the {@link PropertyValue}. For
     * variable sized properties, you'll need to go via the chunk.
     */
    public Map<MAPIProperty, List<PropertyValue>> getProperties();
}
