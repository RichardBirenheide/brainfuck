package org.birenheide.bf;

import java.io.IOException;

import org.birenheide.bf.debug.ui.BfUIListenerContributor;
import org.birenheide.bf.ed.template.BfTemplateType;
import org.birenheide.bf.ed.template.ParametrizedTemplateTypeDescriptor;
import org.birenheide.bf.ui.BfImages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BfActivator extends AbstractUIPlugin {
	
	public static final String BUNDLE_SYMBOLIC_NAME = "org.birenheide.bf";
	
	private static BfActivator instance = null;
	
	private final BfUIListenerContributor uiContributor = new BfUIListenerContributor();
	private TemplateStore templateStore = null;
	private ContextTypeRegistry registry = null;

	public final static String BF_PROBLEM_MARKER_ID = BUNDLE_SYMBOLIC_NAME + ".brainfuckProblemMarker";
	
	public static BfActivator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		this.uiContributor.addListeners();
		this.registry = new ContributionContextTypeRegistry(BfTemplateType.REGISTRY_ID);
//		this.registry.addContextType(new BfTemplateType("org.birenheide.bf.brainfuck.p1", "1 Parameter"));
		for (ParametrizedTemplateTypeDescriptor desc : ParametrizedTemplateTypeDescriptor.values()) {
			this.registry.addContextType(desc.templateType);
		}
		this.templateStore = new ContributionTemplateStore(registry, this.getPreferenceStore(), BfPreferenceInitializer.TEMPLATE_KEY);
		try {
			this.templateStore.load();
		} 
		catch (IOException e) {
			logError("Templates coud not be loaded", e);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		this.uiContributor.removeListeners();
		this.templateStore = null;
		this.registry = null;
		super.stop(context);
	}
	
	public TemplateStore getTemplateStore() {
		return this.templateStore;
	}
	
	public ContextTypeRegistry getTemplateContextTypeRegistry() {
		return this.registry;
	}
	
    public void logError(String message, Throwable cause) {

        this.log(message, cause, IStatus.ERROR);
    }

    public void logInfo(String message, Throwable cause) {

        this.log(message, cause, IStatus.INFO);
    }

    public void logWarning(String message, Throwable cause) {

        this.log(message, cause, IStatus.WARNING);
    }

    @Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		BfImages.initializeImageRegistry(reg);
	}

	private void log(String message, Throwable cause, int severity) {

        String pluginId = this.getBundle() != null ? this.getBundle().getSymbolicName() : BUNDLE_SYMBOLIC_NAME;
        if (message == null) {
            message = cause != null ? cause.getMessage() : "";
        }
        if (cause != null) {
            this.getLog().log(new Status(severity, pluginId, message, cause));
            if (this.isDebugging()) {
                System.err.println("Message: " + message);
                cause.printStackTrace();
            }
        }
        else {
            this.getLog().log(new Status(severity, pluginId, message));
            if (this.isDebugging()) {
                System.err.println("Message: " + message);
            }
        }
    }
	
}
