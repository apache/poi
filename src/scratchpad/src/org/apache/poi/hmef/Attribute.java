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
   
   // MAPI IDs taken from http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertyid%28v=EXCHG.140%29.aspx
   // TODO Merge this with the HSMF lists if appropriate
/*
    AbDefaultDir = 0x3d06,
    AbDefaultPab = 0x3d07,
    AbProviderId = 0x3615,
    AbProviders = 0x3d01,
    AbSearchPath = 0x3d05,
    AbSearchPathUpdate = 0x3d11,
    Access = 0xff4,
    AccessLevel = 0xff7,
    Account = 0x3a00,
    AcknowledgementMode = 1,
    Addrtype = 0x3002,
    AlternateRecipient = 0x3a01,
    AlternateRecipientAllowed = 2,
    Anr = 0x360c,
    Assistant = 0x3a30,
    AssistantTelephoneNumber = 0x3a2e,
    AssocContentCount = 0x3617,
    AttachAdditionalInfo = 0x370f,
    AttachContentBase = 0x3711,
    AttachContentId = 0x3712,
    AttachContentLocation = 0x3713,
    AttachData = 0x3701,
    AttachDisposition = 0x3716,
    AttachEncoding = 0x3702,
    AttachExtension = 0x3703,
    AttachFilename = 0x3704,
    AttachFlags = 0x3714,
    AttachLongFilename = 0x3707,
    AttachLongPathname = 0x370d,
    AttachmentX400Parameters = 0x3700,
    AttachMethod = 0x3705,
    AttachMimeSequence = 0x3710,
    AttachMimeTag = 0x370e,
    AttachNetscapeMacInfo = 0x3715,
    AttachNum = 0xe21,
    AttachPathname = 0x3708,
    AttachRendering = 0x3709,
    AttachSize = 0xe20,
    AttachTag = 0x370a,
    AttachTransportName = 0x370c,
    AuthorizingUsers = 3,
    AutoForwardComment = 4,
    AutoForwarded = 5,
    AutoResponseSuppress = 0x3fdf,
    BeeperTelephoneNumber = 0x3a21,
    Birthday = 0x3a42,
    Body = 0x1000,
    BodyContentId = 0x1015,
    BodyContentLocation = 0x1014,
    BodyCrc = 0xe1c,
    BodyHtml = 0x1013,
    Business2TelephoneNumber = 0x3a1b,
    BusinessAddressCity = 0x3a27,
    BusinessAddressCountry = 0x3a26,
    BusinessAddressPostalCode = 0x3a2a,
    BusinessAddressStreet = 0x3a29,
    BusinessFaxNumber = 0x3a24,
    BusinessHomePage = 0x3a51,
    CallbackTelephoneNumber = 0x3a02,
    CarTelephoneNumber = 0x3a1e,
    ChildrensNames = 0x3a58,
    ClientSubmitTime = 0x39,
    Comment = 0x3004,
    CommonViewsEntryId = 0x35e6,
    CompanyMainPhoneNumber = 0x3a57,
    CompanyName = 0x3a16,
    ComputerNetworkName = 0x3a49,
    ContactAddrtypes = 0x3a54,
    ContactDefaultAddressIndex = 0x3a55,
    ContactEmailAddresses = 0x3a56,
    ContactEntryIds = 0x3a53,
    ContactVersion = 0x3a52,
    ContainerClass = 0x3613,
    ContainerContents = 0x360f,
    ContainerFlags = 0x3600,
    ContainerHierarchy = 0x360e,
    ContainerModifyVersion = 0x3614,
    ContentConfidentialityAlgorithmId = 6,
    ContentCorrelator = 7,
    ContentCount = 0x3602,
    ContentIdentifier = 8,
    ContentIntegrityCheck = 0xc00,
    ContentLength = 9,
    ContentReturnRequested = 10,
    ContentsSortOrder = 0x360d,
    ContentUnread = 0x3603,
    ControlFlags = 0x3f00,
    ControlId = 0x3f07,
    ControlStructure = 0x3f01,
    ControlType = 0x3f02,
    ConversationIndex = 0x71,
    ConversationKey = 11,
    ConversationTopic = 0x70,
    ConversionEits = 12,
    ConversionProhibited = 0x3a03,
    ConversionWithLossProhibited = 13,
    ConvertedEits = 14,
    Correlate = 0xe0c,
    CorrelateMtsid = 0xe0d,
    Country = 0x3a26,
    CreateTemplates = 0x3604,
    CreationTime = 0x3007,
    CreationVersion = 0xe19,
    CurrentVersion = 0xe00,
    CustomerId = 0x3a4a,
    DefaultProfile = 0x3d04,
    DefaultStore = 0x3400,
    DefaultViewEntryId = 0x3616,
    DefCreateDl = 0x3611,
    DefCreateMailuser = 0x3612,
    DeferredDeliveryTime = 15,
    Delegation = 0x7e,
    DeleteAfterSubmit = 0xe01,
    DeliverTime = 0x10,
    DeliveryPoint = 0xc07,
    Deltax = 0x3f03,
    Deltay = 0x3f04,
    DepartmentName = 0x3a18,
    Depth = 0x3005,
    DetailsTable = 0x3605,
    DiscardReason = 0x11,
    DiscloseRecipients = 0x3a04,
    DisclosureOfRecipients = 0x12,
    DiscreteValues = 0xe0e,
    DiscVal = 0x4a,
    DisplayBcc = 0xe02,
    DisplayCc = 0xe03,
    DisplayName = 0x3001,
    DisplayNamePrefix = 0x3a45,
    DisplayTo = 0xe04,
    DisplayType = 0x3900,
    DlExpansionHistory = 0x13,
    DlExpansionProhibited = 20,
    EmailAddress = 0x3003,
    EndDate = 0x61,
    EntryId = 0xfff,
    ExpandBeginTime = 0x3618,
    ExpandedBeginTime = 0x361a,
    ExpandedEndTime = 0x361b,
    ExpandEndTime = 0x3619,
    ExpiryTime = 0x15,
    ExplicitConversion = 0xc01,
    FilteringHooks = 0x3d08,
    FinderEntryId = 0x35e7,
    FolderAssociatedContents = 0x3610,
    FolderType = 0x3601,
    FormCategory = 0x3304,
    FormCategorySub = 0x3305,
    FormClsid = 0x3302,
    FormContactName = 0x3303,
    FormDesignerGuid = 0x3309,
    FormDesignerName = 0x3308,
    FormHidden = 0x3307,
    FormHostMap = 0x3306,
    FormMessageBehavior = 0x330a,
    FormVersion = 0x3301,
    FtpSite = 0x3a4c,
    Gender = 0x3a4d,
    Generation = 0x3a05,
    GivenName = 0x3a06,
    GovernmentIdNumber = 0x3a07,
    Hasattach = 0xe1b,
    HeaderFolderEntryId = 0x3e0a,
    Hobbies = 0x3a43,
    Home2TelephoneNumber = 0x3a2f,
    HomeAddressCity = 0x3a59,
    HomeAddressCountry = 0x3a5a,
    HomeAddressPostalCode = 0x3a5b,
    HomeAddressPostOfficeBox = 0x3a5e,
    HomeAddressStateOrProvince = 0x3a5c,
    HomeAddressStreet = 0x3a5d,
    HomeFaxNumber = 0x3a25,
    HomeTelephoneNumber = 0x3a09,
    Icon = 0xffd,
    IdentityDisplay = 0x3e00,
    IdentityEntryId = 0x3e01,
    IdentitySearchKey = 0x3e05,
    ImplicitConversionProhibited = 0x16,
    Importance = 0x17,
    IncompleteCopy = 0x35,
    INetMailOverrideCharset = 0x5903,
    INetMailOverrideFormat = 0x5902,
    InitialDetailsPane = 0x3f08,
    Initials = 0x3a0a,
    InReplyToId = 0x1042,
    InstanceKey = 0xff6,
    InternetApproved = 0x1030,
    InternetArticleNumber = 0xe23,
    InternetControl = 0x1031,
    InternetCPID = 0x3fde,
    InternetDistribution = 0x1032,
    InternetFollowupTo = 0x1033,
    InternetLines = 0x1034,
    InternetMessageId = 0x1035,
    InternetNewsgroups = 0x1036,
    InternetNntpPath = 0x1038,
    InternetOrganization = 0x1037,
    InternetPrecedence = 0x1041,
    InternetReferences = 0x1039,
    IpmId = 0x18,
    IpmOutboxEntryId = 0x35e2,
    IpmOutboxSearchKey = 0x3411,
    IpmReturnRequested = 0xc02,
    IpmSentmailEntryId = 0x35e4,
    IpmSentmailSearchKey = 0x3413,
    IpmSubtreeEntryId = 0x35e0,
    IpmSubtreeSearchKey = 0x3410,
    IpmWastebasketEntryId = 0x35e3,
    IpmWastebasketSearchKey = 0x3412,
    IsdnNumber = 0x3a2d,
    Keyword = 0x3a0b,
    Language = 0x3a0c,
    Languages = 0x2f,
    LastModificationTime = 0x3008,
    LatestDeliveryTime = 0x19,
    ListHelp = 0x1043,
    ListSubscribe = 0x1044,
    ListUnsubscribe = 0x1045,
    Locality = 0x3a27,
    LocallyDelivered = 0x6745,
    Location = 0x3a0d,
    LockBranchId = 0x3800,
    LockDepth = 0x3808,
    LockEnlistmentContext = 0x3804,
    LockExpiryTime = 0x380a,
    LockPersistent = 0x3807,
    LockResourceDid = 0x3802,
    LockResourceFid = 0x3801,
    LockResourceMid = 0x3803,
    LockScope = 0x3806,
    LockTimeout = 0x3809,
    LockType = 0x3805,
    MailPermission = 0x3a0e,
    ManagerName = 0x3a4e,
    MappingSignature = 0xff8,
    MdbProvider = 0x3414,
    MessageAttachments = 0xe13,
    MessageCcMe = 0x58,
    MessageClass = 0x1a,
    MessageCodepage = 0x3ffd,
    MessageDeliveryId = 0x1b,
    MessageDeliveryTime = 0xe06,
    MessageDownloadTime = 0xe18,
    MessageFlags = 0xe07,
    MessageRecipients = 0xe12,
    MessageRecipMe = 0x59,
    MessageSecurityLabel = 30,
    MessageSize = 0xe08,
    MessageSubmissionId = 0x47,
    MessageToken = 0xc03,
    MessageToMe = 0x57,
    MhsCommonName = 0x3a0f,
    MiddleName = 0x3a44,
    MiniIcon = 0xffc,
    MobileTelephoneNumber = 0x3a1c,
    ModifyVersion = 0xe1a,
    MsgStatus = 0xe17,
    NdrDiagCode = 0xc05,
    NdrReasonCode = 0xc04,
    NdrStatusCode = 0xc20,
    NewsgroupName = 0xe24,
    Nickname = 0x3a4f,
    NntpXref = 0x1040,
    NonReceiptNotificationRequested = 0xc06,
    NonReceiptReason = 0x3e,
    NormalizedSubject = 0xe1d,
    NtSecurityDescriptor = 0xe27,
    Null = 1,
    ObjectType = 0xffe,
    ObsoletedIpms = 0x1f,
    Office2TelephoneNumber = 0x3a1b,
    OfficeLocation = 0x3a19,
    OfficeTelephoneNumber = 0x3a08,
    OofReplyType = 0x4080,
    OrganizationalIdNumber = 0x3a10,
    OrigEntryId = 0x300f,
    OriginalAuthorAddrtype = 0x79,
    OriginalAuthorEmailAddress = 0x7a,
    OriginalAuthorEntryId = 0x4c,
    OriginalAuthorName = 0x4d,
    OriginalAuthorSearchKey = 0x56,
    OriginalDeliveryTime = 0x55,
    OriginalDisplayBcc = 0x72,
    OriginalDisplayCc = 0x73,
    OriginalDisplayName = 0x3a13,
    OriginalDisplayTo = 0x74,
    OriginalEits = 0x21,
    OriginalEntryId = 0x3a12,
    OriginallyIntendedRecipAddrtype = 0x7b,
    OriginallyIntendedRecipEmailAddress = 0x7c,
    OriginallyIntendedRecipEntryId = 0x1012,
    OriginallyIntendedRecipientName = 0x20,
    OriginalSearchKey = 0x3a14,
    OriginalSenderAddrtype = 0x66,
    OriginalSenderEmailAddress = 0x67,
    OriginalSenderEntryId = 0x5b,
    OriginalSenderName = 90,
    OriginalSenderSearchKey = 0x5c,
    OriginalSensitivity = 0x2e,
    OriginalSentRepresentingAddrtype = 0x68,
    OriginalSentRepresentingEmailAddress = 0x69,
    OriginalSentRepresentingEntryId = 0x5e,
    OriginalSentRepresentingName = 0x5d,
    OriginalSentRepresentingSearchKey = 0x5f,
    OriginalSubject = 0x49,
    OriginalSubmitTime = 0x4e,
    OriginatingMtaCertificate = 0xe25,
    OriginatorAndDlExpansionHistory = 0x1002,
    OriginatorCertificate = 0x22,
    OriginatorDeliveryReportRequested = 0x23,
    OriginatorNonDeliveryReportRequested = 0xc08,
    OriginatorRequestedAlternateRecipient = 0xc09,
    OriginatorReturnAddress = 0x24,
    OriginCheck = 0x27,
    OrigMessageClass = 0x4b,
    OtherAddressCity = 0x3a5f,
    OtherAddressCountry = 0x3a60,
    OtherAddressPostalCode = 0x3a61,
    OtherAddressPostOfficeBox = 0x3a64,
    OtherAddressStateOrProvince = 0x3a62,
    OtherAddressStreet = 0x3a63,
    OtherTelephoneNumber = 0x3a1f,
    OwnerApptId = 0x62,
    OwnStoreEntryId = 0x3e06,
    PagerTelephoneNumber = 0x3a21,
    ParentDisplay = 0xe05,
    ParentEntryId = 0xe09,
    ParentKey = 0x25,
    PersonalHomePage = 0x3a50,
    PhysicalDeliveryBureauFaxDelivery = 0xc0a,
    PhysicalDeliveryMode = 0xc0b,
    PhysicalDeliveryReportRequest = 0xc0c,
    PhysicalForwardingAddress = 0xc0d,
    PhysicalForwardingAddressRequested = 0xc0e,
    PhysicalForwardingProhibited = 0xc0f,
    PhysicalRenditionAttributes = 0xc10,
    PostalAddress = 0x3a15,
    PostalCode = 0x3a2a,
    PostFolderEntries = 0x103b,
    PostFolderNames = 0x103c,
    PostOfficeBox = 0x3a2b,
    PostReplyDenied = 0x103f,
    PostReplyFolderEntries = 0x103d,
    PostReplyFolderNames = 0x103e,
    PreferredByName = 0x3a47,
    Preprocess = 0xe22,
    PrimaryCapability = 0x3904,
    PrimaryFaxNumber = 0x3a23,
    PrimaryTelephoneNumber = 0x3a1a,
    Priority = 0x26,
    Profession = 0x3a46,
    ProfileName = 0x3d12,
    ProofOfDelivery = 0xc11,
    ProofOfDeliveryRequested = 0xc12,
    ProofOfSubmission = 0xe26,
    ProofOfSubmissionRequested = 40,
    PropIdSecureMax = 0x67ff,
    PropIdSecureMin = 0x67f0,
    ProviderDisplay = 0x3006,
    ProviderDllName = 0x300a,
    ProviderOrdinal = 0x300d,
    ProviderSubmitTime = 0x48,
    ProviderUid = 0x300c,
    Puid = 0x300e,
    RadioTelephoneNumber = 0x3a1d,
    RcvdRepresentingAddrtype = 0x77,
    RcvdRepresentingEmailAddress = 120,
    RcvdRepresentingEntryId = 0x43,
    RcvdRepresentingName = 0x44,
    RcvdRepresentingSearchKey = 0x52,
    ReadReceiptEntryId = 70,
    ReadReceiptRequested = 0x29,
    ReadReceiptSearchKey = 0x53,
    ReceiptTime = 0x2a,
    ReceivedByAddrtype = 0x75,
    ReceivedByEmailAddress = 0x76,
    ReceivedByEntryId = 0x3f,
    ReceivedByName = 0x40,
    ReceivedBySearchKey = 0x51,
    ReceiveFolderSettings = 0x3415,
    RecipientCertificate = 0xc13,
    RecipientNumberForAdvice = 0xc14,
    RecipientReassignmentProhibited = 0x2b,
    RecipientStatus = 0xe15,
    RecipientType = 0xc15,
    RecordKey = 0xff9,
    RedirectionHistory = 0x2c,
    ReferredByName = 0x3a47,
    RegisteredMailType = 0xc16,
    RelatedIpms = 0x2d,
    RemoteProgress = 0x3e0b,
    RemoteProgressText = 0x3e0c,
    RemoteValidateOk = 0x3e0d,
    RenderingPosition = 0x370b,
    ReplyRecipientEntries = 0x4f,
    ReplyRecipientNames = 80,
    ReplyRequested = 0xc17,
    ReplyTime = 0x30,
    ReportEntryId = 0x45,
    ReportingDlName = 0x1003,
    ReportingMtaCertificate = 0x1004,
    ReportName = 0x3a,
    ReportSearchKey = 0x54,
    ReportTag = 0x31,
    ReportText = 0x1001,
    ReportTime = 50,
    RequestedDeliveryMethod = 0xc18,
    ResourceFlags = 0x3009,
    ResourceMethods = 0x3e02,
    ResourcePath = 0x3e07,
    ResourceType = 0x3e03,
    ResponseRequested = 0x63,
    Responsibility = 0xe0f,
    ReturnedIpm = 0x33,
    Rowid = 0x3000,
    RowType = 0xff5,
    RtfCompressed = 0x1009,
    RtfInSync = 0xe1f,
    RtfSyncBodyCount = 0x1007,
    RtfSyncBodyCrc = 0x1006,
    RtfSyncBodyTag = 0x1008,
    RtfSyncPrefixCount = 0x1010,
    RtfSyncTrailingCount = 0x1011,
    Search = 0x3607,
    SearchKey = 0x300b,
    Security = 0x34,
    Selectable = 0x3609,
    SenderAddrtype = 0xc1e,
    SenderEmailAddress = 0xc1f,
    SenderEntryId = 0xc19,
    SenderName = 0xc1a,
    SenderSearchKey = 0xc1d,
    SendInternetEncoding = 0x3a71,
    SendRecallReport = 0x6803,
    SendRichInfo = 0x3a40,
    Sensitivity = 0x36,
    SentmailEntryId = 0xe0a,
    SentRepresentingAddrtype = 100,
    SentRepresentingEmailAddress = 0x65,
    SentRepresentingEntryId = 0x41,
    SentRepresentingName = 0x42,
    SentRepresentingSearchKey = 0x3b,
    ServiceDeleteFiles = 0x3d10,
    ServiceDllName = 0x3d0a,
    ServiceEntryName = 0x3d0b,
    ServiceExtraUids = 0x3d0d,
    ServiceName = 0x3d09,
    Services = 0x3d0e,
    ServiceSupportFiles = 0x3d0f,
    ServiceUid = 0x3d0c,
    SevenBitDisplayName = 0x39ff,
    SmtpAddress = 0x39fe,
    SpoolerStatus = 0xe10,
    SpouseName = 0x3a48,
    StartDate = 0x60,
    StateOrProvince = 0x3a28,
    Status = 0x360b,
    StatusCode = 0x3e04,
    StatusString = 0x3e08,
    StoreEntryId = 0xffb,
    StoreProviders = 0x3d00,
    StoreRecordKey = 0xffa,
    StoreState = 0x340e,
    StoreSupportMask = 0x340d,
    StreetAddress = 0x3a29,
    Subfolders = 0x360a,
    Subject = 0x37,
    SubjectIpm = 0x38,
    SubjectPrefix = 0x3d,
    SubmitFlags = 0xe14,
    Supersedes = 0x103a,
    SupplementaryInfo = 0xc1b,
    Surname = 0x3a11,
    TelexNumber = 0x3a2c,
    Templateid = 0x3902,
    Title = 0x3a17,
    TnefCorrelationKey = 0x7f,
    TransmitableDisplayName = 0x3a20,
    TransportKey = 0xe16,
    TransportMessageHeaders = 0x7d,
    TransportProviders = 0x3d02,
    TransportStatus = 0xe11,
    TtytddPhoneNumber = 0x3a4b,
    TypeOfMtsUser = 0xc1c,
    UserCertificate = 0x3a22,
    UserX509Certificate = 0x3a70,
    ValidFolderMask = 0x35df,
    ViewsEntryId = 0x35e5,
    WeddingAnniversary = 0x3a41,
    X400ContentType = 60,
    X400DeferredDeliveryCancel = 0x3e09,
    Xpos = 0x3f05,
    Ypos = 0x3f06
 */
   
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
}
