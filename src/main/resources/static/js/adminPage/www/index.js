var load;
$(function() {
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem: '#upload',
			url: '/adminPage/main/upload',
			accept: 'file',
			before: function(res) {
				showLoad();
			},
			done: function(res) {
				closeLoad();
				// 上传完毕回调
				if (res.success) {
					var path = res.obj.split('/');
					if (path[path.length - 1].indexOf('.zip') == -1) {
						layer.alert("只能上传zip文件");
						return;
					}

					$("#fileName").html(path[path.length - 1]);
					$("#dirTemp").val(res.obj);
				}

			},
			error: function() {
				closeLoad();
				// 请求异常回调
			}
		});

	});
	
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
	$("#dir").val("");
	$("#dirTemp").html("");
	$("#fileName").html("");
	//$("#zipDiv").show();

	// $("#action").val("addOver");

	showWindow(wwwStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['560px', '360px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#dir").val() == '') {
		layer.alert(wwwStr.noFill);
		return;
	}

	if ($("#dirTemp").val() == '') {
		layer.alert(wwwStr.noUpload);
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/www/addOver',
		data: $('#addForm').serialize(),
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
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


function del(id) {
	if (confirm(commonStr.confirmDel)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/www/del',
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
			url : ctx + '/adminPage/www/del',
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

function copy(str) {
	var oInput = document.createElement('input');
	oInput.value = str;
	document.body.appendChild(oInput);
	oInput.select(); // 选择对象
	document.execCommand("Copy"); // 执行浏览器复制命令
	oInput.className = 'oInput';
	oInput.style.display = 'none';
	layer.msg(wwwStr.cpoySuccess);
}

function edit(id) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/www/detail',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var www = data.obj;

				$("#id").val(www.id);
				$("#dir").val(www.dir);
				$("#dirTemp").html("");
				$("#fileName").html("");
				//$("#nameDiv").show();
				//$("#zipDiv").hide();

				//$("#action").val("rename");
				showWindow(wwwStr.editOrUpdate);
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}



function selectRootCustom() {
	rootSelect.selectOne(function callBack(val) {
		$("#dir").val(val);
		//$("#fileName").html(val);
	});
}


function editDescr(id) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/www/getDescr',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#wwwId").val(id);
				$("#descr").val(data.obj != null ? data.obj : '');

				layer.open({
					type: 1,
					title: commonStr.descr,
					area: ['500px', '360px'], // 宽高
					content: $('#descrDiv')
				});

			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});

}

function editDescrOver() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/www/editDescr',
		data: {
			id: $("#wwwId").val(),
			descr: $("#descr").val()
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



function clean(id) {
	if (confirm(wwwStr.clean + "?")) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/www/clean',
			data: {
				id: id
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					layer.msg(wwwStr.cleanSuccess)
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