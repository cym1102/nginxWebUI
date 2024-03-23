$(function() {
	form.on('checkbox(checkAll)', function(data) {
		if (data.elem.checked) {
			$("input[name='ids']").prop("checked", true)
		} else {
			$("input[name='ids']").prop("checked", false)
		}

		form.render();
	});	
})

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




function delMany() {
	if (confirm(commonStr.confirmDel)) {
		var ids = [];

		$("input[name='ids']").each(function() {
			if ($(this).prop("checked")) {
				ids.push($(this).val());
			}
		})

		if (ids.length == 0) {
			layer.msg(commonStr.unselected);
			return;
		}

		$.ajax({
			type: 'POST',
			url : ctx + '/adminPage/log/del',
			data: {
				id: ids.join(",")
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg)
				}
			},
			error: function() {
				layer.alert("请求失败，请刷新重试");
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
		  area : [ '90%', '90%' ], // 宽高
		  title : path,
		  resize  : false,
		  content: ctx + "/adminPage/log/tail?id=" + id
	}); 
}

function down(id){
	window.open(ctx + "/adminPage/log/down?id=" + id);
}