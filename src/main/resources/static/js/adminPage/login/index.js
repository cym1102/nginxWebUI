$(function() {

	if($("#adminCount").val() > 0){
		layer.open({
			type : 1,
			shade : false,
			title : "登陆nginxWebUI",
			closeBtn : false,
			area : [ '400px', '330px' ], //宽高
			content : $('#windowDiv')
		});
	}else{
		layer.open({
			type : 1,
			shade : false,
			title : "初始化管理员",
			closeBtn : false,
			area : [ '400px', '330px' ], //宽高
			content : $('#addUserDiv')
		});
	}
	

})

function login() {
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/login/login',
		data : $("#loginForm").serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.href = ctx + "adminPage/monitor";
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
	
}


function addAdmin(){
	if($("#adminName").val()==''){
		layer.msg("用户名未填写");
		return;
	}
	if($("#adminPass").val() == '' || $("#repeatPass").val() == ''){
		layer.msg("密码未填写");
		return;
	}
	if($("#adminPass").val() != $("#repeatPass").val()){
		layer.msg("密码不一致");
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/login/addAdmin',
		data : $("#adminForm").serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function getKey() {
	if (event.keyCode == 13) {
		login();
	}
}


function refreshCode(id) {
	$("#" + id).attr("src", ctx + "adminPage/login/getCode?t=" + (new Date()).getTime());
}

