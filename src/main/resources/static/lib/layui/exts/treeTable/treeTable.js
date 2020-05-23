layui.define(['jquery'], function(exports) {
	var MOD_NAME = 'treeTable',
		o = layui.jquery,
		tree = function() {};
	tree.prototype.config = function() {
		return {
			top_value: 0,
			primary_key: 'id',
			parent_key: 'pid',
			hide_class: 'layui-hide',
			icon: {
				open: 'layui-icon layui-icon-triangle-d',
				close: 'layui-icon layui-icon-triangle-r',
				left: 16,
			},
			cols: [],
			checked: {},
			is_click_icon: false,
			is_checkbox: false,
			is_cache: true,
		};
	}
	tree.prototype.template = function(e) {
		var t = this,
			level = [],
			tbody = '',
			is_table = o('table' + e.elem).length || !(e.is_click_icon = true),
			checkbox = e.is_checkbox ? '<div class="layui-unselect layui-form-checkbox cbx" lay-skin="primary"><i class="layui-icon layui-icon-ok"></i></div>' : '',
			checked = checkbox ? checkbox.replace('cbx', 'cbx layui-form-checked') : '',
			thead = checkbox && '<th style="width:28px;">' + (o.inArray(e.top_value, e.checked.data) > -1 ? checked : checkbox) + '</th>';
		o.each(t.data(e, e.data), function(idx, item) {
			var tr = '',
				is_checked = false,
				hide_class = (item[e.parent_key] == e.top_value) || (item[e.parent_key] == t.cache(e, item[e.parent_key])) ? '' : e.hide_class;
			// 设置每行数据层级
			item.level = level[item[e.primary_key]] = item[e.parent_key] != e.top_value ? (level[item[e.parent_key]] + 1) : 0;
			// 设置是否为最后一级
			item.is_end = !e.childs[item[e.primary_key]];
			o.each(e.cols, function(index, obj) {
				var style = '';
				obj.width && (style += 'width:' + obj.width + ';'), obj.align && (style += 'text-align:' + obj.align + ';'), style && (style = 'style="' + style + '"');
				// 标记设置行checkbox选中
				if(e.is_checkbox && e.checked && o.inArray(item[e.checked.key], e.checked.data) > -1) {
					is_checked = true;
				}
				// 第一次遍历头部的时候拼接表格头部
				idx || (thead += '<th ' + style + '>' + obj.title + '</th>');
				// 指定列加入开启、关闭小图标
				var icon = (obj.key == e.icon_key && !item.is_end) ? '<i class="' + (t.cache(e, item[e.primary_key]) ? e.icon.open : e.icon.close) + '"></i>' : '<span></span>';
				// 指定列小图标按照层级向后位移
				var left = (obj.key == e.icon_key ? level[item[e.primary_key]] * e.icon.left * 2 + 'px' : '');
				icon = icon.replace('>', ' style="margin-left:' + left + ';">');
				// 拼接行
				tr += '<td ' + style + (left ? 'data-down' : '') + '>' + icon + (is_table ? '' : (is_checked ? checked : checkbox)) + (obj.template ? obj.template(item) : item[obj.key]) + '</td>';
			});
			var box = is_table ? o(is_checked ? checked : checkbox).wrap('<td style="width:28px;">').parent().prop('outerHTML') : '';
			tbody += '<tr class="' + hide_class + '" data-id="' + item[e.primary_key] + '" data-pid="' + item[e.parent_key] + '">' + box + tr + '</tr>';
		});
		// 处理表树和树的赋值模板
		var table = is_table ? '<thead><tr data-id="' + e.top_value + '">' + thead + '</tr></thead><tbody>' + tbody + '</tbody>' : tbody.replace(/<tr/g, '<ul').replace(/tr>/g, 'ul>').replace(/<td/g, '<li').replace(/td>/g, 'li>');
		// 确认点击图标或点击列触发展开关闭
		var click_btn = e.is_click_icon ? '[data-down] i:not(.layui-icon-ok)' : '[data-down]';
		// 模板渲染并处理点击展开收起等功能
		o(e.elem).html(table).off('click', click_btn).on('click', click_btn, function() {
			var tr = o(this).parents('[data-id]'),
				td = tr.find('[data-down]'),
				id = tr.data('id'),
				pid = tr.data('pid'),
				is_open = (td.find('i:not(.layui-icon-ok)').attr('class') == e.icon.close);
			if(is_open) {
				// 展开子级（子级出现、更改图标）
				td.find('i:not(.layui-icon-ok)').attr('class', e.icon.open);
				td.parents(e.elem).find('[data-pid=' + id + ']').removeClass(e.hide_class);
				t.cache(e, id, true);
			} else {
				// 关闭子级（更改图标、隐藏所有子孙级）
				td.find('i:not(.layui-icon-ok)').attr('class', e.icon.close);
				t.childs_hide(e, id);
			}
			// 设置监听展开关闭
			layui.event.call(this, MOD_NAME, 'tree(flex)', {
				elem: this,
				item: e.childs[pid][id],
				table: e.elem,
				is_open: is_open,
			})
		}).off('click', '.cbx').on('click', '.cbx', function() {
			var is_checked = o(this).toggleClass('layui-form-checked').hasClass('layui-form-checked'),
				tr = o(this).parents('[data-id]'),
				id = tr.data('id'),
				pid = tr.data('pid');
			t.childs_checkbox(e, id, is_checked);
			t.parents_checkbox(e, pid);
			// 设置监听checkbox选择
			layui.event.call(this, MOD_NAME, 'tree(box)', {
				elem: this,
				item: pid === undefined ? {} : e.childs[pid][id],
				table: e.elem,
				is_checked: is_checked,
			})
		}).off('click', '[lay-filter]').on('click', '[lay-filter]', function() {
			var tr = o(this).parents('[data-id]'),
				id = tr.data('id'),
				pid = tr.data('pid'),
				filter = o(this).attr("lay-filter");
			return layui.event.call(this, MOD_NAME, 'tree(' + filter + ')', {
				elem: this,
				item: e.childs[pid][id],
			})
		});
		e.end && e.end(e);
	};
	// 同级全部选中父级选中/同级全部取消取消父级
	tree.prototype.parents_checkbox = function(e, pid) {
		var po = o(e.elem).find('[data-pid=' + pid + ']'),
			co = o(e.elem).find('[data-id=' + pid + ']'),
			len = o(e.elem).find('[data-pid=' + pid + '] .cbx.layui-form-checked').length;
		if(po.length == len || len == 0) {
			var pid = co.data('pid');
			len ? co.find('.cbx').addClass('layui-form-checked') : co.find('.cbx').removeClass('layui-form-checked');
			pid === undefined || this.parents_checkbox(e, pid);
		}
	};
	// 子级反选
	tree.prototype.childs_checkbox = function(e, id, is_checked) {
		var t = this;
		o(e.elem).find('[data-pid=' + id + ']').each(function() {
			var checkbox = o(this).find('.cbx');
			is_checked ? checkbox.addClass('layui-form-checked') : checkbox.removeClass('layui-form-checked');
			t.childs_checkbox(e, o(this).data('id'), is_checked);
		})
	};
	// 点击收起循环隐藏子级元素
	tree.prototype.childs_hide = function(e, id) {
		var t = this;
		t.cache(e, id, false);
		o(e.elem).find('[data-pid=' + id + ']:not(.' + e.hide_class + ')').each(function() {
			var td = o(this).find('[data-down]'),
				i = td.find('i:not(.layui-icon-ok)');
			// 关闭更换小图标
			i.length && i.attr('class', e.icon.close);
			// 隐藏子级
			td.parents(e.elem).find('[data-pid=' + id + ']').addClass(e.hide_class);
			t.childs_hide(e, o(this).data('id'))
		});
	};
	// 重新组合数据，父子级关系跟随
	tree.prototype.data = function(e) {
		var lists = [],
			childs = [];
		o.each(e.data, function(idx, item) {
			lists[item[e.primary_key]] = item;
			if(!childs[item[e.parent_key]]) {
				childs[item[e.parent_key]] = [];
			}
			childs[item[e.parent_key]][item[e.primary_key]] = item;
		});
		e.childs = childs;
		return this.tree_data(e, lists, e.top_value, []);
	};
	tree.prototype.tree_data = function(e, lists, pid, data) {
		var t = this;
		if(lists[pid]) {
			data.push(lists[pid]);
			delete lists[pid]
		}
		o.each(e.data, function(index, item) {
			if(item[e.parent_key] == pid) {
				data.concat(t.tree_data(e, lists, item[e.primary_key], data))
			}
		});
		return data;
	};
	tree.prototype.render = function(e) {
		var t = this;
		e = o.extend(t.config(), e);
		if(e.url) {
			o.get(e.url, function(res) {
				e.data = res;
				t.template(e);
			})
		} else {
			t.template(e);
		}
		return e;
	};
	// 获取已选值集合
	tree.prototype.checked = function(e) {
		var ids = [];
		o(e.elem).find('.cbx.layui-form-checked').each(function() {
			var id = o(this).parents('[data-id]').data('id');
			ids.push(id);
		})
		return ids;
	};
	// 全部展开
	tree.prototype.openAll = function(e) {
		var t = this;
		o.each(e.data, function(idx, item) {
			item[e.primary_key] && t.cache(e, item[e.primary_key], true);
		})
		t.render(e);
	}
	// 全部关闭
	tree.prototype.closeAll = function(e) {
		localStorage.setItem(e.elem.substr(1), '');
		this.render(e);
	}
	tree.prototype.on = function(events, callback) {
		return layui.onevent.call(this, MOD_NAME, events, callback)
	};
	// 存储折叠状态
	tree.prototype.cache = function(e, val, option) {
		if(!e.is_cache) {
			return false;
		}
		var t = this,
			name = e.elem.substr(1),
			val = val.toString(),
			cache = localStorage.getItem(name) ? localStorage.getItem(name).split(',') : [],
			index = o.inArray(val, cache);
		if(option === undefined) {
			return index == -1 ? false : val
		}
		if(option && index == -1) {
			cache.push(val)
		}
		if(!option && index > -1) {
			cache.splice(index, 1)
		}
		localStorage.setItem(name, cache.join(','));
	};
	var tree = new tree();
	exports(MOD_NAME, tree)
});