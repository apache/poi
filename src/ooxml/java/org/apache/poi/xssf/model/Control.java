package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFActiveXData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.TargetMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTControl;

/**
 * A control object in XSSF, which will typically
 *  have active x data associated with it.
 */
public class Control implements XSSFChildContainingModel {
	private CTControl control;
	private String originalId;
	private ArrayList<XSSFActiveXData> activexBins;

	public Control(InputStream is, String originalId) throws IOException {
		readFrom(is);
		this.originalId = originalId;
		this.activexBins = new ArrayList<XSSFActiveXData>();
	}
	
	public String getOriginalId() {
		return this.originalId;
	}
	
	public Control() {
		this.control = CTControl.Factory.newInstance();
	}
	/**
	 * For unit testing only!
	 */
	protected Control(CTControl control) {
		this.control = control;
	}
	
	public void readFrom(InputStream is) throws IOException {
		try {
			CTControl doc = CTControl.Factory.parse(is);
			control = doc;
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
	}
	public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSaveOuter();
        options.setUseDefaultNamespace();        
        // Requests use of whitespace for easier reading
        options.setSavePrettyPrint();
        control.save(out, options);
	}
	
	/**
	 * Finds our XSSFActiveXData children
	 */
	public void findChildren(PackagePart modelPart) throws IOException, InvalidFormatException {
		for(PackageRelationship rel : modelPart.getRelationshipsByType(XSSFWorkbook.ACTIVEX_BINS.getRelation())) {
			PackagePart binPart = XSSFWorkbook.getTargetPart(modelPart.getPackage(), rel);
			XSSFActiveXData actX = new XSSFActiveXData(binPart, rel.getId());
			activexBins.add(actX);
		}
	}
	/**
	 * Writes back out our XSSFPictureData children
	 */
	public void writeChildren(PackagePart modelPart) throws IOException, InvalidFormatException {
		int binIndex = 1;
		OutputStream out;
		
		for(XSSFActiveXData actX : activexBins) {
			PackagePartName binPartName = PackagingURIHelper.createPartName(XSSFWorkbook.ACTIVEX_BINS.getFileName(binIndex));
			modelPart.addRelationship(binPartName, TargetMode.INTERNAL, XSSFWorkbook.ACTIVEX_BINS.getRelation(), getOriginalId());
			PackagePart imagePart = modelPart.getPackage().createPart(binPartName, XSSFWorkbook.ACTIVEX_BINS.getContentType());                     
			out = imagePart.getOutputStream();
			actX.writeTo(out);
			out.close();
			binIndex++;
		}
	}

	public ArrayList<XSSFActiveXData> getData()	{
		return this.activexBins;
	}
	
	public void addData(XSSFActiveXData activeX) {
		this.activexBins.add(activeX);
	}	
}