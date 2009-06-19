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

package org.apache.poi.hwpf.model;

public interface CharIndexTranslator {

    /**
     * Calculates the char index of the given byte index.
     *
     * @param bytePos The character offset to check 
     * @return the char index
     */
    int getCharIndex(int bytePos);

    /**
     * Is the text at the given byte offset unicode, or plain old ascii? In a
     * very evil fashion, you have to actually know this to make sense of
     * character and paragraph properties :(
     *
     * @param bytePos The character offset to check about
     * @return true if the text at the given byte offset is unicode
     */
    boolean isUnicodeAtByteOffset(int bytePos);

}
