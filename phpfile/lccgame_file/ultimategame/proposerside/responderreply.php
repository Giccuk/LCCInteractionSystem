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
        
        $firstagent_state=CreateFirstagent($lccengineaddress,$institutionname,$gameprotocol_id,$firstagent_id,$firstagent_role);
        $interactionid_proposerside=GetInteractionId($firstagent_state,$lccengineaddress,$institutionname); 

        if ($interactionid_proposerside!=""){
          /*---------2.2 check firstagent state---------------*/
          $interactionpath="http://{$lccengineaddress}/interaction/user/manager/{$institutionname}/{$interactionid_proposerside}";
          /*----------3. create second agent----------------*/
          CreateOtherAgent($lccengineaddress,$institutionname,$interactionid_proposerside,$secondagent_id,$secondagent_role);
          sleep(1);
          /*------------3.1 check if all agents are created ---------------*/
          $allagentsstates_json=getrequest($interactionpath);
          $allagentsstates=json_decode($allagentsstates_json,true);

          if (count($allagentsstates["agents"])==2){

            //$firstagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$firstagent_id);

            $firstagent_response_1="e(offernum({$_POST["proposeroffer"]}, {$secondagent_id}), _)";  
            AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_proposerside,$firstagent_id,$firstagent_response_1);
            sleep(1);

            //store data
            
            msgstorecsv("{$gameprotocol_id}","{$firstagent_id}","{$firstagent_role}","{$secondagent_id}","{$secondagent_role}","e(offernum({$_POST["proposeroffer"]}#{$secondagent_id}))");

            //$secondagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$secondagent_id);

            $secondagent_response_1="e(acceptornot({$responderchoice}, {$_POST["proposeroffer"]}), _)";
            AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_proposerside,$secondagent_id,$secondagent_response_1);
            sleep(1);

            //store data
            msgstorecsv("{$gameprotocol_id}","{$secondagent_id}","{$secondagent_role}","{$firstagent_id}","{$firstagent_role}","e(acceptornot({$responderchoice}#{$_POST["proposeroffer"]}))");
            
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
