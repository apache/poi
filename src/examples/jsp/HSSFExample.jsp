<!--
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
-->
<%@page contentType="text/html" 
import="java.io.*,org.apache.poi.poifs.filesystem.POIFSFileSystem,org.apache.poi
.hssf.record.*,org.apache.poi.hssf.model.*,org.apache.poi.hssf.usermodel.*,org.a
pache.poi.hssf.util.*" %>
<html>
<head><title>Read Excel file </title>
</head>
<body>
An example of using Jakarta POI's HSSF package to read an excel spreadsheet: 


<form name="form1" method="get" action="">
Select an Excel file to read. 
  <input type="file" name="xls_filename" onChange="form1.submit()">
</form>

<%
	String filename = request.getParameter("xls_filename"); 
	if (filename != null && !filename.equals("")) {
%>	
	<br>You chose the file <%= filename %>. 
	<br><br>It's contents are: 	
<%	
            try
            {

                // create a poi workbook from the excel spreadsheet file
                POIFSFileSystem fs =
                    new POIFSFileSystem(new FileInputStream(filename));
                HSSFWorkbook wb = new HSSFWorkbook(fs);

                for (int k = 0; k < wb.getNumberOfSheets(); k++)
                {
%>				
                    <br><br>Sheet  <%= k %> <br>
<%					
					
                    HSSFSheet sheet = wb.getSheetAt(k);
                    int       rows  = sheet.getPhysicalNumberOfRows();

                    for (int r = 0; r < rows; r++)
                    {
                        HSSFRow row   = sheet.getRow(r);
                        if (row != null) { 
                            int     cells = row.getPhysicalNumberOfCells();
%>
							<br><b>ROW  <%= 
row.getRowNum() %> </b>
<%
                            for (short c = 0; c < cells; c++) 
                            { 
                                HSSFCell cell  = row.getCell(c);
                                if (cell != null) { 
                                    String   value = null;

                                    switch (cell.getCellType())
                                    {

                                        case HSSFCell.CELL_TYPE_FORMULA :
                                            value = "FORMULA ";
                                            break;

                                        case HSSFCell.CELL_TYPE_NUMERIC :
                                            value = "NUMERIC value="
                                                    + cell.getNumericCellValue
();
                                            break;

                                        case HSSFCell.CELL_TYPE_STRING :
                                            value = "STRING value="
                                                    + cell.getStringCellValue();
                                            break;

                                        default :
                                    }
%>									
                                    <%= "CELL col=" 
									
	+ cell.getColumnIndex()
                                        + " VALUE=" + value %>
<%
                                } 
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
%>
	Error occurred:  <%= e.getMessage() %>
<%			
                e.printStackTrace();
            }

	} 
%> 
</body>
</html>

