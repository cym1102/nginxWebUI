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
					layer.alert(commonStr.errorInfo);
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
		layer.msg(confStr.jserror2);
		return;
	}

	var json = {};
	json.nginxPath = $("#nginxPath").val();
	json.nginxContent = $("#nginxContent").val();
	json.subContent = [];
	json.subName = [];
	$("textarea[name='subContent']").each(function(){
		json.subContent.push($(this).val());
	})
	$("input[name='subName']").each(function(){
		json.subName.push($(this).val());
	})
	
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/conf/replace',
		data : { 
			json: JSON.stringify(json) 
		},
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
			layer.alert(commonStr.errorInfo);
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
					var uuid = confFile.name.replace(/\./g, "-");
					html += `<div class="title" onclick="showHide('${uuid}')">${confFile.name} ▼</div>
							<textarea lang="${uuid}" class="layui-textarea conf sub" name="subContent" style="height: 200px; resize: none;"  spellcheck="false">${confFile.conf}</textarea>
							<input type="hidden" name="subName" value="${confFile.name}">
					`;
				}
				
				$("#nginxContentOther").html(html);
			
				$(".conf").setTextareaCount();
				$(".sub").parent().hide();　
			} else {
				layer.alert(data.msg);
			}
		},
		error : function() {
			//layer.closeAll();
			layer.alert(commonStr.errorInfo);
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
					var uuid = confFile.name.replace(/\./g, "-");
					html += `<div class="title" onclick="showHide('${uuid}')">${confFile.name} ▼</div>
					<textarea lang="${uuid}" class="layui-textarea org sub" style="height: 200px; resize: none; background-color: #ededed;" readonly="readonly" spellcheck="false">${confFile.conf}</textarea>`;
				}
				$("#orgOther").html(html);
				
				$(".org").setTextareaCount();
				$(".sub").parent().hide();　
			} else {
				layer.alert(data.msg);
			}
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}

function showHide(id){
	
	if($(`textarea[lang="${id}"]`).parent().is(':hidden')){
		　$(`textarea[lang="${id}"]`).parent().show();　
	} else {
		 $(`textarea[lang="${id}"]`).parent().hide();　
	}
	
}

function check() {
	if ($("#nginxPath").val() == '') {
		layer.msg(confStr.jserror2);
		return;
	}
	
	if ($("#nginxExe").val() == '') {
		layer.msg(confStr.jserror3);
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			layer.msg(confStr.jserror4);
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
			layer.alert(commonStr.errorInfo);
		}
	});
}

function reload() {
	if ($("#nginxPath").val() == '') {
		layer.msg(confStr.jserror2);
		return;
	}
	
	if ($("#nginxExe").val() == '') {
		layer.msg(confStr.jserror3);
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			layer.msg(confStr.jserror4);
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
			layer.alert(commonStr.errorInfo);
		}
	});

}

function start(){
	if ($("#nginxPath").val() == '') {
		layer.msg(confStr.jserror2);
		return;
	}
	
	if ($("#nginxExe").val() == '') {
		layer.msg(confStr.jserror3);
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			layer.msg(confStr.jserror4);
			return;
		}
	}
	
	if(confirm(confStr.confirmStart)){
		layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/conf/start',
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
				
				setTimeout(() => {
					nginxStatus();
				}, 1000);
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}



function stop(){
	if ($("#nginxExe").val() == '') {
		layer.msg(confStr.jserror3);
		return;
	}
	
	if($("#nginxExe").val().indexOf('/') > -1 || $("#nginxExe").val().indexOf('\\') > -1){
		if ($("#nginxDir").val() == '') {
			layer.msg(confStr.jserror4);
			return;
		}
	}
	
	
	if(confirm(confStr.confirmStop)){
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
				
				setTimeout(() => {
					nginxStatus();
				}, 3000);
				
			},
			error : function() {
				layer.closeAll();
				layer.alert(commonStr.errorInfo);
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


function diffUsingJS() {
    // get the baseText and newText values from the two textboxes, and split them into lines
    var base = difflib.stringAsLines($("#org ").val());
    var newtxt = difflib.stringAsLines($("#nginxContent").val());

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
        baseTextLines: base,
        newTextLines: newtxt,
        opcodes: opcodes,
        // set the display titles for each resource
        baseTextName: confStr.target,
        newTextName: confStr.build,
        //contextSize: contextSize,
        viewType: 1
    }));

    // scroll down to the diff view window.
    layer.open({
		type : 1,
		title: false,
		area : [ '1000px', '700px' ], //宽高
		content : $('#diffoutput')
	});
}