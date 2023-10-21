package vmTranslator;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main {

	public static boolean isDirectory = false;	//you could actually get by removing this but ill leave it in for readability and such
	
	//Scans input path and determines if the path is a directory or just a singular file. "isDirectory" is then adjusted accordingly.
	//Outputs a String-Array that holds all the elements, ending in .vm, or the singular .vm file. 
	//Outputs an empty String-Array if input path is a directory containing no .vm files or is a file that does not end in .vm.
	
	//also this whole thing i described above is done in such a way that the resulting array looks as follows:
	//[FolderName, File1.Path, File1.Name, File2.Path, File2.Name, ...]
	private static String[] directory2StringArray(String path) {
		File file = new File(path);
		if(file.listFiles() != null) {
			isDirectory = true;
			ArrayList<String> retList = new ArrayList<String>();
			retList.add(file.getName());
			for(File f : file.listFiles()) {
				if(f.getName().endsWith(".vm")) {
					retList.add(f.getPath());
					retList.add(f.getName().replace(".vm",""));
				}
			}
			return retList.toArray(new String[retList.size()]);
		}
		else {
			isDirectory = false;
			String retArr[] = new String[3];
			retArr[0] = file.getParent().substring(file.getParent().lastIndexOf("\\")+1);
			if(file.exists() && file.getName().endsWith(".vm")) {
				retArr[1] = file.getPath();
				retArr[2] = file.getName().replace(".vm","");
			}
			return retArr;
		}
	}
	
	private static void translate(String input, String outputFile) throws FileNotFoundException, IOException {
		CodeWriter codeWriter = new CodeWriter(outputFile);
		String[] directory = directory2StringArray(input);
		codeWriter.setFileName(directory[0]); //cf. directory2StringArray (doc) as to why
		//only if there's at least one existing .vm-file in "input", is the VM-translator gonna be able to do its' thing
		if(directory.length != 0 && directory[0] != null && directory[1] != null && directory[2] != null) {
			//only add bootstrap code if "input" represents the path of a directory not a singular file
			if(isDirectory == true) {
				codeWriter.writeInit();
			}
			//scans through all .vm-files in the provided directory / or the singular provided .vm-file
			for(int i = 1; i < directory.length-1; i++) {
				//construct a new Parser for each .vm-file, that keeps advancing through the .vm-file until it hits its' end, translating each
				//line it comes across, while also ignoring whitespace
				Parser parser = new Parser(directory[i]);
				codeWriter.setFileName(directory[++i]);
				while(parser.advance()!="end") {
					switch(parser.getCommandAdvanced()) {
					case("isWhitespace"): 
						continue;
					default:
						switch(parser.commandType()) {
						case("C_PUSH"): 
							codeWriter.writePushPop("C_PUSH", parser.arg1(), parser.arg2()); 
							break;
						case("C_POP"): 
							codeWriter.writePushPop("C_POP", parser.arg1(), parser.arg2()); 
							break;
						case("C_ARITHMETIC"): 
							codeWriter.writeArithmetic(parser.arg1()); 
							break;
						case("C_LABEL"):
							codeWriter.writeLabel(parser.arg1());
							break;
						case("C_GOTO"):
							codeWriter.writeGoto(parser.arg1());
							break;
						case("C_IF"):
							codeWriter.writeIf(parser.arg1());
							break;
						case("C_FUNCTION"):
							codeWriter.writeFunction(parser.arg1(), parser.arg2());
							break;
						case("C_CALL"):
							codeWriter.writeCall(parser.arg1(), parser.arg2());
							break;
						case("C_RETURN"):
							codeWriter.writeReturn();
							break;
						default: System.out.println("Invalid C_ command");
						}
					}
				}
				parser = null;
			}
		}
		codeWriter.close(); //codeWriter.close() does the same as fileWriter.close()
		codeWriter = null;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		translate("E:\\11\\Seven", "E:\\11\\Seven\\Seven.asm");
	}
}
