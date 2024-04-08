
function preview(id,type){
	
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/main/preview',
		data: {
			id: id,
			type : type
		},
		dataType: 'json',
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
				layer.msg(data.msg)
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}