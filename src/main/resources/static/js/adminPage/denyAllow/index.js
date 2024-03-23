$(function() {
	form.on('checkbox(checkAll)', function(data) {
		if (data.elem.checked) {
			$("input[name='ids']").prop("checked", true)
		} else {
			$("input[name='ids']").prop("checked", false)
		}

		form.render();
	});
})


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}


function add() {
	$("#id").val("");
	$("#name").val("");
	$("#ip").val("");
	showWindow(commonStr.add);
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['1000px', '610px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == '' || $("#ip").val() == '') {
		layer.msg(serverStr.noFill);
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/denyAllow/addOver',
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
		url: ctx + '/adminPage/denyAllow/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			if (data.success) {
				var denyAllow = data.obj;
				$("#id").val(denyAllow.id);
				$("#name").val(denyAllow.name);
				$("#ip").val(denyAllow.ip);

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
			url: ctx + '/adminPage/denyAllow/del',
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
