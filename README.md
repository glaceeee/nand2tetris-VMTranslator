# nand2tetris-VMTranslator
The VM-Translator created within the nand2tetris project.
Like i said in my first (and probably only commit message) this is the implementation of the VM-translator participants of the course of nand2tetris part 2 were tasked to write.
(also be alarmed cause the documentation in this may not be the greatest since it was one of my first ever coding projects)
My implementation works for the test-scripts that were provided in the course but i havent tested them any further than that. Also this translates VM-commands line by line into the HACK Assembly, which means its really inefficient.
- Main.java executes everything, creates a parser and codewriter object and then goes to town with those. As input it gets either a single .vm file or a folder that contains at least one .vm file (it can also contain other files, Main.java will simply ignore thos)
- Parser.java returns the command type were looking at: C_ARITHMETIC, C_PUSHPOP, C_FUNCTION, and so on and also the virtual memory segment plus the index (like they were specified in the course (if you read this you are most likely already familiar with all the command types so i wont go into detail)
- CodeWriter.java then generates the necessary assembly code with the given instruction. Like i said, line-by-line so its really unoptimized (as was specified in the course).

















:3
