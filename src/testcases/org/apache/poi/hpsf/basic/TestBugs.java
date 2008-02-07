package org.apache.poi.hpsf.basic;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestBugs extends TestCase {
	private String dirname;

	protected void setUp() throws Exception {
		dirname = System.getProperty("HPSF.testdata.path");
	}
	
	public void BROKENtestBug44375() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				new FileInputStream(new File(dirname,"Bug44375.xls"))
		);
		
		DocumentInputStream dis;
		PropertySet set;

		dis = fs.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
		set = PropertySetFactory.create(dis);
		
		dis = fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME);
		// This currently fails
		set = PropertySetFactory.create(dis);
	}

}
