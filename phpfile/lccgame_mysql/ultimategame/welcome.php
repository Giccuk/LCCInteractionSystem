<!DOCTYPE html>
<html>
<head>
	<title>Demo of interaction system</title>
</head>
<body>
<h2>Welcome to join UltimateGame!</h2>
<form action="welcome.php" method="post">
    First, plesase choose a role to play:<br><br>
    <input type="radio" name="role" value="Proposer" >Proposer
    <input type="radio" name="role" value="Responder">Responder<br><br>
    Then, Click the button to start the game:<br><br>
    <input type="submit" name="press" value="Start Game"><br><br>
</form>

<?php

  include 'welcomeinfo.php';

  if ($_SERVER["REQUEST_METHOD"] == "POST"){
    if (isset($_POST["role"])&&$_POST["role"]=="Proposer"){/*-----------proposer side---------------------------------*/
      //echo "0. Check initial state<br><br>";
      $defaultstate_json=getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';
      //sleep(1);
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="{$defaultinst}"){//server is ready
          header("Location:http://{$gameserveraddress}/ultimategame/proposerside/proposerform.php");
      }
      else{
        echo "Failed to start the game server *_*<br><br>";
      }    
    }
    elseif (isset($_POST["role"])&&$_POST["role"]=="Responder"){/*----------responder side--------------------*/
      //echo "0. Check initial state<br><br>";
      $defaultstate_json=getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';
      sleep(1);
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="{$defaultinst}"){//server is ready
          $firstagent_state=CreateFirstagent($lccengineaddress,$institutionname,$gameprotocol_id,$firstagent_id,$firstagent_role);//create first agent 
          $interactionid_responderside=GetInteractionId($firstagent_state,$lccengineaddress,$institutionname);
          if ($interactionid_responderside!=""){//firstagent has been created successfully
            $interactionpath="http://{$lccengineaddress}/interaction/user/manager/{$institutionname}/{$interactionid_responderside}";
            CreateOtherAgent($lccengineaddress,$institutionname,$interactionid_responderside,$secondagent_id,$secondagent_role);//create second agent
            sleep(1);
            $allagentsstates_json=getrequest($interactionpath);
            $allagentsstates=json_decode($allagentsstates_json,true);
            if (count($allagentsstates["agents"])==2){//all two agents have been created successfully
              //$firstagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid_responderside,$firstagent_id);
              $firstagent_response_1="e(offernum({$proposeroffer}, richard), _)";  
              AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_responderside,$firstagent_id,$firstagent_response_1);
              sleep(1);

              msgstorecsv("{$gameprotocol_id}","{$firstagent_id}","{$firstagent_role}","{$secondagent_id}","{$secondagent_role}","e(offernum({$proposeroffer}#{$secondagent_id}))");

              $keydata=array("interid"=>$interactionid_responderside);

              $keydata_json=json_encode($keydata);
              $fp=fopen("{$sourcefiledir}/ultimategame/responderside/responderinfo.json",'w' );
              fwrite($fp, $keydata_json);
              fclose($fp);

              header("Location:http://{$gameserveraddress}/ultimategame/responderside/responderform.php");
            }
            else{
              echo "Failed to create the second agent. *_*<br><br>";
            }
          }
          else{
            echo "Failed to create new interaction. *_* <br><br>";
          } 
        }
      else{
        echo "Failed to start the game server *_*<br><br>";
      }    
    }
    else {
      echo "User did not choose a role.<br><br>";
      exit;//header("Location:http://localhost/weblcc/index.php");    
    }
  }
?>

<img src="bot.png">

</body>
</html>
