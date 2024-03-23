$(function() {
	form.on('switch(monitor)', function(data) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/upstream/setMonitor',
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

	form.on('select(proxyType)', function(data) {
		if(data.value == 0){
			$("#tacticsDiv").show();
		}else{
			$("#tacticsDiv").hide();
		}
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
	$("#name").val("");
	$("#tactics option:first").prop("checked", true);
	$("#itemList").html("");
	$("#proxyType option:first").prop("selected", true);

	form.render();
	showWindow(upstreamStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['1000px', '600px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg(upstreamStr.noName);
		return;
	}

	var over = true;
	$("input[name='server']").each(function() {
		if ($(this).val().trim() == '') {
			over = false;
		}
	})
	$("input[name='port']").each(function() {
		if ($(this).val().trim() == '') {
			over = false;
		}
	})

	if (!over) {
		layer.msg(upstreamStr.noFill);
		return;
	}



	var upstream = {};
	upstream.id = $("#id").val();
	upstream.name = $("#name").val();
	upstream.proxyType = $("#proxyType").val();
	upstream.tactics = $("#tactics").val();

	var upstreamParamJson = $("#upstreamParamJson").val();

	var upstreamServers = [];
	$(".itemList").children().each(function() {

		var upstreamServer = {};
		upstreamServer.server = $(this).find("input[name='server']").val();
		upstreamServer.port = $(this).find("input[name='port']").val();
		upstreamServer.weight = $(this).find("input[name='weight']").val();
		upstreamServer.maxFails = $(this).find("input[name='maxFails']").val();
		upstreamServer.maxConns = $(this).find("input[name='maxConns']").val();
		upstreamServer.failTimeout = $(this).find("input[name='failTimeout']").val();
		upstreamServer.status = $(this).find("select[name='status']").val();
		upstreamServer.param = $(this).find("input[name='param']").val();
		
		upstreamServers.push(upstreamServer);
	})


	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/upstream/addOver',
		data: {
			upstreamJson: JSON.stringify(upstream),
			upstreamParamJson: upstreamParamJson,
			upstreamServerJson: JSON.stringify(upstreamServers),
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function clone(id) {
	if (confirm(serverStr.confirmClone)) {
		edit(id, true);
	}
}


function edit(id,clone) {

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/upstream/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var ext = data.obj;
				var list = ext.upstreamServerList;
				
				if (!clone) {
					$("#id").val(ext.upstream.id);
				} else {
					$("#id").val("");
				}
				
				$("#name").val(ext.upstream.name);
				$("#tactics").val(ext.upstream.tactics);
				$("#proxyType").val(ext.upstream.proxyType);
				
				if(ext.upstream.proxyType == 0){
					$("#tacticsDiv").show();
				}else{
					$("#tacticsDiv").hide();
				}
				
				$("#upstreamParamJson").val(ext.paramJson);

				var html = ``;
				for (let i = 0; i < list.length; i++) {
					var upstreamServer = list[i];
					var uuid = guid();
					html += `<tr id='${uuid}'>
									<td><input type="text" name="server" class="layui-input" value="${upstreamServer.server}"></td>
									<td><input type="number" name="port" class="layui-input" value="${upstreamServer.port}"></td>
									<td><input type="number" name="weight" class="layui-input" value="${upstreamServer.weight}"></td>
									<td><input type="number" name="maxFails" class="layui-input" value="${upstreamServer.maxFails}"></td>
									<td><input type="number" name="maxConns" class="layui-input" value="${upstreamServer.maxConns}"></td>
									<td><input type="number" name="failTimeout" class="layui-input" value="${upstreamServer.failTimeout}"></td>
									<td>
										<select name="status">
											<option ${upstreamServer.status == 'none' ? 'selected' : ''} value="none">${upstreamStr.none}</option>
											<option ${upstreamServer.status == 'down' ? 'selected' : ''} value="down">${upstreamStr.down}</option>
											<option ${upstreamServer.status == 'backup' ? 'selected' : ''} value="backup">${upstreamStr.backup}</option>
										</select>
									</td>
									<td><input type="text" name="param" class="layui-input" value="${upstreamServer.param}"></td>
									<td><button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button></td>
							</tr>`
				}
				$("#itemList").html(html);


				form.render();
				showWindow(upstreamStr.edit);
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
			url: ctx + '/adminPage/upstream/del',
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
			url : ctx + '/adminPage/upstream/del',
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

function addItem() {
	var uuid = guid();
	var html = `<tr id='${uuid}'>
						<td><input type="text" name="server" class="layui-input"></td>
						<td><input type="number" name="port" class="layui-input"></td>
						<td><input type="number" name="weight" class="layui-input"></td>
						<td><input type="number" name="maxFails" class="layui-input"></td>
						<td><input type="number" name="maxConns" class="layui-input"></td>
						<td><input type="number" name="failTimeout" class="layui-input"></td>
						<td>
							<select name="status">
								<option value="none">${upstreamStr.none}</option>
								<option value="down">${upstreamStr.down}</option>
								<option value="backup">${upstreamStr.backup}</option>
							</select>
						</td>
						<td><input type="text" name="param" class="layui-input"></td>
						<td><button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button></td>
				</tr>`
	$("#itemList").append(html);

	form.render();
}


function delTr(id) {
	$("#" + id).remove();
}


function upstreamParam() {
	var json = $("#upstreamParamJson").val();
	$("#targertId").val("upstreamParamJson");
	var params = json != '' ? JSON.parse(json) : [];
	fillTable(params);
}

var paramIndex;
function fillTable(params) {
	var html = "";
	for (var i = 0; i < params.length; i++) {
		var param = params[i];

		var uuid = guid();

		if(param.templateValue == null){
			html += `
			<tr name="param" id=${uuid}>
				<td>
					<textarea  name="name" class="layui-textarea">${param.name}</textarea>
				</td>
				<td  style="width: 50%;">
					<textarea  name="value" class="layui-textarea">${param.value}</textarea>
				</td>
				<td>
					<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>
					
					<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
					<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
				</td>
			</tr>
			`;
		} else {
			html +=  buildTemplateParam(uuid,param);
		}
	
	}

	$("#paramList").html(html);

	paramIndex = layer.open({
		type: 1,
		title: upstreamStr.extParam,
		area: ['800px', '600px'], // 宽高
		content: $('#paramJsonDiv')
	});
}

function addParam() {
	var uuid = guid();

	var html = `
	<tr name="param" id="${uuid}">
		<td>
			<textarea  name="name" class="layui-textarea"></textarea>
		</td>
		<td  style="width: 50%;">
			<textarea  name="value" class="layui-textarea"></textarea>
		</td>
		<td>
			<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>
			
			<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
			<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
		</td>
	</tr>
	`;

	$("#paramList").append(html);

}


function addParamOver() {
	var targertId = $("#targertId").val();
	var params = [];
	$("tr[name='param']").each(function() {
		var param = {};
		if ($(this).find("input[name='templateValue']").val() == null) {
			param.name = $(this).find("textarea[name='name']").val();
			param.value = $(this).find("textarea[name='value']").val();
		} else {
			param.templateValue = $(this).find("input[name='templateValue']").val();
			param.templateName = $(this).find("input[name='templateName']").val();
		}
		params.push(param);
	})
	$("#" + targertId).val(JSON.stringify(params));

	layer.close(paramIndex);
}




function upstreamMonitor() {

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/upstream/upstreamStatus',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#mail").val(data.obj.mail);
				$("#upstreamMonitor").val(data.obj.upstreamMonitor);

				form.render();
				layer.open({
					type: 1,
					title: upstreamStr.upstreamMonitor,
					area: ['600px', '300px'], // 宽高
					content: $('#monitorDiv')
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


function upstreamOver() {
	//if ($("#mail").val().indexOf("@") == -1) {
	//	layer.alert(remoteStr.emailTips);
	//	return;
	//}
	
	loadIndex = layer.load();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/upstream/upstreamOver',
		data: {
			mail: $("#mail").val(),
			upstreamMonitor: $("#upstreamMonitor").val()
		},
		dataType: 'json',
		success: function(data) {
			layer.close(loadIndex);
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.close(loadIndex);
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

		loadIndex = layer.load();
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/admin/testMail',
			data: {
				mail: $("#mail").val(),
			},
			dataType: 'json',
			success: function(data) {
				layer.close(loadIndex);
				if (data.success) {
					layer.msg(remoteStr.sendSuccess);
				} else {
					layer.alert(data.msg);
				}
			},
			error: function() {
				layer.close(loadIndex);
				layer.alert(commonStr.errorInfo);
			}
		});
	}


}

var batchIndex;
function addBatch(){
	$("#batchIp").val("");
	batchIndex = layer.open({
		type: 1,
		title: upstreamStr.addServerBatch,
		area: ['800px', '500px'], // 宽高
		content: $('#batchDiv')
	});
}

function addBatchOver(){
	var batchIp = $("#batchIp").val();
	
	var list = batchIp.split(",");
	
	var html = ``;
	for (let i = 0; i < list.length; i++) {
		var ip = list[i].split(":")[0];
		var port = list[i].split(":")[1];
		var uuid = guid();
		html += `<tr id='${uuid}'>
						<td><input type="text" name="server" class="layui-input" value="${ip}"></td>
						<td><input type="number" name="port" class="layui-input" value="${port}"></td>
						<td><input type="number" name="weight" class="layui-input"></td>
						<td><input type="number" name="maxFails" class="layui-input"></td>
						<td><input type="number" name="maxConns" class="layui-input"></td>
						<td><input type="number" name="failTimeout" class="layui-input"></td>
						<td>
							<select name="status">
								<option value="none">${upstreamStr.none}</option>
								<option value="down">${upstreamStr.down}</option>
								<option value="backup">${upstreamStr.backup}</option>
							</select>
						</td>
						<td><button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button></td>
				</tr>`
	}
	$("#itemList").html(html);
	form.render();
	layer.close(batchIndex);
}



function setOrder(id, count){
	showLoad();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/upstream/setOrder',
		data : {
			id : id,
			count : count
		},
		dataType : 'json',
		success : function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}

function editDescr(id){
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/upstream/getDescr',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#upstreamId").val(id);
				$("#descr").val(data.obj!=null?data.obj:'');
				
				layer.open({
					type: 1,
					title: upstreamStr.descr,
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
		url: ctx + '/adminPage/upstream/editDescr',
		data: {
			id: $("#upstreamId").val(),
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
