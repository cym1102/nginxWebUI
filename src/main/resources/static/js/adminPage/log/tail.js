var run = true;
var guid = guid();

$(function() {
	setInterval(startTail, 1000);
});
// 保留上一次数据
var temp = "";

function startTail() {
	if (run) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/log/tailCmd',
			data: {
				id: $("#id").val(),
				guid: guid
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					// 接收服务端的实时日志并添加到HTML页面中
					$("#log-container").html(data.obj);
					// 滚动条滚动到最低部
					if (data.obj != "" && data.obj != temp) {
						window.scrollTo(0, document.body.scrollHeight);
					}
					temp = data.obj;
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}

function stopOrStart() {
	if (run) {
		run = false;
		$("#stopOrStart").html(loginStr.continue);
	} else {
		run = true;
		$("#stopOrStart").html(loginStr.pause);
	}
}
