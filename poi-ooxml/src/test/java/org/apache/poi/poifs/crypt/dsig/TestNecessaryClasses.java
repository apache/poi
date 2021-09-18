package org.apache.poi.poifs.crypt.dsig;

import org.etsi.uri.x01903.v13.DataObjectFormatType;
import org.etsi.uri.x01903.v13.ObjectIdentifierType;
import org.etsi.uri.x01903.v13.SignaturePolicyIdType;
import org.etsi.uri.x01903.v14.ValidationDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// aim is to get these classes loaded and included in poi-ooxml-lite.jar
public class TestNecessaryClasses {

    @Test
    void testProblemClasses() {
        DataObjectFormatType dataObjectFormatType = DataObjectFormatType.Factory.newInstance();
        assertNotNull(dataObjectFormatType);
        ObjectIdentifierType objectIdentifierType = ObjectIdentifierType.Factory.newInstance();
        assertNotNull(objectIdentifierType);
        SignaturePolicyIdType signaturePolicyIdType = SignaturePolicyIdType.Factory.newInstance();
        assertNotNull(signaturePolicyIdType);
        ValidationDataType validationDataType = ValidationDataType.Factory.newInstance();
        assertNotNull(validationDataType);
    }

}
