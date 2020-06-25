var load;
$(function(){
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem : '#upload',
			url : '/upload/',
			accept : 'file',
			before : function(res){
				load = layer.load();
			},
			done : function(res) {
				layer.close(load);
				// 上传完毕回调
				if (res.success) {
					debugger
					var path = res.obj.split('/');
					if(path[path.length-1].indexOf('.zip')==-1){
						alert("只能上传zip文件");
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
	
	showWindow("添加zip包");
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '550px', '300px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#name").val() == '' || $("#dir").val() == ''){
		alert("未填写完整");
		return;
	}
	
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/www/addOver',
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
			alert("出错了,请联系技术人员!");
		}
	});
}


function del(id){
	if(confirm("确认删除?")){
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
				alert("出错了,请联系技术人员!");
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
    alert('复制成功');
}

