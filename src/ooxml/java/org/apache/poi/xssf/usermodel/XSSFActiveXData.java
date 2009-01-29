package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.model.XSSFWritableModel;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;

public class XSSFActiveXData implements PictureData, XSSFWritableModel {

    private PackagePart packagePart;
    private String originalId;

    public XSSFActiveXData(PackagePart packagePart, String originalId) {
        this(packagePart);
        this.originalId = originalId;
    }
    
    public XSSFActiveXData(PackagePart packagePart) {
        this.packagePart = packagePart;
    }

    public String getOriginalId() {
    	return originalId;
    }
    
    public PackagePart getPart() {
    	return packagePart;
    }
    
	public void writeTo(OutputStream out) throws IOException {
		IOUtils.copy(packagePart.getInputStream(), out);
	}

    public byte[] getData() {
    	// TODO - is this right?
    	// Are there headers etc?
    	try {
    		return IOUtils.toByteArray(packagePart.getInputStream());
    	} catch(IOException e) {
    		throw new POIXMLException(e);
    	}
    }

    public String suggestFileExtension() {
    	return packagePart.getPartName().getExtension();
    }
}
