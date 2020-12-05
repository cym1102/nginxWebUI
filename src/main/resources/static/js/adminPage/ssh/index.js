function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#name").val("");
	$("#host").val("");
	$("#port").val("22");
	$("#username").val("");
	$("#password").val("");

	showWindow(commonStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['400px', '500px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/ssh/addOver',
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
		url: ctx + '/adminPage/ssh/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var ssh = data.obj;
				$("#name").val(ssh.name);
				$("#host").val(ssh.host);
				$("#port").val(ssh.port);
				$("#username").val(ssh.username);
				$("#password").val(ssh.password);

				form.render();
				showWindow(commonStr.edit);
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
			url: ctx + '/adminPage/ssh/del',
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

function link(id, name) {

	layer.open({
		type: 2,
		title: name,
		resize  : false,
		area: ['1000px', '430px'], // 宽高
		content: ctx + `/adminPage/ssh/webssh?id=` + id
	});

}
