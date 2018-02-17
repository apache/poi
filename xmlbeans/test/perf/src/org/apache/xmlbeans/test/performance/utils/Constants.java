/*   Copyright 2004 The Apache Software Foundation
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.apache.xmlbeans.test.performance.utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

public class Constants
{
  private Constants(){}
  
  public static final String DELIM = "TEST: ";

  public static final String P = File.separator;
  public static final int ITERATIONS = 6000;
  public static final int CURSOR_ITERATIONS = 1000;
  public static final int XML_SIZE = 10000;

  // for getting/setting scenarios
  public static final int GET_SET_ITERATIONS = 5000000;
  public static final int STRING_SIZE = 1000;

  // schema instances
  public static final String XSD_DIR = System.getProperty("PERF_ROOT")+P+"cases"+P+"xsd";
  public static final String XML_DIR = System.getProperty("PERF_ROOT")+P+"cases"+P+"xml";  
  public static final String PO_XSD = XSD_DIR+P+"purchase-order.xsd";
  public static final String PO_INSTANCE_1 = XSD_DIR+P+"purchase-order-1.xml";
  public static final String PO_INSTANCE_2 = XSD_DIR+P+"purchase-order-2.xml";
  public static final String PO_INSTANCE_3 = XSD_DIR+P+"purchase-order-3.xml";
  public static final String PO_INSTANCE_4 = XSD_DIR+P+"purchase-order-4.xml";
  public static final String PO_INSTANCE_5 = XSD_DIR+P+"purchase-order-5.xml";
  public static final String PO_INSTANCE_6 = XSD_DIR+P+"purchase-order-6.xml";
  public static final String PO_INSTANCE_7 = XSD_DIR+P+"purchase-order-7.xml";

  // values for purchase order in-memory construction
  public static final String PO_CUSTOMER_NAME = "First Last";
  public static final String PO_CUSTOMER_ADDR = "123 Sesame St.";
  public static final int PO_NUM_LINEITEMS = 50;
  public static final String PO_LI_DESC = "line item description";
  public static final BigDecimal PO_LI_PUO = new BigDecimal("1.23");
  public static final double PO_LI_PRICE = 1.23000;
  public static final int PO_LI_QUANTITY = 123;
  public static final String PO_SHIPPER_NAME = "Joe Shipper";
  public static final BigDecimal PO_SHIPPER_POR = new BigDecimal("4.56");
  public static final String PO_NS = "http://openuri.org/easypo";
  // for xerces dom/sax
  public static final String PO_DATE_STRING = "2003-01-07T14:16:00-05:00";
  public static final String PO_LI_PUO_STRING = "1.23";
  public static final String PO_LI_PRICE_STRING = "1.23000";
  public static final String PO_LI_QUANTITY_STRING = "123";
  public static final String PO_SHIPPER_POR_STRING = "4.56";
  // for sax parsing
  public static final String sAddress = "address";
  public static final String sName = "name";
  public static final String sDesc = "description";
  public static final String sPrice = "price";
  public static final String sLineItem = "line-item";
  // for sax construction;
  public static final int initialBuffSize = 2048;
  public static final String prefix = "po";
  public static final String qNamespace = "xmlns:"+prefix;
  public static final String qString = "xs:string";
  public static final String qPO = "po:purchase-order";
  public static final String qCustomer = "po:customer";
  public static final String qName = "po:name";
  public static final String qAddress = "po:address";
  public static final String qDate = "po:date";
  public static final String qLineItem = "po:line-item";
  public static final String qDescription = "po:description";
  public static final String qPerUnitOunces = "po:per-unit-ounces";
  public static final String qPrice = "po:price";
  public static final String qQuantity = "po:quantity";
  public static final String qShipper = "po:shipper";
  public static final String qPerOunceRate = "po:per-ounce-rate";

  // values for primitives top down construction
  public static final byte myByte = 127;
  public static final int myInt = 7;
  public static final int myNegInt = -7;
  public static final long myLong = 123456789;
  public static final short myShort = 1;
  public static final byte[] myHexbin = {-127,0,127};
  public static final double myDouble = 987654321;
  public static final float myFloat = 3.14f;
  public static final boolean myBool = true;

  // values for non-primitives top down construction
  public static final BigDecimal myBigDecimal = new BigDecimal(987654321.123456789);
  public static final BigInteger myPosBigInteger = new BigInteger("987654321");
  public static final BigInteger myNegBigInteger = new BigInteger("-987654321");
  public static final GregorianCalendar myDate = new GregorianCalendar();
  public static final String myString = "hello world!";
  
  // memory scenario values
  public static final int MEM_INTERVAL = 100000;
  public static final int MEM_INITIALSIZE = 2000000;
  
  // netui config xsd for real-world example
  public static final String NETUI_CONFIG_INSTANCE = XSD_DIR+P+"netui-config.xml";
}
