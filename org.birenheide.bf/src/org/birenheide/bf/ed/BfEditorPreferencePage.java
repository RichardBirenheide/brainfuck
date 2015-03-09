package org.birenheide.bf.ed;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;

public class BfEditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private static final String LINK_TEXT = "Brainfuck editor preferences. "
			+ "See <a href=\"org.eclipse.ui.preferencePages.GeneralTextEditor\">'Text Editors'</a> "
			+ "for general text editor preferences and " 
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations\">'Annotations'</a> " 
			+ "to have the exact "
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations#Brainfuck Breakpoints\">breakpoint</a> "
			+ "point location and "
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations#Brainfuck Instruction Pointer\">instruction</a> "
			+ "pointer location being displayed.";
	private static final String TITLE = "Brainfuck Editor"; 
	private static final String RESTORE_DEFAULTS_TEXT = "Restore Defaults";
	private static final String COLOR_LABEL_TEXT = "Color:";
	
	private Button bracketHighlighting = null;
	private Button showMatchingBracket = null;
	private Button showCaretLocation = null;
	private Button showEnclosingBrackets = null;
	
	private Button closeBrackets = null;
	
	private List colorPreferenceList = null;
	private java.util.List<ColorPreference> colorPreferenceStore = Arrays.asList(
				new ColorPreference(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_COLOR, "Matching brackets highlight"),
				new ColorPreference(EditorConstants.PREF_EDITOR_KEY_CHAR_COLOR, "Key characters color"),
				new ColorPreference(EditorConstants.PREF_EDITOR_COMMENT_CHAR_COLOR, "Comment character color"),
				new ColorPreference(EditorConstants.PREF_EDITOR_TEMPLATE_PARAMS_COLOR, "Template parameter character color"),
				new ColorPreference(EditorConstants.PREF_EDITOR_OTHER_CHAR_COLOR, "Non-key character color")
			);
	private ColorSelector colorSelector;
	

	public BfEditorPreferencePage() {
		super(TITLE);
		this.setMessage(TITLE);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(GridDataFactory.fillDefaults().create());
		area.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		
		Link intro = new Link(area, SWT.NONE);
		intro.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).span(2, 1).hint(150, SWT.DEFAULT).create());
		intro.setText(LINK_TEXT);
		intro.addSelectionListener(new LinkSelectionListener());
		
		Point newParagraph = new Point(0, this.convertHeightInCharsToPixels(1));
		
		this.closeBrackets = new Button(area, SWT.CHECK);
		this.closeBrackets.setLayoutData(GridDataFactory.swtDefaults().indent(newParagraph).span(2, 1).create());
		this.closeBrackets.setText("Close brackets automatically");
		
		this.bracketHighlighting = new Button(area, SWT.CHECK);
		this.bracketHighlighting.setLayoutData(GridDataFactory.swtDefaults().indent(newParagraph).span(2, 1).create());
		this.bracketHighlighting.setText("Bracket highlighting");
		this.bracketHighlighting.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setBracketGroupEnablement();
			}
		});
		
		Composite bracketHighlightGroup = new Composite(area, SWT.NONE);
		int indent = this.convertWidthInCharsToPixels(3);
		bracketHighlightGroup.setLayoutData(GridDataFactory.swtDefaults().indent(indent, 0).align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).create());
		bracketHighlightGroup.setLayout(GridLayoutFactory.fillDefaults().create());
		
		this.showMatchingBracket = new Button(bracketHighlightGroup, SWT.RADIO);
		this.showMatchingBracket.setLayoutData(GridDataFactory.swtDefaults().create());
		this.showMatchingBracket.setText("Matching bracket");
		
		this.showCaretLocation = new Button(bracketHighlightGroup, SWT.RADIO);
		this.showCaretLocation.setLayoutData(GridDataFactory.swtDefaults().create());
		this.showCaretLocation.setText("Matching bracket and caret location");
		
		this.showEnclosingBrackets = new Button(bracketHighlightGroup, SWT.RADIO);
		this.showEnclosingBrackets.setLayoutData(GridDataFactory.swtDefaults().create());
		this.showEnclosingBrackets.setText("Enclosing brackets");
		
		Composite colorGroup = new Composite(area, SWT.NONE);
		colorGroup.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(newParagraph).span(2, 1).create());
		colorGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		
		Label colorOptionsLabel = new Label(colorGroup, SWT.NONE);
		colorOptionsLabel.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		colorOptionsLabel.setText("Appearance color options:");
		
		this.colorPreferenceList = new List(colorGroup, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		int heightHint = this.colorPreferenceList.getItemHeight() * 8;
		this.colorPreferenceList.setLayoutData(GridDataFactory.swtDefaults().hint(SWT.DEFAULT, heightHint).create());
		for (int i = 0; i < this.colorPreferenceStore.size(); i++) {
			this.colorPreferenceList.add(this.colorPreferenceStore.get(i).getLabel(), i);
		}
		
		Composite colorButtonGroup = new Composite(colorGroup, SWT.NONE);
		colorButtonGroup.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).create());
		colorButtonGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		
		Label colorButtonLabel = new Label(colorButtonGroup, SWT.NONE);
		colorButtonLabel.setLayoutData(GridDataFactory.swtDefaults().create());
		colorButtonLabel.setText(COLOR_LABEL_TEXT);
		
		int selectorButtonWidth = this.convertWidthInCharsToPixels(RESTORE_DEFAULTS_TEXT.length()) - this.convertWidthInCharsToPixels(COLOR_LABEL_TEXT.length() + 1);
		this.colorSelector = new ColorSelector(colorButtonGroup);
		this.colorSelector.getButton().setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(selectorButtonWidth, SWT.DEFAULT).create());
		
		Button colorDefaults = new Button(colorButtonGroup, SWT.PUSH);
		colorDefaults.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		colorDefaults.setText(RESTORE_DEFAULTS_TEXT);
		colorDefaults.setToolTipText("Restore defaults\nfor colors only");
		colorDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initializeColorValues(true);
				setColorListSelection(-1);
			}
		});
		
		this.colorPreferenceList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = colorPreferenceList.getSelectionIndex();
				if (index > -1 && index < colorPreferenceStore.size()) {
					colorSelector.setColorValue(colorPreferenceStore.get(index).getValue());
				}
			}
		});

		this.colorSelector.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				int index = colorPreferenceList.getSelectionIndex();
				if (index > -1 && index < colorPreferenceStore.size()) {
					colorPreferenceStore.get(index).setValue((RGB) event.getNewValue());
				}
			}
		});
		
		this.initializeValues(false);
		this.setColorListSelection(0);
		return area;
	}
	

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}
	
	@Override
	protected void performDefaults() {
		this.initializeValues(true);
		this.setColorListSelection(-1);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = this.getPreferenceStore();
		boolean editorCloseBracket = this.closeBrackets.getSelection();
		store.setValue(EditorConstants.PREF_EDITOR_CLOSE_BRACKET, editorCloseBracket);
		
		boolean highlightBrackets = this.bracketHighlighting.getSelection();
		store.setValue(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS, highlightBrackets);
		
		boolean showCaretLocation = false;
		boolean showEnclosing = false;
		if (this.showMatchingBracket.getSelection()) {
			//Do nothing
		}
		else if (this.showCaretLocation.getSelection()) {
			showCaretLocation = true;
			showEnclosing = false;
		}
		else if (this.showEnclosingBrackets.getSelection()) {
			showCaretLocation = true;
			showEnclosing = true;
		}
		store.setValue(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_CARET, showCaretLocation);
		store.setValue(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_ENCLOSING, showEnclosing);
		
		for (ColorPreference pref : this.colorPreferenceStore) {
			pref.writeValue();
		}
		return true;
	}

	private void setColorListSelection(int index) {
		if (index == -1) {
			index = Math.max(this.colorPreferenceList.getSelectionIndex(), 0);
		}
		if (index < 0 || index >= this.colorPreferenceStore.size()) {
			return;
		}
		this.colorPreferenceList.setSelection(index);
		this.colorSelector.setColorValue(this.colorPreferenceStore.get(index).getValue());
	}
	
	private void initializeValues(boolean setDefaults) {
		IPreferenceStore store = this.getPreferenceStore();
		boolean editorCloseBracket = setDefaults ? store.getDefaultBoolean(EditorConstants.PREF_EDITOR_CLOSE_BRACKET) : store.getBoolean(EditorConstants.PREF_EDITOR_CLOSE_BRACKET);
		this.closeBrackets.setSelection(editorCloseBracket);
		
		boolean highlightBrackets = setDefaults ? store.getDefaultBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS) : store.getBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS);
		this.bracketHighlighting.setSelection(highlightBrackets);

		boolean showCaret = setDefaults ? store.getDefaultBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_CARET) : store.getBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_CARET);
		boolean showEnclosing = setDefaults ? store.getDefaultBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_ENCLOSING) : store.getBoolean(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_ENCLOSING);
		this.showMatchingBracket.setSelection(!showEnclosing && !showCaret);
		this.showCaretLocation.setSelection(showCaret && !showEnclosing);
		this.showEnclosingBrackets.setSelection(showEnclosing && showCaret);
		this.setBracketGroupEnablement();
		
		this.initializeColorValues(setDefaults);
	}
	
	private void setBracketGroupEnablement() {
		boolean active = bracketHighlighting.getSelection();
		showMatchingBracket.setEnabled(active);
		showCaretLocation.setEnabled(active);
		showEnclosingBrackets.setEnabled(active);
	}
	
	private void initializeColorValues(boolean setDefaults) {
		for (ColorPreference pref : this.colorPreferenceStore) {
			pref.initializeValue(setDefaults);
		}
	}
	

	/**
	 * @author Richard Birenheide
	 *
	 */
	private class LinkSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String linkText = e.text;
			String additionalInfo = null;
			if (linkText.contains("#")) {
				String[] parts = linkText.split("#");
				linkText = parts[0];
				additionalInfo = parts[1];
			}
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), linkText, null, additionalInfo);
			dialog.open();
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private class ColorPreference {
		private final String label;
		private final String preferenceKey;
		private RGB value;
		
		ColorPreference(String preferenceKey, String label) {
			this.label = label;
			this.preferenceKey = preferenceKey;
		}
		
		void setValue(RGB value) {
			this.value = value;
		}
		
		RGB getValue() {
			return this.value;
		}
		
		void initializeValue(boolean setDefault) {
			IPreferenceStore store = getPreferenceStore();
			this.value = setDefault 
					? PreferenceConverter.getDefaultColor(store, preferenceKey) 
							: PreferenceConverter.getColor(store, preferenceKey);
		}
		
		void writeValue() {
			IPreferenceStore store = getPreferenceStore();
			PreferenceConverter.setValue(store, preferenceKey, value);
		}
		
		String getLabel() {
			return this.label;
		}
	}
}
