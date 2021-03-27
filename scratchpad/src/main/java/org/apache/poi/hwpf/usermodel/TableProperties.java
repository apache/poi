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

import org.apache.poi.common.Duplicatable;
import org.apache.poi.hwpf.model.types.TAPAbstractType;

public final class TableProperties extends TAPAbstractType implements Duplicatable {

    public TableProperties() {
        setTlp(new TableAutoformatLookSpecifier());
        setShdTable(new ShadingDescriptor());
        setBrcBottom(new BorderCode());
        setBrcHorizontal(new BorderCode());
        setBrcLeft(new BorderCode());
        setBrcRight(new BorderCode());
        setBrcTop(new BorderCode());
        setBrcVertical(new BorderCode());
        setRgbrcInsideDefault_0(new BorderCode());
        setRgbrcInsideDefault_1(new BorderCode());
        setRgdxaCenter(new short[0]);
        setRgdxaCenterPrint(new short[0]);
        setRgshd(new ShadingDescriptor[0]);
        setRgtc(new TableCellDescriptor[0]);
    }

    public TableProperties(TableProperties other) {
        super(other);
    }

    public TableProperties( short columns ) {
        setItcMac( columns );
        setRgshd( new ShadingDescriptor[columns] );

        for ( int x = 0; x < columns; x++ )
        {
            getRgshd()[x] = new ShadingDescriptor();
        }

        TableCellDescriptor[] tableCellDescriptors = new TableCellDescriptor[columns];
        for ( int x = 0; x < columns; x++ )
        {
            tableCellDescriptors[x] = new TableCellDescriptor();
        }
        setRgtc( tableCellDescriptors );

        setRgdxaCenter( new short[columns] );
        setRgdxaCenterPrint( new short[columns] );
    }

    @Override
    public TableProperties copy() {
        return new TableProperties(this);
    }
}
