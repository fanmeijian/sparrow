import test from 'node:test';
import assert from 'node:assert/strict';
import {applyCapabilities,refreshCapabilities} from '../src/main/resources/static/sparrow-admin/ui-capabilities.js';
class Element {
  constructor(code){this.dataset={sparrowElement:code};this.style={};this.attributes={};this.events={};this.disabled=false;this.readOnly=false;}
  setAttribute(key,value){this.attributes[key]=value;}
  getAttribute(key){return this.attributes[key]??null;}
  matches(){return true;}
  addEventListener(type,callback){this.events[type]=callback;}
}
const container=(...elements)=>({querySelectorAll:()=>elements});
test('hidden and disabled controls reject keyboard and click',()=>{
  const e=new Element('save'),root=container(e);applyCapabilities(root);assert.equal(e.hidden,true);assert.equal(e.disabled,true);
  applyCapabilities(root,{save:{visible:true,enabled:false,readOnly:false}});assert.equal(e.hidden,false);assert.equal(e.disabled,true);
  let prevented=false;e.events.keydown({key:'Enter',preventDefault(){prevented=true},stopImmediatePropagation(){}});assert.equal(prevented,true);
  applyCapabilities(root,{save:{visible:true,enabled:true,readOnly:false}});assert.equal(e.disabled,false);assert.equal(e.getAttribute('tabindex'),'0');
});
test('read-only fields cannot be edited but retain their current display',()=>{
  const e=new Element('name');applyCapabilities(container(e),{name:{visible:true,enabled:false,readOnly:true}});
  assert.equal(e.readOnly,true);assert.equal(e.disabled,false);assert.equal(e.hidden,false);
});
test('failed refresh immediately hides previous grants',async()=>{
  const e=new Element('save'),root=container(e);applyCapabilities(root,{save:{visible:true,enabled:true}});
  globalThis.fetch=async()=>({ok:false});await assert.rejects(refreshCapabilities(root,'page',null,{}));assert.equal(e.hidden,true);
});
test('an older allowed response cannot override a newer denial',async()=>{
  const e=new Element('save'),root=container(e);let resolveOld;
  globalThis.fetch=()=>new Promise(resolve=>{resolveOld=resolve;});const old=refreshCapabilities(root,'page',null,{});
  globalThis.fetch=async()=>({ok:true,json:async()=>({save:{visible:false,enabled:false}})});await refreshCapabilities(root,'page',null,{});
  resolveOld({ok:true,json:async()=>({save:{visible:true,enabled:true}})});await old;assert.equal(e.hidden,true);
});
