/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2015" 
 *  crc="3121893167" > 
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
package com.ibm.etools.mft.pattern.bpm.integration.code;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jet.JET2Context;
import org.eclipse.jet.taglib.workspace.WorkspaceContextExtender;

import com.ibm.broker.config.appdev.patterns.PatternInstanceManager;
import com.ibm.broker.pattern.api.Pattern;
import com.ibm.broker.pattern.api.PatternExtensionPoints;
import com.ibm.broker.pattern.api.PatternFile;
import com.ibm.broker.pattern.api.PatternFolder;
import com.ibm.broker.pattern.api.PatternGroup;
import com.ibm.broker.pattern.api.PatternInstance;
import com.ibm.broker.pattern.api.PatternInstanceExtensionPoint;
import com.ibm.broker.pattern.api.PatternModel;
import com.ibm.broker.pattern.api.PatternParameter;
import com.ibm.broker.pattern.api.PatternPlugin;
import com.ibm.broker.pattern.api.PatternReferencedProject;
import com.ibm.broker.pattern.api.PatternStateModifier;
import com.ibm.broker.pattern.extensions.api.PatternExtensionPointManager;
import com.ibm.broker.pattern.extensions.api.PatternInstanceFileTransform;
import com.ibm.broker.pattern.extensions.api.PatternInstanceTransform;
import com.ibm.broker.pattern.extensions.edit.api.PatternEditorExtensionPointManager;
import com.ibm.etools.mft.pattern.bpm.integration.code.pattern.IntegrationPatternTransform;
import com.ibm.etools.mft.pattern.bpm.integration.code.pattern.ParameterNames;
import com.ibm.etools.mft.pattern.bpm.integration.code.utility.IntegrationUtility;
import com.ibm.etools.mft.pattern.support.ExpressionProvider;
import com.ibm.etools.mft.pattern.support.ExtensionUtility;
import com.ibm.etools.mft.pattern.support.Model;
import com.ibm.etools.mft.pattern.support.Patterns;
import com.ibm.etools.mft.pattern.support.ProjectUtility;
import com.ibm.etools.mft.pattern.support.actions.DisplayExceptionsWorkspaceAction;
import com.ibm.etools.mft.pattern.support.actions.RefreshWorkspaceWorkspaceAction;
import com.ibm.etools.mft.pattern.support.extensions.actions.PatternInstanceCommitWorkspaceAction;
import com.ibm.etools.mft.pattern.support.extensions.actions.PatternInstanceFileTransformWorkspaceAction;
import com.ibm.etools.mft.pattern.support.extensions.actions.PatternInstanceTransformWorkspaceAction;
import com.ibm.etools.mft.pattern.support.extensions.actions.RemoveGeneratedInstanceProjectAction;
import com.ibm.etools.mft.pattern.support.php.PatternInstanceManagerImpl;
import com.ibm.etools.patterns.xpath.CodecUtility;
import com.ibm.etools.patterns.xpath.PatternXPathEvaluator;
import com.ibm.etools.patterns.xpath.PatternXPathManager;

public final class Generation {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2010, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	public static final String TEMPLATES_PATH = "templates";
	public static final String FILE_EXTENSION = ".data";

	private PatternEditorExtensionPointManager patternEditorExtensionPointManager;
	private JET2Context context;
	private PatternInstance patternInstance;
	private String pluginName;
	private PatternExtensionPointManager patternExtensionPointManager;
	private WorkspaceContextExtender workspaceExtender;
	private IWorkspace workspace;
	private IWorkspaceRoot workspaceRoot;
	private String workspacePath;
	private PatternModel patternModel;
	private String templatePath;
	private String pluginId;
	private String patternId;
	private Pattern pattern;
	private PatternPlugin patternPlugin;
	private PatternStateModifier patternState;
	private IProgressMonitor progressMonitor;
	private String projectInstanceName;
	private PatternInstanceManager patternInstanceManager;

	/**
	 * Utility classes have private constructors.
	 * 
	 * @param context
	 *            <code>JET2Context</code>.
	 * @param patternInstance
	 *            <code>PatternInstance</code>.
	 * @param pluginName
	 *            The bundle name for the pattern plug-in.
	 * @param pluginId
	 *            The unique identifier for the plug-in.
	 * @param patternId
	 *            Identifies which pattern we are generating.
	 * @param templatePath
	 *            The relative path in the plug-in the the templates.
	 */
	private Generation(JET2Context context, PatternInstance patternInstance, String pluginName, String pluginId, String patternId, String templatePath) { 
		this.context = context;
		this.patternInstance = patternInstance;
		this.pluginName = pluginName;
		this.pluginId = pluginId;
		this.patternId = patternId;
		this.templatePath = templatePath;
		
	    this.patternExtensionPointManager = ExtensionUtility.getPatternExtensionPointManager();
	    
		this.patternModel = this.patternInstance.getPatternModel();
		this.patternPlugin = this.patternModel.getPlugin(this.pluginId);
		this.pattern = this.patternPlugin.getPattern(this.patternId);
	    this.patternState = (PatternStateModifier) this.patternInstance.getPatternState();

	    this.patternEditorExtensionPointManager = ExtensionUtility.getPatternEditorExtensionPointManager();

		this.workspace = ResourcesPlugin.getWorkspace();
		this.workspaceRoot = this.workspace.getRoot();
		this.workspaceExtender = WorkspaceContextExtender.getInstance(context);
		this.workspacePath = this.workspaceRoot.getLocation().toString();
		
		patternInstanceManager = new PatternInstanceManagerImpl(this.patternInstance, this.pattern);
		this.projectInstanceName = IntegrationUtility.getMBServiceName(patternInstanceManager.getParameterValue(ParameterNames.BROKER_SERVICE_NAME));
		this.progressMonitor = new NullProgressMonitor();				
	}

	/**
	 * Invoked at the start of pattern instance generation.
	 * 
	 * @param context
	 *            <code>JET2Context</code>.
	 * @param pluginName
	 *            The bundle name for the pattern plug-in.
	 * @param pluginId
	 *            The unique identifier for the plug-in.
	 * @param patternId
	 *            Identifies which pattern we are generating.
	 * @param templatePath
	 *            The relative path in the plug-in the the templates.
	 */
	public static void onGenerate(JET2Context context, String pluginName, String pluginId, String patternId, String templatePath) {
		PatternInstance patternInstance = Model.loadModelForPatternInstance(context, pluginName);
		Generation generation = new Generation(context, patternInstance, pluginName, pluginId, patternId, templatePath);			
		
		generation.generatePatternInstance();
	}

	/**
	 * Gets an <code>IFolder</code> in this <code>IContainer</code>.
	 * 
	 * @param container
	 *            <code>IContainer</code>.
	 * @param folderName
	 *            The folder name to be created.
	 *            
	 * @return <code>IFolder</code>.
	 * @throws CoreException <code>CoreException</code>.
	 */
	private IFolder getFolder(IContainer container, String folderName) throws CoreException {
		if (container instanceof IFolder) {
			IFolder containerFolder = (IFolder) container;
			return containerFolder.getFolder(folderName);
		}

		IProject containerProject = (IProject) container;
		return containerProject.getFolder(folderName);
	}
	
	/**
	 * Gets an <code>IFile</code> in this <code>IContainer</code>.
	 * 
	 * @param container
	 *            <code>IContainer</code>.
	 * @param fileName
	 *            The file name to be created.
	 *            
	 * @return <code>IFile</code>.
	 * @throws CoreException <code>CoreException</code>.
	 */
	private IFile getFile(IContainer container, String fileName) throws CoreException {
		if (container instanceof IFolder) {
			IFolder containerFolder = (IFolder) container;
			return containerFolder.getFile(fileName);
		}

		IProject containerProject = (IProject) container;
		return containerProject.getFile(fileName);
	}
	
	/**
	 * Creates the <code>IFile</code> for the <code>PatternReferencedProject</code>.
	 * 
	 * @param referencedProject
	 *            <code>PatternReferencedProject</code>.
	 * @param createFile
	 *            <code>IFile</code>.
	 * @param currentFile
	 *            <code>PatternFile</code>.
	 *            
	 * @throws CoreException <code>CoreException</code>.
	 */
	private void createFile(PatternReferencedProject referencedProject, IFile createFile, PatternFile currentFile) throws CoreException {
	    String projectName = referencedProject.getDisplayName();	    
		String fileName = currentFile.getFileName();
		boolean customProject = referencedProject.isCustomProject();

		// Does the file currently exist?
    	if (createFile.exists() == true) {
    		if (customProject == true) {
    			this.context.logInfo("Skipping file [" + fileName + "]");
    			return; // Leave the existing file alone!
    		}
    		createFile.delete(true, null);
		}

		this.context.logInfo("Creating file from resource [" + fileName + "]");
		String sourcePath = (this.templatePath + "/" + TEMPLATES_PATH + "/" + 
			projectName + "/" + currentFile.getRelativePath() + FILE_EXTENSION);

		this.context.logInfo("Source path for file [" + sourcePath + "]");
		byte[] fileContents = Patterns.loadBundleResource(this.context, this.pluginName, sourcePath);
		createFile.create(new ByteArrayInputStream(fileContents), true, null); 
	}

	/**
	 * Creates the folder structure and files for the referenced project.
	 * 
	 * @param referencedProject
	 *            <code>PatternReferencedProject</code>.
	 * @param container
	 *            <code>IProject</code> or <code>IFolder</code>.
	 * @param currentFolder
	 *            The current folder to be generated.
	 * @param transformActions
	 *            List<PatternInstanceFileTransformWorkspaceAction>.
	 * 
	 * @throws CoreException
	 *             <code>CoreException</code>.
	 */
	private void createFoldersAndFiles(PatternReferencedProject referencedProject, IContainer container, PatternFolder currentFolder, List<PatternInstanceFileTransformWorkspaceAction> transformActions) throws CoreException {
		if (currentFolder.isEnabled() == false) { return; }
	    String projectName = referencedProject.getDisplayName();	    
	    PatternFile[] patternFiles = currentFolder.getFiles();

		onChangeFolder(currentFolder);
		
		// Process files before going to child folders
		for (PatternFile currentFile : patternFiles) {
			if (currentFile.isEnabled() == false) { continue; }
			String fileName = currentFile.getFileName();
			String relativePath = currentFile.getRelativePath();
			String targetPath = this.workspacePath + "/" + projectInstanceName + "/" + relativePath;
			String targetFileName = fileName;
			
			onChangeFile(currentFile);

			PatternInstanceFileTransform generateFile = null;
			if (fileName.startsWith(IntegrationUtility.TEMPLATE_FILENAME)) {
				targetPath =  targetPath.replaceAll(IntegrationUtility.TEMPLATE_FILENAME, this.projectInstanceName);
				targetFileName = targetFileName.replaceAll(IntegrationUtility.TEMPLATE_FILENAME, this.projectInstanceName);
			} else {		
				generateFile = this.patternExtensionPointManager.getPatternInstanceFileExtension(fileName);
			}
			
		    if (generateFile == null) {
				IFile createFile = getFile(container, targetFileName);
		    	createFile(referencedProject, createFile, currentFile);
    			continue; // All done for the current file!
	    	}

//			String projectInstanceName = IntegrationUtility.getProjectInstanceName(this.pattern, this.patternInstance, projectName);
			
			this.context.logInfo("Creating file [" + fileName + "," + projectName + "]");
			String sourcePath = (this.templatePath + "/" + TEMPLATES_PATH + "/" + 
				projectName + "/" + relativePath + FILE_EXTENSION);
			

			byte[] fileContents = Patterns.loadBundleResource(this.context, this.pluginName, sourcePath);
			String a = fileContents.toString();
			
			
			this.context.logInfo("Adding workspace action for file [" + fileName + "]");
			
			// Create an action which will be executed after all the files, folders and projects have been processed
			PatternInstanceFileTransformWorkspaceAction fileHandlerAction = new PatternInstanceFileTransformWorkspaceAction(
				this.patternInstance, generateFile, currentFile, this.pluginName, fileContents, sourcePath, targetPath);
			
			int insertLocation = transformActions.size();
			
			// Projects must be configured before the actions kick in..!
			if (relativePath.equals(Patterns.PROJECT_FILE_NAME) == true) {
				insertLocation = 0; // Add the transform to the start
			}			
			transformActions.add(insertLocation, fileHandlerAction);
		}

		// Generate the child folders of the current folder
		for (PatternFolder childFolder : currentFolder.getFolders()) {
			if (childFolder.isEnabled() == true) { 
				String childFolderName = childFolder.getFolderName();
				IFolder createChildFolder = getFolder(container, childFolderName);
				this.context.logInfo("Creating folder [" + childFolderName + "]");
				if (createChildFolder.exists() == false) {
					createChildFolder.create(true, true, null);
				}
				createFoldersAndFiles(referencedProject, createChildFolder, childFolder, transformActions);
			}
		}
	}

	/**
	 * Add workspace actions so that pattern transformations are invoked.
	 */
	private void addPatternInstanceExtensionActions() throws XPathExpressionException, ParserConfigurationException {
		PatternInstanceTransform[] registeredTransforms = this.patternExtensionPointManager.getPatternInstanceExtensions();
		PatternExtensionPoints model = pattern.getPatternExtensionPoints();
		PatternInstanceExtensionPoint[] patternExtensionPoints = model.getPatternInstanceExtensionPoints();

		this.context.logInfo("Adding pattern instance transform actions");

		// Add pattern instance transforms that are not linked to any particular pattern parameter
		for (PatternInstanceExtensionPoint currentPatternExtensionPoint : patternExtensionPoints) {
			String extensionClassName = currentPatternExtensionPoint.getExtensionClass();
			this.context.logInfo("Extension class name [" + extensionClassName + "]");
			for (PatternInstanceTransform currentTransform : registeredTransforms) {
				String transformClassName = currentTransform.getClass().getCanonicalName();
				this.context.logInfo("Transform class name [" + transformClassName + "]");
				if (transformClassName.equals(extensionClassName) == true) {
					this.context.logInfo("Adding transform action [" + this.pluginName + "]");
					PatternInstanceTransformWorkspaceAction extensionAction = new PatternInstanceTransformWorkspaceAction(
						this.patternInstance, currentTransform, this.pattern, currentPatternExtensionPoint, this.pluginName);
					
					this.workspaceExtender.addFinalAction(extensionAction);
				}
			}
		}
		this.context.logInfo("Finished adding pattern instance transform actions");
	}

	
	/**
	 * Runs the logic to generate the pattern instance.
	 */
	private void generatePatternInstance() {
		onChangePlugin(this.patternPlugin);
		try {

			onChangePattern(this.pattern);
		
			PatternXPathManager patternXPathManager = PatternXPathManager.getPatternXPathManager();
			PatternXPathEvaluator evaluator = patternXPathManager.createPatternXPathEvaluator();
			ExpressionProvider provider = new ExpressionProvider(this.patternInstance);
			ProjectUtility.resetPatternInstanceProjects(this.patternInstance);
			List<PatternInstanceFileTransformWorkspaceAction> transformActions = new ArrayList<PatternInstanceFileTransformWorkspaceAction>();
			
			evaluator.setPatternExpressionProvider(provider);
			
			HashSet<String> referencedProjectNames = new HashSet<String>();
			
			// Process each reference project in sequence firing model events as we go
			for (PatternReferencedProject referencedProject : this.pattern.getReferencedProjects()) {
				String projectName = referencedProject.getDisplayName();
				
				// Check the referenced project is enabled
				if (referencedProject.isEnabled() == false) { 
					continue; // Project is disabled..!
				}
				
				onChangeReferencedProject(referencedProject);

				String enableExpression = referencedProject.getExpression();
				if (enableExpression.length() > 0) {
					boolean projectIsEnabled = evaluator.evaluateBoolean(enableExpression);
					if (projectIsEnabled == false) {
						continue; // Ignore this disabled project
					}
				}

//				String projectInstanceName = IntegrationUtility.getProjectInstanceName(this.pattern, this.patternInstance, projectName);
				referencedProjectNames.add(projectInstanceName);
				this.context.logInfo("Generating project [" + projectInstanceName + "]");
				boolean customProject = referencedProject.isCustomProject();
				IProject currentProject = this.workspaceRoot.getProject(projectInstanceName);
				
				// Delete the project if already exists
				if (currentProject.exists() == true) {
					currentProject.refreshLocal(IResource.DEPTH_INFINITE, this.progressMonitor);
					currentProject.open(this.progressMonitor);
					if (customProject == false) {
						currentProject.delete(true, true, this.progressMonitor);
					}
				}

				PatternFolder[] folders = referencedProject.getFolders();
				
				// Create the project if it doesn't exist
				if (currentProject.exists() == false) {
					currentProject.create(this.progressMonitor);
					currentProject.open(this.progressMonitor);
					ProjectUtility.onGenerateProject(this.patternInstance, currentProject);
				}

				ProjectUtility.addProjectToPatternInstance(patternInstance, currentProject);
				this.context.logInfo("Creating files and folders [" + projectInstanceName + "]");
				ProjectUtility.setCustomProject(this.patternInstance, currentProject, customProject);
				createFoldersAndFiles(referencedProject, currentProject, folders[0], transformActions);				
			}

			this.context.logInfo("Adding file transform actions");			
			for (PatternInstanceFileTransformWorkspaceAction currentAction : transformActions) {
				this.workspaceExtender.addFinalAction(currentAction);
			}

			addPatternInstanceExtensionActions();
			
			IntegrationPatternTransform transform = new IntegrationPatternTransform(this.progressMonitor);
			transform.onGeneratePatternInstance(patternInstanceManager);
			this.context.logInfo("Finished running IntegrationPatternTransform"); //$NON-NLS-1$ //$NON-NLS-2$
			
			this.context.logInfo("Adding resource manager commit action");			
			PatternInstanceCommitWorkspaceAction resourceManagerAction = new PatternInstanceCommitWorkspaceAction(this.patternInstance);
			if (resourceManagerAction != null) {
				this.workspaceExtender.addFinalAction(resourceManagerAction);
			}    			
			
			if ( this.context.hasVariable(Model.PATTERN_GENERATION_REMOVE_GENERATED_PROJECTS) ) {
				this.context.logInfo( "" );
				RemoveGeneratedInstanceProjectAction removeGeneratedInstanceProjectAction = new RemoveGeneratedInstanceProjectAction(this.patternInstance, this.pattern, referencedProjectNames);
				this.workspaceExtender.addFinalAction(removeGeneratedInstanceProjectAction);
			}
			
			this.context.logInfo("Adding final workspace refresh action");			
			RefreshWorkspaceWorkspaceAction workspaceAction = new RefreshWorkspaceWorkspaceAction(this.context);
			if (workspaceAction != null) {
				this.workspaceExtender.addFinalAction(workspaceAction);
			}    			

			this.context.logInfo("Adding exception logging workspace action");
			DisplayExceptionsWorkspaceAction exceptionAction = new DisplayExceptionsWorkspaceAction(this.patternInstance);
			if (exceptionAction != null) {
				this.workspaceExtender.addFinalAction(exceptionAction);
			}    			
			
		} catch (Exception exception) {
			this.context.logError("Unable to generate pattern instance [" + exception.getMessage() + "]");
		}
		this.patternInstance.clearPatternState();
	}

	/**
	 * Changes the current <code>PatternPlugin</code> instance.
	 * 
	 * @param patternPlugin
	 *            <code>PatternPlugin</code>.
	 */
	private void onChangePlugin(PatternPlugin patternPlugin) {
		String pluginId = patternPlugin.getPluginId();
		this.context.logInfo("Changing plugin to [" + pluginId + "]");
		this.patternState.onChangePlugin(patternPlugin);
	}
	
	/**
	 * Changes the current <code>Pattern</code> instance.
	 * 
	 * @param pattern
	 *            <code>Pattern</code>.
	 */
	private void onChangePattern(Pattern pattern) {
		String patternId = pattern.getPatternId();
		this.context.logInfo("Changing pattern to [" + patternId + "]");
		this.patternState.onChangePattern(pattern);
		
		try {

			this.context.logInfo("Setting hidden parameter values [" + patternId + "]");

			// First scan sets the default value for hidden parameters
			for (PatternGroup currentGroup : pattern.getGroups()) {
				for (PatternParameter currentParameter : currentGroup.getParameters()) {
					String parameterId = currentParameter.getParameterId();
					boolean isParameterTable = this.patternInstance.isParameterTable(parameterId);
					if (isParameterTable == false) {
						if (currentParameter.isVisible() == false) {
							String defaultValue = currentParameter.getDefaultValue();
							defaultValue = (defaultValue == null ? Patterns.DEFAULT_PARAMETER_VALUE : defaultValue);
							this.context.logInfo("Setting default value [" + parameterId + "," + defaultValue + "]");
							this.patternInstance.setParameterValue(parameterId, defaultValue);
						}
					}
				}
			}

			this.context.logInfo("Replacing disabled parameters [" + patternId + "]");

			// Replace parameter values with defaults if disabled
			for (PatternGroup currentGroup : pattern.getGroups()) {
				for (PatternParameter currentParameter : currentGroup.getParameters()) {
					String parameterId = currentParameter.getParameterId();
					boolean isParameterTable = this.patternInstance.isParameterTable(parameterId);
					if (isParameterTable == false) {
						if (currentParameter.isVisible() == true) {
							if (currentParameter.isContinuation() == false) {
								boolean targetIsEnabled = this.patternInstance.isParameterEnabled(currentParameter);
								if (targetIsEnabled == false) {
									String defaultValue = currentParameter.getDefaultValue();
									defaultValue = (defaultValue == null ? Patterns.DEFAULT_PARAMETER_VALUE : defaultValue);
									this.context.logInfo("Setting default value [" + parameterId + "," + defaultValue + "]");
									this.patternInstance.setParameterValue(parameterId, defaultValue);
								}
							}
						}
					}
				}
			}			

			this.context.logInfo("Decoding parameter values [" + patternId + "]");

			// Decode hexadecimal parameter values used by enumerations
			for (PatternGroup currentGroup : pattern.getGroups()) {
				for (PatternParameter currentParameter : currentGroup.getParameters()) {
					String parameterId = currentParameter.getParameterId();
					boolean isParameterTable = this.patternInstance.isParameterTable(parameterId);
					if (isParameterTable == false) {
						String parameterValue = this.patternInstance.getParameterValue(parameterId);
						if (parameterValue != null) {
							this.context.logInfo("Decoding value [" + parameterValue + "]");
							parameterValue = CodecUtility.decodeValue(parameterValue);
							this.context.logInfo("Return value [" + parameterValue + "]");
							this.patternInstance.setParameterValue(parameterId, parameterValue);
						}
					}
				}
			}

			PatternXPathManager patternXPathManager = PatternXPathManager.getPatternXPathManager();
			PatternXPathEvaluator evaluator = patternXPathManager.createPatternXPathEvaluator();
			ExpressionProvider provider = new ExpressionProvider(this.patternInstance);
			this.context.logInfo("Evaluating transform expressions [" + patternId + "]");

			evaluator.setPatternExpressionProvider(provider);
			
			// Evaluate all the expressions configured for the parameters 
			for (PatternGroup currentGroup : pattern.getGroups()) {
				for (PatternParameter currentParameter : currentGroup.getParameters()) {
					String parameterId = currentParameter.getParameterId();
					boolean isParameterTable = this.patternInstance.isParameterTable(parameterId);
					if (isParameterTable == false) {
						String expression = currentParameter.getExpression();
						if (expression != null) {
							if (expression.length() > 0) {
								this.context.logInfo("Evaluating expression [" + parameterId + "," + expression + "]");
								String updatedValue = evaluator.evaluate(expression);
								this.context.logInfo("Return value [" + updatedValue + "]");
								this.patternInstance.setParameterValue(parameterId, updatedValue);
							}
						}
					}
				}
			}
			
		} catch (Exception exception) {
			this.context.logError(exception.getMessage());
		}
	}
	
	/**
	 * Changes the current <code>PatternReferencedProject</code> instance.
	 * 
	 * @param referencedProject
	 *            <code>PatternReferencedProject</code>.
	 */
	private void onChangeReferencedProject(PatternReferencedProject referencedProject) {
		String projectName = referencedProject.getDisplayName();
		this.context.logInfo("Changing referenced project to [" + projectName + "]");
		this.patternState.onChangeReferencedProject(referencedProject);
	}
    
	/**
	 * Changes the current <code>PatternFolder</code> instance.
	 * 
	 * @param patternFolder
	 *            <code>PatternFolder</code>.
	 */
	private void onChangeFolder(PatternFolder patternFolder) {
		String relativePath = patternFolder.getRelativePath();
		this.context.logInfo("Changing folder to [" + relativePath + "]");
		this.patternState.onChangeFolder(patternFolder);
	}
    
	/**
	 * Changes the current <code>PatternFile</code> instance.
	 * 
	 * @param patternFile
	 *            <code>PatternFile</code>.
	 */
	private void onChangeFile(PatternFile patternFile) {
		String relativePath = patternFile.getRelativePath();
		this.context.logInfo("Changing file to [" + relativePath + "]");
		this.patternState.onChangeFile(patternFile);
	}

	/**
	 * @return <code>PatternEditorExtensionPointManager</code>.
	 */
	public PatternEditorExtensionPointManager getPatternEditorExtensionPointManager() {
		return patternEditorExtensionPointManager;
	}
}
