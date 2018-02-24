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
package org.apache.xmlbeans.test.performance.jibx.binding;

//import java.util.Calendar;
import java.util.ArrayList;

public class JibxPurchaseOrder{
  private Customer customer;
  // NEED TO: implement a deserializer for Calendar
  private String date;
  private ArrayList lineitems;
  private Shipper shipper;

  public Customer getCustomer(){return customer;}
  public String getDate(){return date;}
  public ArrayList getLineitems(){return lineitems;}
  public Shipper getShipper(){return shipper;}
}
