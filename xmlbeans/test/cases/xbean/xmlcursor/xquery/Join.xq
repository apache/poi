for $a in .//employee
return <result>
		{ $a/ssn },
		{ $a/name },
		{
		for $b in .//employee 
		where $b/ssn=$a/ssn and $a/name !=$b/name
		return $b/name
		}
	</result>
