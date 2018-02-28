<?php
	$dbconn = pg_connect("host=ohm.f4.htw-berlin.de 
						  dbname=ohdm_public 
						  user= 
						  password=")
						  or die('Verbindungsaufbau fehlgeschlagen: ' . pg_last_error());
 ?>