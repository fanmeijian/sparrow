package cn.sparrowmini.common.zk.view;

import org.zkoss.formbuilder.FormField;
import org.zkoss.formbuilder.FormModel;
import org.zkoss.formbuilder.FormNode;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Div;

import java.util.ArrayList;

public class AbstractToZulComposer extends SelectorComposer<Component> {

	private FormModel formModel;
	
	@Wire
	private Div host;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		buildFormModel();
		FormHelper.showUseInput();
	}

	/**
	 * assume you already have a form structure in mind.
	 */
	private void buildFormModel() {
		formModel = new FormModel();
		formModel.add(new FormField("name", "shortText", "name"));
		formModel.add(new FormField("code", "shortText", "code"));
	}

	@Listen("onClick=#buildZulFromAbstract")
	public void buildZulFromAbstract() {
		String zulData = formModel.toZul();

		Components.removeAllChildren(host);
		Executions.createComponentsDirectly(zulData, null, host, null);
	}
}
