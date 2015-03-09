package org.birenheide.bf.debug.ui;

import org.birenheide.bf.ed.BfEditor;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class WatchpointExtensionFactory extends ExtensionContributionFactory {

	public WatchpointExtensionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		CommandContributionItemParameter toggleWatchpointParam = new CommandContributionItemParameter(serviceLocator, null, "org.eclipse.debug.ui.commands.ToggleWatchpoint", CommandContributionItem.STYLE_PUSH);
		toggleWatchpointParam.icon = DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_WATCHPOINT);
		toggleWatchpointParam.disabledIcon = DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_WATCHPOINT_DISABLED);
		toggleWatchpointParam.label = "Add Watchpoint";
		CommandContributionItem toggleWatchpoint = new CommandContributionItem(toggleWatchpointParam);
		Expression toggleWatchpointVisible = new Expression() {
			
			@Override
			public EvaluationResult evaluate(IEvaluationContext context)
					throws CoreException {
				return EvaluationResult.valueOf((context.getVariable("activeEditor") instanceof BfEditor));
			}
		};
		additions.addContributionItem(toggleWatchpoint, toggleWatchpointVisible);
	}
}
