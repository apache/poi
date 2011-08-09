package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;

@Internal
public enum FSPADocumentPart {
    HEADER( FIBFieldHandler.PLCSPAHDR ),

    MAIN( FIBFieldHandler.PLCSPAMOM );

    private final int fibFieldsField;

    private FSPADocumentPart( final int fibHandlerField )
    {
        this.fibFieldsField = fibHandlerField;
    }

    public int getFibFieldsField()
    {
        return fibFieldsField;
    }
}
