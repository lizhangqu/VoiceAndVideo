<?php
include_once('function.php');
if(isset($_POST['username']) && isset($_POST['password'])){
	$account['username']=$_POST['username'] ;  	
	$account['password']=$_POST['password'];
	//这里处理自己服务器登录的流程
	//自己服务器登录成功后向客户端下发加密的rsa串
	$result=signature("1071","123","1400001973","1400001973",60*60*24*30,"./private_key");
	if($result==null){
		$res['status']=500;
		$res['message']="server error";
		echo json_encode($res);
	}else{
		$res['status']=200;
		$res['message']=$result;
		echo json_encode($res);
	}
	
}else{
	$res['status']=404;
	$res['message']="params is not right";
	echo json_encode($res);
}

?>
