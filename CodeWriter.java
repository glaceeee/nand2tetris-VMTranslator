package vmTranslator;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
	
	//keep track of number of eq, gt, lt label, since we need a new one everytime we call it
	//keep track of current file name and function name within that file, the return index of the currently called function within our function
	private static Integer EQ_N;
	private static Integer GT_N;
	private static Integer LT_N;
	public static String currentfileName;
	public static String currentfunctionName;
	private static int returnIndex;
	private static FileWriter writer;

	static {
		EQ_N=0;
		GT_N=0;
		LT_N=0;
		returnIndex=0;
	}
	
	CodeWriter(){};
	
	CodeWriter(String outputFile) throws IOException {
		CodeWriter.writer = new FileWriter(outputFile); 
	}
	
	public void setFileName(String fileName) {
		currentfileName = fileName;
	}
	
	public void close() throws IOException {
		writer.close();
	}
	
	public void writeInit() throws IOException {
		//the reason we store the frame of all the bootstrap stuff is for conformity but mostly to retain the possiblity to have programs
		//with multiple entry points and some other weird initializations, i dont really know what im talking about but chatgpt said that's why
		//and it sounded pretty reasonable, even if thats a functionality we're probably never gonna use in this course but who am i to say
		currentfunctionName = "bootstrap";
		writer.write("//SP = 256\n@256\nD=A\n@SP\nM=D\n"
				+ "//call Sys.init\n"
				+ "//push returnAddress\n@" + currentfileName + "." + currentfunctionName + "$ret." + returnIndex + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push LCL\n@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push ARG\n@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push THIS\n@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push THAT\n@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nMD=M+1\n"
				+ "//ARG = SP-5\n@5\nD=D-A\n@ARG\nM=D\n"
				+ "//LCL = SP\n@SP\nD=M\n@LCL\nM=D\n"
				+ "//goto Sys.init\n@Sys.init\n0;JMP\n"
				+ "(" + currentfileName + "." + currentfunctionName + "$ret." + returnIndex + ")\n");
	}

	//only for C_ARITHMETIC
	public void writeArithmetic(String command) throws IOException {
		switch(command) {
		case("add"): 
			writer.write("//add\n@SP\nAM=M-1\nD=M\nA=A-1\nM=D+M\n");
			break;
		case("sub"): 
			writer.write("//sub\n@SP\nAM=M-1\nD=M\nA=A-1\nM=M-D\n");
			break;
		case("neg"): 
			writer.write("//neg\n@SP\nA=M-1\nM=-M\n");
			break;
		case("eq"): 
			EQ_N++;
			//return eq.asm, while renaming EQ_N to EQ_0, EQ_1, ...
			writer.write("//eq\n@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D\nM=-1\n@EQ_N\nD;JEQ\n@SP\nA=M-1\nM=0\n(EQ_N)\n".replaceAll("N", EQ_N.toString()));
			break;
		case("gt"):
			GT_N++;
			//return gt.asm, while renaming GT_N to GT_0, GT_1, ...
			writer.write("//gt\n@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D\nM=-1\n@GT_N\nD;JGT\n@SP\nA=M-1\nM=0\n(GT_N)\n".replaceAll("N", GT_N.toString()));
			break;
		case("lt"): 
			LT_N++;
			//return eq.asm, while renaming
			writer.write("//lt\n@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D\nM=-1\n@LT_N\nD;JLT\n@SP\nA=M-1\nM=0\n(LT_N)\n".replaceAll("N", LT_N.toString()));
			break;
		//bit-wise and
		case("and"): 
			writer.write("//and\n@SP\nAM=M-1\nD=M\nA=A-1\nM=D&M\n");
			break;
		//bit-wise or
		case("or"): 
			writer.write("//or\n@SP\nAM=M-1\nD=M\nA=A-1\nM=D|M\n");
			break;
		//bit-wise not
		case("not"): 
			writer.write("//not\n@SP\nA=M-1\nM=!M\n");
			break;
		default: System.out.println("Invalid C_ARITHMETIC");
		}
	}
	
	//only for C_PUSH, C_POP, will be done in main
	public void writePushPop(String command, String segment, int index) throws IOException {
		switch(command) {
		case("C_PUSH"):
			switch(segment) {
			case("constant"): 
				writer.write("//push constant index\n@index\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			//replace FileName and n by the respective fileName and the index
			case("static"): 
				writer.write("//push static index\n@FileName.n\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString()).replaceAll("FileName",currentfileName).replaceAll("n",((Integer)index).toString())); 
				break;
			case("local"): 
				writer.write("//push local index\n@index\nD=A\n@LCL\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString()));
				break;
			case("argument"): 
				writer.write("//push argument index\n@index\nD=A\n@ARG\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("pointer"): 
				writer.write("//push pointer index\n@index\nD=A\n@THIS\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("this"): 
				writer.write("//push this index\n@index\nD=A\n@THIS\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("that"): 
				writer.write("//push that index\n@index\nD=A\n@THAT\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("temp"): 
				writer.write("//push temp index\n@index\nD=A\n@5\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			default: System.out.println("Invalid C_PUSH");
			}
		break;
		case("C_POP"):
			switch(segment) {
			case("static"): 
				writer.write("//pop static index\n@SP\nAM=M-1\nD=M\n@FileName.index\nM=D\n".replaceAll("index", ((Integer)index).toString()).replaceAll("FileName",currentfileName).replaceAll("n",((Integer)index).toString()));
				break;
			case("local"): 
				writer.write("//pop local index\n@index\nD=A\n@LCL\nD=D+M\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("argument"): 
				writer.write("//pop argument index\n@index\nD=A\n@ARG\nD=D+M\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("pointer"): 
				writer.write("//pop pointer index\n@index\nD=A\n@THIS\nD=D+A\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("this"): 
				writer.write("//pop this index\n@index\nD=A\n@THIS\nD=D+M\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString()));
				break;
			case("that"): 
				writer.write("//pop that index\n@index\nD=A\n@THAT\nD=D+M\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			case("temp"): 
				writer.write("//pop temp index\n@index\nD=A\n@5\nD=D+A\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n".replaceAll("index", ((Integer)index).toString())); 
				break;
			default: System.out.println("Invalid C_POP");
			}
		break;
		default: System.out.println("Invalid C_PUSH/C_POP");
		}
	}		
	
	//only for C_LABEL
	public void writeLabel(String label) throws IOException {
		writer.write("//label " + label + "\n" + "(" + currentfileName + "." + label + ")\n");
	}
	
	//only for C_GOTO
	public void writeGoto(String label) throws IOException {
		writer.write("//goto " + label + "\n" + "@" + currentfileName + "." + label + "\n0;JMP\n");
	}
	
	//only for C_IF
	public void writeIf(String label) throws IOException {
		writer.write("//if-goto " + label + "\n" + "@SP\nAM=M-1\nD=M\n@" + currentfileName + "." + label + "\nD;JNE\n");
	}
	
	//only for C_CALL
	public void writeCall(String functionName, int numArgs) throws IOException {
		//update returnIndex with ever function call in one function
		returnIndex++; 
		//if function already contains "." then don't add the currentFile name to it, i.e. it already has has the fileName in its name 
		//or its from another VM-file
		//if this is the case currentfileNameSave will be used where normally currentfileName would be necessary (absolutely horrid naming and approach but it is what it is)
		String currentfileNameSave = currentfileName;
		String currentfileNameSave2 = currentfileName;
		if(currentfunctionName.contains(".")) {
			currentfileName = "";
		}
		else {
			currentfileName = currentfileName + ".";
		}
		
		if(functionName.contains(".")) {
			currentfileNameSave2 = "";
		}
		else {
			currentfileNameSave2 = currentfileNameSave2 + ".";
		}
		
		
		writer.write("//call " + functionName + " " + numArgs + "\n"
				+ "//push returnAddress\n@" + currentfileName + currentfunctionName + "$ret." + returnIndex + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push LCL\n@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push ARG\n@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push THIS\n@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"
				+ "//push THAT\n@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nMD=M+1\n"
				+ "//ARG = SP-5-numArgs\n@5\nD=D-A\n@" + numArgs + "\nD=D-A\n@ARG\nM=D\n"
				+ "//LCL = SP\n@SP\nD=M\n@LCL\nM=D\n"
				+ "//goto " + currentfileNameSave2 + functionName + "\n@" + currentfileNameSave2 + functionName + "\n0;JMP\n"
				+ "(" + currentfileName + currentfunctionName + "$ret." + returnIndex + ")\n");
		
		currentfileName = currentfileNameSave;
	}
	
	//only for C_FUNCTION
	public void writeFunction(String functionName, int numVars) throws IOException {
		//reset returnIndex everytime VM translator enters a new function
		returnIndex = 0;
		//update currentfunctionName so we know which function were in right now
		currentfunctionName = functionName;
		//if function already contains "." then don't add the currentFile name to it, i.e. it already has the fileName in its name, otherwise add it.
		String currentfileNameSave = currentfileName;
		if(functionName.contains(".")) {
			currentfileName = "";
		}
		else {
			currentfileName = currentfileName + ".";	
		}
		
		writer.write("//function " + currentfileName + functionName + "\n(" + currentfileName + functionName + ")\n");
		//push 0 numArgs times
		for(int i = 0; i < numVars; i++) {
			writer.write("//push constant 0\n@SP\nA=M\nM=0\n@SP\nM=M+1\n");
		}
		
		currentfileName = currentfileNameSave;
	}
	
	//only for C_RETURN
	public void writeReturn() throws IOException {
		writer.write("//RETURN\n//retAddr = *(endFrame-5)\n@LCL\nD=M\n@5\nA=D-A\nD=M\n@R13\nM=D\n"
				+ "//pop argument 0\n@ARG\nD=M\n@SP\nAM=M-1\nD=D+M\nA=D-M\nM=D-A\n"
				+ "//SP = *ARG+1\n@ARG\nD=M+1\n@SP\nM=D\n"
				+ "//THAT = *(endFrame-1)\n@LCL\nA=M-1\nD=M\n@THAT\nM=D\n"
				+ "//THIS = *(endFrame-2)\n@LCL\nD=M-1\nA=D-1\nD=M\n@THIS\nM=D\n"
				+ "//ARG = *(endFrame-3)\n@LCL\nD=M\n@3\nA=D-A\nD=M\n@ARG\nM=D\n"
				+ "//LCL = *(endFrame-4)\n@LCL\nD=M\n@4\nA=D-A\nD=M\n@LCL\nM=D\n"
				+ "//goto retAddr\n@R13\nA=M\n0;JMP\n"); 
	}
}
