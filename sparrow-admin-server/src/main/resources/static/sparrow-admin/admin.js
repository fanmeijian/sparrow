const $ = id => document.getElementById(id);
let session, active, generation = 0;
const modules = [['users','用户'],['roles','角色与功能授权'],['groups','群组'],['policies','数据策略'],['dictionaries','字典'],['files','文件'],['audit','管理审计'],['ui-pages','页面元素'],['configurations','配置'],['navigation','导航'],['trees','层级结构']];
const can = (module, scope) => session.permissions.some(p => p.resource === `admin.${module}` && p.scope === scope);
const notice = (message, error = false) => { $('notice').textContent = message; $('notice').className = error ? 'error' : ''; };
const node = (tag, text) => { const n = document.createElement(tag); if (text !== undefined) n.textContent = text; return n; };
async function request(path, method = 'GET', data) {
  const headers = { Accept: 'application/json' };
  if (method !== 'GET' && session?.csrf?.header) headers[session.csrf.header] = session.csrf.token;
  let body;
  if (data instanceof FormData) body = data;
  else if (data !== undefined) { headers['Content-Type'] = 'application/json'; body = JSON.stringify(data); }
  const response = await fetch(path, { method, headers, body, credentials: 'same-origin', cache: 'no-store' });
  if (response.status === 401 || response.redirected && new URL(response.url).pathname === '/login') {
    location.assign('/login'); throw new Error('会话已失效，请重新登录');
  }
  if (!response.ok) {
    let code; try { code = (await response.json()).code; } catch { code = ''; }
    const messages = {ACCESS_DENIED:'没有此操作的权限', STALE_VERSION:'数据已变更，请刷新后重新操作', INVALID_REQUEST:'提交内容不符合要求', OPERATION_UNAVAILABLE:'当前状态不支持此操作，或引用检查未通过'};
    throw new Error(messages[code] || `请求失败（${response.status}）`);
  }
  const text = await response.text(); return text ? JSON.parse(text) : null;
}
function button(text, action, enabled = true) {
  const b = node('button', text); b.type = 'button'; b.disabled = !enabled;
  b.addEventListener('click', async () => { b.disabled = true; try { await action(); } catch(e) { notice(e.message, true); } finally { b.disabled = !enabled; } }); return b;
}
function form(title, fields, action, enabled = true, submitText = '保存') {
  const f = node('form'); f.append(node('h2', title)); const inputs = {};
  for (const field of fields) {
    const [name, label, type = 'text', value = '', options] = field;
    const l = node('label', label); const input = node(type === 'textarea' ? 'textarea' : type === 'select' ? 'select' : 'input');
    if (type !== 'textarea' && type !== 'select') input.type = type;
    if (options) for (const option of options) { const o = node('option', option); o.value = option; input.append(o); }
    input.name = name; input.value = value; input.required = true; input.disabled = !enabled;
    if (type === 'password') input.autocomplete = 'new-password';
    if (type === 'number') { input.min = '0'; input.step = '1'; }
    l.append(input); f.append(l); inputs[name] = input;
  }
  const submit = node('button', submitText); submit.type = 'submit'; submit.className = 'primary'; submit.disabled = !enabled; f.append(submit);
  f.addEventListener('submit', async e => { e.preventDefault(); if (!enabled || submit.disabled) return; submit.disabled = true;
    try { const values = {}; for (const [key,input] of Object.entries(inputs)) values[key] = input.type === 'number' ? Number(input.value) : input.type === 'file' ? input.files[0] : input.value;
      await action(values); notice('操作成功'); for (const input of Object.values(inputs)) if (input.type === 'password') input.value = '';
    } catch(e) { notice(e.message, true); } finally { submit.disabled = !enabled; }
  }); return f;
}
function table(rows, columns, actions) {
  const wrap = node('div'); wrap.className = 'table-wrap'; const t = node('table'), head = node('tr');
  for (const [key,label] of columns) head.append(node('th',label)); if (actions) head.append(node('th','操作')); t.append(head);
  for (const row of rows) { const tr = node('tr'); for (const [key] of columns) tr.append(node('td', String(row[key] ?? '')));
    if (actions) { const td = node('td'); for (const b of actions(row)) td.append(b); tr.append(td); } t.append(tr); }
  wrap.append(t); if (!rows.length) wrap.append(node('p','暂无数据')); return wrap;
}
const id = value => encodeURIComponent(value);
async function refreshNavigation(){const items=await request('/api/navigation');$('application-links').replaceChildren();for(const item of items){const link=node('a',item.title);link.href=item.href;link.style.display='block';link.style.padding='8px 12px';$('application-links').append(link);}}
async function show(module, offset = 0) {
  active = module; const ticket = ++generation; notice('正在加载…'); $('heading').textContent = modules.find(m => m[0] === module)[1]; $('content').replaceChildren();
  for (const b of $('navigation').children) b.setAttribute('aria-current', b.dataset.module === module ? 'page' : 'false');
  const content = node('div'), manage = can(module,'manage'), base = `/api/admin/${module}`;
  const reload = () => show(module, offset);
  try {
    if (module === 'users') {
      const capabilities=await request(`${base}/capabilities`);const supports=c=>manage&&capabilities.includes(c);
      const rows = await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows,[['username','登录名'],['displayName','名称'],['enabled','启用'],['id','主体 ID'],['version','版本']],r=>[
        button(r.enabled?'停用':'启用',async()=>{await request(`${base}/${id(r.id)}/enabled`,'PUT',{version:r.version,enabled:!r.enabled});await reload();},supports('SET_ENABLED')),
        button('编辑',()=>{content.append(form(`编辑 ${r.username}`,[['displayName','名称','text',r.displayName]],async v=>{await request(`${base}/${id(r.id)}`,'PUT',{...v,version:r.version});await reload();},supports('UPDATE')));},supports('UPDATE')),
        button('重置密码',()=>{content.append(form(`重置 ${r.username} 的密码`,[['password','新密码（至少 12 字符）','password']],async v=>{await request(`${base}/${id(r.id)}/password`,'PUT',{...v,version:r.version});await reload();},supports('RESET_PASSWORD')));},supports('RESET_PASSWORD'))]));
      content.append(form('创建用户',[['username','登录名'],['displayName','名称'],['password','初始密码','password']],async v=>{await request(base,'POST',v);await reload();},supports('CREATE')));
      pager(content,rows.length,offset,module);
    } else if (module === 'roles') {
      const rows = await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows,[['code','编码'],['name','名称'],['enabled','启用'],['id','角色 ID']],r=>[button('查看授权',async()=>{
        const members=await request(`${base}/${id(r.id)}/members`),grants=await request(`${base}/${id(r.id)}/permissions`);const card=node('section');card.className='card';card.append(node('h2',r.name));
        card.append(table(members,[['kind','主体类型'],['id','主体 ID']],m=>[button('撤销成员',async()=>{await request(`${base}/${id(r.id)}/members`,'DELETE',{kind:m.kind,subject:m.id});await reload();},manage)]));
        card.append(table(grants.map(g=>({...g,...g.permission})),[['resource','资源'],['scope','scope'],['effect','效果']],g=>[button('撤销授权',async()=>{await request(`${base}/${id(r.id)}/permissions`,'DELETE',g);await reload();},manage)]));content.append(card);
      }),button(r.enabled?'停用':'启用',async()=>{await request(`${base}/${id(r.id)}/enabled`,'PUT',{version:r.version,enabled:!r.enabled});await reload();},manage)]));
      content.append(form('创建角色',[['code','编码'],['name','名称']],async v=>{await request(base,'POST',v);await reload();},manage));
      content.append(form('角色成员',[['role','角色 ID'],['kind','主体类型','select','USER',['USER','GROUP']],['subject','主体 ID（含命名空间）'],['operation','操作','select','assign',['assign','revoke']]],async v=>{await request(`${base}/${id(v.role)}/members`,v.operation==='assign'?'POST':'DELETE',{kind:v.kind,subject:v.subject});},manage));
      content.append(form('功能授权',[['role','角色 ID'],['resource','资源编码'],['scope','scope'],['effect','效果','select','ALLOW',['ALLOW','DENY']],['operation','操作','select','grant',['grant','revoke']]],async v=>{await request(`${base}/${id(v.role)}/permissions`,v.operation==='grant'?'PUT':'DELETE',{resource:v.resource,scope:v.scope,effect:v.effect});},manage));
      pager(content,rows.length,offset,module);
    } else if (module === 'groups') {
      const snapshot = await request(base), revision = snapshot.revision;
      content.append(table(Object.values(snapshot.graph.nodes),[['id','群组 ID'],['name','名称'],['enabled','启用']],r=>[button(r.enabled?'停用':'启用',async()=>{await request(`${base}/state`,'PUT',{revision,group:r.id,enabled:!r.enabled});await reload();},manage),button('删除',async()=>{await request(`${base}/${id(r.id)}?revision=${revision}`,'DELETE');await reload();},manage)]));
      content.append(form('创建群组',[['code','编码'],['name','名称']],async v=>{await request(base,'POST',{revision,...v});await reload();},manage));
      content.append(table(snapshot.graph.memberships,[['group','群组'],['subject','成员']]));
      content.append(form('群组成员',[['group','群组 ID'],['subject','主体 ID'],['operation','操作','select','add',['add','remove']]],async v=>{await request(`${base}/members`,v.operation==='add'?'POST':'DELETE',{revision,group:v.group,subject:v.subject});await reload();},manage));
      content.append(table(snapshot.graph.edges,[['parent','父群组'],['child','子群组']]));
      content.append(form('嵌套关系',[['parent','父群组 ID'],['child','子群组 ID'],['operation','操作','select','add',['add','remove']]],async v=>{await request(`${base}/edges`,v.operation==='add'?'POST':'DELETE',{revision,parent:v.parent,child:v.child});await reload();},manage));
    } else if (module === 'policies') {
      content.append(node('p','数据许可需同时满足功能授权。草稿保存后，必须发布才生效。'));
      content.append(form('加载或新建策略',[['resource','资源编码','text','file.object'],['scope','scope','select','read',['read','download','update','delete']]],async key=>{
        const query = `?resource=${id(key.resource)}&scope=${id(key.scope)}`; const policy = await request(base+query);
        const editor = form('策略规则（JSON）',[['document','规则列表','textarea',policy.document]],async v=>{JSON.parse(v.document);const saved=await request(base+query,'PUT',{version:policy.version,document:v.document});policy.version=saved.version;policy.document=saved.document;},manage,'保存草稿');
        editor.append(button('发布',async()=>{if(policy.version===null)throw new Error('请先保存草稿');const saved=await request(`${base}/publish${query}`,'POST',{version:policy.version});policy.version=saved.version;notice('已发布');},manage));
        editor.append(button('撤销发布',async()=>{await request(`${base}/retire${query}`,'POST',{version:policy.version});await show(module);},manage));
        const panel=node('section');panel.append(editor);
        panel.append(form('添加规则',[['kind','主体类型','select','ROLE',['ROLE','USER','GROUP']],['subject','主体 ID'],['effect','效果','select','ALLOW',['ALLOW','DENY']],['target','目标','select','ROW',['ROW','FIELD_READ','FIELD_WRITE']],['fields','字段（行规则填 -）','text','-'],['op','行条件','select','all',['all','none','eq']],['field','条件字段（无条件填 -）','text','-'],['valueType','值类型','select','string',['string','int','long','decimal','boolean','null']],['value','条件值（无条件填 -）','text','-']],async v=>{
          const area=editor.querySelector('textarea[name=document]');const rules=JSON.parse(area.value);if(!Array.isArray(rules))throw new Error('规则必须为列表');
          const condition=v.op==='eq'?{op:'eq',field:v.field,value:v.valueType==='null'?{type:'null'}:{type:v.valueType,value:v.value}}:{op:v.op};
          rules.push({kind:v.kind,subject:v.subject,resource:key.resource,scope:key.scope,effect:v.effect,target:v.target,fields:v.target==='ROW'?[]:v.fields.split(',').map(s=>s.trim()).filter(Boolean),condition});area.value=JSON.stringify(rules,null,2);
        },manage,'加入草稿'));
        panel.append(form('预览已发布策略',[['subject','用户主体 ID'],['recordId','文件 ID']],async v=>{
          const result=await request('/api/admin/policies/preview','POST',{...v,scope:key.scope});const summary=node('pre',JSON.stringify(result,null,2));content.append(summary);
        },true,'预览'));
        content.append(panel);
      },true,'加载'));
    } else if (module === 'dictionaries') {
      const rows=await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows,[['code','编码'],['name','名称']],r=>[button('条目',async()=>{
        const path=`${base}/${id(r.code)}/entries`,entries=await request(path);const card=node('section');card.className='card';card.append(node('h2',r.name));
        card.append(table(entries,[['code','编码'],['label','名称'],['enabled','启用']],e=>[button(e.enabled?'停用':'启用',async()=>{await request(`${path}/${id(e.id)}`,'PUT',{version:e.version,label:e.label,order:e.order,enabled:!e.enabled});await reload();},manage)]));
        card.append(form('添加条目',[['code','编码'],['label','名称'],['order','排序','number','0']],async v=>{await request(path,'POST',v);await reload();},manage));content.append(card);
      })]));
      content.append(form('创建字典',[['code','编码'],['name','名称']],async v=>{await request(base,'POST',v);await reload();},manage));pager(content,rows.length,offset,module);
    } else if (module === 'trees') {
      const rows=await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows,[['name','名称'],['mode','模式'],['id','层级 ID'],['revision','版本']],r=>[button('管理节点',async()=>{
        const path=`${base}/${id(r.id)}`,snapshot=await request(path),revision=snapshot.revision,panel=node('section');panel.className='card';panel.append(node('h2',r.name));
        panel.append(table(Object.values(snapshot.graph.nodes),[['id','节点 ID'],['rootOrder','根排序']],n=>[
          button('删除节点',async()=>{await request(`${path}/nodes/${id(n.id)}?revision=${revision}`,'DELETE');await reload();},manage),
          button('删除子树',async()=>{if(!confirm('将删除该节点及符合删除规则的全部后代，是否继续？'))return;await request(`${path}/subtrees/${id(n.id)}?revision=${revision}`,'DELETE');await reload();},manage)]));
        panel.append(table(snapshot.graph.edges,[['parent','父节点'],['child','子节点'],['sortOrder','排序']]));
        panel.append(form('添加根节点',[['order','排序','text','0']],async v=>{await request(`${path}/nodes`,'POST',{revision,resource:null,recordId:null,order:v.order});await reload();},manage));
        panel.append(form('关联或解除关联',[['parent','父节点 ID'],['child','子节点 ID'],['order','排序','text','0'],['operation','操作','select','link',['link','detach']]],async v=>{await request(`${path}/edges`,v.operation==='link'?'POST':'DELETE',{revision,parent:v.parent,child:v.child,order:v.order});await reload();},manage));
        panel.append(form('原子移动',[['oldParent','原父 ID（根填 -）','text','-'],['newParent','新父 ID（根填 -）','text','-'],['child','节点 ID'],['order','排序','text','0']],async v=>{await request(`${path}/move`,'PUT',{revision,oldParent:v.oldParent==='-'?null:v.oldParent,newParent:v.newParent==='-'?null:v.newParent,child:v.child,order:v.order});await reload();},manage));content.append(panel);
      })]));
      content.append(form('创建层级',[['name','名称'],['mode','模式','select','TREE',['TREE','DAG']]],async v=>{await request(base,'POST',v);await reload();},manage));pager(content,rows.length,offset,module);
    } else if (module === 'navigation') {
      const snapshot=await request(base);const save=async items=>{await request(base,'PUT',{version:snapshot.version,items});await refreshNavigation();await reload();};
      content.append(table(snapshot.items,[['code','编码'],['parent','父项'],['title','名称'],['href','地址'],['enabled','启用']],r=>[
        button(r.enabled?'停用':'启用',()=>save(snapshot.items.map(i=>i.code===r.code?{...i,enabled:!i.enabled}:i)),manage),
        button('删除',()=>save(snapshot.items.filter(i=>i.code!==r.code)),manage)]));
      content.append(form('添加导航项',[['code','编码'],['parent','父编码（无则 -）','text','-'],['title','名称'],['href','应用内路径','text','/'],['sortOrder','排序','text','0'],['resource','可见资源（公开填 -）'],['scope','scope','text','read']],async v=>{
        await save([...snapshot.items,{code:v.code,parent:v.parent==='-'?null:v.parent,title:v.title,href:v.href,sortOrder:v.sortOrder,enabled:true,publicVisible:v.resource==='-',permission:v.resource==='-'?null:{resource:v.resource,scope:v.scope}}]);
      },manage));
    } else if (module === 'configurations') {
      const rows=await request(base);
      for(const row of rows){const editor=form(`${row.code} · ${row.kind}`,[['value','配置值','textarea',row.value]],async v=>{await request(`${base}/${id(row.code)}`,'PUT',{version:row.version,value:v.value});await reload();},manage);
        if(!row.defaulted)editor.append(button('恢复默认',async()=>{await request(`${base}/${id(row.code)}?version=${row.version}`,'DELETE');await reload();},manage));content.append(editor);}
    } else if (module === 'ui-pages') {
      const pages=await request(base);content.append(table(pages.map(code=>({code})),[['code','页面编码']]));
      content.append(form('编辑页面',[['page','页面编码']],async v=>{
        const path=`${base}/${id(v.page)}`,snapshot=await request(path),panel=node('section');
        const editor=form('元素与呈现策略',[['definition','定义','textarea',JSON.stringify(snapshot.definition,null,2)]],async input=>{
          const saved=await request(path,'PUT',{version:snapshot.version,definition:JSON.parse(input.definition)});snapshot.version=saved.version;
        },manage);panel.append(editor);
        panel.append(form('添加元素',[['code','元素编码'],['parent','父元素（无则 -）','text','-'],['kind','类型','select','BUTTON',['BUTTON','FIELD','PANEL']],['viewResource','读取资源（公开填 -）'],['viewScope','读取 scope','text','read'],['actionResource','操作资源（无则 -）'],['actionScope','操作 scope','text','manage'],['field','字段（无则 -）','text','-'],['mode','呈现方式','select','DERIVED',['DERIVED','RESTRICTED']],['denied','无操作权限时','select','disable',['disable','hide']]],async input=>{
          const area=editor.querySelector('textarea'),definition=JSON.parse(area.value);
          definition.elements.push({code:input.code,parent:input.parent==='-'?null:input.parent,kind:input.kind,publicVisible:input.viewResource==='-',view:input.viewResource==='-'?null:{resource:input.viewResource,scope:input.viewScope},action:input.actionResource==='-'?null:{resource:input.actionResource,scope:input.actionScope},field:input.field==='-'?null:input.field,mode:input.mode,hideWhenDenied:input.denied==='hide'});area.value=JSON.stringify(definition,null,2);
        },manage,'加入定义'));content.append(panel);
      },true,'加载'));
    } else if (module === 'audit') {
      const rows=await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows.map(r=>({...r,occurredAt:new Date(r.occurredAt).toLocaleString()})),[['occurredAt','时间'],['subject','主体'],['operation','操作'],['target','目标'],['outcome','结果']]));pager(content,rows.length,offset,module);
    } else if (module === 'files') {
      const rows=await request(`${base}?offset=${offset}&limit=50`);
      content.append(table(rows,[['name','名称'],['size','字节数'],['state','状态'],['id','文件 ID']],r=>[
        button('下载',()=>{location.assign(`${base}/${id(r.id)}/content`);},r.state==='READY'),
        button('引用',async()=>{const rows=await request(`${base}/${id(r.id)}/references`);content.append(table(rows,[['resource','业务资源'],['recordId','记录'],['slot','位置']]));}),
        button(r.state==='DELETE_PENDING'?'执行删除':'标记删除',async()=>{if(r.state==='DELETE_PENDING')await request(`${base}/${id(r.id)}/complete-delete`,'POST');else await request(`${base}/${id(r.id)}?version=${r.version}`,'DELETE');await reload();},manage&&r.state!=='DELETED'),
        button('重命名',()=>{content.append(form('重命名文件',[['name','名称','text',r.name]],async v=>{await request(`${base}/${id(r.id)}`,'PUT',{version:r.version,...v});await reload();},manage));},manage)]));
      content.append(form('上传文件',[['file','文件','file']],async v=>{const data=new FormData();data.append('file',v.file);await request(base,'POST',data);await reload();},manage,'上传'));
      pager(content,rows.length,offset,module);
    }
    if(ticket===generation){$('content').replaceChildren(content);notice('已加载');}
  } catch(e) { if(ticket===generation)notice(e.message,true); }
}
function pager(content,count,offset,module){const p=node('div');p.className='pager';p.append(button('上一页',()=>show(module,Math.max(0,offset-50)),offset>0),node('span',`第 ${offset/50+1} 页`),button('下一页',()=>show(module,offset+50),count===50));content.append(p);}
async function start(){try{session=await request('/api/session');$('identity').textContent=session.subject;const configuration=await request('/api/configuration');document.querySelector('header strong').textContent=configuration['ui.brand']||'Sparrow 管理';$('logout').disabled=false;await refreshNavigation();
  $('logout').addEventListener('click',async()=>{const data=new URLSearchParams();data.set('_csrf',session.csrf.token);await fetch('/logout',{method:'POST',body:data,credentials:'same-origin'});location.assign('/login');});
  for(const [module,label] of modules)if(can(module,'read')){const b=button(label,()=>show(module));b.dataset.module=module;$('navigation').append(b);}
  if($('navigation').children.length)await show($('navigation').firstChild.dataset.module);else notice('当前账号没有管理权限');
}catch(e){notice(e.message,true);}}
start();
