package p6.codeclarity.expansion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import p6.codeclarity.ClaritySuggestionTuple;

public class ExpandingCurrentClaritySuggestionTuple {
	public static ArrayList<ExpandedClaritySuggestionTuple> convertSuggestionTupleToExpanded(
			ArrayList<ClaritySuggestionTuple> in, String languageCode) {
		ArrayList<ExpandedClaritySuggestionTuple> out = new ArrayList<ExpandedClaritySuggestionTuple>();

		for (ClaritySuggestionTuple c : in) {
			// to store the issues and their explanations
			ArrayList<String> issueKeywords = new ArrayList<String>();
			ArrayList<String> issueExplanations = new ArrayList<String>();

			// set the primary issue keyword and explanation if new
			String issue = getIssueKeywordFromClaritySuggestionTuple(languageCode, c.getTargetedContent(),
					c.getPotentialIssue());
			if (ExpandedCodeClarityContentGenerator.doesIssueKeywordExistInMessages(issue, out) == false) {
				issueKeywords.add(issue);
				issueExplanations.add(getIssueExplanationsdFromClaritySuggestionTuple(languageCode,
						c.getTargetedContent(), c.getPotentialIssue(), c));
			}

			// set the remaining issue keywords and their explanations if any and the
			// keywords are new

			// comment around syntax
			if (c.isRequireCommentAroundSyntax()) {
				issue = getIssueKeywordFromClaritySuggestionTuple(languageCode, "comment", "not descriptive");
				if (ExpandedCodeClarityContentGenerator.doesIssueKeywordExistInMessages(issue, out) == false) {
					issueKeywords.add(issue);
					issueExplanations.add(getIssueExplanationsdFromClaritySuggestionTuple(languageCode, "comment",
							"not descriptive", c));
				}
			}

			// inconsistent transition capitalisation
			if (c.isInconsistentIdentCapitalisation()) {
				issue = getIssueKeywordFromClaritySuggestionTuple(languageCode, "identifier",
						"inconsistent transition capitalisation");
				if (ExpandedCodeClarityContentGenerator.doesIssueKeywordExistInMessages(issue, out) == false) {
					issueKeywords.add(issue);
					issueExplanations.add(getIssueExplanationsdFromClaritySuggestionTuple(languageCode, "identifier",
							"inconsistent transition capitalisation", c));
				}
			}

			// inconsistent transition underscore
			if (c.isInconsistentIdentUnderscore()) {
				issue = getIssueKeywordFromClaritySuggestionTuple(languageCode, "identifier",
						"inconsistent transition underscore");
				if (ExpandedCodeClarityContentGenerator.doesIssueKeywordExistInMessages(issue, out) == false) {
					issueKeywords.add(issue);
					issueExplanations.add(getIssueExplanationsdFromClaritySuggestionTuple(languageCode, "identifier",
							"inconsistent transition underscore", c));
				}
			}

			// create and add new entry if the issues are more than one
			if (issueKeywords.size() > 0) {
				// create the entry
				ExpandedClaritySuggestionTuple n = new ExpandedClaritySuggestionTuple(c.getTargetedToken(), c.getLine(),
						c.getCol(), c.getHintTokenText(), issueKeywords, issueExplanations);

				// add the entry
				out.add(n);
			}
		}

		return out;
	}

	private static String getIssueKeywordFromClaritySuggestionTuple(String language, String targetedContent,
			String potentialIssue) {
		/*
		 * Given targetedContent and potentialIssue of a ClaritySuggestionTuple, it
		 * generates the issue keyword based on the language
		 */
		String out = "";
		if (targetedContent.equals("identifier")) {
			if (potentialIssue.equals("too short")) {
				out = (language.equalsIgnoreCase("en")) ? "Overly short identifier" : "Identifier terlalu pendek";
			} else if (potentialIssue.equals("not meaningful")) {
				out = (language.equalsIgnoreCase("en")) ? "Not meaningful identifier" : "Identifier tidak bermakna";
			} else if (potentialIssue.equals("incorrectly written")) {
				out = (language.equalsIgnoreCase("en")) ? "Incorrectly written identifier"
						: "Identifier tidak tertulis dengan benar";
			} else if (potentialIssue.equals("inconsistent transition capitalisation")) {
				out = (language.equalsIgnoreCase("en"))
						? "Inconsistent transition among identifier words (capitalisation expected)"
						: "Transisi inkonsisten antar kata di identifier (diharapkan kapital)";
			} else if (potentialIssue.equals("inconsistent transition underscore")) {
				out = (language.equalsIgnoreCase("en"))
						? "Inconsistent transition among identifier words (underscore expected)"
						: "Transisi inkonsisten antar kata di identifier (diharapkan underskor)";
			}
		} else if (targetedContent.equals("comment")) {
			if (potentialIssue.equals("too short")) {
				out = (language.equalsIgnoreCase("en")) ? "Overly short comment" : "Komentar terlalu pendek";
			} else if (potentialIssue.equals("not meaningful")) {
				out = (language.equalsIgnoreCase("en")) ? "Not meaningful comment" : "Komentar tidak bermakna";
			} else if (potentialIssue.equals("incorrectly written")) {
				out = (language.equalsIgnoreCase("en")) ? "Incorrectly written comment"
						: "Komentar tidak tertulis dengan benar";
			} else if (potentialIssue.equals("not descriptive")) {
				out = (language.equalsIgnoreCase("en")) ? "No comments around syntax block"
						: "Tidak ada komentar penjelas di sekitar blok sintaks";
			}
		}
		return out;
	}

	private static String getIssueExplanationsdFromClaritySuggestionTuple(String languageCode, String targetedContent,
			String potentialIssue, ClaritySuggestionTuple c) {
		/*
		 * Given a ClaritySuggestionTuple, it then generates the issue explanation
		 */
		String message = "";
		if (targetedContent.equals("identifier")) {
			if (potentialIssue.equals("too short")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + c.getHintTokenText() + "' is too short and might be not meaningful"
						: "Nama identifier '" + c.getHintTokenText() + "' terlalu pendek dan mungkin tidak bermakna";
			} else if (potentialIssue.equals("not meaningful")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + c.getHintTokenText()
								+ "' has no meaningful words and might not be descriptive"
						: "Nama identifier '" + c.getHintTokenText()
								+ "' tidak mengandung kata dan mungkin tidak deskriptif";
			} else if (potentialIssue.equals("incorrectly written")) {
				message = languageCode.equalsIgnoreCase("en")
						? "Part of an identifier '" + c.getHintTokenText() + "' might be incorrectly written"
						: "Bagian dari identifier '" + c.getHintTokenText() + "' mungkin salah tulis";

				// per corrected word
				Iterator<Entry<String, String>> it = c.getCorrectedWords().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> cc = it.next();
					message += languageCode.equalsIgnoreCase("en")
							? ("\n\tDid you mean '" + cc.getValue() + "' instead of '" + cc.getKey() + "'?")
							: ("\n\tApakah maksud kamu '" + cc.getValue() + "' bukannya '" + cc.getKey() + "'?");
				}

				// message to deal with students forgetting to use proper transition for
				// readability
				message += languageCode.equalsIgnoreCase("en")
						? "\nDid you forget to put proper transition between words (capitalisation or underscore) in the identifier?"
						: "\nApakah kamu lupa menggunakan transisi antar kata yang baik (kapitalisasi atau underskor) di nama identifier?";
			} else if (potentialIssue.equals("inconsistent transition capitalisation")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + c.getHintTokenText()
								+ "' seems not to consistently use capitalisation for word transition"
						: "Nama identifier '" + c.getHintTokenText()
								+ "' tampak tidak menggunakan kapitalisasi sebagai transisi antar kata";
			} else if (potentialIssue.equals("inconsistent transition underscore")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + c.getHintTokenText()
								+ "' seems not to consistently use underscore for word transition"
						: "Nama identifier '" + c.getHintTokenText()
								+ "' tampak tidak menggunakan underskor sebagai transisi antar kata";
			}
		} else if (targetedContent.equals("comment")) {
			if (potentialIssue.equals("too short")) {
				message = languageCode.equalsIgnoreCase("en")
						? "A comment '" + c.getHintTokenText() + "' is too short and might be not meaningful"
						: "Komentar '" + c.getHintTokenText() + "' terlalu pendek dan mungkin tidak bermakna";
			} else if (potentialIssue.equals("not meaningful")) {
				message = languageCode.equalsIgnoreCase("en")
						? "A comment starting with '" + c.getHintTokenText()
								+ "' has no meaningful words and might not be descriptive"
						: "Komentar diawali dengan '" + c.getHintTokenText()
								+ "' tidak memiliki kata bermakna dan mungkin tidak deskriptif";
			} else if (potentialIssue.equals("incorrectly written")) {
				message = languageCode.equalsIgnoreCase("en")
						? "Part of a comment started with '" + c.getHintTokenText() + "' might be incorrectly written"
						: "Bagian dari komentar diawali dengan '" + c.getHintTokenText() + "' mungkin salah tulis";

				// per corrected word
				Iterator<Entry<String, String>> it = c.getCorrectedWords().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> cc = it.next();
					message += languageCode.equalsIgnoreCase("en")
							? ("\n\tDid you mean '" + cc.getValue() + "' instead of '" + cc.getKey() + "'?")
							: ("\n\tApakah maksud kamu '" + cc.getValue() + "' bukannya '" + cc.getKey() + "'?");
				}

				// message to deal with students commenting their code
				message += languageCode.equalsIgnoreCase("en")
						? "\nIs it a part of program code commented out? if unnecessary, you might want to remove it"
						: "\nApakah ini bagian dari kode program yang dijadikan komentar? jika tidak diperlukan, kamu mungkin ingin membuangnya";

			} else if (potentialIssue.equals("not descriptive")) {
				message = c.getMessageForNonDescriptiveComment(languageCode);
			}
		}
		return message;
	}

}
