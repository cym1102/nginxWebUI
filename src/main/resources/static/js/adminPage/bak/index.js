
function content(id) {
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/bak/content',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var bak = data.obj;
				$("#preview").val(bak.content);
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

function del(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/del',
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



function replace(id){
	if(confirm(bakStr.restoreNotice)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/replace',
			data : {
				id : id
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