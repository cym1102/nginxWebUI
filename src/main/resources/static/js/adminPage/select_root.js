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
			title : "选择文件",
			area : [ '400px', '600px' ], // 宽高
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
