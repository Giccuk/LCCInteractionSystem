<!DOCTYPE html>
<html>
<head>
	<title>Demo of interaction system</title>
</head>
<body>
<h2>Welcome to join TrustGame!</h2>
<form action="welcome.php" method="post">
    First, plesase choose a role to play:<br><br>
    <input type="radio" name="role" value="Investor">Investor
    <input type="radio" name="role" value="Trustee">Trustee<br><br>
    Then, Click the button to start the game:<br><br>
    <input type="submit" name="press" value="Start Game"><br><br>
</form>

<?php

  include 'welcomeinfo.php';

  if ($_SERVER["REQUEST_METHOD"] == "POST"){
    if (isset($_POST["role"])&&$_POST["role"]=="Investor"){/*-------------------------------Investor side---------------------------------*/
      //echo "0. Check initial state<br><br>";
      $defaultstate_json=getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';
      //sleep(1)
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="{$defaultinst}"){//server is ready
          header("Location:http://{$gameserveraddress}/trustgame/investorside/investorform.php");
      }
      else{
        echo "Failed to start the game server *_*<br><br>";
      }    
    }
    elseif (isset($_POST["role"])&&$_POST["role"]=="Trustee"){/*-------------------------------Trustee side---------------------------------*/
      //echo "0. Check initial state<br><br>";
      $defaultstate_json=getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';
      sleep(1);
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="{$defaultinst}"){//server is ready
          $firstagent_state=CreateFirstagent($lccengineaddress,$institutionname,$gameprotocol_id,$firstagent_id,$firstagent_role);//create first agent 
          $interactionid_Trusteeside=GetInteractionId($firstagent_state,$lccengineaddress,$institutionname);

          if ($interactionid_Trusteeside!=""){//firstagent has been created successfully
            $interactionpath="http://{$lccengineaddress}/interaction/user/manager/{$institutionname}/{$interactionid_Trusteeside}";
            CreateOtherAgent($lccengineaddress,$institutionname,$interactionid_Trusteeside,$secondagent_id,$secondagent_role);//create second agent
            sleep(1);
            $allagentsstates_json=getrequest($interactionpath);
            $allagentsstates=json_decode($allagentsstates_json,true);
            if (count($allagentsstates["agents"])==2){//all two agents have been created successfully

              //$firstagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid_Trusteeside,$firstagent_id);
              $firstagent_response_1="e(invest({$investoroffer}, {$secondagent_id}), _)";  
              AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_Trusteeside,$firstagent_id,$firstagent_response_1);
              sleep(1);  

              msgstorecsv("{$gameprotocol_id}","{$firstagent_id}","{$firstagent_role}","{$secondagent_id}","{$secondagent_role}","e(invest({$investoroffer}#{$secondagent_id}))");

              //pass interactionid to trusteeside
              $keydata=array("interid"=>$interactionid_Trusteeside);
              $keydata_json=json_encode($keydata);
              $fp=fopen("{$sourcefiledir}/trustgame/trusteeside/trusteeinfo.json",'w');
              fwrite($fp, $keydata_json);
              fclose($fp);
              //go to next step to get trustee repay
              header("Location:http://{$gameserveraddress}/trustgame/Trusteeside/Trusteeform.php");
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

<img src="smile2.png">

</body>
</html>
