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

import org.apache.poi.hwpf.model.types.FFDataBaseAbstractType;
import org.apache.poi.util.Internal;

/**
 * The FFData structure specifies form field data for a text box, check box, or
 * drop-down list box.
 * <p>
 * Class and fields descriptions are quoted from [MS-DOC] -- v20121003 Word
 * (.doc) Binary File Format; Copyright (c) 2012 Microsoft Corporation; Release:
 * October 8, 2012
 * <p>
 * This class is internal. It content or properties may change without notice
 * due to changes in our knowledge of internal Microsoft Word binary structures.
 */
@Internal
public class FFDataBase extends FFDataBaseAbstractType
{
    public FFDataBase()
    {
        super();
    }

    public FFDataBase( byte[] std, int offset )
    {
        fillFields( std, offset );
    }
}
