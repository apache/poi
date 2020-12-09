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

import java.util.Objects;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.hwpf.model.types.BKFAbstractType;
import org.apache.poi.util.Internal;

@Internal
public final class BookmarkFirstDescriptor extends BKFAbstractType implements Duplicatable {
    public BookmarkFirstDescriptor() { }

    public BookmarkFirstDescriptor(BookmarkFirstDescriptor other) {
        super(other);
    }

    public BookmarkFirstDescriptor( byte[] data, int offset ) {
        fillFields( data, offset );
    }

    @Override
    public BookmarkFirstDescriptor copy() {
        return new BookmarkFirstDescriptor(this);
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        BookmarkFirstDescriptor other = (BookmarkFirstDescriptor) obj;
        if ( field_1_ibkl != other.field_1_ibkl )
            return false;
        if ( field_2_bkf_flags != other.field_2_bkf_flags )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_ibkl,field_2_bkf_flags);
    }

    public boolean isEmpty()
    {
        return field_1_ibkl == 0 && field_2_bkf_flags == 0;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[BKF] EMPTY";

        return super.toString();
    }
}
