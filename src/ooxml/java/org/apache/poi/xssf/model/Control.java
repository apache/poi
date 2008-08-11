package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFActiveXData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.PackagePart;
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
	 * We expect active x binary parts
	 */
	public String[] getChildrenRelationshipTypes() {
		return new String[] {
				XSSFRelation.ACTIVEX_BINS.getRelation()
		};
	}
	
	public int getNumberOfChildren() {
		return activexBins.size();
	}
	
	/**
	 * Generates and adds XSSFActiveXData children
	 */
	public void generateChild(PackagePart childPart, String childRelId) {
		XSSFActiveXData actX = new XSSFActiveXData(childPart, childRelId);
		activexBins.add(actX);
	}

	public WritableChild getChildForWriting(int index) {
		if(index >= activexBins.size()) {
			throw new IllegalArgumentException("Can't get child at " + index + " when size is " + getNumberOfChildren());
		}
		return new WritableChild(
				activexBins.get(index),
				XSSFRelation.ACTIVEX_BINS
		);
	}
	
	public ArrayList<XSSFActiveXData> getData()	{
		return this.activexBins;
	}
	
	public void addData(XSSFActiveXData activeX) {
		this.activexBins.add(activeX);
	}	
}