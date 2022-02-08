function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}


function add() {
	$("#id").val(""); 
	$("#path").val(""); 
	
	showWindow(adminStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '550px', '350px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#path").val() == ''){
		alert(commonStr.noDir);
		return;
	}
	
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/log/addOver',
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
		url : ctx + '/adminPage/log/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var log = data.obj;
				$("#id").val(log.id); 
				$("#path").val(log.path); 
				
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
			url : ctx + '/adminPage/log/del',
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


function selectRootCustom(){
	rootSelect.selectOne(function callBack(val){
		$("#path").val(val);
		//$("#fileName").html(val);
	});
}

function tail(id,path){
	layer.open({
		  type: 2, 
		  area : [ '1300px', '671px' ], // 宽高
		  title : path,
		  resize  : false,
		  content: ctx + "/adminPage/log/tail?id=" + id
	}); 
}

function down(id){
	window.open(ctx + "/adminPage/log/down?id=" + id);
}