var parentId;
var groupParentId;
var remoteFromId;
var remoteSelectId;
var remoteCmdId;

var load;
$(function() {
	// 加载组件
	layui.config({
		base: ctx + '/lib/layui/exts/xmSelect/'
	}).extend({
		xmSelect: 'xm-select'
	}).use(['xmSelect'], function() {
		var xmSelect = layui.xmSelect;

		// 每个下拉控件单独请求ajax, 防止产生绑定关联
		$.ajax({
			type: 'GET',
			url: ctx + '/adminPage/remote/getGroupTree',
			success: function(data) {
				if (data.success) {
					parentId = xmSelect.render({
						el: '#parentId',
						name: 'parentId',
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

				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});


		$.ajax({
			type: 'GET',
			url: ctx + '/adminPage/remote/getGroupTree',
			success: function(data) {
				if (data.success) {
					groupParentId = xmSelect.render({
						el: '#groupParentId',
						name: 'parentId',
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
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});


		$.ajax({
			type: 'GET',
			url: ctx + '/adminPage/remote/getCmdRemote',
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					// 渲染多选
					remoteFromId = xmSelect.render({
						el: '#remoteFromId',
						name: 'fromId',
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

				} else {
					layer.msg(data.msg)
				}
			},
			error: function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});


		$.ajax({
			type: 'GET',
			url: ctx + '/adminPage/remote/getCmdRemote',
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					// 渲染多选
					remoteSelectId = xmSelect.render({
						el: '#remoteSelectId',
						name: 'remoteId',
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

				} else {
					layer.msg(data.msg)
				}
			},
			error: function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	})

	form.on('switch(monitor)', function(data) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/remote/setMonitor',
			data: {
				id: data.value,
				monitor: data.elem.checked ? 1 : 0
			},
			dataType: 'json',
			success: function(data) {

				if (data.success) {
					//location.reload();
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	});

	form.on('select(cmd)', function(data) {
		if (data.value == 'reload') {
			$("#intervalDiv").show();
		} else {
			$("#intervalDiv").hide();
		}
	});


	layui.treeTable.render({
		elem: '#tree-table',
		url: ctx + '/adminPage/remote/allTable',
		tree: {
			data: {
				isSimpleData: true
			},
			view: {
				iconLeaf: `<i class="layui-icon layui-icon-component"></i> `,
				expandAllDefault: true
			},
			customName :{
				isParent : "type"
			}
		},
		cols: [[{
			field: 'name',
			width: 250,
			title: remoteStr.alias,
			templet: function(remote) {
				if (remote.type == 0) {
					return `<span class="name">${remote.descr}</span>`
				}
				if (remote.type == 1) {
					return `<span class="name">${remote.descr}</span>`
				}
			}
		}, {
			title: remoteStr.addr,
			minWidth: 200,
			templet: function(remote) {
				if (remote.type == 0 && remote.id != 'local') {
					return remote.protocol + "://" + remote.ip + ":" + remote.port;
				}
				return "";
			}
		}, {
			field: 'version',
			title: remoteStr.version
		}, {
			field: 'system',
			title: remoteStr.system
		}, {
			title: 'nginx',
			templet: function(remote) {
				if (remote.nginx == 2) {
					return `<span class="black">${remoteStr.unknown}</span>`
				}
				if (remote.nginx == 1) {
					return `<span class="green">${remoteStr.running}</span>`
				}
				if (remote.nginx == 0) {
					return `<span class="red">${remoteStr.stopped}</span>`
				}

				return "";
			}
		}, {
			title: commonStr.status,
			templet: function(remote) {
				if (remote.status == 1) {
					return `<span class="green">${remoteStr.online}</span>`
				}
				if (remote.status == 0) {
					return `<span class="red">${remoteStr.offline}</span>`
				}

				return "";
			}
		}, {
			title: remoteStr.mailNotice,
			templet: function(remote) {
				if (remote.type == 0) {
					var checked = remote.monitor == 1 ? 'checked' : '';
					return `<input type="checkbox" name="switch" lay-filter="monitor" value="${remote.id}" lay-text="ON|OFF" lay-skin="switch" ${checked}>`;
				}
				return "";
			}
		}, {
			title: remoteStr.select,
			templet: function(remote) {
				if (remote.select == 1) {
					return `<span class="green">${remoteStr.yes}</span>`
				}

				return "";
			}
		}, {
			title: commonStr.operation,
			width: 300,
			templet: function(remote) {
				var html = "";

				if (remote.type == 0) {
					// 服务器(同版本的才能切换)
					//console.log( remote.version + " : "  + $("#projectVersion").val());
					if (remote.status == 1 && remote.version == $("#projectVersion").val()) {
						html += `
						<div class="layui-inline" style="vertical-align: baseline">
							<button class="layui-btn layui-btn-xs layui-btn-normal" onclick="change('${remote.id}')">${remoteStr.changeTo}</button>
						</div>	
						`;
					}

					if (remote.id != 'local') {
						// 本地
						if (remote.status == 1) {
							html += `<div class="layui-inline" style="vertical-align: baseline">
										<button class="layui-btn layui-btn-xs" onclick="content('${remote.id}')">${remoteStr.see} conf</button>
									 </div>	
									`;
						}

						html += `
								<div class="layui-inline" style="vertical-align: baseline">
									<button class="layui-btn layui-btn-xs" onclick="edit('${remote.id}')">${commonStr.edit}</button>
								</div>
								<div class="layui-inline" style="vertical-align: baseline">
									<button class="layui-btn layui-btn-danger layui-btn-xs" onclick="del('${remote.id}')">${commonStr.del}</button>
								</div>
								`;
					} else {
						// 远程
						if (remote.status == 1) {
							html += `
							<div class="layui-inline" style="vertical-align: baseline">
								<button class="layui-btn layui-btn-xs" onclick="contentLocal()">${remoteStr.see} conf</button>
							</div>
							`;
						}
					}

				} else {
					// 分组
					html += `
							<div class="layui-inline" style="vertical-align: baseline">
								<button class="layui-btn layui-btn-xs" onclick="editGroup('${remote.id}')">${commonStr.edit}</button>
							</div>
							<div class="layui-inline" style="vertical-align: baseline">
								<button class="layui-btn layui-btn-danger layui-btn-xs" onclick="delGroup('${remote.id}')">${commonStr.del}</button>
							</div>
							`;
				}
				return html;
			}
		}]]
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


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['500px', '550px'], // 宽高
		content: $('#windowDiv')
	});
}

function contentLocal() {
	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/remote/readContent',
		success: function(data) {
			if (data) {

				$("#preview").val(data);
				layer.open({
					type: 1,
					title: commonStr.preview,
					area: ['800px', '600px'], // 宽高
					content: $('#previewDiv')
				});


			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function content(id) {
	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/remote/content',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {

				$("#preview").val(data.obj);
				layer.open({
					type: 1,
					title: commonStr.preview,
					area: ['800px', '600px'], // 宽高
					content: $('#previewDiv')
				});
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function edit(id) {
	$("#id").val(id);

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/remote/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
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
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function del(id) {
	if (confirm(commonStr.confirmDel)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/remote/del',
			data: {
				id: id
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
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}



function change(id) {
	if (confirm(remoteStr.confirmChange)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/remote/change',
			data: {
				id: id
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
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}

function asycSelect() {

	layer.open({
		type: 1,
		title: remoteStr.asycSelect,
		area: ['600px', '500px'], // 宽高
		content: $('#selectDiv')
	});
}

function selectAll() {
	$("input[name='asycData']").prop("checked", true);
	form.render();
}

function asycOver() {

	$(".asycData")
	var asycData = [];
	$(".asycData").each(function() {
		if ($(this).prop("checked")) {
			asycData.push($(this).val());
		}
	});
	if (asycData.length == 0) {
		layer.msg(remoteStr.noData);
		return;
	}


	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/asyc',
		data: $("#asycForm").serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				layer.msg(remoteStr.asycSuccess)
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function cmdGroup() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/getCmdRemote',
		data: {

		},
		dataType: 'json',
		success: function(data) {
			layer.closeAll();
			if (data.success) {

				// 渲染多选
				remoteCmdId = xmSelect.render({
					el: '#remoteCmdSelectId',
					name: 'remoteId',
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
					type: 1,
					title: remoteStr.cmdOver,
					area: ['700px', '500px'], // 宽高
					content: $('#cmdDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});

}



function cmdOver() {
	if (remoteCmdId.getValue().length == 0) {
		layer.msg(remoteStr.noSelect);
		return;
	}


	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/cmdOver',
		data: $("#cmdForm").serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				layer.open({
					type: 0,
					area: ['810px', '400px'],
					content: data.obj
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function addGroup() {
	$("#groupId").val("");
	$("#groupName").val("");
	groupParentId.setValue([""]);

	layer.open({
		type: 1,
		title: remoteStr.addGroup,
		area: ['400px', '450px'], // 宽高
		content: $('#groupDiv')
	});

}


function editGroup(id) {
	$("#groupId").val(id);

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/remote/groupDetail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var group = data.obj;
				$("#groupId").val(group.id);
				$("#groupName").val(group.name);
				groupParentId.setValue([group.parentId]);
				layer.open({
					type: 1,
					title: remoteStr.editGroup,
					area: ['400px', '500px'], // 宽高
					content: $('#groupDiv')
				});

				form.render();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}


function addGroupOver() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/addGroupOver',
		data: $("#addGroupForm").serialize(),
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}


function delGroup(id) {
	if (confirm(commonStr.confirmDel)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/remote/delGroup',
			data: {
				id: id
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
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}

}


function nginxMonitor() {

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/nginxStatus',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#mail").val(data.obj.mail);
				$("#nginxMonitor").val(data.obj.nginxMonitor);

				form.render();
				layer.open({
					type: 1,
					title: remoteStr.nginxMonitor,
					area: ['650px', '300px'], // 宽高
					content: $('#nginxDiv')
				});
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}

function nginxOver() {
	if ($("#mail").val().indexOf("@") == -1) {
		layer.alert(remoteStr.emailTips);
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/nginxOver',
		data: {
			mail: $("#mail").val(),
			nginxMonitor: $("#nginxMonitor").val()
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
			layer.closeAll();
			layer.alert(commonStr.errorInfo);
		}
	});
}



var loadIndex;
function testMail() {
	if (confirm(remoteStr.testSend)) {
		if ($("#mail").val().indexOf("@") == -1) {
			layer.alert(remoteStr.emailTips);
			return;
		}

		showLoad();
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/admin/testMail',
			data: {
				mail: $("#mail").val(),
			},
			dataType: 'json',
			success: function(data) {
				closeLoad();
				if (data.success) {
					layer.msg(remoteStr.sendSuccess);
				} else {
					layer.alert(data.msg);
				}
			},
			error: function() {
				closeLoad();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}




function addOver() {
	if ($("#ip").val().trim() == '' || $("#port").val().trim() == '' || $("#name").val().trim() == '' || $("#pass").val().trim() == '') {
		layer.msg(remoteStr.notFill);
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/getAuth',
		data: $('#addForm').serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				if (String(data.obj.auth) == 'true') {
					$("#authCode").show();
					$("#imgCode").hide();
				} else {
					$("#authCode").hide();
					$("#imgCode").show();
				}

				refreshCode();
				codeIndex = layer.open({
					type: 1,
					title: remoteStr.verify,
					area: ['500px', '200px'], // 宽高
					content: $('#codeDiv')
				});
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});

}

function addOverSubmit() {
	$("#code").val($("#codeInput").val());
	$("#auth").val($("#authInput").val());
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/remote/addOver',
		data: $('#addForm').serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
				refreshCode();
			}
		},
		error: function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}

function refreshCode() {
	var src = encodeURIComponent($("#protocol").val() + "://" + $("#ip").val() + ":" + $("#port").val() + "/adminPage/login/getRemoteCode?t=" + guid());

	$("#codeImg").attr("src", ctx + "/adminPage/remote/src?url=" + src)
}