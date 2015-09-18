<?php
include_once('Easemob.class.php');

$options['client_id']="YXA6bI0xUFa1EeWjzYFxAxSayQ";
$options['client_secret']="YXA6Rlyaq7MK9i5L0luXKC00EJowt74";
$options['org_name']="lizhangqu";
$options['app_name']="test";
$easemob=new Easemob($options);

if(isset($_POST['username']) && isset($_POST['password'])){
	$account['username']=$_POST['username'] ;  	
	$account['password']=$_POST['password'];
	//这里处理自己服务器注册的流程
	//自己服务器注册成功后向环信服务器注册
	$result=$easemob->accreditRegister($account);
	echo $result;
}else{
	$res['status']=404;
	$res['message']="params is not right";
	echo json_encode($res);
}

?>