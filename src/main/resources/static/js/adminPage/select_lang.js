var changeLangIndex;
function changeLang() {
	changeLangIndex = layer.open({
		type: 1,
		title: "Language",
		area: ['450px', '300px'], // 宽高
		content: $('#changeLangDiv')
	});
}


function changeLangOver() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/changeLang',
		data: {
			lang: $("#lang").val()
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