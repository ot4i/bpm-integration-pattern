package com.ibm.etools.mft.pattern.bpm.integration.code.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import com.ibm.etools.mft.pattern.bpm.integration.code.Messages;
import com.ibm.etools.mft.util.ui.IMFTUtilUIConstants;
import com.ibm.etools.mft.util.ui.UIPlugin;

public class MBServiceWizard extends Wizard {

	private SelectMBServicePage page1;
	private ServiceEditor owner;
	
	public MBServiceWizard(ServiceEditor serviceEditor) {
		setWindowTitle(Messages.getString("ServiceDialog.dialogTitle.txt"));
		this.owner = serviceEditor;
	}
		
	@Override
	public void addPages() {
		ImageDescriptor image = UIPlugin.getDefault().getImageDescriptor(IMFTUtilUIConstants.IMAGE_NEW_SERVICE_PROJECT_WIZARD);
		this.page1 = new SelectMBServicePage("page1", Messages.getString("ServiceDialog.serviceTitle.txt"), image);
		this.page1.setDescription(Messages.getString("ServiceDialog.servicePrompt.txt"));
		this.page1.setPatternName(this.owner.getPatternInstanceName());
		if (this.owner.getValue() != null && !this.owner.getValue().isEmpty()) {			
			this.page1.setServiceName(this.owner.getValue().substring(0, this.owner.getValue().indexOf(";")));
			String option = this.owner.getValue().substring(this.owner.getValue().indexOf(";")+1, this.owner.getValue().length());
			if (option.equalsIgnoreCase(SelectMBServicePage.CREATE_SERVICE_OPTION))
				this.page1.setIsCreateNewService(true);
			else
				this.page1.setIsCreateNewService(false);
		}
		addPage(this.page1);
	}
	
	@Override
	public boolean performFinish() {
		if (this.page1.isCreateNewService())
			this.owner.setOption(this.page1.CREATE_SERVICE_OPTION);
		else
			this.owner.setOption(this.page1.USE_EXISTING_SERVICE_OPTION);
		
		this.owner.setValue(this.page1.getServiceName());
		return true;
	}

}
