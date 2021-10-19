var parentId;


$(function(){
	// 加载组件
	layui.config({
		base: ctx + 'lib/layui/exts/xmSelect/'
	}).extend({
		xmSelect: 'xm-select'
	}).use(['xmSelect'], function(){
		var xmSelect = layui.xmSelect;
		
		$.ajax({
			type : 'GET',
			url : ctx + '/adminPage/admin/getGroupTree',
			success : function(data) {
				if (data) {
					// 渲染多选
					parentId = xmSelect.render({
					    el: '#parentId', 
					    name : 'parentId',
					    // 显示为text模式
					    model: { label: { type: 'text' } },
					    // 单选模式
					    radio: false,
					    // 选中关闭
					    clickClose: false,
					    // 树
					    tree: {
					    	show: true,
					    	// 严格模式
					    	strict: true,
					    	// 默认展开节点
					    	expandedKeys: true,
							// 极简模式
							simple : true
					    },
					    data: data.obj
					})
				}else{
					layer.msg(data.msg);
				}
			},
			error : function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	})
	
	
	form.on('select(type)', function(data) {
		if(data.value == 0){
			$("#remoteTree").hide();
		} else {
			$("#remoteTree").show();
		}
	});
	
})


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#pass").val(""); 
	$("#auth").val("false"); 
	$("#api").val("false"); 
	$("#type option:first").prop("checked", true); 
	$("#remoteTree").hide();
	
	parentId.setValue([""]);
	
	form.render();
	
	showWindow(adminStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '450px', '600px' ], // 宽高
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
				var admin = data.obj.admin;
				$("#id").val(admin.id); 
				$("#pass").val(admin.pass); 
				$("#name").val(admin.name);
				$("#auth").val(admin.auth + "");
				$("#api").val(admin.api+ "");
				$("#type").val(admin.type); 
				parentId.setValue(data.obj.groupIds);
				if(admin.type == 0){
					$("#remoteTree").hide();
				} else {
					$("#remoteTree").show();
				}
		
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
	layer.open({
		type : 1,
		title : adminStr.downApk,
		area : [ '600px', '350px' ], // 宽高
		content : $('#downDiv')
	});
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

function apiPage(){
	window.open(ctx + "/doc.html")
}


function permission(id){
	
	
}