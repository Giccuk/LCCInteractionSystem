<!DOCTYPE html>
<html>
<head>
  <title></title>
</head>
<body>


<?php

    include 'gamecommands.php';
    echo "0. Check initial state<br><br>";
    $defaultstate_json=getrequest("http://{$lccengineaddress}/institutions");echo '<br><br>';
    $defaultstate=json_decode($defaultstate_json,true);
    $subject=$defaultstate["0"]["path"];
    $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
    preg_match($pattern,$subject,$matches);
    if ($matches[1]=="default"){
      echo "1. Create an institution<br><br>";
      $institutionstate_json=CreateInstitution($lccengineaddress,$institutionname);
      $institutionstate=json_decode($institutionstate_json,true);
      $subject=$institutionstate["path"];
      $pattern="/http:\/\/{$lccengineaddress}\/institution\/user\/manager\/(\w+)/";
      preg_match($pattern,$subject,$matches);
      if ($matches[1]==$institutionname){
        echo "New institution exists<br><br>";
      }
      else{
        echo "reload the page";
      }
    }
    else{
      echo "the server does not exist";
    }
?>

</body>
</html>
