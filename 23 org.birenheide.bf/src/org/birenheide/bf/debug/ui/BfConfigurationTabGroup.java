package org.birenheide.bf.debug.ui;

import org.birenheide.bf.ui.HelpContext;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class BfConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		
		BfMainTab mainTab = new BfMainTab();
		mainTab.setHelpContextId(HelpContext.DEBUG_ID);

		CommonTab commonTab = new CommonTab();
		commonTab.setHelpContextId(HelpContext.DEBUG_ID);
		
		this.setTabs(new ILaunchConfigurationTab[] {mainTab, commonTab});
	}
}
