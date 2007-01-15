#  ====================================================================
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ====================================================================

require 'test/unit'
require 'release/poi4r'

class TC_base_tests < Test::Unit::TestCase

    def setup()
    end

	def test_get_constant
		h=Poi4r::HSSFWorkbook.new
		s=h.createSheet("Sheet1")
		r=s.createRow(0)
		c=r.createCell(0)
		assert_equal(3,Poi4r::HSSFCell.CELL_TYPE_BLANK,"Constant  CELL_TYPE_BLANK")
	end
	
	def test_base
	    	system("rm test.xls")
		h=Poi4r::HSSFWorkbook.new
		
		#Test Sheet Creation
		s=h.createSheet("Sheet1")
		s=h.createSheet("Sheet2")
		assert_equal(2,h.getNumberOfSheets(),"Number of sheets is 2")
		
		#Test setting cell values
		s=h.getSheetAt(0)
		r=s.createRow(0)
		c=r.createCell(0)
		c.setCellValue(1.5)
		assert_equal(c.getNumericCellValue(),1.5,"Numeric Cell Value")
		c=r.createCell(1)
		c.setCellValue("Ruby")
		assert_equal(c.getStringCellValue(),"Ruby","String Cell Value")
		#Test error handling
		assert_raise (RuntimeError) {c.getNumericCellValue()}
		
		#Test styles
		st = h.createCellStyle()
		c=r.createCell(2)
		st.setAlignment(Poi4r::HSSFCellStyle.ALIGN_CENTER)
		c.setCellStyle(st)
		c.setCellValue("centr'd")
		
		#Date handling
		c=r.createCell(3)
		t1=Time.now
		c.setCellValue(Time.now)
		t2= c.getDateCellValue().gmtime
		assert_equal(t1.year,t2.year,"year")
		assert_equal(t1.mon,t2.mon,"month")
		assert_equal(t1.day,t2.day,"day")
		assert_equal(t1.hour,t2.hour,"hour")
		assert_equal(t1.min,t2.min,"min")
		assert_equal(t1.sec,t2.sec,"sec")
		st=h.createCellStyle();
		st.setDataFormat(Poi4r::HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"))
		c.setCellStyle(st)

		#Fonts
		c=r.createCell(4)
		font = h.createFont();
		font.setFontHeightInPoints(24);
		font.setFontName("Courier New");
		font.setItalic(true);
		font.setStrikeout(true);
	        style = h.createCellStyle();
		style.setFont(font);
		c.setCellValue("This is a test of fonts");
		c.setCellStyle(style);

		#Formulas
		c=r.createCell(5)
		c.setCellFormula("A1*2")
		assert_equal("A1*2",c.getCellFormula,"formula")

		#Test writing
		h.write(File.new("test.xls","w"))
		assert_nothing_raised {File.new("test.xls","r")}
		#h.write(0.1)
	end
	
end
