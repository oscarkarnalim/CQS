package p6.codeclarity.expansion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

import p6.codeclarity.ClaritySuggestionTuple;
import p6.codeclarity.Suggester;
import support.AdditionalKeywordsManager;
import support.LibJavaExtractor;
import support.LibPythonExtractor;
import support.LibTuple;

public class ExpandedCodeClarityContentGenerator {

	public static void execute(String path, String additional_keywords_path, String programmingLanguageCode,
			String languageCode, boolean reindex, boolean isOutText, String[] selectedSuggestions) {
		// read the keywords if needed
		ArrayList<ArrayList<String>> additional_keywords = new ArrayList<ArrayList<String>>();
		if (additional_keywords_path != null)
			additional_keywords = AdditionalKeywordsManager.readAdditionalKeywords(additional_keywords_path);

		// get the tokens based on the programming language
		ArrayList<LibTuple> tokens = null;
		if (programmingLanguageCode.equalsIgnoreCase("py"))
			tokens = LibPythonExtractor.getDefaultTokenString(path, additional_keywords);
		else if (programmingLanguageCode.equalsIgnoreCase("java"))
			tokens = LibJavaExtractor.getDefaultTokenString(path, additional_keywords);

		// get the messages from our code clarity suggestion module
		ArrayList<ClaritySuggestionTuple> messages = null;
		if (selectedSuggestions.length == 0) {
			// no specific preferences, all suggestions are applied
			messages = Suggester.getSuggestionMessages(tokens, programmingLanguageCode, languageCode, reindex);
		} else {
			// set booleans for allowed standard suggestions
			boolean isTooShortIdentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S01", selectedSuggestions))
				isTooShortIdentSuggested = true;

			boolean isNotDescriptiveIdentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S02", selectedSuggestions))
				isNotDescriptiveIdentSuggested = true;

			boolean isIncorrectlyWrittenIdentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S03", selectedSuggestions))
				isIncorrectlyWrittenIdentSuggested = true;

			boolean isInconsistentTransitionIdentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S04", selectedSuggestions))
				isInconsistentTransitionIdentSuggested = true;

			boolean isTooShortCommentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S05", selectedSuggestions))
				isTooShortCommentSuggested = true;

			boolean isNotDescriptiveCommentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S06", selectedSuggestions))
				isNotDescriptiveCommentSuggested = true;

			boolean isIncorrectlyWrittenCommentSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S07", selectedSuggestions))
				isIncorrectlyWrittenCommentSuggested = true;

			boolean isCommentPerSyntaxBlockSuggested = false;
			if (doesIssueCodeExistInSelectedCodes("S08", selectedSuggestions))
				isCommentPerSyntaxBlockSuggested = true;

			messages = Suggester.getSuggestionMessages(tokens, programmingLanguageCode, languageCode, reindex,
					isTooShortIdentSuggested, isNotDescriptiveIdentSuggested, isIncorrectlyWrittenIdentSuggested,
					isInconsistentTransitionIdentSuggested, isTooShortCommentSuggested,
					isNotDescriptiveCommentSuggested, isIncorrectlyWrittenCommentSuggested,
					isCommentPerSyntaxBlockSuggested);
		}
		// convert all "iden" to "id" given that the html needs to show indonesian text
		if (languageCode.contentEquals("iden"))
			languageCode = "id";

		// convert all messages to their expanded versions
		ArrayList<ExpandedClaritySuggestionTuple> newMessages = ExpandingCurrentClaritySuggestionTuple
				.convertSuggestionTupleToExpanded(messages, languageCode);

		if (tokens.size() > 0) {
			// add messages from checkstyle if the programming language is Java
			if (programmingLanguageCode.equals("java"))
				JavaAdditionalSuggestions.addSuggestionTuplesFromCheckStyle(newMessages, path, languageCode, tokens,
						selectedSuggestions);
			// add messages from flake8 if the programming language is Python
			else if (programmingLanguageCode.equals("py"))
				PythonAdditionalSuggestion.addSuggestionTuplesFromFlake8(newMessages, path, languageCode, tokens,
						selectedSuggestions);
		}

		// sort
		Collections.sort(newMessages);

		// assign visual id
		for (int i = 0; i < newMessages.size(); i++) {
			newMessages.get(i).setVisualId("s" + (i + 1));
		}

		// get the html template based on given human language
		String targetHTMLPath = "expanded_code_clarity_html_template_en.html";
		if (languageCode.equals("id"))
			targetHTMLPath = "expanded_code_clarity_html_template_id.html";

		if (isOutText) {
			try {
				FileWriter fw = new FileWriter(new File("out.txt"));
				for (int i = 0; i < newMessages.size(); i++) {
					fw.write(newMessages.get(i).toString().replaceAll("\n", System.lineSeparator()));
					fw.write(System.lineSeparator() + System.lineSeparator());
				}
				fw.close();
				System.out.println("The result can be seen in \"out.txt\"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				generateHtml(tokens, newMessages, targetHTMLPath, "out.html", programmingLanguageCode, languageCode);
				System.out.println("The result can be seen in \"out.html\"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean doesIssueCodeExistInSelectedCodes(String code, String[] selectedSuggestionCodes) {
		// return true if the code is in selectedSuggestionCodes
		for (String s : selectedSuggestionCodes) {
			if (code.equals(s))
				return true;
		}
		return false;
	}

	public static boolean doesIssueKeywordExistInMessages(String issueKeyword,
			ArrayList<ExpandedClaritySuggestionTuple> messages) {
		/*
		 * check if the issueKeyword exists in messages. This is necessary to keep only
		 * distinct issues displayed as displaying all of them can be overwhelming.
		 */
		for (ExpandedClaritySuggestionTuple m : messages) {
			ArrayList<String> issueKeywords = m.getIssueKeywords();
			for (String i : issueKeywords) {
				if (i.equals(issueKeyword))
					return true;
			}
		}

		return false;
	}

	public static LibTuple getTargetedToken(ArrayList<LibTuple> tokens, int row, int col) {
		// get a token in given position. Otherwise return null

		for (int i = 0; i < tokens.size(); i++) {
			LibTuple c = tokens.get(i);
			if (row == c.getLine()) {
				if (col == -1) {
					// if no col is assigned,return current token
					return c;
				} else if (col == c.getColumn()) {
					// exact same position
					return c;
				} else if (col < c.getColumn()) {
					// the correct token is located at previous index if any
					if (i > 0)
						return tokens.get(i - 1);
					else
						return c;
				}
			} else if (row < c.getLine()) {
				if (col == -1)
					// if no col is assigned,return current token
					return c;
				else {
					// the correct token is located at previous index if any
					if (i > 0)
						return tokens.get(i - 1);
					else
						return c;
				}
			}
		}

		return null;
	}

	public static int indexOfMessages(ArrayList<ExpandedClaritySuggestionTuple> out, int line, int col) {
		/*
		 * This method searches a message in a particular position
		 */
		for (int i = 0; i < out.size(); i++) {
			if (out.get(i).getLine() == line && out.get(i).getCol() == col)
				return i;
		}
		return -1;
	}

	public static void generateHtml(ArrayList<LibTuple> tokens, ArrayList<ExpandedClaritySuggestionTuple> messages,
			String templateHTMLPath, String outputHTMLPath, String programmingLanguage, String humanLanguage)
			throws Exception {
		String code = generateCode1(tokens, messages);
		String tablecontent = generateTableContent(messages, humanLanguage);
		String explanation = generateExplanation(messages, humanLanguage);

		File templateFile = new File(templateHTMLPath);
		File outputFile = new File(outputHTMLPath);
		BufferedReader fr = new BufferedReader(new FileReader(templateFile));
		BufferedWriter fw = new BufferedWriter(new FileWriter(outputFile));
		String line;
		while ((line = fr.readLine()) != null) {

			// for default data
			if (line.contains("@code")) {
				line = line.replace("@code", code);
			}
			if (line.contains("@tablecontent")) {
				line = line.replace("@tablecontent", tablecontent);
			}
			if (line.contains("@explanation")) {
				line = line.replace("@explanation", explanation);
			}
			if (line.contains("@othersuggestions")) {
				line = line.replace("@othersuggestions",
						getAdditionalSuggestionRules(programmingLanguage, humanLanguage));
			}

			fw.write(line);
			fw.write(System.lineSeparator());
		}
		fr.close();
		fw.close();
	}

	private static String getAdditionalSuggestionRules(String programmingLanguage, String humanLanguage) {
		// returns additional suggestion rules for code clarity
		String out = "";
		if (programmingLanguage.equals("java")) {
			if (humanLanguage.equals("en")) {
				out = "<li>Declarations should start with those of static variables, followed by attributes, constructors, and other methods</li>"
						+ "<li>Each program line is expected to be reasonably short</li>"
						+ "<li>Each statement is expected to be in its own line</li>"
						+ "<li>An empty line might be needed to separate code components</li>"
						+ "<li>No empty syntax blocks are expected</li>"
						+ "<li>Parentheses and semicolons are expected to be adequately used</li>"
						+ "<li>Braces are expected even when a syntax block only has one program statement</li>"
						+ "<li>Strings are expected to be compared with 'equals' or one of its derivations</li>"
						+ "<li>Each variable declaration is expected to be in its own line and statement</li>"
						+ "<li>Each time a non-static attribute is accessed, it is expected to use 'this'</li>"
						+ "<li>Constant names are expected to be without lowercased letters</li>"
						+ "<li>Array brackets are expected to be declared before the identifier name to make the data type more explicit</li>"
						+ "<li>Boolean expressions and logic are expected to be efficient</li>"
						+ "<li>Each complex boolean expression should be broken down to a few simpler expressions</li>"
						+ "<li>Inline conditionals are hard to understand and they are not expected to be used</li>"
						+ "<li>Each method is expected to contain a reasonable number of program statements</li>"
						+ "<li>Nested syntax blocks are expected to have reasonable depth</li>"
						+ "<li>Abstract class name and prefix 'Abstract' are expected to be consistently used together</li>";
			} else {
				out = "<li>Deklarasi sebaiknya dimulai dari variabel static, diikuti dengan atribut, konstruktor, dan method-method lainnya</li>"
						+ "<li>Setiap baris program sebaiknya pendek</li>"
						+ "<li>Setiap statemen sebaiknya ada di baris tersendiri</li>"
						+ "<li>Sebuah baris kosong mungkin dibutuhkan untuk memisahkan komponen kode</li>"
						+ "<li>Sebaiknya tidak ada blok sintaks kosong</li>"
						+ "<li>Kurung dan titik koma sebaiknya digunakan secukupnya</li>"
						+ "<li>Kurung sebaiknya ada walaupun sebuah blok sintaks hanya berisi satu statemen program</li>"
						+ "<li>String sebaiknya dibandingkan dengan 'equals' atau turunannya</li>"
						+ "<li>Setiap deklarasi variabel sebaiknya ada di baris dan statemen tersendiri</li>"
						+ "<li>Setiap kali atribut non-static diakses, ada baiknya menggunakan 'this'</li>"
						+ "<li>Nama konstan sebaiknya tanpa huruf kecil</li>"
						+ "<li>Kurung siku array sebaiknya dideklarasi sebelum nama identifiernya agar tipe datanya terlihat lebih eksplisit</li>"
						+ "<li>Ekspresi dan logika boolean sebaiknya efisien</li>"
						+ "<li>Setiap ekspresi boolean kompleks perlu dipecah menjadi beberapa ekspresi yang lebih simpel</li>"
						+ "<li>If-else satu baris sulit dimengerti dan sebaiknya tidak digunakan</li>"
						+ "<li>Setiap method sebaiknya berisi program statemen dengan jumlah yang masuk akal</li>"
						+ "<li>Blok sintaks bersarang sebaiknya memiliki kedalaman yang masuk akal</li>"
						+ "<li>Nama kelas abstrak dan prefik 'Abstract' sebaiknya konsisten digunakan bersamaan</li>";
			}
		} else if (programmingLanguage.equals("py")) {
			if (humanLanguage.equals("en")) {
				out = "<li>For readability, each line is expected to have fewer than 80 characters</li>"
						+ "<li>Each statement is expected to be on its own line</li>"
						+ "<li>Only defined identifiers should be used</li>"
						+ "<li>Indentation is expected to be adequately used</li>"
						+ "<li>Comment should have at least one space between '#' and the content</li>"
						+ "<li>A semicolon is not expected if it is at the end of a line</li>"
						+ "<li>In a function, each parameter is expected to have its own name</li>"
						+ "<li>'break' and 'continue' should be in loop</li>"
						+ "<li>'return' should be in a function</li>" + "<li>Modules are imported only when used</li>";
			} else {
				out = "<li>Untuk kemudahan pembacaan, setiap baris sebaiknya berisi paling banyak 80 karakter</li>"
						+ "<li>Setiap statemen sebaiknya memiliki baris tersendiri</li>"
						+ "<li>Hanya identifier yang sudah didefinisikan yang boleh dipakai</li>"
						+ "<li>Indentasi sebaiknya digunakan secukupnya</li>"
						+ "<li>Komentar harus memiliki paling tidak satu spasi antara '#' dan konten komentar</li>"
						+ "<li>Titik koma tidak dibutuhkan kalau karakter tersebut ada di akhir baris</li>"
						+ "<li>Di dalam sebuah fungsi, setiap parameter sebaiknya memiliki nama tersendiri</li>"
						+ "<li>'break' dan 'continue' harus ada di dalam pengulangan</li>"
						+ "<li>'return' harus ada di dalam fungsi</li>"
						+ "<li>Modul-modul hanya diimpor jika dipakai</li>";
			}
		}
		return out;
	}

	public static String generateExplanation(ArrayList<ExpandedClaritySuggestionTuple> messages, String humanLanguage) {
		StringBuffer s = new StringBuffer();
		// add explanation for each fragment
		for (ExpandedClaritySuggestionTuple m : messages) {
			// append the string
			s.append("<div class=\"explanationcontent\" id=\"" + m.getVisualId() + "he\">\n\t");
			s.append(m.getIssueExplanationsAsString().replaceAll("\n", "<br />").replaceAll("\t",
					"&nbsp;&nbsp;&nbsp;&nbsp;"));
			s.append("\n</div>\n");
		}

		return s.toString();
	}

	public static String generateTableContent(ArrayList<ExpandedClaritySuggestionTuple> list, String humanLanguage) {
		String tableId = "origtablecontent";

		StringBuffer s = new StringBuffer();

		// start generating the resulted string
		for (int i = 0; i < list.size(); i++) {
			ExpandedClaritySuggestionTuple cur = list.get(i);

			// set the first line
			s.append("<tr id=\"" + cur.getVisualId() + "hr\" onclick=\"markSelectedWithoutChangingTableFocus('"
					+ cur.getVisualId() + "','" + tableId + "')\">");

			/*
			 * Get table ID from visual ID and then aligns it for readability.
			 */
			String visualId = cur.getVisualId();
			// search for the numeric ID part
			int curIdNumPos = 0;
			for (int k = 0; k < visualId.length(); k++) {
				if (Character.isLetter(visualId.charAt(k)) == false) {
					curIdNumPos = k;
					break;
				}
			}
			// merge them together
			String alignedTableID = visualId.toUpperCase().charAt(0) + "";
			int curIdNum = Integer.parseInt(visualId.substring(curIdNumPos));
			if (curIdNum < 10) {
				alignedTableID += "00" + curIdNum;
			} else if (curIdNum < 100) {
				alignedTableID += "0" + curIdNum;
			} else {
				alignedTableID += curIdNum;
			}

			// visualising the rest of the lines
			s.append("\n\t<td style='width:10%;'><a href=\"#" + cur.getVisualId() + "a\" id=\"" + cur.getVisualId()
					+ "hl\">" + alignedTableID + "</a></td>");

			// hint text
			s.append("\n\t<td style='width:20%;'>" + cur.getHintTokenText().trim() + "</td>");

			// set location
			String location = cur.getLine() + "";
			s.append("\n\t<td style='width:10%;'>" + location + "</td>");

			// set issues
			String issues = cur.getIssueKeywordsAsString();
			s.append("\n\t<td>" + issues.replaceAll("\n", "<br />") + "</td>");

			// close the entry tag
			s.append("\n</tr>\n");
		}

		return s.toString();
	}

	public static String generateCode1(ArrayList<LibTuple> tokenString,
			ArrayList<ExpandedClaritySuggestionTuple> messages) {
		String codeClass = "syntaxsim";

		StringBuffer s = new StringBuffer();

		// starting from the first message, take all the required data
		int matchIdx = 0;
		ExpandedClaritySuggestionTuple m = messages.get(matchIdx);
		String visualIdForM = m.getVisualId();
		int targetedIdx = tokenString.indexOf(m.getTargetedToken());

		// for each token from code1
		for (int i = 0; i < tokenString.size(); i++) {
			LibTuple cur = tokenString.get(i);

			// to make sure the code is not wrongly visualised, replace all HTML escape
			// characters
			cur.setRawText(cur.getRawText().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));

			if (i == targetedIdx) {
				// add an opening link tag
				s.append("<a class='" + codeClass + "' id='" + visualIdForM + "a' href=\"#" + visualIdForM
						+ "a\" onclick=\"markSelected('" + visualIdForM + "','origtablecontent')\" >");
				// append the raw text
				s.append(cur.getRawText());
				// add a closing link tag
				s.append("</a>");
				// check for next message if any
				if (matchIdx + 1 < messages.size()) {
					// increment the idx
					matchIdx++;
					// take the new data
					m = messages.get(matchIdx);
					visualIdForM = m.getVisualId();
					targetedIdx = tokenString.indexOf(m.getTargetedToken());
				}
			} else {
				// append the raw text
				s.append(cur.getRawText());
			}
		}
		return s.toString();
	}
}
