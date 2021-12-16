
function content(id) {
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/bak/content',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var bak = data.obj;
				$("#preview").val(bak.content);
				layer.open({
					type: 1,
					title: commonStr.preview,
					area: ['800px', '600px'], // 宽高
					content: $('#previewDiv')
				});
			}else{
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
			url : ctx + '/adminPage/bak/del',
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


function compare(id){
	
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/bak/getCompare',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			if (data.success) {
				var bak = data.obj.bak;
				var pre = data.obj.pre;
				
				diffUsingJS(pre, bak);
				
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
	
	
}

function diffUsingJS(pre, bak){
	// get the baseText and newText values from the two textboxes, and split them into lines
  	var base = difflib.stringAsLines(pre.content);
    var newtxt = difflib.stringAsLines(bak.content);

    // create a SequenceMatcher instance that diffs the two sets of lines
    var sm = new difflib.SequenceMatcher(base, newtxt);

    // get the opcodes from the SequenceMatcher instance
    // opcodes is a list of 3-tuples describing what changes should be made to the base text
    // in order to yield the new text
    var opcodes = sm.get_opcodes();
    var diffoutputdiv = $("#diffoutput");
    while (diffoutputdiv.firstChild){
		diffoutputdiv.removeChild(diffoutputdiv.firstChild);
	} 
    //var contextSize = $("contextSize").value;
    //contextSize = contextSize ? contextSize : null;

    // build the diff view and add it to the current DOM
	diffoutputdiv.html("");
    diffoutputdiv.append(diffview.buildView({
        baseTextLines: base ,
        newTextLines: newtxt,
        opcodes: opcodes,
        // set the display titles for each resource
        baseTextName: pre.time,
        newTextName: bak.time,
        //contextSize: contextSize,
        viewType: 1
    }));

    // scroll down to the diff view window.
    layer.open({
		type : 1,
		title: false,
		area : [ '90%', '90%' ], //宽高
		content : $('#diffoutput')
	});
}


function replace(id){
	if(confirm(bakStr.restoreNotice)){
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/bak/replace',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					layer.msg(bakStr.operationSuccess);
					
					parent.loadOrg();
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

function delAll(){
	if(confirm(bakStr.clearNotice)){
		$.ajax({
			type : 'GET',
			url : ctx + '/adminPage/bak/delAll',
			dataType : 'json',
		
			success : function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg);
				}
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
	
}