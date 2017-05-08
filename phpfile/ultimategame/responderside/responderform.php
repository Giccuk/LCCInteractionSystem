<!DOCTYPE html>
<html>
<head>
	<title>Get proposer's offer</title>
</head>
<body>
<?php 
  include 'responderinfo.php';
  //$midid=$_GET["interid"];
  //$nextpath="http://localhost/phpserver/ultimategame/responderside/proposerreply.php?finalid={$midid}";
?>

<form action="proposerreply.php" method="post">
	
  Okay, you choose to be responder.<br><br> 
	Now the proposer has decided to offer <?php  sleep(1); echo $proposeroffer;?>. <br>
	What's your choice?<br><br> 

  <input type="radio" name="responderchoice" value="Accept" >Accept the offer
  <input type="radio" name="responderchoice" value="Reject" >Reject the offer<br><br>
  <input type="submit" name="press" value="I have decided ! "><br><br>	

</form>

<img src="bot.png">


</body>
</html>
