/**
 * 
 */
package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.BinaryPart;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
import org.apache.poi.xssf.model.Drawing;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.XSSFChildContainingModel;
import org.apache.poi.xssf.model.XSSFModel;
import org.apache.poi.xssf.model.XSSFWritableModel;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.TargetMode;

public class XSSFRelation {
	public static final XSSFRelation WORKBOOK = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/workbook",
			"/xl/workbook.xml",
			null
	);
	public static final XSSFRelation MACROS_WORKBOOK = new XSSFRelation(
			"application/vnd.ms-excel.sheet.macroEnabled.main+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
			"/xl/workbook.xml",
			null
	);
	public static final XSSFRelation WORKSHEET = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet",
			"/xl/worksheets/sheet#.xml",
			null
	);
	public static final XSSFRelation SHARED_STRINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
			"/xl/sharedStrings.xml",
			SharedStringsTable.class
	);
	public static final XSSFRelation STYLES = new XSSFRelation(
		    "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml",
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles",
		    "/xl/styles.xml",
		    StylesTable.class
	);
	public static final XSSFRelation DRAWINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing",
			"/xl/drawings/drawing#.xml",
			null
	);
	public static final XSSFRelation VML_DRAWINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.vmlDrawing",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
			"/xl/drawings/vmlDrawing#.vml",
			Drawing.class
	);
    public static final XSSFRelation IMAGES = new XSSFRelation(
    		"image/x-emf", // TODO
     		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
    		"/xl/media/image#.emf",
    		null
    );
	public static final XSSFRelation SHEET_COMMENTS = new XSSFRelation(
		    "application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml",
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
		    "/xl/comments#.xml",
		    CommentsTable.class
	);
	public static final XSSFRelation SHEET_HYPERLINKS = new XSSFRelation(
		    null,
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
		    null,
		    null
	);
	public static final XSSFRelation OLEEMBEDDINGS = new XSSFRelation(
	        null,
	        POIXMLDocument.OLE_OBJECT_REL_TYPE,
	        null,
	        BinaryPart.class
	);
	public static final XSSFRelation PACKEMBEDDINGS = new XSSFRelation(
            null,
            POIXMLDocument.PACK_OBJECT_REL_TYPE,
            null,
            BinaryPart.class
    );

	public static final XSSFRelation VBA_MACROS = new XSSFRelation(
            "application/vnd.ms-office.vbaProject",
            "http://schemas.microsoft.com/office/2006/relationships/vbaProject",
            "/xl/vbaProject.bin",
	        BinaryPart.class
    );
	public static final XSSFRelation ACTIVEX_CONTROLS = new XSSFRelation(
			"application/vnd.ms-office.activeX+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/control",
			"/xl/activeX/activeX#.xml",
			Control.class
	);
	public static final XSSFRelation ACTIVEX_BINS = new XSSFRelation(
			"application/vnd.ms-office.activeX",
			"http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary",
			"/xl/activeX/activeX#.bin",
	        BinaryPart.class
	);
	
	
    private static POILogger log = POILogFactory.getLogger(XSSFRelation.class);
    
   
	private String TYPE;
	private String REL;
	private String DEFAULT_NAME;
	private Class<? extends XSSFModel> CLASS;
	
	protected XSSFRelation(String TYPE, String REL, String DEFAULT_NAME, Class<? extends XSSFModel> CLASS) {
		this.TYPE = TYPE;
		this.REL = REL;
		this.DEFAULT_NAME = DEFAULT_NAME;
		this.CLASS = CLASS;
	}
	public String getContentType() { return TYPE; }
	public String getRelation() { return REL; }
	public String getDefaultFileName() { return DEFAULT_NAME; }
	
	/**
	 * Does one of these exist for the given core
	 *  package part?
	 */
	public boolean exists(PackagePart corePart) throws IOException, InvalidFormatException {
		if(corePart == null) {
			// new file, can't exist
			return false;
		}
		
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(REL);
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
        	return true;
        } else {
        	return false;
        }
	}
	
	/**
	 * Returns the filename for the nth one of these, 
	 *  eg /xl/comments4.xml
	 */
	public String getFileName(int index) {
		if(DEFAULT_NAME.indexOf("#") == -1) {
			// Generic filename in all cases
			return getDefaultFileName();
		}
		return DEFAULT_NAME.replace("#", Integer.toString(index));
	}

	/**
	 * Fetches the InputStream to read the contents, based
	 *  of the specified core part, for which we are defined
	 *  as a suitable relationship
	 */
	public InputStream getContents(PackagePart corePart) throws IOException, InvalidFormatException {
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(REL);
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
            PackageRelationship rel = it.next();
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart part = corePart.getPackage().getPart(relName);
            return part.getInputStream();
        } else {
        	log.log(POILogger.WARN, "No part " + DEFAULT_NAME + " found");
        	return null;
        }
	}
	
	/**
	 * Loads all the XSSFModels of this type which are
	 *  defined as relationships of the given parent part
	 */
	public ArrayList<? extends XSSFModel> loadAll(PackagePart parentPart) throws Exception {
		ArrayList<XSSFModel> found = new ArrayList<XSSFModel>();
		for(PackageRelationship rel : parentPart.getRelationshipsByType(REL)) {
			PackagePart part = XSSFWorkbook.getTargetPart(parentPart.getPackage(), rel);
			found.add(create(part, rel));
		}
		return found;
	}
	
	/**
	 * Load a single Model, which is defined as a suitable
	 *  relationship from the specified core (parent) 
	 *  package part.
	 */
	public XSSFModel load(PackagePart corePart) throws Exception {
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(REL);
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
            PackageRelationship rel = it.next();
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart part = corePart.getPackage().getPart(relName);
            return create(part, rel);
        } else {
        	log.log(POILogger.WARN, "No part " + DEFAULT_NAME + " found");
        	return null;
        }
	}
	
	/**
	 * Does the actual Model creation
	 */
	private XSSFModel create(PackagePart thisPart, PackageRelationship rel) throws Exception {
		XSSFModel model = null;
		
		Constructor<? extends XSSFModel> c;
		boolean withString = false;
		
		// Find the right constructor 
		try {
			c = CLASS.getConstructor(InputStream.class, String.class);
			withString = true;
		} catch(NoSuchMethodException e) {
			c = CLASS.getConstructor(InputStream.class);
		}
		
		// Instantiate, if we can
		InputStream inp = thisPart.getInputStream();
		if(inp != null) {
            try {
            	if(withString) {
            		model = c.newInstance(inp, rel.getId());
            	} else {
            		model = c.newInstance(inp);
            	}
            } finally {
            	inp.close();
            }
            
			// Do children, if required
			if(model instanceof XSSFChildContainingModel) {
				XSSFChildContainingModel ccm = 
					(XSSFChildContainingModel)model;
				for(String relType : ccm.getChildrenRelationshipTypes()) {
					for(PackageRelationship cRel : thisPart.getRelationshipsByType(relType)) {
						PackagePart childPart = XSSFWorkbook.getTargetPart(thisPart.getPackage(), cRel);
						ccm.generateChild(childPart, cRel.getId());
					}
				}
			}
        }
		
		
        return model;
	}
	
	/**
	 * Save, with the default name
	 * @return The internal reference ID it was saved at, normally then used as an r:id
	 */
	protected String save(XSSFWritableModel model, PackagePart corePart) throws IOException {
		return save(model, corePart, DEFAULT_NAME);
	}
	/**
	 * Save, with the name generated by the given index
	 * @return The internal reference ID it was saved at, normally then used as an r:id
	 */
	protected String save(XSSFWritableModel model, PackagePart corePart, int index) throws IOException {
		return save(model, corePart, getFileName(index));
	}
	/**
	 * Save, with the specified name
	 * @return The internal reference ID it was saved at, normally then used as an r:id
	 */
	protected String save(XSSFWritableModel model, PackagePart corePart, String name) throws IOException {
        PackagePartName ppName = null;
        try {
        	ppName = PackagingURIHelper.createPartName(name);
        } catch(InvalidFormatException e) {
        	throw new IllegalStateException("Can't create part with name " + name + " for " + model, e);
        }
        PackageRelationship rel =
        	corePart.addRelationship(ppName, TargetMode.INTERNAL, REL);
        PackagePart part = corePart.getPackage().createPart(ppName, TYPE);
        
        OutputStream out = part.getOutputStream();
        model.writeTo(out);
        out.close();
        
		// Do children, if required
		if(model instanceof XSSFChildContainingModel) {
			XSSFChildContainingModel ccm = 
				(XSSFChildContainingModel)model;
			// Loop over each child, writing it out
			int numChildren = ccm.getNumberOfChildren();
			for(int i=0; i<numChildren; i++) {
				XSSFChildContainingModel.WritableChild child =
					ccm.getChildForWriting(i);
				child.getRelation().save(
						child.getModel(),
						part, 
						(i+1)
				);
			}
		}
        
        return rel.getId();
	}
}
