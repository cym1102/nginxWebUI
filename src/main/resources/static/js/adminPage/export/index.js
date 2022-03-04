$(function() {
	if (getQueryString("over") == 'true') {
		layer.msg(exportStr.importSuccess);
	}
});

function dExport() {
	window.open(ctx + "/adminPage/export/dataExport")
}


function dImport() {
	$("#file").click();
}

function dImportOver() {
	var files = $('#file').prop('files');// 获取到文件列表
	if (files.length == 0) {
		layer.alert(exportStr.selectFile);
	} else {
		if (confirm(exportStr.confirm)) {
			$("#dataImport").submit();
		}

		/*
		var reader = new FileReader();// 新建一个FileReader
		reader.readAsText(files[0], "UTF-8");// 读取文件
		reader.onload = function(evt) { // 读取完文件之后会回来这里
			var json = evt.target.result; // 读取文件内容

			$.ajax({
				type : 'POST',
				url : ctx + '/adminPage/export/dataImport',
				data : {
					json : json
				},
				dataType : 'json',
				success : function(data) {
					if (data.success) {
						layer.msg(exportStr.importSuccess);
					} else {
						layer.msg(data.msg);
					}
				},
				error : function() {
					layer.alert(commonStr.errorInfo);
				}
			});
		}
		*/
	}
}

function lExport() {
	window.open(ctx + "/adminPage/export/logExport")
}