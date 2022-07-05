package p6.codeclarity;

import java.io.File;
import java.util.HashMap;

import p6.codeclarity.expansion.ExpandedCodeClarityContentGenerator;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		 * -outtext: set the output to a text file
		 * 
		 * -path: program path
		 * 
		 * -akpath: additional keyword path
		 * 
		 * -proglang: programming language ('py' or 'java')
		 * 
		 * -humanlang: human language ('en', 'id', or 'iden')
		 * 
		 * -reindex: reindex word database, used when the human language is just changed
		 * 
		 * -reselect: define applied code quality checks
		 * 
		 */

		// if no arguments given, show help
		if (args.length == 0) {
			showHelp();
			return;
		}

		// get all the setting tuples
		HashMap<String, String> settingTuples = new HashMap<String, String>();
		int i = 0;
		while (i < args.length) {
			String cur = args[i];
			if (cur.startsWith("-")) {
				// this is a correct argument
				if (cur.equals("-path") || cur.equals("-akpath") || cur.equals("-proglang") || cur.equals("-humanlang")
						|| cur.equals("-reselect")) {
					// keywords requiring a value
					if (++i < args.length) {
						if (args[i].startsWith("-") == false) {
							// get the value and add a new setting tuple
							settingTuples.put(cur, args[i]);
						} else {
							// error the next element is another keyword
							System.err.println("Argument '" + args[i]
									+ "' is expected to be a setting value but another keyword is given");
							System.err.println("Run this program without arguments to show help");
							return;
						}
					} else {
						// error the next element is expected but empty
						System.err.println(
								"Argument " + (i + 1) + " is expected to be a setting value but none is given");
						System.err.println("Run this program without arguments to show help");
						return;
					}
				} else if (cur.equals("-reindex") || cur.equals("-outtext")) {
					// keywords without any values
					settingTuples.put(cur, "true");
				} else {
					// error the read string is not recognised
					System.err.println("Argument '" + args[i] + "' is not recognised as a setting keyword");
					System.err.println("Run this program without arguments to show help");
					return;
				}
			} else {
				// error the read string is not a setting keyword
				System.err.println(
						"Argument '" + args[i] + "' is expected to be a setting keyword started with hypen ('-')");
				System.err.println("Run this program without arguments to show help");
				return;
			}
			i++;
		}

		// checking program path
		String path = settingTuples.get("-path");
		if (path == null) {
			System.err.println("No program path is given");
			System.err.println("Run this program without arguments to show help");
			return;
		} else {
			path = preparePathOrRegex(path);
			if (isPathValidAndExist(path) == false) {
				System.err.println("Program path is not valid or refers to a nonexistent file.");
				System.err.println("Run this program without arguments to show help");
				return;
			}
		}

		// checking programming language
		String programmingLanguageCode = settingTuples.get("-proglang");
		if (programmingLanguageCode == null) {
			System.err.println("No programming language is defined");
			System.err.println("Run this program without arguments to show help");
			return;
		} else {
			if (isProgrammingLanguageValid(programmingLanguageCode) == false) {
				System.err.println("Programming language should be either 'java' or 'py'");
				System.err.println("Run this program without arguments to show help");
				return;
			}
		}

		// checking human language
		String languageCode = settingTuples.get("-humanlang");
		if (languageCode == null) {
			// if no human language is set, define as english ("en")
			languageCode = "en";
		}
		// check whether the human language is valid
		if (isHumanLanguageValid(languageCode) == false) {
			System.err.println("Human language should be either 'en', 'id', or 'iden'");
			System.err.println("Run this program without arguments to show help");
			return;
		}

		// checking additional keywords
		String additionalKeywordsPath = settingTuples.get("-akpath");
		if (additionalKeywordsPath == null) {
			if (programmingLanguageCode.equals("java"))
				additionalKeywordsPath = "java input output keywords.txt";
			else if (programmingLanguageCode.equals("py"))
				additionalKeywordsPath = "python input output keywords.txt";
		}
		// check whether the additional keywords file path is valid
		if (additionalKeywordsPath != null && isPathValidAndExist(additionalKeywordsPath) == false) {
			System.err.println("Additional keywords file path is not valid or refers to a nonexistent file.");
			System.err.println("Run this program without arguments to show help");
			return;
		}

		// check whether user prefers some suggestions
		String[] appliedSuggestions = new String[0];

		if (settingTuples.get("-reselect") != null) {
			// check whether the selected suggestions are all valid
			appliedSuggestions = settingTuples.get("-reselect").split(",");
			String[] availableSuggestionCodes = new String[] { "S01", "S02", "S03", "S04", "S05", "S06", "S07", "S08",
					"J01", "J02", "J03", "J04", "J05", "J06", "J07", "J08", "J09", "J10", "J11", "J12", "J13", "J14",
					"J15", "J16", "J17", "J18", "J19", "J20", "J21", "J22", "J23", "J24", "P01", "P02", "P03", "P04",
					"P05", "P06", "P07", "P08", "P09", "P10", "P11", "P12", "P13" };
			for (String suggestion : appliedSuggestions) {
				boolean isFound = false;

				// check if the suggestion is supported by given programming language
				if ((programmingLanguageCode.equals("java")
						&& (suggestion.startsWith("S") || suggestion.startsWith("J")))
						|| (programmingLanguageCode.equals("py")
								&& (suggestion.startsWith("S") || suggestion.startsWith("P")))) {
					// search from the list of available suggestions
					for (int k = 0; k < availableSuggestionCodes.length; k++) {
						if (suggestion.equals(availableSuggestionCodes[k])) {
							isFound = true;
							break;
						}
					}
				}
				// if not found, stop the process and print error
				if (!isFound) {
					System.err.println("Suggestion code '" + suggestion
							+ "' is either invalid or unsupported for given programming language");
					System.err.println("Run this program without arguments to show help");
					return;
				}
			}
		}

		// remaining boolean arguments

		boolean reindex = false;
		if (settingTuples.get("-reindex") != null)
			reindex = true;

		boolean isOutText = false;
		if (settingTuples.get("-outtext") != null)
			isOutText = true;

		// start to process
		ExpandedCodeClarityContentGenerator.execute(path, additionalKeywordsPath, programmingLanguageCode, languageCode,
				reindex, isOutText, appliedSuggestions);

	}

	private static void showHelp() {
		println("Code quality suggester (CQS) aims to educate computing students about code quality in programming.");
		println("It scans Java/Python program code and It scans Java/Python program code and highlights code quality issues.");
		println("To avoid over information, only one instance per issue is reported.");
		println("Students are expected to check their whole program while fixing the issue.");
		println("For convenience, the suggestion is mapped to an interactive HTML file.");

		println("\nMinimum command: -path <program_path> -proglang <programming_language>");
		println("  <program_path> is the complete path of targeted program code file.");
		println("  <programming_language> refers to the programming language used in the program code.");
		println("    values: 'java' (for Java) or 'py' (for Python).");
		println("  The result can be seen in 'out.html'.");

		println("\nAdditional arguments:");
		println("  -akpath <additional_keyword_path>");
		println("    It is applicable when some identifiers need to be recognised as keywords. This typically");
		println("    happens when the program uses third-party libraries. <additional_keywords_path> refers");
		println("    to a file containing additional keywords with newline as the delimiter. Keywords with");
		println("    more than one token should be written by embedding spaces between the tokens. For example,");
		println("    'System.out.print' should be written as 'System . out . print'.");
		println("  -reselect <applied_suggestions>");
		println("    This enables only a set of suggestions applied. <applied_suggestions> lists codes of the");
		println("    suggestions as comma separated values. Full list of the codes can be seen below");
		println("  -humanlang <human_language>");
		println("    This changes the human language used while delivering the suggestions.");
		println("    <human_language> values: 'en' for both British and American English (default), 'id' for ");
		println("      Indonesian, and 'iden' for Indonesian and English.");
		println("  -reindex");
		println("    This forces CCS to reindex word database for spell checking. Please use it only when the ");
		println("    human language is changed as the process is quite time consuming.");
		println("  -outtext");
		println("    This alters the resulted output to a standard text file ('out.txt'). This might be useful");
		println("    when CCS is integrated to larger system.");

		println("\nCCS general suggestion codes:");
		println("S01: Overly short identifier");
		println("S02: Not meaningful identifier");
		println("S03: Incorrectly written identifier");
		println("S04: Inconsistent transition among identifier words");
		println("S05: Overly short comment");
		println("S06: Not meaningful comment");
		println("S07: Incorrectly written comment");
		println("S08: No comments around syntax block");

		println("\nCCS Java-specific suggestion codes:");
		println("J01: Less explicit array data type (e.g., 'int arr[]' instead of 'int[] arr'");
		println("J02: Inconsistent use of abstract class name and/or 'Abstract' prefix");
		println("J03: Using inline conditional");
		println("J04: Overly complex boolean expression");
		println("J05: Constant name with lowercased letters");
		println("J06: Incorrect declaration order (expectation: static variables, attributes, constructors,");
		println("     and other methods)");
		println("J07: Empty block");
		println("J08: Empty catch block");
		println("J09: Improper use of empty line(s) to separate syntax blocks");
		println("J10: Semicolon without program statement");
		println("J11: Too many program statements in a method");
		println("J12: Multiple variable declaration");
		println("J13: No braces for single-statement body of syntax block (e.g., loop or if-else");
		println("J14: Overly deep nested loop block");
		println("J15: Overly deep nested if-else block");
		println("J16: Overly deep nested try-catch block");
		println("J17: Too many program statements in one line");
		println("J18: 'this' keyword is not used to access attributes");
		println("J19: Inefficient boolean expression");
		println("J20: Inefficient boolean return");
		println("J21: Comparing strings with '=='");
		println("J22: Unnecessary parentheses");
		println("J23: Unnecessary semicolon after declaration");
		println("J24: Overly long line");

		println("\nCCS Python-specific suggestion codes:");
		println("P01: Module imported but unused");
		println("P02: 'break' and 'continue' not properly in loop");
		println("P03: 'return' outside function");
		println("P04: Locally undefined identifier");
		println("P05: Duplicate parameter in function definition");
		println("P06: Local variable is assigned but unused");
		println("P07: Compilation error");
		println("P08: Expected an indented block when the last statement is header of syntax block ");
		println("     (e.g., loop)");
		println("P09: Inconsistent indentation");
		println("P10: No spaces between '#' and comment content");
		println("P11: Overly long line");
		println("P12: More than one statement in one line");
		println("P13: Unnecessary semicolon");
	}

	private static boolean isHumanLanguageValid(String humanLang) {
		if (humanLang != null && (humanLang.equals("id") || humanLang.equals("en") || humanLang.equals("iden")))
			return true;
		else
			return false;
	}

	// copied from STRANGE
	private static void println(String s) {
		System.out.println(s);
	}

	private static String preparePathOrRegex(String path) {
		if (path != null && (path.startsWith("'") || path.startsWith("\"")))
			return path.substring(1, path.length() - 1);
		else
			return path;
	}

	private static boolean isPathValidAndExist(String path) {
		// check the validity of the string
		if (isPathValid(path) == false)
			return false;

		// check whether such file exists
		File f = new File(path);
		if (f.exists() == false)
			return false;

		return true;
	}

	private static boolean isPathValid(String path) {
		// check the validity of the string
		if (path == null || path.length() == 0)
			return false;
		else
			return true;
	}

	private static boolean isProgrammingLanguageValid(String prog) {
		if (prog != null && (prog.equals("java") || prog.equals("py")))
			return true;
		else
			return false;
	}
}
