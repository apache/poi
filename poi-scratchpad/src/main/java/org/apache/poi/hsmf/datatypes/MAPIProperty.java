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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.util.StringUtil;

import static org.apache.poi.hsmf.datatypes.Types.ASCII_STRING;
import static org.apache.poi.hsmf.datatypes.Types.BINARY;
import static org.apache.poi.hsmf.datatypes.Types.BOOLEAN;
import static org.apache.poi.hsmf.datatypes.Types.CLS_ID;
import static org.apache.poi.hsmf.datatypes.Types.DIRECTORY;
import static org.apache.poi.hsmf.datatypes.Types.LONG;
import static org.apache.poi.hsmf.datatypes.Types.LONG_LONG;
import static org.apache.poi.hsmf.datatypes.Types.SHORT;
import static org.apache.poi.hsmf.datatypes.Types.TIME;

/**
 * Holds the list of MAPI Attributes, and allows lookup by friendly name, ID and
 * MAPI Property Name.
 *
 * These are taken from the following MSDN resources:
 * https://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertyid(v=exchg.150).aspx
 * http://msdn.microsoft.com/en-us/library/ms526356%28v=exchg.10%29.aspx
 */
@SuppressWarnings("unused")
public class MAPIProperty {
    private static Map<Integer, MAPIProperty> attributes = new HashMap<>();

    public static final MAPIProperty AB_DEFAULT_DIR =
        new MAPIProperty(0x3d06, BINARY, "AbDefaultDir", "PR_AB_DEFAULT_DIR");
    public static final MAPIProperty AB_DEFAULT_PAB =
        new MAPIProperty(0x3d07, BINARY, "AbDefaultPab", "PR_AB_DEFAULT_PAB");
    public static final MAPIProperty AB_PROVIDER_ID =
        new MAPIProperty(0x3615, BINARY, "AbProviderId", "PR_AB_PROVIDER_ID");
    public static final MAPIProperty AB_PROVIDERS =
        new MAPIProperty(0x3d01, BINARY, "AbProviders", "PR_AB_PROVIDERS");
    public static final MAPIProperty AB_SEARCH_PATH =
        new MAPIProperty(0x3d05, Types.createCustom(4354), "AbSearchPath", "PR_AB_SEARCH_PATH");
    public static final MAPIProperty AB_SEARCH_PATH_UPDATE =
        new MAPIProperty(0x3d11, BINARY, "AbSearchPathUpdate", "PR_AB_SEARCH_PATH_UPDATE");
    public static final MAPIProperty ACCESS =
        new MAPIProperty(0xff4, LONG, "Access", "PR_ACCESS");
    public static final MAPIProperty ACCESS_LEVEL =
        new MAPIProperty(0xff7, LONG, "AccessLevel", "PR_ACCESS_LEVEL");
    public static final MAPIProperty ACCOUNT =
        new MAPIProperty(0x3a00, ASCII_STRING, "Account", "PR_ACCOUNT");
    public static final MAPIProperty ADDRTYPE =
        new MAPIProperty(0x3002, ASCII_STRING, "Addrtype", "PR_ADDRTYPE");
    public static final MAPIProperty ALTERNATE_RECIPIENT =
        new MAPIProperty(0x3a01, BINARY, "AlternateRecipient", "PR_ALTERNATE_RECIPIENT");
    public static final MAPIProperty ALTERNATE_RECIPIENT_ALLOWED =
        new MAPIProperty(2, BOOLEAN, "AlternateRecipientAllowed", "PR_ALTERNATE_RECIPIENT_ALLOWED");
    public static final MAPIProperty ANR =
        new MAPIProperty(0x360c, ASCII_STRING, "Anr", "PR_ANR");
    public static final MAPIProperty ASSISTANT =
        new MAPIProperty(0x3a30, ASCII_STRING, "Assistant", "PR_ASSISTANT");
    public static final MAPIProperty ASSISTANT_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a2e, ASCII_STRING, "AssistantTelephoneNumber", "PR_ASSISTANT_TELEPHONE_NUMBER");
    public static final MAPIProperty ASSOC_CONTENT_COUNT =
        new MAPIProperty(0x3617, LONG, "AssocContentCount", "PR_ASSOC_CONTENT_COUNT");
    public static final MAPIProperty ATTACH_ADDITIONAL_INFO =
        new MAPIProperty(0x370f, BINARY, "AttachAdditionalInfo", "PR_ATTACH_ADDITIONAL_INFO");
    public static final MAPIProperty ATTACH_CONTENT_BASE =
        new MAPIProperty(0x3711, Types.UNKNOWN,  "AttachContentBase", "PR_ATTACH_CONTENT_BASE");
    public static final MAPIProperty ATTACH_CONTENT_ID =
        new MAPIProperty(0x3712, Types.UNKNOWN,  "AttachContentId", "PR_ATTACH_CONTENT_ID");
    public static final MAPIProperty ATTACH_CONTENT_LOCATION =
        new MAPIProperty(0x3713, Types.UNKNOWN,  "AttachContentLocation", "PR_ATTACH_CONTENT_LOCATION");
    public static final MAPIProperty ATTACH_DATA =
        new MAPIProperty(0x3701, BINARY, "AttachData", "PR_ATTACH_DATA_OBJ");
    public static final MAPIProperty ATTACH_DISPOSITION =
        new MAPIProperty(0x3716, Types.UNKNOWN,  "AttachDisposition", "PR_ATTACH_DISPOSITION");
    public static final MAPIProperty ATTACH_ENCODING =
        new MAPIProperty(0x3702, BINARY, "AttachEncoding", "PR_ATTACH_ENCODING");
    public static final MAPIProperty ATTACH_EXTENSION =
        new MAPIProperty(0x3703, ASCII_STRING, "AttachExtension", "PR_ATTACH_EXTENSION");
    public static final MAPIProperty ATTACH_FILENAME =
        new MAPIProperty(0x3704, ASCII_STRING, "AttachFilename", "PR_ATTACH_FILENAME");
    public static final MAPIProperty ATTACH_FLAGS =
        new MAPIProperty(0x3714, Types.UNKNOWN,  "AttachFlags", "PR_ATTACH_FLAGS");
    public static final MAPIProperty ATTACH_LONG_FILENAME =
        new MAPIProperty(0x3707, ASCII_STRING, "AttachLongFilename", "PR_ATTACH_LONG_FILENAME");
    public static final MAPIProperty ATTACH_LONG_PATHNAME =
        new MAPIProperty(0x370d, ASCII_STRING, "AttachLongPathname", "PR_ATTACH_LONG_PATHNAME");
    public static final MAPIProperty ATTACH_METHOD =
        new MAPIProperty(0x3705, LONG, "AttachMethod", "PR_ATTACH_METHOD");
    public static final MAPIProperty ATTACH_MIME_SEQUENCE =
        new MAPIProperty(0x3710, Types.UNKNOWN,  "AttachMimeSequence", "PR_ATTACH_MIME_SEQUENCE");
    public static final MAPIProperty ATTACH_MIME_TAG =
        new MAPIProperty(0x370e, ASCII_STRING, "AttachMimeTag", "PR_ATTACH_MIME_TAG");
    public static final MAPIProperty ATTACH_NETSCAPE_MAC_INFO =
        new MAPIProperty(0x3715, Types.UNKNOWN,  "AttachNetscapeMacInfo", "PR_ATTACH_NETSCAPE_MAC_INFO");
    public static final MAPIProperty ATTACH_NUM =
        new MAPIProperty(0xe21, LONG, "AttachNum", "PR_ATTACH_NUM");
    public static final MAPIProperty ATTACH_PATHNAME =
        new MAPIProperty(0x3708, ASCII_STRING, "AttachPathname", "PR_ATTACH_PATHNAME");
    public static final MAPIProperty ATTACH_RENDERING =
        new MAPIProperty(0x3709, BINARY, "AttachRendering", "PR_ATTACH_RENDERING");
    public static final MAPIProperty ATTACH_SIZE =
        new MAPIProperty(0xe20, LONG, "AttachSize", "PR_ATTACH_SIZE");
    public static final MAPIProperty ATTACH_TAG =
        new MAPIProperty(0x370a, BINARY, "AttachTag", "PR_ATTACH_TAG");
    public static final MAPIProperty ATTACH_TRANSPORT_NAME =
        new MAPIProperty(0x370c, ASCII_STRING, "AttachTransportName", "PR_ATTACH_TRANSPORT_NAME");
    public static final MAPIProperty ATTACHMENT_X400_PARAMETERS =
        new MAPIProperty(0x3700, BINARY, "AttachmentX400Parameters", "PR_ATTACHMENT_X400_PARAMETERS");
    public static final MAPIProperty AUTHORIZING_USERS =
        new MAPIProperty(3, BINARY, "AuthorizingUsers", "PR_AUTHORIZING_USERS");
    public static final MAPIProperty AUTO_FORWARD_COMMENT =
        new MAPIProperty(4, ASCII_STRING, "AutoForwardComment", "PR_AUTO_FORWARD_COMMENT");
    public static final MAPIProperty AUTO_FORWARDED =
        new MAPIProperty(5, BOOLEAN, "AutoForwarded", "PR_AUTO_FORWARDED");
    public static final MAPIProperty AUTO_RESPONSE_SUPPRESS =
        new MAPIProperty(0x3fdf, Types.UNKNOWN,  "AutoResponseSuppress", "PR_AUTO_RESPONSE_SUPPRESS");
    public static final MAPIProperty BIRTHDAY =
        new MAPIProperty(0x3a42, TIME, "Birthday", "PR_BIRTHDAY");
    public static final MAPIProperty BODY =
        new MAPIProperty(0x1000, ASCII_STRING, "Body", "PR_BODY");
    public static final MAPIProperty BODY_CONTENT_ID =
        new MAPIProperty(0x1015, Types.UNKNOWN,  "BodyContentId", "PR_BODY_CONTENT_ID");
    public static final MAPIProperty BODY_CONTENT_LOCATION =
        new MAPIProperty(0x1014, Types.UNKNOWN,  "BodyContentLocation", "PR_BODY_CONTENT_LOCATION");
    public static final MAPIProperty BODY_CRC =
        new MAPIProperty(0xe1c, LONG, "BodyCrc", "PR_BODY_CRC");
    public static final MAPIProperty BODY_HTML =
        new MAPIProperty(0x1013, Types.UNKNOWN,  "BodyHtml", "data");
    public static final MAPIProperty BUSINESS_FAX_NUMBER =
        new MAPIProperty(0x3a24, ASCII_STRING, "BusinessFaxNumber", "PR_BUSINESS_FAX_NUMBER");
    public static final MAPIProperty BUSINESS_HOME_PAGE =
        new MAPIProperty(0x3a51, ASCII_STRING, "BusinessHomePage", "PR_BUSINESS_HOME_PAGE");
    public static final MAPIProperty CALLBACK_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a02, ASCII_STRING, "CallbackTelephoneNumber", "PR_CALLBACK_TELEPHONE_NUMBER");
    public static final MAPIProperty CAR_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1e, ASCII_STRING, "CarTelephoneNumber", "PR_CAR_TELEPHONE_NUMBER");
    public static final MAPIProperty CHILDRENS_NAMES =
        new MAPIProperty(0x3a58, Types.createCustom(4126), "ChildrensNames", "PR_CHILDRENS_NAMES");
    public static final MAPIProperty CLIENT_SUBMIT_TIME =
        new MAPIProperty(0x39, TIME, "ClientSubmitTime", "PR_CLIENT_SUBMIT_TIME");
    public static final MAPIProperty COMMENT =
        new MAPIProperty(0x3004, ASCII_STRING, "Comment", "PR_COMMENT");
    public static final MAPIProperty COMMON_VIEWS_ENTRY_ID =
        new MAPIProperty(0x35e6, BINARY, "CommonViewsEntryId", "PR_COMMON_VIEWS_ENTRYID");
    public static final MAPIProperty COMPANY_MAIN_PHONE_NUMBER =
        new MAPIProperty(0x3a57, ASCII_STRING, "CompanyMainPhoneNumber", "PR_COMPANY_MAIN_PHONE_NUMBER");
    public static final MAPIProperty COMPANY_NAME =
        new MAPIProperty(0x3a16, ASCII_STRING, "CompanyName", "PR_COMPANY_NAME");
    public static final MAPIProperty COMPUTER_NETWORK_NAME =
        new MAPIProperty(0x3a49, ASCII_STRING, "ComputerNetworkName", "PR_COMPUTER_NETWORK_NAME");
    public static final MAPIProperty CONTACT_ADDRTYPES =
        new MAPIProperty(0x3a54, Types.createCustom(4126), "ContactAddrtypes", "PR_CONTACT_ADDRTYPES");
    public static final MAPIProperty CONTACT_DEFAULT_ADDRESS_INDEX =
        new MAPIProperty(0x3a55, LONG, "ContactDefaultAddressIndex", "PR_CONTACT_DEFAULT_ADDRESS_INDEX");
    public static final MAPIProperty CONTACT_EMAIL_ADDRESSES =
        new MAPIProperty(0x3a56, Types.createCustom(4126), "ContactEmailAddresses", "PR_CONTACT_EMAIL_ADDRESSES");
    public static final MAPIProperty CONTACT_ENTRY_IDS =
        new MAPIProperty(0x3a53, Types.createCustom(4354), "ContactEntryIds", "PR_CONTACT_ENTRYIDS");
    public static final MAPIProperty CONTACT_VERSION =
        new MAPIProperty(0x3a52, CLS_ID, "ContactVersion", "PR_CONTACT_VERSION");
    public static final MAPIProperty CONTAINER_CLASS =
        new MAPIProperty(0x3613, ASCII_STRING, "ContainerClass", "PR_CONTAINER_CLASS");
    public static final MAPIProperty CONTAINER_CONTENTS =
        new MAPIProperty(0x360f, DIRECTORY, "ContainerContents", "PR_CONTAINER_CONTENTS");
    public static final MAPIProperty CONTAINER_FLAGS =
        new MAPIProperty(0x3600, LONG, "ContainerFlags", "PR_CONTAINER_FLAGS");
    public static final MAPIProperty CONTAINER_HIERARCHY =
        new MAPIProperty(0x360e, DIRECTORY, "ContainerHierarchy", "PR_CONTAINER_HIERARCHY");
    public static final MAPIProperty CONTAINER_MODIFY_VERSION =
        new MAPIProperty(0x3614, LONG_LONG, "ContainerModifyVersion", "PR_CONTAINER_MODIFY_VERSION");
    public static final MAPIProperty CONTENT_CONFIDENTIALITY_ALGORITHM_ID =
        new MAPIProperty(6, BINARY, "ContentConfidentialityAlgorithmId", "PR_CONTENT_CONFIDENTIALITY_ALGORITHM_ID");
    public static final MAPIProperty CONTENT_CORRELATOR =
        new MAPIProperty(7, BINARY, "ContentCorrelator", "PR_CONTENT_CORRELATOR");
    public static final MAPIProperty CONTENT_COUNT =
        new MAPIProperty(0x3602, LONG, "ContentCount", "PR_CONTENT_COUNT");
    public static final MAPIProperty CONTENT_IDENTIFIER =
        new MAPIProperty(8, ASCII_STRING, "ContentIdentifier", "PR_CONTENT_IDENTIFIER");
    public static final MAPIProperty CONTENT_INTEGRITY_CHECK =
        new MAPIProperty(0xc00, BINARY, "ContentIntegrityCheck", "PR_CONTENT_INTEGRITY_CHECK");
    public static final MAPIProperty CONTENT_LENGTH =
        new MAPIProperty(9, LONG, "ContentLength", "PR_CONTENT_LENGTH");
    public static final MAPIProperty CONTENT_RETURN_REQUESTED =
        new MAPIProperty(10, BOOLEAN, "ContentReturnRequested", "PR_CONTENT_RETURN_REQUESTED");
    public static final MAPIProperty CONTENT_UNREAD =
        new MAPIProperty(0x3603, LONG, "ContentUnread", "PR_CONTENT_UNREAD");
    public static final MAPIProperty CONTENTS_SORT_ORDER =
        new MAPIProperty(0x360d, Types.createCustom(4099), "ContentsSortOrder", "PR_CONTENTS_SORT_ORDER");
    public static final MAPIProperty CONTROL_FLAGS =
        new MAPIProperty(0x3f00, LONG, "ControlFlags", "PR_CONTROL_FLAGS");
    public static final MAPIProperty CONTROL_ID =
        new MAPIProperty(0x3f07, BINARY, "ControlId", "PR_CONTROL_ID");
    public static final MAPIProperty CONTROL_STRUCTURE =
        new MAPIProperty(0x3f01, BINARY, "ControlStructure", "PR_CONTROL_STRUCTURE");
    public static final MAPIProperty CONTROL_TYPE =
        new MAPIProperty(0x3f02, LONG, "ControlType", "PR_CONTROL_TYPE");
    public static final MAPIProperty CONVERSATION_INDEX =
        new MAPIProperty(0x71, BINARY, "ConversationIndex", "PR_CONVERSATION_INDEX");
    public static final MAPIProperty CONVERSATION_KEY =
        new MAPIProperty(11, BINARY, "ConversationKey", "PR_CONVERSATION_KEY");
    public static final MAPIProperty CONVERSATION_TOPIC =
        new MAPIProperty(0x70, ASCII_STRING, "ConversationTopic", "PR_CONVERSATION_TOPIC");
    public static final MAPIProperty CONVERSION_EITS =
        new MAPIProperty(12, BINARY, "ConversionEits", "PR_CONVERSION_EITS");
    public static final MAPIProperty CONVERSION_PROHIBITED =
        new MAPIProperty(0x3a03, BOOLEAN, "ConversionProhibited", "PR_CONVERSION_PROHIBITED");
    public static final MAPIProperty CONVERSION_WITH_LOSS_PROHIBITED =
        new MAPIProperty(13, BOOLEAN, "ConversionWithLossProhibited", "PR_CONVERSION_WITH_LOSS_PROHIBITED");
    public static final MAPIProperty CONVERTED_EITS =
        new MAPIProperty(14, BINARY, "ConvertedEits", "PR_CONVERTED_EITS");
    public static final MAPIProperty CORRELATE =
        new MAPIProperty(0xe0c, BOOLEAN, "Correlate", "PR_CORRELATE");
    public static final MAPIProperty CORRELATE_MTSID =
        new MAPIProperty(0xe0d, BINARY, "CorrelateMtsid", "PR_CORRELATE_MTSID");
    public static final MAPIProperty COUNTRY =
        new MAPIProperty(0x3a26, ASCII_STRING, "Country", "PR_COUNTRY");
    public static final MAPIProperty CREATE_TEMPLATES =
        new MAPIProperty(0x3604, DIRECTORY, "CreateTemplates", "PR_CREATE_TEMPLATES");
    public static final MAPIProperty CREATION_TIME =
        new MAPIProperty(0x3007, TIME, "CreationTime", "PR_CREATION_TIME");
    public static final MAPIProperty CREATION_VERSION =
        new MAPIProperty(0xe19, LONG_LONG, "CreationVersion", "PR_CREATION_VERSION");
    public static final MAPIProperty CURRENT_VERSION =
        new MAPIProperty(0xe00, LONG_LONG, "CurrentVersion", "PR_CURRENT_VERSION");
    public static final MAPIProperty CUSTOMER_ID =
        new MAPIProperty(0x3a4a, ASCII_STRING, "CustomerId", "PR_CUSTOMER_ID");
    public static final MAPIProperty DEF_CREATE_DL =
        new MAPIProperty(0x3611, BINARY, "DefCreateDl", "PR_DEF_CREATE_DL");
    public static final MAPIProperty DEF_CREATE_MAILUSER =
        new MAPIProperty(0x3612, BINARY, "DefCreateMailuser", "PR_DEF_CREATE_MAILUSER");
    public static final MAPIProperty DEFAULT_PROFILE =
        new MAPIProperty(0x3d04, BOOLEAN, "DefaultProfile", "PR_DEFAULT_PROFILE");
    public static final MAPIProperty DEFAULT_STORE =
        new MAPIProperty(0x3400, BOOLEAN, "DefaultStore", "PR_DEFAULT_STORE");
    public static final MAPIProperty DEFAULT_VIEW_ENTRY_ID =
        new MAPIProperty(0x3616, BINARY, "DefaultViewEntryId", "PR_DEFAULT_VIEW_ENTRYID");
    public static final MAPIProperty DEFERRED_DELIVERY_TIME =
        new MAPIProperty(15, TIME, "DeferredDeliveryTime", "PR_DEFERRED_DELIVERY_TIME");
    public static final MAPIProperty DELEGATION =
        new MAPIProperty(0x7e, BINARY, "Delegation", "PR_DELEGATION");
    public static final MAPIProperty DELETE_AFTER_SUBMIT =
        new MAPIProperty(0xe01, BOOLEAN, "DeleteAfterSubmit", "PR_DELETE_AFTER_SUBMIT");
    public static final MAPIProperty DELIVER_TIME =
        new MAPIProperty(0x10, TIME, "DeliverTime", "PR_DELIVER_TIME");
    public static final MAPIProperty DELIVERY_POINT =
        new MAPIProperty(0xc07, LONG, "DeliveryPoint", "PR_DELIVERY_POINT");
    public static final MAPIProperty DELTAX =
        new MAPIProperty(0x3f03, LONG, "Deltax", "PR_DELTAX");
    public static final MAPIProperty DELTAY =
        new MAPIProperty(0x3f04, LONG, "Deltay", "PR_DELTAY");
    public static final MAPIProperty DEPARTMENT_NAME =
        new MAPIProperty(0x3a18, ASCII_STRING, "DepartmentName", "PR_DEPARTMENT_NAME");
    public static final MAPIProperty DEPTH =
        new MAPIProperty(0x3005, LONG, "Depth", "PR_DEPTH");
    public static final MAPIProperty DETAILS_TABLE =
        new MAPIProperty(0x3605, DIRECTORY, "DetailsTable", "PR_DETAILS_TABLE");
    public static final MAPIProperty DISC_VAL =
        new MAPIProperty(0x4a, BOOLEAN, "DiscVal", "PR_DISC_VAL");
    public static final MAPIProperty DISCARD_REASON =
        new MAPIProperty(0x11, LONG, "DiscardReason", "PR_DISCARD_REASON");
    public static final MAPIProperty DISCLOSE_RECIPIENTS =
        new MAPIProperty(0x3a04, BOOLEAN, "DiscloseRecipients", "PR_DISCLOSE_RECIPIENTS");
    public static final MAPIProperty DISCLOSURE_OF_RECIPIENTS =
        new MAPIProperty(0x12, BOOLEAN, "DisclosureOfRecipients", "PR_DISCLOSURE_OF_RECIPIENTS");
    public static final MAPIProperty DISCRETE_VALUES =
        new MAPIProperty(0xe0e, BOOLEAN, "DiscreteValues", "PR_DISCRETE_VALUES");
    public static final MAPIProperty DISPLAY_BCC =
        new MAPIProperty(0xe02, ASCII_STRING, "DisplayBcc", "PR_DISPLAY_BCC");
    public static final MAPIProperty DISPLAY_CC =
        new MAPIProperty(0xe03, ASCII_STRING, "DisplayCc", "PR_DISPLAY_CC");
    public static final MAPIProperty DISPLAY_NAME =
        new MAPIProperty(0x3001, ASCII_STRING, "DisplayName", "PR_DISPLAY_NAME");
    public static final MAPIProperty DISPLAY_NAME_PREFIX =
        new MAPIProperty(0x3a45, ASCII_STRING, "DisplayNamePrefix", "PR_DISPLAY_NAME_PREFIX");
    public static final MAPIProperty DISPLAY_TO =
        new MAPIProperty(0xe04, ASCII_STRING, "DisplayTo", "PR_DISPLAY_TO");
    public static final MAPIProperty DISPLAY_TYPE =
        new MAPIProperty(0x3900, LONG, "DisplayType", "PR_DISPLAY_TYPE");
    public static final MAPIProperty DL_EXPANSION_HISTORY =
        new MAPIProperty(0x13, BINARY, "DlExpansionHistory", "PR_DL_EXPANSION_HISTORY");
    public static final MAPIProperty DL_EXPANSION_PROHIBITED =
        new MAPIProperty(20, BOOLEAN, "DlExpansionProhibited", "PR_DL_EXPANSION_PROHIBITED");
    public static final MAPIProperty EMAIL_ADDRESS =
        new MAPIProperty(0x3003, ASCII_STRING, "EmailAddress", "PR_EMAIL_ADDRESS");
    public static final MAPIProperty END_DATE =
        new MAPIProperty(0x61, TIME, "EndDate", "PR_END_DATE");
    public static final MAPIProperty ENTRY_ID =
        new MAPIProperty(0xfff, BINARY, "EntryId", "PR_ENTRYID");
    public static final MAPIProperty EXPAND_BEGIN_TIME =
        new MAPIProperty(0x3618, Types.UNKNOWN,  "ExpandBeginTime", "PR_EXPAND_BEGIN_TIME");
    public static final MAPIProperty EXPAND_END_TIME =
        new MAPIProperty(0x3619, Types.UNKNOWN,  "ExpandEndTime", "PR_EXPAND_END_TIME");
    public static final MAPIProperty EXPANDED_BEGIN_TIME =
        new MAPIProperty(0x361a, Types.UNKNOWN,  "ExpandedBeginTime", "PR_EXPANDED_BEGIN_TIME");
    public static final MAPIProperty EXPANDED_END_TIME =
        new MAPIProperty(0x361b, Types.UNKNOWN,  "ExpandedEndTime", "PR_EXPANDED_END_TIME");
    public static final MAPIProperty EXPIRY_TIME =
        new MAPIProperty(0x15, TIME, "ExpiryTime", "PR_EXPIRY_TIME");
    public static final MAPIProperty EXPLICIT_CONVERSION =
        new MAPIProperty(0xc01, LONG, "ExplicitConversion", "PR_EXPLICIT_CONVERSION");
    public static final MAPIProperty FILTERING_HOOKS =
        new MAPIProperty(0x3d08, BINARY, "FilteringHooks", "PR_FILTERING_HOOKS");
    public static final MAPIProperty FINDER_ENTRY_ID =
        new MAPIProperty(0x35e7, BINARY, "FinderEntryId", "PR_FINDER_ENTRYID");
    public static final MAPIProperty FOLDER_ASSOCIATED_CONTENTS =
        new MAPIProperty(0x3610, DIRECTORY, "FolderAssociatedContents", "PR_FOLDER_ASSOCIATED_CONTENTS");
    public static final MAPIProperty FOLDER_TYPE =
        new MAPIProperty(0x3601, LONG, "FolderType", "PR_FOLDER_TYPE");
    public static final MAPIProperty FORM_CATEGORY =
        new MAPIProperty(0x3304, ASCII_STRING, "FormCategory", "PR_FORM_CATEGORY");
    public static final MAPIProperty FORM_CATEGORY_SUB =
        new MAPIProperty(0x3305, ASCII_STRING, "FormCategorySub", "PR_FORM_CATEGORY_SUB");
    public static final MAPIProperty FORM_CLSID =
        new MAPIProperty(0x3302, CLS_ID, "FormClsid", "PR_FORM_ClsID");
    public static final MAPIProperty FORM_CONTACT_NAME =
        new MAPIProperty(0x3303, ASCII_STRING, "FormContactName", "PR_FORM_CONTACT_NAME");
    public static final MAPIProperty FORM_DESIGNER_GUID =
        new MAPIProperty(0x3309, CLS_ID, "FormDesignerGuid", "PR_FORM_DESIGNER_GUID");
    public static final MAPIProperty FORM_DESIGNER_NAME =
        new MAPIProperty(0x3308, ASCII_STRING, "FormDesignerName", "PR_FORM_DESIGNER_NAME");
    public static final MAPIProperty FORM_HIDDEN =
        new MAPIProperty(0x3307, BOOLEAN, "FormHidden", "PR_FORM_HIDDEN");
    public static final MAPIProperty FORM_HOST_MAP =
        new MAPIProperty(0x3306, Types.createCustom(4099), "FormHostMap", "PR_FORM_HOST_MAP");
    public static final MAPIProperty FORM_MESSAGE_BEHAVIOR =
        new MAPIProperty(0x330a, LONG, "FormMessageBehavior", "PR_FORM_MESSAGE_BEHAVIOR");
    public static final MAPIProperty FORM_VERSION =
        new MAPIProperty(0x3301, ASCII_STRING, "FormVersion", "PR_FORM_VERSION");
    public static final MAPIProperty FTP_SITE =
        new MAPIProperty(0x3a4c, ASCII_STRING, "FtpSite", "PR_FTP_SITE");
    public static final MAPIProperty GENDER =
        new MAPIProperty(0x3a4d, SHORT, "Gender", "PR_GENDER");
    public static final MAPIProperty GENERATION =
        new MAPIProperty(0x3a05, ASCII_STRING, "Generation", "PR_GENERATION");
    public static final MAPIProperty GIVEN_NAME =
        new MAPIProperty(0x3a06, ASCII_STRING, "GivenName", "PR_GIVEN_NAME");
    public static final MAPIProperty GOVERNMENT_ID_NUMBER =
        new MAPIProperty(0x3a07, ASCII_STRING, "GovernmentIdNumber", "PR_GOVERNMENT_ID_NUMBER");
    public static final MAPIProperty HASATTACH =
        new MAPIProperty(0xe1b, BOOLEAN, "Hasattach", "PR_HASATTACH");
    public static final MAPIProperty HEADER_FOLDER_ENTRY_ID =
        new MAPIProperty(0x3e0a, BINARY, "HeaderFolderEntryId", "PR_HEADER_FOLDER_ENTRYID");
    public static final MAPIProperty HOBBIES =
        new MAPIProperty(0x3a43, ASCII_STRING, "Hobbies", "PR_HOBBIES");
    public static final MAPIProperty HOME2_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a2f, ASCII_STRING, "Home2TelephoneNumber", "PR_HOME2_TELEPHONE_NUMBER");
    public static final MAPIProperty HOME_ADDRESS_CITY =
        new MAPIProperty(0x3a59, ASCII_STRING, "HomeAddressCity", "PR_HOME_ADDRESS_CITY");
    public static final MAPIProperty HOME_ADDRESS_COUNTRY =
        new MAPIProperty(0x3a5a, ASCII_STRING, "HomeAddressCountry", "PR_HOME_ADDRESS_COUNTRY");
    public static final MAPIProperty HOME_ADDRESS_POST_OFFICE_BOX =
        new MAPIProperty(0x3a5e, ASCII_STRING, "HomeAddressPostOfficeBox", "PR_HOME_ADDRESS_POST_OFFICE_BOX");
    public static final MAPIProperty HOME_ADDRESS_POSTAL_CODE =
        new MAPIProperty(0x3a5b, ASCII_STRING, "HomeAddressPostalCode", "PR_HOME_ADDRESS_POSTAL_CODE");
    public static final MAPIProperty HOME_ADDRESS_STATE_OR_PROVINCE =
        new MAPIProperty(0x3a5c, ASCII_STRING, "HomeAddressStateOrProvince", "PR_HOME_ADDRESS_STATE_OR_PROVINCE");
    public static final MAPIProperty HOME_ADDRESS_STREET =
        new MAPIProperty(0x3a5d, ASCII_STRING, "HomeAddressStreet", "PR_HOME_ADDRESS_STREET");
    public static final MAPIProperty HOME_FAX_NUMBER =
        new MAPIProperty(0x3a25, ASCII_STRING, "HomeFaxNumber", "PR_HOME_FAX_NUMBER");
    public static final MAPIProperty HOME_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a09, ASCII_STRING, "HomeTelephoneNumber", "PR_HOME_TELEPHONE_NUMBER");
    public static final MAPIProperty INET_MAIL_OVERRIDE_CHARSET =
        new MAPIProperty(0x5903, Types.UNKNOWN,  "INetMailOverrideCharset", "Charset");
    public static final MAPIProperty INET_MAIL_OVERRIDE_FORMAT =
        new MAPIProperty(0x5902, Types.UNKNOWN,  "INetMailOverrideFormat", "Format");
    public static final MAPIProperty ICON =
        new MAPIProperty(0xffd, BINARY, "Icon", "PR_ICON");
    public static final MAPIProperty IDENTITY_DISPLAY =
        new MAPIProperty(0x3e00, ASCII_STRING, "IdentityDisplay", "PR_IDENTITY_DISPLAY");
    public static final MAPIProperty IDENTITY_ENTRY_ID =
        new MAPIProperty(0x3e01, BINARY, "IdentityEntryId", "PR_IDENTITY_ENTRYID");
    public static final MAPIProperty IDENTITY_SEARCH_KEY =
        new MAPIProperty(0x3e05, BINARY, "IdentitySearchKey", "PR_IDENTITY_SEARCH_KEY");
    public static final MAPIProperty IMPLICIT_CONVERSION_PROHIBITED =
        new MAPIProperty(0x16, BOOLEAN, "ImplicitConversionProhibited", "PR_IMPLICIT_CONVERSION_PROHIBITED");
    public static final MAPIProperty IMPORTANCE =
        new MAPIProperty(0x17, LONG, "Importance", "PR_IMPORTANCE");
    public static final MAPIProperty IN_REPLY_TO_ID =
        new MAPIProperty(0x1042, Types.UNKNOWN,  "InReplyToId", "PR_IN_REPLY_TO_ID");
    public static final MAPIProperty INCOMPLETE_COPY =
        new MAPIProperty(0x35, BOOLEAN, "IncompleteCopy", "PR_INCOMPLETE_COPY");
    public static final MAPIProperty INITIAL_DETAILS_PANE =
        new MAPIProperty(0x3f08, LONG, "InitialDetailsPane", "PR_INITIAL_DETAILS_PANE");
    public static final MAPIProperty INITIALS =
        new MAPIProperty(0x3a0a, ASCII_STRING, "Initials", "PR_INITIALS");
    public static final MAPIProperty INSTANCE_KEY =
        new MAPIProperty(0xff6, BINARY, "InstanceKey", "PR_INSTANCE_KEY");
    public static final MAPIProperty INTERNET_APPROVED =
        new MAPIProperty(0x1030, ASCII_STRING, "InternetApproved", "PR_INTERNET_APPROVED");
    public static final MAPIProperty INTERNET_ARTICLE_NUMBER =
        new MAPIProperty(0xe23, LONG, "InternetArticleNumber", "PR_INTERNET_ARTICLE_NUMBER");
    public static final MAPIProperty INTERNET_CPID =
        new MAPIProperty(0x3fde, Types.LONG,  "InternetCPID", "PR_INTERNET_CPID");
    public static final MAPIProperty INTERNET_CONTROL =
        new MAPIProperty(0x1031, ASCII_STRING, "InternetControl", "PR_INTERNET_CONTROL");
    public static final MAPIProperty INTERNET_DISTRIBUTION =
        new MAPIProperty(0x1032, ASCII_STRING, "InternetDistribution", "PR_INTERNET_DISTRIBUTION");
    public static final MAPIProperty INTERNET_FOLLOWUP_TO =
        new MAPIProperty(0x1033, ASCII_STRING, "InternetFollowupTo", "PR_INTERNET_FOLLOWUP_TO");
    public static final MAPIProperty INTERNET_LINES =
        new MAPIProperty(0x1034, LONG, "InternetLines", "PR_INTERNET_LINES");
    public static final MAPIProperty INTERNET_MESSAGE_ID =
        new MAPIProperty(0x1035, ASCII_STRING, "InternetMessageId", "PR_INTERNET_MESSAGE_ID");
    public static final MAPIProperty INTERNET_NEWSGROUPS =
        new MAPIProperty(0x1036, ASCII_STRING, "InternetNewsgroups", "PR_INTERNET_NEWSGROUPS");
    public static final MAPIProperty INTERNET_NNTP_PATH =
        new MAPIProperty(0x1038, ASCII_STRING, "InternetNntpPath", "PR_INTERNET_NNTP_PATH");
    public static final MAPIProperty INTERNET_ORGANIZATION =
        new MAPIProperty(0x1037, ASCII_STRING, "InternetOrganization", "PR_INTERNET_ORGANIZATION");
    public static final MAPIProperty INTERNET_PRECEDENCE =
        new MAPIProperty(0x1041, ASCII_STRING, "InternetPrecedence", "PR_INTERNET_PRECEDENCE");
    public static final MAPIProperty INTERNET_REFERENCES =
        new MAPIProperty(0x1039, ASCII_STRING, "InternetReferences", "PR_INTERNET_REFERENCES");
    public static final MAPIProperty IPM_ID =
        new MAPIProperty(0x18, BINARY, "IpmId", "PR_IPM_ID");
    public static final MAPIProperty IPM_OUTBOX_ENTRY_ID =
        new MAPIProperty(0x35e2, BINARY, "IpmOutboxEntryId", "PR_IPM_OUTBOX_ENTRYID");
    public static final MAPIProperty IPM_OUTBOX_SEARCH_KEY =
        new MAPIProperty(0x3411, BINARY, "IpmOutboxSearchKey", "PR_IPM_OUTBOX_SEARCH_KEY");
    public static final MAPIProperty IPM_RETURN_REQUESTED =
        new MAPIProperty(0xc02, BOOLEAN, "IpmReturnRequested", "PR_IPM_RETURN_REQUESTED");
    public static final MAPIProperty IPM_SENTMAIL_ENTRY_ID =
        new MAPIProperty(0x35e4, BINARY, "IpmSentmailEntryId", "PR_IPM_SENTMAIL_ENTRYID");
    public static final MAPIProperty IPM_SENTMAIL_SEARCH_KEY =
        new MAPIProperty(0x3413, BINARY, "IpmSentmailSearchKey", "PR_IPM_SENTMAIL_SEARCH_KEY");
    public static final MAPIProperty IPM_SUBTREE_ENTRY_ID =
        new MAPIProperty(0x35e0, BINARY, "IpmSubtreeEntryId", "PR_IPM_SUBTREE_ENTRYID");
    public static final MAPIProperty IPM_SUBTREE_SEARCH_KEY =
        new MAPIProperty(0x3410, BINARY, "IpmSubtreeSearchKey", "PR_IPM_SUBTREE_SEARCH_KEY");
    public static final MAPIProperty IPM_WASTEBASKET_ENTRY_ID =
        new MAPIProperty(0x35e3, BINARY, "IpmWastebasketEntryId", "PR_IPM_WASTEBASKET_ENTRYID");
    public static final MAPIProperty IPM_WASTEBASKET_SEARCH_KEY =
        new MAPIProperty(0x3412, BINARY, "IpmWastebasketSearchKey", "PR_IPM_WASTEBASKET_SEARCH_KEY");
    public static final MAPIProperty ISDN_NUMBER =
        new MAPIProperty(0x3a2d, ASCII_STRING, "IsdnNumber", "PR_ISDN_NUMBER");
    public static final MAPIProperty KEYWORD =
        new MAPIProperty(0x3a0b, ASCII_STRING, "Keyword", "PR_KEYWORD");
    public static final MAPIProperty LANGUAGE =
        new MAPIProperty(0x3a0c, ASCII_STRING, "Language", "PR_LANGUAGE");
    public static final MAPIProperty LANGUAGES =
        new MAPIProperty(0x2f, ASCII_STRING, "Languages", "PR_LANGUAGES");
    public static final MAPIProperty LAST_MODIFICATION_TIME =
        new MAPIProperty(0x3008, TIME, "LastModificationTime", "PR_LAST_MODIFICATION_TIME");
    public static final MAPIProperty LATEST_DELIVERY_TIME =
        new MAPIProperty(0x19, TIME, "LatestDeliveryTime", "PR_LATEST_DELIVERY_TIME");
    public static final MAPIProperty LIST_HELP =
        new MAPIProperty(0x1043, Types.UNKNOWN,  "ListHelp", "PR_LIST_HELP");
    public static final MAPIProperty LIST_SUBSCRIBE =
        new MAPIProperty(0x1044, Types.UNKNOWN,  "ListSubscribe", "PR_LIST_SUBSCRIBE");
    public static final MAPIProperty LIST_UNSUBSCRIBE =
        new MAPIProperty(0x1045, Types.UNKNOWN,  "ListUnsubscribe", "PR_LIST_UNSUBSCRIBE");
    public static final MAPIProperty LOCALITY =
        new MAPIProperty(0x3a27, ASCII_STRING, "Locality", "PR_LOCALITY");
    public static final MAPIProperty LOCALLY_DELIVERED =
        new MAPIProperty(0x6745, Types.UNKNOWN,  "LocallyDelivered", "ptagLocallyDelivered");
    public static final MAPIProperty LOCATION =
        new MAPIProperty(0x3a0d, ASCII_STRING, "Location", "PR_LOCATION");
    public static final MAPIProperty LOCK_BRANCH_ID =
        new MAPIProperty(0x3800, Types.UNKNOWN,  "LockBranchId", "PR_LOCK_BRANCH_ID");
    public static final MAPIProperty LOCK_DEPTH =
        new MAPIProperty(0x3808, Types.UNKNOWN,  "LockDepth", "PR_LOCK_DEPTH");
    public static final MAPIProperty LOCK_ENLISTMENT_CONTEXT =
        new MAPIProperty(0x3804, Types.UNKNOWN,  "LockEnlistmentContext", "PR_LOCK_ENLISTMENT_CONTEXT");
    public static final MAPIProperty LOCK_EXPIRY_TIME =
        new MAPIProperty(0x380a, Types.UNKNOWN,  "LockExpiryTime", "PR_LOCK_EXPIRY_TIME");
    public static final MAPIProperty LOCK_PERSISTENT =
        new MAPIProperty(0x3807, Types.UNKNOWN,  "LockPersistent", "PR_LOCK_PERSISTENT");
    public static final MAPIProperty LOCK_RESOURCE_DID =
        new MAPIProperty(0x3802, Types.UNKNOWN,  "LockResourceDid", "PR_LOCK_RESOURCE_DID");
    public static final MAPIProperty LOCK_RESOURCE_FID =
        new MAPIProperty(0x3801, Types.UNKNOWN,  "LockResourceFid", "PR_LOCK_RESOURCE_FID");
    public static final MAPIProperty LOCK_RESOURCE_MID =
        new MAPIProperty(0x3803, Types.UNKNOWN,  "LockResourceMid", "PR_LOCK_RESOURCE_MID");
    public static final MAPIProperty LOCK_SCOPE =
        new MAPIProperty(0x3806, Types.UNKNOWN,  "LockScope", "PR_LOCK_SCOPE");
    public static final MAPIProperty LOCK_TIMEOUT =
        new MAPIProperty(0x3809, Types.UNKNOWN,  "LockTimeout", "PR_LOCK_TIMEOUT");
    public static final MAPIProperty LOCK_TYPE =
        new MAPIProperty(0x3805, Types.UNKNOWN,  "LockType", "PR_LOCK_TYPE");
    public static final MAPIProperty MAIL_PERMISSION =
        new MAPIProperty(0x3a0e, BOOLEAN, "MailPermission", "PR_MAIL_PERMISSION");
    public static final MAPIProperty MANAGER_NAME =
        new MAPIProperty(0x3a4e, ASCII_STRING, "ManagerName", "PR_MANAGER_NAME");
    public static final MAPIProperty MAPPING_SIGNATURE =
        new MAPIProperty(0xff8, BINARY, "MappingSignature", "PR_MAPPING_SIGNATURE");
    public static final MAPIProperty MDB_PROVIDER =
        new MAPIProperty(0x3414, BINARY, "MdbProvider", "PR_MDB_PROVIDER");
    public static final MAPIProperty MESSAGE_ATTACHMENTS =
        new MAPIProperty(0xe13, DIRECTORY, "MessageAttachments", "PR_MESSAGE_ATTACHMENTS");
    public static final MAPIProperty MESSAGE_CC_ME =
        new MAPIProperty(0x58, BOOLEAN, "MessageCcMe", "PR_MESSAGE_CC_ME");
    public static final MAPIProperty MESSAGE_CLASS =
        new MAPIProperty(0x1a, ASCII_STRING, "MessageClass", "PR_MESSAGE_CLASS");
    public static final MAPIProperty MESSAGE_CODEPAGE =
        new MAPIProperty(0x3ffd, Types.LONG,  "MessageCodepage", "PR_MESSAGE_CODEPAGE");
    public static final MAPIProperty MESSAGE_LOCALE_ID =
        new MAPIProperty(0x3ff1, Types.LONG,  "MessageLocaleId", "PR_MESSAGE_LOCALE_ID");
    public static final MAPIProperty MESSAGE_DELIVERY_ID =
        new MAPIProperty(0x1b, BINARY, "MessageDeliveryId", "PR_MESSAGE_DELIVERY_ID");
    public static final MAPIProperty MESSAGE_DELIVERY_TIME =
        new MAPIProperty(0xe06, TIME, "MessageDeliveryTime", "PR_MESSAGE_DELIVERY_TIME");
    public static final MAPIProperty MESSAGE_DOWNLOAD_TIME =
        new MAPIProperty(0xe18, LONG, "MessageDownloadTime", "PR_MESSAGE_DOWNLOAD_TIME");
    public static final MAPIProperty MESSAGE_FLAGS =
        new MAPIProperty(0xe07, LONG, "MessageFlags", "PR_MESSAGE_FLAGS");
    public static final MAPIProperty MESSAGE_RECIP_ME =
        new MAPIProperty(0x59, BOOLEAN, "MessageRecipMe", "PR_MESSAGE_RECIP_ME");
    public static final MAPIProperty MESSAGE_RECIPIENTS =
        new MAPIProperty(0xe12, DIRECTORY, "MessageRecipients", "PR_MESSAGE_RECIPIENTS");
    public static final MAPIProperty MESSAGE_SECURITY_LABEL =
        new MAPIProperty(30, BINARY, "MessageSecurityLabel", "PR_MESSAGE_SECURITY_LABEL");
    public static final MAPIProperty MESSAGE_SIZE =
        new MAPIProperty(0xe08, LONG, "MessageSize", "PR_MESSAGE_SIZE");
    public static final MAPIProperty MESSAGE_SUBMISSION_ID =
        new MAPIProperty(0x47, BINARY, "MessageSubmissionId", "PR_MESSAGE_SUBMISSION_ID");
    public static final MAPIProperty MESSAGE_TO_ME =
        new MAPIProperty(0x57, BOOLEAN, "MessageToMe", "PR_MESSAGE_TO_ME");
    public static final MAPIProperty MESSAGE_TOKEN =
        new MAPIProperty(0xc03, BINARY, "MessageToken", "PR_MESSAGE_TOKEN");
    public static final MAPIProperty MHS_COMMON_NAME =
        new MAPIProperty(0x3a0f, ASCII_STRING, "MhsCommonName", "PR_MHS_COMMON_NAME");
    public static final MAPIProperty MIDDLE_NAME =
        new MAPIProperty(0x3a44, ASCII_STRING, "MiddleName", "PR_MIDDLE_NAME");
    public static final MAPIProperty MINI_ICON =
        new MAPIProperty(0xffc, BINARY, "MiniIcon", "PR_MINI_ICON");
    public static final MAPIProperty MOBILE_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1c, ASCII_STRING, "MobileTelephoneNumber", "PR_MOBILE_TELEPHONE_NUMBER");
    public static final MAPIProperty MODIFY_VERSION =
        new MAPIProperty(0xe1a, LONG_LONG, "ModifyVersion", "PR_MODIFY_VERSION");
    public static final MAPIProperty MSG_STATUS =
        new MAPIProperty(0xe17, LONG, "MsgStatus", "PR_MSG_STATUS");
    public static final MAPIProperty NDR_DIAG_CODE =
        new MAPIProperty(0xc05, LONG, "NdrDiagCode", "PR_NDR_DIAG_CODE");
    public static final MAPIProperty NDR_REASON_CODE =
        new MAPIProperty(0xc04, LONG, "NdrReasonCode", "PR_NDR_REASON_CODE");
    public static final MAPIProperty NDR_STATUS_CODE =
        new MAPIProperty(0xc20, Types.UNKNOWN,  "NdrStatusCode", "PR_NDR_STATUS_CODE");
    public static final MAPIProperty NEWSGROUP_NAME =
        new MAPIProperty(0xe24, ASCII_STRING, "NewsgroupName", "PR_NEWSGROUP_NAME");
    public static final MAPIProperty NICKNAME =
        new MAPIProperty(0x3a4f, ASCII_STRING, "Nickname", "PR_NICKNAME");
    public static final MAPIProperty NNTP_XREF =
        new MAPIProperty(0x1040, ASCII_STRING, "NntpXref", "PR_NNTP_XREF");
    public static final MAPIProperty NON_RECEIPT_NOTIFICATION_REQUESTED =
        new MAPIProperty(0xc06, BOOLEAN, "NonReceiptNotificationRequested", "PR_NON_RECEIPT_NOTIFICATION_REQUESTED");
    public static final MAPIProperty NON_RECEIPT_REASON =
        new MAPIProperty(0x3e, LONG, "NonReceiptReason", "PR_NON_RECEIPT_REASON");
    public static final MAPIProperty NORMALIZED_SUBJECT =
        new MAPIProperty(0xe1d, ASCII_STRING, "NormalizedSubject", "PR_NORMALIZED_SUBJECT");
    public static final MAPIProperty NT_SECURITY_DESCRIPTOR =
        new MAPIProperty(0xe27, Types.UNKNOWN,  "NtSecurityDescriptor", "PR_NT_SECURITY_DESCRIPTOR");
    public static final MAPIProperty NULL =
        new MAPIProperty(1, LONG, "Null", "PR_NULL");
    public static final MAPIProperty OBJECT_TYPE =
        new MAPIProperty(0xffe, LONG, "ObjectType", "PR_Object_TYPE");
    public static final MAPIProperty OBSOLETED_IPMS =
        new MAPIProperty(0x1f, BINARY, "ObsoletedIpms", "PR_OBSOLETED_IPMS");
    public static final MAPIProperty OFFICE2_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1b, ASCII_STRING, "Office2TelephoneNumber", "PR_OFFICE2_TELEPHONE_NUMBER");
    public static final MAPIProperty OFFICE_LOCATION =
        new MAPIProperty(0x3a19, ASCII_STRING, "OfficeLocation", "PR_OFFICE_LOCATION");
    public static final MAPIProperty OFFICE_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a08, ASCII_STRING, "OfficeTelephoneNumber", "PR_OFFICE_TELEPHONE_NUMBER");
    public static final MAPIProperty OOF_REPLY_TYPE =
        new MAPIProperty(0x4080, Types.UNKNOWN,  "OofReplyType", "PR_OOF_REPLY_TYPE");
    public static final MAPIProperty ORGANIZATIONAL_ID_NUMBER =
        new MAPIProperty(0x3a10, ASCII_STRING, "OrganizationalIdNumber", "PR_ORGANIZATIONAL_ID_NUMBER");
    public static final MAPIProperty ORIG_ENTRY_ID =
        new MAPIProperty(0x300f, Types.UNKNOWN,  "OrigEntryId", "PR_ORIG_ENTRYID");
    public static final MAPIProperty ORIG_MESSAGE_CLASS =
        new MAPIProperty(0x4b, ASCII_STRING, "OrigMessageClass", "PR_ORIG_MESSAGE_CLASS");
    public static final MAPIProperty ORIGIN_CHECK =
        new MAPIProperty(0x27, BINARY, "OriginCheck", "PR_ORIGIN_CHECK");
    public static final MAPIProperty ORIGINAL_AUTHOR_ADDRTYPE =
        new MAPIProperty(0x79, ASCII_STRING, "OriginalAuthorAddrtype", "PR_ORIGINAL_AUTHOR_ADDRTYPE");
    public static final MAPIProperty ORIGINAL_AUTHOR_EMAIL_ADDRESS =
        new MAPIProperty(0x7a, ASCII_STRING, "OriginalAuthorEmailAddress", "PR_ORIGINAL_AUTHOR_EMAIL_ADDRESS");
    public static final MAPIProperty ORIGINAL_AUTHOR_ENTRY_ID =
        new MAPIProperty(0x4c, BINARY, "OriginalAuthorEntryId", "PR_ORIGINAL_AUTHOR_ENTRYID");
    public static final MAPIProperty ORIGINAL_AUTHOR_NAME =
        new MAPIProperty(0x4d, ASCII_STRING, "OriginalAuthorName", "PR_ORIGINAL_AUTHOR_NAME");
    public static final MAPIProperty ORIGINAL_AUTHOR_SEARCH_KEY =
        new MAPIProperty(0x56, BINARY, "OriginalAuthorSearchKey", "PR_ORIGINAL_AUTHOR_SEARCH_KEY");
    public static final MAPIProperty ORIGINAL_DELIVERY_TIME =
        new MAPIProperty(0x55, TIME, "OriginalDeliveryTime", "PR_ORIGINAL_DELIVERY_TIME");
    public static final MAPIProperty ORIGINAL_DISPLAY_BCC =
        new MAPIProperty(0x72, ASCII_STRING, "OriginalDisplayBcc", "PR_ORIGINAL_DISPLAY_BCC");
    public static final MAPIProperty ORIGINAL_DISPLAY_CC =
        new MAPIProperty(0x73, ASCII_STRING, "OriginalDisplayCc", "PR_ORIGINAL_DISPLAY_CC");
    public static final MAPIProperty ORIGINAL_DISPLAY_NAME =
        new MAPIProperty(0x3a13, ASCII_STRING, "OriginalDisplayName", "PR_ORIGINAL_DISPLAY_NAME");
    public static final MAPIProperty ORIGINAL_DISPLAY_TO =
        new MAPIProperty(0x74, ASCII_STRING, "OriginalDisplayTo", "PR_ORIGINAL_DISPLAY_TO");
    public static final MAPIProperty ORIGINAL_EITS =
        new MAPIProperty(0x21, BINARY, "OriginalEits", "PR_ORIGINAL_EITS");
    public static final MAPIProperty ORIGINAL_ENTRY_ID =
        new MAPIProperty(0x3a12, BINARY, "OriginalEntryId", "PR_ORIGINAL_ENTRYID");
    public static final MAPIProperty ORIGINAL_SEARCH_KEY =
        new MAPIProperty(0x3a14, BINARY, "OriginalSearchKey", "PR_ORIGINAL_SEARCH_KEY");
    public static final MAPIProperty ORIGINAL_SENDER_ADDRTYPE =
        new MAPIProperty(0x66, ASCII_STRING, "OriginalSenderAddrtype", "PR_ORIGINAL_SENDER_ADDRTYPE");
    public static final MAPIProperty ORIGINAL_SENDER_EMAIL_ADDRESS =
        new MAPIProperty(0x67, ASCII_STRING, "OriginalSenderEmailAddress", "PR_ORIGINAL_SENDER_EMAIL_ADDRESS");
    public static final MAPIProperty ORIGINAL_SENDER_ENTRY_ID =
        new MAPIProperty(0x5b, BINARY, "OriginalSenderEntryId", "PR_ORIGINAL_SENDER_ENTRYID");
    public static final MAPIProperty ORIGINAL_SENDER_NAME =
        new MAPIProperty(90, ASCII_STRING, "OriginalSenderName", "PR_ORIGINAL_SENDER_NAME");
    public static final MAPIProperty ORIGINAL_SENDER_SEARCH_KEY =
        new MAPIProperty(0x5c, BINARY, "OriginalSenderSearchKey", "PR_ORIGINAL_SENDER_SEARCH_KEY");
    public static final MAPIProperty ORIGINAL_SENSITIVITY =
        new MAPIProperty(0x2e, LONG, "OriginalSensitivity", "PR_ORIGINAL_SENSITIVITY");
    public static final MAPIProperty ORIGINAL_SENT_REPRESENTING_ADDRTYPE =
        new MAPIProperty(0x68, ASCII_STRING, "OriginalSentRepresentingAddrtype", "PR_ORIGINAL_SENT_REPRESENTING_ADDRTYPE");
    public static final MAPIProperty ORIGINAL_SENT_REPRESENTING_EMAIL_ADDRESS =
        new MAPIProperty(0x69, ASCII_STRING, "OriginalSentRepresentingEmailAddress", "PR_ORIGINAL_SENT_REPRESENTING_EMAIL_ADDRESS");
    public static final MAPIProperty ORIGINAL_SENT_REPRESENTING_ENTRY_ID =
        new MAPIProperty(0x5e, BINARY, "OriginalSentRepresentingEntryId", "PR_ORIGINAL_SENT_REPRESENTING_ENTRYID");
    public static final MAPIProperty ORIGINAL_SENT_REPRESENTING_NAME =
        new MAPIProperty(0x5d, ASCII_STRING, "OriginalSentRepresentingName", "PR_ORIGINAL_SENT_REPRESENTING_NAME");
    public static final MAPIProperty ORIGINAL_SENT_REPRESENTING_SEARCH_KEY =
        new MAPIProperty(0x5f, BINARY, "OriginalSentRepresentingSearchKey", "PR_ORIGINAL_SENT_REPRESENTING_SEARCH_KEY");
    public static final MAPIProperty ORIGINAL_SUBJECT =
        new MAPIProperty(0x49, ASCII_STRING, "OriginalSubject", "PR_ORIGINAL_SUBJECT");
    public static final MAPIProperty ORIGINAL_SUBMIT_TIME =
        new MAPIProperty(0x4e, TIME, "OriginalSubmitTime", "PR_ORIGINAL_SUBMIT_TIME");
    public static final MAPIProperty ORIGINALLY_INTENDED_RECIP_ADDRTYPE =
        new MAPIProperty(0x7b, ASCII_STRING, "OriginallyIntendedRecipAddrtype", "PR_ORIGINALLY_INTENDED_RECIP_ADDRTYPE");
    public static final MAPIProperty ORIGINALLY_INTENDED_RECIP_EMAIL_ADDRESS =
        new MAPIProperty(0x7c, ASCII_STRING, "OriginallyIntendedRecipEmailAddress", "PR_ORIGINALLY_INTENDED_RECIP_EMAIL_ADDRESS");
    public static final MAPIProperty ORIGINALLY_INTENDED_RECIP_ENTRY_ID =
        new MAPIProperty(0x1012, BINARY, "OriginallyIntendedRecipEntryId", "PR_ORIGINALLY_INTENDED_RECIP_ENTRYID");
    public static final MAPIProperty ORIGINALLY_INTENDED_RECIPIENT_NAME =
        new MAPIProperty(0x20, BINARY, "OriginallyIntendedRecipientName", "PR_ORIGINALLY_INTENDED_RECIPIENT_NAME");
    public static final MAPIProperty ORIGINATING_MTA_CERTIFICATE =
        new MAPIProperty(0xe25, BINARY, "OriginatingMtaCertificate", "PR_ORIGINATING_MTA_CERTIFICATE");
    public static final MAPIProperty ORIGINATOR_AND_DL_EXPANSION_HISTORY =
        new MAPIProperty(0x1002, BINARY, "OriginatorAndDlExpansionHistory", "PR_ORIGINATOR_AND_DL_EXPANSION_HISTORY");
    public static final MAPIProperty ORIGINATOR_CERTIFICATE =
        new MAPIProperty(0x22, BINARY, "OriginatorCertificate", "PR_ORIGINATOR_CERTIFICATE");
    public static final MAPIProperty ORIGINATOR_DELIVERY_REPORT_REQUESTED =
        new MAPIProperty(0x23, BOOLEAN, "OriginatorDeliveryReportRequested", "PR_ORIGINATOR_DELIVERY_REPORT_REQUESTED");
    public static final MAPIProperty ORIGINATOR_NON_DELIVERY_REPORT_REQUESTED =
        new MAPIProperty(0xc08, BOOLEAN, "OriginatorNonDeliveryReportRequested", "PR_ORIGINATOR_NON_DELIVERY_REPORT_REQUESTED");
    public static final MAPIProperty ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT =
        new MAPIProperty(0xc09, BINARY, "OriginatorRequestedAlternateRecipient", "PR_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT");
    public static final MAPIProperty ORIGINATOR_RETURN_ADDRESS =
        new MAPIProperty(0x24, BINARY, "OriginatorReturnAddress", "PR_ORIGINATOR_RETURN_ADDRESS");
    public static final MAPIProperty OTHER_ADDRESS_CITY =
        new MAPIProperty(0x3a5f, ASCII_STRING, "OtherAddressCity", "PR_OTHER_ADDRESS_CITY");
    public static final MAPIProperty OTHER_ADDRESS_COUNTRY =
        new MAPIProperty(0x3a60, ASCII_STRING, "OtherAddressCountry", "PR_OTHER_ADDRESS_COUNTRY");
    public static final MAPIProperty OTHER_ADDRESS_POST_OFFICE_BOX =
        new MAPIProperty(0x3a64, ASCII_STRING, "OtherAddressPostOfficeBox", "PR_OTHER_ADDRESS_POST_OFFICE_BOX");
    public static final MAPIProperty OTHER_ADDRESS_POSTAL_CODE =
        new MAPIProperty(0x3a61, ASCII_STRING, "OtherAddressPostalCode", "PR_OTHER_ADDRESS_POSTAL_CODE");
    public static final MAPIProperty OTHER_ADDRESS_STATE_OR_PROVINCE =
        new MAPIProperty(0x3a62, ASCII_STRING, "OtherAddressStateOrProvince", "PR_OTHER_ADDRESS_STATE_OR_PROVINCE");
    public static final MAPIProperty OTHER_ADDRESS_STREET =
        new MAPIProperty(0x3a63, ASCII_STRING, "OtherAddressStreet", "PR_OTHER_ADDRESS_STREET");
    public static final MAPIProperty OTHER_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1f, ASCII_STRING, "OtherTelephoneNumber", "PR_OTHER_TELEPHONE_NUMBER");
    public static final MAPIProperty OWN_STORE_ENTRY_ID =
        new MAPIProperty(0x3e06, BINARY, "OwnStoreEntryId", "PR_OWN_STORE_ENTRYID");
    public static final MAPIProperty OWNER_APPT_ID =
        new MAPIProperty(0x62, LONG, "OwnerApptId", "PR_OWNER_APPT_ID");
    public static final MAPIProperty PAGER_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a21, ASCII_STRING, "PagerTelephoneNumber", "PR_PAGER_TELEPHONE_NUMBER");
    public static final MAPIProperty PARENT_DISPLAY =
        new MAPIProperty(0xe05, ASCII_STRING, "ParentDisplay", "PR_PARENT_DISPLAY");
    public static final MAPIProperty PARENT_ENTRY_ID =
        new MAPIProperty(0xe09, BINARY, "ParentEntryId", "PR_PARENT_ENTRYID");
    public static final MAPIProperty PARENT_KEY =
        new MAPIProperty(0x25, BINARY, "ParentKey", "PR_PARENT_KEY");
    public static final MAPIProperty PERSONAL_HOME_PAGE =
        new MAPIProperty(0x3a50, ASCII_STRING, "PersonalHomePage", "PR_PERSONAL_HOME_PAGE");
    public static final MAPIProperty PHYSICAL_DELIVERY_BUREAU_FAX_DELIVERY =
        new MAPIProperty(0xc0a, BOOLEAN, "PhysicalDeliveryBureauFaxDelivery", "PR_PHYSICAL_DELIVERY_BUREAU_FAX_DELIVERY");
    public static final MAPIProperty PHYSICAL_DELIVERY_MODE =
        new MAPIProperty(0xc0b, LONG, "PhysicalDeliveryMode", "PR_PHYSICAL_DELIVERY_MODE");
    public static final MAPIProperty PHYSICAL_DELIVERY_REPORT_REQUEST =
        new MAPIProperty(0xc0c, LONG, "PhysicalDeliveryReportRequest", "PR_PHYSICAL_DELIVERY_REPORT_REQUEST");
    public static final MAPIProperty PHYSICAL_FORWARDING_ADDRESS =
        new MAPIProperty(0xc0d, BINARY, "PhysicalForwardingAddress", "PR_PHYSICAL_FORWARDING_ADDRESS");
    public static final MAPIProperty PHYSICAL_FORWARDING_ADDRESS_REQUESTED =
        new MAPIProperty(0xc0e, BOOLEAN, "PhysicalForwardingAddressRequested", "PR_PHYSICAL_FORWARDING_ADDRESS_REQUESTED");
    public static final MAPIProperty PHYSICAL_FORWARDING_PROHIBITED =
        new MAPIProperty(0xc0f, BOOLEAN, "PhysicalForwardingProhibited", "PR_PHYSICAL_FORWARDING_PROHIBITED");
    public static final MAPIProperty PHYSICAL_RENDITION_ATTRIBUTES =
        new MAPIProperty(0xc10, BINARY, "PhysicalRenditionAttributes", "PR_PHYSICAL_RENDITION_ATTRIBUTES");
    public static final MAPIProperty POST_FOLDER_ENTRIES =
        new MAPIProperty(0x103b, BINARY, "PostFolderEntries", "PR_POST_FOLDER_ENTRIES");
    public static final MAPIProperty POST_FOLDER_NAMES =
        new MAPIProperty(0x103c, ASCII_STRING, "PostFolderNames", "PR_POST_FOLDER_NAMES");
    public static final MAPIProperty POST_OFFICE_BOX =
        new MAPIProperty(0x3a2b, ASCII_STRING, "PostOfficeBox", "PR_POST_OFFICE_BOX");
    public static final MAPIProperty POST_REPLY_DENIED =
        new MAPIProperty(0x103f, BINARY, "PostReplyDenied", "PR_POST_REPLY_DENIED");
    public static final MAPIProperty POST_REPLY_FOLDER_ENTRIES =
        new MAPIProperty(0x103d, BINARY, "PostReplyFolderEntries", "PR_POST_REPLY_FOLDER_ENTRIES");
    public static final MAPIProperty POST_REPLY_FOLDER_NAMES =
        new MAPIProperty(0x103e, ASCII_STRING, "PostReplyFolderNames", "PR_POST_REPLY_FOLDER_NAMES");
    public static final MAPIProperty POSTAL_ADDRESS =
        new MAPIProperty(0x3a15, ASCII_STRING, "PostalAddress", "PR_POSTAL_ADDRESS");
    public static final MAPIProperty POSTAL_CODE =
        new MAPIProperty(0x3a2a, ASCII_STRING, "PostalCode", "PR_POSTAL_CODE");
    public static final MAPIProperty PREPROCESS =
        new MAPIProperty(0xe22, BOOLEAN, "Preprocess", "PR_PREPROCESS");
    public static final MAPIProperty PRIMARY_CAPABILITY =
        new MAPIProperty(0x3904, BINARY, "PrimaryCapability", "PR_PRIMARY_CAPABILITY");
    public static final MAPIProperty PRIMARY_FAX_NUMBER =
        new MAPIProperty(0x3a23, ASCII_STRING, "PrimaryFaxNumber", "PR_PRIMARY_FAX_NUMBER");
    public static final MAPIProperty PRIMARY_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1a, ASCII_STRING, "PrimaryTelephoneNumber", "PR_PRIMARY_TELEPHONE_NUMBER");
    public static final MAPIProperty PRIORITY =
        new MAPIProperty(0x26, LONG, "Priority", "PR_PRIORITY");
    public static final MAPIProperty PROFESSION =
        new MAPIProperty(0x3a46, ASCII_STRING, "Profession", "PR_PROFESSION");
    public static final MAPIProperty PROFILE_NAME =
        new MAPIProperty(0x3d12, ASCII_STRING, "ProfileName", "PR_PROFILE_NAME");
    public static final MAPIProperty PROOF_OF_DELIVERY =
        new MAPIProperty(0xc11, BINARY, "ProofOfDelivery", "PR_PROOF_OF_DELIVERY");
    public static final MAPIProperty PROOF_OF_DELIVERY_REQUESTED =
        new MAPIProperty(0xc12, BOOLEAN, "ProofOfDeliveryRequested", "PR_PROOF_OF_DELIVERY_REQUESTED");
    public static final MAPIProperty PROOF_OF_SUBMISSION =
        new MAPIProperty(0xe26, BINARY, "ProofOfSubmission", "PR_PROOF_OF_SUBMISSION");
    public static final MAPIProperty PROOF_OF_SUBMISSION_REQUESTED =
        new MAPIProperty(40, BOOLEAN, "ProofOfSubmissionRequested", "PR_PROOF_OF_SUBMISSION_REQUESTED");
    public static final MAPIProperty PROP_ID_SECURE_MAX =
        new MAPIProperty(0x67ff, Types.UNKNOWN,  "PropIdSecureMax", "PROP_ID_SECURE_MAX");
    public static final MAPIProperty PROP_ID_SECURE_MIN =
        new MAPIProperty(0x67f0, Types.UNKNOWN,  "PropIdSecureMin", "PROP_ID_SECURE_MIN");
    public static final MAPIProperty PROVIDER_DISPLAY =
        new MAPIProperty(0x3006, ASCII_STRING, "ProviderDisplay", "PR_PROVIDER_DISPLAY");
    public static final MAPIProperty PROVIDER_DLL_NAME =
        new MAPIProperty(0x300a, ASCII_STRING, "ProviderDllName", "PR_PROVIDER_DLL_NAME");
    public static final MAPIProperty PROVIDER_ORDINAL =
        new MAPIProperty(0x300d, LONG, "ProviderOrdinal", "PR_PROVIDER_ORDINAL");
    public static final MAPIProperty PROVIDER_SUBMIT_TIME =
        new MAPIProperty(0x48, TIME, "ProviderSubmitTime", "PR_PROVIDER_SUBMIT_TIME");
    public static final MAPIProperty PROVIDER_UID =
        new MAPIProperty(0x300c, BINARY, "ProviderUid", "PR_PROVIDER_UID");
    public static final MAPIProperty PUID =
        new MAPIProperty(0x300e, Types.UNKNOWN, "Puid", "PR_PUID");
    public static final MAPIProperty RADIO_TELEPHONE_NUMBER =
        new MAPIProperty(0x3a1d, ASCII_STRING, "RadioTelephoneNumber", "PR_RADIO_TELEPHONE_NUMBER");
    public static final MAPIProperty RCVD_REPRESENTING_ADDRTYPE =
        new MAPIProperty(0x77, ASCII_STRING, "RcvdRepresentingAddrtype", "PR_RCVD_REPRESENTING_ADDRTYPE");
    public static final MAPIProperty RCVD_REPRESENTING_EMAIL_ADDRESS =
        new MAPIProperty(120, ASCII_STRING, "RcvdRepresentingEmailAddress", "PR_RCVD_REPRESENTING_EMAIL_ADDRESS");
    public static final MAPIProperty RCVD_REPRESENTING_ENTRY_ID =
        new MAPIProperty(0x43, BINARY, "RcvdRepresentingEntryId", "PR_RCVD_REPRESENTING_ENTRYID");
    public static final MAPIProperty RCVD_REPRESENTING_NAME =
        new MAPIProperty(0x44, ASCII_STRING, "RcvdRepresentingName", "PR_RCVD_REPRESENTING_NAME");
    public static final MAPIProperty RCVD_REPRESENTING_SEARCH_KEY =
        new MAPIProperty(0x52, BINARY, "RcvdRepresentingSearchKey", "PR_RCVD_REPRESENTING_SEARCH_KEY");
    public static final MAPIProperty READ_RECEIPT_ENTRY_ID =
        new MAPIProperty(70, BINARY, "ReadReceiptEntryId", "PR_READ_RECEIPT_ENTRYID");
    public static final MAPIProperty READ_RECEIPT_REQUESTED =
        new MAPIProperty(0x29, BOOLEAN, "ReadReceiptRequested", "PR_READ_RECEIPT_REQUESTED");
    public static final MAPIProperty READ_RECEIPT_SEARCH_KEY =
        new MAPIProperty(0x53, BINARY, "ReadReceiptSearchKey", "PR_READ_RECEIPT_SEARCH_KEY");
    public static final MAPIProperty RECEIPT_TIME =
        new MAPIProperty(0x2a, TIME, "ReceiptTime", "PR_RECEIPT_TIME");
    public static final MAPIProperty RECEIVE_FOLDER_SETTINGS =
        new MAPIProperty(0x3415, DIRECTORY, "ReceiveFolderSettings", "PR_RECEIVE_FOLDER_SETTINGS");
    public static final MAPIProperty RECEIVED_BY_ADDRTYPE =
        new MAPIProperty(0x75, ASCII_STRING, "ReceivedByAddrtype", "PR_RECEIVED_BY_ADDRTYPE");
    public static final MAPIProperty RECEIVED_BY_EMAIL_ADDRESS =
        new MAPIProperty(0x76, ASCII_STRING, "ReceivedByEmailAddress", "PR_RECEIVED_BY_EMAIL_ADDRESS");
    public static final MAPIProperty RECEIVED_BY_ENTRY_ID =
        new MAPIProperty(0x3f, BINARY, "ReceivedByEntryId", "PR_RECEIVED_BY_ENTRYID");
    public static final MAPIProperty RECEIVED_BY_NAME =
        new MAPIProperty(0x40, ASCII_STRING, "ReceivedByName", "PR_RECEIVED_BY_NAME");
    public static final MAPIProperty RECEIVED_BY_SMTP_ADDRESS =
        new MAPIProperty(0x5D07, ASCII_STRING, "ReceivedBySmtpAddress", "PR_RECEIVED_BY_SMTP_ADDRESS");
    public static final MAPIProperty RECIPIENT_DISPLAY_NAME =
        new MAPIProperty(0x5ff6, Types.UNICODE_STRING, "RecipientDisplayName", null);
    public static final MAPIProperty RECIPIENT_ENTRY_ID =
        new MAPIProperty(0x5ff7, Types.UNKNOWN, "RecipientEntryId", null);
    public static final MAPIProperty RECIPIENT_FLAGS =
        new MAPIProperty(0x5ffd, Types.UNKNOWN, "RecipientFlags", null);
    public static final MAPIProperty RECEIVED_BY_SEARCH_KEY =
        new MAPIProperty(0x51, BINARY, "ReceivedBySearchKey", "PR_RECEIVED_BY_SEARCH_KEY");
    public static final MAPIProperty RECIPIENT_CERTIFICATE =
        new MAPIProperty(0xc13, BINARY, "RecipientCertificate", "PR_RECIPIENT_CERTIFICATE");
    public static final MAPIProperty RECIPIENT_NUMBER_FOR_ADVICE =
        new MAPIProperty(0xc14, ASCII_STRING, "RecipientNumberForAdvice", "PR_RECIPIENT_NUMBER_FOR_ADVICE");
    public static final MAPIProperty RECIPIENT_REASSIGNMENT_PROHIBITED =
        new MAPIProperty(0x2b, BOOLEAN, "RecipientReassignmentProhibited", "PR_RECIPIENT_REASSIGNMENT_PROHIBITED");
    public static final MAPIProperty RECIPIENT_STATUS =
        new MAPIProperty(0xe15, LONG, "RecipientStatus", "PR_RECIPIENT_STATUS");
    public static final MAPIProperty RECIPIENT_TYPE =
        new MAPIProperty(0xc15, LONG, "RecipientType", "PR_RECIPIENT_TYPE");
    public static final MAPIProperty RECORD_KEY =
        new MAPIProperty(0xff9, BINARY, "RecordKey", "PR_RECORD_KEY");
    public static final MAPIProperty REDIRECTION_HISTORY =
        new MAPIProperty(0x2c, BINARY, "RedirectionHistory", "PR_REDIRECTION_HISTORY");
    public static final MAPIProperty REFERRED_BY_NAME =
        new MAPIProperty(0x3a47, ASCII_STRING, "ReferredByName", "PR_REFERRED_BY_NAME");
    public static final MAPIProperty REGISTERED_MAIL_TYPE =
        new MAPIProperty(0xc16, LONG, "RegisteredMailType", "PR_REGISTERED_MAIL_TYPE");
    public static final MAPIProperty RELATED_IPMS =
        new MAPIProperty(0x2d, BINARY, "RelatedIpms", "PR_RELATED_IPMS");
    public static final MAPIProperty REMOTE_PROGRESS =
        new MAPIProperty(0x3e0b, LONG, "RemoteProgress", "PR_REMOTE_PROGRESS");
    public static final MAPIProperty REMOTE_PROGRESS_TEXT =
        new MAPIProperty(0x3e0c, ASCII_STRING, "RemoteProgressText", "PR_REMOTE_PROGRESS_TEXT");
    public static final MAPIProperty REMOTE_VALIDATE_OK =
        new MAPIProperty(0x3e0d, BOOLEAN, "RemoteValidateOk", "PR_REMOTE_VALIDATE_OK");
    public static final MAPIProperty RENDERING_POSITION =
        new MAPIProperty(0x370b, LONG, "RenderingPosition", "PR_RENDERING_POSITION");
    public static final MAPIProperty REPLY_RECIPIENT_ENTRIES =
        new MAPIProperty(0x4f, BINARY, "ReplyRecipientEntries", "PR_REPLY_RECIPIENT_ENTRIES");
    public static final MAPIProperty REPLY_RECIPIENT_NAMES =
        new MAPIProperty(80, ASCII_STRING, "ReplyRecipientNames", "PR_REPLY_RECIPIENT_NAMES");
    public static final MAPIProperty REPLY_REQUESTED =
        new MAPIProperty(0xc17, BOOLEAN, "ReplyRequested", "PR_REPLY_REQUESTED");
    public static final MAPIProperty REPLY_TIME =
        new MAPIProperty(0x30, TIME, "ReplyTime", "PR_REPLY_TIME");
    public static final MAPIProperty REPORT_ENTRY_ID =
        new MAPIProperty(0x45, BINARY, "ReportEntryId", "PR_REPORT_ENTRYID");
    public static final MAPIProperty REPORT_NAME =
        new MAPIProperty(0x3a, ASCII_STRING, "ReportName", "PR_REPORT_NAME");
    public static final MAPIProperty REPORT_SEARCH_KEY =
        new MAPIProperty(0x54, BINARY, "ReportSearchKey", "PR_REPORT_SEARCH_KEY");
    public static final MAPIProperty REPORT_TAG =
        new MAPIProperty(0x31, BINARY, "ReportTag", "PR_REPORT_TAG");
    public static final MAPIProperty REPORT_TEXT =
        new MAPIProperty(0x1001, ASCII_STRING, "ReportText", "PR_REPORT_TEXT");
    public static final MAPIProperty REPORT_TIME =
        new MAPIProperty(50, TIME, "ReportTime", "PR_REPORT_TIME");
    public static final MAPIProperty REPORTING_DL_NAME =
        new MAPIProperty(0x1003, BINARY, "ReportingDlName", "PR_REPORTING_DL_NAME");
    public static final MAPIProperty REPORTING_MTA_CERTIFICATE =
        new MAPIProperty(0x1004, BINARY, "ReportingMtaCertificate", "PR_REPORTING_MTA_CERTIFICATE");
    public static final MAPIProperty REQUESTED_DELIVERY_METHOD =
        new MAPIProperty(0xc18, LONG, "RequestedDeliveryMethod", "PR_REQUESTED_DELIVERY_METHOD");
    public static final MAPIProperty RESOURCE_FLAGS =
        new MAPIProperty(0x3009, LONG, "ResourceFlags", "PR_RESOURCE_FLAGS");
    public static final MAPIProperty RESOURCE_METHODS =
        new MAPIProperty(0x3e02, LONG, "ResourceMethods", "PR_RESOURCE_METHODS");
    public static final MAPIProperty RESOURCE_PATH =
        new MAPIProperty(0x3e07, ASCII_STRING, "ResourcePath", "PR_RESOURCE_PATH");
    public static final MAPIProperty RESOURCE_TYPE =
        new MAPIProperty(0x3e03, LONG, "ResourceType", "PR_RESOURCE_TYPE");
    public static final MAPIProperty RESPONSE_REQUESTED =
        new MAPIProperty(0x63, BOOLEAN, "ResponseRequested", "PR_RESPONSE_REQUESTED");
    public static final MAPIProperty RESPONSIBILITY =
        new MAPIProperty(0xe0f, BOOLEAN, "Responsibility", "PR_RESPONSIBILITY");
    public static final MAPIProperty RETURNED_IPM =
        new MAPIProperty(0x33, BOOLEAN, "ReturnedIpm", "PR_RETURNED_IPM");
    public static final MAPIProperty ROW_TYPE =
        new MAPIProperty(0xff5, LONG, "RowType", "PR_ROW_TYPE");
    public static final MAPIProperty ROWID =
        new MAPIProperty(0x3000, LONG, "Rowid", "PR_ROWID");
    public static final MAPIProperty RTF_COMPRESSED =
        new MAPIProperty(0x1009, BINARY, "RtfCompressed", "PR_RTF_COMPRESSED");
    public static final MAPIProperty RTF_IN_SYNC =
        new MAPIProperty(0xe1f, BOOLEAN, "RtfInSync", "PR_RTF_IN_SYNC");
    public static final MAPIProperty RTF_SYNC_BODY_COUNT =
        new MAPIProperty(0x1007, LONG, "RtfSyncBodyCount", "PR_RTF_SYNC_BODY_COUNT");
    public static final MAPIProperty RTF_SYNC_BODY_CRC =
        new MAPIProperty(0x1006, LONG, "RtfSyncBodyCrc", "PR_RTF_SYNC_BODY_CRC");
    public static final MAPIProperty RTF_SYNC_BODY_TAG =
        new MAPIProperty(0x1008, ASCII_STRING, "RtfSyncBodyTag", "PR_RTF_SYNC_BODY_TAG");
    public static final MAPIProperty RTF_SYNC_PREFIX_COUNT =
        new MAPIProperty(0x1010, LONG, "RtfSyncPrefixCount", "PR_RTF_SYNC_PREFIX_COUNT");
    public static final MAPIProperty RTF_SYNC_TRAILING_COUNT =
        new MAPIProperty(0x1011, LONG, "RtfSyncTrailingCount", "PR_RTF_SYNC_TRAILING_COUNT");
    public static final MAPIProperty SEARCH =
        new MAPIProperty(0x3607, DIRECTORY, "Search", "PR_SEARCH");
    public static final MAPIProperty SEARCH_KEY =
        new MAPIProperty(0x300b, BINARY, "SearchKey", "PR_SEARCH_KEY");
    public static final MAPIProperty SECURITY =
        new MAPIProperty(0x34, LONG, "Security", "PR_SECURITY");
    public static final MAPIProperty SELECTABLE =
        new MAPIProperty(0x3609, BOOLEAN, "Selectable", "PR_SELECTABLE");
    public static final MAPIProperty SEND_INTERNET_ENCODING =
        new MAPIProperty(0x3a71, LONG, "SendInternetEncoding", "PR_SEND_INTERNET_ENCODING");
    public static final MAPIProperty SEND_RECALL_REPORT =
        new MAPIProperty(0x6803, Types.UNKNOWN, "SendRecallReport", "messages");
    public static final MAPIProperty SEND_RICH_INFO =
        new MAPIProperty(0x3a40, BOOLEAN, "SendRichInfo", "PR_SEND_RICH_INFO");
    public static final MAPIProperty SENDER_ADDRTYPE =
        new MAPIProperty(0xc1e, ASCII_STRING, "SenderAddrtype", "PR_SENDER_ADDRTYPE");
    public static final MAPIProperty SENDER_EMAIL_ADDRESS =
        new MAPIProperty(0xc1f, ASCII_STRING, "SenderEmailAddress", "PR_SENDER_EMAIL_ADDRESS");
    public static final MAPIProperty SENDER_ENTRY_ID =
        new MAPIProperty(0xc19, BINARY, "SenderEntryId", "PR_SENDER_ENTRYID");
    public static final MAPIProperty SENDER_NAME =
        new MAPIProperty(0xc1a, ASCII_STRING, "SenderName", "PR_SENDER_NAME");
    public static final MAPIProperty SENDER_SEARCH_KEY =
        new MAPIProperty(0xc1d, BINARY, "SenderSearchKey", "PR_SENDER_SEARCH_KEY");
    public static final MAPIProperty SENSITIVITY =
        new MAPIProperty(0x36, LONG, "Sensitivity", "PR_SENSITIVITY");
    public static final MAPIProperty SENT_REPRESENTING_ADDRTYPE =
        new MAPIProperty(100, ASCII_STRING, "SentRepresentingAddrtype", "PR_SENT_REPRESENTING_ADDRTYPE");
    public static final MAPIProperty SENT_REPRESENTING_EMAIL_ADDRESS =
        new MAPIProperty(0x65, ASCII_STRING, "SentRepresentingEmailAddress", "PR_SENT_REPRESENTING_EMAIL_ADDRESS");
    public static final MAPIProperty SENT_REPRESENTING_ENTRY_ID =
        new MAPIProperty(0x41, BINARY, "SentRepresentingEntryId", "PR_SENT_REPRESENTING_ENTRYID");
    public static final MAPIProperty SENT_REPRESENTING_NAME =
        new MAPIProperty(0x42, ASCII_STRING, "SentRepresentingName", "PR_SENT_REPRESENTING_NAME");
    public static final MAPIProperty SENT_REPRESENTING_SEARCH_KEY =
        new MAPIProperty(0x3b, BINARY, "SentRepresentingSearchKey", "PR_SENT_REPRESENTING_SEARCH_KEY");
    public static final MAPIProperty SENTMAIL_ENTRY_ID =
        new MAPIProperty(0xe0a, BINARY, "SentmailEntryId", "PR_SENTMAIL_ENTRYID");
    public static final MAPIProperty SERVICE_DELETE_FILES =
        new MAPIProperty(0x3d10, Types.createCustom(4126), "ServiceDeleteFiles", "PR_SERVICE_DELETE_FILES");
    public static final MAPIProperty SERVICE_DLL_NAME =
        new MAPIProperty(0x3d0a, ASCII_STRING, "ServiceDllName", "PR_SERVICE_DLL_NAME");
    public static final MAPIProperty SERVICE_ENTRY_NAME =
        new MAPIProperty(0x3d0b, ASCII_STRING, "ServiceEntryName", "PR_SERVICE_ENTRY_NAME");
    public static final MAPIProperty SERVICE_EXTRA_UIDS =
        new MAPIProperty(0x3d0d, BINARY, "ServiceExtraUids", "PR_SERVICE_EXTRA_UIDS");
    public static final MAPIProperty SERVICE_NAME =
        new MAPIProperty(0x3d09, ASCII_STRING, "ServiceName", "PR_SERVICE_NAME");
    public static final MAPIProperty SERVICE_SUPPORT_FILES =
        new MAPIProperty(0x3d0f, Types.createCustom(4126), "ServiceSupportFiles", "PR_SERVICE_SUPPORT_FILES");
    public static final MAPIProperty SERVICE_UID =
        new MAPIProperty(0x3d0c, BINARY, "ServiceUid", "PR_SERVICE_UID");
    public static final MAPIProperty SERVICES =
        new MAPIProperty(0x3d0e, BINARY, "Services", "PR_SERVICES");
    public static final MAPIProperty SEVEN_BIT_DISPLAY_NAME =
        new MAPIProperty(0x39ff, ASCII_STRING, "SevenBitDisplayName", "PR_SEVEN_BIT_DISPLAY_NAME");
    public static final MAPIProperty SMTP_ADDRESS =
        new MAPIProperty(0x39fe, Types.UNICODE_STRING, "SmtpAddress", "PR_SMTP_ADDRESS");
    public static final MAPIProperty SPOOLER_STATUS =
        new MAPIProperty(0xe10, LONG, "SpoolerStatus", "PR_SPOOLER_STATUS");
    public static final MAPIProperty SPOUSE_NAME =
        new MAPIProperty(0x3a48, ASCII_STRING, "SpouseName", "PR_SPOUSE_NAME");
    public static final MAPIProperty START_DATE =
        new MAPIProperty(0x60, TIME, "StartDate", "PR_START_DATE");
    public static final MAPIProperty STATE_OR_PROVINCE =
        new MAPIProperty(0x3a28, ASCII_STRING, "StateOrProvince", "PR_STATE_OR_PROVINCE");
    public static final MAPIProperty STATUS =
        new MAPIProperty(0x360b, LONG, "Status", "PR_STATUS");
    public static final MAPIProperty STATUS_CODE =
        new MAPIProperty(0x3e04, LONG, "StatusCode", "PR_STATUS_CODE");
    public static final MAPIProperty STATUS_STRING =
        new MAPIProperty(0x3e08, ASCII_STRING, "StatusString", "PR_STATUS_STRING");
    public static final MAPIProperty STORE_ENTRY_ID =
        new MAPIProperty(0xffb, BINARY, "StoreEntryId", "PR_STORE_ENTRYID");
    public static final MAPIProperty STORE_PROVIDERS =
        new MAPIProperty(0x3d00, BINARY, "StoreProviders", "PR_STORE_PROVIDERS");
    public static final MAPIProperty STORE_RECORD_KEY =
        new MAPIProperty(0xffa, BINARY, "StoreRecordKey", "PR_STORE_RECORD_KEY");
    public static final MAPIProperty STORE_STATE =
        new MAPIProperty(0x340e, LONG, "StoreState", "PR_STORE_STATE");
    public static final MAPIProperty STORE_SUPPORT_MASK =
        new MAPIProperty(0x340d, LONG, "StoreSupportMask", "PR_STORE_SUPPORT_MASK");
    public static final MAPIProperty STREET_ADDRESS =
        new MAPIProperty(0x3a29, ASCII_STRING, "StreetAddress", "PR_STREET_ADDRESS");
    public static final MAPIProperty SUBFOLDERS =
        new MAPIProperty(0x360a, BOOLEAN, "Subfolders", "PR_SUBFOLDERS");
    public static final MAPIProperty SUBJECT =
        new MAPIProperty(0x37, ASCII_STRING, "Subject", "PR_SUBJECT");
    public static final MAPIProperty SUBJECT_IPM =
        new MAPIProperty(0x38, BINARY, "SubjectIpm", "PR_SUBJECT_IPM");
    public static final MAPIProperty SUBJECT_PREFIX =
        new MAPIProperty(0x3d, ASCII_STRING, "SubjectPrefix", "PR_SUBJECT_PREFIX");
    public static final MAPIProperty SUBMIT_FLAGS =
        new MAPIProperty(0xe14, LONG, "SubmitFlags", "PR_SUBMIT_FLAGS");
    public static final MAPIProperty SUPERSEDES =
        new MAPIProperty(0x103a, ASCII_STRING, "Supersedes", "PR_SUPERSEDES");
    public static final MAPIProperty SUPPLEMENTARY_INFO =
        new MAPIProperty(0xc1b, ASCII_STRING, "SupplementaryInfo", "PR_SUPPLEMENTARY_INFO");
    public static final MAPIProperty SURNAME =
        new MAPIProperty(0x3a11, ASCII_STRING, "Surname", "PR_SURNAME");
    public static final MAPIProperty TELEX_NUMBER =
        new MAPIProperty(0x3a2c, ASCII_STRING, "TelexNumber", "PR_TELEX_NUMBER");
    public static final MAPIProperty TEMPLATEID =
        new MAPIProperty(0x3902, BINARY, "Templateid", "PR_TEMPLATEID");
    public static final MAPIProperty TITLE =
        new MAPIProperty(0x3a17, ASCII_STRING, "Title", "PR_TITLE");
    public static final MAPIProperty TNEF_CORRELATION_KEY =
        new MAPIProperty(0x7f, BINARY, "TnefCorrelationKey", "PR_TNEF_CORRELATION_KEY");
    public static final MAPIProperty TRANSMITABLE_DISPLAY_NAME =
        new MAPIProperty(0x3a20, ASCII_STRING, "TransmitableDisplayName", "PR_TRANSMITABLE_DISPLAY_NAME");
    public static final MAPIProperty TRANSPORT_KEY =
        new MAPIProperty(0xe16, LONG, "TransportKey", "PR_TRANSPORT_KEY");
    public static final MAPIProperty TRANSPORT_MESSAGE_HEADERS =
        new MAPIProperty(0x7d, ASCII_STRING, "TransportMessageHeaders", "PR_TRANSPORT_MESSAGE_HEADERS");
    public static final MAPIProperty TRANSPORT_PROVIDERS =
        new MAPIProperty(0x3d02, BINARY, "TransportProviders", "PR_TRANSPORT_PROVIDERS");
    public static final MAPIProperty TRANSPORT_STATUS =
        new MAPIProperty(0xe11, LONG, "TransportStatus", "PR_TRANSPORT_STATUS");
    public static final MAPIProperty TTYTDD_PHONE_NUMBER =
        new MAPIProperty(0x3a4b, ASCII_STRING, "TtytddPhoneNumber", "PR_TTYTDD_PHONE_NUMBER");
    public static final MAPIProperty TYPE_OF_MTS_USER =
        new MAPIProperty(0xc1c, LONG, "TypeOfMtsUser", "PR_TYPE_OF_MTS_USER");
    public static final MAPIProperty USER_CERTIFICATE =
        new MAPIProperty(0x3a22, BINARY, "UserCertificate", "PR_USER_CERTIFICATE");
    public static final MAPIProperty USER_X509_CERTIFICATE =
        new MAPIProperty(0x3a70, Types.createCustom(4354), "UserX509Certificate", "PR_USER_X509_CERTIFICATE");
    public static final MAPIProperty VALID_FOLDER_MASK =
        new MAPIProperty(0x35df, LONG, "ValidFolderMask", "PR_VALID_FOLDER_MASK");
    public static final MAPIProperty VIEWS_ENTRY_ID =
        new MAPIProperty(0x35e5, BINARY, "ViewsEntryId", "PR_VIEWS_ENTRYID");
    public static final MAPIProperty WEDDING_ANNIVERSARY =
        new MAPIProperty(0x3a41, TIME, "WeddingAnniversary", "PR_WEDDING_ANNIVERSARY");
    public static final MAPIProperty X400_CONTENT_TYPE =
        new MAPIProperty(60, BINARY, "X400ContentType", "PR_X400_CONTENT_TYPE");
    public static final MAPIProperty X400_DEFERRED_DELIVERY_CANCEL =
        new MAPIProperty(0x3e09, BOOLEAN, "X400DeferredDeliveryCancel", "PR_X400_DEFERRED_DELIVERY_CANCEL");
    public static final MAPIProperty XPOS =
        new MAPIProperty(0x3f05, LONG, "Xpos", "PR_XPOS");
    public static final MAPIProperty YPOS =
        new MAPIProperty(0x3f06, LONG, "Ypos", "PR_YPOS");

    public static final MAPIProperty UNKNOWN =
        new MAPIProperty(-1, Types.UNKNOWN, "Unknown", null);

    // 0x8??? ones are outlook specific, and not standard MAPI
    // TODO See http://msdn.microsoft.com/en-us/library/ee157150%28v=exchg.80%29
    // for some
    // info on how we might decode them properly in the future
    private static final int ID_FIRST_CUSTOM = 0x8000;
    private static final int ID_LAST_CUSTOM = 0xFFFE;

    /* --------------------------------------------------------------------- */

    public final int id;
    public final MAPIType usualType;
    public final String name;
    public final String mapiProperty;

    private MAPIProperty(int id, MAPIType usualType, String name,
            String mapiProperty) {
        this.id = id;
        this.usualType = usualType;
        this.name = name;
        this.mapiProperty = mapiProperty;

        // If it isn't unknown or custom, store it for lookup
        if (id == -1
            || (id >= ID_FIRST_CUSTOM && id <= ID_LAST_CUSTOM)
            || (this instanceof CustomMAPIProperty)) {
            // Custom/Unknown, skip
        } else {
            if (attributes.containsKey(id)) {
                throw new IllegalArgumentException(
                    "Duplicate MAPI Property with ID " + id + " : "
                    + toString() + " vs "
                    + attributes.get(id));
            }
            attributes.put(id, this);
        }
    }

    public String asFileName() {
        StringBuilder str = new StringBuilder(Integer.toHexString(id).toUpperCase(Locale.ROOT));
        int need0count = 4 - str.length();
        if (need0count > 0) {
            str.insert(0, StringUtil.repeat('0', need0count));
        }
        return str + usualType.asFileEnding();
    }

    @Override
    public String toString() {
        return name + " [" + id + "]" + (mapiProperty == null ? "" : " (" + mapiProperty + ")");
    }

    public static MAPIProperty get(int id) {
        MAPIProperty attr = attributes.get(id);
        if (attr != null) {
            return attr;
        } else {
            return UNKNOWN;
        }
    }

    public static Collection<MAPIProperty> getAll() {
        return Collections.unmodifiableCollection(attributes.values());
    }

    public static MAPIProperty createCustom(int id, MAPIType type, String name) {
        return new CustomMAPIProperty(id, type, name, null);
    }

    private static final class CustomMAPIProperty extends MAPIProperty {
        private CustomMAPIProperty(int id, MAPIType usualType, String name, String mapiProperty) {
            super(id, usualType, name, mapiProperty);
        }
    }
}
