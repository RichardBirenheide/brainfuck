package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.BrainfuckInterpreter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.editors.text.EditorsUI;

class BfCodeScanner extends RuleBasedScanner {
	
	
	
	BfCodeScanner(IPreferenceStore editorPreferenceStore) {
		
		List<IRule> rules= new ArrayList<>();
		
		rules.add(new BfRule(editorPreferenceStore));
		this.setRules(rules.toArray(new IRule[rules.size()]));
	}
	
	
	private static class BfRule implements IRule {
		
		private final IPreferenceStore editorPreferenceStore;
		
		BfRule(IPreferenceStore editorPreferenceStore) {
			this.editorPreferenceStore = editorPreferenceStore;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			if (BrainfuckInterpreter.isReservedChar(value)) {
				Color foreGround = EditorsUI.
						getSharedTextColors().
						getColor(PreferenceConverter.getColor(
								this.editorPreferenceStore, 
								BfEditor.EDITOR_KEY_CHAR_COLOR_PREF));
				return new Token(new TextAttribute(foreGround, null, SWT.BOLD));
			}
			Color foreGround = EditorsUI.
					getSharedTextColors().
					getColor(PreferenceConverter.getColor(
							this.editorPreferenceStore, 
							BfEditor.EDITOR_OTHER_CHAR_COLOR_PREF));
			return new Token(new TextAttribute(foreGround));
		}
		
	}
}
