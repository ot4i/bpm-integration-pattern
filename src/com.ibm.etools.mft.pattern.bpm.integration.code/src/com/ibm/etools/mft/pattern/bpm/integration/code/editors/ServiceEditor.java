/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2012" 
 *  crc="2178871932" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corporation 2012, 2015
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.pattern.bpm.integration.code.editors;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ibm.broker.config.appdev.patterns.ui.BasePatternPropertyEditor;
import com.ibm.broker.config.appdev.patterns.ui.PatternPropertyEditorSite;
import com.ibm.etools.mft.pattern.bpm.integration.code.Messages;

public class ServiceEditor extends BasePatternPropertyEditor implements SelectionListener{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2012, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	private Text text;
	private String configurationValue;
	private Composite container;
	private Button configureButton;
	private String option;
	private String patternInstanceName;
	
	@Override
	public void configureEditor(PatternPropertyEditorSite site, boolean required, String configurationValue) {
		super.configureEditor(site, required, configurationValue);
		this.configurationValue = configurationValue;
		this.patternInstanceName = site.getPatternInstanceName();
	}

	@Override
	public void createControls(Object parent) {
		// This method is called with the parent Composite	
		Composite parentComposite = (Composite) parent;
		
		container = new Composite( parentComposite, SWT.None);
		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
		gridLayout.marginWidth = 1;
		gridLayout.marginHeight = 1;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 0;
 		 		
		text = new Text(container, SWT.BORDER);
		GridData textLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		textLayoutData.widthHint = 300;
		textLayoutData.horizontalIndent = 0;
		text.setEnabled(false);
		text.setLayoutData(textLayoutData);
		
		configureButton = new Button(container, SWT.NONE);
		configureButton.setText(Messages.getString("ServiceComposite.btnConfigure.text"));
		configureButton.addSelectionListener(this);
		
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if ( event.getSource() == configureButton ) {
			MBServiceWizard w = new MBServiceWizard( this ); 
			WizardDialog d = new WizardDialog(configureButton.getShell(), w);
			d.open();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void setValue(String value) {
		if (this.text != null && value != null) {
			if (value.indexOf(";")>0) {
				this.text.setText(value.substring(0, value.indexOf(";")));
			} else {
				this.text.setText(value);
			}
			// append the option (create or existing MB service)at the end of the service name
			this.configurationValue = value + ";" + getOption();
			getSite().valueChanged();
		}
	}

	@Override
	public String getValue() {
		return this.configurationValue;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Notification that we should enable or disable the user-defined editor
		container.setEnabled(enabled);
		configureButton.setEnabled(enabled);
	}

	public Button getConfigureButton() {
		return configureButton;
	}

	public void setConfigureButton(Button configureButton) {
		this.configureButton = configureButton;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getPatternInstanceName() {
		return patternInstanceName;
	}

}
