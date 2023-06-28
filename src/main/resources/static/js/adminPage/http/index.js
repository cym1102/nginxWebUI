$(function() {
	form.on('switch(enable)', function(data) {

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/http/setEnable',
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
})

function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#name").val("");
	$("#value").val("");

	showWindow(httpStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['600px', '400px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg(httpStr.noname);
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/http/addOver',
		data: $('#addForm').serialize(),
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
	$("#id").val(id);

	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/http/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var http = data.obj;
				$("#id").val(http.id);
				$("#value").val(http.value);
				$("#name").val(http.name);

				form.render();
				showWindow(httpStr.edit);
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
			url: ctx + '/adminPage/http/del',
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

function guide() {

	layer.open({
		type: 1,
		title: httpStr.guide,
		area: ['800px', '90%'], // 宽高
		content: $('#guideDiv')
	});

}

function addGiudeOver() {

	var params = [];
	$("input[name='param']").each(function() {

		var http = {};
		http.name = $(this).attr("id");
		http.value = $(this).val();
		http.unit = $(this).attr("lang");

		if (http.name == 'gzip') {
			if ($(this).prop("checked")) {
				http.value = "on";
			} else {
				http.value = "off";
			}
		}

		params.push(http);
	})

	var http = {
		name: "gzip_types",
		value: "",
		unit : ""
	};

	$("input[name='type']").each(function() {

		if ($(this).val() == 'js' && $(this).prop("checked")) {
			http.value += "application/javascript application/x-javascript text/javascript ";
		}
		if ($(this).val() == 'css' && $(this).prop("checked")) {
			http.value += "text/css ";
		}
		if ($(this).val() == 'json' && $(this).prop("checked")) {
			http.value += "application/json ";
		}
		if ($(this).val() == 'xml' && $(this).prop("checked")) {
			http.value += "application/xml ";
		}
	})

	if (http.value != "") {
		params.push(http);
	}


	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/http/addGiudeOver',
		data: {
			json: JSON.stringify(params),
			logStatus: $("#logStatus").prop("checked"),
			webSocket: $("#webSocket").prop("checked")
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
		url: ctx + '/adminPage/http/setOrder',
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
