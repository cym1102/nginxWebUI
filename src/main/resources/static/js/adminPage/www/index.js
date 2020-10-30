var load;
$(function(){
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem : '#upload',
			url : '/adminPage/main/upload/',
			accept : 'file',
			before : function(res){
				load = layer.load();
			},
			done : function(res) {
				layer.close(load);
				// 上传完毕回调
				if (res.success) {
					var path = res.obj.split('/');
					if(path[path.length-1].indexOf('.zip')==-1){
						layer.alert("只能上传zip文件");
						return;
					}
						
					$("#fileName").html(path[path.length-1]);
					$("#dir").val(res.obj);
				}

			},
			error : function() {
				layer.close(load);
				// 请求异常回调
			}
		});

	});
	
	
})

function search() {
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#name").val(""); 
	$("#fileName").html(""); 
	$("#dir").val(""); 
	$("#zipDiv").show();
	
	 $("#action").val("addOver");

	showWindow(wwwStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '550px', '350px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#id").val() == '' && ($("#name").val() == '' || $("#dir").val() == '')){
		layer.alert(wwwStr.noFile);
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/www/' + $("#action").val(),
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


function del(id){
	if(confirm(commonStr.confirmDel)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/www/del',
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

function copy(str){
    var oInput = document.createElement('input');
    oInput.value = str;
    document.body.appendChild(oInput);
    oInput.select(); // 选择对象
    document.execCommand("Copy"); // 执行浏览器复制命令
    oInput.className = 'oInput';
    oInput.style.display='none';
    layer.msg(wwwStr.cpoySuccess);
}

function edit(id){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/www/detail',
		data : {
			id : id
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var www = data.obj;
				
				$("#id").val(www.id); 
				$("#name").val(www.name); 
				$("#fileName").html(""); 
				$("#dir").val(""); 
				
				$("#nameDiv").show();
				$("#zipDiv").hide();
				
				$("#action").val("rename");
				showWindow(commonStr.edit);
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function update(id){
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/www/detail',
		data : {
			id : id
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var www = data.obj;
				
				$("#id").val(www.id); 
				$("#name").val(www.name); 
				$("#fileName").html(""); 
				$("#dir").val(""); 
				
				$("#nameDiv").hide();
				$("#zipDiv").show();
				
					
				$("#action").val("update");
				showWindow(commonStr.update);
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}