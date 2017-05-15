<!DOCTYPE html>
<html>
<head>
	<title>php request test</title>
</head>
<body>
<?php    include 'interactiongame.php'; 


  /*--------initial information------------------------*/
  $lccengineaddress="localhost:8888";
  $institutionname="game_institution";

  $gameprotocol_id="ultimategame";

  $firstagent_id="peter";
  $firstagent_role="proposer(10)";

  $secondagent_id="richard";
  $secondagent_role="responder(10)";

  $firstagent_response_1="e(offernum(2, richard), _)";
  $secondagent_response_1="e(acceptornot(reject, 2), _)";


  


  /*----------------0. check whether the server is ready-------------*/
  echo "0. Check initial state<br><br>";
  echo getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';

  /*--------------1. create an institution------------------*/
  echo "1. Create an institution<br><br>";
  CreateInstitution($lccengineaddress,$institutionname);
  
  /*----------1.1check if the new institution exists--------------- */
  echo "1.1 Check if institution exists:<br><br>";
  echo getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';

  /*-----------2. Create first agent--------------------*/
  echo "2. Create first Agent <br><br>";
  $firstagent_state=CreateFirstagent($lccengineaddress,$institutionname,$gameprotocol_id,$firstagent_id,$firstagent_role);
  $interactionid=GetInteractionId($firstagent_state,$lccengineaddress,$institutionname);
  
  /*---------2.2 check firstagent state---------------*/
  echo "2.2 Check if firstagent exists<br><br>'";
  $interactionpath="http://{$lccengineaddress}/interaction/user/manager/{$institutionname}/{$interactionid}";
  var_dump(getrequest($interactionpath));echo"<br><br>";

  /*----------3. add second agent----------------*/
  echo "3. Create second agent<br><br>";
  CreateOtherAgent($lccengineaddress,$institutionname,$interactionid,$secondagent_id,$secondagent_role);

  /*------------3.1 check if all agents are created ---------------*/
  echo "3.1 Check if agents all exist:<br><br>";
  var_dump(getrequest($interactionpath)); echo '<br><br>';
  sleep(1);

  /*------------4. ask for first agent's next step --------------------*/
  echo "4. Ask for first agent's next_step <br><br>";
  $firstagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$firstagent_id);
  var_dump($firstagent_nextstep_1);echo"<br><br>";

 /*---get the body of next_step
 
 $pattern="#(e|i)\(((\w+)\((\w+)\,\s(\w+)\))\,\s\_\)#";
  preg_match($pattern,$next_step_set[0],$matches);
  var_dump($matches);

  */

  /*---------5. answer firstagent---------------*/
  echo "5. Answer first agnt<br><br>";
  AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid,$firstagent_id,$firstagent_response_1);
  sleep(1);

  /*---------6. get second agnet's nextstep---------------------------------*/
  echo "6. Get second agnet's next step<br><br>";
  $secondagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$secondagent_id);
  var_dump($secondagent_nextstep_1);echo"<br><br>";

  /*--------7. answer second agent--------------------------------*/
  echo "7. Answer second agent<br><br>";
  AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid,$secondagent_id,$secondagent_response_1);
  sleep(1);

  /*--------8 check next step------------------------*/
  echo "8. Check next step<br><br>";
  $out_json=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$firstagent_id);
  sleep(1);
  $out_json2=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$secondagent_id);
  sleep(1);
  var_dump($out_json);echo"<br><br>";
  var_dump($out_json2);echo"<br><br>";
  

?>


</body>
</html>