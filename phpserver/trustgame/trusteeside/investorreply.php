<!DOCTYPE html>
<html>
<head>
	<title>investor reply</title>
</head>
<body>

<?php 

	include 'trusteeinfo.php';
	$fp=fopen('trusteeinfo.json','r');
	clearstatcache();
    $gamedata_json=fread($fp,filesize('trusteeinfo.json'));
    fclose($fp);
    $gamedata=json_decode($gamedata_json,true);
	$finalrepay=$gamedata["trusteerepay"];
	$interactionid_trusteeside=$gamedata["interid"];

	$secondagent_response_1="e(repay({$finalrepay}, {$firstagent_id}), _)";
	AnswerAgentNextStep($localhost_path,$institutionname,$interactionid_trusteeside,$secondagent_id,$secondagent_response_1);
	sleep(1);
	//$trusteeown=$investoroffer*$game_rate-$finalrepay; 
	//echo $trusteeown."<br>";  

	echo "You just repay {$finalrepay} to the investor."; echo"<br><br>";


?>

<img src="smile2.png">

</body>
</html>