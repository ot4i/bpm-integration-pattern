/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2012" 
 *  crc="627091348" > 
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
package com.ibm.etools.mft.pattern.bpm.integration.code.designer;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.osgi.framework.Bundle;

@SuppressWarnings("unchecked")
public class ResourceManager extends SWTResourceManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2012, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	private static Map descriptorImageMap = new HashMap();
	private static Map[] decoratedImageMap = new Map[LAST_CORNER_KEY];
	private static Map urlImageMap = new HashMap();
	private static PluginResourceProvider designTimePluginResourceProvider = null;

	public static ImageDescriptor getImageDescriptor(Class clazz, String path) {
		return ImageDescriptor.createFromFile(clazz, path);
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		try {
			return ImageDescriptor.createFromURL((new File(path)).toURI().toURL());
		} catch (MalformedURLException exception) {
			return null;
		}
	}

	public static Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		Image image = (Image) descriptorImageMap.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			descriptorImageMap.put(descriptor, image);
		}
		return image;
	}

	public static Image decorateImage(Image baseImage, Image decorator) {
		return decorateImage(baseImage, decorator, BOTTOM_RIGHT);
	}

	public static Image decorateImage(final Image baseImage, final Image decorator, final int corner) {
		if (corner <= 0 || corner >= LAST_CORNER_KEY) {
			throw new IllegalArgumentException("Wrong decorate corner"); //$NON-NLS-1$
		}
		Map cornerDecoratedImageMap = decoratedImageMap[corner];
		if (cornerDecoratedImageMap == null) {
			cornerDecoratedImageMap = new HashMap();
			decoratedImageMap[corner] = cornerDecoratedImageMap;
		}
		Map decoratedMap = (Map) cornerDecoratedImageMap.get(baseImage);
		if (decoratedMap == null) {
			decoratedMap = new HashMap();
			cornerDecoratedImageMap.put(baseImage, decoratedMap);
		}

		Image result = (Image) decoratedMap.get(decorator);
		if (result == null) {
			final Rectangle bib = baseImage.getBounds();
			final Rectangle dib = decorator.getBounds();
			final Point baseImageSize = new Point(bib.width, bib.height);
			CompositeImageDescriptor compositImageDesc = new CompositeImageDescriptor() {
				@Override
				protected void drawCompositeImage(int width, int height) {
					drawImage(baseImage.getImageData(), 0, 0);
					if (corner == TOP_LEFT) {
						drawImage(decorator.getImageData(), 0, 0);
					} else if (corner == TOP_RIGHT) {
						drawImage(decorator.getImageData(), bib.width - dib.width, 0);
					} else if (corner == BOTTOM_LEFT) {
						drawImage(decorator.getImageData(), 0, bib.height - dib.height);
					} else if (corner == BOTTOM_RIGHT) {
						drawImage(decorator.getImageData(), bib.width - dib.width, bib.height - dib.height);
					}
				}
				@Override
				protected Point getSize() {
					return baseImageSize;
				}
			};

			result = compositImageDesc.createImage();
			decoratedMap.put(decorator, result);
		}
		return result;
	}

	public static void disposeImages() {
		SWTResourceManager.disposeImages();
		for (Iterator scan = descriptorImageMap.values().iterator(); scan.hasNext();) {
			((Image) scan.next()).dispose();
		}
		descriptorImageMap.clear();

		for (int index = 0; index < decoratedImageMap.length; index++) {
			Map cornerDecoratedImageMap = decoratedImageMap[index];
			if (cornerDecoratedImageMap != null) {
				for (Iterator scan = cornerDecoratedImageMap.values().iterator(); scan.hasNext();) {
					Map decoratedMap = (Map) scan.next();
					for (Iterator mapScan = decoratedMap.values().iterator(); mapScan.hasNext();) {
						Image image = (Image) mapScan.next();
						image.dispose();
					}
					decoratedMap.clear();
				}
				cornerDecoratedImageMap.clear();
			}
		}
		
		for (Iterator scan = urlImageMap.values().iterator(); scan.hasNext();) {
			((Image) scan.next()).dispose();
		}
		urlImageMap.clear();
	}

	public interface PluginResourceProvider {
		URL getEntry(String symbolicName, String path);
	}

	public static Image getPluginImage(Object plugin, String name) {
		try {
			URL url = getPluginImageURL(plugin, name);
			if (url != null) {
				return getPluginImageFromUrl(url);
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		return null;
	}

	public static Image getPluginImage(String symbolicName, String path) {
		try {
			URL url = getPluginImageURL(symbolicName, path);
			if (url != null) {
				return getPluginImageFromUrl(url);
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		return null;
	}

	private static Image getPluginImageFromUrl(URL url) {
		try {
			try {
				if (urlImageMap.containsKey(url)) {
					return (Image) urlImageMap.get(url);
				}
				InputStream stream = url.openStream();
				Image image;
				try {
					image = getImage(stream);
					urlImageMap.put(url, image);
				} finally {
					stream.close();
				}
				return image;
			} catch (Throwable exception) {
				// Ignore any exceptions
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		return null;
	}

	public static ImageDescriptor getPluginImageDescriptor(Object plugin, String name) {
		try {
			try {
				URL url = getPluginImageURL(plugin, name);
				return ImageDescriptor.createFromURL(url);
			} catch (Throwable exception) {
				// Ignore any exceptions
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		return null;
	}

	public static ImageDescriptor getPluginImageDescriptor(String symbolicName, String path) {
		try {
			URL url = getPluginImageURL(symbolicName, path);
			if (url != null) {
				return ImageDescriptor.createFromURL(url);
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		return null;
	}

	private static URL getPluginImageURL(String symbolicName, String path) {
		Bundle bundle = Platform.getBundle(symbolicName);
		if (bundle != null) {
			return bundle.getEntry(path);
		}

		if (designTimePluginResourceProvider != null) {
			return designTimePluginResourceProvider.getEntry(symbolicName, path);
		}
		return null;
	}

	private static URL getPluginImageURL(Object plugin, String name) throws Exception {
		try {
			Class bundleClass = Class.forName("org.osgi.framework.Bundle"); //$NON-NLS-1$
			Class bundleContextClass = Class.forName("org.osgi.framework.BundleContext"); //$NON-NLS-1$
			if (bundleContextClass.isAssignableFrom(plugin.getClass())) {
				Method getBundleMethod = bundleContextClass.getMethod("getBundle", new Class[0]); //$NON-NLS-1$
				Object bundle = getBundleMethod.invoke(plugin, new Object[0]);
				Class pathClass = Class.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
				Constructor pathConstructor = pathClass.getConstructor(new Class[] { String.class });
				Object path = pathConstructor.newInstance(new Object[] { name });

				Class pathInterfaceClass = Class.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
				Class platformClass = Class.forName("org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
				Method findMethod = platformClass.getMethod("find", new Class[] { bundleClass, pathInterfaceClass }); //$NON-NLS-1$
				return (URL) findMethod.invoke(null, new Object[] { bundle, path });
			}
		} catch (Throwable exception) {
			// Ignore any exceptions
		}
		Class pluginClass = Class.forName("org.eclipse.core.runtime.Plugin"); //$NON-NLS-1$
		if (pluginClass.isAssignableFrom(plugin.getClass())) {
			Class pathClass = Class.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
			Constructor pathConstructor = pathClass.getConstructor(new Class[] { String.class });
			Object path = pathConstructor.newInstance(new Object[] { name });
			Class pathInterfaceClass = Class.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
			Method findMethod = pluginClass.getMethod("find", new Class[] { pathInterfaceClass }); //$NON-NLS-1$
			return (URL) findMethod.invoke(plugin, new Object[] { path });
		}
		return null;
	}

	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
	}
}
