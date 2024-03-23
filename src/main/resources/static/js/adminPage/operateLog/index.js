function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function detail(id) {

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/operateLog/detail',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				diffUsingJS(data.obj.beforeConf, data.obj.afterConf);
			}
		},
		error: function() {

		}
	});
}

function delAll() {
	if (confirm(commonStr.confirmDel)) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/operateLog/delAll',
			data: {
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg)
				}
			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	}
}


function diffUsingJS(before, after) {
	// get the baseText and newText values from the two textboxes, and split them into lines
	var base = difflib.stringAsLines(before);
	var newtxt = difflib.stringAsLines(after);

	// create a SequenceMatcher instance that diffs the two sets of lines
	var sm = new difflib.SequenceMatcher(base, newtxt);

	// get the opcodes from the SequenceMatcher instance
	// opcodes is a list of 3-tuples describing what changes should be made to the base text
	// in order to yield the new text
	var opcodes = sm.get_opcodes();
	var diffoutputdiv = $("#diffoutput");
	while (diffoutputdiv.firstChild) {
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
		baseTextName: operateLogStr.base,
		newTextName: operateLogStr.next,
		//contextSize: contextSize,
		viewType: 1
	}));

	// scroll down to the diff view window.
	layer.open({
		type: 1,
		title: false,
		area: ['1000px', '90%'], //宽高
		content: $('#diffoutput')
	});
}