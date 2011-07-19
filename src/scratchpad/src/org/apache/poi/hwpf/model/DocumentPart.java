package org.apache.poi.hwpf.model;

public enum DocumentPart {

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

    private final int fibHandlerFieldsField;

    private DocumentPart( final int fibHandlerField )
    {
        this.fibHandlerFieldsField = fibHandlerField;
    }

    public int getFibHandlerFieldsPosition()
    {
        return fibHandlerFieldsField;
    }

}
