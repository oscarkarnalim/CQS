package p6.codeclarity.expansion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import support.LibTuple;

public class PythonAdditionalSuggestion {
	public static void addSuggestionTuplesFromFlake8(ArrayList<ExpandedClaritySuggestionTuple> out, String path,
			String languageCode, ArrayList<LibTuple> tokens, String[] selectedSuggestions) {
		String flake8CodeString = convertSuggestionCodestoFlake8Codes(selectedSuggestions);

		String[] args = new String[] { "flake8", "--select", flake8CodeString, path };
		try {
			Process p = Runtime.getRuntime().exec(args);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				// generate issue message and explanation
				String flake8code = line.substring(line.lastIndexOf(": ") + 2);
				String flake8Message = flake8code.substring(flake8code.indexOf(" ") + 1);
				flake8code = flake8code.substring(0, flake8code.indexOf(" "));
				String issueKeyword = (languageCode.equals("en")) ? getIssueKeywordFromFlake8CodeEN(flake8code)
						: getIssueKeywordFromFlake8CodeID(flake8code);
				String issueExplanation = (languageCode.equals("en"))
						? getIssueExplanationFromFlake8CodeEN(flake8code, flake8Message)
						: getIssueExplanationFromFlake8CodeID(flake8code, flake8Message);

				// get the targeted token
				String[] r = line.substring(0, line.lastIndexOf(": ")).split(":");
				int tokenLine = Integer.parseInt(r[r.length - 2]);
				int tokenCol = Integer.parseInt(r[r.length - 1]);
				// special handling for line too long issue as it targets the end of the line
				// instead of the beginning
				if (flake8code.equals("E501"))
					tokenCol = 1;
				// the column should be reduced by one as flake8 starts the column index from 1,
				// not 0
				LibTuple targetedToken = ExpandedCodeClarityContentGenerator.getTargetedToken(tokens, tokenLine,
						tokenCol - 1);

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
					issueExplanations.add(issueExplanation);
					out.add(new ExpandedClaritySuggestionTuple(targetedToken, targetedToken.getLine(),
							targetedToken.getColumn(), targetedToken.getRawText(), issueKeywords, issueExplanations));
				} else {
					// otherwise, update the existing one
					out.get(pos).addIssueKeyword(issueKeyword);
					out.get(pos).addIssueExplanation(issueExplanation);
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

	private static String convertSuggestionCodestoFlake8Codes(String[] codes) {
		// return a string containing the list of preferred Flake8 codes

		if (codes.length == 0)
			// if no preference, all suggestions are considered
			return "F401,F701,F702,F706,F821,F831,F841,E999,E112,E113,E262,E265,E501,E701,E704,E702,E703";
		else {
			// otherwise, convert the codes from our codes to flake8 codes

			// to store preferred flake8 codes
			String out = "";

			// for each element, convert if eligible
			for (int i = 0; i < codes.length; i++) {
				String flake8Code = "";
				if (codes[i].equals("P01"))
					flake8Code = "F401";
				else if (codes[i].equals("P02"))
					flake8Code = "F701,F702";
				else if (codes[i].equals("P03"))
					flake8Code = "F706";
				else if (codes[i].equals("P04"))
					flake8Code = "F821";
				else if (codes[i].equals("P05"))
					flake8Code = "F831";
				else if (codes[i].equals("P06"))
					flake8Code = "F841";
				else if (codes[i].equals("P07"))
					flake8Code = "E999";
				else if (codes[i].equals("P08"))
					flake8Code = "E112";
				else if (codes[i].equals("P09"))
					flake8Code = "E113";
				else if (codes[i].equals("P10"))
					flake8Code = "E262,E265";
				else if (codes[i].equals("P11"))
					flake8Code = "E501";
				else if (codes[i].equals("P12"))
					flake8Code = "E701,E704,E702";
				else if (codes[i].equals("P13"))
					flake8Code = "E703";

				// add the new code to out if any
				if (flake8Code.length() > 0)
					out += (flake8Code + ",");
			}

			// remove the last comma
			out = out.substring(0, out.length() - 1);

			// return the result
			return out;
		}
	}

	private static String getIssueKeywordFromFlake8CodeEN(String flake8code) {
		if (flake8code.equals("F401"))
			return "Module imported but unused";
		else if (flake8code.equals("F701"))
			return "'break' not properly in loop";
		else if (flake8code.equals("F702"))
			return "'continue' not properly in loop";
		else if (flake8code.equals("F706"))
			return "'return' outside function";
		else if (flake8code.equals("F821"))
			return "Locally undefined identifier";
		else if (flake8code.equals("F831"))
			return "Duplicate parameter in function definition";
		else if (flake8code.equals("F841"))
			return "Local variable is assigned but unused";
		else if (flake8code.equals("E999"))
			return "Compilation error";
		else if (flake8code.equals("E112"))
			return "Expected an indented block";
		else if (flake8code.equals("E113"))
			return "Unexpected indentation";
		else if (flake8code.equals("E262"))
			return "Space needed between '#' and comment content";
		else if (flake8code.equals("E265"))
			return "Space needed between '#' and comment content";
		else if (flake8code.equals("E501"))
			return "Overly long line";
		else if (flake8code.equals("E701"))
			return "More than one statement in one line";
		else if (flake8code.equals("E704"))
			return "More than one statement in one line";
		else if (flake8code.equals("E702"))
			return "More than one statement in one line";
		else if (flake8code.equals("E703"))
			return "Unnecessary semicolon";

		return null;
	}

	private static String getIssueKeywordFromFlake8CodeID(String flake8code) {
		if (flake8code.equals("F401"))
			return "Modul diimpor namun tidak dipakai";
		else if (flake8code.equals("F701"))
			return "'break' tidak digunakan dalam pengulangan";
		else if (flake8code.equals("F702"))
			return "'continue' tidak digunakan dalam pengulagan";
		else if (flake8code.equals("F706"))
			return "'return' diluar fungsi";
		else if (flake8code.equals("F821"))
			return "Identifier belum didefiniskan secara lokal";
		else if (flake8code.equals("F831"))
			return "Parameter duplikat di definisi fungsi";
		else if (flake8code.equals("F841"))
			return "Variabel lokal diassign tapi tidak dipakai";
		else if (flake8code.equals("E999"))
			return "Error kompilasi";
		else if (flake8code.equals("E112"))
			return "Diharapkan blok dengan indentasi";
		else if (flake8code.equals("E113"))
			return "Indentasi tidak terduga";
		else if (flake8code.equals("E262"))
			return "Spasi dibutuhkan antara '#' dan konten komentar";
		else if (flake8code.equals("E265"))
			return "Spasi dibutuhkan antara '#' dan konten komentar";
		else if (flake8code.equals("E501"))
			return "Baris terlalu panjang";
		else if (flake8code.equals("E701"))
			return "Lebih dari satu statemen dalam satu baris";
		else if (flake8code.equals("E704"))
			return "Lebih dari satu statemen dalam satu baris";
		else if (flake8code.equals("E702"))
			return "Lebih dari satu statemen dalam satu baris";
		else if (flake8code.equals("E703"))
			return "Titik koma tidak dibutuhkan";

		return null;
	}

	private static String getIssueExplanationFromFlake8CodeEN(String flake8code, String flake8Message) {
		if (flake8code.equals("F401"))
			return "If unused, module "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " is not expected to be imported";
		else if (flake8code.equals("F701"))
			return "'break' is expected to be in a loop";
		else if (flake8code.equals("F702"))
			return "'continue' is expected to be in a loop";
		else if (flake8code.equals("F706"))
			return "'return' is expected to be in a function";
		else if (flake8code.equals("F821"))
			return "The identifier "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " is either undefined or imported from an external module";
		else if (flake8code.equals("F831"))
			return "Parameter "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " is expected to be defined once in function";
		else if (flake8code.equals("F841"))
			return "If unused, local variable "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " is expected to be removed";
		else if (flake8code.equals("E999"))
			return "The program is uncompilable; some syntax suggestions are not provided";
		else if (flake8code.equals("E112"))
			return "If the statement is the body of a syntax block (e.g., loop), it is expected to be indented further";
		else if (flake8code.equals("E113"))
			return "The statement is indented differently though it is unnecessary";
		else if (flake8code.equals("E262"))
			return "For clarity, the comment is expected to have at least one space between '#' and the content";
		else if (flake8code.equals("E265"))
			return "For clarity, the comment is expected to have at least one space between '#' and the content";
		else if (flake8code.equals("E501"))
			return "For readability, each line is expected to have fewer than 80 characters";
		else if (flake8code.equals("E701"))
			return "Header and body of the syntax block are both in one line; for readability, each statement is expected to be on its own line";
		else if (flake8code.equals("E704"))
			return "Header and body of the syntax block are both in one line; for readability, each statement is expected to be on its own line";
		else if (flake8code.equals("E702"))
			return "The line has more than one statements; for readability, each statement is expected to be on its own line";
		else if (flake8code.equals("E703"))
			return "The semicolon can be removed as it has no purposes";

		return null;
	}

	private static String getIssueExplanationFromFlake8CodeID(String flake8code, String flake8Message) {
		if (flake8code.equals("F401"))
			return "Jika tidak digunakan, modul "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " sebaiknya tidak diimpor";
		else if (flake8code.equals("F701"))
			return "'break' sebaiknya ada di dalam pengulangan";
		else if (flake8code.equals("F702"))
			return "'continue' sebaiknya ada di dalam pengulangan";
		else if (flake8code.equals("F706"))
			return "'return' sebaiknya ada di dalam fungsi";
		else if (flake8code.equals("F821"))
			return "Identifier"
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " tidak didefiniskan atau diimpor dari module luar";
		else if (flake8code.equals("F831"))
			return "Parameter "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " sebaiknya didefiniskan hanya sekali di fungsi";
		else if (flake8code.equals("F841"))
			return "Jika tidak digunakan, variabel lokal "
					+ flake8Message.substring(flake8Message.indexOf("'"), flake8Message.lastIndexOf("'") + 1)
					+ " sebaiknya dibuang";
		else if (flake8code.equals("E999"))
			return "Program tidak dapat dikompile; beberapa rekomendasi sintaks tidak diberikan";
		else if (flake8code.equals("E112"))
			return "Jika statemen terkait adalah badan dari sebuah blok sintaks (misal pengulangan), statemen sebaiknya diberi indentasi lebih";
		else if (flake8code.equals("E113"))
			return "Statemen terkait diindentasi berbeda walaupun hal terkait tidak dibutuhkan";
		else if (flake8code.equals("E262"))
			return "Untuk kejelasan, komentar terkait diharapkan memiliki paling tidak satu spasi antara '#' dan konten komentar";
		else if (flake8code.equals("E265"))
			return "Untuk kejelasan, komentar terkait diharapkan memiliki paling tidak satu spasi antara '#' dan konten komentar";
		else if (flake8code.equals("E501"))
			return "Untuk kemudahan pembacaan, setiap baris sebaiknya berisi paling banyak 80 karakter";
		else if (flake8code.equals("E701"))
			return "Bagian header dan badan dari blok sintaks terkait ada dalam satu baris; untuk kemudahan pembacaan, setiap statemen sebaiknya ada di baris tersendiri";
		else if (flake8code.equals("E704"))
			return "Bagian header dan badan dari blok sintaks terkait ada dalam satu baris; untuk kemudahan pembacaan, setiap statemen sebaiknya ada di baris tersendiri";
		else if (flake8code.equals("E702"))
			return "Baris terkait berisi lebih dari satu statemen; untuk kemudahan pembacaan, setiap statemen sebaiknya ada di baris tersendiri";
		else if (flake8code.equals("E703"))
			return "Titik koma terkait sebaiknya dibuang karena tidak berguna";

		return null;
	}
}
