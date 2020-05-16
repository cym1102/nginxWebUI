$(function() {
	loadOrg();

	$("#nginxContent").setTextareaCount();

	$("#org").setTextareaCount();
})

function replace() {
	if ($("#nginxPath").val() == '') {
		alert("nginx配置文件路径为空");
		return;
	}

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/replace',
		data : {
			nginxPath : $("#nginxPath").val(),
			nginxContent : $("#nginxContent").val()

		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				layer.msg(data.obj);

				loadOrg();

			} else {
				layer.alert(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function loadOrg() {

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/loadOrg',
		data : {
			nginxPath : $("#nginxPath").val()
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				$("#org").val(data.obj)
				$("#org").setTextareaCount();
			} else {
				layer.alert(data.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}

function check() {
	if ($("#nginxPath").val() == '') {
		alert("nginx配置文件路径为空");
		return;
	}
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/check',
		data : {
			nginxPath : $("#nginxPath").val()
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.msg(data.obj);
			} else {
				layer.open({
					  type: 0, 
					  area : [ '800px', '400px' ],
					  content: data.msg
				});
			}
		},
		error : function() {
			layer.closeAll();
			alert("出错了,请联系技术人员!");
		}
	});
}

function reboot() {
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/reboot',
		data : {
			nginxPath : $("#nginxPath").val()
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.msg(data.obj);
			} else {
				layer.open({
					  type: 0, 
					  area : [ '800px', '400px' ],
					  content: data.msg
				});
			}
		},
		error : function() {
			layer.closeAll();
			alert("出错了,请联系技术人员!");
		}
	});

}