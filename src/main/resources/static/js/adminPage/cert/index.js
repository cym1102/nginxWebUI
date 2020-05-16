$(function(){
	form.on('switch(autoRenew)', function(data){
		  //console.log(data.elem); //得到checkbox原始DOM对象
		  //console.log(data.elem.checked); //是否被选中，true或者false
		  //console.log(data.value); //复选框value值，也可以通过data.elem.value得到
		  //console.log(data.othis); //得到美化后的DOM对象
		  
		  $.ajax({
				type : 'POST',
				url : ctx + '/adminPage/cert/setAutoRenew',
				data : {
					id : data.value,
					autoRenew : data.elem.checked?1:0
				},
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
	});        
	
})


function add() {
	$("#id").val(""); 
	$("#domain").val(""); 
	
	showWindow("添加证书");
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
	if ($("#domain").val() == "") {
		layer.msg("域名为空");
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/cert/addOver',
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
			alert("出错了,请联系技术人员!");
		}
	});
}


function del(id){
	if(confirm("确认删除?")){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/del',
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
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


function apply(id){
	
	if(confirm("确认申请? 过程中nginx将被暂时关闭.")){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/apply',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					location.reload();
				}else{
					layer.open({
						  type: 0, 
						  area : [ '800px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


function renew(id){
	
	if(confirm("确认续签? 过程中nginx将被暂时关闭.")){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/cert/renew',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					location.reload();
				}else{
					layer.open({
						  type: 0, 
						  area : [ '800px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}