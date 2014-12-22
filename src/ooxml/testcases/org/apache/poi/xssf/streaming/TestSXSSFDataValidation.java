package org.apache.poi.xssf.streaming;

import java.util.List;

import org.apache.poi.ss.usermodel.BaseTestDataValidation;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.SXSSFITestDataProvider;

public class TestSXSSFDataValidation extends BaseTestDataValidation {

    public TestSXSSFDataValidation(){
        super(SXSSFITestDataProvider.instance);
    }

    public void test53965() throws Exception {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        try {
            Sheet sheet = wb.createSheet();
            List<? extends DataValidation> lst = sheet.getDataValidations();    //<-- works
            assertEquals(0, lst.size());
    
            //create the cell that will have the validation applied
            sheet.createRow(0).createCell(0);
    
            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dataValidationHelper.createCustomConstraint("SUM($A$1:$A$1) <= 3500");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dataValidationHelper.createValidation(constraint, addressList);
            sheet.addValidationData(validation);
    
            // this line caused XmlValueOutOfRangeException , see Bugzilla 3965
            lst = sheet.getDataValidations();
            assertEquals(1, lst.size());
        } finally {
            wb.close();
        }
    }
}
