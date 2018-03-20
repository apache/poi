<xs:schema
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    >
 <xs:simpleType name="SizeT">
            <xs:restriction base="xs:integer"/>
        </xs:simpleType>

    <xs:simpleType name="ColorT">
            <xs:restriction base="xs:string">
                <xs:pattern value="white|green"/>
            </xs:restriction>
        </xs:simpleType>


    <xs:element name="OldSizeElt" type="SizeT"/>
    <xs:element name="OldColorElt" type="ColorT"/>
</xs:schema>
