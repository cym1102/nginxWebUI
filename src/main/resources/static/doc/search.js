let api = [];
api.push({
    alias: 'api',
    order: '1',
    desc: '基础参数接口',
    link: '基础参数接口',
    list: []
})
api[0].list.push({
    order: '1',
    desc: '获取Http参数',
});
api[0].list.push({
    order: '2',
    desc: '添加或编辑Http参数',
});
api[0].list.push({
    order: '3',
    desc: '删除Http参数',
});
api[0].list.push({
    order: '4',
    desc: '获取基础参数',
});
api[0].list.push({
    order: '5',
    desc: '添加或编辑基础参数',
});
api[0].list.push({
    order: '6',
    desc: '删除基础参数',
});
api[0].list.push({
    order: '7',
    desc: '获取Stream参数',
});
api[0].list.push({
    order: '8',
    desc: '添加或编辑Stream参数',
});
api[0].list.push({
    order: '9',
    desc: '删除Stream参数',
});
api.push({
    alias: 'CertApiController',
    order: '2',
    desc: '证书接口',
    link: '证书接口',
    list: []
})
api[1].list.push({
    order: '1',
    desc: '获取证书分页列表',
});
api[1].list.push({
    order: '2',
    desc: '添加或编辑证书',
});
api[1].list.push({
    order: '3',
    desc: '获取域名解析码',
});
api[1].list.push({
    order: '4',
    desc: '设置证书自动续签',
});
api[1].list.push({
    order: '5',
    desc: '删除证书',
});
api[1].list.push({
    order: '6',
    desc: '执行申请',
});
api[1].list.push({
    order: '7',
    desc: '下载证书文件',
});
api.push({
    alias: 'DenyAllowApiController',
    order: '3',
    desc: 'IP黑白名单接口',
    link: 'ip黑白名单接口',
    list: []
})
api[2].list.push({
    order: '1',
    desc: '获取全部IP黑白名单列表',
});
api[2].list.push({
    order: '2',
    desc: '添加或编辑IP黑白名单',
});
api[2].list.push({
    order: '3',
    desc: '删除IP黑白名单',
});
api.push({
    alias: 'NginxApiController',
    order: '4',
    desc: 'nginx接口',
    link: 'nginx接口',
    list: []
})
api[3].list.push({
    order: '1',
    desc: '获取nginx状态',
});
api[3].list.push({
    order: '2',
    desc: '替换conf文件',
});
api[3].list.push({
    order: '3',
    desc: '效验conf文件',
});
api[3].list.push({
    order: '4',
    desc: '重载conf文件',
});
api[3].list.push({
    order: '5',
    desc: '获取nginx启动命令',
});
api[3].list.push({
    order: '6',
    desc: '获取nginx停止命令',
});
api[3].list.push({
    order: '7',
    desc: '执行nginx命令',
});
api.push({
    alias: 'ParamApiController',
    order: '5',
    desc: '额外参数接口',
    link: '额外参数接口',
    list: []
})
api[4].list.push({
    order: '1',
    desc: '根据项目获取参数列表',
});
api[4].list.push({
    order: '2',
    desc: '添加或编辑参数',
});
api[4].list.push({
    order: '3',
    desc: '删除额外参数',
});
api.push({
    alias: 'PasswordApiController',
    order: '6',
    desc: '密码文件接口',
    link: '密码文件接口',
    list: []
})
api[5].list.push({
    order: '1',
    desc: '获取全部密码文件列表',
});
api[5].list.push({
    order: '2',
    desc: '添加或编辑密码文件',
});
api[5].list.push({
    order: '3',
    desc: '删除密码文件',
});
api.push({
    alias: 'ServerApiController',
    order: '7',
    desc: '反向代理(server)接口',
    link: '反向代理(server)接口',
    list: []
})
api[6].list.push({
    order: '1',
    desc: '获取server分页列表',
});
api[6].list.push({
    order: '2',
    desc: '添加或编辑server',
});
api[6].list.push({
    order: '3',
    desc: '删除server',
});
api[6].list.push({
    order: '4',
    desc: '根据serverId获取location列表',
});
api[6].list.push({
    order: '5',
    desc: '添加或编辑location',
});
api[6].list.push({
    order: '6',
    desc: '删除location',
});
api.push({
    alias: 'TokenController',
    order: '8',
    desc: '获取token',
    link: '获取token',
    list: []
})
api[7].list.push({
    order: '1',
    desc: '获取Token',
});
api.push({
    alias: 'UploadController',
    order: '9',
    desc: '文件上传接口',
    link: '文件上传接口',
    list: []
})
api[8].list.push({
    order: '1',
    desc: '文件上传',
});
api.push({
    alias: 'UpstreamApiController',
    order: '10',
    desc: '负载均衡(upstream)接口',
    link: '负载均衡(upstream)接口',
    list: []
})
api[9].list.push({
    order: '1',
    desc: '获取upstream分页列表',
});
api[9].list.push({
    order: '2',
    desc: '添加或编辑upstream',
});
api[9].list.push({
    order: '3',
    desc: '删除upstream',
});
api[9].list.push({
    order: '4',
    desc: '根据upstreamId获取server列表',
});
api[9].list.push({
    order: '5',
    desc: '添加或编辑server',
});
document.onkeydown = keyDownSearch;
function keyDownSearch(e) {
    const theEvent = e;
    const code = theEvent.keyCode || theEvent.which || theEvent.charCode;
    if (code === 13) {
        const search = document.getElementById('search');
        const searchValue = search.value;
        let searchArr = [];
        for (let i = 0; i < api.length; i++) {
            let apiData = api[i];
            const desc = apiData.desc;
            if (desc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                searchArr.push({
                    order: apiData.order,
                    desc: apiData.desc,
                    link: apiData.link,
                    alias: apiData.alias,
                    list: apiData.list
                });
            } else {
                let methodList = apiData.list || [];
                let methodListTemp = [];
                for (let j = 0; j < methodList.length; j++) {
                    const methodData = methodList[j];
                    const methodDesc = methodData.desc;
                    if (methodDesc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                        methodListTemp.push(methodData);
                        break;
                    }
                }
                if (methodListTemp.length > 0) {
                    const data = {
                        order: apiData.order,
                        desc: apiData.desc,
                        alias: apiData.alias,
                        link: apiData.link,
                        list: methodListTemp
                    };
                    searchArr.push(data);
                }
            }
        }
        let html;
        if (searchValue === '') {
            const liClass = "";
            const display = "display: none";
            html = buildAccordion(api,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        } else {
            const liClass = "open";
            const display = "display: block";
            html = buildAccordion(searchArr,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        }
        const Accordion = function (el, multiple) {
            this.el = el || {};
            this.multiple = multiple || false;
            const links = this.el.find('.dd');
            links.on('click', {el: this.el, multiple: this.multiple}, this.dropdown);
        };
        Accordion.prototype.dropdown = function (e) {
            const $el = e.data.el;
            let $this = $(this), $next = $this.next();
            $next.slideToggle();
            $this.parent().toggleClass('open');
            if (!e.data.multiple) {
                $el.find('.submenu').not($next).slideUp("20").parent().removeClass('open');
            }
        };
        new Accordion($('#accordion'), false);
    }
}

function buildAccordion(apiData, liClass, display) {
    let html = "";
    if (apiData.length > 0) {
         for (let j = 0; j < apiData.length; j++) {
            html += '<li class="'+liClass+'">';
            html += '<a class="dd" href="' + apiData[j].alias + '.html#header">' + apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
            html += '<ul class="sectlevel2" style="'+display+'">';
            let doc = apiData[j].list;
            for (let m = 0; m < doc.length; m++) {
                html += '<li><a href="' + apiData[j].alias + '.html#_' + apiData[j].order + '_' + doc[m].order + '_' + doc[m].desc + '">' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + doc[m].desc + '</a> </li>';
            }
            html += '</ul>';
            html += '</li>';
        }
    }
    return html;
}