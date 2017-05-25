<!DOCTYPE html>
<html>
<head>
	<title>Get trustee's repay</title>
</head>
<body>
<?php 
  include 'trusteeinfo.php';
  $fp=fopen('trusteeinfo.json','r');
  $interiddata_json=fread($fp,filesize('trusteeinfo.json'));
  fclose($fp);
  $interiddata=json_decode($interiddata_json,true);
  $investorchoice=$interiddata["investorchoice"];
  $trusteegetNum=$game_rate*$investorchoice;
?>

<p>You are now the trustee. The investor has decided to offer <?php  sleep(1); echo $investorchoice;?> to you. So you have <?php echo $trusteegetNum; ?>. How much will you repay?</p>
<span style="color: #FF0000;">*Please enter the number smaller than <?php echo $trusteegetNum;?> !</span><br><br>

<form action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]);?>" method="post">
  <input type="text", name="trusteechoice">
  <input type="submit" name="submit" value="send the rapay">
</form>

<?php

  $repay="";

  if ($_SERVER["REQUEST_METHOD"] == "POST") {
    if (empty($_POST["trusteechoice"])) {
      echo "<br>";
      echo '<span style="color:#FF0000;"> Nothing is entered. Please enter your repay and send again. </span>';
    } 
    else {
      $repay = test_input($_POST["trusteechoice"]);
      // check if name only contains letters and whitespace
      if (preg_match("/[^\d+]/",$repay)) {
        echo "<br>";
        echo '<span style="color:#FF0000;">Only number are allowed. Please enter your rapay and send again.</span>';
      }
      else{
         $trusteechoiceNUM=$repay*1;
         if ($trusteechoiceNUM<=$trusteegetNum) {
            $fp=fopen('trusteeinfo.json','r');
            $interiddata_json=fread($fp,filesize('trusteeinfo.json'));
            fclose($fp);
            $fp2=fopen('trusteeinfo.json', 'w');
            $interiddata=json_decode($interiddata_json,true);
            $interid=$interiddata["interid"];
            $newdata=array("interid"=>$interid,"trusteerepay"=>$repay);
            $newdata_json=json_encode($newdata);
            fwrite($fp2, $newdata_json);
            fclose($fp2);

            header("Location:http://{$gameserveraddress}/trustgame/trusteeside/investorreply.php");
         }
         else{
            echo "<br>";
            echo '<span style="color:#FF0000;">'."Your repay is bigger than {$trusteegetNum}".'</span>'.'<span style="color:#FF0000;">. Please enter your rapay and send again.</span>';
         }
      }
    }
  }

  function test_input($data) {
    $data = trim($data);
    $data = stripslashes($data);
    $data = htmlspecialchars($data);
    return $data;
  }


?>

<br>
<img src="smile2.png">


</body>
</html>
