package org.birenheide.bf.core;

import java.io.IOException;

import org.birenheide.bf.AbstractBfActivator;
import org.birenheide.bf.BfPreferenceInitializer;
import org.birenheide.bf.ed.EditorCloseListener;
import org.birenheide.bf.ed.template.BfTemplateType;
import org.birenheide.bf.ed.template.ParametrizedTemplateTypeDescriptor;
import org.birenheide.bf.ui.BfImages;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.osgi.framework.BundleContext;

public class BfActivator extends AbstractBfActivator {
	
	public static final String BUNDLE_SYMBOLIC_NAME = "org.birenheide.bf";
	
	private static BfActivator instance = null;
	

	private TemplateStore templateStore = null;
	private ContextTypeRegistry registry = null;
	private final IPartListener2 editorCloseListener = new EditorCloseListener();

	public final static String BF_PROBLEM_MARKER_ID = BUNDLE_SYMBOLIC_NAME + ".brainfuckProblemMarker";
	
	public static BfActivator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
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
		Display.getDefault().asyncExec(() -> {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(editorCloseListener);
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		
		this.templateStore = null;
		this.registry = null;
		Display.getDefault().asyncExec(() -> {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(editorCloseListener);
		});
		super.stop(context);
	}
	
	public TemplateStore getTemplateStore() {
		return this.templateStore;
	}
	
	public ContextTypeRegistry getTemplateContextTypeRegistry() {
		return this.registry;
	}
	
    @Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		BfImages.initializeImageRegistry(reg);
	}

	@Override
	protected String getDefaultPluginId() {
		return BUNDLE_SYMBOLIC_NAME;
	}
	
}
