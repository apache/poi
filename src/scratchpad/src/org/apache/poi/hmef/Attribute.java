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

package org.apache.poi.hmef;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;


/**
 * An attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 * Note - the types and IDs differ from standard Outlook/MAPI
 *  ones, so we can't just re-use the HSMF ones.
 */
public final class Attribute {
   // Types taken from http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefattributetype%28v=EXCHG.140%29.aspx
   public static final int TYPE_TRIPLES = 0x0000;
   public static final int TYPE_STRING  = 0x0001;
   public static final int TYPE_TEXT    = 0x0002;
   public static final int TYPE_DATE    = 0x0003;
   public static final int TYPE_SHORT   = 0x0004;
   public static final int TYPE_LONG    = 0x0005;
   public static final int TYPE_BYTE    = 0x0006;
   public static final int TYPE_WORD    = 0x0007;
   public static final int TYPE_DWORD   = 0x0008;
   public static final int TYPE_MAX     = 0x0009;

   // Types taken from http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertytype%28v=EXCHG.140%29.aspx
   /** AppTime - application time value */
   public static final int PTYPE_APPTIME = 0x0007;
   /** Binary - counted byte array */
   public static final int PTYPE_BINARY  = 0x0102;
   /** Boolean - 16-bit Boolean value. '0' is false. Non-zero is true */
   public static final int PTYPE_BOOLEAN = 0x000B;
   /** ClassId - OLE GUID */
   public static final int PTYPE_CLASSID = 0x0048;
   /** Currency - signed 64-bit integer that represents a base ten decimal with four digits to the right of the decimal point */
   public static final int PTYPE_CURRENCY = 0x0006;
   /** Double - floating point double */
   public static final int PTYPE_DOUBLE   = 0x0005;
   /** Error - 32-bit error value */
   public static final int PTYPE_ERROR = 0x000A;
   /** I2 - signed 16-bit value */
   public static final int PTYPE_I2 = 0x0002;
   /** I8 - 8-byte signed integer */
   public static final int PTYPE_I8 = 0x0014;
   /** Long - signed 32-bit value */
   public static final int PTYPE_LONG = 0x0003;
   /** MultiValued - Value part contains multiple values */
   public static final int PTYPE_MULTIVALUED = 0x1000;
   /** Null - NULL property value */
   public static final int PTYPE_NULL = 0x0001;
   /** Object - embedded object in a property */
   public static final int PTYPE_OBJECT = 0x000D;
   /** R4 - 4-byte floating point value */
   public static final int PTYPE_R4 = 0x0004;
   /** String8 - null-terminated 8-bit character string */
   public static final int PTYPE_STRING8 = 0x001E;
   /** SysTime - FILETIME 64-bit integer specifying the number of 100ns periods since Jan 1, 1601 */
   public static final int PTYPE_SYSTIME = 0x0040;
   /** Unicode - null-terminated Unicode string */
   public static final int PTYPE_UNICODE = 0x001F;
   /** Unspecified */
   public static final int PTYPE_UNSPECIFIED = 0x0000;
                    

   // Levels taken from http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefattributelevel%28v=EXCHG.140%29.aspx
   public static final int LEVEL_MESSAGE     = 0x01;
   public static final int LEVEL_ATTACHMENT  = 0x02;
   public static final int LEVEL_END_OF_FILE = -0x01;
   
   // ID information taken from http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefattributetag%28v=EXCHG.140%29.aspx
   public static final AttributeID ID_AIDOWNER = 
      new AttributeID(0x0008, TYPE_LONG, "AidOwner", "PR_OWNER_APPT_ID");
   public static final AttributeID ID_ATTACHCREATEDATE = 
      new AttributeID(0x8012, TYPE_DATE, "AttachCreateDate", "PR_CREATION_TIME");
   public static final AttributeID ID_ATTACHDATA = 
      new AttributeID(0x800F, TYPE_BYTE, "AttachData", "PR_ATTACH_DATA_BIN");
   public static final AttributeID ID_ATTACHMENT = 
      new AttributeID(0x9005, TYPE_BYTE, "Attachment", null);
   public static final AttributeID ID_ATTACHMETAFILE = 
      new AttributeID(0x8011, TYPE_BYTE, "AttachMetaFile", "PR_ATTACH_RENDERING");
   public static final AttributeID ID_ATTACHMODIFYDATE = 
      new AttributeID(0x8013, TYPE_DATE, "AttachModifyDate", "PR_LAST_MODIFICATION_TIME");
   public static final AttributeID ID_ATTACHRENDERDATA = 
      new AttributeID(0x9002, TYPE_BYTE, "AttachRenderData", "attAttachRenddata");
   public static final AttributeID ID_ATTACHTITLE = 
      new AttributeID(0x8010, TYPE_STRING, "AttachTitle", "PR_ATTACH_FILENAME");
   public static final AttributeID ID_ATTACHTRANSPORTFILENAME = 
      new AttributeID(0x9001, TYPE_BYTE, "AttachTransportFilename", "PR_ATTACH_TRANSPORT_NAME");
   public static final AttributeID ID_BODY = 
      new AttributeID(0x800C, TYPE_TEXT, "Body", "PR_BODY");
   public static final AttributeID ID_CONVERSATIONID = 
      new AttributeID(0x800B, TYPE_STRING, "ConversationId", "PR_CONVERSATION_KEY");
   public static final AttributeID ID_DATEEND =
      new AttributeID(0x0007, TYPE_DATE, "DateEnd", "PR_END_DATE");
   public static final AttributeID ID_DATEMODIFIED = 
      new AttributeID(0x8020, TYPE_DATE, "DateModified", "PR_LAST_MODIFICATION_TIME ");
   public static final AttributeID ID_DATERECEIVED = 
      new AttributeID(0x8006, TYPE_DATE, "DateReceived", "PR_MESSAGE_DELIVERY_TIME ");
   public static final AttributeID ID_DATESENT = 
      new AttributeID(0x8005, TYPE_DATE, "DateSent", "PR_CLIENT_SUBMIT_TIME ");
   public static final AttributeID ID_DATESTART = 
      new AttributeID(0x0006, TYPE_DATE, "DateStart", "PR_START_DATE ");
   public static final AttributeID ID_DELEGATE = 
      new AttributeID(0x0002, TYPE_BYTE, "Delegate", "PR_RCVD_REPRESENTING_xxx ");
   public static final AttributeID ID_FROM = 
      new AttributeID(0x8000, TYPE_STRING, "From", "PR_SENDER_ENTRYID");
   public static final AttributeID ID_MAPIPROPERTIES = 
      new AttributeID(0x9003, TYPE_BYTE, "MapiProperties", null);
   public static final AttributeID ID_MESSAGECLASS = 
      new AttributeID(0x8008, TYPE_WORD, "MessageClass", "PR_MESSAGE_CLASS ");
   public static final AttributeID ID_MESSAGEID = 
      new AttributeID(0x8009, TYPE_STRING, "MessageId", "PR_SEARCH_KEY");
   public static final AttributeID ID_MESSAGESTATUS = 
      new AttributeID(0x8007, TYPE_BYTE, "MessageStatus", "PR_MESSAGE_FLAGS");
   public static final AttributeID ID_NULL = 
      new AttributeID(0x0000, -1, "Null", null);
   public static final AttributeID ID_OEMCODEPAGE = 
      new AttributeID(0x9007, TYPE_BYTE, "OemCodepage", "AttOemCodepage");
   public static final AttributeID ID_ORIGINALMESSAGECLASS = 
      new AttributeID(0x0006, TYPE_WORD, "OriginalMessageClass", "PR_ORIG_MESSAGE_CLASS"); 
   public static final AttributeID ID_OWNER = 
      new AttributeID(0x0000, TYPE_BYTE, "Owner", "PR_RCVD_REPRESENTING_xxx");
   public static final AttributeID ID_PARENTID = 
      new AttributeID(0x800A, TYPE_STRING, "ParentId", "PR_PARENT_KEY");
   public static final AttributeID ID_PRIORITY = 
      new AttributeID(0x800D, TYPE_SHORT, "Priority", "PR_IMPORTANCE");
   public static final AttributeID ID_RECIPIENTTABLE = 
      new AttributeID(0x9004, TYPE_BYTE, "RecipientTable", "PR_MESSAGE_RECIPIENTS");
   public static final AttributeID ID_REQUESTRESPONSE = 
      new AttributeID(0x009, TYPE_SHORT, "RequestResponse", "PR_RESPONSE_REQUESTED");
   public static final AttributeID ID_SENTFOR = 
      new AttributeID(0x0001, TYPE_BYTE, "SentFor", "PR_SENT_REPRESENTING_xxx");
   public static final AttributeID ID_SUBJECT = 
      new AttributeID(0x8004, TYPE_STRING, "Subject", "PR_SUBJECT");
   public static final AttributeID ID_TNEFVERSION = 
      new AttributeID(0x9006, TYPE_DWORD, "TnefVersion", "attTnefVersion");
   public static final AttributeID ID_UNKNOWN =
      new AttributeID(-1, -1, "Unknown", null);
   
   /**
    * Holds information on one potential ID of an
    *  attribute, and provides handy lookups for it.
    */
   public static class AttributeID {
      private static Map<Integer, List<AttributeID>> attributes = new HashMap<Integer, List<AttributeID>>();
      
      public final int id;
      public final int usualType;
      public final String name;
      public final String mapiProperty;
      
      private AttributeID(int id, int usualType, String name, String mapiProperty) {
         this.id = id;
         this.usualType = usualType;
         this.name = name;
         this.mapiProperty = mapiProperty;
         
         // Store it for lookup
         if(! attributes.containsKey(id)) {
            attributes.put(id, new ArrayList<AttributeID>());
         }
         attributes.get(id).add(this);
      }
      public static AttributeID getBest(int id, int type) {
         List<AttributeID> attrs = attributes.get(id);
         if(attrs == null) {
            return ID_UNKNOWN;
         }
         
         // If there's only one, it's easy
         if(attrs.size() == 1) {
            return attrs.get(0);
         }
         
         // Try by type
         for(AttributeID attr : attrs) {
            if(attr.usualType == type) return attr;
         }
            
         // Go for the first if we can't otherwise decide...
         return attrs.get(0);
      }
      public String toString() {
         StringBuffer str = new StringBuffer();
         str.append(name);
         str.append(" [");
         str.append(id);
         str.append("]");
         if(mapiProperty != null) {
            str.append(" (");
            str.append(mapiProperty);
            str.append(")");
         }
         return str.toString();
      }
   }
   
   private final AttributeID id;
   private final int type;
   private final byte[] data;
   private final int checksum;
   
   /**
    * Constructs a single new attribute from
    *  the contents of the stream
    */
   public Attribute(InputStream inp) throws IOException {
      int id     = LittleEndian.readUShort(inp);
      this.type  = LittleEndian.readUShort(inp);
      int length = LittleEndian.readInt(inp);
      
      this.id = AttributeID.getBest(id, type);
      data = new byte[length];
      IOUtils.readFully(inp, data);
      
      checksum = LittleEndian.readUShort(inp);
      
      // TODO Handle the MapiProperties attribute in
      //  a different way, as we need to recurse into it
   }

   public AttributeID getId() {
      return id;
   }

   public int getType() {
      return type;
   }

   public byte[] getData() {
      return data;
   }
   
   public String toString() {
      return "Attachment " + getId().toString() + ", type=" + type + 
             ", data length=" + data.length; 
   }
}
