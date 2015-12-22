/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2012" 
 *  crc="2104818114" > 
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("unchecked")
public class SWTResourceManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corporation 2012, 2015 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	public static final int MISSING_IMAGE_SIZE = 10;
	public static final int TOP_LEFT = 1;
	public static final int TOP_RIGHT = 2;
	public static final int BOTTOM_LEFT = 3;
	public static final int BOTTOM_RIGHT = 4;
	public static final int LAST_CORNER_KEY = 5;
	
	private static Map colorMap = new HashMap();
	private static Map<Integer, Cursor> idToCursorMap = new HashMap<Integer, Cursor>();
	private static Map fontMap = new HashMap();
	private static Map imageMap = new HashMap();
	private static Map fontToBoldFontMap = new HashMap();
	private static Map[] decoratedImageMap = new Map[LAST_CORNER_KEY];
	
	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}
	
	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}

	public static Color getColor(RGB rgb) {
		Color color = (Color) colorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			colorMap.put(rgb, color);
		}
		return color;
	}

	public static void disposeColors() {
		for (Iterator scan = colorMap.values().iterator(); scan.hasNext();) {
			((Color) scan.next()).dispose();
		}
		colorMap.clear();
	}

	protected static Image getImage(InputStream stream) throws IOException {
		try {
			Display display = Display.getCurrent();
			ImageData data = new ImageData(stream);
			if (data.transparentPixel > 0) {
				return new Image(display, data, data.getTransparencyMask());
			}
			return new Image(display, data);
		} finally {
			stream.close();
		}
	}

	public static Image getImage(String path) {
		Image image = (Image) imageMap.get(path);
		if (image == null) {
			try {
				image = getImage(new FileInputStream(path));
				imageMap.put(path, image);
			} catch (Exception e) {
				image = getMissingImage();
				imageMap.put(path, image);
			}
		}
		return image;
	}

	public static Image getImage(Class clazz, String path) {
		String key = clazz.getName() + '|' + path;
		Image image = (Image) imageMap.get(key);
		if (image == null) {
			try {
				image = getImage(clazz.getResourceAsStream(path));
				imageMap.put(key, image);
			} catch (Exception e) {
				image = getMissingImage();
				imageMap.put(key, image);
			}
		}
		return image;
	}

	private static Image getMissingImage() {
		Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);

		GC gc = new GC(image);
		gc.setBackground(getColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		gc.dispose();

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
			Rectangle bib = baseImage.getBounds();
			Rectangle dib = decorator.getBounds();
			result = new Image(Display.getCurrent(), bib.width, bib.height);

			GC gc = new GC(result);
			gc.drawImage(baseImage, 0, 0);
			if (corner == TOP_LEFT) {
				gc.drawImage(decorator, 0, 0);
			} else if (corner == TOP_RIGHT) {
				gc.drawImage(decorator, bib.width - dib.width, 0);
			} else if (corner == BOTTOM_LEFT) {
				gc.drawImage(decorator, 0, bib.height - dib.height);
			} else if (corner == BOTTOM_RIGHT) {
				gc.drawImage(decorator, bib.width - dib.width, bib.height - dib.height);
			}
			gc.dispose();
			decoratedMap.put(decorator, result);
		}
		return result;
	}

	public static void disposeImages() {
		for (Iterator scan = imageMap.values().iterator(); scan.hasNext();) {
			((Image) scan.next()).dispose();
		}
		imageMap.clear();

		for (int i = 0; i < decoratedImageMap.length; i++) {
			Map cornerDecoratedImageMap = decoratedImageMap[i];
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
	}

	public static Font getFont(String name, int height, int style) {
		return getFont(name, height, style, false, false);
	}

	public static Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
		String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
		Font font = (Font) fontMap.get(fontName);
		if (font == null) {
			FontData fontData = new FontData(name, size, style);
			if (strikeout || underline) {
				try {
					Class logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
					Object logFont = FontData.class.getField("data").get(fontData); //$NON-NLS-1$
					if (logFont != null) {
						if (logFontClass != null) {
							if (strikeout) {
								logFontClass.getField("lfStrikeOut").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
							}
							if (underline) {
								logFontClass.getField("lfUnderline").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
							}
						}
					}
				} catch (Throwable exception) {
					System.err.println("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + exception); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			font = new Font(Display.getCurrent(), fontData);
			fontMap.put(fontName, font);
		}
		return font;
	}

	public static Font getBoldFont(Font baseFont) {
		Font font = (Font) fontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData[] fontDatas = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), data.getHeight(), SWT.BOLD);
			fontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}

	public static void disposeFonts() {
		for (Iterator scan = fontMap.values().iterator(); scan.hasNext();) {
			((Font) scan.next()).dispose();
		}
		fontMap.clear();

		for (Iterator scan = fontToBoldFontMap.values().iterator(); scan.hasNext();) {
			((Font) scan.next()).dispose();
		}
		fontToBoldFontMap.clear();
	}

	public static Cursor getCursor(int id) {
		Integer key = Integer.valueOf(id);
		Cursor cursor = idToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			idToCursorMap.put(key, cursor);
		}
		return cursor;
	}

	public static void disposeCursors() {
		for (Cursor cursor : idToCursorMap.values()) {
			cursor.dispose();
		}
		idToCursorMap.clear();
	}

	public static void dispose() {
		disposeColors();
		disposeImages();
		disposeFonts();
		disposeCursors();
	}
}
