<?php
	while ($line = pg_fetch_array($result, null, PGSQL_ASSOC)) 
	{
		foreach ($line as $col_value) 
	 	{
			echo "<option>$col_value</option>";
    	}
	}
?>