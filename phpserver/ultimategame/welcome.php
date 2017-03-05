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
      $defaultstate_json=getrequest("http://{$localhost_path}/institutions");echo '<br><br>';
      //sleep(1);
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$localhost_path}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="default"){//server is ready
        //echo "1. Create an institution<br><br>";
        $institutionstate_json=CreateInstitution($localhost_path,$institutionname);
        $institutionstate=json_decode($institutionstate_json,true);
        $subject=$institutionstate["path"];
        $pattern="/http:\/\/{$localhost_path}\/institution\/user\/manager\/(\w+)/";
        preg_match($pattern,$subject,$matches);
        if ($matches[1]==$institutionname){//institution is created successfully
          header("Location:http://localhost/phpserver/ultimategame/proposerside/proposerform.php");
        }
        else{
          echo "Failed to create the game institution *_*<br><br>";
        }
      }
      else{
        echo "Failed to start teh game server *_*<br><br>";
      }    
    }
    elseif (isset($_POST["role"])&&$_POST["role"]=="Responder"){/*----------responder side--------------------*/
      //echo "0. Check initial state<br><br>";
      $defaultstate_json=getrequest("http://{$localhost_path}/institutions");echo '<br><br>';
      sleep(1);
      $defaultstate=json_decode($defaultstate_json,true);
      $subject=$defaultstate["0"]["path"];
      $pattern="/http:\/\/{$localhost_path}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]=="default"){//server is ready
        //Create an institution
        $institutionstate_json=CreateInstitution($localhost_path,$institutionname);
        $institutionstate=json_decode($institutionstate_json,true);
        $subject=$institutionstate["path"];
        $pattern="/http:\/\/{$localhost_path}\/institution\/user\/manager\/(\w+)/";
        preg_match($pattern,$subject,$matches);
        if ($matches[1]==$institutionname){//instution has been created successfully
          $firstagent_state=CreateFirstagent($localhost_path,$institutionname,$game_protocolid,$firstagent_id,$firstagent_role);//create first agent 
          $interactionid_responderside=GetInteractionId($firstagent_state,$localhost_path,$institutionname);

          if ($interactionid_responderside!=""){//firstagent has been created successfully
            $interactionpath="http://{$localhost_path}/interaction/user/manager/{$institutionname}/{$interactionid_responderside}";
            CreateOtherAgent($localhost_path,$institutionname,$interactionid_responderside,$secondagent_id,$secondagent_role);//create second agent
            sleep(1);
            $allagentsstates_json=getrequest($interactionpath);
            $allagentsstates=json_decode($allagentsstates_json,true);
            if (count($allagentsstates["agents"])==2){//all two agents have been created successfully
              //$firstagent_nextstep_1=AskAgentNextStep($localhost_path,$institutionname,$interactionid_responderside,$firstagent_id);
              $firstagent_response_1="e(offernum({$proposeroffer}, richard), _)";  
              AnswerAgentNextStep($localhost_path,$institutionname,$interactionid_responderside,$firstagent_id,$firstagent_response_1);
              sleep(1);

              $keydata=array("interid"=>$interactionid_responderside);
              $keydata_json=json_encode($keydata);
              $fp=fopen('/Applications/XAMPP/htdocs/phpserver/ultimategame/responderside/responderinfo.json','w' );
              fwrite($fp, $keydata_json);
              fclose($fp);

              header("Location:http://localhost/phpserver/ultimategame/responderside/responderform.php");
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
          echo "Failed to create the game institution *_*<br><br>";
        }
      }
      else{
        echo "Failed to start the  game server *_*<br><br>";
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
