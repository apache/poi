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

package org.apache.poi.xwpf.usermodel;

import java.util.Arrays;
import java.util.List;

import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xwpf.XWPFTestDataSamples;


import junit.framework.TestCase;

public class TestXWPFPictureData extends TestCase {
	   public void testRead(){
		   XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("VariousPictures.docx");
	        List<XWPFPictureData> pictures = sampleDoc.getAllPictures();
	        assertSame(pictures, sampleDoc.getAllPictures());

	        assertEquals(5, pictures.size());
	        String[] ext = {"wmf", "png", "emf", "emf", "jpeg"};
	        for (int i = 0; i < pictures.size(); i++) {
	            assertEquals(ext[i], pictures.get(i).suggestFileExtension());
	        }

	        int num = pictures.size();

	        byte[] pictureData = {0xA, 0xB, 0XC, 0xD, 0xE, 0xF};

	        int idx;
			try {
				idx = sampleDoc.addPicture(pictureData, XWPFDocument.PICTURE_TYPE_JPEG);
				assertEquals(num + 1, pictures.size());
				//idx is 0-based index in the #pictures array
				assertEquals(pictures.size() - 1, idx);
				XWPFPictureData pict = pictures.get(idx);
				assertEquals("jpeg", pict.suggestFileExtension());
				assertTrue(Arrays.equals(pictureData, pict.getData()));
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    public void testNew(){
	        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("EmptyDocumentWithHeaderFooter.docx");
	        byte[] jpegData = "test jpeg data".getBytes();
	        byte[] wmfData =  "test wmf data".getBytes();
	        byte[] pngData =  "test png data".getBytes();
	        List<XWPFPictureData> pictures = doc.getAllPictures();
	        assertEquals(0, pictures.size());
	        int jpegIdx;
			try {
				jpegIdx = doc.addPicture(jpegData, XWPFDocument.PICTURE_TYPE_JPEG);
				assertEquals(1, pictures.size());
				assertEquals("jpeg", pictures.get(jpegIdx).suggestFileExtension());
				assertTrue(Arrays.equals(jpegData, pictures.get(jpegIdx).getData()));
				
				PackageRelationship addPictureReference = doc.addPictureReference(jpegData, Document.PICTURE_TYPE_JPEG );
				XWPFPictureData pictureDataByID = doc.getPictureDataByID(addPictureReference.getId());
				byte [] newJPEGData = pictureDataByID.getData();
				assertEquals(newJPEGData.length, jpegData.length);
				for(int i = 0; i < newJPEGData.length; i++){
					assertEquals(newJPEGData[i], jpegData[i]);	
				}
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
}
