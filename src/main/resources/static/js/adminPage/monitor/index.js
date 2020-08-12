var element;
$(function(){
	
	layui.use('element', function(){
		  element = layui.element; // Tab的切换功能，切换事件监听等，需要依赖element模块
	});

	if($("#isInit").val() == 'false'){
		layer.open({
			type : 1,
			closeBtn  :0,
			title : monitorStr.init,
			area : [ '800px', '400px' ], //宽高
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


function addNginxGiudeOver(){
	if($("#nginxPath").val() == ''){
		layer.alert(monitorStr.pathAlert);
	}
	if($("#nginxExe").val() == ''){
		layer.alert(monitorStr.exeAlert);
	}
	
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/monitor/addNginxGiudeOver',
		data :{
			nginxPath : $("#nginxPath").val(),
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