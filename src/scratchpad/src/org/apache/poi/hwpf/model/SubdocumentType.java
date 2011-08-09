package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;

/**
 * Document text parts that can have text pieces (CPs)
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
public enum SubdocumentType {
    MAIN( FIBLongHandler.CCPTEXT ),

    FOOTNOTE( FIBLongHandler.CCPFTN ),

    HEADER( FIBLongHandler.CCPHDD ),

    MACRO( FIBLongHandler.CCPMCR ),

    ANNOTATION( FIBLongHandler.CCPATN ),

    ENDNOTE( FIBLongHandler.CCPEDN ),

    TEXTBOX( FIBLongHandler.CCPTXBX ),

    HEADER_TEXTBOX( FIBLongHandler.CCPHDRTXBX );

    /**
     * Array of {@link SubdocumentType}s ordered by document position and FIB
     * field order
     */
    public static final SubdocumentType[] ORDERED = new SubdocumentType[] {
            MAIN, FOOTNOTE, HEADER, MACRO, ANNOTATION, ENDNOTE, TEXTBOX,
            HEADER_TEXTBOX };

    private final int fibLongFieldIndex;

    private SubdocumentType( int fibLongFieldIndex )
    {
        this.fibLongFieldIndex = fibLongFieldIndex;
    }

    public int getFibLongFieldIndex()
    {
        return fibLongFieldIndex;
    }

}
