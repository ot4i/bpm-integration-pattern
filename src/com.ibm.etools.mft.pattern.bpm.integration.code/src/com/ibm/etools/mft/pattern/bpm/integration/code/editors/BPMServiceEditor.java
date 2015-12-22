/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2012" 
 *  crc="1634444897" > 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
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
import com.ibm.etools.mft.bpm.integration.twx.utils.TWXLabelProvider;
import com.ibm.etools.mft.connector.base.ConnectorModelManager;
import com.ibm.etools.mft.connector.bpm.eclipse.TWXConstants;
import com.ibm.etools.mft.connector.ui.wizard.simple.ConnectorSimpleWizard;
import com.ibm.etools.mft.pattern.bpm.integration.code.Messages;
import com.ibm.etools.mft.pattern.bpm.integration.code.utility.IntegrationUtility;
import com.ibm.etools.mft.util.ui.IMFTUtilUIConstants;
import com.ibm.etools.mft.util.ui.UIPlugin;

public class BPMServiceEditor extends BasePatternPropertyEditor implements SelectionListener{

	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2012, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	private Composite container;
	private Button configureButton;	
	private Text text;
	private String configurationValue;
	private ConnectorModelManager model = null;
	private String serviceFullPathDisplay;

	@Override
	public void configureEditor(PatternPropertyEditorSite site, boolean required, String configurationValues) {
		super.configureEditor(site, required, configurationValues);
		this.configurationValue = configurationValues;
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
		text.setEnabled(true);
		text.setLayoutData(textLayoutData);
		
		configureButton = new Button(container, SWT.NONE);
		configureButton.setText(Messages.getString("ServiceComposite.btnConfigure.text"));
		configureButton.addSelectionListener(this);

	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if ( event.getSource() == configureButton ) {
			ConnectorSimpleWizard bpmWizard = 
				new ConnectorSimpleWizard(com.ibm.etools.mft.connector.bpm.Messages.BPMServiceDialog_Title, TWXConstants.BPM_CONNECTOR_QNAME);
			bpmWizard.setCustomImageMap(TWXLabelProvider.imageMap);
			bpmWizard.setCustomDescription(com.ibm.etools.mft.connector.bpm.Messages.BPMServiceDialog_Description);
			
			ImageDescriptor wizImage = UIPlugin.getDefault().getImageDescriptor(IMFTUtilUIConstants.IMAGE_NEW_SERVICE_PROJECT_WIZARD);
			bpmWizard.setCustomWizardImage(wizImage);
			bpmWizard.setNeedsProgressMonitor(true);
			List<String> objects = new ArrayList<String>(Arrays.asList(TWXConstants.BPM_OBJECT_TYPE));		
			bpmWizard.setObjectTypesEnablingDetails(objects);
			bpmWizard.setTreeLabelText(com.ibm.etools.mft.connector.bpm.Messages.BPMServiceDialog_IntegrationService);
			
			WizardDialog dialog = new WizardDialog(configureButton.getShell(), bpmWizard);			
			int retCode = dialog.open();
			if (retCode == WizardDialog.OK)
			{
				String configuration = bpmWizard.getConfiguration();
				System.out.println(configuration);
				try {
					this.model = ConnectorModelManager.getInstance(TWXConstants.BPM_CONNECTOR_QNAME);
					this.model.loadConfiguration(configuration.getBytes());	
					setValue(configuration);				
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void setValue(String value) {
		if (value != null) {
			this.configurationValue = value;
			if (this.model == null) {
				try {
					this.model = ConnectorModelManager.getInstance(TWXConstants.BPM_CONNECTOR_QNAME);
					this.model.loadConfiguration(value.getBytes());	
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
			this.text.setText(IntegrationUtility.getDisplayName(this.model.getContext()));
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

}
