<!DOCTYPE html>
<html>
<head>
  <title>reply from trustee in Trust Game</title>
</head>
<body>

<?php 

 include 'investorinfo.php'; 
  /*------initial information--------------------------*/
  if ($_SERVER["REQUEST_METHOD"] == "POST"){

    if (isset($_POST["investoroffer"])&&!empty($_POST["investoroffer"])){

        /*-----------2. Create first agent--------------------*/
        $firstagent_state=CreateFirstagent($lccengineaddress,$institutionname,$gameprotocol_id,$firstagent_id,$firstagent_role);
        $interactionid_investorside=GetInteractionId($firstagent_state,$lccengineaddress,$institutionname); 

        if ($interactionid_investorside!=""){
          /*---------2.2 check firstagent state---------------*/
          $interactionpath="http://{$lccengineaddress}/interaction/user/manager/{$institutionname}/{$interactionid_investorside}";
          /*----------3. create second agent----------------*/
          CreateOtherAgent($lccengineaddress,$institutionname,$interactionid_investorside,$secondagent_id,$secondagent_role);
          sleep(1);
          /*------------3.1 check if all agents are created ---------------*/
          $allagentsstates_json=getrequest($interactionpath);
          $allagentsstates=json_decode($allagentsstates_json,true);

          if (count($allagentsstates["agents"])==2){

            //$firstagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid,$firstagent_id);

            $firstagent_response_1="e(invest({$_POST["investoroffer"]}, {$secondagent_id}), _)";  
            AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_investorside,$firstagent_id,$firstagent_response_1);
            sleep(1);

            //$secondagent_nextstep_1=AskAgentNextStep($lccengineaddress,$institutionname,$interactionid_investorside,$secondagent_id);

            $secondagent_response_1="e(repay({$trusteerepay}, {$firstagent_id}), _)";
            AnswerAgentNextStep($lccengineaddress,$institutionname,$interactionid_investorside,$secondagent_id,$secondagent_response_1);
            sleep(1);
            
            //var_dump($secondagent_response_1); echo"<br><br>";
            //$investorown=$game_total-$_POST["investoroffer"]+$trusteerepay;
            //echo "$investorown";
            echo "The trustee has decide to repay {$trusteerepay} to you.<br><br>";
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

<form action="investorcomments.php" metho="post">
  What do you want to say about the offer?<br><br>
  <input type="text" name="investorcomments"><br><br>
  <input type="submit" value="That's all">
</form>

<img src="smile2.png">

</body>
</html>
