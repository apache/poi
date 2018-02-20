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

import org.openuri.mytest.Person;
import org.openuri.mytest.CustomerDocument;

import java.util.Date;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import drtcases.TestEnv;
import junit.framework.Assert;

public class SimplePersonTest
{
    public static void main(String args[]) throws Exception
    {
        test();
    }

    public static void test() throws Exception
    {
        CustomerDocument doc =
            CustomerDocument.Factory.parse(
                TestEnv.xbeanCase("schema/simple/person.xml"), null);

        // Move from the root to the root customer element
        Person person = doc.getCustomer();
        Assert.assertEquals("Howdy", person.getFirstname());
        Assert.assertEquals(4,   person.sizeOfNumberArray());
        Assert.assertEquals(436, person.getNumberArray(0));
        Assert.assertEquals(123, person.getNumberArray(1));
        Assert.assertEquals(44,  person.getNumberArray(2));
        Assert.assertEquals(933, person.getNumberArray(3));
        Assert.assertEquals(2,   person.sizeOfBirthdayArray());
        Assert.assertEquals(new Date("Tue Aug 25 17:00:00 PDT 1998"), person.getBirthdayArray(0));

        Person.Gender.Enum g = person.getGender();
        Assert.assertEquals(Person.Gender.MALE, g);

        Assert.assertEquals("EGIQTWYZJ", new String(person.getHex()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64()));

        Assert.assertEquals("GGIQTWYGG", new String(person.getHexAtt()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        person.setFirstname("George");
        Assert.assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        Assert.assertEquals("hex encoding", new String(person.getHex()));

        person.setBase64("base64 encoded".getBytes());
        Assert.assertEquals("base64 encoded",
                            new String(person.getBase64()));

        //person.setHexAtt("hex encoding in attributes".getBytes());
        //Assert.assertEquals("hex encoding in attributes",
        //                    new String(person.getHexAtt()));

        //person.setBase64Att("base64 encoded in attributes".getBytes());
        //Assert.assertEquals("base64 encoded in attributes",
        //                    new String(person.getBase64Att()));
//
//        XmlCursor cp = person.newXmlCursor();
//        Root.dump( cp );

//        XmlCursor c = person.xgetBirthdayArray(0).newXmlCursor();

//        Root.dump( c );

//        person.setBirthday(0,new Date("Tue Aug 25 16:00:00 PDT 2001"));

//        Root.dump( c );

//        c.toNextToken();

//        System.out.println( "---" + c.getText() + "---" );

//        Root.dump( c );

//        Assert.assertEquals(person.getBirthdayArray(0), new Date("Tue Aug 25 16:00:00 PDT 2002"));
//
//        person.setFirstname("George");
//        Assert.assertEquals(person.getFirstname(), "George");
//
//        person.addNumber( (short) 69 );
//        Assert.assertEquals(person.countNumber(), 5);
//        Assert.assertEquals(person.getNumberArray(4), 69);
//
//
//        while ( c.hasNextToken() )
//            c.toNextToken();
    }
}
