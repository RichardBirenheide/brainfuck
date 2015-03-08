package org.birenheide.bf.debug;

import org.birenheide.bf.AbstractBfActivator;
import org.birenheide.bf.debug.ui.BfUIListenerContributor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DbgActivator extends AbstractBfActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.birenheide.bf.debug"; //$NON-NLS-1$

	// The shared instance
	private static DbgActivator plugin;
	
	private final BfUIListenerContributor uiContributor = new BfUIListenerContributor();
	/**
	 * The constructor
	 */
	public DbgActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.uiContributor.addListeners();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.uiContributor.removeListeners();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DbgActivator getDefault() {
		return plugin;
	}

	@Override
	protected String getDefaultPluginId() {
		return PLUGIN_ID;
	}
}
