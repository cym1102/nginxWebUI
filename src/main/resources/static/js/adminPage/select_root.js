var rootSelect = {
	zTreeObj : null,
	callBackFunc : null,
	index : null,
	setting : {
		async : {
			enable : true,
			dataType : "json",
			url: ctx + 'adminPage/root/getList',
			autoParam: ["id"]
		},
        data:{
            simpleData:{
                enable: true,
                idKey:'id',
                pIdKey:'pid',
                rootPId: ''
            }
        }
	},
	load : function() {
		rootSelect.zTreeObj = $.fn.zTree.init($("#rootSelect"), rootSelect.setting);
	},
	selectOne : function(callBack) {
		this.callBackFunc = callBack;
		this.load();
		
		this.index = layer.open({
			type : 1,
			title : commonStr.selectFile,
			area : [ '400px', '610px' ], // 宽高
			content : $('#rootSelectDiv')
		});
	},
	selectOver : function(){
		var nodes = rootSelect.zTreeObj.getSelectedNodes();
		if(nodes.length > 0){
			rootSelect.callBackFunc(nodes[0].id.replace(/\\/g,"/"));
		}
		
		layer.close(this.index);
	},
	addDir : function(){
		var dir ="";
		var nodes = rootSelect.zTreeObj.getSelectedNodes();
		if(nodes.length > 0){
			dir = nodes[0].id.replace(/\\/g,"/");
		}
		if(dir == ""){
			alert(commonStr.noDir);
			return;
		}
			
		layer.prompt(function(value, index, elem){
			$.ajax({
				type: 'POST',
				url: ctx + '/adminPage/root/mkdir',
				data: {
					name : value,
					dir : dir
				},
				dataType: 'json',
				success: function(data) {
					if (data.success) {
						layer.close(index);
						//debugger
						rootSelect.zTreeObj.reAsyncChildNodes(rootSelect.zTreeObj.getSelectedNodes()[0], "refresh", false);
					} else {
						layer.msg(data.msg)
					}
				},
				error: function() {
					layer.alert(commonStr.errorInfo);
				}
			});
		  	
		});
	},
	close : function() {
		layer.close(this.index);
	},
	clear : function() {
		rootSelect.callBackFunc("");
		layer.close(this.index);
	}
}

function selectRoot(inputId){
	rootSelect.selectOne(function callBack(val){
		$("#" + inputId).val(val);
	});
}
