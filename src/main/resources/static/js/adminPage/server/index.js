$(function() {
	form.on('switch(enable)', function(data) {

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/server/setEnable',
			data: {
				enable: data.elem.checked ? 1 : 0,
				id: data.elem.value
			},
			dataType: 'json',
			success: function(data) {

			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	});


	form.on('select(type)', function(data) {
		checkType(data.value, $(data.elem).attr("lang"));
	});
	form.on('select(ssl)', function(data) {
		checkSsl(data.value);
	});
	form.on('select(proxyType)', function(data) {
		checkProxyType(data.value);
	});
	form.on('select(rewrite)', function(data) {
		checkRewrite(data.value);
	});
	form.on('select(denyAllowValue)', function(data) {
		checkDenyAllow(data.value);
	});

	form.on('checkbox(checkAll)', function(data) {
		if (data.elem.checked) {
			$("input[name='ids']").prop("checked", true)
		} else {
			$("input[name='ids']").prop("checked", false)
		}

		form.render();
	});

	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem: '#pemBtn',
			url: '/adminPage/server/upload',
			accept: 'file',
			done: function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#pem").val(res.obj);
					$("#pemPath").html(res.obj);
				}

			},
			error: function() {
				// 请求异常回调
			}
		});

		upload.render({
			elem: '#keyBtn',
			url: '/adminPage/server/upload',
			accept: 'file',
			done: function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#key").val(res.obj);
					$("#keyPath").html(res.obj);
				}
			},
			error: function() {
				// 请求异常回调
			}
		});
	});
})

function checkType(type, id) {
	if (type == 0) {
		$("#" + id + " span[name='valueSpan']").show();
		$("#" + id + " span[name='rootPathSpan']").hide();
		$("#" + id + " span[name='upstreamSelectSpan']").hide();
		$("#" + id + " span[name='blankSpan']").hide();
		$("#" + id + " span[name='headerSpan']").show();
		$("#" + id + " span[name='returnSpan']").hide();
	}
	if (type == 1) {
		$("#" + id + " span[name='valueSpan']").hide();
		$("#" + id + " span[name='rootPathSpan']").show();
		$("#" + id + " span[name='upstreamSelectSpan']").hide();
		$("#" + id + " span[name='blankSpan']").hide();
		$("#" + id + " span[name='headerSpan']").hide();
		$("#" + id + " span[name='returnSpan']").hide();
	}
	if (type == 2) {
		$("#" + id + " span[name='valueSpan']").hide();
		$("#" + id + " span[name='rootPathSpan']").hide();
		$("#" + id + " span[name='upstreamSelectSpan']").show();
		$("#" + id + " span[name='blankSpan']").hide();
		$("#" + id + " span[name='headerSpan']").show();
		$("#" + id + " span[name='returnSpan']").hide();
	}
	if (type == 3) {
		$("#" + id + " span[name='valueSpan']").hide();
		$("#" + id + " span[name='rootPathSpan']").hide();
		$("#" + id + " span[name='upstreamSelectSpan']").hide();
		$("#" + id + " span[name='blankSpan']").show();
		$("#" + id + " span[name='headerSpan']").hide();
		$("#" + id + " span[name='returnSpan']").hide();
	}
	if (type == 4) {
		$("#" + id + " span[name='valueSpan']").hide();
		$("#" + id + " span[name='rootPathSpan']").hide();
		$("#" + id + " span[name='upstreamSelectSpan']").hide();
		$("#" + id + " span[name='blankSpan']").hide();
		$("#" + id + " span[name='headerSpan']").hide();
		$("#" + id + " span[name='returnSpan']").show();
	}
}

function checkSsl(value) {
	if (value == 0) {
		$(".pemDiv").hide();
	}
	if (value == 1) {
		$(".pemDiv").show();
	}
}

function checkProxyType(value) {
	if (value == 0) {
		$(".proxyHttp").show();
		$(".proxyTcp").hide();

	}
	if (value == 1 || value == 2) {
		$(".proxyHttp").hide();
		$(".proxyTcp").show();
	}
}

function checkRewrite(value) {
	if (value == null || value == '' || value == 0) {
		$("#rewriteListenDiv").hide();

	}
	if (value == 1) {
		$("#rewriteListenDiv").show();
	}
}

function checkDenyAllow(value) {
	$("#denyDiv").hide();
	$("#allowDiv").hide();

	if (value == 1) {
		$("#denyDiv").show();
	}
	if (value == 2) {
		$("#allowDiv").show();
	}
	if (value == 3) {
		$("#denyDiv").show();
		$("#allowDiv").show();
	}
}

function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#listen").val("");
	$("#def").prop("checked", false);
	$("#proxyProtocol").prop("checked", false);
	$("#ip").val("");
	$("#serverName").val("");
	$("#ssl option:first").prop("selected", true);
	$("#rewrite option:first").prop("selected", true);
	$("#http2 option:first").prop("selected", true);
	$("#proxyType option:first").prop("selected", true);
	$("#proxyUpstreamId option:first").prop("selected", true);
	$("#passwordId option:first").prop("selected", true);

	$("#rewriteListen").val("80");

	$("#pem").val("");
	$("#pemPath").html("");
	$("#key").val("");
	$("#keyPath").html("");
	$("#itemList").html("");
	$("#paramJson").val("");

	$("#denyAllow").val("0");
	$("#denyId option:first").prop("selected", true);
	$("#allowId option:first").prop("selected", true);

	$(".protocols").prop("checked", true);

	checkProxyType(0);
	checkSsl(0);
	checkRewrite(1);

	form.render();
	showWindow(serverStr.add);
}

function showWindow(title) {

	var width = "1350px";
	var height = "90%";
	if (window.innerWidth <= 1000) {
		// 手机端
		width = "1000px";
		height = "1500px";
	}

	layer.open({
		type: 1,
		title: title,
		area: [width, height], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#listen").val().trim() == '') {
		layer.msg(serverStr.noPort);
		return;
	}

	var over = true;
	$("input[name='path']").each(function() {
		if ($(this).val().trim() == '') {
			over = false;
		}
	})
	$("input[name='value']").each(function() {
		if (!$(this).is(":hidden") && $(this).val().trim() == '') {
			over = false;
		}
	})
	$("input[name='rootPath']").each(function() {
		if (!$(this).is(":hidden") && $(this).val().trim() == '') {
			over = false;
		}
	})
	$("select[name='upstreamId']").each(function() {
		if (!$(this).parent().is(":hidden") && ($(this).val() == '' || $(this).val() == null)) {
			over = false;
		}
	})
	$("select[name='proxyUpstreamId']").each(function() {
		if ($("#proxyType").val() == 1 && ($(this).val() == '' || $(this).val() == null)) {
			over = false;
		}
	})
	if (!over) {
		layer.msg(serverStr.noFill);
		return;
	}


	var server = {};
	server.id = $("#id").val();
	server.proxyType = $("#proxyType").val();
	server.proxyUpstreamId = $("#proxyUpstreamId").val();
	server.listen = $("#listen").val();
	if ($("#ip").val() != '') {
		var ip = $("#ip").val();
		if (ip.indexOf(":") > -1) {
			ip = `[${ip}]`;
		}
		server.listen = ip + ":" + $("#listen").val();
	}


	server.proxyProtocol = $("#proxyProtocol").prop("checked") ? "1" : "0";
	server.def = $("#def").prop("checked") ? "1" : "0";
	server.serverName = $("#serverName").val();
	server.ssl = $("#ssl").val();
	server.pem = $("#pem").val();
	server.key = $("#key").val();
	server.rewrite = $("#rewrite").val();
	if (server.rewrite == 1) {
		server.rewriteListen = $("#rewriteListen").val();
		if ($("#rewriteIp").val() != '') {
			var ip = $("#rewriteIp").val();
			if (ip.indexOf(":") > -1) {
				ip = `[${ip}]`;
			}
			server.rewriteListen = ip + ":" + $("#rewriteListen").val();
		}
	}
	server.http2 = $("#http2").val();
	server.passwordId = $("#passwordId").val();

	var protocols = [];
	$(".protocols").each(function() {
		if ($(this).prop("checked")) {
			protocols.push($(this).val());
		}
	});
	server.protocols = protocols.join(" ");

	server.denyAllow = $("#denyAllow").val();
	server.denyId = $("#denyId").val();
	server.allowId = $("#allowId").val();

	var serverParamJson = $("#serverParamJson").val();

	var locations = [];

	$(".itemList").children().each(function() {
		var location = {};
		location.path = $(this).find("input[name='path']").val();
		location.type = $(this).find("select[name='type']").val();
		location.value = $(this).find("input[name='value']").val();
		location.upstreamType = $(this).find("select[name='upstreamType']").val();
		location.upstreamId = $(this).find("select[name='upstreamId']").val();
		location.upstreamPath = $(this).find("input[name='upstreamPath']").val();
		location.rootPath = $(this).find("input[name='rootPath']").val();
		location.rootPage = $(this).find("input[name='rootPage']").val();
		location.rootType = $(this).find("select[name='rootType']").val();
		location.locationParamJson = $(this).find("textarea[name='locationParamJson']").val();
		location.header = $(this).find("input[name='header']").prop("checked") ? 1 : 0;
		location.websocket = $(this).find("input[name='websocket']").prop("checked") ? 1 : 0;
		location.cros = $(this).find("input[name='cros']").prop("checked") ? 1 : 0;
		location.headerHost = $(this).find("select[name='headerHost']").val();
		location.returnUrl = $(this).find("input[name='returnUrl']").val();
		
		locations.push(location);
	})

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/server/addOver',
		data: {
			serverJson: JSON.stringify(server),
			serverParamJson: serverParamJson,
			locationJson: JSON.stringify(locations),
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

function edit(id, clone) {
	$("#id").val(id);

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/server/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {

				var server = data.obj.server;
				if (!clone) {
					$("#id").val(server.id);
				} else {
					$("#id").val("");
				}

				if (server.listen != null && server.listen.indexOf(":") > -1) {
					var listens = server.listen.split(":");

					$("#ip").val(server.listen.replace(":" + listens[listens.length - 1], "").replace("[", "").replace("]", ""));
					$("#listen").val(listens[listens.length - 1]);
				} else {
					$("#ip").val("");
					$("#listen").val(server.listen);
				}

				if (server.rewriteListen != null && server.rewriteListen.indexOf(":") > -1) {
					var listens = server.rewriteListen.split(":");

					$("#rewriteIp").val(server.rewriteListen.replace(":" + listens[listens.length - 1], "").replace("[", "").replace("]", ""));
					$("#rewriteListen").val(listens[listens.length - 1]);
				} else {
					$("#rewriteIp").val("");
					$("#rewriteListen").val(server.rewriteListen);
				}

				$("#def").prop("checked", server.def == 1);
				$("#proxyProtocol").prop("checked", server.proxyProtocol == 1);
				$("#serverName").val(server.serverName);
				$("#ssl").val(server.ssl);
				$("#pem").val(server.pem);
				$("#key").val(server.key);
				$("#pemPath").html(server.pem);
				$("#keyPath").html(server.key);
				$("#proxyType").val(server.proxyType);
				$("#proxyType").val(server.proxyType);
				$("#proxyUpstreamId").val(server.proxyUpstreamId);
				$("#serverParamJson").val(data.obj.paramJson);
				$("#denyAllow").val(server.denyAllow);
				$("#denyId").val(server.denyId);
				$("#allowId").val(server.allowId);
				$("#passwordId").val(server.passwordId);


				if (server.rewrite != null) {
					$("#rewrite").val(server.rewrite);
				} else {
					$("#rewrite option:first").prop("selected", true);
				}

				if (server.http2 != null) {
					$("#http2").val(server.http2);
				} else {
					$("#http2 option:first").prop("selected", true);
				}

				$(".protocols").prop("checked", false);
				if (server.protocols != null) {
					var protocols = server.protocols.split(" ");

					if (protocols.indexOf("TLSv1") > -1) {
						$("#TLSv1").prop("checked", true);
					}
					if (protocols.indexOf("TLSv1.1") > -1) {
						$("#TLSv1_1").prop("checked", true);
					}
					if (protocols.indexOf("TLSv1.2") > -1) {
						$("#TLSv1_2").prop("checked", true);
					}
					if (protocols.indexOf("TLSv1.3") > -1) {
						$("#TLSv1_3").prop("checked", true);
					}
				}
				form.render();


				checkProxyType(server.proxyType);
				checkSsl(server.ssl);
				checkRewrite(server.rewrite);

				var list = data.obj.locationList;

				var upstreamSelect = $("#upstreamSelect").html();
				$("#itemList").html("");
				for (let i = 0; i < list.length; i++) {
					var location = list[i];
					var uuid = guid();

					location.locationParamJson = location.locationParamJson;
					var html = buildHtml(uuid, location, upstreamSelect);

					$("#itemList").append(html);

					$("#" + uuid + " input[name='value']").val(location.value);
					$("#" + uuid + " input[name='rootType']").val(location.rootType);
					$("#" + uuid + " input[name='rootPath']").val(location.rootPath);
					$("#" + uuid + " input[name='rootPage']").val(location.rootPage);
					$("#" + uuid + " select[name='rootType']").val(location.rootType);
					$("#" + uuid + " select[name='upstreamType']").val(location.upstreamType);
					$("#" + uuid + " select[name='upstreamId']").val(location.upstreamId);
					$("#" + uuid + " input[name='upstreamPath']").val(location.upstreamPath);
					$("#" + uuid + " select[name='headerHost']").val(location.headerHost);
					$("#" + uuid + " input[name='returnUrl']").val(location.returnUrl);
					
					if (location.header == 1) {
						$("#" + uuid + " input[name='header']").prop("checked", true);
					} else {
						$("#" + uuid + " input[name='header']").prop("checked", false);
					}

					if (location.websocket == 1) {
						$("#" + uuid + " input[name='websocket']").prop("checked", true);
					} else {
						$("#" + uuid + " input[name='websocket']").prop("checked", false);
					}

					if (location.cros == 1) {
						$("#" + uuid + " input[name='cros']").prop("checked", true);
					} else {
						$("#" + uuid + " input[name='cros']").prop("checked", false);
					}

					checkType(location.type, uuid)
				}

				form.render();
				showWindow(serverStr.edit);
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
	if (confirm(commonStr.del)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/server/del',
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
			url: ctx + '/adminPage/server/del',
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

	var upstreamSelect = $("#upstreamSelect").html();

	var html = buildHtml(uuid, null, upstreamSelect);

	$("#itemList").append(html);
	checkType(0, uuid);
	form.render();

}

function buildHtml(uuid, location, upstreamSelect) {
	if (location == null) {
		location = {
			path: "/",
			type: "0",
			locationParamJson: ""
		};
	}
	//将双引号转义
	location.path = location.path.replace(/\"/g, "&quot;");

	var str = `<tr id='${uuid}'>
				<td>
					<div class="layui-inline" >
						<input type="text" name="path" class="layui-input short" value="${location.path}">
					</div>
				</td>
				<td>
					<div class="layui-inline" style="width: 130px;">
						<select name="type" lang='${uuid}' lay-filter="type">
							<option ${location.type == '0' ? 'selected' : ''} value="0">${serverStr.serverType0}</option>
							<option ${location.type == '1' ? 'selected' : ''} value="1">${serverStr.serverType1}</option>
							<option ${location.type == '2' ? 'selected' : ''} value="2">${serverStr.serverType2}</option>
							<option ${location.type == '4' ? 'selected' : ''} value="4">${serverStr.serverType4}</option>
							<option ${location.type == '3' ? 'selected' : ''} value="3">${serverStr.serverType3}</option>
						</select>
					</div>
				</td>
				
				<td style="width: 740px;">
					<span name="valueSpan">
						<div class="layui-inline">
							<input type="text"  style="width: 277px;" name="value" id="value_${uuid}" class="layui-input long" value=""  placeholder="${serverStr.example}：http://127.0.0.1:8080">
						</div>
					</span>
					
					<span name="rootPathSpan">
						<div class="layui-inline" style="width: 124px;"> 
							<select name="rootType" >
								<option value="root">${serverStr.rootModel}</option>
								<option value="alias">${serverStr.aliasModel}</option>
							</select>
						</div>
						
						<div class="layui-inline" style="width: 150px;">
							<input type="text" name="rootPath" id="rootPath_${uuid}" class="layui-input" placeholder="${serverStr.example}：/root/www">
						</div>
						
						<div class="layui-inline">
							<i class="layui-icon layui-icon-export" lang="value" onclick="selectWww('${uuid}')"></i> 
						</div>
						
						<div class="layui-inline" style="width: 120px;">
							<input type="text" name="rootPage" id="rootPage_${uuid}" class="layui-input" placeholder="${serverStr.defaultPage}">
						</div>	
					</span>
					
					<span name="upstreamSelectSpan">
						${upstreamSelect}
					</span>
					
					<span name="blankSpan">
					
					</span>
					
					<span name="headerSpan" style="padding-left:7px;">
						<div class="layui-inline">
							<input type="checkbox" name="websocket" title="${serverStr.websocket}" lay-skin="primary"> 
						</div>
						<div class="layui-inline">
							<input type="checkbox" name="cros" title="${serverStr.cros}" lay-skin="primary"> 
						</div>
						<div class="layui-inline">
							<input type="checkbox" name="header" title="${serverStr.headerAddHost} :" lay-skin="primary" checked> 
						</div>
						<div class="layui-inline" style="width: 110px;">
							<select name="headerHost" lay-filter="type">
								<option ${location.headerHost == '$host' ? 'selected' : ''}>$host</option>
								<option ${location.headerHost == '$http_host' ? 'selected' : ''}>$http_host</option>
								<option ${location.headerHost == '$host:$proxy_port' ? 'selected' : ''}>$host:$proxy_port</option>
								<option ${location.headerHost == '$host:$server_port' ? 'selected' : ''}>$host:$server_port</option>
								
							</select>
						</div>
					</span>
					
					<span name="returnSpan">
						<div class="layui-inline">
							<input type="text"  style="width: 277px;" name="returnUrl" id="returnUrl_${uuid}" class="layui-input long" value=""  placeholder="${serverStr.example}：https://www.baidu.com">
						</div>
					</span>
				</td> 
				<td>
					<textarea style="display: none;" id="locationParamJson_${uuid}" name="locationParamJson" >${location.locationParamJson}</textarea>
					<div class="layui-inline">
						<button type="button" class="layui-btn layui-btn-sm" onclick="locationParam('${uuid}')">${serverStr.extParm}</button>
					</div>
					<div class="layui-inline">
						<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>
					</div>
					<div class="layui-inline">
						<button type="button" class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
					</div>
					<div class="layui-inline">
						<button type="button" class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
					</div>
				</td>
			</tr>`

	return str;
}


function delTr(id) {
	$("#" + id).remove();
}

var certIndex;
function selectCert() {
	certIndex = layer.open({
		type: 1,
		title: serverStr.selectCert,
		area: ['500px', '410px'], // 宽高
		content: $('#certDiv')
	});

}

function selectCertOver() {
	var id = $("#certId").val();

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/cert/detail',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var cert = data.obj;
				$("#pem").val(cert.pem);
				$("#pemPath").html(cert.pem);
				$("#key").val(cert.key);
				$("#keyPath").html(cert.key);

				layer.close(certIndex);
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}



function selectPem() {
	rootSelect.selectOne(function(rs) {
		$("#pem").val(rs);
		$("#pemPath").html(rs);
	})
}


function selectKey() {
	rootSelect.selectOne(function(rs) {
		$("#key").val(rs);
		$("#keyPath").html(rs);
	})
}


function serverParam() {
	var json = $("#serverParamJson").val();
	$("#targertId").val("serverParamJson");
	var params = json != '' ? JSON.parse(json) : [];
	fillTable(params);

}


function locationParam(uuid) {
	var json = $("#locationParamJson_" + uuid).val();
	$("#targertId").val("locationParamJson_" + uuid);
	var params = json != '' ? JSON.parse(json) : [];
	fillTable(params);
}

var denyAllowIndex;
function setDenyAllow() {
	var denyAllow = $("#denyAllow").val();
	var denyId = $("#denyId").val();
	var allowId = $("#allowId").val();

	$("#denyAllowValue").val(denyAllow);
	if (denyId != null) {
		$("#denyIdValue").val(denyId);
	}
	if (allowId != null) {
		$("#allowIdValue").val(allowId);
	}
	checkDenyAllow(denyAllow);

	form.render();
	denyAllowIndex = layer.open({
		type: 1,
		title: serverStr.denyAllowModel,
		area: ['600px', '500px'], // 宽高
		content: $('#denyAllowDiv')
	});
}

function setDenyAllowOver() {
	var denyAllow = $("#denyAllowValue").val();
	var denyId = $("#denyIdValue").val();
	var allowId = $("#allowIdValue").val();

	$("#denyAllow").val(denyAllow);
	$("#denyId").val(denyId);
	$("#allowId").val(allowId);

	layer.close(denyAllowIndex)
}

var paramIndex;
function fillTable(params) {
	var html = "";
	for (var i = 0; i < params.length; i++) {
		var param = params[i];

		var uuid = guid();
		if (param.templateValue == null) {
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
			html += buildTemplateParam(uuid, param);
		}
	}

	$("#paramList").html(html);

	paramIndex = layer.open({
		type: 1,
		title: serverStr.extParm,
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
		<td style="width: 50%;">
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


/*function sort(id) {
	$("#sort").val(id.replace("Sort", ""))
	if ($("#" + id).attr("class").indexOf("blue") > -1) {
		if ($("#direction").val() == 'asc') {
			$("#direction").val("desc")
		} else {
			$("#direction").val("asc")
		}
	} else {
		$("#direction").val("asc")
	}

	search();
}*/


var wwwIndex;
var uuid;
function selectWww(id) {
	uuid = id;
	rootSelect.selectOne(function callBack(val) {
		$("#rootPath_" + uuid).val(val);
	});
}


function clone(id) {
	if (confirm(serverStr.confirmClone)) {
		edit(id, true);
	}
}


function importServer() {
	var formData = new FormData();
	formData.append("nginxPath", $("#nginxPath").val());
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/server/importServer',
		data: formData,
		dataType: 'json',
		processData: false,
		contentType: false,
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

var importIndex;
function openImport() {
	importIndex = layer.open({
		type: 1,
		title: serverStr.importServer,
		area: ['500px', '300px'], // 宽高
		content: $('#importDiv')
	});
}
// 选择系统文件
function selectRootCustom(inputId) {
	rootSelect.selectOne(function callBack(val) {
		$("#" + inputId).val(val);
	});
}

function testPort() {
	if (confirm(serverStr.testAllPort)) {
		layer.load();
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/server/testPort',
			dataType: 'json',
			processData: false,
			contentType: false,
			success: function(data) {
				layer.closeAll();
				if (data.success) {
					layer.msg(serverStr.noPortUsed);
				} else {
					layer.alert(data.msg);
				}
			},
			error: function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}

}

function editDescr(id) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/server/getDescr',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#serverId").val(id);
				$("#descr").val(data.obj != null ? data.obj : '');

				layer.open({
					type: 1,
					title: serverStr.descr,
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
		url: ctx + '/adminPage/server/editDescr',
		data: {
			id: $("#serverId").val(),
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


function setOrder(id, count) {
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/server/setOrder',
		data: {
			id: id,
			count: count
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
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


function editLocationDescr(id) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/server/getLocationDescr',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				/*$("#serverId").val(id);
				$("#descr").val(data.obj!=null?data.obj:'');
				
				layer.open({
					type: 1,
					title: serverStr.descr,
					area: ['500px', '360px'], // 宽高
					content: $('#descrDiv')
				});*/

				layer.prompt({
					formType: 0,
					value: data.obj,
					title: commonStr.descr,
					area: ['400px', '350px'] //自定义文本域宽高
				}, function(value, index, elem) {
					//alert(value); //得到value
					layer.close(index);

					$.ajax({
						type: 'POST',
						url: ctx + '/adminPage/server/setLocationDescr',
						data: {
							id: id,
							descr: value
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
							closeLoad();
							layer.alert(commonStr.errorInfo);
						}
					});
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


