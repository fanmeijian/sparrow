// Install before enabling business controls. Backend operations remain independently authorized.
const guarded = new WeakSet();
const generations = new WeakMap();
export function applyCapabilities(container, decisions = {}) {
  for (const element of container.querySelectorAll('[data-sparrow-element]')) {
    const state = decisions[element.dataset.sparrowElement] || {visible:false,enabled:false,readOnly:false};
    element.hidden = !state.visible;
    element.style.visibility = state.visible ? 'visible' : 'hidden';
    const blocked = !state.visible || !state.enabled;
    element.setAttribute('aria-disabled', String(blocked));
    if ('disabled' in element) element.disabled = blocked && !state.readOnly;
    if ('readOnly' in element) element.readOnly = !!state.readOnly;
    if (element.matches('a, [role=button]')) {
      if (!element.dataset.sparrowOriginalTabindex) element.dataset.sparrowOriginalTabindex = element.getAttribute('tabindex') ?? '0';
      element.setAttribute('tabindex',blocked?'-1':element.dataset.sparrowOriginalTabindex);
    }
    if (!guarded.has(element)) {
      const prevent = event => { if (element.getAttribute('aria-disabled') === 'true' && (event.type === 'click' || ['Enter',' '].includes(event.key))) {event.preventDefault();event.stopImmediatePropagation();} };
      element.addEventListener('click',prevent,true);element.addEventListener('keydown',prevent,true);guarded.add(element);
    }
  }
}
export async function refreshCapabilities(container, page, recordId, csrf) {
  const generation=(generations.get(container)||0)+1;generations.set(container,generation);
  applyCapabilities(container);
  const elements=[...new Set([...container.querySelectorAll('[data-sparrow-element]')].map(e=>e.dataset.sparrowElement))];
  if (elements.length>200) throw new Error('Element batch limit');
  const headers={'Content-Type':'application/json','Accept':'application/json'};
  if(csrf?.header)headers[csrf.header]=csrf.token;
  const response=await fetch('/api/ui/capabilities',{method:'POST',headers,credentials:'same-origin',cache:'no-store',body:JSON.stringify({page,elements,recordId})});
  if(!response.ok||response.redirected)throw new Error('Capability evaluation unavailable');
  const decisions=await response.json();
  if(generations.get(container)===generation)applyCapabilities(container,decisions);
}
