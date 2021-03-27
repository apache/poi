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

import java.util.Objects;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.hwpf.model.types.TLPAbstractType;

public class TableAutoformatLookSpecifier extends TLPAbstractType implements Duplicatable {
    public static final int SIZE = 4;

    public TableAutoformatLookSpecifier() {}

    public TableAutoformatLookSpecifier(TableAutoformatLookSpecifier other) {
        super(other);
    }

    public TableAutoformatLookSpecifier( byte[] data, int offset ) {
        fillFields( data, offset );
    }

    @Override
    public TableAutoformatLookSpecifier copy() {
        return new TableAutoformatLookSpecifier(this);
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
        TableAutoformatLookSpecifier other = (TableAutoformatLookSpecifier) obj;
        if ( field_1_itl != other.field_1_itl )
            return false;
        if ( field_2_tlp_flags != other.field_2_tlp_flags )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_itl,field_2_tlp_flags);
    }

    public boolean isEmpty()
    {
        return field_1_itl == 0 && field_2_tlp_flags == 0;
    }
}
