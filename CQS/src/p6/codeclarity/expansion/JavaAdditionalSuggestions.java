package p6.codeclarity.expansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import support.LibTuple;

public class JavaAdditionalSuggestions {
	public static String checkStyleJarPath = "style" + File.separator + "checkstyle-8.43-all.jar";
	public static String checkStyleXMLEnPath = "style" + File.separator + "java_style_rules_en.xml";
	public static String checkStyleXMLIdPath = "style" + File.separator + "java_style_rules_id.xml";

	public static void addSuggestionTuplesFromCheckStyle(ArrayList<ExpandedClaritySuggestionTuple> out, String path,
			String languageCode, ArrayList<LibTuple> tokens, String[] selectedSuggestions) {

		// convert each suggestion code to checkstyle's code
		selectedSuggestions = convertSuggestionCodestoCheckStyleCodes(selectedSuggestions);

		String[] args = new String[] { "java", "-jar", checkStyleJarPath, "-c",
				((languageCode.equals("en")) ? checkStyleXMLEnPath : checkStyleXMLIdPath), path };
		try {
			Process p = Runtime.getRuntime().exec(args);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					// split the message based on ':' to get row, column, issue type, and the
					// explanation
					String[] r = line.substring(line.indexOf(path + ":") + path.length() + 1).split(":");

					// get the line
					int tokenLine = Integer.parseInt(r[0]);

					int processedIdx = 1; // to mark how many indexes have been processed

					// get the column if any
					int tokenCol = -1;
					if (r.length > 2) {
						/*
						 * only assign col if there are more than two ':' as one message has no column
						 * information the column is reduced by one since apparently check style counts
						 * column from one while ours from zero. Ours follows ANTLR definition.
						 */
						tokenCol = Integer.parseInt(r[1]) - 1;
						processedIdx++;
					}
					// get explanation and its "check style"'s issue type
					String explanation = "";
					String checkStyleIssueType = "";
					for (int i = processedIdx; i < r.length; i++)
						explanation = explanation + r[i] + ((i != r.length - 1) ? ":" : "");
					explanation = explanation.trim();
					checkStyleIssueType = explanation.substring(explanation.lastIndexOf("[") + 1,
							explanation.length() - 1);
					explanation = explanation.substring(0, explanation.lastIndexOf("["));

					// based on checkStyleIssueType, determine whether the suggestion is selected by
					// the user.
					
					// check if the user has preferred suggestions
					if (selectedSuggestions.length > 0) {
						// if the suggestion is not allowed, skip
						if (ExpandedCodeClarityContentGenerator.doesIssueCodeExistInSelectedCodes(checkStyleIssueType,
								selectedSuggestions) == false)
							continue;
					}

					// generate the new keyword
					String issueKeyword = getIssueKeywordFromCheckStyleKeyword(checkStyleIssueType, languageCode);

					// get the targeted token
					LibTuple targetedToken = ExpandedCodeClarityContentGenerator.getTargetedToken(tokens, tokenLine,
							tokenCol);

					// if such issue keyword exists, skip
					if (ExpandedCodeClarityContentGenerator.doesIssueKeywordExistInMessages(issueKeyword, out))
						continue;

					// add the message to the list
					// check if there are messages in that position
					int pos = ExpandedCodeClarityContentGenerator.indexOfMessages(out, targetedToken.getLine(),
							targetedToken.getColumn());
					if (pos == -1) {
						// if not, add a new one
						ArrayList<String> issueKeywords = new ArrayList<String>();
						issueKeywords.add(issueKeyword);
						ArrayList<String> issueExplanations = new ArrayList<String>();
						issueExplanations.add(explanation);
						out.add(new ExpandedClaritySuggestionTuple(targetedToken, targetedToken.getLine(),
								targetedToken.getColumn(), targetedToken.getRawText(), issueKeywords,
								issueExplanations));
					} else {
						// otherwise, update the existing one
						out.get(pos).addIssueKeyword(issueKeyword);
						out.get(pos).addIssueExplanation(explanation);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			// compilation error

			// generate keyword and message
			String issueKeyword = "";
			String issueExplanation = "";
			if (languageCode.equals("en")) {
				issueKeyword = ("Incorrect and/or uncommon syntaxes");
				issueExplanation = ("The program seems to use incorrect and/or uncommon syntaxes! No syntax suggestions are provided");
			} else {
				issueKeyword = ("Sintaks tidak benar dan/atau tidak umum");
				issueExplanation = ("Program tampak menggunakan sintaks yang tidak benar dan/atau tidak umum! Tidak ada rekomendasi sintaks yang dapat diberikan");
			}

			// check whether there is a message at the first token
			LibTuple firstToken = tokens.get(0);
			int pos = ExpandedCodeClarityContentGenerator.indexOfMessages(out, firstToken.getLine(),
					firstToken.getColumn());
			if (pos == -1) {
				// if not, add a new one
				ArrayList<String> issueKeywords = new ArrayList<String>();
				issueKeywords.add(issueKeyword);
				ArrayList<String> issueExplanations = new ArrayList<String>();
				issueExplanations.add(issueExplanation);
				out.add(0, new ExpandedClaritySuggestionTuple(firstToken, firstToken.getLine(), firstToken.getColumn(),
						firstToken.getRawText(), issueKeywords, issueExplanations));
			} else {
				// otherwise, update the existing one
				out.get(pos).addIssueKeyword(issueKeyword);
				out.get(pos).addIssueExplanation(issueExplanation);
			}
		}
	}

	private static String[] convertSuggestionCodestoCheckStyleCodes(String[] codes) {
		// convert each suggestion code to checkstyle's code
		for (int i = 0; i < codes.length; i++) {
			if (codes[i].equals("J01"))
				codes[i] = "ArrayTypeStyle";
			else if (codes[i].equals("J02"))
				codes[i] = "AbstractClassName";
			else if (codes[i].equals("J03"))
				codes[i] = "AvoidInlineConditionals";
			else if (codes[i].equals("J04"))
				codes[i] = "BooleanExpressionComplexity";
			else if (codes[i].equals("J05"))
				codes[i] = "ConstantName";
			else if (codes[i].equals("J06"))
				codes[i] = "DeclarationOrder";
			else if (codes[i].equals("J07"))
				codes[i] = "EmptyBlock";
			else if (codes[i].equals("J08"))
				codes[i] = "EmptyCatchBlock";
			else if (codes[i].equals("J09"))
				codes[i] = "EmptyLineSeparator";
			else if (codes[i].equals("J10"))
				codes[i] = "EmptyStatement";
			else if (codes[i].equals("J11"))
				codes[i] = "ExecutableStatementCount";
			else if (codes[i].equals("J12"))
				codes[i] = "MultipleVariableDeclarations";
			else if (codes[i].equals("J13"))
				codes[i] = "NeedBraces";
			else if (codes[i].equals("J14"))
				codes[i] = "NestedForDepth";
			else if (codes[i].equals("J15"))
				codes[i] = "NestedIfDepth";
			else if (codes[i].equals("J16"))
				codes[i] = "NestedTryDepth";
			else if (codes[i].equals("J17"))
				codes[i] = "OneStatementPerLine";
			else if (codes[i].equals("J18"))
				codes[i] = "RequireThis";
			else if (codes[i].equals("J19"))
				codes[i] = "SimplifyBooleanExpression";
			else if (codes[i].equals("J20"))
				codes[i] = "SimplifyBooleanReturn";
			else if (codes[i].equals("J21"))
				codes[i] = "StringLiteralEquality";
			else if (codes[i].equals("J22"))
				codes[i] = "UnnecessaryParentheses";
			else if (codes[i].equals("J23"))
				codes[i] = "UnnecessarySemicolonAfterTypeMemberDeclaration";
			else if (codes[i].equals("J24"))
				codes[i] = "LineLength";
		}
		return codes;
	}

	private static String getIssueKeywordFromCheckStyleKeyword(String checkStyleKeyword, String languageCode) {
		if (checkStyleKeyword.equals("ArrayTypeStyle"))
			return (languageCode.equals("en") ? "Less explicit array data type" : "Tipe data array kurang eksplisit");
		else if (checkStyleKeyword.equals("AbstractClassName"))
			return (languageCode.equals("en") ? "Inconsistent use of abstract class name and/or 'Abstract' prefix"
					: "Ketidakkonsistenan penggunaan nama kelas abstrak dan prefik 'Abstract'");
		else if (checkStyleKeyword.equals("AvoidInlineConditionals"))
			return (languageCode.equals("en") ? "Using inline conditional" : "Penggunaan if-else satu baris");
		else if (checkStyleKeyword.equals("BooleanExpressionComplexity"))
			return (languageCode.equals("en") ? "Overly complex boolean expression"
					: "Ekspresi boolean terlalu kompleks");
		else if (checkStyleKeyword.equals("ConstantName"))
			return (languageCode.equals("en") ? "Constant name with lowercased letters"
					: "Nama konstan dengan huruf kecil");
		else if (checkStyleKeyword.equals("DeclarationOrder"))
			return (languageCode.equals("en") ? "Incorrect declaration order" : "Urutan deklarasi salah");
		else if (checkStyleKeyword.equals("EmptyBlock"))
			return (languageCode.equals("en") ? "Empty block" : "Blok kosong");
		else if (checkStyleKeyword.equals("EmptyCatchBlock"))
			return (languageCode.equals("en") ? "Empty catch block" : "Blok catch kosong");
		else if (checkStyleKeyword.equals("EmptyLineSeparator"))
			return (languageCode.equals("en") ? "Only one empty line separator needed"
					: "Hanya satu baris kosong pemisah dibutuhkan");
		else if (checkStyleKeyword.equals("EmptyStatement"))
			return (languageCode.equals("en") ? "Semicolon without program statement"
					: "Titik koma tanpa statemen program");
		else if (checkStyleKeyword.equals("ExecutableStatementCount"))
			return (languageCode.equals("en") ? "Too many program statements in a method"
					: "Terlalu banyak statemen program dalam sebuah method");
		else if (checkStyleKeyword.equals("MultipleVariableDeclarations"))
			return (languageCode.equals("en") ? "Multiple variable declaration" : "Deklarasi beberapa variabel");
		else if (checkStyleKeyword.equals("NeedBraces"))
			return (languageCode.equals("en") ? "Braces needed" : "Kurung kurawal dibutuhkan");
		else if (checkStyleKeyword.equals("NestedForDepth"))
			return (languageCode.equals("en") ? "Overly deep nested loop block"
					: "Pengulangan bersarang yang terlalu dalam");
		else if (checkStyleKeyword.equals("NestedIfDepth"))
			return (languageCode.equals("en") ? "Overly deep nested if-else block"
					: "Percabangan bersarang yang terlalu dalam");
		else if (checkStyleKeyword.equals("NestedTryDepth"))
			return (languageCode.equals("en") ? "Overly deep nested try-catch block"
					: "Try-catch bersarang yang terlalu dalam");
		else if (checkStyleKeyword.equals("OneStatementPerLine"))
			return (languageCode.equals("en") ? "Too many program statements in one line"
					: "Terlalu banyak statemen program di satu baris");
		else if (checkStyleKeyword.equals("RequireThis"))
			return (languageCode.equals("en") ? "'this' keyword required" : "Kata kunci 'this' dibutuhkan");
		else if (checkStyleKeyword.equals("SimplifyBooleanExpression"))
			return (languageCode.equals("en") ? "Inefficient boolean expression" : "Ekspresi boolean tidak efisien");
		else if (checkStyleKeyword.equals("SimplifyBooleanReturn"))
			return (languageCode.equals("en") ? "Inefficient boolean return"
					: "Pengembalian nilai boolean tidak efisien");
		else if (checkStyleKeyword.equals("StringLiteralEquality"))
			return (languageCode.equals("en") ? "'equals' or one of its derivations needed for comparison"
					: "'equals' dibutuhkan untuk komparasi");
		else if (checkStyleKeyword.equals("UnnecessaryParentheses"))
			return (languageCode.equals("en") ? "Unnecessary parentheses" : "Kurung yang tidak dibutuhkan");
		else if (checkStyleKeyword.equals("UnnecessarySemicolonAfterTypeMemberDeclaration"))
			return (languageCode.equals("en") ? "Unnecessary semicolon after declaration"
					: "Titik koma tidak dibutuhkan sehabis deklarasi");
		else if (checkStyleKeyword.equals("LineLength"))
			return (languageCode.equals("en") ? "Too many characters in one line"
					: "Terlalu banyak karakter di satu baris");

		return null;
	}

}
