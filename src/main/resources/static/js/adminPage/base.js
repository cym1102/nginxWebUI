var layer;
var element;
var form;
var laypage;
var laydate;

// 使用layui内部jQuery
var $ = layui.$;
var jQuery = layui.$;

$(function() {

	// layer变量
	layer = layui.layer;
	element = layui.element;
	form = layui.form;
	laypage = layui.laypage;

	// 执行一个laypage实例
	laypage.render({
		elem : 'pageInfo', // 渲染节点
		count : page.count, // 总记录数
		curr : page.curr, // 起始页
		limit : page.limit, // 每页记录数
		layout : ['count', 'prev', 'page', 'next',  'skip' ,'limit'],
		jump : function(obj, first) {
			// 首次不执行
			if (!first) {
				// do something
				$("input[name='curr']").val(obj.curr);
				$("input[name='limit']").val(obj.limit);
				$("#searchForm").submit();
			}
		}
	});
	
	// 日期控件
	layui.use('laydate', function() {
		laydate = layui.laydate;

		// 执行laydate实例
		$(".laydate").each(function(){
			$(this).attr("id", "date_" + guid());
			$(this).attr("readonly",true);
			
			laydate.render({
				elem : "#" + $(this).attr("id"), // 指定元素
				type : 'date',
				trigger: 'click',
				format : 'yyyy-MM-dd' // 可任意组合
			}); 
		})
	});

	form.render();

	// 关闭input自动填充
	$("input").attr("autocomplete", "off");
	
	// 菜单选中
	var url = location.pathname + location.search;
	$("a[href='" + ctx + url.substr(1) + "']").parent().addClass("layui-this");
	
	
	$.ajax({
		type : 'POST',
		url : ctx + 'adminPage/login/getLocalType',
		dataType : 'json',
		success : function(data) {
			if (data.success) {
				$("#localType").html(data.obj);
			} 
		},
		error : function() {
			layer.alert(commonStr.errorInfo);
		}
	});
	
	// 判断屏幕分辨率, 给table加上lay-size="sm"
	//alert(document.body.clientWidth);
	if(document.body.clientWidth  <= 1600){
		$(".layui-table").attr("lay-size","sm");
	}
})

// 关闭AJAX相应的缓存
$.ajaxSetup({
	cache : false
});


function gohref(url) {
	location.href = url;
	
}

// 退出登录
function loginOut() {
	if (confirm(baseStr.exit)) {
		location.href = ctx + "/adminPage/login/loginOut";
	}
}

// 日期格式化
Date.prototype.format = function(format) {
	var date = {
		"M+" : this.getMonth() + 1,
		"d+" : this.getDate(),
		"H+" : this.getHours(),
		"m+" : this.getMinutes(),
		"s+" : this.getSeconds(),
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		"S+" : this.getMilliseconds()
	};
	if (/(y+)/i.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + '')
				.substr(4 - RegExp.$1.length));
	}
	for ( var k in date) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? date[k]
					: ("00" + date[k]).substr(("" + date[k]).length));
		}
	}
	return format;
}

function formatDate(now) {
	if (now == null || now == '') {
		return "";
	}

	return new Date(now).format("yyyy-MM-dd HH:mm:ss");
}

// 查看图片
function seePic(url) {
	window.open(url);
}

// 生成uuid
function S4() {
	return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
}
function guid() {
	return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4()
			+ S4() + S4());
}

// 时间字符串转时间戳
function strToTime(str) {
	var str = str.replace(/-/g, '/');
	var timestamp = new Date(str).getTime();
	
	return timestamp
}

// 获取url参数
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

// 下载文件
function downloadFile(url, name) {
	window.open(ctx + "downloadFile?url=" + encodeURIComponent(url) + "&name="
			+ encodeURIComponent(name));
}

function showUpdate(version, url, docker,update){
	var str = `
		<div style="font-size: 16px; font-weight: bolder;">${commonStr.newVersion} ${version}</div>
		<div>${baseStr.updateContent}: ${update}</div>
		<div>${baseStr.jar}: <span class='green'>${url}</span></div>
		<div>${baseStr.docker}: <span class='green'>${docker}</span></div>
		<div>&nbsp;</div>
		<div>
			<button type="button" class="layui-btn layui-btn-sm" onclick="autoUpdate('${url}')">${baseStr.click}</button>
		</div>
	`;
	
	layer.open({
		  type: 0, 
		  title : commonStr.update,
		  btn: [commonStr.close],
		  yes: function(index, layero){
		       layer.closeAll();
		  },
		  area : [ '600px', '400px' ],
		  content: str
	});
	
}

// form转json
function form2JsonString(formId) {
	var paramArray = $('#' + formId).serializeArray();  
	/* 请求参数转json对象 */  
	var jsonObj={};  
	$(paramArray).each(function(){  
		jsonObj[this.name]=this.value;  
	});  
	return JSON.stringify(jsonObj);
	
}

var loaded;
function autoUpdate(url){
	if(confirm(baseStr.confirmUpdate)){
		loaded =	layer.load();
		$.ajax({
			type : 'POST',
			url : ctx + '/autoUpdate',
			data:{
				url : url
			},
			dataType : 'json',
			success : function(data) {
				if(!data.success){
					layer.close(loaded);
					layer.alert(data.msg);
					return;
				}
				
				setTimeout(function(){
					layer.close(loaded);
					layer.alert(baseStr.updateOver);
				},10000)
				
				
			},
			error : function() {
				setTimeout(function(){
					layer.layer.close(loaded);
					layer.alert(baseStr.updateOver);
				},10000)
			}
		});
	}
	
}


function changeLang() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/login/changeLang',
		data: $("#adminForm").serialize(),
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert(commonStr.errorInfo);
		}
	});
}


function  setParamOrder(id, seq){
	if(seq == -1){
		// 前移
		var prev = $("#" + id).prev();
		$("#" + id).after(prev);
	}else{
		// 后移
		var next = $("#" + id).next();
		$("#" + id).before(next);
	}
}

// 显示载入框
var loadIndex;
function showLoad() {
	loadIndex = layer.load();
}
function closeLoad() {
	layer.close(loadIndex);
}


// Base64算法
function Base64() {  
    // private property  
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";  

    // public method for encoding  
    this.encode = function (input) {  
        var output = "";  
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;  
        var i = 0;  
        input = _utf8_encode(input);  
        while (i < input.length) {  
            chr1 = input.charCodeAt(i++);  
            chr2 = input.charCodeAt(i++);  
            chr3 = input.charCodeAt(i++);  
            enc1 = chr1 >> 2;  
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);  
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);  
            enc4 = chr3 & 63;  
            if (isNaN(chr2)) {  
                enc3 = enc4 = 64;  
            } else if (isNaN(chr3)) {  
                enc4 = 64;  
            }  
            output = output +  
            _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +  
            _keyStr.charAt(enc3) + _keyStr.charAt(enc4);  
        }  
        return output;  
    }  

    // public method for decoding  
    this.decode = function (input) {  
        var output = "";  
        var chr1, chr2, chr3;  
        var enc1, enc2, enc3, enc4;  
        var i = 0;  
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");  
        while (i < input.length) {  
            enc1 = _keyStr.indexOf(input.charAt(i++));  
            enc2 = _keyStr.indexOf(input.charAt(i++));  
            enc3 = _keyStr.indexOf(input.charAt(i++));  
            enc4 = _keyStr.indexOf(input.charAt(i++));  
            chr1 = (enc1 << 2) | (enc2 >> 4);  
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);  
            chr3 = ((enc3 & 3) << 6) | enc4;  
            output = output + String.fromCharCode(chr1);  
            if (enc3 != 64) {  
                output = output + String.fromCharCode(chr2);  
            }  
            if (enc4 != 64) {  
                output = output + String.fromCharCode(chr3);  
            }  
        }  
        output = _utf8_decode(output);  
        return output;  
    }  

    // private method for UTF-8 encoding  
    _utf8_encode = function (string) {  
        string = string.replace(/\r\n/g,"\n");  
        var utftext = "";  
        for (var n = 0; n < string.length; n++) {  
            var c = string.charCodeAt(n);  
            if (c < 128) {  
                utftext += String.fromCharCode(c);  
            } else if((c > 127) && (c < 2048)) {  
                utftext += String.fromCharCode((c >> 6) | 192);  
                utftext += String.fromCharCode((c & 63) | 128);  
            } else {  
                utftext += String.fromCharCode((c >> 12) | 224);  
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);  
                utftext += String.fromCharCode((c & 63) | 128);  
            }  

        }  
        return utftext;  
    }  

    // private method for UTF-8 decoding  
    _utf8_decode = function (utftext) {  
        var string = "";  
        var i = 0;  
        var c = c1 = c2 = 0;  
        while ( i < utftext.length ) {  
            c = utftext.charCodeAt(i);  
            if (c < 128) {  
                string += String.fromCharCode(c);  
                i++;  
            } else if((c > 191) && (c < 224)) {  
                c2 = utftext.charCodeAt(i+1);  
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));  
                i += 2;  
            } else {  
                c2 = utftext.charCodeAt(i+1);  
                c3 = utftext.charCodeAt(i+2);  
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));  
                i += 3;  
            }  
        }  
        return string;  
    }  
}