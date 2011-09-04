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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.util.HashMap;
import java.util.Map;

@Beta
public class XSLFRelation extends POIXMLRelation {

   private static POILogger log = POILogFactory.getLogger(XSLFRelation.class);

   /**
    * A map to lookup POIXMLRelation by its relation type
    */
   protected static Map<String, XSLFRelation> _table = new HashMap<String, XSLFRelation>();
   
   public static final XSLFRelation MAIN = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation MACRO = new XSLFRelation(
           "application/vnd.ms-powerpoint.slideshow.macroEnabled.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation MACRO_TEMPLATE = new XSLFRelation(
           "application/vnd.ms-powerpoint.template.macroEnabled.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation PRESENTATIONML = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.presentationml.slideshow.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation PRESENTATIONML_TEMPLATE = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.presentationml.template.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation PRESENTATION_MACRO = new XSLFRelation(
           "application/vnd.ms-powerpoint.presentation.macroEnabled.main+xml",
           null, null, null
   );
   
   public static final XSLFRelation THEME_MANAGER = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.themeManager+xml",
           null, null, null
   );
   
   public static final XSLFRelation NOTES = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
           "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide", 
           "/ppt/notesSlides/notesSlide#.xml",
           XSLFNotes.class
   );
   
   public static final XSLFRelation SLIDE = new XSLFRelation(
           "application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
           "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide", 
           "/ppt/slides/slide#.xml", 
           XSLFSlide.class
   );
   
   public static final XSLFRelation SLIDE_LAYOUT = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout",
         "/ppt/slideLayouts/slideLayout#.xml", 
         XSLFSlideLayout.class
   );
   
   public static final XSLFRelation SLIDE_MASTER = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster",
         "/ppt/slideMasters/slideMaster#.xml",
         XSLFSlideMaster.class
   );

   public static final XSLFRelation NOTES_MASTER = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster",
         "/ppt/notesMasters/notesMaster#.xml",
         XSLFNotesMaster.class
   );

   public static final XSLFRelation COMMENTS = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.presentationml.comments+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
         "/ppt/comments/comment#.xml",
         XSLFComments.class
   );
   
   public static final XSLFRelation COMMENT_AUTHORS = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.presentationml.commentAuthors+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/commentAuthors",
         "/ppt/commentAuthors.xml",
         XSLFCommentAuthors.class
   );
   
    public static final XSLFRelation HYPERLINK = new XSLFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
            null,
            null
    );

   public static final XSLFRelation THEME = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.theme+xml",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
         "/ppt/theme/theme#.xml", 
         XSLFTheme.class
   );
   
   public static final XSLFRelation VML_DRAWING = new XSLFRelation(
         "application/vnd.openxmlformats-officedocument.vmlDrawing",
         "http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
         "/ppt/drawings/vmlDrawing#.vml", 
         null
   );
   
    public static final XSLFRelation IMAGE_EMF = new XSLFRelation(
          "image/x-emf",
          "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
          "/ppt/media/image#.emf",
          XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_WMF = new XSLFRelation(
	      "image/x-wmf",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.wmf",
	      XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_PICT = new XSLFRelation(
	      "image/pict",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.pict",
	      XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_JPEG = new XSLFRelation(
	      "image/jpeg",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.jpeg",
	      XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_PNG = new XSLFRelation(
	      "image/png",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.png",
	      XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_DIB = new XSLFRelation(
	      "image/dib",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.dib",
	      XSLFPictureData.class
	);
	public static final XSLFRelation IMAGE_GIF = new XSLFRelation(
	      "image/gif",
	      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
	      "/ppt/media/image#.gif",
	      XSLFPictureData.class
	);

    public static final XSLFRelation IMAGES = new XSLFRelation(
            null,
     		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
    		null,
    		null
    );

   private XSLFRelation(String type, String rel, String defaultName, Class<? extends POIXMLDocumentPart> cls) {
      super(type, rel, defaultName, cls);

      if(cls != null && !_table.containsKey(rel)) _table.put(rel, this);
   }

   /**
    * Get POIXMLRelation by relation type
    *
    * @param rel relation type, for example,
    *    <code>http://schemas.openxmlformats.org/officeDocument/2006/relationships/image</code>
    * @return registered POIXMLRelation or null if not found
    */
   public static XSLFRelation getInstance(String rel){
       return _table.get(rel);
   }
}
