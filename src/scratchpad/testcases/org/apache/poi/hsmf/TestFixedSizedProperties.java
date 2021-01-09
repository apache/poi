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

package org.apache.poi.hsmf;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.ChunkBasedPropertyValue;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.TimePropertyValue;
import org.apache.poi.hsmf.dev.HSMFDump;
import org.apache.poi.hsmf.extractor.OutlookTextExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that we can read fixed sized properties, as well as variable
 *  ones, for example Submission Dates
 */
public final class TestFixedSizedProperties {
   private static final String messageSucceeds = "53784_succeeds.msg";
   private static final String messageFails = "53784_fails.msg";
   private static MAPIMessage mapiMessageSucceeds;
   private static MAPIMessage mapiMessageFails;
   private static POIFSFileSystem fsMessageSucceeds;
   private static POIFSFileSystem fsMessageFails;
   private static SimpleDateFormat messageDateFormat;
   private static TimeZone userTimeZone;

   /**
    * Initialize this test, load up the messages.
    */
   @BeforeAll
   public static void initMapi()  throws Exception {
       POIDataSamples samples = POIDataSamples.getHSMFInstance();
       fsMessageSucceeds = new POIFSFileSystem(samples.getFile(messageSucceeds));
       fsMessageFails = new POIFSFileSystem(samples.getFile(messageFails));

       mapiMessageSucceeds = new MAPIMessage(fsMessageSucceeds);
       mapiMessageFails = new MAPIMessage(fsMessageFails);

       messageDateFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss", Locale.ROOT);
       messageDateFormat.setTimeZone(LocaleUtil.TIMEZONE_UTC);

       userTimeZone = LocaleUtil.getUserTimeZone();
       LocaleUtil.setUserTimeZone(LocaleUtil.TIMEZONE_UTC);
   }


   @AfterAll
   public static void closeFS() throws Exception {
       LocaleUtil.setUserTimeZone(userTimeZone);
       fsMessageSucceeds.close();
       fsMessageFails.close();
   }

   /**
    * Check we can find a sensible number of properties on a few
    * of our test files
    */
   @Test
   void testPropertiesFound() {
       Map<MAPIProperty,List<PropertyValue>> props;

       props = mapiMessageSucceeds.getMainChunks().getProperties();
       assertTrue(props.size() > 10, props.toString());

       props = mapiMessageFails.getMainChunks().getProperties();
       assertTrue(props.size() > 10, props.toString());
   }

   /**
    * Check we find properties of a variety of different types
    */
   @Test
   void testPropertyValueTypes() {
       Chunks mainChunks = mapiMessageSucceeds.getMainChunks();

       // Ask to have the values looked up
       Map<MAPIProperty,List<PropertyValue>> props = mainChunks.getProperties();
       HashSet<Class<? extends PropertyValue>> seenTypes =
               new HashSet<>();
       for (List<PropertyValue> pvs : props.values()) {
           for (PropertyValue pv : pvs) {
               seenTypes.add(pv.getClass());
           }
       }
       assertTrue(seenTypes.size() > 3, seenTypes.toString());
       assertTrue(seenTypes.contains(LongPropertyValue.class), seenTypes.toString());
       assertTrue(seenTypes.contains(TimePropertyValue.class), seenTypes.toString());
       assertFalse(seenTypes.contains(ChunkBasedPropertyValue.class), seenTypes.toString());

       // Ask for the raw values
       seenTypes.clear();
       for (PropertyValue pv : mainChunks.getRawProperties().values()) {
           seenTypes.add(pv.getClass());
       }
       assertTrue(seenTypes.size() > 3, seenTypes.toString());
       assertTrue(seenTypes.contains(LongPropertyValue.class), seenTypes.toString());
       assertTrue(seenTypes.contains(TimePropertyValue.class), seenTypes.toString());
       assertTrue(seenTypes.contains(ChunkBasedPropertyValue.class), seenTypes.toString());
   }

   /**
    * Test to see if we can read the Date Chunk with OutlookTextExtractor.
    */
   @Test
   void testReadMessageDateSucceedsWithOutlookTextExtractor() throws Exception {
      OutlookTextExtractor ext = new OutlookTextExtractor(mapiMessageSucceeds);
      ext.setCloseFilesystem(false);

      String text = ext.getText();
      assertContains(text, "Date: Fri, 22 Jun 2012 18:32:54 +0000\n");
      ext.close();
   }

    /**
    * Test to see if we can read the Date Chunk with OutlookTextExtractor.
    */
   @Test
   void testReadMessageDateFailsWithOutlookTextExtractor() throws Exception {
      OutlookTextExtractor ext = new OutlookTextExtractor(mapiMessageFails);
      ext.setCloseFilesystem(false);

      String text = ext.getText();
      assertContains(text, "Date: Thu, 21 Jun 2012 14:14:04 +0000\n");
      ext.close();
   }

   /**
    * Test to see if we can read the Date Chunk with HSMFDump.
    */
   @Test
   void testReadMessageDateSucceedsWithHSMFDump() throws IOException {
       PrintStream stream = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
       HSMFDump dump = new HSMFDump(fsMessageSucceeds);
       dump.dump(stream);
   }

   /**
    * Test to see if we can read the Date Chunk with HSMFDump.
    */
   @Test
   void testReadMessageDateFailsWithHSMFDump() throws Exception {
       PrintStream stream = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
       HSMFDump dump = new HSMFDump(fsMessageFails);
       dump.dump(stream);
   }

   /**
    * Will be based on the ClientSubmit time
    */
   @Test
   void testClientSubmitTime() throws Exception {
       // Check via the message date
       Calendar clientSubmitTime = mapiMessageSucceeds.getMessageDate();
       assertEquals(
               "Fri, 22 Jun 2012 18:32:54",
               messageDateFormat.format(clientSubmitTime.getTime()));

       // Fetch the property value directly
       Map<MAPIProperty,List<PropertyValue>> props =
               mapiMessageSucceeds.getMainChunks().getProperties();
       List<PropertyValue> pv = props.get(MAPIProperty.CLIENT_SUBMIT_TIME);
       assertNotNull(pv);
       assertEquals(1, pv.size());

       clientSubmitTime = (Calendar)pv.get(0).getValue();
       assertEquals(
               "Fri, 22 Jun 2012 18:32:54",
               messageDateFormat.format(clientSubmitTime.getTime()));
   }
}
