var element;
$(function(){
	
	layui.use('element', function(){
		  element = layui.element; // Tab的切换功能，切换事件监听等，需要依赖element模块
	});

	if($("#isInit").val() == 'false'){
		layer.open({
			type : 1,
			closeBtn  :0,
			title : "初始化nginx配置",
			area : [ '750px', '400px' ], //宽高
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
				
				$("#memContent").html("( 已用"+monitorInfo.usedMemory + " / 总共" + monitorInfo.totalMemorySize + " )");
				$("#cpuCount").html("( 核心数:" + monitorInfo.cpuCount + " )");
				
			}
		},
		error : function() {
			//alert("出错了,请联系技术人员!");
		}
	});
}


function addNginxGiudeOver(){
	if($("#nginxPath").val() == ''){
		alert("nginx.conf路径未填写");
	}
	if($("#nginxExe").val() == ''){
		alert("nginx执行命令未填写");
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
				alert(obj.msg);
			}
		},
		error : function() {
			alert("出错了,请联系技术人员!");
		}
	});
}