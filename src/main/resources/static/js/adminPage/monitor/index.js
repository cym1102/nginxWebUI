var element;
$(function(){
	
	layui.use('element', function(){
		  element = layui.element; // Tab的切换功能，切换事件监听等，需要依赖element模块
	});

	if($("#isInit").val() == 'false'){
		$("#selectForm").show();
		$("#addForm").hide();
		
		layer.open({
			type : 1,
			closeBtn  :0,
			title : monitorStr.init,
			area : [ '500px', '300px' ], //宽高
			content : $('#nginxGuideDiv')
		});
	}
	
	setInterval(() => {
		load();
	}, 1000);

})


function load(){

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/monitor/check',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var monitorInfo = data.obj;
				element.progress('cpu', monitorInfo.cpuRatio);
				element.progress('mem', monitorInfo.memRatio);
				
				$("#memContent").html("( " + monitorStr.used + ":" + monitorInfo.usedMemory + " / "+ monitorStr.total + ":" + monitorInfo.totalMemorySize + " )");
				$("#cpuCount").html("( " + monitorStr.coreCount + ":" + monitorInfo.cpuCount + " )");
				
			}
		},
		error : function() {
			//layer.alert(commonStr.errorInfo);
		}
	});
}

function back(){
	$("#selectForm").show();
	$("#addForm").hide();
}

function selectNginxGiudeOver(){
	var checkType = 0;
	if($("#check0").prop("checked")){
		checkType = 0;
	}
	if($("#check1").prop("checked")){
		checkType = 1;
	}
	
	if(checkType == 0){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/monitor/addNginxGiudeOver',
			data :{
				//nginxPath : "",
				nginxExe : "nginx",
				nginxDir : ""
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					location.reload();
				}else{
					layer.alert(obj.msg);
				}
			},
			error : function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	} else {
		$("#selectForm").hide();
		$("#addForm").show();
	}
}

function addNginxGiudeOver(){
	//if($("#nginxPath").val() == ''){
	//	layer.alert(monitorStr.pathAlert);
	//}
	if($("#nginxExe").val() == ''){
		layer.msg(monitorStr.exeAlert);
		return;
	}
	if($("#nginxDir").val() == ''){
		layer.msg(monitorStr.dirAlert);
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/monitor/addNginxGiudeOver',
		data :{
			//nginxPath : $("#nginxPath").val(),
			nginxExe : $("#nginxExe").val(),
			nginxDir : $("#nginxDir").val()
		},
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				location.reload();
			}else{
				layer.alert(obj.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}