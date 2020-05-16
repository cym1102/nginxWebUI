$(function() {

	layer.open({
		type : 1,
		shade : false,
		title : "nginx配置器",
		closeBtn : false,
		area : [ '400px', '330px' ], //宽高
		content : $('#windowDiv')
	});

})

function login() {
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/login/login',
		data : $("#loginForm").serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.href = ctx + "adminPage/http";
			} else {
				alert("登录失败,请检查输入");
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