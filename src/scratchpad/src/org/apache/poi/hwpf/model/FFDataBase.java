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
 * 
 * @author Sergey Vladimirov; according to [MS-DOC] -- v20121003 Word (.doc)
 *         Binary File Format; Copyright (c) 2012 Microsoft Corporation;
 *         Release: October 8, 2012
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
