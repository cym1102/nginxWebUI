$(function(){
//	form.on('select(proxyType)', function(data) {
//		checkProxyType(data.value);
//	});
})


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}


//function checkProxyType(value){
//	if (value == 0) {
//		$(".proxyHttp").show();
//	} 
//	if (value == 1) {
//		$(".proxyHttp").hide();
//	} 
//	
//}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#tactics option:first").prop("checked",true); 
	$("#itemList").html("");
	$("#proxyType option:first").prop("selected", true);
	
//	checkProxyType(0);
	form.render();
	showWindow("添加负载均衡");
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '1100px', '600px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg("名称为空");
		return;
	}
	
	var over = true;
	$("input[name='server']").each(function(){
		if($(this).val().trim() == ''){
			over = false;
		}
	})
	$("input[name='port']").each(function(){
		if($(this).val().trim() == ''){
			over = false;
		}
	})
	$("input[name='weight']").each(function(){
		if($(this).val().trim() == ''){
			over = false;
		}
	})
	
	if(!over){
		layer.msg("填写不完整");
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/upstream/addOver',
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
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/upstream/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var ext = data.obj;
				var list = ext.upstreamServerList;
				
				$("#id").val(ext.upstream.id);
				$("#name").val(ext.upstream.name);
				$("#tactics").val(ext.upstream.tactics);
				$("#proxyType").val(ext.upstream.proxyType);
				
				
				var html = ``;
				for(let i=0;i<list.length;i++){
					var upstreamServer = list[i];
					var uuid = guid();
					html += `<tr id='${uuid}'>
									<td><input type="text" name="server" class="layui-input" value="${upstreamServer.server}"></td>
									<td><input type="number" name="port" class="layui-input" value="${upstreamServer.port}"></td>
									<td><input type="number" name="weight" class="layui-input" value="${upstreamServer.weight}"></td>
									<td><input type="number" name="maxFails" class="layui-input" value="${upstreamServer.maxFails}"></td>
									<td><input type="number" name="failTimeout" class="layui-input" value="${upstreamServer.failTimeout}"></td>
									<td>
										<select name="status">
											<option ${upstreamServer.status=='none'?'selected':''} value="none">无</option>
											<option ${upstreamServer.status=='down'?'selected':''} value="down">停用(down)</option>
											<option ${upstreamServer.status=='backup'?'selected':''} value="backup">备用(backup)</option>
										</select>
									</td>
									<td><button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button></td>
							</tr>`
				}
				$("#itemList").html(html);
				
//				checkProxyType(ext.upstream.proxyType);
				
				form.render();
				showWindow("编辑负载均衡");
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
			url : ctx + '/adminPage/upstream/del',
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

function addItem(){
	var uuid = guid();
	var html = `<tr id='${uuid}'>
						<td><input type="text" name="server" class="layui-input" value=""></td>
						<td><input type="number" name="port" class="layui-input" value=""></td>
						<td><input type="number" name="weight" class="layui-input" value="1"></td>
						<td><input type="number" name="maxFails" class="layui-input" value="1"></td>
						<td><input type="number" name="failTimeout" class="layui-input" value="10"></td>
						<td>
							<select name="status">
								<option value="none">无</option>
								<option value="down">停用(down)</option>
								<option value="backup">备用(backup)</option>
							</select>
						</td>
						<td><button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button></td>
				</tr>`
	$("#itemList").append(html);
	
//	checkProxyType($("#proxyType").val());
	
	form.render();
}


function delTr(id){
	$("#" + id).remove();
}