/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2015" 
 *  crc="2545754994" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corporation 2010, 2015 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.pattern.bpm.integration.code.extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.broker.config.appdev.patterns.PatternInstanceManager;
import com.ibm.broker.pattern.api.PackagePattern;
import com.ibm.broker.pattern.api.Pattern;
import com.ibm.broker.pattern.api.PatternFile;
import com.ibm.broker.pattern.api.PatternInstance;
import com.ibm.broker.pattern.api.PatternModel;
import com.ibm.broker.pattern.api.PatternPlugin;
import com.ibm.broker.pattern.api.PatternReferencedProject;
import com.ibm.broker.pattern.extensions.api.PackagePatternFileTransform;
import com.ibm.broker.pattern.extensions.api.PatternInstanceFileTransform;
import com.ibm.etools.mft.pattern.bpm.integration.code.pattern.ParameterNames;
import com.ibm.etools.mft.pattern.bpm.integration.code.utility.IntegrationUtility;
import com.ibm.etools.mft.pattern.support.Patterns;
import com.ibm.etools.mft.pattern.support.ProjectUtility;
import com.ibm.etools.mft.pattern.support.extensions.utility.ProjectTransformUtility;
import com.ibm.etools.mft.pattern.support.php.PatternInstanceManagerImpl;


public class IntegrationServiceProjectTransform implements	PatternInstanceFileTransform, PackagePatternFileTransform {

	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2010, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	public static final String PROJECT_FILE_EXTENSION = "service"; //$NON-NLS-1$	
	private PatternInstance patternInstance;
	private byte[] fileContents;
	private String sourcePath;
	private PatternFile patternFile;
	private String targetPath;
	private Pattern pattern;
	private PatternInstanceManager patternInstanceManager;
	private String projectInstanceName;

	
	@Override
	public void onPackage(PackagePattern packagePattern, PatternFile patternFile, String filePath) {
		packagePattern.logInformation("Packaging project file [" + patternFile.getRelativePath() + "]");
	}

	@Override
	public String getExtension() {
		return PROJECT_FILE_EXTENSION;
	}

	@Override
	public void onGenerate(PatternInstance patternInstance, Pattern pattern,
			PatternFile patternFile, String bundleName, byte[] fileContents,
			String sourcePath, String targetPath) {

		this.patternInstance = patternInstance;
		this.fileContents = fileContents;
		this.sourcePath = sourcePath;
		this.patternFile = patternFile;
		this.pattern = pattern;
		this.targetPath = targetPath;
		this.patternInstanceManager = new PatternInstanceManagerImpl(this.patternInstance, this.pattern);
		this.projectInstanceName = IntegrationUtility.getMBServiceName(patternInstanceManager.getParameterValue(ParameterNames.BROKER_SERVICE_NAME));
			
		PatternReferencedProject currentProject = ProjectTransformUtility.getReferencedProjectForFile(this.pattern, this.patternFile);
		
		try {

			this.patternInstance.logInformation("Generating project file [" + this.sourcePath + "]");

    		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = builderFactory.newDocumentBuilder();
    		Charset projectEncoding = Charset.forName(Patterns.FILE_ENCODING);
    		String documentContents = new String(this.fileContents, projectEncoding);
    		StringReader stringReader = new StringReader(documentContents);
    		Document document = builder.parse(new InputSource(stringReader));
    		
		    String instanceName = this.patternInstance.getPatternInstanceName();
		    
		    this.patternInstance.logInformation("Instance name [" + instanceName + "]");
    		String projectName = updateProjectName(document, instanceName, currentProject);
    		this.patternInstance.logInformation("Updating referenced projects");
    		updateReferencedProjects(document, instanceName);
    		
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, projectEncoding);
            OutputFormat outputFormat = new OutputFormat(Patterns.FILE_FORMAT, Patterns.FILE_ENCODING, true);
            XMLSerializer writer = new XMLSerializer(outputStreamWriter, outputFormat);            

            writer.serialize(document);            
            outputStreamWriter.close();
            outputStream.close();
            
            byte[] byteContents = outputStream.toByteArray();

    		NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    		IWorkspace workspace = ResourcesPlugin.getWorkspace();
    		IWorkspaceRoot workspaceRoot = workspace.getRoot();
    		IProject project = workspaceRoot.getProject(projectName);
            String projectFileName = this.patternFile.getRelativePath();
    		IFile projectFile = project.getFile(projectFileName);
			boolean customProject = currentProject.isCustomProject();

			this.patternInstance.logInformation("Project file ready [" + projectFileName + "]");

			// Check existing custom project
			if (customProject == true) {
				if (ProjectUtility.isGeneratedProject(project) == false) {
					return; // Leave existing project alone!
				}
			}

			this.patternInstance.logInformation("Writing project file [" + projectFileName + "]");
    		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteContents);
       		projectFile.setContents(inputStream, true, true, nullProgressMonitor);
			this.patternInstance.logInformation("Setting custom project [" + customProject + "]");
       		ProjectUtility.setCustomProject(patternInstance, project, customProject);
       		
			ProjectUtility.resetGeneratedProject(project);
    		
		} catch (Exception exception) {
			this.patternInstance.logError("Exception writing project file [" + exception.getMessage() + "]");
        }
	}

	/**
	 * Updates the referenced projects to include the pattern instance name.
	 * 
	 * @param document
	 *            <code>Document</code>.
	 * @param instanceName
	 *            The instance name for this generation.
	 */
	private void updateReferencedProjects(Document document, String instanceName) { 
		Map<String, PatternReferencedProject> projects = getReferencedProjects();
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expression = xpath.compile("/projectDescription/projects/project");
			NodeList projectNodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			
			for (int index = 0; index < projectNodeList.getLength(); index++) {
				Element currentProject = (Element) projectNodeList.item(index);
				String projectName = currentProject.getTextContent();
				if (projects.containsKey(projectName) == true) {
					//String projectInstanceName = IntegrationUtility.getProjectInstanceName(this.pattern, this.patternInstance, projectName);
				    currentProject.setTextContent(projectInstanceName);
					this.patternInstance.logInformation("Updated project name [" + currentProject.getTextContent() + "]");
				}
			}
			
        } catch (Exception exception) {
        	this.patternInstance.logError("Exception updating referenced project name [" + exception.getMessage() + "]");
        }
	}

	/**
	 * Gets a <code>Map</code> containing the <code>PatternReferencedProject</code>.
	 * 
	 * @return <code>Map</code> of <code>PatternReferencedProject</code>.
	 */
	private Map<String, PatternReferencedProject> getReferencedProjects() {
		PatternModel patternModel = this.patternInstance.getPatternModel();
		PatternReferencedProject[] projectsToScan = null;
		
		// Scan the referenced projects until we find our file!
		for (PatternPlugin plugin : patternModel.getPlugins()) {
			if (projectsToScan != null) { break; }
			for (Pattern pattern : plugin.getPatterns()) {
				if (projectsToScan != null) { break; }
				for (PatternReferencedProject project : pattern.getReferencedProjects()) {
					Map<String, PatternFile> files = project.getAllFilesIndexedOnRelativePath();
					Collection<PatternFile> values = files.values();
					if (values.contains(this.patternFile) == true) {
						projectsToScan = pattern.getReferencedProjects(); break;
					}
				}
			}
		}
		
		Map<String, PatternReferencedProject> projectMap = new HashMap<String, PatternReferencedProject>();
		for (int index = 0; index < projectsToScan.length; index++) {
			PatternReferencedProject currentProject = projectsToScan[index];
			String displayName = currentProject.getDisplayName();
			this.patternInstance.logInformation("Scanning referenced project [" + displayName + "]");
			projectMap.put(displayName, currentProject);			
		}
		return projectMap;
	}	

	/**
	 * Updates the project name to include the pattern instance name.
	 * 
	 * @param document
	 *            <code>Document</code>.
	 * @param instanceName
	 *            The instance name for this generation.
	 * @param currentProject
	 *            <code>PatternReferencedProject</code>.
	 *            
	 * @return The updated project name saved to the file.            
	 */
	private String updateProjectName(Document document, String instanceName, PatternReferencedProject currentProject) {
		String projectName = currentProject.getDisplayName();
//		String projectInstanceName = IntegrationUtility.getProjectInstanceName(this.pattern, this.patternInstance, projectName);
		
		try {
						
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expression = xpath.compile("/projectDescription/name");			
			Element projectNameElement = (Element) expression.evaluate(document, XPathConstants.NODE);
		    this.patternInstance.logInformation("Updated project name [" + projectInstanceName + "]");
		    projectNameElement.setTextContent(projectInstanceName);		    

        } catch (Exception exception) {
        	this.patternInstance.logError("Exception updating project name [" + exception.getMessage() + "]");
        }
	    return projectInstanceName;
	}	
}
