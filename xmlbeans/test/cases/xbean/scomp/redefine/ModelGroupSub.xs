<xs:schema
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://xbean/scomp/redefine/GroupRedefine"
    targetNamespace="http://xbean/scomp/redefine/GroupRedefine"
    >
    <xs:redefine schemaLocation="BaseModelGroup.xs">
        <xs:complexType name="GroupT">
         <xs:complexContent>
             <xs:restriction base="GroupT">
                <xs:sequence>
                   <xs:element name="child2" type="xs:string"/>
            </xs:sequence>
             </xs:restriction>
         </xs:complexContent>
        </xs:complexType>
    </xs:redefine>

    <xs:element name="GroupSubElt" type="GroupT"/>

</xs:schema>