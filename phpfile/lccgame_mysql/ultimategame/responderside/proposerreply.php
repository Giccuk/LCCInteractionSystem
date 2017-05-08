<!DOCTYPE html>
<html>
<head>
	<title>proposer reply</title>
</head>
<body>

<?php 
	include 'responderinfo.php';
	
	if ($_SERVER["REQUEST_METHOD"] == "POST"){
		if (isset($_POST["responderchoice"])&&!empty($_POST["responderchoice"])){
			$fp=fopen('responderinfo.json','r');
  			$interiddata_json=fread($fp,filesize('responderinfo.json'));
  			fclose($fp);
			$interiddata=json_decode($interiddata_json,true);
  			$interactionid_responderside=$interiddata["interid"];
			//$interactionid_responderside=$_GET["finalid"];
			$secondagent_response_1="e(acceptornot({$_POST["responderchoice"]}, {$proposeroffer}), _)";
			AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_responderside,$secondagent_id,$secondagent_response_1);
			
	        sleep(1);

	        msgstorecsv("{$gameprotocol_id}","{$secondagent_id}","{$secondagent_role}","{$firstagent_id}","{$firstagent_role}","e(acceptornot({$_POST["responderchoice"]}#{$proposeroffer}))");
	            
	        echo "You just ".$_POST["responderchoice"]." the proposer's offer."; echo"<br><br>";

	 	}
	}

?>

<img src="bot.png">

</body>
</html>