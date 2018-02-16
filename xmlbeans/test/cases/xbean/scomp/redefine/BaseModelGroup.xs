<xs:schema
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns="http://xbean/scomp/redefine/GroupRedefine"
    targetNamespace="http://xbean/scomp/redefine/GroupRedefine"
    xmlns:color="http://xbean/scomp/redefine/SimpleRedefined"
      >
 <xs:complexType name="GroupT">
     <xs:sequence>
         <xs:element name="child1" type="color:ColorT" minOccurs="0"/>
         <xs:element name="child2" type="xs:string"/>
         <xs:element name="child3" type="xs:int" minOccurs="0"/>
     </xs:sequence>
 </xs:complexType>


</xs:schema>
