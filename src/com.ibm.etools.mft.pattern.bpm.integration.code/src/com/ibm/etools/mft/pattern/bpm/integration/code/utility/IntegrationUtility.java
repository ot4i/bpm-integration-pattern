package com.ibm.etools.mft.pattern.bpm.integration.code.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import com.ibm.etools.mft.bpm.model.TWProcess;
import com.ibm.etools.mft.bpm.model.twx.TWXPackage;
import com.ibm.etools.mft.connector.bpm.ConfigUtility;
import com.ibm.etools.mft.connector.bpm.eclipse.TWXConstants;
import com.ibm.etools.mft.connector.bpm.model.beans.Discovery;
import com.ibm.mb.connector.discovery.framework.IDiscoveryConfiguration;
import com.ibm.mb.connector.discovery.framework.IDiscoveryContext;


public class IntegrationUtility {

	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2012, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	public static String PACKAGE_BPM_INTEGRATION = "com.ibm.etools.mft.pattern.bpm.integration.code.model";
	public static String TEMPLATE_FILENAME = "IntegrationService";
	
	
	/**
	 * Utility classes should have private constructors!
	 */
	private IntegrationUtility() { }
	

	/**
	 * 
	 * @param model
	 * @param packageName
	 * @return
	 */
	public static String saveModel(JAXBElement model, String packageName) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(packageName);
			Marshaller m = jc.createMarshaller();
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			m.marshal(model, o);

			return new String(o.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	

	/**
	 * The display name will contains the twx file path
	 * @return The display name for the currently selected <code>IntegrationService</code>.
	 */
	public static String getDisplayName(IDiscoveryContext ctx) {

		IDiscoveryConfiguration discoveryConfig = ctx.getConfiguration();	
		Discovery discovery =(Discovery)discoveryConfig.getJaxbConnectorObject();
		String twxFile = discovery.getDiscoveryConnection().getConnectionProperties().getUrlProperty();
		
		if (!(new File (twxFile)).exists())
			return "";
		
		TWProcess selectedProcess = ConfigUtility.getIntegrationService(discovery);
		TWXPackage twxPackage = selectedProcess.getParentPackage();
		
		String slash = TWXConstants.BACKWARD_SLASH;
		if (System.getProperty("os.name").equalsIgnoreCase("Linux"))
			slash = TWXConstants.FORWARD_SLASH;
		
		String getDisplayName = twxPackage.getProjectName() + slash + selectedProcess.getName(); 
		if (twxPackage != null && twxPackage.getParentPackage() != null) {
			getDisplayName = getProcessPackageName(twxPackage) + slash +  getDisplayName;
		}
		
		getDisplayName = "<" + twxFile + ">" + slash + getDisplayName;
		
//		if (System.getProperty("os.name").equalsIgnoreCase("Linux"))
//			getDisplayName = getDisplayName.replaceAll(TWXConstants.BACKWARD_SLASH, TWXConstants.FORWARD_SLASH);
		
		return getDisplayName;
	}

	
	public static String getProcessPackageName (TWXPackage twxPackage) {
		String name = "";
		name = twxPackage.getProjectName();
		if (twxPackage.getParentPackage() != null)
			name = getProcessPackageName(twxPackage.getParentPackage()) + "\\" + name; 
			
		return name;
	}

	

	/**
	 * Perform string search and replace in the given file
	 * This method can replace two search replace. If just need one, set null for search2 and replace2. 
	 * @param file
	 * @param search1
	 * @param replace1
	 * @param search2
	 * @param replace2
	 * @throws Exception
	 */
	static public void replaceFileContent (File file, String search1, String replace1, String search2, String replace2, String search3, String replace3 ) throws Exception{
		
		if (!file.exists())
			return;
		
	    StringBuffer buffer = new StringBuffer();
	    String str;
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    while (true) {
	      str = br.readLine();
	      if (str == null)
	        break;
	      if (str.indexOf(search1)>=0)
	    	  str = str.replaceAll(search1, replace1);
	      
	      if (search2 != null && !search2.isEmpty()) 	    
		      if (str.indexOf(search2)>=0)
		    	  str = str.replaceAll(search2, replace2);

	      if (search3 != null && !search3.isEmpty()) 	    
		      if (str.indexOf(search3)>=0)
		    	  str = str.replaceAll(search3, replace3);
  
	      buffer.append(str);
	      buffer.append("\n");
	    }

	    br.close();
	    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	    bw.write(buffer.toString());
	    bw.close();		
	}
	
	static public String getMBServiceName (String name) {
		return name.substring(0, name.indexOf(";"));
	}

}
