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
				alert("出错了,请联系技术人员!");
			}
		});
	})
	
	
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
								title : '别名',
								template : function(remote) {
									if(remote.type == 0){
										return `<span class="black">${remote.descr}</span>`
									}
									if(remote.type == 1){
										return `<span class="blue">${remote.descr}</span>`
									}
								}
							},{
								key: 'protocol',
								title : '协议'
							},{
								key: 'ip',
								title : 'ip'
							},{
								key: 'port',
								title : '端口',
								template : function(remote) {
									return remote.port != null?remote.port:"";
								}
							},{
								key: 'version',
								title : '版本'
							},{
								key: 'system',
								title : '操作系统'
							},{	
								title : 'nginx',
								template : function(remote) {
									if(remote.nginx == 2){
										return `<span class="black">未知</span>`
									}
									if(remote.nginx == 1){
										return `<span class="green">运行中</span>`
									}
									if(remote.nginx == 0){
										return `<span class="red">未运行</span>`
									}
									
									return "";
								}
							},{	
								title : '状态',
								template : function(remote) {
									if(remote.status == 1){
										return `<span class="green">在线</span>`
									}
									if(remote.status == 0){
										return `<span class="red">掉线</span>`
									}
									
									return "";
								}
							},{
								title : '操作',
								template : function(remote) {
									var html = "";
									
									if(remote.type == 0){
										// 服务器(同版本的才能切换)
										if(remote.status == 1 && remote.version == $("#version").val()){
											html += `<button class="layui-btn layui-btn-sm layui-btn-normal" onclick="change('${remote.id}')">切换到此服务器</button>`;
										}
										
										if(remote.id != '本地'){
											// 本地
											if(remote.status == 1){
												html += `<button class="layui-btn layui-btn-sm" onclick="content('${remote.id}')">查看conf</button>`;
											}
											
											html += `
												<button class="layui-btn layui-btn-sm" onclick="edit('${remote.id}')">编辑</button>
												<button class="layui-btn layui-btn-danger layui-btn-sm" onclick="del('${remote.id}')">删除</button>
											`;
										} else {
											// 远程
											if(remote.status == 1){
												html += `<button class="layui-btn layui-btn-sm" onclick="contentLocal()">查看conf</button>`;
											}
										}
										
									} else {
										// 分组
										html += `
										<button class="layui-btn layui-btn-sm" onclick="editGroup('${remote.id}')">编辑</button>
										<button class="layui-btn layui-btn-danger layui-btn-sm" onclick="delGroup('${remote.id}')">删除</button>
										`;
									}
									return html;
								}
							}]
				});
		});
})

function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#ip").val(""); 
	$("#port").val(""); 
	$("#protocol").val("http"); 
	$("#name").val(""); 
	$("#pass").val(""); 
	//$("#parentId option:first").prop("checked", true);
	parentId.setValue([""]);
	
	showWindow("添加远程服务器");
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '500px', '700px' ], // 宽高
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
					title : "内容",
					area : [ '1200px', '745px' ], // 宽高
					content : $('#contentDiv')
				});
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
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
					title : "内容",
					area : [ '1200px', '745px' ], // 宽高
					content : $('#contentDiv')
				});
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

var load;
function addOver() {
	if($("#ip").val().trim() == '' || $("#port").val().trim() == '' || $("#name").val().trim() == '' || $("#pass").val().trim() == ''){
		layer.msg("未填写完成");
		return;
	}
	
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
			alert("出错了,请联系技术人员!");
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
				//$("#parentId").val(remote.parentId); 
				parentId.setValue([remote.parentId]);
				
				form.render();
				showWindow("编辑远程服务器");
			}else{
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
				alert("出错了,请联系技术人员!");
			}
		});
	}
}



function change(id){
	if(confirm("确认切换到此服务器?")){
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
				alert("出错了,请联系技术人员!");
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
					title : "同步服务器",
					area : [ '600px', '500px' ], // 宽高
					content : $('#selectDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			alert("出错了,请联系技术人员!");
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
				layer.msg("同步成功")
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			alert("出错了,请联系技术人员!");
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
					title : "执行命令",
					area : [ '600px', '500px' ], // 宽高
					content : $('#cmdDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.closeAll();
			alert("出错了,请联系技术人员!");
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
			alert("出错了,请联系技术人员!");
		}
	});
}


function addGroup(){
	$("#groupId").val("");
	$("#GroupName").val("");
	groupParentId.setValue([""]);
	
	layer.open({
		type : 1,
		title : "添加分组",
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
					title : "编辑分组",
					area : [ '400px', '500px' ], // 宽高
					content : $('#groupDiv')
				});
				
				form.render();
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
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
			alert("出错了,请联系技术人员!");
		}
	});
}


function delGroup(id){
	if(confirm("确认删除?")){
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
				alert("出错了,请联系技术人员!");
			}
		});
	}
	
}