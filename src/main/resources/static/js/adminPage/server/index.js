$(function() {
	form.on('switch(enable)', function(data){
		  
		  $.ajax({
				type : 'POST',
				url : ctx + '/adminPage/server/setEnable',
				data : {
					enable : data.elem.checked?1:0,
					id : data.elem.value
				},
				dataType : 'json',
				success : function(data) {			
					
				},
				error : function() {
					alert("出错了,请联系技术人员!");
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
	
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem : '#pemBtn',
			url : '/upload/',
			accept : 'file',
			done : function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#pem").val(res.obj);
					$("#pemPath").html(res.obj);
				}

			},
			error : function() {
				// 请求异常回调
			}
		});

		upload.render({
			elem : '#keyBtn',
			url : '/upload/',
			accept : 'file',
			done : function(res) {
				// 上传完毕回调
				if (res.success) {
					$("#key").val(res.obj);
					$("#keyPath").html(res.obj);
				}
			},
			error : function() {
				// 请求异常回调
			}
		});
	});
})

function checkType(type,id){
	if (type == 0 || type == 1) {
		$("#" + id + " input[lang='value']").show();
		$("#" + id + " span[name='upstreamSelect']").hide();
	} 
	if (type == 2) {
		$("#" + id + " input[lang='value']").hide();
		$("#" + id + " span[name='upstreamSelect']").show();
	} 
}

function checkSsl(value){
	if (value == 0) {
		$(".pemDiv").hide();
	} 
	if (value == 1) {
		$(".pemDiv").show();
	} 
}

function checkProxyType(value){
	if (value == 0) {
		$(".proxyHttp").show();
		$(".proxyTcp").hide();
		
	} 
	if (value == 1) {
		$(".proxyHttp").hide();
		$(".proxyTcp").show();
	} 
	
}

function search() {
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#listen").val("");
	$("#serverName").val("");
	$("#ssl option:first").prop("selected", true);
	$("#rewrite option:first").prop("selected", true);
	$("#http2 option:first").prop("selected", true);
	$("#proxyType option:first").prop("selected", true);
	$("#proxyUpstreamId option:first").prop("selected", true);
	
	$("#pem").val("");
	$("#pemPath").html("");
	$("#key").val("");
	$("#keyPath").html("");
	$("#itemList").html("");
	$("#paramJson").val("");
	
	checkSsl(0);
	checkProxyType(0);
	
	form.render();
	showWindow("添加反向代理");
}

function showWindow(title) {
	layer.open({
		type : 1,
		title : title,
		area : [ '1200px', '700px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#listen").val().trim() == ''){
		layer.msg("端口未填写");
		return;
	}
	
	if($("#ssl").val() == 1 && $("#serverName").val() == ''){
		layer.msg("开启ssl必须填写域名");
		return;
	}
	
	var over = true;
	$("input[name='path']").each(function(){
		if($(this).val().trim() == ''){
			over = false;
		}
	})
	$("input[name='value']").each(function(){
		if(!$(this).is(":hidden") && $(this).val().trim() == ''){
			over = false;
		}
	})
	$("select[name='upstreamId']").each(function(){
		if(!$(this).parent().is(":hidden") && ($(this).val() == '' || $(this).val() == null)){
			over = false;
		}
	})
	$("select[name='proxyUpstreamId']").each(function(){
		if($("#proxyType").val() == 1 &&  ($(this).val() == '' || $(this).val() == null)){
			over = false;
		}
	})
	if(!over){
		layer.msg("填写不完整");
		return;
	}
	
	$("input[name='upstreamPath']").each(function(){
		if($(this).val() == ''){
			$(this).val("is_null");
		}
	})
	
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/server/addOver',
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

function edit(id) {
	$("#id").val(id);

	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/server/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				
				var server = data.obj.server;
				$("#id").val(server.id);
				$("#listen").val(server.listen);
				$("#serverName").val(server.serverName);
				$("#ssl").val(server.ssl);
				$("#pem").val(server.pem);
				$("#key").val(server.key);
				$("#pemPath").html(server.pem);
				$("#keyPath").html(server.key);
				$("#proxyType").val(server.proxyType);
				$("#proxyUpstreamId").val(server.proxyUpstreamId);
				$("#serverParamJson").val(data.obj.paramJson.replace(/,/g,"%2C"));
				
				if(server.rewrite != null){
					$("#rewrite").val(server.rewrite);
				} else{
					$("#rewrite option:first").prop("selected", true);
				}
				
				if(server.http2 != null){
					$("#http2").val(server.http2);
				} else{
					$("#http2 option:first").prop("selected", true);
				}
				
				checkSsl(server.ssl);
				checkProxyType(server.proxyType);
				var list = data.obj.locationList;
				
				var upstreamSelect = $("#upstreamSelect").html();
				$("#itemList").html("");
				for(let i=0;i<list.length;i++){
					var location = list[i];
					var uuid = guid();
					
					location.locationParamJson = location.locationParamJson.replace(/,/g,"%2C");
					var html = `<tr id='${uuid}'>
								<td>
									<input type="text" name="path" class="layui-input short" value="${location.path}">
								</td>
								<td style="width: 200px;">
									<div class="layui-input-inline">
									<select name="type" lang='${uuid}' lay-filter="type">
										<option ${location.type=='0'?'selected':''} value="0">代理动态http</option>
										<option ${location.type=='1'?'selected':''} value="1">代理静态html</option>
										<option ${location.type=='2'?'selected':''} value="2">负载均衡</option>
									</select>
									</div>
								</td>
								
								<td>
									<input type="text" lang="value" name="value" id="value_${uuid}" class="layui-input long" value=""  placeholder="例：http://127.0.0.1:8080 或 /root/www">
									<i class="layui-icon layui-icon-export" lang="value" onclick="selectWww('${uuid}')"></i>  
									
									<span name="upstreamSelect">
									${upstreamSelect}
									</span>
								</td> 
								<td>
									<input type="hidden" id="locationParamJson_${uuid}" name="locationParamJson" value='${location.locationParamJson}'>
									<button type="button" class="layui-btn layui-btn-sm" onclick="locationParam('${uuid}')">设置额外参数</button>
									<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
								</td>
						</tr>`
						
					$("#itemList").append(html);
					
					if(location.type == 0 || location.type == 1){
						$("#" + uuid + " input[name='value']").val(location.value);
					} else {
						$("#" + uuid + " select[name='upstreamId']").val(location.upstreamId);
						$("#" + uuid + " input[name='upstreamPath']").val(location.upstreamPath);
					}
					
					checkType(location.type, uuid)
				}
				
				form.render();
				showWindow("编辑反向代理");
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function del(id) {
	if (confirm("确认删除?")) {
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/server/del',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg)
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}



function addItem(){
	var uuid = guid();
	
	var upstreamSelect = $("#upstreamSelect").html();
	
	var html = `<tr id='${uuid}'>
						<td>
							<input type="text" name="path" class="layui-input short" value="/">
						</td>
						<td style="width: 200px;">
							<div class="layui-input-inline">
							<select name="type" lang='${uuid}' lay-filter="type">
								<option value="0">代理动态http</option>
								<option value="1">代理静态html</option>
								<option value="2">负载均衡</option>
							</select>
							</div>
						</td>
						
						<td>
							<input type="text" lang="value" name="value" id="value_${uuid}" class="layui-input long" value=""  placeholder="例：http://127.0.0.1:8080 或 /root/www">
							<i class="layui-icon layui-icon-export" lang="value" onclick="selectWww('${uuid}')"></i>  
							
							<span name="upstreamSelect">
								${upstreamSelect}
							</span>
						</td> 
						<td>
							<input type="hidden" id="locationParamJson_${uuid}" name="locationParamJson"  value="">
							<button type="button" class="layui-btn layui-btn-sm" onclick="locationParam('${uuid}')">设置额外参数</button>
							<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
						</td>
				</tr>`
	$("#itemList").append(html);
	checkType(0, uuid);
	form.render();
	
}


function delTr(id){
	$("#" + id).remove();
}

var certIndex;
function selectCert(){
	certIndex = layer.open({
		type : 1,
		title : "选择内置证书",
		area : [ '500px', '300px' ], // 宽高
		content : $('#certDiv')
	});
	
}

function selectCertOver(){
	var id = $("#certId").val();
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/cert/detail',
		data : {
			id : id
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var cert = data.obj;
				$("#pem").val(cert.pem);
				$("#pemPath").html(cert.pem);
				$("#key").val(cert.key);
				$("#keyPath").html(cert.pem);
				
				layer.close(certIndex);
			} else {
				layer.msg(data.msg)
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}



function selectPem(){
	rootSelect.selectOne(function(rs){
		$("#pem").val(rs);
		$("#pemPath").html(rs);
	})
}


function selectKey(){
	rootSelect.selectOne(function(rs){
		$("#key").val(rs);
		$("#keyPath").html(rs);
	})
}


function serverParam(){
	var json = $("#serverParamJson").val();
	$("#targertId").val("serverParamJson");
	var params = json!=''?JSON.parse(json.replace(/%2C/g,",")):[];
	fillTable(params);
	
}

function locationParam(uuid){
	var json = $("#locationParamJson_" + uuid).val();
	$("#targertId").val("locationParamJson_" + uuid);
	var params = json!=''?JSON.parse(json.replace(/%2C/g,",")):[];
	fillTable(params);
}

var paramIndex;
function fillTable(params){
	var html = "";
	for(var i=0;i<params.length;i++){
		var param = params[i];
		
		var uuid = guid();
		
		html += `
		<tr name="param" id=${uuid}>
			<td>
				<textarea  name="name" class="layui-textarea">${param.name}</textarea>
			</td>
			<td  style="width: 60%;">
				<textarea  name="value" class="layui-textarea">${param.value}</textarea>
			</td>
			<td>
				<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
			</td>
		</tr>
		`;
	}
	
	$("#paramList").html(html);
	
	paramIndex = layer.open({
		type : 1,
		title : "添加参数",
		area : [ '800px', '600px' ], // 宽高
		content : $('#paramJsonDiv')
	});
}

function addParam(){
	var uuid = guid();
	
	var html = `
	<tr name="param" id="${uuid}">
		<td>
			<textarea  name="name" class="layui-textarea"></textarea>
		</td>
		<td  style="width: 60%;">
			<textarea  name="value" class="layui-textarea"></textarea>
		</td>
		<td>
			<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
		</td>
	</tr>
	`;
	
	$("#paramList").append(html);
	
}


function addParamOver(){
	var targertId = $("#targertId").val();
	var params = [];
	$("tr[name='param']").each(function(){
		var param = {};
		param.name = $(this).find("textarea[name='name']").val();
		param.value = $(this).find("textarea[name='value']").val();
		
		params.push(param);
	})
	$("#" + targertId).val(JSON.stringify(params).replace(/,/g,"%2C"));
	
	layer.close(paramIndex);
}


function sort(id){
	$("#sort").val(id.replace("Sort",""))
	if($("#"+id).attr("class").indexOf("blue") > -1){
		if($("#direction").val()=='asc'){
			$("#direction").val("desc")
		}else{
			$("#direction").val("asc")
		}
	}else{
		$("#direction").val("asc")
	}
	
	search();
}


var wwwIndex;
var uuid;
function selectWww(id){
	uuid = id;
//	wwwIndex = layer.open({
//		type : 1,
//		title : "选择静态网页",
//		area : [ '500px', '300px' ], // 宽高
//		content : $('#wwwDiv')
//	});
	
	rootSelect.selectOne(function callBack(val){
		$("#value_" + uuid).val(val);
	});
	
}

//function selectWwwOver(){
//	var dir = $("#wwwId").val();
//	$("#value_" + uuid).val(dir);
//	layer.close(wwwIndex)
//}


function clone(id){
	if(confirm("确认进行克隆?")){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/server/clone',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg)
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
	
}