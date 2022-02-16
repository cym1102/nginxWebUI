var load;
$(function(){
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem : '#upload',
			url : '/adminPage/main/upload',
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
					$("#dirTemp").val(res.obj);
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
	$("#dir").val(""); 
	$("#dirTemp").html(""); 
	$("#fileName").html(""); 
	//$("#zipDiv").show();
	
	// $("#action").val("addOver");

	showWindow(wwwStr.add);
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '560px', '360px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#dir").val() == ''){
		layer.alert(wwwStr.noFill);
		return;
	}
	
	if($("#dirTemp").val() == ''){
		layer.alert(wwwStr.noUpload);
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
				$("#dir").val(www.dir); 
				$("#dirTemp").html(""); 
				$("#fileName").html("");
				//$("#nameDiv").show();
				//$("#zipDiv").hide();
				
				//$("#action").val("rename");
				showWindow(wwwStr.editOrUpdate); 
			}else{
				layer.msg(data.msg)
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}



function selectRootCustom(){
	rootSelect.selectOne(function callBack(val){
		$("#dir").val(val);
		//$("#fileName").html(val);
	});
}