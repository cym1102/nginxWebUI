var parentId;
var groupParentId
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
			url : ctx + '/adminPage/remote/getGroupTree',
			success : function(data) {
				if (data) {
					// 渲染多选
					parentId = xmSelect.render({
					    el: '#parentId', 
					    name : 'parentId',
					    // 显示为text模式
					    model: { label: { type: 'text' } },
					    // 单选模式
					    radio: true,
					    // 选中关闭
					    clickClose: true,
					    // 树
					    tree: {
					    	show: true,
					    	// 非严格模式
					    	strict: false,
					    	// 默认展开节点
					    	expandedKeys: true,
					    },
					    data: data.obj
					})
					
					groupParentId = xmSelect.render({
					    el: '#groupParentId', 
					    name : 'parentId',
					    // 显示为text模式
					    model: { label: { type: 'text' } },
					    // 单选模式
					    radio: true,
					    // 选中关闭
					    clickClose: true,
					    // 树
					    tree: {
					    	show: true,
					    	// 非严格模式
					    	strict: false,
					    	// 默认展开节点
					    	expandedKeys: true,
					    	
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
	
	form.on('switch(monitor)', function(data){
		  $.ajax({
				type : 'POST',
				url : ctx + '/adminPage/remote/setMonitor',
				data : {
					id : data.value,
					monitor : data.elem.checked?1:0
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
					layer.alert(commonStr.errorInfo);
				}
		});
	});   
	
	
	layui.config({
		base: ctx + 'lib/layui/exts/treeTable/'
	}).extend({
		treeTable: 'treeTable'
	}).use(['treeTable'], function(){
		var treeTable = layui.treeTable;
		var re = treeTable
				.render({
					elem : '#tree-table',
					url: ctx + 'adminPage/remote/allTable' ,
					icon_key : 'descr',
					primary_key: 'id',
					parent_key: 'parentId',
					is_checkbox : false,
					end : function(e) {
						console.dir(e);
						// checkPermission();
						form.render();
					},
					cols : [{
								key : 'descr',
								title :remoteStr.alias,
								template : function(remote) {
									if(remote.type == 0){
										return `<span class="black">${remote.descr}</span>`
									}
									if(remote.type == 1){
										return `<span class="blue">${remote.descr}</span>`
									}
								}
							},{
								title : remoteStr.addr,
								template : function(remote) {
									if(remote.type == 0 && remote.id!='local'){
										return remote.protocol + "://" + remote.ip + ":" + remote.port;
									}
									return "";
								}
							},{
								key: 'version',
								title :remoteStr.version
							},{
								key: 'system',
								title : remoteStr.system
							},{	
								title : 'nginx',
								template : function(remote) {
									if(remote.nginx == 2){
										return `<span class="black">${remoteStr.unknown}</span>`
									}
									if(remote.nginx == 1){
										return `<span class="green">${remoteStr.running}</span>`
									}
									if(remote.nginx == 0){
										return `<span class="red">${remoteStr.stopped}</span>`
									}
									
									return "";
								}
							},{	
								title : commonStr.status,
								template : function(remote) {
									if(remote.status == 1){
										return `<span class="green">${remoteStr.online}</span>`
									}
									if(remote.status == 0){
										return `<span class="red">${remoteStr.offline}</span>`
									}
									
									return "";
								}
							},{	
								title : remoteStr.mailNotice,
								template : function(remote) {
									if(remote.type == 0){
										var checked = remote.monitor==1?'checked':'';
										return `<input type="checkbox" name="switch" lay-filter="monitor" value="${remote.id}" lay-text="ON|OFF" lay-skin="switch" ${checked}>`;
									}
									return "";
								}
							},{
								title :commonStr.operation,
								template : function(remote) {
									var html = "";
									
									if(remote.type == 0){
										// 服务器(同版本的才能切换)
										//console.log( remote.version + " : "  + $("#projectVersion").val());
										if(remote.status == 1 && remote.version == $("#projectVersion").val()){
											html += `<button class="layui-btn layui-btn-sm layui-btn-normal" onclick="change('${remote.id}')">${remoteStr.changeTo}</button>`;
										}
										
										if(remote.id != 'local'){
											// 本地
											if(remote.status == 1){
												html += `<button class="layui-btn layui-btn-sm" onclick="content('${remote.id}')">${remoteStr.see} conf</button>`;
											}
											
											html += `
												<button class="layui-btn layui-btn-sm" onclick="edit('${remote.id}')">${commonStr.edit}</button>
												<button class="layui-btn layui-btn-danger layui-btn-sm" onclick="del('${remote.id}')">${commonStr.del}</button>
											`;
										} else {
											// 远程
											if(remote.status == 1){
												html += `<button class="layui-btn layui-btn-sm" onclick="contentLocal()">${remoteStr.see} conf</button>`;
											}
										}
										
									} else {
										// 分组
										html += `
										<button class="layui-btn layui-btn-sm" onclick="editGroup('${remote.id}')">${commonStr.edit}</button>
										<button class="layui-btn layui-btn-danger layui-btn-sm" onclick="delGroup('${remote.id}')">${commonStr.del}</button>
										`;
									}
									return html;
								}
							}]
				});
		});
})



function add() {
	$("#id").val(""); 
	$("#ip").val(""); 
	$("#port").val(""); 
	$("#descr").val(""); 
	$("#protocol").val("http"); 
	$("#name").val(""); 
	$("#pass").val(""); 
	$("#monitor option:first").prop("checked", true);
	parentId.setValue([""]);
	
	showWindow(remoteStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '500px', '600px' ], // 宽高
		content : $('#windowDiv')
	});
}

function contentLocal(){
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/remote/readContent',
		success : function(data) {
			if (data) {
				$("#content").val(data);
				$("#content").setTextareaCount();
				
				form.render();
				
				layer.open({
					type : 1,
					title :remoteStr.content,
					area : [ '1200px', '745px' ], // 宽高
					content : $('#contentDiv')
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

function content(id) {
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/remote/content',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				$("#content").val(data.obj);
				$("#content").setTextareaCount();
				
				form.render();
				
				layer.open({
					type : 1,
					title :remoteStr.content,
					area : [ '1200px', '745px' ], // 宽高
					content : $('#contentDiv')
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

function edit(id) {
	$("#id").val(id); 
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/remote/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var remote = data.obj;
				$("#id").val(remote.id); 
				$("#pass").val(remote.pass); 
				$("#name").val(remote.name);
				$("#ip").val(remote.ip); 
				$("#port").val(remote.port); 
				$("#protocol").val(remote.protocol); 
				$("#descr").val(remote.descr); 
				$("#monitor").val(remote.monitor); 
				parentId.setValue([remote.parentId]);
				
				form.render();
				showWindow(remoteStr.edit);
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
			url : ctx + '/adminPage/remote/del',
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



function change(id){
	if(confirm(remoteStr.confirmChange)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/remote/change',
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

function asycSelect(){
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/getCmdRemote',
		data : {
			
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {

				// 渲染多选
				xmSelect.render({
				    el: '#remoteFromId', 
				    name : 'fromId',
				    // 显示为text模式
				    model: { label: { type: 'text' } },
				    // 单选模式
				    radio: true,
				    // 高度
				    height: '400px',
				    // 选中关闭
				    clickClose: true,
				    // 树
				    tree: {
				    	show: true,
				    	// 非严格模式
				    	strict: true,
				    	// 默认展开节点
				    	expandedKeys: true,
				    },
				    data: data.obj
				})
				
				
				// 渲染多选
				xmSelect.render({
				    el: '#remoteSelectId', 
				    name : 'remoteId',
				    // 显示为text模式
				    model: { label: { type: 'text' } },
				    // 单选模式
				    radio: false,
				    // 高度
				    height: '400px',
				    // 选中关闭
				    clickClose: false,
				    // 树
				    tree: {
				    	show: true,
				    	// 非严格模式
				    	strict: true,
				    	// 默认展开节点
				    	expandedKeys: true,
				    },
				    data: data.obj
				})
				
				form.render();
				
				layer.open({
					type : 1,
					title : remoteStr.asycSelect,
					area : [ '600px', '500px' ], // 宽高
					content : $('#selectDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
	
}


function asycOver(){
	
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/asyc',
		data : $("#asycForm").serialize(),
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.msg(remoteStr.asycSuccess)
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function cmdGroup(){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/getCmdRemote',
		data : {
		
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				
				// 渲染多选
				xmSelect.render({
				    el: '#remoteCmdSelectId', 
				    name : 'remoteId',
				    // 显示为text模式
				    model: { label: { type: 'text' } },
				    // 单选模式
				    radio: false,
				    // 高度
				    height: '400px',
				    // 选中关闭
				    clickClose: false,
				    // 树
				    tree: {
				    	show: true,
				    	// 非严格模式
				    	strict: true,
				    	// 默认展开节点
				    	expandedKeys: true,
				    },
				    data: data.obj
				})
				
				form.render();
				
				layer.open({
					type : 1,
					title : remoteStr.cmdOver,
					area : [ '600px', '500px' ], // 宽高
					content : $('#cmdDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
	
}



function cmdOver(){
	
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/cmdOver',
		data : $("#cmdForm").serialize(),
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.open({
					  type: 0, 
					  area : [ '810px', '400px' ],
					  content: data.obj
				});
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function addGroup(){
	$("#groupId").val("");
	$("#GroupName").val("");
	groupParentId.setValue([""]);
	
	layer.open({
		type : 1,
		title :remoteStr.addGroup,
		area : [ '400px', '500px' ], // 宽高
		content : $('#groupDiv')
	});
	
}


function editGroup(id) {
	$("#groupId").val(id); 
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/remote/groupDetail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var group = data.obj;
				$("#groupId").val(group.id); 
				$("#groupName").val(group.name);
				groupParentId.setValue([group.parentId]);
				layer.open({
					type : 1,
					title :remoteStr.editGroup,
					area : [ '400px', '500px' ], // 宽高
					content : $('#groupDiv')
				});
				
				form.render();
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}


function addGroupOver(){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/addGroupOver',
		data : $("#addGroupForm").serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function delGroup(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/remote/delGroup',
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
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
	
}


function nginxMonitor(){
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/nginxStatus',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				$("#mail").val(data.obj.mail);
				$("#nginxMonitor").val(data.obj.nginxMonitor);
				
				form.render();
				layer.open({
					type : 1,
					title : remoteStr.nginxMonitor,
					area : [ '650px', '300px' ], // 宽高
					content : $('#nginxDiv')
				});
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}

function nginxOver(){
		var myreg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;                
		if ($("#mail").val() == '' || !myreg.test($("#mail").val())) {                    
			layer.alert(remoteStr.emailTips);               
			return;                
		}
		
		$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/nginxOver',
		data : {
			mail : $("#mail").val(),
			nginxMonitor : $("#nginxMonitor").val()
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
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}



var loadIndex;
function testMail(){
	if(confirm(remoteStr.testSend)){
		var myreg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;                
		if ($("#mail").val() == '' || !myreg.test($("#mail").val())) {                    
			layer.alert(remoteStr.emailTips);               
			return;                
		}
		
		loadIndex = layer.load();
		$.ajax({
			type: 'POST',
			url: ctx + 'adminPage/admin/testMail',
			data: {
				mail: $("#mail").val(),
			},
			dataType: 'json',
			success: function(data) {
				layer.close(loadIndex);
				if (data.success) {
					layer.msg(remoteStr.sendSuccess);
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}




function addOver() {
	if($("#ip").val().trim() == '' || $("#port").val().trim() == '' || $("#name").val().trim() == '' || $("#pass").val().trim() == ''){
		layer.msg(remoteStr.notFill);
		return;
	}
	load = layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/getAuth',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			layer.close(load);
			if (data.success) {
				if(data.obj.auth){
					$("#authCode").show();
					$("#imgCode").hide();
				} else {
					$("#authCode").hide();
					$("#imgCode").show();
				}
				
				refreshCode();
				codeIndex = layer.open({
					type : 1,
					title : loginStr.googleAuth,
					area : [ '500px', '200px' ], // 宽高
					content : $('#codeDiv')
				});
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.close(load);
			layer.alert(commonStr.errorInfo);
		}
	});
	
}

function addOverSubmit(){
	$("#code").val($("#codeInput").val());
	$("#auth").val($("#authInput").val());
	load = layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/remote/addOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			layer.close(load);
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.close(load);
			layer.alert(commonStr.errorInfo);
		}
	});
}

function refreshCode(){
	var src = $("#protocol").val() + "://" + $("#ip").val() + ":" + $("#port").val() + "/adminPage/login/getRemoteCode?t=" + guid();
	$("#codeImg").attr("src", src)
}