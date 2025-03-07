$(function() {
	if ($("#adminCount").val() > 0) {
		var autoKey = window.localStorage.getItem("autoKey");
		var time = window.localStorage.getItem("time");
		
		if(autoKey != null && autoKey != '' && new Date().getTime() - time < 7 * 24 * 60 * 60 * 1000){
			// 自动登录
			showLoad();
			$.ajax({
				type: 'POST',
				url: ctx + '/adminPage/login/autoLogin',
				data: {
					autoKey : autoKey
				},
				dataType: 'json',
				success: function(data) {
					closeLoad();
					if (data.success) {
						window.localStorage.setItem("time", new Date().getTime());
						location.href = ctx + "/adminPage/monitor";
					} 
				},
				error: function() {
					closeLoad();
					layer.alert(commonStr.errorInfo);
				}
			});
		}
		
		layer.open({
			type: 1,
			shade: false,
			title: loginStr.title1,
			closeBtn: false,
			area: ['450px', '350px'], //宽高
			content: $('#windowDiv')
		});
	} else {
		layer.open({
			type: 1,
			shade: false,
			title: loginStr.title2,
			closeBtn: false,
			area: ['450px', '400px'], //宽高
			content: $('#addUserDiv')
		});
	}


})

function login() {
	$("#authCode").val($("#codeInput").val());
	
	var name = Base64.encode(Base64.encode($("#name").val()));
	var pass = Base64.encode(Base64.encode($("#pass").val()));
	var code = Base64.encode(Base64.encode($("#code").val()));
	var authCode = Base64.encode(Base64.encode($("#authCode").val()));
	
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/login',
		data: {
			name : name,
			pass : pass,
			code : code,
			authCode : authCode
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				if($("#remember").prop("checked")){
					window.localStorage.setItem("time", new Date().getTime());
					window.localStorage.setItem("autoKey", data.obj.autoKey);
				} else {
					window.localStorage.removeItem("autoKey");
				}
				
				location.href = ctx + "/adminPage/monitor";
			} else {
				layer.msg(data.msg);
				refreshCode('codeImg');
			}
			
		
			
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});

}

function getAuth() {
	var name = Base64.encode(Base64.encode($("#name").val()));
	var pass = Base64.encode(Base64.encode($("#pass").val()));
	var code = Base64.encode(Base64.encode($("#code").val()));
	
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/getAuth',
		data: {
			name : name,
			pass : pass,
			code : code
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				if(data.obj.auth){
					// 两步验证
					codeIndex = layer.open({
						type : 1,
						title : loginStr.googleAuth,
						area : [ '500px', '200px' ], // 宽高
						content : $('#codeDiv')
					});
				} else {
					login();
				}
			} else {
				layer.msg(data.msg);
				refreshCode('codeImg');
			}
			
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});

}




function addAdmin() {
	if ($("#adminName").val() == '') {
		layer.msg(loginStr.error1);
		return;
	}
	if ($("#adminPass").val() == '' || $("#repeatPass").val() == '') {
		layer.msg(loginStr.error2);
		return;
	}
	if ($("#adminPass").val() != $("#repeatPass").val()) {
		layer.msg(loginStr.error3);
		return;
	}

	var pwdRegex = new RegExp('(?=.*[0-9])(?=.*[a-zA-Z]).{8,100}');
	if (!pwdRegex.test($("#adminPass").val())) {
		layer.msg(loginStr.error4);
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/addAdmin',
		data: $("#adminForm").serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}

function getKey() {
	if (event.keyCode == 13) {
		getAuth();
	}
}

function getKeyCode() {
	if (event.keyCode == 13) {
		login();
	}
}

function refreshCode(id) {
	$("#" + id).attr("src", ctx + "/adminPage/login/getCode?t=" + (new Date()).getTime());
}


/*
function changeLangLogin() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/changeLang',
		data: $("#adminForm").serialize(),
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}
*/