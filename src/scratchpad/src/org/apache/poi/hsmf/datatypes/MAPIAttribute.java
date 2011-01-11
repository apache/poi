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

package org.apache.poi.hsmf.datatypes;

import static org.apache.poi.hsmf.datatypes.Types.ASCII_STRING;
import static org.apache.poi.hsmf.datatypes.Types.BINARY;
import static org.apache.poi.hsmf.datatypes.Types.BOOLEAN;
import static org.apache.poi.hsmf.datatypes.Types.DIRECTORY;
import static org.apache.poi.hsmf.datatypes.Types.LONG;
import static org.apache.poi.hsmf.datatypes.Types.TIME;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the list of MAPI Attributes, and allows lookup
 *  by friendly name, ID and MAPI Property Name.
 * 
 * These are taken from the following MSDN resources:
 *  http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertyid%28v=EXCHG.140%29.aspx
 *  http://msdn.microsoft.com/en-us/library/ms526356%28v=exchg.10%29.aspx
 */
public final class MAPIAttribute {
   private static Map<Integer, MAPIAttribute> attributes = new HashMap<Integer, MAPIAttribute>();
   
   public static final MAPIAttribute AB_DEFAULT_DIR =
      new MAPIAttribute(0x3d06, BINARY, "AbDefaultDir", "PR_AB_DEFAULT_DIR");
   public static final MAPIAttribute AB_DEFAULT_PAB =
      new MAPIAttribute(0x3d07, BINARY, "AbDefaultPab", "PR_AB_DEFAULT_PAB");
   public static final MAPIAttribute AB_PROVIDER_ID =
      new MAPIAttribute(0x3615, BINARY, "AbProviderId", "PR_AB_PROVIDER_ID");
   public static final MAPIAttribute AB_PROVIDERS =
      new MAPIAttribute(0x3d01, BINARY, "AbProviders", "PR_AB_PROVIDERS");
   public static final MAPIAttribute AB_SEARCH_PATH =
      new MAPIAttribute(0x3d05, 4354, "AbSearchPath", "PR_AB_SEARCH_PATH");
   public static final MAPIAttribute AB_SEARCH_PATH_UPDATE =
      new MAPIAttribute(0x3d11, BINARY, "AbSearchPathUpdate", "PR_AB_SEARCH_PATH_UPDATE");
   public static final MAPIAttribute ACCESS =
      new MAPIAttribute(0xff4, LONG, "Access", "PR_ACCESS");
   public static final MAPIAttribute ACCESS_LEVEL =
      new MAPIAttribute(0xff7, LONG, "AccessLevel", "PR_ACCESS_LEVEL");
   public static final MAPIAttribute ACCOUNT =
      new MAPIAttribute(0x3a00, ASCII_STRING, "Account", "PR_ACCOUNT");
   public static final MAPIAttribute ADDRTYPE =
      new MAPIAttribute(0x3002, ASCII_STRING, "Addrtype", "PR_ADDRTYPE");
   public static final MAPIAttribute ALTERNATE_RECIPIENT =
      new MAPIAttribute(0x3a01, BINARY, "AlternateRecipient", "PR_ALTERNATE_RECIPIENT");
   public static final MAPIAttribute ALTERNATE_RECIPIENT_ALLOWED =
      new MAPIAttribute(2, BOOLEAN, "AlternateRecipientAllowed", "PR_ALTERNATE_RECIPIENT_ALLOWED");
   public static final MAPIAttribute ANR =
      new MAPIAttribute(0x360c, ASCII_STRING, "Anr", "PR_ANR");
   public static final MAPIAttribute ASSISTANT =
      new MAPIAttribute(0x3a30, ASCII_STRING, "Assistant", "PR_ASSISTANT");
   public static final MAPIAttribute ASSISTANT_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a2e, ASCII_STRING, "AssistantTelephoneNumber", "PR_ASSISTANT_TELEPHONE_NUMBER");
   public static final MAPIAttribute ASSOC_CONTENT_COUNT =
      new MAPIAttribute(0x3617, LONG, "AssocContentCount", "PR_ASSOC_CONTENT_COUNT");
   public static final MAPIAttribute ATTACH_ADDITIONAL_INFO =
      new MAPIAttribute(0x370f, BINARY, "AttachAdditionalInfo", "PR_ATTACH_ADDITIONAL_INFO");
   public static final MAPIAttribute ATTACH_CONTENT_BASE =
      new MAPIAttribute(0x3711, -1, "AttachContentBase", "PR_ATTACH_CONTENT_BASE");
   public static final MAPIAttribute ATTACH_CONTENT_ID =
      new MAPIAttribute(0x3712, -1, "AttachContentId", "PR_ATTACH_CONTENT_ID");
   public static final MAPIAttribute ATTACH_CONTENT_LOCATION =
      new MAPIAttribute(0x3713, -1, "AttachContentLocation", "PR_ATTACH_CONTENT_LOCATION");
   public static final MAPIAttribute ATTACH_DATA =
      new MAPIAttribute(0x3701, BINARY, "AttachData", "PR_ATTACH_DATA_OBJ");
   public static final MAPIAttribute ATTACH_DISPOSITION =
      new MAPIAttribute(0x3716, -1, "AttachDisposition", "PR_ATTACH_DISPOSITION");
   public static final MAPIAttribute ATTACH_ENCODING =
      new MAPIAttribute(0x3702, BINARY, "AttachEncoding", "PR_ATTACH_ENCODING");
   public static final MAPIAttribute ATTACH_EXTENSION =
      new MAPIAttribute(0x3703, ASCII_STRING, "AttachExtension", "PR_ATTACH_EXTENSION");
   public static final MAPIAttribute ATTACH_FILENAME =
      new MAPIAttribute(0x3704, ASCII_STRING, "AttachFilename", "PR_ATTACH_FILENAME");
   public static final MAPIAttribute ATTACH_FLAGS =
      new MAPIAttribute(0x3714, -1, "AttachFlags", "PR_ATTACH_FLAGS");
   public static final MAPIAttribute ATTACH_LONG_FILENAME =
      new MAPIAttribute(0x3707, ASCII_STRING, "AttachLongFilename", "PR_ATTACH_LONG_FILENAME");
   public static final MAPIAttribute ATTACH_LONG_PATHNAME =
      new MAPIAttribute(0x370d, ASCII_STRING, "AttachLongPathname", "PR_ATTACH_LONG_PATHNAME");
   public static final MAPIAttribute ATTACH_METHOD =
      new MAPIAttribute(0x3705, LONG, "AttachMethod", "PR_ATTACH_METHOD");
   public static final MAPIAttribute ATTACH_MIME_SEQUENCE =
      new MAPIAttribute(0x3710, -1, "AttachMimeSequence", "PR_ATTACH_MIME_SEQUENCE");
   public static final MAPIAttribute ATTACH_MIME_TAG =
      new MAPIAttribute(0x370e, ASCII_STRING, "AttachMimeTag", "PR_ATTACH_MIME_TAG");
   public static final MAPIAttribute ATTACH_NETSCAPE_MAC_INFO =
      new MAPIAttribute(0x3715, -1, "AttachNetscapeMacInfo", "PR_ATTACH_NETSCAPE_MAC_INFO");
   public static final MAPIAttribute ATTACH_NUM =
      new MAPIAttribute(0xe21, LONG, "AttachNum", "PR_ATTACH_NUM");
   public static final MAPIAttribute ATTACH_PATHNAME =
      new MAPIAttribute(0x3708, ASCII_STRING, "AttachPathname", "PR_ATTACH_PATHNAME");
   public static final MAPIAttribute ATTACH_RENDERING =
      new MAPIAttribute(0x3709, BINARY, "AttachRendering", "PR_ATTACH_RENDERING");
   public static final MAPIAttribute ATTACH_SIZE =
      new MAPIAttribute(0xe20, LONG, "AttachSize", "PR_ATTACH_SIZE");
   public static final MAPIAttribute ATTACH_TAG =
      new MAPIAttribute(0x370a, BINARY, "AttachTag", "PR_ATTACH_TAG");
   public static final MAPIAttribute ATTACH_TRANSPORT_NAME =
      new MAPIAttribute(0x370c, ASCII_STRING, "AttachTransportName", "PR_ATTACH_TRANSPORT_NAME");
   public static final MAPIAttribute ATTACHMENT_X400_PARAMETERS =
      new MAPIAttribute(0x3700, BINARY, "AttachmentX400Parameters", "PR_ATTACHMENT_X400_PARAMETERS");
   public static final MAPIAttribute AUTHORIZING_USERS =
      new MAPIAttribute(3, BINARY, "AuthorizingUsers", "PR_AUTHORIZING_USERS");
   public static final MAPIAttribute AUTO_FORWARD_COMMENT =
      new MAPIAttribute(4, ASCII_STRING, "AutoForwardComment", "PR_AUTO_FORWARD_COMMENT");
   public static final MAPIAttribute AUTO_FORWARDED =
      new MAPIAttribute(5, BOOLEAN, "AutoForwarded", "PR_AUTO_FORWARDED");
   public static final MAPIAttribute AUTO_RESPONSE_SUPPRESS =
      new MAPIAttribute(0x3fdf, -1, "AutoResponseSuppress", "PR_AUTO_RESPONSE_SUPPRESS");
   public static final MAPIAttribute BIRTHDAY =
      new MAPIAttribute(0x3a42, TIME, "Birthday", "PR_BIRTHDAY");
   public static final MAPIAttribute BODY =
      new MAPIAttribute(0x1000, ASCII_STRING, "Body", "PR_BODY");
   public static final MAPIAttribute BODY_CONTENT_ID =
      new MAPIAttribute(0x1015, -1, "BodyContentId", "PR_BODY_CONTENT_ID");
   public static final MAPIAttribute BODY_CONTENT_LOCATION =
      new MAPIAttribute(0x1014, -1, "BodyContentLocation", "PR_BODY_CONTENT_LOCATION");
   public static final MAPIAttribute BODY_CRC =
      new MAPIAttribute(0xe1c, LONG, "BodyCrc", "PR_BODY_CRC");
   public static final MAPIAttribute BODY_HTML =
      new MAPIAttribute(0x1013, -1, "BodyHtml", "data");
   public static final MAPIAttribute BUSINESS_FAX_NUMBER =
      new MAPIAttribute(0x3a24, ASCII_STRING, "BusinessFaxNumber", "PR_BUSINESS_FAX_NUMBER");
   public static final MAPIAttribute BUSINESS_HOME_PAGE =
      new MAPIAttribute(0x3a51, ASCII_STRING, "BusinessHomePage", "PR_BUSINESS_HOME_PAGE");
   public static final MAPIAttribute CALLBACK_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a02, ASCII_STRING, "CallbackTelephoneNumber", "PR_CALLBACK_TELEPHONE_NUMBER");
   public static final MAPIAttribute CAR_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1e, ASCII_STRING, "CarTelephoneNumber", "PR_CAR_TELEPHONE_NUMBER");
   public static final MAPIAttribute CHILDRENS_NAMES =
      new MAPIAttribute(0x3a58, 4126, "ChildrensNames", "PR_CHILDRENS_NAMES");
   public static final MAPIAttribute CLIENT_SUBMIT_TIME =
      new MAPIAttribute(0x39, TIME, "ClientSubmitTime", "PR_CLIENT_SUBMIT_TIME");
   public static final MAPIAttribute COMMENT =
      new MAPIAttribute(0x3004, ASCII_STRING, "Comment", "PR_COMMENT");
   public static final MAPIAttribute COMMON_VIEWS_ENTRY_ID =
      new MAPIAttribute(0x35e6, BINARY, "CommonViewsEntryId", "PR_COMMON_VIEWS_ENTRYID");
   public static final MAPIAttribute COMPANY_MAIN_PHONE_NUMBER =
      new MAPIAttribute(0x3a57, ASCII_STRING, "CompanyMainPhoneNumber", "PR_COMPANY_MAIN_PHONE_NUMBER");
   public static final MAPIAttribute COMPANY_NAME =
      new MAPIAttribute(0x3a16, ASCII_STRING, "CompanyName", "PR_COMPANY_NAME");
   public static final MAPIAttribute COMPUTER_NETWORK_NAME =
      new MAPIAttribute(0x3a49, ASCII_STRING, "ComputerNetworkName", "PR_COMPUTER_NETWORK_NAME");
   public static final MAPIAttribute CONTACT_ADDRTYPES =
      new MAPIAttribute(0x3a54, 4126, "ContactAddrtypes", "PR_CONTACT_ADDRTYPES");
   public static final MAPIAttribute CONTACT_DEFAULT_ADDRESS_INDEX =
      new MAPIAttribute(0x3a55, LONG, "ContactDefaultAddressIndex", "PR_CONTACT_DEFAULT_ADDRESS_INDEX");
   public static final MAPIAttribute CONTACT_EMAIL_ADDRESSES =
      new MAPIAttribute(0x3a56, 4126, "ContactEmailAddresses", "PR_CONTACT_EMAIL_ADDRESSES");
   public static final MAPIAttribute CONTACT_ENTRY_IDS =
      new MAPIAttribute(0x3a53, 4354, "ContactEntryIds", "PR_CONTACT_ENTRYIDS");
   public static final MAPIAttribute CONTACT_VERSION =
      new MAPIAttribute(0x3a52, 72, "ContactVersion", "PR_CONTACT_VERSION");
   public static final MAPIAttribute CONTAINER_CLASS =
      new MAPIAttribute(0x3613, ASCII_STRING, "ContainerClass", "PR_CONTAINER_CLASS");
   public static final MAPIAttribute CONTAINER_CONTENTS =
      new MAPIAttribute(0x360f, DIRECTORY, "ContainerContents", "PR_CONTAINER_CONTENTS");
   public static final MAPIAttribute CONTAINER_FLAGS =
      new MAPIAttribute(0x3600, LONG, "ContainerFlags", "PR_CONTAINER_FLAGS");
   public static final MAPIAttribute CONTAINER_HIERARCHY =
      new MAPIAttribute(0x360e, DIRECTORY, "ContainerHierarchy", "PR_CONTAINER_HIERARCHY");
   public static final MAPIAttribute CONTAINER_MODIFY_VERSION =
      new MAPIAttribute(0x3614, 20, "ContainerModifyVersion", "PR_CONTAINER_MODIFY_VERSION");
   public static final MAPIAttribute CONTENT_CONFIDENTIALITY_ALGORITHM_ID =
      new MAPIAttribute(6, BINARY, "ContentConfidentialityAlgorithmId", "PR_CONTENT_CONFIDENTIALITY_ALGORITHM_ID");
   public static final MAPIAttribute CONTENT_CORRELATOR =
      new MAPIAttribute(7, BINARY, "ContentCorrelator", "PR_CONTENT_CORRELATOR");
   public static final MAPIAttribute CONTENT_COUNT =
      new MAPIAttribute(0x3602, LONG, "ContentCount", "PR_CONTENT_COUNT");
   public static final MAPIAttribute CONTENT_IDENTIFIER =
      new MAPIAttribute(8, ASCII_STRING, "ContentIdentifier", "PR_CONTENT_IDENTIFIER");
   public static final MAPIAttribute CONTENT_INTEGRITY_CHECK =
      new MAPIAttribute(0xc00, BINARY, "ContentIntegrityCheck", "PR_CONTENT_INTEGRITY_CHECK");
   public static final MAPIAttribute CONTENT_LENGTH =
      new MAPIAttribute(9, LONG, "ContentLength", "PR_CONTENT_LENGTH");
   public static final MAPIAttribute CONTENT_RETURN_REQUESTED =
      new MAPIAttribute(10, BOOLEAN, "ContentReturnRequested", "PR_CONTENT_RETURN_REQUESTED");
   public static final MAPIAttribute CONTENT_UNREAD =
      new MAPIAttribute(0x3603, LONG, "ContentUnread", "PR_CONTENT_UNREAD");
   public static final MAPIAttribute CONTENTS_SORT_ORDER =
      new MAPIAttribute(0x360d, 4099, "ContentsSortOrder", "PR_CONTENTS_SORT_ORDER");
   public static final MAPIAttribute CONTROL_FLAGS =
      new MAPIAttribute(0x3f00, LONG, "ControlFlags", "PR_CONTROL_FLAGS");
   public static final MAPIAttribute CONTROL_ID =
      new MAPIAttribute(0x3f07, BINARY, "ControlId", "PR_CONTROL_ID");
   public static final MAPIAttribute CONTROL_STRUCTURE =
      new MAPIAttribute(0x3f01, BINARY, "ControlStructure", "PR_CONTROL_STRUCTURE");
   public static final MAPIAttribute CONTROL_TYPE =
      new MAPIAttribute(0x3f02, LONG, "ControlType", "PR_CONTROL_TYPE");
   public static final MAPIAttribute CONVERSATION_INDEX =
      new MAPIAttribute(0x71, BINARY, "ConversationIndex", "PR_CONVERSATION_INDEX");
   public static final MAPIAttribute CONVERSATION_KEY =
      new MAPIAttribute(11, BINARY, "ConversationKey", "PR_CONVERSATION_KEY");
   public static final MAPIAttribute CONVERSATION_TOPIC =
      new MAPIAttribute(0x70, ASCII_STRING, "ConversationTopic", "PR_CONVERSATION_TOPIC");
   public static final MAPIAttribute CONVERSION_EITS =
      new MAPIAttribute(12, BINARY, "ConversionEits", "PR_CONVERSION_EITS");
   public static final MAPIAttribute CONVERSION_PROHIBITED =
      new MAPIAttribute(0x3a03, BOOLEAN, "ConversionProhibited", "PR_CONVERSION_PROHIBITED");
   public static final MAPIAttribute CONVERSION_WITH_LOSS_PROHIBITED =
      new MAPIAttribute(13, BOOLEAN, "ConversionWithLossProhibited", "PR_CONVERSION_WITH_LOSS_PROHIBITED");
   public static final MAPIAttribute CONVERTED_EITS =
      new MAPIAttribute(14, BINARY, "ConvertedEits", "PR_CONVERTED_EITS");
   public static final MAPIAttribute CORRELATE =
      new MAPIAttribute(0xe0c, BOOLEAN, "Correlate", "PR_CORRELATE");
   public static final MAPIAttribute CORRELATE_MTSID =
      new MAPIAttribute(0xe0d, BINARY, "CorrelateMtsid", "PR_CORRELATE_MTSID");
   public static final MAPIAttribute COUNTRY =
      new MAPIAttribute(0x3a26, ASCII_STRING, "Country", "PR_COUNTRY");
   public static final MAPIAttribute CREATE_TEMPLATES =
      new MAPIAttribute(0x3604, DIRECTORY, "CreateTemplates", "PR_CREATE_TEMPLATES");
   public static final MAPIAttribute CREATION_TIME =
      new MAPIAttribute(0x3007, TIME, "CreationTime", "PR_CREATION_TIME");
   public static final MAPIAttribute CREATION_VERSION =
      new MAPIAttribute(0xe19, 20, "CreationVersion", "PR_CREATION_VERSION");
   public static final MAPIAttribute CURRENT_VERSION =
      new MAPIAttribute(0xe00, 20, "CurrentVersion", "PR_CURRENT_VERSION");
   public static final MAPIAttribute CUSTOMER_ID =
      new MAPIAttribute(0x3a4a, ASCII_STRING, "CustomerId", "PR_CUSTOMER_ID");
   public static final MAPIAttribute DEF_CREATE_DL =
      new MAPIAttribute(0x3611, BINARY, "DefCreateDl", "PR_DEF_CREATE_DL");
   public static final MAPIAttribute DEF_CREATE_MAILUSER =
      new MAPIAttribute(0x3612, BINARY, "DefCreateMailuser", "PR_DEF_CREATE_MAILUSER");
   public static final MAPIAttribute DEFAULT_PROFILE =
      new MAPIAttribute(0x3d04, BOOLEAN, "DefaultProfile", "PR_DEFAULT_PROFILE");
   public static final MAPIAttribute DEFAULT_STORE =
      new MAPIAttribute(0x3400, BOOLEAN, "DefaultStore", "PR_DEFAULT_STORE");
   public static final MAPIAttribute DEFAULT_VIEW_ENTRY_ID =
      new MAPIAttribute(0x3616, BINARY, "DefaultViewEntryId", "PR_DEFAULT_VIEW_ENTRYID");
   public static final MAPIAttribute DEFERRED_DELIVERY_TIME =
      new MAPIAttribute(15, TIME, "DeferredDeliveryTime", "PR_DEFERRED_DELIVERY_TIME");
   public static final MAPIAttribute DELEGATION =
      new MAPIAttribute(0x7e, BINARY, "Delegation", "PR_DELEGATION");
   public static final MAPIAttribute DELETE_AFTER_SUBMIT =
      new MAPIAttribute(0xe01, BOOLEAN, "DeleteAfterSubmit", "PR_DELETE_AFTER_SUBMIT");
   public static final MAPIAttribute DELIVER_TIME =
      new MAPIAttribute(0x10, TIME, "DeliverTime", "PR_DELIVER_TIME");
   public static final MAPIAttribute DELIVERY_POINT =
      new MAPIAttribute(0xc07, LONG, "DeliveryPoint", "PR_DELIVERY_POINT");
   public static final MAPIAttribute DELTAX =
      new MAPIAttribute(0x3f03, LONG, "Deltax", "PR_DELTAX");
   public static final MAPIAttribute DELTAY =
      new MAPIAttribute(0x3f04, LONG, "Deltay", "PR_DELTAY");
   public static final MAPIAttribute DEPARTMENT_NAME =
      new MAPIAttribute(0x3a18, ASCII_STRING, "DepartmentName", "PR_DEPARTMENT_NAME");
   public static final MAPIAttribute DEPTH =
      new MAPIAttribute(0x3005, LONG, "Depth", "PR_DEPTH");
   public static final MAPIAttribute DETAILS_TABLE =
      new MAPIAttribute(0x3605, DIRECTORY, "DetailsTable", "PR_DETAILS_TABLE");
   public static final MAPIAttribute DISC_VAL =
      new MAPIAttribute(0x4a, BOOLEAN, "DiscVal", "PR_DISC_VAL");
   public static final MAPIAttribute DISCARD_REASON =
      new MAPIAttribute(0x11, LONG, "DiscardReason", "PR_DISCARD_REASON");
   public static final MAPIAttribute DISCLOSE_RECIPIENTS =
      new MAPIAttribute(0x3a04, BOOLEAN, "DiscloseRecipients", "PR_DISCLOSE_RECIPIENTS");
   public static final MAPIAttribute DISCLOSURE_OF_RECIPIENTS =
      new MAPIAttribute(0x12, BOOLEAN, "DisclosureOfRecipients", "PR_DISCLOSURE_OF_RECIPIENTS");
   public static final MAPIAttribute DISCRETE_VALUES =
      new MAPIAttribute(0xe0e, BOOLEAN, "DiscreteValues", "PR_DISCRETE_VALUES");
   public static final MAPIAttribute DISPLAY_BCC =
      new MAPIAttribute(0xe02, ASCII_STRING, "DisplayBcc", "PR_DISPLAY_BCC");
   public static final MAPIAttribute DISPLAY_CC =
      new MAPIAttribute(0xe03, ASCII_STRING, "DisplayCc", "PR_DISPLAY_CC");
   public static final MAPIAttribute DISPLAY_NAME =
      new MAPIAttribute(0x3001, ASCII_STRING, "DisplayName", "PR_DISPLAY_NAME");
   public static final MAPIAttribute DISPLAY_NAME_PREFIX =
      new MAPIAttribute(0x3a45, ASCII_STRING, "DisplayNamePrefix", "PR_DISPLAY_NAME_PREFIX");
   public static final MAPIAttribute DISPLAY_TO =
      new MAPIAttribute(0xe04, ASCII_STRING, "DisplayTo", "PR_DISPLAY_TO");
   public static final MAPIAttribute DISPLAY_TYPE =
      new MAPIAttribute(0x3900, LONG, "DisplayType", "PR_DISPLAY_TYPE");
   public static final MAPIAttribute DL_EXPANSION_HISTORY =
      new MAPIAttribute(0x13, BINARY, "DlExpansionHistory", "PR_DL_EXPANSION_HISTORY");
   public static final MAPIAttribute DL_EXPANSION_PROHIBITED =
      new MAPIAttribute(20, BOOLEAN, "DlExpansionProhibited", "PR_DL_EXPANSION_PROHIBITED");
   public static final MAPIAttribute EMAIL_ADDRESS =
      new MAPIAttribute(0x3003, ASCII_STRING, "EmailAddress", "PR_EMAIL_ADDRESS");
   public static final MAPIAttribute END_DATE =
      new MAPIAttribute(0x61, TIME, "EndDate", "PR_END_DATE");
   public static final MAPIAttribute ENTRY_ID =
      new MAPIAttribute(0xfff, BINARY, "EntryId", "PR_ENTRYID");
   public static final MAPIAttribute EXPAND_BEGIN_TIME =
      new MAPIAttribute(0x3618, -1, "ExpandBeginTime", "PR_EXPAND_BEGIN_TIME");
   public static final MAPIAttribute EXPAND_END_TIME =
      new MAPIAttribute(0x3619, -1, "ExpandEndTime", "PR_EXPAND_END_TIME");
   public static final MAPIAttribute EXPANDED_BEGIN_TIME =
      new MAPIAttribute(0x361a, -1, "ExpandedBeginTime", "PR_EXPANDED_BEGIN_TIME");
   public static final MAPIAttribute EXPANDED_END_TIME =
      new MAPIAttribute(0x361b, -1, "ExpandedEndTime", "PR_EXPANDED_END_TIME");
   public static final MAPIAttribute EXPIRY_TIME =
      new MAPIAttribute(0x15, TIME, "ExpiryTime", "PR_EXPIRY_TIME");
   public static final MAPIAttribute EXPLICIT_CONVERSION =
      new MAPIAttribute(0xc01, LONG, "ExplicitConversion", "PR_EXPLICIT_CONVERSION");
   public static final MAPIAttribute FILTERING_HOOKS =
      new MAPIAttribute(0x3d08, BINARY, "FilteringHooks", "PR_FILTERING_HOOKS");
   public static final MAPIAttribute FINDER_ENTRY_ID =
      new MAPIAttribute(0x35e7, BINARY, "FinderEntryId", "PR_FINDER_ENTRYID");
   public static final MAPIAttribute FOLDER_ASSOCIATED_CONTENTS =
      new MAPIAttribute(0x3610, DIRECTORY, "FolderAssociatedContents", "PR_FOLDER_ASSOCIATED_CONTENTS");
   public static final MAPIAttribute FOLDER_TYPE =
      new MAPIAttribute(0x3601, LONG, "FolderType", "PR_FOLDER_TYPE");
   public static final MAPIAttribute FORM_CATEGORY =
      new MAPIAttribute(0x3304, ASCII_STRING, "FormCategory", "PR_FORM_CATEGORY");
   public static final MAPIAttribute FORM_CATEGORY_SUB =
      new MAPIAttribute(0x3305, ASCII_STRING, "FormCategorySub", "PR_FORM_CATEGORY_SUB");
   public static final MAPIAttribute FORM_CLSID =
      new MAPIAttribute(0x3302, 72, "FormClsid", "PR_FORM_ClsID");
   public static final MAPIAttribute FORM_CONTACT_NAME =
      new MAPIAttribute(0x3303, ASCII_STRING, "FormContactName", "PR_FORM_CONTACT_NAME");
   public static final MAPIAttribute FORM_DESIGNER_GUID =
      new MAPIAttribute(0x3309, 72, "FormDesignerGuid", "PR_FORM_DESIGNER_GUID");
   public static final MAPIAttribute FORM_DESIGNER_NAME =
      new MAPIAttribute(0x3308, ASCII_STRING, "FormDesignerName", "PR_FORM_DESIGNER_NAME");
   public static final MAPIAttribute FORM_HIDDEN =
      new MAPIAttribute(0x3307, BOOLEAN, "FormHidden", "PR_FORM_HIDDEN");
   public static final MAPIAttribute FORM_HOST_MAP =
      new MAPIAttribute(0x3306, 4099, "FormHostMap", "PR_FORM_HOST_MAP");
   public static final MAPIAttribute FORM_MESSAGE_BEHAVIOR =
      new MAPIAttribute(0x330a, LONG, "FormMessageBehavior", "PR_FORM_MESSAGE_BEHAVIOR");
   public static final MAPIAttribute FORM_VERSION =
      new MAPIAttribute(0x3301, ASCII_STRING, "FormVersion", "PR_FORM_VERSION");
   public static final MAPIAttribute FTP_SITE =
      new MAPIAttribute(0x3a4c, ASCII_STRING, "FtpSite", "PR_FTP_SITE");
   public static final MAPIAttribute GENDER =
      new MAPIAttribute(0x3a4d, 2, "Gender", "PR_GENDER");
   public static final MAPIAttribute GENERATION =
      new MAPIAttribute(0x3a05, ASCII_STRING, "Generation", "PR_GENERATION");
   public static final MAPIAttribute GIVEN_NAME =
      new MAPIAttribute(0x3a06, ASCII_STRING, "GivenName", "PR_GIVEN_NAME");
   public static final MAPIAttribute GOVERNMENT_ID_NUMBER =
      new MAPIAttribute(0x3a07, ASCII_STRING, "GovernmentIdNumber", "PR_GOVERNMENT_ID_NUMBER");
   public static final MAPIAttribute HASATTACH =
      new MAPIAttribute(0xe1b, BOOLEAN, "Hasattach", "PR_HASATTACH");
   public static final MAPIAttribute HEADER_FOLDER_ENTRY_ID =
      new MAPIAttribute(0x3e0a, BINARY, "HeaderFolderEntryId", "PR_HEADER_FOLDER_ENTRYID");
   public static final MAPIAttribute HOBBIES =
      new MAPIAttribute(0x3a43, ASCII_STRING, "Hobbies", "PR_HOBBIES");
   public static final MAPIAttribute HOME2_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a2f, ASCII_STRING, "Home2TelephoneNumber", "PR_HOME2_TELEPHONE_NUMBER");
   public static final MAPIAttribute HOME_ADDRESS_CITY =
      new MAPIAttribute(0x3a59, ASCII_STRING, "HomeAddressCity", "PR_HOME_ADDRESS_CITY");
   public static final MAPIAttribute HOME_ADDRESS_COUNTRY =
      new MAPIAttribute(0x3a5a, ASCII_STRING, "HomeAddressCountry", "PR_HOME_ADDRESS_COUNTRY");
   public static final MAPIAttribute HOME_ADDRESS_POST_OFFICE_BOX =
      new MAPIAttribute(0x3a5e, ASCII_STRING, "HomeAddressPostOfficeBox", "PR_HOME_ADDRESS_POST_OFFICE_BOX");
   public static final MAPIAttribute HOME_ADDRESS_POSTAL_CODE =
      new MAPIAttribute(0x3a5b, ASCII_STRING, "HomeAddressPostalCode", "PR_HOME_ADDRESS_POSTAL_CODE");
   public static final MAPIAttribute HOME_ADDRESS_STATE_OR_PROVINCE =
      new MAPIAttribute(0x3a5c, ASCII_STRING, "HomeAddressStateOrProvince", "PR_HOME_ADDRESS_STATE_OR_PROVINCE");
   public static final MAPIAttribute HOME_ADDRESS_STREET =
      new MAPIAttribute(0x3a5d, ASCII_STRING, "HomeAddressStreet", "PR_HOME_ADDRESS_STREET");
   public static final MAPIAttribute HOME_FAX_NUMBER =
      new MAPIAttribute(0x3a25, ASCII_STRING, "HomeFaxNumber", "PR_HOME_FAX_NUMBER");
   public static final MAPIAttribute HOME_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a09, ASCII_STRING, "HomeTelephoneNumber", "PR_HOME_TELEPHONE_NUMBER");
   public static final MAPIAttribute INET_MAIL_OVERRIDE_CHARSET =
      new MAPIAttribute(0x5903, -1, "INetMailOverrideCharset", "Charset");
   public static final MAPIAttribute INET_MAIL_OVERRIDE_FORMAT =
      new MAPIAttribute(0x5902, -1, "INetMailOverrideFormat", "Format");
   public static final MAPIAttribute ICON =
      new MAPIAttribute(0xffd, BINARY, "Icon", "PR_ICON");
   public static final MAPIAttribute IDENTITY_DISPLAY =
      new MAPIAttribute(0x3e00, ASCII_STRING, "IdentityDisplay", "PR_IDENTITY_DISPLAY");
   public static final MAPIAttribute IDENTITY_ENTRY_ID =
      new MAPIAttribute(0x3e01, BINARY, "IdentityEntryId", "PR_IDENTITY_ENTRYID");
   public static final MAPIAttribute IDENTITY_SEARCH_KEY =
      new MAPIAttribute(0x3e05, BINARY, "IdentitySearchKey", "PR_IDENTITY_SEARCH_KEY");
   public static final MAPIAttribute IMPLICIT_CONVERSION_PROHIBITED =
      new MAPIAttribute(0x16, BOOLEAN, "ImplicitConversionProhibited", "PR_IMPLICIT_CONVERSION_PROHIBITED");
   public static final MAPIAttribute IMPORTANCE =
      new MAPIAttribute(0x17, LONG, "Importance", "PR_IMPORTANCE");
   public static final MAPIAttribute IN_REPLY_TO_ID =
      new MAPIAttribute(0x1042, -1, "InReplyToId", "PR_IN_REPLY_TO_ID");
   public static final MAPIAttribute INCOMPLETE_COPY =
      new MAPIAttribute(0x35, BOOLEAN, "IncompleteCopy", "PR_INCOMPLETE_COPY");
   public static final MAPIAttribute INITIAL_DETAILS_PANE =
      new MAPIAttribute(0x3f08, LONG, "InitialDetailsPane", "PR_INITIAL_DETAILS_PANE");
   public static final MAPIAttribute INITIALS =
      new MAPIAttribute(0x3a0a, ASCII_STRING, "Initials", "PR_INITIALS");
   public static final MAPIAttribute INSTANCE_KEY =
      new MAPIAttribute(0xff6, BINARY, "InstanceKey", "PR_INSTANCE_KEY");
   public static final MAPIAttribute INTERNET_APPROVED =
      new MAPIAttribute(0x1030, ASCII_STRING, "InternetApproved", "PR_INTERNET_APPROVED");
   public static final MAPIAttribute INTERNET_ARTICLE_NUMBER =
      new MAPIAttribute(0xe23, LONG, "InternetArticleNumber", "PR_INTERNET_ARTICLE_NUMBER");
   public static final MAPIAttribute INTERNET_CPID =
      new MAPIAttribute(0x3fde, -1, "InternetCPID", "PR_INTERNET_CPID");
   public static final MAPIAttribute INTERNET_CONTROL =
      new MAPIAttribute(0x1031, ASCII_STRING, "InternetControl", "PR_INTERNET_CONTROL");
   public static final MAPIAttribute INTERNET_DISTRIBUTION =
      new MAPIAttribute(0x1032, ASCII_STRING, "InternetDistribution", "PR_INTERNET_DISTRIBUTION");
   public static final MAPIAttribute INTERNET_FOLLOWUP_TO =
      new MAPIAttribute(0x1033, ASCII_STRING, "InternetFollowupTo", "PR_INTERNET_FOLLOWUP_TO");
   public static final MAPIAttribute INTERNET_LINES =
      new MAPIAttribute(0x1034, LONG, "InternetLines", "PR_INTERNET_LINES");
   public static final MAPIAttribute INTERNET_MESSAGE_ID =
      new MAPIAttribute(0x1035, ASCII_STRING, "InternetMessageId", "PR_INTERNET_MESSAGE_ID");
   public static final MAPIAttribute INTERNET_NEWSGROUPS =
      new MAPIAttribute(0x1036, ASCII_STRING, "InternetNewsgroups", "PR_INTERNET_NEWSGROUPS");
   public static final MAPIAttribute INTERNET_NNTP_PATH =
      new MAPIAttribute(0x1038, ASCII_STRING, "InternetNntpPath", "PR_INTERNET_NNTP_PATH");
   public static final MAPIAttribute INTERNET_ORGANIZATION =
      new MAPIAttribute(0x1037, ASCII_STRING, "InternetOrganization", "PR_INTERNET_ORGANIZATION");
   public static final MAPIAttribute INTERNET_PRECEDENCE =
      new MAPIAttribute(0x1041, ASCII_STRING, "InternetPrecedence", "PR_INTERNET_PRECEDENCE");
   public static final MAPIAttribute INTERNET_REFERENCES =
      new MAPIAttribute(0x1039, ASCII_STRING, "InternetReferences", "PR_INTERNET_REFERENCES");
   public static final MAPIAttribute IPM_ID =
      new MAPIAttribute(0x18, BINARY, "IpmId", "PR_IPM_ID");
   public static final MAPIAttribute IPM_OUTBOX_ENTRY_ID =
      new MAPIAttribute(0x35e2, BINARY, "IpmOutboxEntryId", "PR_IPM_OUTBOX_ENTRYID");
   public static final MAPIAttribute IPM_OUTBOX_SEARCH_KEY =
      new MAPIAttribute(0x3411, BINARY, "IpmOutboxSearchKey", "PR_IPM_OUTBOX_SEARCH_KEY");
   public static final MAPIAttribute IPM_RETURN_REQUESTED =
      new MAPIAttribute(0xc02, BOOLEAN, "IpmReturnRequested", "PR_IPM_RETURN_REQUESTED");
   public static final MAPIAttribute IPM_SENTMAIL_ENTRY_ID =
      new MAPIAttribute(0x35e4, BINARY, "IpmSentmailEntryId", "PR_IPM_SENTMAIL_ENTRYID");
   public static final MAPIAttribute IPM_SENTMAIL_SEARCH_KEY =
      new MAPIAttribute(0x3413, BINARY, "IpmSentmailSearchKey", "PR_IPM_SENTMAIL_SEARCH_KEY");
   public static final MAPIAttribute IPM_SUBTREE_ENTRY_ID =
      new MAPIAttribute(0x35e0, BINARY, "IpmSubtreeEntryId", "PR_IPM_SUBTREE_ENTRYID");
   public static final MAPIAttribute IPM_SUBTREE_SEARCH_KEY =
      new MAPIAttribute(0x3410, BINARY, "IpmSubtreeSearchKey", "PR_IPM_SUBTREE_SEARCH_KEY");
   public static final MAPIAttribute IPM_WASTEBASKET_ENTRY_ID =
      new MAPIAttribute(0x35e3, BINARY, "IpmWastebasketEntryId", "PR_IPM_WASTEBASKET_ENTRYID");
   public static final MAPIAttribute IPM_WASTEBASKET_SEARCH_KEY =
      new MAPIAttribute(0x3412, BINARY, "IpmWastebasketSearchKey", "PR_IPM_WASTEBASKET_SEARCH_KEY");
   public static final MAPIAttribute ISDN_NUMBER =
      new MAPIAttribute(0x3a2d, ASCII_STRING, "IsdnNumber", "PR_ISDN_NUMBER");
   public static final MAPIAttribute KEYWORD =
      new MAPIAttribute(0x3a0b, ASCII_STRING, "Keyword", "PR_KEYWORD");
   public static final MAPIAttribute LANGUAGE =
      new MAPIAttribute(0x3a0c, ASCII_STRING, "Language", "PR_LANGUAGE");
   public static final MAPIAttribute LANGUAGES =
      new MAPIAttribute(0x2f, ASCII_STRING, "Languages", "PR_LANGUAGES");
   public static final MAPIAttribute LAST_MODIFICATION_TIME =
      new MAPIAttribute(0x3008, TIME, "LastModificationTime", "PR_LAST_MODIFICATION_TIME");
   public static final MAPIAttribute LATEST_DELIVERY_TIME =
      new MAPIAttribute(0x19, TIME, "LatestDeliveryTime", "PR_LATEST_DELIVERY_TIME");
   public static final MAPIAttribute LIST_HELP =
      new MAPIAttribute(0x1043, -1, "ListHelp", "PR_LIST_HELP");
   public static final MAPIAttribute LIST_SUBSCRIBE =
      new MAPIAttribute(0x1044, -1, "ListSubscribe", "PR_LIST_SUBSCRIBE");
   public static final MAPIAttribute LIST_UNSUBSCRIBE =
      new MAPIAttribute(0x1045, -1, "ListUnsubscribe", "PR_LIST_UNSUBSCRIBE");
   public static final MAPIAttribute LOCALITY =
      new MAPIAttribute(0x3a27, ASCII_STRING, "Locality", "PR_LOCALITY");
   public static final MAPIAttribute LOCALLY_DELIVERED =
      new MAPIAttribute(0x6745, -1, "LocallyDelivered", "ptagLocallyDelivered");
   public static final MAPIAttribute LOCATION =
      new MAPIAttribute(0x3a0d, ASCII_STRING, "Location", "PR_LOCATION");
   public static final MAPIAttribute LOCK_BRANCH_ID =
      new MAPIAttribute(0x3800, -1, "LockBranchId", "PR_LOCK_BRANCH_ID");
   public static final MAPIAttribute LOCK_DEPTH =
      new MAPIAttribute(0x3808, -1, "LockDepth", "PR_LOCK_DEPTH");
   public static final MAPIAttribute LOCK_ENLISTMENT_CONTEXT =
      new MAPIAttribute(0x3804, -1, "LockEnlistmentContext", "PR_LOCK_ENLISTMENT_CONTEXT");
   public static final MAPIAttribute LOCK_EXPIRY_TIME =
      new MAPIAttribute(0x380a, -1, "LockExpiryTime", "PR_LOCK_EXPIRY_TIME");
   public static final MAPIAttribute LOCK_PERSISTENT =
      new MAPIAttribute(0x3807, -1, "LockPersistent", "PR_LOCK_PERSISTENT");
   public static final MAPIAttribute LOCK_RESOURCE_DID =
      new MAPIAttribute(0x3802, -1, "LockResourceDid", "PR_LOCK_RESOURCE_DID");
   public static final MAPIAttribute LOCK_RESOURCE_FID =
      new MAPIAttribute(0x3801, -1, "LockResourceFid", "PR_LOCK_RESOURCE_FID");
   public static final MAPIAttribute LOCK_RESOURCE_MID =
      new MAPIAttribute(0x3803, -1, "LockResourceMid", "PR_LOCK_RESOURCE_MID");
   public static final MAPIAttribute LOCK_SCOPE =
      new MAPIAttribute(0x3806, -1, "LockScope", "PR_LOCK_SCOPE");
   public static final MAPIAttribute LOCK_TIMEOUT =
      new MAPIAttribute(0x3809, -1, "LockTimeout", "PR_LOCK_TIMEOUT");
   public static final MAPIAttribute LOCK_TYPE =
      new MAPIAttribute(0x3805, -1, "LockType", "PR_LOCK_TYPE");
   public static final MAPIAttribute MAIL_PERMISSION =
      new MAPIAttribute(0x3a0e, BOOLEAN, "MailPermission", "PR_MAIL_PERMISSION");
   public static final MAPIAttribute MANAGER_NAME =
      new MAPIAttribute(0x3a4e, ASCII_STRING, "ManagerName", "PR_MANAGER_NAME");
   public static final MAPIAttribute MAPPING_SIGNATURE =
      new MAPIAttribute(0xff8, BINARY, "MappingSignature", "PR_MAPPING_SIGNATURE");
   public static final MAPIAttribute MDB_PROVIDER =
      new MAPIAttribute(0x3414, BINARY, "MdbProvider", "PR_MDB_PROVIDER");
   public static final MAPIAttribute MESSAGE_ATTACHMENTS =
      new MAPIAttribute(0xe13, DIRECTORY, "MessageAttachments", "PR_MESSAGE_ATTACHMENTS");
   public static final MAPIAttribute MESSAGE_CC_ME =
      new MAPIAttribute(0x58, BOOLEAN, "MessageCcMe", "PR_MESSAGE_CC_ME");
   public static final MAPIAttribute MESSAGE_CLASS =
      new MAPIAttribute(0x1a, ASCII_STRING, "MessageClass", "PR_MESSAGE_CLASS");
   public static final MAPIAttribute MESSAGE_CODEPAGE =
      new MAPIAttribute(0x3ffd, -1, "MessageCodepage", "PR_MESSAGE_CODEPAGE");
   public static final MAPIAttribute MESSAGE_DELIVERY_ID =
      new MAPIAttribute(0x1b, BINARY, "MessageDeliveryId", "PR_MESSAGE_DELIVERY_ID");
   public static final MAPIAttribute MESSAGE_DELIVERY_TIME =
      new MAPIAttribute(0xe06, TIME, "MessageDeliveryTime", "PR_MESSAGE_DELIVERY_TIME");
   public static final MAPIAttribute MESSAGE_DOWNLOAD_TIME =
      new MAPIAttribute(0xe18, LONG, "MessageDownloadTime", "PR_MESSAGE_DOWNLOAD_TIME");
   public static final MAPIAttribute MESSAGE_FLAGS =
      new MAPIAttribute(0xe07, LONG, "MessageFlags", "PR_MESSAGE_FLAGS");
   public static final MAPIAttribute MESSAGE_RECIP_ME =
      new MAPIAttribute(0x59, BOOLEAN, "MessageRecipMe", "PR_MESSAGE_RECIP_ME");
   public static final MAPIAttribute MESSAGE_RECIPIENTS =
      new MAPIAttribute(0xe12, DIRECTORY, "MessageRecipients", "PR_MESSAGE_RECIPIENTS");
   public static final MAPIAttribute MESSAGE_SECURITY_LABEL =
      new MAPIAttribute(30, BINARY, "MessageSecurityLabel", "PR_MESSAGE_SECURITY_LABEL");
   public static final MAPIAttribute MESSAGE_SIZE =
      new MAPIAttribute(0xe08, LONG, "MessageSize", "PR_MESSAGE_SIZE");
   public static final MAPIAttribute MESSAGE_SUBMISSION_ID =
      new MAPIAttribute(0x47, BINARY, "MessageSubmissionId", "PR_MESSAGE_SUBMISSION_ID");
   public static final MAPIAttribute MESSAGE_TO_ME =
      new MAPIAttribute(0x57, BOOLEAN, "MessageToMe", "PR_MESSAGE_TO_ME");
   public static final MAPIAttribute MESSAGE_TOKEN =
      new MAPIAttribute(0xc03, BINARY, "MessageToken", "PR_MESSAGE_TOKEN");
   public static final MAPIAttribute MHS_COMMON_NAME =
      new MAPIAttribute(0x3a0f, ASCII_STRING, "MhsCommonName", "PR_MHS_COMMON_NAME");
   public static final MAPIAttribute MIDDLE_NAME =
      new MAPIAttribute(0x3a44, ASCII_STRING, "MiddleName", "PR_MIDDLE_NAME");
   public static final MAPIAttribute MINI_ICON =
      new MAPIAttribute(0xffc, BINARY, "MiniIcon", "PR_MINI_ICON");
   public static final MAPIAttribute MOBILE_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1c, ASCII_STRING, "MobileTelephoneNumber", "PR_MOBILE_TELEPHONE_NUMBER");
   public static final MAPIAttribute MODIFY_VERSION =
      new MAPIAttribute(0xe1a, 20, "ModifyVersion", "PR_MODIFY_VERSION");
   public static final MAPIAttribute MSG_STATUS =
      new MAPIAttribute(0xe17, LONG, "MsgStatus", "PR_MSG_STATUS");
   public static final MAPIAttribute NDR_DIAG_CODE =
      new MAPIAttribute(0xc05, LONG, "NdrDiagCode", "PR_NDR_DIAG_CODE");
   public static final MAPIAttribute NDR_REASON_CODE =
      new MAPIAttribute(0xc04, LONG, "NdrReasonCode", "PR_NDR_REASON_CODE");
   public static final MAPIAttribute NDR_STATUS_CODE =
      new MAPIAttribute(0xc20, -1, "NdrStatusCode", "PR_NDR_STATUS_CODE");
   public static final MAPIAttribute NEWSGROUP_NAME =
      new MAPIAttribute(0xe24, ASCII_STRING, "NewsgroupName", "PR_NEWSGROUP_NAME");
   public static final MAPIAttribute NICKNAME =
      new MAPIAttribute(0x3a4f, ASCII_STRING, "Nickname", "PR_NICKNAME");
   public static final MAPIAttribute NNTP_XREF =
      new MAPIAttribute(0x1040, ASCII_STRING, "NntpXref", "PR_NNTP_XREF");
   public static final MAPIAttribute NON_RECEIPT_NOTIFICATION_REQUESTED =
      new MAPIAttribute(0xc06, BOOLEAN, "NonReceiptNotificationRequested", "PR_NON_RECEIPT_NOTIFICATION_REQUESTED");
   public static final MAPIAttribute NON_RECEIPT_REASON =
      new MAPIAttribute(0x3e, LONG, "NonReceiptReason", "PR_NON_RECEIPT_REASON");
   public static final MAPIAttribute NORMALIZED_SUBJECT =
      new MAPIAttribute(0xe1d, ASCII_STRING, "NormalizedSubject", "PR_NORMALIZED_SUBJECT");
   public static final MAPIAttribute NT_SECURITY_DESCRIPTOR =
      new MAPIAttribute(0xe27, -1, "NtSecurityDescriptor", "PR_NT_SECURITY_DESCRIPTOR");
   public static final MAPIAttribute NULL =
      new MAPIAttribute(1, LONG, "Null", "PR_NULL");
   public static final MAPIAttribute OBJECT_TYPE =
      new MAPIAttribute(0xffe, LONG, "ObjectType", "PR_Object_TYPE");
   public static final MAPIAttribute OBSOLETED_IPMS =
      new MAPIAttribute(0x1f, BINARY, "ObsoletedIpms", "PR_OBSOLETED_IPMS");
   public static final MAPIAttribute OFFICE2_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1b, ASCII_STRING, "Office2TelephoneNumber", "PR_OFFICE2_TELEPHONE_NUMBER");
   public static final MAPIAttribute OFFICE_LOCATION =
      new MAPIAttribute(0x3a19, ASCII_STRING, "OfficeLocation", "PR_OFFICE_LOCATION");
   public static final MAPIAttribute OFFICE_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a08, ASCII_STRING, "OfficeTelephoneNumber", "PR_OFFICE_TELEPHONE_NUMBER");
   public static final MAPIAttribute OOF_REPLY_TYPE =
      new MAPIAttribute(0x4080, -1, "OofReplyType", "PR_OOF_REPLY_TYPE");
   public static final MAPIAttribute ORGANIZATIONAL_ID_NUMBER =
      new MAPIAttribute(0x3a10, ASCII_STRING, "OrganizationalIdNumber", "PR_ORGANIZATIONAL_ID_NUMBER");
   public static final MAPIAttribute ORIG_ENTRY_ID =
      new MAPIAttribute(0x300f, -1, "OrigEntryId", "PR_ORIG_ENTRYID");
   public static final MAPIAttribute ORIG_MESSAGE_CLASS =
      new MAPIAttribute(0x4b, ASCII_STRING, "OrigMessageClass", "PR_ORIG_MESSAGE_CLASS");
   public static final MAPIAttribute ORIGIN_CHECK =
      new MAPIAttribute(0x27, BINARY, "OriginCheck", "PR_ORIGIN_CHECK");
   public static final MAPIAttribute ORIGINAL_AUTHOR_ADDRTYPE =
      new MAPIAttribute(0x79, ASCII_STRING, "OriginalAuthorAddrtype", "PR_ORIGINAL_AUTHOR_ADDRTYPE");
   public static final MAPIAttribute ORIGINAL_AUTHOR_EMAIL_ADDRESS =
      new MAPIAttribute(0x7a, ASCII_STRING, "OriginalAuthorEmailAddress", "PR_ORIGINAL_AUTHOR_EMAIL_ADDRESS");
   public static final MAPIAttribute ORIGINAL_AUTHOR_ENTRY_ID =
      new MAPIAttribute(0x4c, BINARY, "OriginalAuthorEntryId", "PR_ORIGINAL_AUTHOR_ENTRYID");
   public static final MAPIAttribute ORIGINAL_AUTHOR_NAME =
      new MAPIAttribute(0x4d, ASCII_STRING, "OriginalAuthorName", "PR_ORIGINAL_AUTHOR_NAME");
   public static final MAPIAttribute ORIGINAL_AUTHOR_SEARCH_KEY =
      new MAPIAttribute(0x56, BINARY, "OriginalAuthorSearchKey", "PR_ORIGINAL_AUTHOR_SEARCH_KEY");
   public static final MAPIAttribute ORIGINAL_DELIVERY_TIME =
      new MAPIAttribute(0x55, TIME, "OriginalDeliveryTime", "PR_ORIGINAL_DELIVERY_TIME");
   public static final MAPIAttribute ORIGINAL_DISPLAY_BCC =
      new MAPIAttribute(0x72, ASCII_STRING, "OriginalDisplayBcc", "PR_ORIGINAL_DISPLAY_BCC");
   public static final MAPIAttribute ORIGINAL_DISPLAY_CC =
      new MAPIAttribute(0x73, ASCII_STRING, "OriginalDisplayCc", "PR_ORIGINAL_DISPLAY_CC");
   public static final MAPIAttribute ORIGINAL_DISPLAY_NAME =
      new MAPIAttribute(0x3a13, ASCII_STRING, "OriginalDisplayName", "PR_ORIGINAL_DISPLAY_NAME");
   public static final MAPIAttribute ORIGINAL_DISPLAY_TO =
      new MAPIAttribute(0x74, ASCII_STRING, "OriginalDisplayTo", "PR_ORIGINAL_DISPLAY_TO");
   public static final MAPIAttribute ORIGINAL_EITS =
      new MAPIAttribute(0x21, BINARY, "OriginalEits", "PR_ORIGINAL_EITS");
   public static final MAPIAttribute ORIGINAL_ENTRY_ID =
      new MAPIAttribute(0x3a12, BINARY, "OriginalEntryId", "PR_ORIGINAL_ENTRYID");
   public static final MAPIAttribute ORIGINAL_SEARCH_KEY =
      new MAPIAttribute(0x3a14, BINARY, "OriginalSearchKey", "PR_ORIGINAL_SEARCH_KEY");
   public static final MAPIAttribute ORIGINAL_SENDER_ADDRTYPE =
      new MAPIAttribute(0x66, ASCII_STRING, "OriginalSenderAddrtype", "PR_ORIGINAL_SENDER_ADDRTYPE");
   public static final MAPIAttribute ORIGINAL_SENDER_EMAIL_ADDRESS =
      new MAPIAttribute(0x67, ASCII_STRING, "OriginalSenderEmailAddress", "PR_ORIGINAL_SENDER_EMAIL_ADDRESS");
   public static final MAPIAttribute ORIGINAL_SENDER_ENTRY_ID =
      new MAPIAttribute(0x5b, BINARY, "OriginalSenderEntryId", "PR_ORIGINAL_SENDER_ENTRYID");
   public static final MAPIAttribute ORIGINAL_SENDER_NAME =
      new MAPIAttribute(90, ASCII_STRING, "OriginalSenderName", "PR_ORIGINAL_SENDER_NAME");
   public static final MAPIAttribute ORIGINAL_SENDER_SEARCH_KEY =
      new MAPIAttribute(0x5c, BINARY, "OriginalSenderSearchKey", "PR_ORIGINAL_SENDER_SEARCH_KEY");
   public static final MAPIAttribute ORIGINAL_SENSITIVITY =
      new MAPIAttribute(0x2e, LONG, "OriginalSensitivity", "PR_ORIGINAL_SENSITIVITY");
   public static final MAPIAttribute ORIGINAL_SENT_REPRESENTING_ADDRTYPE =
      new MAPIAttribute(0x68, ASCII_STRING, "OriginalSentRepresentingAddrtype", "PR_ORIGINAL_SENT_REPRESENTING_ADDRTYPE");
   public static final MAPIAttribute ORIGINAL_SENT_REPRESENTING_EMAIL_ADDRESS =
      new MAPIAttribute(0x69, ASCII_STRING, "OriginalSentRepresentingEmailAddress", "PR_ORIGINAL_SENT_REPRESENTING_EMAIL_ADDRESS");
   public static final MAPIAttribute ORIGINAL_SENT_REPRESENTING_ENTRY_ID =
      new MAPIAttribute(0x5e, BINARY, "OriginalSentRepresentingEntryId", "PR_ORIGINAL_SENT_REPRESENTING_ENTRYID");
   public static final MAPIAttribute ORIGINAL_SENT_REPRESENTING_NAME =
      new MAPIAttribute(0x5d, ASCII_STRING, "OriginalSentRepresentingName", "PR_ORIGINAL_SENT_REPRESENTING_NAME");
   public static final MAPIAttribute ORIGINAL_SENT_REPRESENTING_SEARCH_KEY =
      new MAPIAttribute(0x5f, BINARY, "OriginalSentRepresentingSearchKey", "PR_ORIGINAL_SENT_REPRESENTING_SEARCH_KEY");
   public static final MAPIAttribute ORIGINAL_SUBJECT =
      new MAPIAttribute(0x49, ASCII_STRING, "OriginalSubject", "PR_ORIGINAL_SUBJECT");
   public static final MAPIAttribute ORIGINAL_SUBMIT_TIME =
      new MAPIAttribute(0x4e, TIME, "OriginalSubmitTime", "PR_ORIGINAL_SUBMIT_TIME");
   public static final MAPIAttribute ORIGINALLY_INTENDED_RECIP_ADDRTYPE =
      new MAPIAttribute(0x7b, ASCII_STRING, "OriginallyIntendedRecipAddrtype", "PR_ORIGINALLY_INTENDED_RECIP_ADDRTYPE");
   public static final MAPIAttribute ORIGINALLY_INTENDED_RECIP_EMAIL_ADDRESS =
      new MAPIAttribute(0x7c, ASCII_STRING, "OriginallyIntendedRecipEmailAddress", "PR_ORIGINALLY_INTENDED_RECIP_EMAIL_ADDRESS");
   public static final MAPIAttribute ORIGINALLY_INTENDED_RECIP_ENTRY_ID =
      new MAPIAttribute(0x1012, BINARY, "OriginallyIntendedRecipEntryId", "PR_ORIGINALLY_INTENDED_RECIP_ENTRYID");
   public static final MAPIAttribute ORIGINALLY_INTENDED_RECIPIENT_NAME =
      new MAPIAttribute(0x20, BINARY, "OriginallyIntendedRecipientName", "PR_ORIGINALLY_INTENDED_RECIPIENT_NAME");
   public static final MAPIAttribute ORIGINATING_MTA_CERTIFICATE =
      new MAPIAttribute(0xe25, BINARY, "OriginatingMtaCertificate", "PR_ORIGINATING_MTA_CERTIFICATE");
   public static final MAPIAttribute ORIGINATOR_AND_DL_EXPANSION_HISTORY =
      new MAPIAttribute(0x1002, BINARY, "OriginatorAndDlExpansionHistory", "PR_ORIGINATOR_AND_DL_EXPANSION_HISTORY");
   public static final MAPIAttribute ORIGINATOR_CERTIFICATE =
      new MAPIAttribute(0x22, BINARY, "OriginatorCertificate", "PR_ORIGINATOR_CERTIFICATE");
   public static final MAPIAttribute ORIGINATOR_DELIVERY_REPORT_REQUESTED =
      new MAPIAttribute(0x23, BOOLEAN, "OriginatorDeliveryReportRequested", "PR_ORIGINATOR_DELIVERY_REPORT_REQUESTED");
   public static final MAPIAttribute ORIGINATOR_NON_DELIVERY_REPORT_REQUESTED =
      new MAPIAttribute(0xc08, BOOLEAN, "OriginatorNonDeliveryReportRequested", "PR_ORIGINATOR_NON_DELIVERY_REPORT_REQUESTED");
   public static final MAPIAttribute ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT =
      new MAPIAttribute(0xc09, BINARY, "OriginatorRequestedAlternateRecipient", "PR_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT");
   public static final MAPIAttribute ORIGINATOR_RETURN_ADDRESS =
      new MAPIAttribute(0x24, BINARY, "OriginatorReturnAddress", "PR_ORIGINATOR_RETURN_ADDRESS");
   public static final MAPIAttribute OTHER_ADDRESS_CITY =
      new MAPIAttribute(0x3a5f, ASCII_STRING, "OtherAddressCity", "PR_OTHER_ADDRESS_CITY");
   public static final MAPIAttribute OTHER_ADDRESS_COUNTRY =
      new MAPIAttribute(0x3a60, ASCII_STRING, "OtherAddressCountry", "PR_OTHER_ADDRESS_COUNTRY");
   public static final MAPIAttribute OTHER_ADDRESS_POST_OFFICE_BOX =
      new MAPIAttribute(0x3a64, ASCII_STRING, "OtherAddressPostOfficeBox", "PR_OTHER_ADDRESS_POST_OFFICE_BOX");
   public static final MAPIAttribute OTHER_ADDRESS_POSTAL_CODE =
      new MAPIAttribute(0x3a61, ASCII_STRING, "OtherAddressPostalCode", "PR_OTHER_ADDRESS_POSTAL_CODE");
   public static final MAPIAttribute OTHER_ADDRESS_STATE_OR_PROVINCE =
      new MAPIAttribute(0x3a62, ASCII_STRING, "OtherAddressStateOrProvince", "PR_OTHER_ADDRESS_STATE_OR_PROVINCE");
   public static final MAPIAttribute OTHER_ADDRESS_STREET =
      new MAPIAttribute(0x3a63, ASCII_STRING, "OtherAddressStreet", "PR_OTHER_ADDRESS_STREET");
   public static final MAPIAttribute OTHER_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1f, ASCII_STRING, "OtherTelephoneNumber", "PR_OTHER_TELEPHONE_NUMBER");
   public static final MAPIAttribute OWN_STORE_ENTRY_ID =
      new MAPIAttribute(0x3e06, BINARY, "OwnStoreEntryId", "PR_OWN_STORE_ENTRYID");
   public static final MAPIAttribute OWNER_APPT_ID =
      new MAPIAttribute(0x62, LONG, "OwnerApptId", "PR_OWNER_APPT_ID");
   public static final MAPIAttribute PAGER_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a21, ASCII_STRING, "PagerTelephoneNumber", "PR_PAGER_TELEPHONE_NUMBER");
   public static final MAPIAttribute PARENT_DISPLAY =
      new MAPIAttribute(0xe05, ASCII_STRING, "ParentDisplay", "PR_PARENT_DISPLAY");
   public static final MAPIAttribute PARENT_ENTRY_ID =
      new MAPIAttribute(0xe09, BINARY, "ParentEntryId", "PR_PARENT_ENTRYID");
   public static final MAPIAttribute PARENT_KEY =
      new MAPIAttribute(0x25, BINARY, "ParentKey", "PR_PARENT_KEY");
   public static final MAPIAttribute PERSONAL_HOME_PAGE =
      new MAPIAttribute(0x3a50, ASCII_STRING, "PersonalHomePage", "PR_PERSONAL_HOME_PAGE");
   public static final MAPIAttribute PHYSICAL_DELIVERY_BUREAU_FAX_DELIVERY =
      new MAPIAttribute(0xc0a, BOOLEAN, "PhysicalDeliveryBureauFaxDelivery", "PR_PHYSICAL_DELIVERY_BUREAU_FAX_DELIVERY");
   public static final MAPIAttribute PHYSICAL_DELIVERY_MODE =
      new MAPIAttribute(0xc0b, LONG, "PhysicalDeliveryMode", "PR_PHYSICAL_DELIVERY_MODE");
   public static final MAPIAttribute PHYSICAL_DELIVERY_REPORT_REQUEST =
      new MAPIAttribute(0xc0c, LONG, "PhysicalDeliveryReportRequest", "PR_PHYSICAL_DELIVERY_REPORT_REQUEST");
   public static final MAPIAttribute PHYSICAL_FORWARDING_ADDRESS =
      new MAPIAttribute(0xc0d, BINARY, "PhysicalForwardingAddress", "PR_PHYSICAL_FORWARDING_ADDRESS");
   public static final MAPIAttribute PHYSICAL_FORWARDING_ADDRESS_REQUESTED =
      new MAPIAttribute(0xc0e, BOOLEAN, "PhysicalForwardingAddressRequested", "PR_PHYSICAL_FORWARDING_ADDRESS_REQUESTED");
   public static final MAPIAttribute PHYSICAL_FORWARDING_PROHIBITED =
      new MAPIAttribute(0xc0f, BOOLEAN, "PhysicalForwardingProhibited", "PR_PHYSICAL_FORWARDING_PROHIBITED");
   public static final MAPIAttribute PHYSICAL_RENDITION_ATTRIBUTES =
      new MAPIAttribute(0xc10, BINARY, "PhysicalRenditionAttributes", "PR_PHYSICAL_RENDITION_ATTRIBUTES");
   public static final MAPIAttribute POST_FOLDER_ENTRIES =
      new MAPIAttribute(0x103b, BINARY, "PostFolderEntries", "PR_POST_FOLDER_ENTRIES");
   public static final MAPIAttribute POST_FOLDER_NAMES =
      new MAPIAttribute(0x103c, ASCII_STRING, "PostFolderNames", "PR_POST_FOLDER_NAMES");
   public static final MAPIAttribute POST_OFFICE_BOX =
      new MAPIAttribute(0x3a2b, ASCII_STRING, "PostOfficeBox", "PR_POST_OFFICE_BOX");
   public static final MAPIAttribute POST_REPLY_DENIED =
      new MAPIAttribute(0x103f, BINARY, "PostReplyDenied", "PR_POST_REPLY_DENIED");
   public static final MAPIAttribute POST_REPLY_FOLDER_ENTRIES =
      new MAPIAttribute(0x103d, BINARY, "PostReplyFolderEntries", "PR_POST_REPLY_FOLDER_ENTRIES");
   public static final MAPIAttribute POST_REPLY_FOLDER_NAMES =
      new MAPIAttribute(0x103e, ASCII_STRING, "PostReplyFolderNames", "PR_POST_REPLY_FOLDER_NAMES");
   public static final MAPIAttribute POSTAL_ADDRESS =
      new MAPIAttribute(0x3a15, ASCII_STRING, "PostalAddress", "PR_POSTAL_ADDRESS");
   public static final MAPIAttribute POSTAL_CODE =
      new MAPIAttribute(0x3a2a, ASCII_STRING, "PostalCode", "PR_POSTAL_CODE");
   public static final MAPIAttribute PREPROCESS =
      new MAPIAttribute(0xe22, BOOLEAN, "Preprocess", "PR_PREPROCESS");
   public static final MAPIAttribute PRIMARY_CAPABILITY =
      new MAPIAttribute(0x3904, BINARY, "PrimaryCapability", "PR_PRIMARY_CAPABILITY");
   public static final MAPIAttribute PRIMARY_FAX_NUMBER =
      new MAPIAttribute(0x3a23, ASCII_STRING, "PrimaryFaxNumber", "PR_PRIMARY_FAX_NUMBER");
   public static final MAPIAttribute PRIMARY_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1a, ASCII_STRING, "PrimaryTelephoneNumber", "PR_PRIMARY_TELEPHONE_NUMBER");
   public static final MAPIAttribute PRIORITY =
      new MAPIAttribute(0x26, LONG, "Priority", "PR_PRIORITY");
   public static final MAPIAttribute PROFESSION =
      new MAPIAttribute(0x3a46, ASCII_STRING, "Profession", "PR_PROFESSION");
   public static final MAPIAttribute PROFILE_NAME =
      new MAPIAttribute(0x3d12, ASCII_STRING, "ProfileName", "PR_PROFILE_NAME");
   public static final MAPIAttribute PROOF_OF_DELIVERY =
      new MAPIAttribute(0xc11, BINARY, "ProofOfDelivery", "PR_PROOF_OF_DELIVERY");
   public static final MAPIAttribute PROOF_OF_DELIVERY_REQUESTED =
      new MAPIAttribute(0xc12, BOOLEAN, "ProofOfDeliveryRequested", "PR_PROOF_OF_DELIVERY_REQUESTED");
   public static final MAPIAttribute PROOF_OF_SUBMISSION =
      new MAPIAttribute(0xe26, BINARY, "ProofOfSubmission", "PR_PROOF_OF_SUBMISSION");
   public static final MAPIAttribute PROOF_OF_SUBMISSION_REQUESTED =
      new MAPIAttribute(40, BOOLEAN, "ProofOfSubmissionRequested", "PR_PROOF_OF_SUBMISSION_REQUESTED");
   public static final MAPIAttribute PROP_ID_SECURE_MAX =
      new MAPIAttribute(0x67ff, -1, "PropIdSecureMax", "PROP_ID_SECURE_MAX");
   public static final MAPIAttribute PROP_ID_SECURE_MIN =
      new MAPIAttribute(0x67f0, -1, "PropIdSecureMin", "PROP_ID_SECURE_MIN");
   public static final MAPIAttribute PROVIDER_DISPLAY =
      new MAPIAttribute(0x3006, ASCII_STRING, "ProviderDisplay", "PR_PROVIDER_DISPLAY");
   public static final MAPIAttribute PROVIDER_DLL_NAME =
      new MAPIAttribute(0x300a, ASCII_STRING, "ProviderDllName", "PR_PROVIDER_DLL_NAME");
   public static final MAPIAttribute PROVIDER_ORDINAL =
      new MAPIAttribute(0x300d, LONG, "ProviderOrdinal", "PR_PROVIDER_ORDINAL");
   public static final MAPIAttribute PROVIDER_SUBMIT_TIME =
      new MAPIAttribute(0x48, TIME, "ProviderSubmitTime", "PR_PROVIDER_SUBMIT_TIME");
   public static final MAPIAttribute PROVIDER_UID =
      new MAPIAttribute(0x300c, BINARY, "ProviderUid", "PR_PROVIDER_UID");
   public static final MAPIAttribute PUID =
      new MAPIAttribute(0x300e, -1, "Puid", "PR_PUID");
   public static final MAPIAttribute RADIO_TELEPHONE_NUMBER =
      new MAPIAttribute(0x3a1d, ASCII_STRING, "RadioTelephoneNumber", "PR_RADIO_TELEPHONE_NUMBER");
   public static final MAPIAttribute RCVD_REPRESENTING_ADDRTYPE =
      new MAPIAttribute(0x77, ASCII_STRING, "RcvdRepresentingAddrtype", "PR_RCVD_REPRESENTING_ADDRTYPE");
   public static final MAPIAttribute RCVD_REPRESENTING_EMAIL_ADDRESS =
      new MAPIAttribute(120, ASCII_STRING, "RcvdRepresentingEmailAddress", "PR_RCVD_REPRESENTING_EMAIL_ADDRESS");
   public static final MAPIAttribute RCVD_REPRESENTING_ENTRY_ID =
      new MAPIAttribute(0x43, BINARY, "RcvdRepresentingEntryId", "PR_RCVD_REPRESENTING_ENTRYID");
   public static final MAPIAttribute RCVD_REPRESENTING_NAME =
      new MAPIAttribute(0x44, ASCII_STRING, "RcvdRepresentingName", "PR_RCVD_REPRESENTING_NAME");
   public static final MAPIAttribute RCVD_REPRESENTING_SEARCH_KEY =
      new MAPIAttribute(0x52, BINARY, "RcvdRepresentingSearchKey", "PR_RCVD_REPRESENTING_SEARCH_KEY");
   public static final MAPIAttribute READ_RECEIPT_ENTRY_ID =
      new MAPIAttribute(70, BINARY, "ReadReceiptEntryId", "PR_READ_RECEIPT_ENTRYID");
   public static final MAPIAttribute READ_RECEIPT_REQUESTED =
      new MAPIAttribute(0x29, BOOLEAN, "ReadReceiptRequested", "PR_READ_RECEIPT_REQUESTED");
   public static final MAPIAttribute READ_RECEIPT_SEARCH_KEY =
      new MAPIAttribute(0x53, BINARY, "ReadReceiptSearchKey", "PR_READ_RECEIPT_SEARCH_KEY");
   public static final MAPIAttribute RECEIPT_TIME =
      new MAPIAttribute(0x2a, TIME, "ReceiptTime", "PR_RECEIPT_TIME");
   public static final MAPIAttribute RECEIVE_FOLDER_SETTINGS =
      new MAPIAttribute(0x3415, DIRECTORY, "ReceiveFolderSettings", "PR_RECEIVE_FOLDER_SETTINGS");
   public static final MAPIAttribute RECEIVED_BY_ADDRTYPE =
      new MAPIAttribute(0x75, ASCII_STRING, "ReceivedByAddrtype", "PR_RECEIVED_BY_ADDRTYPE");
   public static final MAPIAttribute RECEIVED_BY_EMAIL_ADDRESS =
      new MAPIAttribute(0x76, ASCII_STRING, "ReceivedByEmailAddress", "PR_RECEIVED_BY_EMAIL_ADDRESS");
   public static final MAPIAttribute RECEIVED_BY_ENTRY_ID =
      new MAPIAttribute(0x3f, BINARY, "ReceivedByEntryId", "PR_RECEIVED_BY_ENTRYID");
   public static final MAPIAttribute RECEIVED_BY_NAME =
      new MAPIAttribute(0x40, ASCII_STRING, "ReceivedByName", "PR_RECEIVED_BY_NAME");
   public static final MAPIAttribute RECIPIENT_DISPLAY_NAME =
      new MAPIAttribute(0x5ff6, -1, "RecipientDisplayName", null);
   public static final MAPIAttribute RECIPIENT_ENTRY_ID =
      new MAPIAttribute(0x5ff7, -1, "RecipientEntryId", null);
   public static final MAPIAttribute RECIPIENT_FLAGS =
      new MAPIAttribute(0x5ffd, -1, "RecipientFlags", null);
   public static final MAPIAttribute RECEIVED_BY_SEARCH_KEY =
      new MAPIAttribute(0x51, BINARY, "ReceivedBySearchKey", "PR_RECEIVED_BY_SEARCH_KEY");
   public static final MAPIAttribute RECIPIENT_CERTIFICATE =
      new MAPIAttribute(0xc13, BINARY, "RecipientCertificate", "PR_RECIPIENT_CERTIFICATE");
   public static final MAPIAttribute RECIPIENT_NUMBER_FOR_ADVICE =
      new MAPIAttribute(0xc14, ASCII_STRING, "RecipientNumberForAdvice", "PR_RECIPIENT_NUMBER_FOR_ADVICE");
   public static final MAPIAttribute RECIPIENT_REASSIGNMENT_PROHIBITED =
      new MAPIAttribute(0x2b, BOOLEAN, "RecipientReassignmentProhibited", "PR_RECIPIENT_REASSIGNMENT_PROHIBITED");
   public static final MAPIAttribute RECIPIENT_STATUS =
      new MAPIAttribute(0xe15, LONG, "RecipientStatus", "PR_RECIPIENT_STATUS");
   public static final MAPIAttribute RECIPIENT_TYPE =
      new MAPIAttribute(0xc15, LONG, "RecipientType", "PR_RECIPIENT_TYPE");
   public static final MAPIAttribute RECORD_KEY =
      new MAPIAttribute(0xff9, BINARY, "RecordKey", "PR_RECORD_KEY");
   public static final MAPIAttribute REDIRECTION_HISTORY =
      new MAPIAttribute(0x2c, BINARY, "RedirectionHistory", "PR_REDIRECTION_HISTORY");
   public static final MAPIAttribute REFERRED_BY_NAME =
      new MAPIAttribute(0x3a47, ASCII_STRING, "ReferredByName", "PR_REFERRED_BY_NAME");
   public static final MAPIAttribute REGISTERED_MAIL_TYPE =
      new MAPIAttribute(0xc16, LONG, "RegisteredMailType", "PR_REGISTERED_MAIL_TYPE");
   public static final MAPIAttribute RELATED_IPMS =
      new MAPIAttribute(0x2d, BINARY, "RelatedIpms", "PR_RELATED_IPMS");
   public static final MAPIAttribute REMOTE_PROGRESS =
      new MAPIAttribute(0x3e0b, LONG, "RemoteProgress", "PR_REMOTE_PROGRESS");
   public static final MAPIAttribute REMOTE_PROGRESS_TEXT =
      new MAPIAttribute(0x3e0c, ASCII_STRING, "RemoteProgressText", "PR_REMOTE_PROGRESS_TEXT");
   public static final MAPIAttribute REMOTE_VALIDATE_OK =
      new MAPIAttribute(0x3e0d, BOOLEAN, "RemoteValidateOk", "PR_REMOTE_VALIDATE_OK");
   public static final MAPIAttribute RENDERING_POSITION =
      new MAPIAttribute(0x370b, LONG, "RenderingPosition", "PR_RENDERING_POSITION");
   public static final MAPIAttribute REPLY_RECIPIENT_ENTRIES =
      new MAPIAttribute(0x4f, BINARY, "ReplyRecipientEntries", "PR_REPLY_RECIPIENT_ENTRIES");
   public static final MAPIAttribute REPLY_RECIPIENT_NAMES =
      new MAPIAttribute(80, ASCII_STRING, "ReplyRecipientNames", "PR_REPLY_RECIPIENT_NAMES");
   public static final MAPIAttribute REPLY_REQUESTED =
      new MAPIAttribute(0xc17, BOOLEAN, "ReplyRequested", "PR_REPLY_REQUESTED");
   public static final MAPIAttribute REPLY_TIME =
      new MAPIAttribute(0x30, TIME, "ReplyTime", "PR_REPLY_TIME");
   public static final MAPIAttribute REPORT_ENTRY_ID =
      new MAPIAttribute(0x45, BINARY, "ReportEntryId", "PR_REPORT_ENTRYID");
   public static final MAPIAttribute REPORT_NAME =
      new MAPIAttribute(0x3a, ASCII_STRING, "ReportName", "PR_REPORT_NAME");
   public static final MAPIAttribute REPORT_SEARCH_KEY =
      new MAPIAttribute(0x54, BINARY, "ReportSearchKey", "PR_REPORT_SEARCH_KEY");
   public static final MAPIAttribute REPORT_TAG =
      new MAPIAttribute(0x31, BINARY, "ReportTag", "PR_REPORT_TAG");
   public static final MAPIAttribute REPORT_TEXT =
      new MAPIAttribute(0x1001, ASCII_STRING, "ReportText", "PR_REPORT_TEXT");
   public static final MAPIAttribute REPORT_TIME =
      new MAPIAttribute(50, TIME, "ReportTime", "PR_REPORT_TIME");
   public static final MAPIAttribute REPORTING_DL_NAME =
      new MAPIAttribute(0x1003, BINARY, "ReportingDlName", "PR_REPORTING_DL_NAME");
   public static final MAPIAttribute REPORTING_MTA_CERTIFICATE =
      new MAPIAttribute(0x1004, BINARY, "ReportingMtaCertificate", "PR_REPORTING_MTA_CERTIFICATE");
   public static final MAPIAttribute REQUESTED_DELIVERY_METHOD =
      new MAPIAttribute(0xc18, LONG, "RequestedDeliveryMethod", "PR_REQUESTED_DELIVERY_METHOD");
   public static final MAPIAttribute RESOURCE_FLAGS =
      new MAPIAttribute(0x3009, LONG, "ResourceFlags", "PR_RESOURCE_FLAGS");
   public static final MAPIAttribute RESOURCE_METHODS =
      new MAPIAttribute(0x3e02, LONG, "ResourceMethods", "PR_RESOURCE_METHODS");
   public static final MAPIAttribute RESOURCE_PATH =
      new MAPIAttribute(0x3e07, ASCII_STRING, "ResourcePath", "PR_RESOURCE_PATH");
   public static final MAPIAttribute RESOURCE_TYPE =
      new MAPIAttribute(0x3e03, LONG, "ResourceType", "PR_RESOURCE_TYPE");
   public static final MAPIAttribute RESPONSE_REQUESTED =
      new MAPIAttribute(0x63, BOOLEAN, "ResponseRequested", "PR_RESPONSE_REQUESTED");
   public static final MAPIAttribute RESPONSIBILITY =
      new MAPIAttribute(0xe0f, BOOLEAN, "Responsibility", "PR_RESPONSIBILITY");
   public static final MAPIAttribute RETURNED_IPM =
      new MAPIAttribute(0x33, BOOLEAN, "ReturnedIpm", "PR_RETURNED_IPM");
   public static final MAPIAttribute ROW_TYPE =
      new MAPIAttribute(0xff5, LONG, "RowType", "PR_ROW_TYPE");
   public static final MAPIAttribute ROWID =
      new MAPIAttribute(0x3000, LONG, "Rowid", "PR_ROWID");
   public static final MAPIAttribute RTF_COMPRESSED =
      new MAPIAttribute(0x1009, BINARY, "RtfCompressed", "PR_RTF_COMPRESSED");
   public static final MAPIAttribute RTF_IN_SYNC =
      new MAPIAttribute(0xe1f, BOOLEAN, "RtfInSync", "PR_RTF_IN_SYNC");
   public static final MAPIAttribute RTF_SYNC_BODY_COUNT =
      new MAPIAttribute(0x1007, LONG, "RtfSyncBodyCount", "PR_RTF_SYNC_BODY_COUNT");
   public static final MAPIAttribute RTF_SYNC_BODY_CRC =
      new MAPIAttribute(0x1006, LONG, "RtfSyncBodyCrc", "PR_RTF_SYNC_BODY_CRC");
   public static final MAPIAttribute RTF_SYNC_BODY_TAG =
      new MAPIAttribute(0x1008, ASCII_STRING, "RtfSyncBodyTag", "PR_RTF_SYNC_BODY_TAG");
   public static final MAPIAttribute RTF_SYNC_PREFIX_COUNT =
      new MAPIAttribute(0x1010, LONG, "RtfSyncPrefixCount", "PR_RTF_SYNC_PREFIX_COUNT");
   public static final MAPIAttribute RTF_SYNC_TRAILING_COUNT =
      new MAPIAttribute(0x1011, LONG, "RtfSyncTrailingCount", "PR_RTF_SYNC_TRAILING_COUNT");
   public static final MAPIAttribute SEARCH =
      new MAPIAttribute(0x3607, DIRECTORY, "Search", "PR_SEARCH");
   public static final MAPIAttribute SEARCH_KEY =
      new MAPIAttribute(0x300b, BINARY, "SearchKey", "PR_SEARCH_KEY");
   public static final MAPIAttribute SECURITY =
      new MAPIAttribute(0x34, LONG, "Security", "PR_SECURITY");
   public static final MAPIAttribute SELECTABLE =
      new MAPIAttribute(0x3609, BOOLEAN, "Selectable", "PR_SELECTABLE");
   public static final MAPIAttribute SEND_INTERNET_ENCODING =
      new MAPIAttribute(0x3a71, LONG, "SendInternetEncoding", "PR_SEND_INTERNET_ENCODING");
   public static final MAPIAttribute SEND_RECALL_REPORT =
      new MAPIAttribute(0x6803, -1, "SendRecallReport", "messages");
   public static final MAPIAttribute SEND_RICH_INFO =
      new MAPIAttribute(0x3a40, BOOLEAN, "SendRichInfo", "PR_SEND_RICH_INFO");
   public static final MAPIAttribute SENDER_ADDRTYPE =
      new MAPIAttribute(0xc1e, ASCII_STRING, "SenderAddrtype", "PR_SENDER_ADDRTYPE");
   public static final MAPIAttribute SENDER_EMAIL_ADDRESS =
      new MAPIAttribute(0xc1f, ASCII_STRING, "SenderEmailAddress", "PR_SENDER_EMAIL_ADDRESS");
   public static final MAPIAttribute SENDER_ENTRY_ID =
      new MAPIAttribute(0xc19, BINARY, "SenderEntryId", "PR_SENDER_ENTRYID");
   public static final MAPIAttribute SENDER_NAME =
      new MAPIAttribute(0xc1a, ASCII_STRING, "SenderName", "PR_SENDER_NAME");
   public static final MAPIAttribute SENDER_SEARCH_KEY =
      new MAPIAttribute(0xc1d, BINARY, "SenderSearchKey", "PR_SENDER_SEARCH_KEY");
   public static final MAPIAttribute SENSITIVITY =
      new MAPIAttribute(0x36, LONG, "Sensitivity", "PR_SENSITIVITY");
   public static final MAPIAttribute SENT_REPRESENTING_ADDRTYPE =
      new MAPIAttribute(100, ASCII_STRING, "SentRepresentingAddrtype", "PR_SENT_REPRESENTING_ADDRTYPE");
   public static final MAPIAttribute SENT_REPRESENTING_EMAIL_ADDRESS =
      new MAPIAttribute(0x65, ASCII_STRING, "SentRepresentingEmailAddress", "PR_SENT_REPRESENTING_EMAIL_ADDRESS");
   public static final MAPIAttribute SENT_REPRESENTING_ENTRY_ID =
      new MAPIAttribute(0x41, BINARY, "SentRepresentingEntryId", "PR_SENT_REPRESENTING_ENTRYID");
   public static final MAPIAttribute SENT_REPRESENTING_NAME =
      new MAPIAttribute(0x42, ASCII_STRING, "SentRepresentingName", "PR_SENT_REPRESENTING_NAME");
   public static final MAPIAttribute SENT_REPRESENTING_SEARCH_KEY =
      new MAPIAttribute(0x3b, BINARY, "SentRepresentingSearchKey", "PR_SENT_REPRESENTING_SEARCH_KEY");
   public static final MAPIAttribute SENTMAIL_ENTRY_ID =
      new MAPIAttribute(0xe0a, BINARY, "SentmailEntryId", "PR_SENTMAIL_ENTRYID");
   public static final MAPIAttribute SERVICE_DELETE_FILES =
      new MAPIAttribute(0x3d10, 4126, "ServiceDeleteFiles", "PR_SERVICE_DELETE_FILES");
   public static final MAPIAttribute SERVICE_DLL_NAME =
      new MAPIAttribute(0x3d0a, ASCII_STRING, "ServiceDllName", "PR_SERVICE_DLL_NAME");
   public static final MAPIAttribute SERVICE_ENTRY_NAME =
      new MAPIAttribute(0x3d0b, ASCII_STRING, "ServiceEntryName", "PR_SERVICE_ENTRY_NAME");
   public static final MAPIAttribute SERVICE_EXTRA_UIDS =
      new MAPIAttribute(0x3d0d, BINARY, "ServiceExtraUids", "PR_SERVICE_EXTRA_UIDS");
   public static final MAPIAttribute SERVICE_NAME =
      new MAPIAttribute(0x3d09, ASCII_STRING, "ServiceName", "PR_SERVICE_NAME");
   public static final MAPIAttribute SERVICE_SUPPORT_FILES =
      new MAPIAttribute(0x3d0f, 4126, "ServiceSupportFiles", "PR_SERVICE_SUPPORT_FILES");
   public static final MAPIAttribute SERVICE_UID =
      new MAPIAttribute(0x3d0c, BINARY, "ServiceUid", "PR_SERVICE_UID");
   public static final MAPIAttribute SERVICES =
      new MAPIAttribute(0x3d0e, BINARY, "Services", "PR_SERVICES");
   public static final MAPIAttribute SEVEN_BIT_DISPLAY_NAME =
      new MAPIAttribute(0x39ff, ASCII_STRING, "SevenBitDisplayName", "PR_SEVEN_BIT_DISPLAY_NAME");
   public static final MAPIAttribute SMTP_ADDRESS =
      new MAPIAttribute(0x39fe, -1, "SmtpAddress", "PR_SMTP_ADDRESS");
   public static final MAPIAttribute SPOOLER_STATUS =
      new MAPIAttribute(0xe10, LONG, "SpoolerStatus", "PR_SPOOLER_STATUS");
   public static final MAPIAttribute SPOUSE_NAME =
      new MAPIAttribute(0x3a48, ASCII_STRING, "SpouseName", "PR_SPOUSE_NAME");
   public static final MAPIAttribute START_DATE =
      new MAPIAttribute(0x60, TIME, "StartDate", "PR_START_DATE");
   public static final MAPIAttribute STATE_OR_PROVINCE =
      new MAPIAttribute(0x3a28, ASCII_STRING, "StateOrProvince", "PR_STATE_OR_PROVINCE");
   public static final MAPIAttribute STATUS =
      new MAPIAttribute(0x360b, LONG, "Status", "PR_STATUS");
   public static final MAPIAttribute STATUS_CODE =
      new MAPIAttribute(0x3e04, LONG, "StatusCode", "PR_STATUS_CODE");
   public static final MAPIAttribute STATUS_STRING =
      new MAPIAttribute(0x3e08, ASCII_STRING, "StatusString", "PR_STATUS_STRING");
   public static final MAPIAttribute STORE_ENTRY_ID =
      new MAPIAttribute(0xffb, BINARY, "StoreEntryId", "PR_STORE_ENTRYID");
   public static final MAPIAttribute STORE_PROVIDERS =
      new MAPIAttribute(0x3d00, BINARY, "StoreProviders", "PR_STORE_PROVIDERS");
   public static final MAPIAttribute STORE_RECORD_KEY =
      new MAPIAttribute(0xffa, BINARY, "StoreRecordKey", "PR_STORE_RECORD_KEY");
   public static final MAPIAttribute STORE_STATE =
      new MAPIAttribute(0x340e, LONG, "StoreState", "PR_STORE_STATE");
   public static final MAPIAttribute STORE_SUPPORT_MASK =
      new MAPIAttribute(0x340d, LONG, "StoreSupportMask", "PR_STORE_SUPPORT_MASK");
   public static final MAPIAttribute STREET_ADDRESS =
      new MAPIAttribute(0x3a29, ASCII_STRING, "StreetAddress", "PR_STREET_ADDRESS");
   public static final MAPIAttribute SUBFOLDERS =
      new MAPIAttribute(0x360a, BOOLEAN, "Subfolders", "PR_SUBFOLDERS");
   public static final MAPIAttribute SUBJECT =
      new MAPIAttribute(0x37, ASCII_STRING, "Subject", "PR_SUBJECT");
   public static final MAPIAttribute SUBJECT_IPM =
      new MAPIAttribute(0x38, BINARY, "SubjectIpm", "PR_SUBJECT_IPM");
   public static final MAPIAttribute SUBJECT_PREFIX =
      new MAPIAttribute(0x3d, ASCII_STRING, "SubjectPrefix", "PR_SUBJECT_PREFIX");
   public static final MAPIAttribute SUBMIT_FLAGS =
      new MAPIAttribute(0xe14, LONG, "SubmitFlags", "PR_SUBMIT_FLAGS");
   public static final MAPIAttribute SUPERSEDES =
      new MAPIAttribute(0x103a, ASCII_STRING, "Supersedes", "PR_SUPERSEDES");
   public static final MAPIAttribute SUPPLEMENTARY_INFO =
      new MAPIAttribute(0xc1b, ASCII_STRING, "SupplementaryInfo", "PR_SUPPLEMENTARY_INFO");
   public static final MAPIAttribute SURNAME =
      new MAPIAttribute(0x3a11, ASCII_STRING, "Surname", "PR_SURNAME");
   public static final MAPIAttribute TELEX_NUMBER =
      new MAPIAttribute(0x3a2c, ASCII_STRING, "TelexNumber", "PR_TELEX_NUMBER");
   public static final MAPIAttribute TEMPLATEID =
      new MAPIAttribute(0x3902, BINARY, "Templateid", "PR_TEMPLATEID");
   public static final MAPIAttribute TITLE =
      new MAPIAttribute(0x3a17, ASCII_STRING, "Title", "PR_TITLE");
   public static final MAPIAttribute TNEF_CORRELATION_KEY =
      new MAPIAttribute(0x7f, BINARY, "TnefCorrelationKey", "PR_TNEF_CORRELATION_KEY");
   public static final MAPIAttribute TRANSMITABLE_DISPLAY_NAME =
      new MAPIAttribute(0x3a20, ASCII_STRING, "TransmitableDisplayName", "PR_TRANSMITABLE_DISPLAY_NAME");
   public static final MAPIAttribute TRANSPORT_KEY =
      new MAPIAttribute(0xe16, LONG, "TransportKey", "PR_TRANSPORT_KEY");
   public static final MAPIAttribute TRANSPORT_MESSAGE_HEADERS =
      new MAPIAttribute(0x7d, ASCII_STRING, "TransportMessageHeaders", "PR_TRANSPORT_MESSAGE_HEADERS");
   public static final MAPIAttribute TRANSPORT_PROVIDERS =
      new MAPIAttribute(0x3d02, BINARY, "TransportProviders", "PR_TRANSPORT_PROVIDERS");
   public static final MAPIAttribute TRANSPORT_STATUS =
      new MAPIAttribute(0xe11, LONG, "TransportStatus", "PR_TRANSPORT_STATUS");
   public static final MAPIAttribute TTYTDD_PHONE_NUMBER =
      new MAPIAttribute(0x3a4b, ASCII_STRING, "TtytddPhoneNumber", "PR_TTYTDD_PHONE_NUMBER");
   public static final MAPIAttribute TYPE_OF_MTS_USER =
      new MAPIAttribute(0xc1c, LONG, "TypeOfMtsUser", "PR_TYPE_OF_MTS_USER");
   public static final MAPIAttribute USER_CERTIFICATE =
      new MAPIAttribute(0x3a22, BINARY, "UserCertificate", "PR_USER_CERTIFICATE");
   public static final MAPIAttribute USER_X509_CERTIFICATE =
      new MAPIAttribute(0x3a70, 4354, "UserX509Certificate", "PR_USER_X509_CERTIFICATE");
   public static final MAPIAttribute VALID_FOLDER_MASK =
      new MAPIAttribute(0x35df, LONG, "ValidFolderMask", "PR_VALID_FOLDER_MASK");
   public static final MAPIAttribute VIEWS_ENTRY_ID =
      new MAPIAttribute(0x35e5, BINARY, "ViewsEntryId", "PR_VIEWS_ENTRYID");
   public static final MAPIAttribute WEDDING_ANNIVERSARY =
      new MAPIAttribute(0x3a41, TIME, "WeddingAnniversary", "PR_WEDDING_ANNIVERSARY");
   public static final MAPIAttribute X400_CONTENT_TYPE =
      new MAPIAttribute(60, BINARY, "X400ContentType", "PR_X400_CONTENT_TYPE");
   public static final MAPIAttribute X400_DEFERRED_DELIVERY_CANCEL =
      new MAPIAttribute(0x3e09, BOOLEAN, "X400DeferredDeliveryCancel", "PR_X400_DEFERRED_DELIVERY_CANCEL");
   public static final MAPIAttribute XPOS =
      new MAPIAttribute(0x3f05, LONG, "Xpos", "PR_XPOS");
   public static final MAPIAttribute YPOS =
      new MAPIAttribute(0x3f06, LONG, "Ypos", "PR_YPOS");
   
   public static final MAPIAttribute UNKNOWN =
      new MAPIAttribute(-1, -1, "Unknown", null);
   
   /* ---------------------------------------------------------------------  */
   
   public final int id;
   public final int usualType;
   public final String name;
   public final String mapiProperty;
   
   private MAPIAttribute(int id, int usualType, String name, String mapiProperty) {
      this.id = id;
      this.usualType = usualType;
      this.name = name;
      this.mapiProperty = mapiProperty;
      
      // Store it for lookup
      if(attributes.containsKey(id)) {
         throw new IllegalArgumentException(
               "Duplicate MAPI Property with ID " + id + " : " +
               toString() + " vs " + attributes.get(id).toString()
         );
      }
      attributes.put(id, this);
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
   public static MAPIAttribute get(int id) {
      MAPIAttribute attr = attributes.get(id);
      if(attr != null) {
         return attr;
      } else {
         return UNKNOWN;
      }
   }
}
