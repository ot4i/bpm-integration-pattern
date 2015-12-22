package com.ibm.etools.mft.pattern.bpm.integration.code.editors;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.etools.mft.logicalmodelhelpers.WorkspaceHelper;
import com.ibm.etools.mft.navigator.utils.ServiceUtils;
import com.ibm.etools.mft.pattern.bpm.integration.code.Messages;

public class SelectMBServicePage extends WizardPage {

	public static String CREATE_SERVICE_OPTION = "1";
	public static String USE_EXISTING_SERVICE_OPTION = "2";
	
	private Text fServiceNameText;
//	private Button fSelectServiceOption;
//	private Button fCreateNewServiceOption;
	private Combo comboServices;
	private String serviceName;
	private Boolean isCreateNewService = true;
	private String patternName;

	public SelectMBServicePage(String name) {
		super(name);		
	}

	public SelectMBServicePage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		composite.setFont(parent.getFont());

        // create new service radio button
//        fCreateNewServiceOption = new Button(composite, SWT.RADIO);
//        fCreateNewServiceOption.setText(Messages.getString("ServiceDialog.createServicePrompt.txt"));        
        
		Label serviceNameLabel = new Label(composite, SWT.NULL);
		serviceNameLabel.setText(Messages.getString("ServiceDialog.dialogTitle.txt"));
		
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//        gd.verticalIndent = 3;
        //create the directory text box
		fServiceNameText = new Text(composite, SWT.BORDER);
		fServiceNameText.setLayoutData(gd);
//		fServiceNameText.setEnabled(false);
/*		
        // create select service radio button
        fSelectServiceOption = new Button(composite, SWT.RADIO);
        fSelectServiceOption.setText(Messages.getString("ServiceDialog.selectServicePrompt.txt"));
        fSelectServiceOption.setLayoutData(gd);

        if (isCreateNewService()) {
        	fCreateNewServiceOption.setSelection(true);
		} else {
			fSelectServiceOption.setSelection(true);	
		}

		//create the directory text box
		comboServices = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		comboServices.setLayoutData(gd);
		comboServices.setEnabled(false);
		
		populateServiceProjects();
		comboServices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				onSelectServiceName();
				setIsCreateNewService(false);
				setPageComplete(true);
			}
		});
*/		
		fServiceNameText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				validateServiceName (fServiceNameText.getText());
			}
		});
/*
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Object source = event.getSource();
				if (source.equals(fCreateNewServiceOption)) {
					setIsCreateNewService(true);
					fServiceNameText.setEnabled(true);
					comboServices.setEnabled(false);
					setIsCreateNewService(true);
					validateServiceName (fServiceNameText.getText());
				} else if (source.equals(fSelectServiceOption)) {
					setIsCreateNewService(false);
					fServiceNameText.setEnabled(false);
					comboServices.setEnabled(true);		
					setIsCreateNewService(false);
					if (comboServices.getText().isEmpty())
						setPageComplete(false);
					else
						setPageComplete(true);
				}
			}
		};


		fCreateNewServiceOption.addSelectionListener(selectionListener);
		fSelectServiceOption.addSelectionListener(selectionListener);
*/		
		// set default values
//		if (fCreateNewServiceOption.getSelection()) {
			fServiceNameText.setEnabled(true);
//			comboServices.setEnabled(false);
			if (getServiceName() != null || (getServiceName() != null && !getServiceName().isEmpty()))
				fServiceNameText.setText(getServiceName());			
			
			validateServiceName (fServiceNameText.getText());
//		} 
/*		
		if (fSelectServiceOption.getSelection()) {
			comboServices.setEnabled(true);
			fServiceNameText.setEnabled(false);
			if (getServiceName() != null || (getServiceName() != null && !getServiceName().isEmpty())) {
				for (int index = 0; index < comboServices.getItemCount(); index++) {
					String name = comboServices.getItem(index);
					if (name.equalsIgnoreCase(getServiceName())) {
						comboServices.select(index);
						setPageComplete(true);
						break;
					}
				}
			}
		}
*/		
		
		setControl(composite);
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	private void populateServiceProjects () {
		comboServices.removeAll();	
		List<IProject> serviceList = WorkspaceHelper.getAllServiceProjects();
		for (int index = 0; index < serviceList.size(); index++) {
			IProject project = serviceList.get(index);
			String projectName = project.getName();
			comboServices.add(projectName);
			comboServices.setData(projectName, project);
		}
	}
	
	private boolean isExistingProject (String projectName ) {
		boolean existing = false;
		
		List<IProject> projectList = WorkspaceHelper.getAllProjects();
		for (int index = 0; index < projectList.size(); index++) {
			IProject project = projectList.get(index);
			if (projectName.equalsIgnoreCase(project.getName())) {
				existing = true;
				break;
			}
		}		
		return existing;
	}
	
    /**
     * Called when the current service selection changes.
     */
	private void onSelectServiceName() {
		int selectionIndex = comboServices.getSelectionIndex();
		
		// Is anything selected?
		if (selectionIndex < 0) {
			return; // Nothing selected
		}
		
		String projectName = comboServices.getItem(selectionIndex);
		setServiceName(projectName);
	}

	public Boolean isCreateNewService() {
		return isCreateNewService;
	}

	public void setIsCreateNewService(Boolean isCreateNewService) {
		this.isCreateNewService = isCreateNewService;
	}
	
	private void validateServiceName (String name) {
		
		setErrorMessage(null);
		
		if (name == null || name.isEmpty()) {
			setPageComplete(false);
			return;
		}
					
		String errMsg = ServiceUtils.validateServiceName (name);
		if (errMsg == null && isExistingProject(name)) {
			errMsg = Messages.getString("ServiceDialog.serviceAlreadExist.txt");
		} else 	if (errMsg == null && name.equalsIgnoreCase(getPatternName())) {
			errMsg = Messages.getString("ServiceDialog.serviceSameAsPatternName.txt");
		}

		
		if (errMsg == null) {
			setErrorMessage(null);
			setPageComplete(true);
			setServiceName(name);
		} else {
			setErrorMessage(errMsg);
			setPageComplete(false);
		}

	}

	public String getPatternName() {
		return patternName;
	}

	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}	
}
