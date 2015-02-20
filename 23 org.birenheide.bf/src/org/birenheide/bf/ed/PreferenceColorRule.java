package org.birenheide.bf.ed;

import java.util.Map;
import java.util.TreeMap;

import org.birenheide.bf.Pair;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Abstract base class for syntax coloring rules.
 * Creates out new tokens only when necessary.
 * @author Richard Birenheide
 *
 */
abstract class PreferenceColorRule implements IRule {
	
	private final IPreferenceStore store;
	private final Map<String, Pair<RGB, IToken>> tokens = new TreeMap<>();
	
	PreferenceColorRule(IPreferenceStore store) {
		this.store = store;
	}
	
	IToken getToken(String foregroundColorKey) {
		RGB requiredColor = PreferenceConverter.getColor(store, foregroundColorKey);
		if (!this.tokens.containsKey(foregroundColorKey) || !this.tokens.get(foregroundColorKey).first.equals(requiredColor)) {
			Color newColor = EditorsUI.getSharedTextColors().getColor(requiredColor);
			IToken newToken = new Token(new TextAttribute(newColor, null, this.getStyle(foregroundColorKey)));
			this.tokens.put(foregroundColorKey, new Pair<>(requiredColor, newToken));
		}
		return this.tokens.get(foregroundColorKey).second;
	}
	
	int getStyle(String foregroundColorKey) {
		return SWT.NORMAL;
	}
}
