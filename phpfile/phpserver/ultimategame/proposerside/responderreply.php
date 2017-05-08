<!DOCTYPE html>
<html>
<head>
  <title>reply from responder in Ultimate Game</title>
</head>
<body>

<?php 

 include 'proposerinfo.php'; 
  /*------initial information--------------------------*/
  if ($_SERVER["REQUEST_METHOD"] == "POST"){

    if (isset($_POST["proposeroffer"])&&!empty($_POST["proposeroffer"])){

        /*-----------2. Create first agent--------------------*/
        $firstagent_state=CreateFirstagent($localhost_path,$institutionname,$game_protocolid,$firstagent_id,$firstagent_role);
        $interactionid_proposerside=GetInteractionId($firstagent_state,$localhost_path,$institutionname); 

        if ($interactionid_proposerside!=""){
          /*---------2.2 check firstagent state---------------*/
          $interactionpath="http://{$localhost_path}/interaction/user/manager/{$institutionname}/{$interactionid_proposerside}";
          /*----------3. create second agent----------------*/
          CreateOtherAgent($localhost_path,$institutionname,$interactionid_proposerside,$secondagent_id,$secondagent_role);
          sleep(1);
          /*------------3.1 check if all agents are created ---------------*/
          $allagentsstates_json=getrequest($interactionpath);
          $allagentsstates=json_decode($allagentsstates_json,true);

          if (count($allagentsstates["agents"])==2){

            //$firstagent_nextstep_1=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$firstagent_id);

            $firstagent_response_1="e(offernum({$_POST["proposeroffer"]}, richard), _)";  
            AnswerAgentNextStep($localhost_path,$institutionname,$interactionid_proposerside,$firstagent_id,$firstagent_response_1);
            sleep(1);

            //$secondagent_nextstep_1=AskAgentNextStep($localhost_path,$institutionname,$interactionid,$secondagent_id);

            $secondagent_response_1="e(acceptornot({$responderchoice}, {$_POST["proposeroffer"]}), _)";
            AnswerAgentNextStep($localhost_path,$institutionname,$interactionid_proposerside,$secondagent_id,$secondagent_response_1);
            sleep(1);
            
            //var_dump($secondagent_response_1); echo"<br><br>";
            echo "The responder has decide to {$responderchoice} your offer.<br><br>";
          }
          else{
            echo "Failed to create the second agent. *_*<br><br>";
          }
        }
        else{
          echo "Failed to create new interaction. *_* <br><br>";
        }
    }
  }
?>

<form action="proposercomments.php" metho="post">
  What do you want to say about the offer?<br><br>
  <input type="text" name="proposercomments"><br><br>
  <input type="submit" value="That's all">
</form>

<img src="bot.png">

</body>
</html>
