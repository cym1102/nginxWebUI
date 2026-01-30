$(function() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/template/getTemplate',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var list = data.obj;

				if (list.length == 0) {
					$("#selectTemplateId").append(`<option value="">${commonStr.pleaseSelect}</option>`);
				} else {
					for (var i = 0; i < list.length; i++) {
						var template = list[i];
						var html = `<option value="${template.id}">${template.name}</option>`;
						$("#selectTemplateId").append(html);
					}
				}

				form.render();

			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
})


var selectTemplateTagertId;
var templateIndex;
var templateType;
var isHttp = false;
var isStream = false;

function selectTemplate(id) {
	selectTemplateTagertId = id;
	templateType = "temp";
	templateIndex = layer.open({
		type: 1,
		title: templateStr.select,
		area: ['450px', '350px'], // 宽高
		content: $('#templateSelectDiv')
	});
}

function selectTemplateOver() {
	var templateId = $("#selectTemplateId").val();
	if (templateId == null) {
		layer.msg(templateStr.noSelect);
		return;
	}


	if (isHttp) {
		addHttpParam(templateId);
		return;
	}
	if (isStream) {
		addStreamParam(templateId);
		return;
	}

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/template/detail',
		dataType: 'json',
		data: {
			id: templateId
		},
		success: function(data) {
			if (data.success) {
				var ext = data.obj;

				// Detect if position column is needed (check if the table has position header)
				var includePosition = $("#" + selectTemplateTagertId).closest('table').find('th').length >= 4;

				if (templateType == 'temp') {
					var uuid = guid();
					var positionTd = '';
					if (includePosition) {
						positionTd = `
							<td style="width: 100px;">
								<select name="position" class="layui-input" style="height: 30px;">
									<option value="0" selected>${serverStr.paramAppend}</option>
									<option value="1">${serverStr.paramPrepend}</option>
								</select>
							</td>`;
					}
					var html = `
						<tr name="param" id="${uuid}">
							<td>
								${templateStr.template}
							</td>
							<td  style="width: ${includePosition ? '40%' : '50%'};">
								${ext.template.name}
								<input type="hidden" name="templateValue" value="${ext.template.id}">
								<input type="hidden" name="templateName" value="${ext.template.name}">
							</td>
							${positionTd}
							<td>
								<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>

								<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
								<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
							</td>
						</tr>
					`;


				} else {
					var html = "";
					for (var i = 0; i < data.obj.paramList.length; i++) {
						var param = data.obj.paramList[i];
						var uuid = guid();

						var positionTd = '';
						if (includePosition) {
							positionTd = `
							<td style="width: 100px;">
								<select name="position" class="layui-input" style="height: 30px;">
									<option value="0" selected>${serverStr.paramAppend}</option>
									<option value="1">${serverStr.paramPrepend}</option>
								</select>
							</td>`;
						}

						html += `
						<tr name="param" id=${uuid}>
							<td>
								<textarea  name="name" class="layui-textarea">${param.name}</textarea>
							</td>
							<td  style="width: ${includePosition ? '40%' : '50%'};">
								<textarea  name="value" class="layui-textarea">${param.value}</textarea>
							</td>
							${positionTd}
							<td>
								<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>

								<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
								<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
							</td>
						</tr>
						`;
					}
				}

				$("#" + selectTemplateTagertId).append(html);
				layer.close(templateIndex);
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function selectTemplateAsParam(id) {
	selectTemplateTagertId = id;
	templateType = "param";
	templateIndex = layer.open({
		type: 1,
		title: templateStr.select,
		area: ['450px', '350px'], // 宽高
		content: $('#templateSelectDiv')
	});
}

function selectTemplateAsHttp() {
	isHttp = true;
	templateIndex = layer.open({
		type: 1,
		title: templateStr.select,
		area: ['450px', '350px'], // 宽高
		content: $('#templateSelectDiv')
	});
}

function selectTemplateAsStream() {
	isStream = true;
	templateIndex = layer.open({
		type: 1,
		title: templateStr.select,
		area: ['450px', '350px'], // 宽高
		content: $('#templateSelectDiv')
	});
}

function buildTemplateParam(uuid, param, includePosition) {
	var position = param.position || 0;
	var positionTd = '';
	if (includePosition) {
		positionTd = `
				<td style="width: 100px;">
					<select name="position" class="layui-input" style="height: 30px;">
						<option value="0" ${position == 0 ? 'selected' : ''}>${serverStr.paramAppend}</option>
						<option value="1" ${position == 1 ? 'selected' : ''}>${serverStr.paramPrepend}</option>
					</select>
				</td>`;
	}
	return `
			<tr name="param" id="${uuid}">
				<td>
					${templateStr.template}
				</td>
				<td  style="width: ${includePosition ? '40%' : '50%'};">
					${param.templateName}
					<input type="hidden" name="templateValue" value="${param.templateValue}">
					<input type="hidden" name="templateName" value="${param.templateName}">
				</td>
				${positionTd}
				<td>
					<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">${commonStr.del}</button>

					<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', -1)">${commonStr.up}</button>
					<button class="layui-btn layui-btn-normal layui-btn-sm" onclick="setParamOrder('${uuid}', 1)">${commonStr.down}</button>
				</td>
			</tr>
			`;
}

function addHttpParam(templateId) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/http/addTemplate',
		data: {
			templateId: templateId
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


function addStreamParam(templateId) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/stream/addTemplate',
		data: {
			templateId: templateId
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