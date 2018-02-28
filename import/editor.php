<?php 
	require 'inc/dbconnect.php'; 
?>
<html>  
<head> </head>
<body style="background-color: grey">
	<h1 style="text-align: center">Geodaten-Editor</h1>
	<div style="display: flex; align-items: center; justify-content: center">
		<form action="">
		<label for "Zielobjekt">Zielobjekt: </label>
		<select name="subjects" id="subjects">
		<?php
			<option>Vorauswahl</option>
			$query1 = 'SELECT name FROM berlin.geoobject ORDER BY id ASC LIMIT 100';
			$result = pg_query($query1) or die('Abfrage fehlgeschlagen: ' . pg_last_error());
			require 'subjects.php';
			pg_free_result($result);
		?>
		</select>
		<br><br>
		<label for "Beziehung">Beziehung: </label>
		<?php include ("praedikate.html"); ?>      
		<br><br>
		<label for "Geodata2">Bezugsobjekt: </label>
		<input type="text" id="objekt" name="Geo2" placeholder="Link zum Bezug">
		<select name="objects" id="objects">
			<option>XOR Bezugsopjekt DB</option>
		<?php
			$query1 = 'SELECT name FROM berlin.geoobject ORDER BY id ASC LIMIT 100';
			$result = pg_query($query1) or die('Abfrage fehlgeschlagen: ' . pg_last_error());
			require 'subjects.php';
			pg_free_result($result);
		?>
		</select>
		<br><br>
		<?php
			if(($Geo1 = $_GET['subjects'] === null) && $error = error_get_last())
			{
				switch($error['type'])
				{
					case E_ERROR:
					break;
					case E_NOTICE:
					echo "F&uuml;llen Sie bitte die Felder aus!";
					break;
				}
			}
			else
			{
				$Geo1 = $_GET['subjects'];
				$Geo2 = $_GET['Geo2'];

				if(empty($Geo2))
				{ 
					$list = array(
						'Subjekt' => $Geo1, 
						'Praedikat' => $_GET['praedikat'], 
						'Objekt' => $_GET['objects']
						);
										
					
					$file = fopen('/opt/lampp/htdocs/akmo/results.json', 'a');
					fwrite($file, json_encode($list, JSON_PRETTY_PRINT));
					fclose($file);
				}
				else
				{
					$list = array(
						'Subjekt' => $Geo1, 
						'Praedikat' => $_GET['praedikat'], 
						'Objekt' => $Geo2
						);
										
					
					$file = fopen('/opt/lampp/htdocs/akmo/results.json', 'a');
					fwrite($file, json_encode($list, JSON_PRETTY_PRINT));
					fclose($file);
				}
			}
			pg_close($dbconn);
		?>
		<input type="submit">
	</div>
</body>
</html>