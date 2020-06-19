$(function(){
	form.on('switch(autoRenew)', function(data){
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
						//location.reload();
					} else {
						layer.msg(data.msg);
					}
				},
				error : function() {
					alert("出错了,请联系技术人员!");
				}
		});
	});   
	
	form.on('select(dnsType)', function(data) {
		checkType(data.value);
	});
})


function checkType(value){
	
	if (value == 'ali') {
		$("#ali").show();
		$("#dp").hide();
	} 
	if (value == 'dp') {
		$("#ali").hide();
		$("#dp").show();
	} 
}

function add() {
	$("#id").val(""); 
	$("#domain").val(""); 
	$("#dnsType option:first").prop("selected", true);
	$("#aliKey").val(""); 
	$("#aliSecret").val(""); 
	$("#dpId").val(""); 
	$("#dpKey").val(""); 
	checkType('ali');
	
	showWindow("添加证书");
}


function edit(id) {
	$("#id").val(id);

	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/cert/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				
				var cert = data.obj;
				$("#domain").val(cert.domain); 
				$("#dnsType").val(cert.dnsType!=null?cert.dnsType:'ali');
				$("#aliKey").val(cert.aliKey); 
				$("#aliSecret").val(cert.aliSecret); 
				$("#dpId").val(cert.dpId); 
				$("#dpKey").val(cert.dpKey); 
				checkType(cert.dnsType!=null?cert.dnsType:'ali');
				
				showWindow("编辑证书");
				
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '500px', '400px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if ($("#domain").val() == "") {
		layer.msg("域名为空");
		return;
	}
	
	if($("#dnsType").val() == 'ali'){
		if($("#aliKey").val() == '' || $("#aliSecret").val() == ''){
			layer.msg("填写不完整");
			return;
		}
	}
	if($("#dnsType").val() == 'dp'){
		if($("#dpId").val() == '' || $("#dpKey").val() == ''){
			layer.msg("填写不完整");
			return;
		}
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
	
	if(confirm("确认开始申请？申请时间较长，请耐心等待。")){
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
					alert("申请成功!");
					location.reload();
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				layer.closeAll();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


function renew(id){
	
	if(confirm("确认开始续签？申请时间较长，请耐心等待。")){
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
					alert("续签成功!");
					location.reload();
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
			},
			error : function() {
				layer.closeAll();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}