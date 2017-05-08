<!DOCTYPE html>
<html>
<head>
	<title>proposer comment about the offer</title>
</head>
<body>
<?php
	if ($_SERVER["REQUEST_METHOD"] == "POST"){
	    if (isset($_POST["proposercomments"])&&!empty($_POST["proposercomments"])){
	    	$ddd=$_POST["proposercomments"];
	    	echo $ddd."<br>";
	    }
	}
?>
	  
</body>
</html>