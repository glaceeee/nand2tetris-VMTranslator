package vmTranslator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.io.FileReader;

public class Parser {
	
	//if you're looking at this in the future and for some reason happen to care, please find a way to only use currentCommand
	private static BufferedReader br;
	private static String currentCommand;
	private static HashMap<String,String> commandTypeTable;
	private static int stringIndex;
	private static String commandAdvanced;
	
	static {
		stringIndex = 0;
		commandAdvanced = "";
		//if you wanted to make this more space efficient and in some select cases also more time efficient (tho not
		//generally speaking) you could use a switch-case block here that is ordered in a way that selects the most
		//probable commandTypes first
		commandTypeTable = new HashMap<String,String>();
		commandTypeTable.put("add", "C_ARITHMETIC");
		commandTypeTable.put("sub", "C_ARITHMETIC");
		commandTypeTable.put("neg", "C_ARITHMETIC");		
		commandTypeTable.put("eq", "C_ARITHMETIC");
		commandTypeTable.put("gt", "C_ARITHMETIC");
		commandTypeTable.put("lt", "C_ARITHMETIC");
		commandTypeTable.put("and", "C_ARITHMETIC");
		commandTypeTable.put("or", "C_ARITHMETIC");
		commandTypeTable.put("not", "C_ARITHMETIC");
		commandTypeTable.put("push", "C_PUSH");
		commandTypeTable.put("pop", "C_POP");
		commandTypeTable.put("label", "C_LABEL");
		commandTypeTable.put("goto", "C_GOTO");
		commandTypeTable.put("if-goto", "C_IF");
		commandTypeTable.put("function", "C_FUNCTION");
		commandTypeTable.put("call", "C_CALL");
		commandTypeTable.put("return", "C_RETURN");
	}
	
	Parser(String inputFile) throws FileNotFoundException, IOException {
		Parser.br = new BufferedReader(new FileReader(inputFile));
		Parser.currentCommand = br.readLine();
	}
	
	public String getCurrentCommand() {
		return Parser.currentCommand;
	}
	
	public String getCommandAdvanced() {
		return Parser.commandAdvanced;
	}
	
	//determines if we're at the end of the input file
	public boolean hasMoreCommands() {
		if(currentCommand==null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public String advance() throws IOException{
		if(this.hasMoreCommands()==true) {
			//is currentCommand whitespace?
			if(currentCommand.replaceAll("^\\s+","").startsWith("/")||currentCommand.replaceAll("\\s+","").isEmpty()) {
				currentCommand = br.readLine();
				commandAdvanced = "isWhitespace";
				return commandAdvanced;
			}
			else { 
				commandAdvanced = currentCommand.replace("\\s+", " ").trim();
				currentCommand = br.readLine();
				return commandAdvanced;
			}
		}
		else {
			currentCommand = br.readLine();
			commandAdvanced = "end";
			br.close();
			return commandAdvanced;
		}
	}

	//scans through a string and updates stringIndex based on what it did
	private void scanString(String s) {
		while(true) {			
			
			if(stringIndex == s.length()) {
				break;
			}
			
			if(Character.isWhitespace(s.charAt(stringIndex))||s.charAt(stringIndex)=='/') {
				break;
			}

			else {
				stringIndex++;
			}
		}
	}
	
	//returns command type of current operation, i.e. C_ARITHMETIC, C_PUSH, C_POP
	public String commandType() {
		stringIndex = 0;
		scanString(commandAdvanced);
		return commandTypeTable.get(commandAdvanced.substring(0, stringIndex));
	}
	
	//returns first argument of current operation based on its previously determined command type, i.e. push, pop, add, sub, neg,...
	public String arg1() {
		int temp = ++stringIndex;
		switch(this.commandType()) {
		case("C_ARITHMETIC"):
			//scans C_ARITHMETIC operation
			return commandAdvanced.substring(0, --temp);
		case("C_PUSH"): 
			//scans next element after "push"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_POP"): 
			//scans next element after "pop"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_LABEL"):
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_GOTO"):
			//scans next element after "goto"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_IF"):
			//scans next element after "if-goto"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_FUNCTION"):
			//scans next element after "function"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		case("C_CALL"):
			//scans next element after "call"
			stringIndex++;
			scanString(commandAdvanced);
			return commandAdvanced.substring(temp, stringIndex);
		default:
			return "Invalid Command";
		}
	}
	
	//only call for C_PUSH, C_POP, C_FUNCTION, C_CALL (will be done in main, as to not add unnecessary function calls to the stack)
	public int arg2() {
		stringIndex++;
		int firstIndexNumber = stringIndex;
		scanString(commandAdvanced);
		int lastIndexNumber = stringIndex;
		stringIndex = 0;
		return Integer.parseInt(commandAdvanced.substring(firstIndexNumber, lastIndexNumber));
	}
}
