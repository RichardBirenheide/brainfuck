package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.BrainfuckInterpreter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;

class BfCodeScanner extends BufferedRuleBasedScanner {
	
	BfCodeScanner(IPreferenceStore editorPreferenceStore) {
		
		List<IRule> rules= new ArrayList<>();
		
		rules.add(new BfCodeColorRule(editorPreferenceStore));
		this.setRules(rules.toArray(new IRule[rules.size()]));
	}
	
	
	private static class BfCodeColorRule extends PreferenceColorRule {
		
		BfCodeColorRule(IPreferenceStore editorPreferenceStore) {
			super(editorPreferenceStore);
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			if (BrainfuckInterpreter.isReservedChar(value)) {
				return this.getToken(EditorConstants.PREF_EDITOR_KEY_CHAR_COLOR);
			}
			return this.getToken(EditorConstants.PREF_EDITOR_OTHER_CHAR_COLOR);
		}

		@Override
		int getStyle(String foregroundColorKey) {
			if (foregroundColorKey.equals(EditorConstants.PREF_EDITOR_KEY_CHAR_COLOR)) {
				return SWT.BOLD;
			}
			return super.getStyle(foregroundColorKey);
		}
	}
}
