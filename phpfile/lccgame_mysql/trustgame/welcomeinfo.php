<?php
   //before 0.
    $sourcefiledir="/Applications/XAMPP/htdocs/lccgame_mysql";
    $gameserveraddress="localhost/lccgame_mysql";
    $lccengineaddress="localhost:8888";
    $institutionname="game_institution";
    $gameprotocol_id="trustgame_simple";
    $defaultinst="game_institution";

    $game_total=10;
    $game_rate=3;

    $firstagent_id="ellen";
    $firstagent_role="investor({$game_total},{$game_rate})";

    $secondagent_id="ted";
    $secondagent_role="trustee({$game_rate})";

    $investoroffer="4";
    $trusteerepay="3";
  //0.1
  function msgstorecsv($protocolid,$msgsenderid,$msgsenderrole,$msgreceiverid,$msgreceiverrole,$msgbody){
      //$csv_header=array('msgsenderid','msgsenderrole','msgreceiverid','msgreceiverrole','msgbody');
      $inputdata=array($protocolid,$msgsenderid,$msgsenderrole,$msgreceiverid,$msgreceiverrole,$msgbody);
      for ($x=0;$x<sizeof($inputdata);$x++){
        $inputdata[$x]=str_replace(',', '#', $inputdata[$x]);
      }
      $data=array("{$inputdata[0]},{$inputdata[1]},{$inputdata[2]},{$inputdata[3]},{$inputdata[4]},{$inputdata[5]}");
      $fp=fopen('/Users/cancui/msgdata.csv','a');
      foreach ($data as $row) {
          fputcsv($fp, explode(',',$row));
      }
      fclose($fp);
    }

  //0.
  function getrequest($getpath){
    $curlrequest = curl_init();
    curl_setopt($curlrequest, CURLOPT_URL, $getpath);
    curl_setopt($curlrequest, CURLOPT_HEADER,0); 
    curl_setopt($curlrequest, CURLOPT_RETURNTRANSFER,true);
    $out_json=curl_exec($curlrequest);
    curl_close($curlrequest);

    return $out_json;
  }

  function postrequest($postpath,$postdata){
    $curlrequest=curl_init();
    curl_setopt($curlrequest, CURLOPT_URL,$postpath);
    curl_setopt($curlrequest, CURLOPT_POSTFIELDS, $postdata); 
    curl_setopt($curlrequest, CURLOPT_RETURNTRANSFER,true);
    $reply_json=curl_exec($curlrequest);
    curl_close($curlrequest);

    return $reply_json;
  }

  //1.
  function CreateInstitution($serverpath,$institutionname){
    $create_institution_path="http://{$serverpath}/create_institution";
    $institutionname_json=json_encode(array("name"=>$institutionname));
    $institutionstate=postrequest($create_institution_path,$institutionname_json);

    return $institutionstate;

  }

  //2.
  function CreateFirstagent($serverpath,$institutionname,$game_protocolid,$firstagent_id,$firstagent_role){
    $agent=array(
        "template"=>array(
        "protocol_id"=>$game_protocolid,
        "agents"=>array(
          array(
            "agent_id"=>$firstagent_id,
            "roles"=>array(array("role"=>$firstagent_role))
            )
          )
        ),
        "data"=>array()
      );
    $create_firstagent_path="http://{$serverpath}/institution/create/user/manager/{$institutionname}";
    $firstagent_json=json_encode($agent);
    $reply_json=postrequest($create_firstagent_path,$firstagent_json);

    return $reply_json;
  }

  function GetInteractionId($firstagent_state,$serverpath,$institutionname){
    $reply=json_decode($firstagent_state,true);
    $subject=$reply["path"];
    $pattern="/http:\/\/{$serverpath}\/interaction\/user\/manager\/{$institutionname}\/(\w+)/";
    preg_match($pattern,$subject,$matches);
    $interactionid=$matches[1];

    return $interactionid;
  }

//3.
  function CreateOtherAgent($serverpath,$institutionname,$interactionid,$otheragent_id,$otheragent_role){
    $agent=array(
        "template"=>array(
          "agent_id"=>$otheragent_id,
          "roles"=>array(array("role"=>$otheragent_role))
          ),
        "data"=>array()
    );
    $create_otheragent_path="http://{$serverpath}/interaction/create/user/manager/{$institutionname}/{$interactionid}";
    $secondagent_json=json_encode($agent);
    postrequest($create_otheragent_path,$secondagent_json);
  }

//4.
  function AskAgentNextStep($serverpath,$institutionname,$interactionid,$agentid){
    $agent_path="http://{$serverpath}/agent/user/manager/{$institutionname}/{$interactionid}/{$agentid}";
    $out=json_decode(getrequest($agent_path),true);
    $nextsteps=$out["next_steps"];

    return $nextsteps;
  }

//5.
  function AnswerAgentNextStep($serverpath,$institutionname,$interactionid,$agentid,$response){
      $answer_path="http://{$serverpath}/agent/elicited/user/manager/{$institutionname}/{$interactionid}/{$agentid}";
      $answer_data=array("elicited"=>$response);
      postrequest($answer_path,json_encode($answer_data));
  }

  /*----------------0. check whether the server is ready-------------
  echo "0. Check initial state<br><br>";
  echo getrequest("http://{$localhost_path}/institutions");echo '<br><br>';

  /*--------------1. create an institution------------------
  echo "1. Create an institution<br><br>";
  CreateInstitution($localhost_path,$institutionname);
  
  /*----------1.1check if the new institution exists
  echo "1.1 Check if institution exists:<br><br>";
  echo getrequest("http://{$localhost_path}/institutions");echo '<br><br>'; 

  /*-----------2. Create first agent--------------------
  echo "2. Create first Agent <br><br>";
  $interactionid=CreateFirstagent($localhost_path,$institutionname,$game_protocolid,$firstagent_id,$firstagent_role);

  /*---------2.2 check firstagent state---------------
  echo "2.2 Check if firstagent exists<br><br>'";
  $interactionpath="http://{$localhost_path}/interaction/user/manager/{$institutionname}/{$interactionid}";
  var_dump(getrequest($interactionpath));echo"<br><br>";

  /*----------3. add second agent----------------
  echo "3. Create second agent<br><br>";
  CreateOtherAgent($localhost_path,$institutionname,$interactionid,$secondagent_id,$secondagent_role);

  /*------------3.1 check if all agents are created ---------------
  echo "3.1 Check if agents all exist:<br><br>";
  var_dump(getrequest($interactionpath)); echo '<br><br>';
  sleep(1);

  /*------------4. ask for first agent's next step --------------------
  echo "4. Ask for first agent's next_step <br><br>";
  $firstagent_nextstep_1=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$firstagent_id);
  var_dump($firstagent_nextstep_1);echo"<br><br>";

 /*---get the body of next_step
 
 $pattern="#(e|i)\(((\w+)\((\w+)\,\s(\w+)\))\,\s\_\)#";
  preg_match($pattern,$next_step_set[0],$matches);
  var_dump($matches);

  */

  /*---------5. answer firstagent---------------
  echo "5. Answer first agnt<br><br>";
  AnswerAgentNextStep($localhost_path,$institutionname,$interactionid,$firstagent_id,$firstagent_response_1);
  sleep(1);

  /*---------6. get second agnet's nextstep---------------------------------
  echo "6. Get second agnet's next step<br><br>";
  $secondagent_nextstep_1=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$secondagent_id);
  var_dump($secondagent_nextstep_1);echo"<br><br>";

  /*--------7. answer second agent--------------------------------
  echo "7. Answer second agent<br><br>";
  AnswerAgentNextStep($localhost_path,$institutionname,$interactionid,$secondagent_id,$secondagent_response_1);
  sleep(1);

  /*--------8 check next step------------------------
  echo "8. Check next step<br><br>";
  $out_json=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$firstagent_id);
  sleep(1);
  $out_json2=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$secondagent_id);
  sleep(1);
  var_dump($out_json);echo"<br><br>";
  var_dump($out_json2);echo"<br><br>";

  */

?>
