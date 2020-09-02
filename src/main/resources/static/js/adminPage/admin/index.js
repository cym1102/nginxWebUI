function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#pass").val(""); 
	$("#auth").val("false"); 
	
	showWindow(adminStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '400px', '500px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/admin/addOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function edit(id) {
	$("#id").val(id); 
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/admin/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var admin = data.obj;
				$("#id").val(admin.id); 
				$("#pass").val(admin.pass); 
				$("#name").val(admin.name);
				$("#auth").val(admin.auth + "");
				
				form.render();
				showWindow(commonStr.edit);
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function del(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/admin/del',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				}else{
					layer.msg(data.msg)
				}
			},
			error : function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}

function downApk(){
	window.open("https://www.wandoujia.com/apps/32913");
}

function readme(){
	window.open(ctx + "img/readme.pdf");
}

function qr(name, key){
	$("#qrImg").attr("src", ctx + "adminPage/admin/qr?url=" + encodeURIComponent(`otpauth://totp/${name}?secret=${key}&issuer=nginxWebUI`));
	
	layer.open({
		type : 1,
		title : adminStr.qr,
		area : [ '350px', '380px' ], // 宽高
		content : $('#qrDiv')
	});
}

function test(key){
	$("#key").val(key);
	
	codeIndex = layer.open({
		type : 1,
		title : loginStr.googleAuth,
		area : [ '400px', '200px' ], // 宽高
		content : $('#codeDiv')
	});
}

function testOver(){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/admin/testAuth',
		data : {
			key : $("#key").val(),
			code : $("#codeInput").val()
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				if(data.obj){
					layer.msg(adminStr.testSuccess);
				}else{
					layer.msg(adminStr.testFail);
				}
				
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}