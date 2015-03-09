package org.birenheide.bf.ed;

import org.birenheide.bf.core.BfActivator;

public interface EditorConstants {

	//Preferences 
	//Preference Pages
	public static final String PREF_PAGE_EDITOR_ID = "org.birenheide.bf.Editor";
	static final String PREF_PAGE_GENERAL_TEXT_EDITOR_ID = "org.eclipse.ui.preferencePages.GeneralTextEditor";
	
	//Preference Keys
	/**
	 * Key for "Highlight matching brackets": {@value}.
	 */
	public static final String PREF_EDITOR_MATCHING_BRACKETS = BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBrackets";
	/**
	 * Key for "Highlight matching brackets color": {@value}.
	 */
	public static final String PREF_EDITOR_MATCHING_BRACKETS_COLOR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBracketsColor";
	/**
	 * Key for "Show caret in matching brackets": {@value}.
	 */
	public static final String PREF_EDITOR_MATCHING_BRACKETS_SHOW_CARET = BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBracketsShowCaretLocation";
	/**
	 * Key for "Highlight enclosing matching brackets": {@value}.
	 */
	public static final String PREF_EDITOR_MATCHING_BRACKETS_SHOW_ENCLOSING = BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBracketsShowEnclosedBrackets";
	/**
	 * Key for "Close brackets automatically": {@value}.
	 */
	public static final String PREF_EDITOR_CLOSE_BRACKET= BfActivator.BUNDLE_SYMBOLIC_NAME + ".closeBracket";
	/**
	 * Key for "Key character color": {@value}.
	 */
	public static final String PREF_EDITOR_KEY_CHAR_COLOR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".keyCharColor";
	/**
	 * Key for "Other characters color": {@value}.
	 */
	public static final String PREF_EDITOR_OTHER_CHAR_COLOR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".otherCharColor";
	/**
	 * Key for "Comment characters color": {@value}.
	 */
	public static final String PREF_EDITOR_COMMENT_CHAR_COLOR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".commentCharColor";
	/**
	 * Key for "Template characters color": {@value}.
	 */
	public static final String PREF_EDITOR_TEMPLATE_PARAMS_COLOR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".templateParamsColor";
	

	//Partitioning
	static final String BF_PARTITIONING = "Brainfuck_Partitioning";
	static final String PARTITION_TYPE_BRAINFUCK_CODE = "__brainfuck_code";
	static final String PARTITION_TYPE_TEMPLATE_PARAMETERS = "__template_parameters";
	static final String PARTITION_TYPE_MULTILINE_COMMENT = "__brainfuck_multiline_comment";
	static final String[] BRAINFUCK_PARTITION_TYPES = 
	new String[] {
		PARTITION_TYPE_BRAINFUCK_CODE, 
		PARTITION_TYPE_TEMPLATE_PARAMETERS, 
		PARTITION_TYPE_MULTILINE_COMMENT
		};

	/**
	 * File extension for Brainfuck files: {@value}.
	 */
	public static final String BF_FILE_EXTENSION = "bf";
	
	
	/**
	 * Editor Id for Brainfuck editor: {@value}.
	 */
	public static final String EDITOR_ID = "org.birenheide.bf.BrainfuckEditor";



	
}
