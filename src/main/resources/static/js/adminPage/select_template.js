$(function(){
	  $.ajax({
			type : 'POST',
			url : ctx + '/adminPage/template/getTemplate',
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					var list = data.obj;
					
					for(var i=0;i<list.length;i++){
						var template = list[i];
						var html = `<option value="${template.id}">${template.name}</option>`;
						
						$("#selectTemplateId").append(html);
						
						form.render();
					}
					
				} else {
					layer.msg(data.msg);
				}
			},
			error : function() {
				alert("出错了,请联系技术人员!");
			}
		});
})


var selectTemplateTagertId;
var templateIndex;
function selectTemplate(id){
	selectTemplateTagertId = id;
	
	templateIndex = layer.open({
		type: 1,
		title: "选择参数模板",
		area: ['450px', '350px'], // 宽高
		content: $('#templateSelectDiv')
	});
}

function selectTemplateOver(){
	var templateId = 	$("#selectTemplateId").val();
	if(templateId == null){
		layer.msg("未选中项");
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
				
				var uuid = guid();
				var html = `
					<tr name="param" id="${uuid}">
						<td>
							模板
						</td>
						<td  style="width: 60%;">
							${ext.template.name}
							<input type="hidden" name="templateValue" value="${ext.template.id}">
							<input type="hidden" name="templateName" value="${ext.template.name}">
						</td>
						<td>
							<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
						</td>
					</tr>
				`;
				
				$("#" + selectTemplateTagertId).append(html);
				
				layer.close(templateIndex);
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			alert("出错了,请联系技术人员!");
		}
	});
}


function buildTemplateParam(param){
	return `
			<tr name="param" id="${uuid}">
				<td>
					模板
				</td>
				<td  style="width: 60%;">
					${param.templateName}
					<input type="hidden" name="templateValue" value="${param.templateValue}">
					<input type="hidden" name="templateName" value="${param.templateName}">
				</td>
				<td>
					<button type="button" class="layui-btn layui-btn-sm layui-btn-danger" onclick="delTr('${uuid}')">删除</button>
				</td>
			</tr>
			`;
}