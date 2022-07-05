package p6.codeclarity.expansion;

import java.util.ArrayList;

import support.LibTuple;

public class ExpandedClaritySuggestionTuple implements Comparable<ExpandedClaritySuggestionTuple> {
	private LibTuple targetedToken;
	private String visualId;
	private int line, col;
	private String hintTokenText;
	private ArrayList<String> issueKeywords;
	private ArrayList<String> issueExplanations;
	
	public ExpandedClaritySuggestionTuple(LibTuple targetedToken, int line, int col, String hintTokenText,
			ArrayList<String> issueKeywords, ArrayList<String> issueExplanations) {
		super();
		this.targetedToken = targetedToken;
		this.visualId = null;
		this.line = line;
		this.col = col;
		this.hintTokenText = hintTokenText;
		this.hintTokenText = this.hintTokenText.replaceAll("\\s+", " ");
		if(this.hintTokenText.length() > 20)
			this.hintTokenText = this.hintTokenText.substring(0,20) + "...";
		this.issueKeywords = issueKeywords;
		this.issueExplanations = issueExplanations;
	}
	
	@Override
	public int compareTo(ExpandedClaritySuggestionTuple o) {
		// TODO Auto-generated method stub
		if (this.line != o.getLine())
			return this.line - o.getLine();
		else
			return this.col - o.getCol();
	}

	public String toString() {
		String out =  "Line " + this.getLine() + " column " + this.getCol() + ":\n";
		for(int i=0;i< issueKeywords.size();i++) {
			// add each issue
			
			// put the keyword
			out = out +"\t" + issueKeywords.get(i) + "\n";
			// put the explanation
			// the replaceAll is used just to make the text displayed deeper
			out = out + "\t\t" + issueExplanations.get(i).replaceAll("\n", "\n\t"); 
		}
		// just to separate this entry with others
		out += "\n";
		return out;
	}
	
	public ArrayList<String> getIssueKeywords() {
		return issueKeywords;
	}
	
	public String getIssueKeywordsAsString() {
		String out = "";
		for(int i=0;i< issueKeywords.size();i++) {
			out = out + issueKeywords.get(i) + ((i != issueKeywords.size()-1)?",\n":"\n");
		}
		return out;
	}
	
	public String getIssueExplanationsAsString() {
		String out = "";
		for(int i=0;i< issueExplanations.size();i++) {
			out = out + issueExplanations.get(i) + ((i != issueKeywords.size()-1)?",\n":"\n");
		}
		return out;
	}
	
	public void addIssueKeyword(String e) {
		this.issueKeywords.add(e);
	}
	
	public void addIssueExplanation(String e) {
		this.issueExplanations.add(e);
	}
	
	
	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public void setVisualId(String visualId) {
		this.visualId = visualId;
	}

	public LibTuple getTargetedToken() {
		return targetedToken;
	}

	public String getVisualId() {
		return visualId;
	}

	public String getHintTokenText() {
		return hintTokenText;
	}
}
