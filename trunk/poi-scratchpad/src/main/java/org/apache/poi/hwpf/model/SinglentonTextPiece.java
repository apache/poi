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

import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;

@Internal
public class SinglentonTextPiece extends TextPiece {
    public SinglentonTextPiece(SinglentonTextPiece other) {
        super(other);
    }

    public SinglentonTextPiece( StringBuilder buffer ) {
        super( 0, buffer.length(), StringUtil.getToUnicodeLE(buffer.toString()), new PieceDescriptor( new byte[8], 0 ) );
    }

    @Override
    public int bytesLength()
    {
        return getStringBuilder().length() * 2;
    }

    @Override
    public int characterLength()
    {
        return getStringBuilder().length();
    }

    @Override
    public int getCP()
    {
        return 0;
    }

    @Override
    public int getEnd()
    {
        return characterLength();
    }

    @Override
    public int getStart()
    {
        return 0;
    }

    public String toString()
    {
        return "SinglentonTextPiece (" + characterLength() + " chars)";
    }

    @Override
    public SinglentonTextPiece copy() {
        return new SinglentonTextPiece(this);
    }
}
