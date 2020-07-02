var element;
$(function(){
	
	layui.use('element', function(){
		  element = layui.element; // Tab的切换功能，切换事件监听等，需要依赖element模块
	});
	
	
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