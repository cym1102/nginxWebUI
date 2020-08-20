function add() {
	$("#id").val("");
	$("#name").val("");
	$("#paramList").html("");

	form.render();
	showWindow(templateStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['800px', '600px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg(templateStr.noname);
		return;
	}

	var templateParams = [];
	$("#paramList").children().each(function() {

		var templateParam = {};
		templateParam.name = $(this).find("textarea[name='name']").val();
		templateParam.value = $(this).find("textarea[name='value']").val();

		templateParams.push(templateParam);
	})


	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/template/addOver',
		data: {
			id: $("#id").val(),
			name : $("#name").val(),
			paramJson: JSON.stringify(templateParams),
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

function edit(id) {

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/template/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var ext = data.obj;
				var list = ext.paramList;

				$("#id").val(ext.template.id);
				$("#name").val(ext.template.name);

				var html = ``;
				for (let i = 0; i < list.length; i++) {
					var param = list[i];
					var uuid = guid();
					html += `<tr name="param" id=${uuid}>
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
							</tr>`
				}
				$("#paramList").html(html);


				form.render();
				showWindow(templateStr.edit);
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
			url: ctx + '/adminPage/template/del',
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

function addParam() {
	var uuid = guid();
	var html = `<tr name="param" id=${uuid}>
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
			</tr>`
	$("#paramList").append(html);
}

function delTr(id){
	$("#" + id).remove();
}