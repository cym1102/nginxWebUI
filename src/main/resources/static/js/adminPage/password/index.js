function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#pass").val(""); 
	$("#descr").val(""); 
	
	
	showWindow(commonStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '400px', '300px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#name").val() == '' || $("#pass").val() == ''){
		layer.msg(passwordStr.notFill);
		return;
	}
	
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/password/addOver',
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
		url : ctx + '/adminPage/password/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var password = data.obj;
				$("#id").val(password.id); 
				$("#pass").val(password.pass); 
				$("#name").val(password.name);
				$("#descr").val(password.descr); 
				
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
			url : ctx + '/adminPage/password/del',
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

