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

package scomp.substGroup.restriction.detailed;

import scomp.common.BaseCase;
import xbean.scomp.substGroup.block.BeachUmbrellaT;
import xbean.scomp.substGroup.deep.*;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;

/**
 */
public class Block extends BaseCase {
    /**
     * TODO: verify that w/ respect to substitution
     * groups block all
     * and block substitution are the same thing;
     *
     * @throws Throwable
     */
    public void testBlockAllInvalid() throws Throwable {
        //should not be able to use CasualBusinessShirt  instead of shirt
        //but BusinessShirt instead of shirt should be OK
        String input =
                "<base:items xmlns:pre=\"http://xbean/scomp/substGroup/Block\"" +
                " xmlns:base=\"http://xbean/scomp/substGroup/Deep\">" +
                "<pre:casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</pre:casualBShirt>" +
                "<base:product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</base:product>" +
                "<pre:umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</pre:umbrella>" +
                "<pre:casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</pre:casualBShirt>" +
                "</base:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
                   };
             assertTrue(compareErrorCodes(errExpected));


       ShirtType bs=BusinessShirtType.Factory.newInstance();
        bs.setName("Bus Shirt");
        bs.setNumber("SKU35");
        bs.setColor("blue");
        bs.setSize(new BigInteger("10"));

         doc.getItems().setProductArray(new ProductType[]{
            bs
        });

         XmlCursor cur=doc.newCursor();
        cur.toFirstContentToken();
        cur.toNextToken();
          cur.toNextToken();
          cur.toNextToken();
        assertEquals(XmlCursor.TokenType.START,cur.currentTokenType());
        cur.setName(new QName("http://xbean/scomp/substGroup/Block","businessShirt","pre"));
        System.out.println("*************** "+doc.xmlText());

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testBlockAllValidParse() throws Throwable {
        String input =
                "<base:items xmlns:pre=\"http://xbean/scomp/substGroup/Block\"" +
                " xmlns:base=\"http://xbean/scomp/substGroup/Deep\">" +
                "<pre:businessShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "</pre:businessShirt>" +
                "<base:product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</base:product>" +
                "<pre:umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</pre:umbrella>" +
                "</base:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testBlockAllValidBuild() throws Throwable {
           ItemsDocument doc = ItemsDocument.Factory.newInstance();
            BusinessShirtType bs = BusinessShirtType.Factory.newInstance();
        bs.setName("Oxford Shirt");
        bs.setNumber("SKU35");
        bs.setColor("blue");
        bs.setSize(new BigInteger("10"));
       ItemType it= doc.addNewItems();
       it.setProductArray(new ProductType[]{bs});

          try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /*
    * even though umbrella has subst. blocked, beachumbrella
    * is a valid product substitution
    */
    public void testBlockSubst() throws Throwable {
        ItemsDocument doc = ItemsDocument.Factory.newInstance();
        ItemType items = doc.addNewItems();
        BeachUmbrellaT bu = BeachUmbrellaT.Factory.newInstance();
        bu.setDiameter(3.4f);
        bu.setNumber("324");
        bu.setName("Beach umbrella");
        items.setProductArray(new ProductType[]{bu});
         try {
            clearErrors();
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


        doc = ItemsDocument.Factory.parse("<base:items xmlns:pre=\"http://xbean/scomp/substGroup/Block\"" +
                "  xmlns:base=\"http://xbean/scomp/substGroup/Deep\">" +
                "<pre:beachumbrella>" +
                "  <number>SKU15</number>" +
                "  <name>Umbrella</name>" +
                "</pre:beachumbrella>" +
                "</base:items>");
        try {
            clearErrors();
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }
}
