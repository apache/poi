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

package org.apache.poi.ss.usermodel;

/**
 * Allows the getting and saving of shared strings
 */
public interface SharedStringSource {
    
    /**
     * Return the string at position <code>idx</idx> (0-based) in this source.
     * 
     * @param idx String position.
     * @return The string, or null if not found.
     */
    public String getSharedStringAt(int idx);
    
    /**
     * Store a string in this source.
     * 
     * @param s The string to store.
     * @return The 0-based position of the newly added string.
     */
    public int putSharedString(String s);
}
