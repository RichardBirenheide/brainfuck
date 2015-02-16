package org.birenheide.bf.ui;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.birenheide.bf.BfActivator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * Holds plugin information about images used.
 * @author Richard Birenheide
 *
 */
public class BfImages {

	public static final String ICON_BF = BfActivator.BUNDLE_SYMBOLIC_NAME + ".BfIcon";
	
	private static final Map<String, String> PATHS = new TreeMap<>();
	
	static {
		PATHS.put(ICON_BF, "icons/brainfuck.gif");
	}
	
	/**
	 * Should only be used by {@link BfActivator} to register the images.
	 * Use {@link BfActivator#getImageRegistry()} to obtain images. Works
	 * only with images listed in this class.
	 * @param id the id of the image.
	 * @return the descriptor.
	 */
	private static ImageDescriptor createImageDescriptor(String id, String path) {
		URL imageUrl = BfActivator.getDefault().getBundle().getEntry(path);
		return ImageDescriptor.createFromURL(imageUrl);
	}
	
	/**
	 * Should only be used by {@link BfActivator} to register the images.
	 * Use {@link BfActivator#getImageRegistry()} to obtain images. Works
	 * only with images listed in this class.
	 * @param registry the registry.
	 */
	public static void initializeImageRegistry(ImageRegistry registry) {
		for (Entry<String, String> entry : PATHS.entrySet()) {
			ImageDescriptor descriptor = createImageDescriptor(entry.getKey(), entry.getValue());
			registry.put(entry.getKey(), descriptor);
		}
	}
}
