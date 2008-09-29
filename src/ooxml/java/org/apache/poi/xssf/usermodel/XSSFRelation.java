/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.BinaryPart;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
import org.apache.poi.xssf.model.Drawing;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemeTable;
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

/**
 * 
 */
public final class XSSFRelation<W extends XSSFModel> {

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
	public static final XSSFRelation<SharedStringsTable> SHARED_STRINGS = create(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
			"/xl/sharedStrings.xml",
			SharedStringsTable.class
	);
	public static final XSSFRelation<StylesTable> STYLES = create(
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
	public static final XSSFRelation<Drawing> VML_DRAWINGS = create(
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
	public static final XSSFRelation<CommentsTable> SHEET_COMMENTS = create(
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
	public static final XSSFRelation<BinaryPart> OLEEMBEDDINGS = create(
	        null,
	        POIXMLDocument.OLE_OBJECT_REL_TYPE,
	        null,
	        BinaryPart.class
	);
	public static final XSSFRelation<BinaryPart> PACKEMBEDDINGS = create(
            null,
            POIXMLDocument.PACK_OBJECT_REL_TYPE,
            null,
            BinaryPart.class
    );

	public static final XSSFRelation<BinaryPart> VBA_MACROS = create(
            "application/vnd.ms-office.vbaProject",
            "http://schemas.microsoft.com/office/2006/relationships/vbaProject",
            "/xl/vbaProject.bin",
	        BinaryPart.class
    );
	public static final XSSFRelation<Control> ACTIVEX_CONTROLS = create(
			"application/vnd.ms-office.activeX+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/control",
			"/xl/activeX/activeX#.xml",
			Control.class
	);
	public static final XSSFRelation<BinaryPart> ACTIVEX_BINS = create(
			"application/vnd.ms-office.activeX",
			"http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary",
			"/xl/activeX/activeX#.bin",
	        BinaryPart.class
	);
    public static final XSSFRelation<ThemeTable> THEME = create(
            "application/vnd.openxmlformats-officedocument.theme+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
            "/xl/theme/theme#.xml",
            ThemeTable.class
    );
	
	
    private static POILogger log = POILogFactory.getLogger(XSSFRelation.class);
    
    private static <R extends XSSFModel> XSSFRelation<R> create(String type, String rel, String defaultName, Class<R> cls) {
    	return new XSSFRelation<R>(type, rel, defaultName, cls);
    }
   
	private String _type;
	private String _relation;
	private String _defaultName;
	private Constructor<W> _constructor;
	private final boolean _constructorTakesTwoArgs;
	
	private XSSFRelation(String type, String rel, String defaultName, Class<W> cls) {
		_type = type;
		_relation = rel;
		_defaultName = defaultName;
		if (cls == null) {
			_constructor = null;
			_constructorTakesTwoArgs = false;
		} else {
			Constructor<W> c;
			boolean twoArg;
			
			// Find the right constructor 
			try {
				c = cls.getConstructor(InputStream.class, String.class);
				twoArg = true;
			} catch(NoSuchMethodException e) {
				try {
					c = cls.getConstructor(InputStream.class);
					twoArg = false;
				} catch(NoSuchMethodException e2) {
					throw new RuntimeException(e2);
				}
			}
			_constructor = c;
			_constructorTakesTwoArgs = twoArg;
		}
	}
	public String getContentType() { return _type; }
	public String getRelation() { return _relation; }
	public String getDefaultFileName() { return _defaultName; }
	
	/**
	 * Does one of these exist for the given core
	 *  package part?
	 */
	public boolean exists(PackagePart corePart) throws InvalidFormatException {
		if(corePart == null) {
			// new file, can't exist
			return false;
		}
		
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(_relation);
        Iterator<PackageRelationship> it = prc.iterator();
        return it.hasNext();
	}
	
	/**
	 * Returns the filename for the nth one of these, 
	 *  eg /xl/comments4.xml
	 */
	public String getFileName(int index) {
		if(_defaultName.indexOf("#") == -1) {
			// Generic filename in all cases
			return getDefaultFileName();
		}
		return _defaultName.replace("#", Integer.toString(index));
	}

	/**
	 * Fetches the InputStream to read the contents, based
	 *  of the specified core part, for which we are defined
	 *  as a suitable relationship
	 */
	public InputStream getContents(PackagePart corePart) throws IOException, InvalidFormatException {
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(_relation);
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
            PackageRelationship rel = it.next();
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart part = corePart.getPackage().getPart(relName);
            return part.getInputStream();
        } else {
        	log.log(POILogger.WARN, "No part " + _defaultName + " found");
        	return null;
        }
	}
	
	/**
	 * Loads all the XSSFModels of this type which are
	 *  defined as relationships of the given parent part
	 */
	public List<W> loadAll(PackagePart parentPart) throws Exception {
		List<W> found = new ArrayList<W>();
		for(PackageRelationship rel : parentPart.getRelationshipsByType(_relation)) {
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
	public W load(PackagePart corePart) throws Exception {
        PackageRelationshipCollection prc =
        	corePart.getRelationshipsByType(_relation);
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
            PackageRelationship rel = it.next();
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart part = corePart.getPackage().getPart(relName);
            return create(part, rel);
        } else {
        	log.log(POILogger.WARN, "No part " + _defaultName + " found");
        	return null;
        }
	}
	
	/**
	 * Does the actual Model creation
	 */
	private W create(PackagePart thisPart, PackageRelationship rel) 
			throws IOException, InvalidFormatException {
		
		if (_constructor == null) {
			throw new IllegalStateException("Model class not set");
		}
		// Instantiate, if we can
		InputStream inp = thisPart.getInputStream();
		if (inp == null) {
			return null; // TODO - is this valid?
		}
		Object[] args;
		if (_constructorTakesTwoArgs) {
			args = new Object[] { inp, rel.getId(), };
		} else {
			args = new Object[] { inp, };
		}
		W result;
        try {
        	try {
				result = _constructor.newInstance(args);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				if (t instanceof IOException) {
					throw (IOException)t;
				}
				if (t instanceof RuntimeException) {
					throw (RuntimeException)t;
				}
				throw new RuntimeException(t);
			}
        } finally {
        	inp.close();
        }
        
		// Do children, if required
		if(result instanceof XSSFChildContainingModel) {
			XSSFChildContainingModel ccm = 
				(XSSFChildContainingModel)result;
			for(String relType : ccm.getChildrenRelationshipTypes()) {
				for(PackageRelationship cRel : thisPart.getRelationshipsByType(relType)) {
					PackagePart childPart = XSSFWorkbook.getTargetPart(thisPart.getPackage(), cRel);
					ccm.generateChild(childPart, cRel.getId());
				}
			}
		}
 		
		
        return result;
	}
	
	/**
	 * Save, with the default name
	 * @return The internal reference ID it was saved at, normally then used as an r:id
	 */
	protected String save(XSSFWritableModel model, PackagePart corePart) throws IOException {
		return save(model, corePart, _defaultName);
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
        	corePart.addRelationship(ppName, TargetMode.INTERNAL, _relation);
        PackagePart part = corePart.getPackage().createPart(ppName, _type);
        
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
