var element;
var netList = [];

$(function(){
	netDiv = echarts.init(document.getElementById('netDiv'));
	
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

	initEchart();
	network();
})



function load(){

	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/monitor/load',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var monitorInfo = data.obj;
				element.progress('cpu', monitorInfo.cpuRatio);
				element.progress('mem', monitorInfo.memRatio);
				
				$("#memContent").html("( " + monitorStr.used + ":" + monitorInfo.usedMemory + " / "+ monitorStr.total + ":" + monitorInfo.totalMemorySize + " )");
				$("#cpuCount").html("( " + monitorStr.coreCount + ":" + monitorInfo.cpuCount + " / " + monitorStr.threadCount + ":" + monitorInfo.threadCount+ " )");
				
			
			}
		},
		error : function() {
			//layer.alert(commonStr.errorInfo);
		}
	});
}


function network(){
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/monitor/network',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				var networkInfo = data.obj;
				netList.push(networkInfo);
				
				if(netList.length > 10){
					netList.splice(0, 1); 
				}
				
				initEchart();
				network();
			}
		},
		error : function() {
			//layer.alert(commonStr.errorInfo);
		}
	});
	
}

function initEchart(){
	
	var time = [];
	var send = [];
	var receive = [];
	for(let i=0; i<netList.length; i++){
		time.push(netList[i].time);
		send.push(netList[i].send);
		receive.push(netList[i].receive);
	}
	
	var option = {
		title: {
			text: monitorStr.netStatistics,
			left: 'left'
		},
		tooltip: {
			trigger: 'axis',
			formatter(params) {
				return `
	            	${monitorStr.send}: ${params[0].value} kB/s<br>
	            	${monitorStr.receive}: ${params[1].value}  kB/s
	            `;
			},
		},
	    legend: {
            data:[ monitorStr.send, monitorStr.receive]
        },
		xAxis: {
			name: monitorStr.time,
			type: 'category',
			data: time
		},
		yAxis: {
			type: 'value',
			axisLabel: {
	           formatter: '{value} kB/s'
	        }
		},
		series: [{
			name: monitorStr.send,
			data: send,
			type: 'line',
			showBackground: true,
			backgroundStyle: {
				color: 'rgba(108,80,243,0.3)'
			}
		}, {
			name: monitorStr.receive,
			data: receive,
			type: 'line',
			showBackground: true,
			backgroundStyle: {
				color: 'rgba(0,202,149,0.3)'
			}
		}

		]
	};

	netDiv.setOption(option);
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