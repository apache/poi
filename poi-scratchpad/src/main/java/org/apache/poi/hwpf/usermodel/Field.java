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
package org.apache.poi.hwpf.usermodel;

public interface Field
{

    Range firstSubrange( Range parent );

    /**
     * @return character position of first character after field (i.e.
     *         {@link #getMarkEndOffset()} + 1)
     */
    int getFieldEndOffset();

    /**
     * @return character position of first character in field (i.e.
     *         {@link #getFieldStartOffset()})
     */
    int getFieldStartOffset();

    CharacterRun getMarkEndCharacterRun( Range parent );

    /**
     * @return character position of end field mark
     */
    int getMarkEndOffset();

    CharacterRun getMarkSeparatorCharacterRun( Range parent );

    /**
     * @return character position of separator field mark (if present,
     *         {@link NullPointerException} otherwise)
     */
    int getMarkSeparatorOffset();

    CharacterRun getMarkStartCharacterRun( Range parent );

    /**
     * @return character position of start field mark
     */
    int getMarkStartOffset();

    int getType();

    boolean hasSeparator();

    boolean isHasSep();

    boolean isLocked();

    boolean isNested();

    boolean isPrivateResult();

    boolean isResultDirty();

    boolean isResultEdited();

    boolean isZombieEmbed();

    Range secondSubrange( Range parent );
}