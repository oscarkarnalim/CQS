# Code Quality Suggester

**Code Quality Suggester** \(CQS\) aims to educate computing students about code quality in programming. It scans Java/Python program code and highlights code quality issues. To avoid over information, only one instance per issue is reported. Students are expected to check their whole program while fixing the issue. For convenience, the suggestion is mapped to an interactive HTML file as shown below. Further details can be seen in [the paper](#) published in IFIP WCCE 2022: World Conference on Computers in Education. The tool is expanded from [CCS](https://github.com/oscarkarnalim/CCS).

<p align="center">
<img width="80%" src="https://github.com/oscarkarnalim/ccs/blob/main/code_clarity_sample_layout.png?raw=true">
</p>

## Reported code quality issues:

### Java
J01 Overly short comment  
J02 Words in comment not meaningful  
J03 Misspelled words in comment  
J04 No comment before or after the first line of syntax block  
J05 Overly short identifier  
J06 Words in identifier not meaningful  
J07 Misspelled words in identifier  
J08 Inconsistent naming style for identifier  
J09 Abstract class and prefix ‘abstract’ in class name inconsistently used  
J10 Array declaration: brackets used after variable name  
J11 Inline conditionals  
J12 Too many boolean operators in expression  
J13 Lowercased constant attribute name  
J14 Unexpected declaration order; expected order: static variables, attributes, constructors, other methods  
J15 Empty syntax block  
J16 Empty catch block  
J17 Only one empty line needed as separator  
J18 Empty statement  
J19 Too many statements in method  
J20 Overly long line  
J21 Multiple variable declarations  
J22 Missing braces in branching or looping statements  
J23 Overly deep nested looping  
J24 Overly deep nested branching  
J25 Overly deep nested try-catch  
J26 Too many statements per line  
J27 Non-static attribute access without ‘this’ keyword  
J28 Unnecessarily complex boolean expression (except return)  
J29 Unnecessarily complex boolean return statements  
J30 String comparison with ‘==’  
J31 Too many parentheses  
J32 Unnecessary semicolon  

### Python
P01 Overly short comment  
P02 Words in comment not meaningful  
P03 Misspelled words in comment  
P04 No comment before or after the first line of syntax block  
P05 Overly short identifier  
P06 Words in identifier not meaningful  
P07 Misspelled words in identifier  
P08 Inconsistent naming style for identifiers  
P09 Inappropriate indentation  
P10 No space after comment prefix (#)   
P11 Overly long line  
P12 Multiple statements in one line  
P13 Unnecessary semicolon  
P14 Compilation error  
P15 Unused module  
P16 Improper use of ‘break’ or ‘continue’  
P17 Improper use of ‘return’  
P18 Locally undefined identifier  
P19 Duplicate function parameter  
P20 Unused local variable  

## Minimum command
```
-path <program_path> -proglang <programming_language>
```
<program_path> is the complete path of targeted program code file. 
<programming_language> refers to the programming language used in the program code. Values: 'java' (for Java) or 'py' (for Python). 
The result can be seen in 'out.html'. 

## Additional arguments
### Reporting only some of the code quality issues
```
-reselect <issue_codes>
```
As there are no clear consensus about code quality issues, not all of the issues are required to be reported. User can select issues they want to report via this argument. <issue_codes> are a list of codes which issues will be reported separated by comma. For example, "J01,J02" means CQS will only report comments that are overly short or have meaningless words.

### Treating library identifiers as keywords
```
-akpath <additional_keyword_path>
```
It is applicable when some identifiers need to be recognised as keywords. This typically happens when the program uses third-party libraries. <additional_keywords_path> refers to a file containing additional keywords with newline as the delimiter. Keywords with more than one token should be written by embedding spaces between the tokens. For example, 'System.out.print' should be written as 'System . out . print'.

### Human language
```
-humanlang <human_language>
```
This changes the human language used while delivering the suggestions. <human_language> values: 'en' for both British and American English (default), 'id' for Indonesian, and 'iden' for Indonesian and English.

### Reindex words for spelling correction
```
-reindex
```
This forces CQS to reindex word database for spell checking. Please use it only when the human language is changed as the process is quite time consuming.

### Set the output as a text, not a HTML
```
-outtext
```
This alters the resulted output to a standard text file ('out.txt'). This might be useful when CQS is integrated to larger system.

