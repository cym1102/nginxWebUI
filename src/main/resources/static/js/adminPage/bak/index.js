
function content(path) {
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/bak/content',
		dataType : 'json',
		data : {
			path : path
		},
		success : function(data) {
			if (data.success) {
				$("#preview").val(data.obj);
				layer.open({
					type: 1,
					title: commonStr.preview,
					area: ['800px', '600px'], // 宽高
					content: $('#previewDiv')
				});
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function del(path){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/del',
			data : {
				path : path
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



function replace(path){
	if(confirm(bakStr.restoreNotice)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/replace',
			data : {
				path : path
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					layer.msg(bakStr.operationSuccess);
					
					parent.loadOrg();
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

function delAll(){
	if(confirm(bakStr.clearNotice)){
		$.ajax({
			type : 'GET',
			url : ctx + '/adminPage/bak/delAll',
			dataType : 'json',
		
			success : function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg);
				}
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
	
}