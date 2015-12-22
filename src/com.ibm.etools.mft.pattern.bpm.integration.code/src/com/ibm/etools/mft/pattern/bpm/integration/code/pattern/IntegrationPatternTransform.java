package com.ibm.etools.mft.pattern.bpm.integration.code.pattern;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.ibm.broker.config.appdev.patterns.GeneratePatternInstanceTransform;
import com.ibm.broker.config.appdev.patterns.PatternInstanceManager;
import com.ibm.etools.mft.bpm.integration.model.IntegrationService;
import com.ibm.etools.mft.bpm.utils.TWXUtils;
import com.ibm.etools.mft.connector.base.ConnectorModelManager;
import com.ibm.etools.mft.connector.bpm.ConfigUtility;
import com.ibm.etools.mft.connector.bpm.eclipse.TWXConstants;
import com.ibm.etools.mft.connector.bpm.model.beans.Discovery;
import com.ibm.etools.mft.pattern.bpm.integration.code.utility.IntegrationUtility;

public class IntegrationPatternTransform implements GeneratePatternInstanceTransform {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2010, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	
	private String workspaceLocation;
	private String patternInstanceName;
	private String serviceName;
	private IProgressMonitor progressMonitor;

	
	
	public IntegrationPatternTransform(IProgressMonitor progressMonitor) {
		super();
		this.progressMonitor = progressMonitor;
	}


	@Override
	public void onGeneratePatternInstance(PatternInstanceManager patternInstanceManager) {
		
		// The location for the generated projects 
		this.workspaceLocation = patternInstanceManager.getWorkspaceLocation();		
		// The pattern instance name for this generation
		this.patternInstanceName = patternInstanceManager.getPatternInstanceName();
		this.serviceName = IntegrationUtility.getMBServiceName(patternInstanceManager.getParameterValue(ParameterNames.BROKER_SERVICE_NAME));
		
		String serviceDocument = patternInstanceManager.getParameterValue(ParameterNames.INTEGRATION_SERVICES);		
		try {
			ConnectorModelManager connectorManager = ConnectorModelManager.getInstance(TWXConstants.BPM_CONNECTOR_QNAME);		
			if (connectorManager != null) 
			{		
				connectorManager.loadConfiguration(serviceDocument.getBytes());
				Discovery discovery =(Discovery)connectorManager.getDiscoveryConfiguration().getJaxbConnectorObject();
				IntegrationService process =  ConfigUtility.getIntegrationService(discovery);
				String integrationServiceName = process.getName();
				integrationServiceName = TWXUtils.getValidWSDLOperationName(integrationServiceName);

				String templateTNS =  "http://" + IntegrationUtility.TEMPLATE_FILENAME;
				String newTNS = TWXUtils.getNamespaceFor(process);
			
				String fileName = workspaceLocation + "/" + serviceName + "/service.descriptor";
				IntegrationUtility.replaceFileContent(new File (fileName), IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, "operation1", integrationServiceName, null, null);
			
				fileName = workspaceLocation + "/" + serviceName + "/" + serviceName + ".wsdl";
				IntegrationUtility.replaceFileContent(new File (fileName), templateTNS, newTNS, IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, "operation1", integrationServiceName);

				fileName = workspaceLocation + "/" + serviceName + "/" + serviceName + ".xsd";
				IntegrationUtility.replaceFileContent(new File (fileName), templateTNS, newTNS, IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null);
			
				fileName = workspaceLocation + "/" + serviceName + "/gen/" + serviceName + ".msgflow";
				IntegrationUtility.replaceFileContent(new File (fileName), templateTNS, newTNS, IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null);

				fileName = workspaceLocation + "/" + serviceName + "/gen/" + serviceName + "InputCatchHandler.subflow";
				IntegrationUtility.replaceFileContent(new File (fileName), IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null, null, null);

				fileName = workspaceLocation + "/" + serviceName + "/gen/" + serviceName + "InputFailureHandler.subflow";
				IntegrationUtility.replaceFileContent(new File (fileName), IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null, null, null);

				fileName = workspaceLocation + "/" + serviceName + "/gen/" + serviceName + "InputHTTPTimeoutHandler.subflow";
				IntegrationUtility.replaceFileContent(new File (fileName), IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null, null, null);

				// update .project file
				fileName = workspaceLocation + "/" + serviceName + "/.project.service";
				File projectTemplate = new File (fileName);
				IntegrationUtility.replaceFileContent(projectTemplate, IntegrationUtility.TEMPLATE_FILENAME, this.serviceName, null, null, null, null);
				File projectFile = new File (workspaceLocation + "/" + serviceName + "/.project");
				if (projectFile.delete())
					projectTemplate.renameTo(projectFile);

				// generate xsd inline schema file
				String[] parameters = {serviceName};				
				com.ibm.mb.connector.discovery.framework.resources.IConnectorGeneratedResource[] connectorResources = connectorManager.getDiscoveryConnector().generate(parameters);
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(serviceName);
        	
				if (connectorResources != null)
				{
					for (int i = 0; i < connectorResources.length; i++) {
						com.ibm.mb.connector.discovery.framework.resources.IConnectorGeneratedResource res = connectorResources[i];
						byte[] resContents = res.getContentsAsBytes();
						String resPath = res.getDescriptor().getPath();
						String resName = res.getDescriptor().getName();
						String resFileExt = res.getDescriptor().getFileExtension();
						if (resFileExt != null && resName != null && !resName.endsWith("." + resFileExt))
							resName = resName + "." + resFileExt;
						if (resPath == null)
							resPath = "";
						final IFile resFile = project.getFile (resPath + Path.SEPARATOR + resName);
						resFile.create(new ByteArrayInputStream(resContents), true, this.progressMonitor);
					}
				}
			}	        	
		} catch (Exception exception) {
			String exceptionMessage = exception.getMessage();
			System.err.println(exceptionMessage);
			exception.printStackTrace();
		}

	}
}
