$(function() {

	// layui.use('upload', function() {
	// var upload = layui.upload;
	// upload.render({
	// elem : '#dataImport',
	// url : '/upload/',
	// accept : 'file',
	// done : function(res) {
	// // 上传完毕回调
	// if (res.success) {
	// dataImport(res.obj)
	// }
	//
	// },
	// error : function() {
	// // 请求异常回调
	// }
	// });
	//
	// });
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
		alert('请选择文件');
	} else {
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
						layer.msg("导入成功");
					} else {
						layer.msg(data.msg);
					}
				},
				error : function() {
					alert("出错了,请联系技术人员!");
				}
			});
		}
	}
}
