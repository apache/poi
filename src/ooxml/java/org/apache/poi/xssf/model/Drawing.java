package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.TargetMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;

/**
 * A drawing object in XSSF. May well have raw pictures
 *  attached to it as children.
 */
public class Drawing implements XSSFChildContainingModel {
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
        options.setSavePrettyPrint();
        drawing.save(out, options);
	}
	
	/**
	 * Finds our XSSFPictureData children
	 */
	public void findChildren(PackagePart modelPart) throws IOException, InvalidFormatException {
		for(PackageRelationship rel : modelPart.getRelationshipsByType(XSSFWorkbook.IMAGES.getRelation())) {
			PackagePart imagePart = XSSFWorkbook.getTargetPart(modelPart.getPackage(), rel);
			XSSFPictureData pd = new XSSFPictureData(imagePart, rel.getId());
			pictures.add(pd);
		}
	}
	/**
	 * Writes back out our XSSFPictureData children
	 */
	public void writeChildren(PackagePart modelPart) throws IOException, InvalidFormatException {
		int pictureIndex = 1;
		OutputStream out;
		
		for(XSSFPictureData picture : pictures) {
			PackagePartName imagePartName = PackagingURIHelper.createPartName(XSSFWorkbook.IMAGES.getFileName(pictureIndex));
			modelPart.addRelationship(imagePartName, TargetMode.INTERNAL, XSSFWorkbook.IMAGES.getRelation(), getOriginalId());
			PackagePart imagePart = modelPart.getPackage().createPart(imagePartName, XSSFWorkbook.IMAGES.getContentType());                     
			out = imagePart.getOutputStream();
			picture.writeTo(out);
			out.close();
			pictureIndex++;
		}
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