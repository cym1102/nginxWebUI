$(function() {
	form.on('checkbox(checkAll)', function(data) {
		if (data.elem.checked) {
			$("input[name='ids']").prop("checked", true)
		} else {
			$("input[name='ids']").prop("checked", false)
		}

		form.render();
	});	
	
	form.on('select(denyAllowValue)', function(data) {
		checkDenyAllow(data.value);
	});
})

function checkDenyAllow(value) {
	$("#denyDiv").hide();
	$("#allowDiv").hide();

	if (value == 1) {
		$("#denyDiv").show();
	}
	if (value == 2) {
		$("#allowDiv").show();
	}
	if (value == 3) {
		$("#denyDiv").show();
		$("#allowDiv").show();
	}
}


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#value").val(""); 
	
	showWindow(stream.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '600px', '400px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg(stream.noname);
		return;
	}
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/stream/addOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function edit(id) {
	$("#id").val(id); 
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/stream/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var http = data.obj;
				$("#id").val(http.id); 
				$("#value").val(http.value); 
				$("#name").val(http.name);
				
				form.render();
				showWindow(stream.edit);
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function del(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/stream/del',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				}else{
					layer.msg(data.msg)
				}
			},
			error : function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}


function delMany() {
	if (confirm(commonStr.confirmDel)) {
		var ids = [];

		$("input[name='ids']").each(function() {
			if ($(this).prop("checked")) {
				ids.push($(this).val());
			}
		})

		if (ids.length == 0) {
			layer.msg(commonStr.unselected);
			return;
		}

		$.ajax({
			type: 'POST',
			url : ctx + '/adminPage/stream/del',
			data: {
				id: ids.join(",")
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
				layer.alert("请求失败，请刷新重试");
			}
		});
	}
}



function setOrder(id, count){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/stream/setOrder',
		data : {
			id : id,
			count : count
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function guide(){
	layer.open({
		type: 1,
		title: httpStr.guide,
		area: ['800px', '200px'], // 宽高
		content: $('#guideDiv')
	});
}


function addGiudeOver(){
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/stream/addGiudeOver',
		data: {
			logStatus: $("#logStatus").prop("checked")
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



function setDenyAllow() {

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/stream/getDenyAllow',
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				var map = data.obj;

				$("#denyAllowValue").val(map.denyAllowStream);
				if (map.denyId != null) {
					$("#denyIdValue").val(map.denyIdStream);
				}
				if (map.allowId != null) {
					$("#allowIdValue").val(map.allowIdStream);
				}
				checkDenyAllow(map.denyAllow);

				form.render();
				layer.open({
					type: 1,
					title: serverStr.denyAllowModel,
					area: ['650px', '500px'], // 宽高
					content: $('#denyAllowDiv')
				});
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

function setDenyAllowOver() {
	var denyAllow = $("#denyAllowValue").val();
	var denyId = $("#denyIdValue").val();
	var allowId = $("#allowIdValue").val();

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/stream/setDenyAllow',
		data: {
			denyAllow: denyAllow,
			denyId: denyId,
			allowId: allowId
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
			closeLoad();
			layer.alert(commonStr.errorInfo);
		}
	});
}