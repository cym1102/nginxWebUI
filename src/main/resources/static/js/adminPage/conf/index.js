$(function() {
	loadOrg();
	loadConf();
	
	form.on('switch(decompose)', function(data){
		  
		  $.ajax({
				type : 'POST',
				url : ctx + '/adminPage/conf/decompose',
				data : {
					decompose : data.elem.checked
				},
				dataType : 'json',
				success : function(data) {			
					if (data.success) {
						loadConf();
						loadOrg();
					} else {
						layer.msg(data.msg);
					}
				},
				error : function() {
					alert("出错了,请联系技术人员!");
				}
		});
	});   
	
	nginxStatus();
})

function nginxStatus() {

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/nginxStatus',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				$("#nginxStatus").html(data.obj);
			} 
		},
		error : function() {
			
		}
	});
}


function replace() {
	if ($("#nginxPath").val() == '') {
		alert("nginx配置文件路径为空");
		return;
	}

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/replace',
		data :$("#addForm").serialize(),
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				layer.msg(data.obj);
				//loadConf();
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


function loadConf() {
	//layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/loadConf',
		data : {
		
		},
		dataType : 'json',
		success : function(data) {
			//layer.closeAll();
			if (data.success) {
				var confExt = data.obj
				$("#nginxContent").val(confExt.conf)
				
				var html = "";
				for(var i=0;i<confExt.fileList.length;i++){
					var confFile = confExt.fileList[i];
					
					html += `<div class="title">${confFile.name}</div>
							<textarea class="layui-textarea" name="subContent" style="height: 200px; resize: none;"  spellcheck="false">${confFile.conf}</textarea>
							<input type="hidden" name="subName" value="${confFile.name}">
					`;
				}
				
				$("#nginxContentOther").html(html);
			
				$("textarea").setTextareaCount();
			} else {
				layer.alert(data.msg);
			}
		},
		error : function() {
			//layer.closeAll();
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
				var confExt = data.obj
				$("#org").val(confExt.conf);
				
				var html = "";
				for(var i=0;i<confExt.fileList.length;i++){
					var confFile = confExt.fileList[i];
					
					html += `<div class="title">${confFile.name}</div>
					<textarea class="layui-textarea" style="height: 200px; resize: none; background-color: #ededed;" readonly="readonly" spellcheck="false">${confFile.conf}</textarea>`;
				}
				$("#orgOther").html(html);
				
				$("textarea").setTextareaCount();
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
		alert("conf配置文件路径为空");
		return;
	}
	
	if ($("#nginxExe").val() == '') {
		alert("nginx执行文件路径为空");
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			alert("你使用了绝对路径执行命令，请填写nginx目录");
			return;
		}
	}
	
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/check',
		data : {
			nginxPath : $("#nginxPath").val(),
			nginxExe : $("#nginxExe").val(),
			nginxDir : $("#nginxDir").val()
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.open({
					  type: 0, 
					  area : [ '810px', '400px' ],
					  content: data.obj
				});
			} else {
				layer.open({
					  type: 0, 
					  area : [ '810px', '400px' ],
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

function reload() {
	if ($("#nginxPath").val() == '') {
		alert("conf配置文件路径为空");
		return;
	}
	
	if ($("#nginxExe").val() == '') {
		alert("nginx执行文件路径为空");
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			alert("你使用了绝对路径执行命令，请填写nginx目录");
			return;
		}
	}
	
	layer.load();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/reload',
		data : {
			nginxPath : $("#nginxPath").val(),
			nginxExe : $("#nginxExe").val(),
			nginxDir : $("#nginxDir").val()
		},
		dataType : 'json',
		success : function(data) {
			layer.closeAll();
			if (data.success) {
				layer.open({
					  type: 0, 
					  area : [ '810px', '400px' ],
					  content: data.obj
				});
			} else {
				layer.open({
					  type: 0, 
					  area : [ '810px', '400px' ],
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

function start(){
	if ($("#nginxExe").val() == '') {
		alert("nginx执行文件路径为空");
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			alert("你使用了绝对路径执行命令，请填写nginx目录");
			return;
		}
	}
	
	if(confirm("确认启动?")){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/conf/start',
			data : {
				nginxExe : $("#nginxExe").val(),
				nginxDir : $("#nginxDir").val()
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.obj
					});
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
				
				nginxStatus();
			},
			error : function() {
				layer.closeAll();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}



function stop(){
	if ($("#nginxExe").val() == '') {
		alert("nginx执行文件路径为空");
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			alert("你使用了绝对路径执行命令，请填写nginx目录");
			return;
		}
	}
	
	
	if(confirm("确认停止?")){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/conf/stop',
			data : {
				nginxExe : $("#nginxExe").val(),
				nginxDir : $("#nginxDir").val()
			},
			dataType : 'json',
			success : function(data) {
				layer.closeAll();
				if (data.success) {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.obj
					});
				} else {
					layer.open({
						  type: 0, 
						  area : [ '810px', '400px' ],
						  content: data.msg
					});
				}
				
				nginxStatus();
			},
			error : function() {
				layer.closeAll();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


function saveCmd(){
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/saveCmd',
		data : {
			nginxExe : $("#nginxExe").val(),
			nginxDir : $("#nginxDir").val(),
			nginxPath : $("#nginxPath").val()
		},
		dataType : 'json',
		success : function(data) {
			//layer.msg("ok");
		},
		error : function() {
			
		}
	});
	
}



function selectRootCustom(inputId){
	rootSelect.selectOne(function callBack(val){
		$("#" + inputId).val(val);
		saveCmd();
	});
}
