


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '1200px', '745px' ], // 宽高
		content : $('#windowDiv')
	});
}


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
				$("#content").val(data.obj);

				$("#content").setTextareaCount();
				
				form.render();
				
				showWindow("内容");
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function del(path){
	if(confirm("确认删除?")){
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
				alert("出错了,请联系技术人员!");
			}
		});
	}
}



function replace(path){
	if(confirm("确认还原此版本?")){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/replace',
			data : {
				path : path
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					layer.msg("还原成功")
				}else{
					layer.msg(data.msg)
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}

function delAll(){
	if(confirm("是否清空备份?")){
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
				alert("出错了,请联系技术人员!");
			}
		});
	}
	
}