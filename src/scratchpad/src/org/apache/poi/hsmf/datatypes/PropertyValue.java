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

/**
 * An instance of a {@link MAPIProperty} inside a {@link PropertiesChunk}.
 * Where the {@link Types} type is a fixed length one, this will contain the
 *  actual value.
 * Where the {@link Types} type is a variable length one, this will contain
 *  the length of the property, and the value will be in the associated {@link Chunk}.
 */
public class PropertyValue {
   private MAPIProperty property;
   private long flags;
   private byte[] data;
}
