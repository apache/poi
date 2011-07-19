package org.apache.poi.hwpf.model;

public enum FieldsDocumentPart {

    /**
     * annotation subdocument
     */
    ANNOTATIONS( FIBFieldHandler.PLCFFLDATN ),

    /**
     * endnote subdocument
     */
    ENDNOTES( FIBFieldHandler.PLCFFLDEDN ),

    /**
     * footnote subdocument
     */
    FOOTNOTES( FIBFieldHandler.PLCFFLDFTN ),

    /**
     * header subdocument
     */
    HEADER( FIBFieldHandler.PLCFFLDHDR ),

    /**
     * header textbox subdoc
     */
    HEADER_TEXTBOX( FIBFieldHandler.PLCFFLDHDRTXBX ),

    /**
     * main document
     */
    MAIN( FIBFieldHandler.PLCFFLDMOM ),

    /**
     * textbox subdoc
     */
    TEXTBOX( FIBFieldHandler.PLCFFLDTXBX );

    private final int fibFieldsField;

    private FieldsDocumentPart( final int fibHandlerField )
    {
        this.fibFieldsField = fibHandlerField;
    }

    public int getFibFieldsField()
    {
        return fibFieldsField;
    }

}
