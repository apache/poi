package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;

/**
 * A drawing object in XSSF. May well have raw pictures
 *  attached to it as children.
 */
public class Drawing implements XSSFModel {
	private CTDrawing drawing;
	private String originalId;
	
	/** Raw pictures attached to the drawing */
	private ArrayList<XSSFPictureData> pictures;

	public Drawing(InputStream is, String originalId) throws IOException {
		readFrom(is);
		this.originalId = originalId;
		this.pictures = new ArrayList<XSSFPictureData>();
	}
	
	public String getOriginalId() {
		return this.originalId;
	}
	
	public Drawing() {
		this.drawing = CTDrawing.Factory.newInstance();
	}
	/**
	 * For unit testing only!
	 */
	protected Drawing(CTDrawing drawing) {
		this.drawing = drawing;
	}
	
	public void readFrom(InputStream is) throws IOException {
		try {
			CTDrawing doc = CTDrawing.Factory.parse(is);
			drawing = doc;
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
	}
	public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSaveOuter();
        options.setUseDefaultNamespace();        
        // Requests use of whitespace for easier reading
        //options.setSavePrettyPrint();
        drawing.save(out, options);
	}
	
	/**
	 * We expect image parts
	 */
	public String[] getChildrenRelationshipTypes() {
		return new String[] {
				XSSFRelation.IMAGES.getRelation()
		};
	}
	
	public int getNumberOfChildren() {
		return pictures.size();
	}
	
	/**
	 * Generates and adds XSSFActiveXData children
	 */
	public void generateChild(PackagePart childPart, String childRelId) {
		//XSSFPictureData pd = new XSSFPictureData(childPart, childRelId);
		//pictures.add(pd);
        throw new RuntimeException("deprecated");
    }

	public ArrayList<XSSFPictureData> getPictures()
	{
		return this.pictures;
	}
	
	public void addPictures(XSSFPictureData picture)
	{
		this.pictures.add(picture);
	}	
}