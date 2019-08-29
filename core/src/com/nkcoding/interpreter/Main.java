package com.nkcoding.interpreter;


import com.nkcoding.interpreter.compiler.*;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.operators.AddAssignmentIntegerOperation;
import com.nkcoding.interpreter.operators.LesserIntegerOperation;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MethodDefinition parseInputDefinition = new MethodDefinition(MethodType.Extern, "parseInput", DataTypes.Integer);

        String tempProgramPath = "D:\\SystemFolders\\Desktop\\simpleprogram.txt";
        String[] program = readFile(tempProgramPath).toArray(String[]::new);
        Compiler compiler = new Compiler(program, new MethodDefinition[] {parseInputDefinition});
        try {
            MethodStatement[] methods = compiler.compile();
            /*
            try {
                methods[0].run(new Stack(10, new ScriptingEngine()));
            } catch (ReturnException e) {
                System.out.println("a return exception: this should not happen");
                e.printStackTrace();
            }
             */
            ScriptingEngine engine = new ScriptingEngine();
            engine.runMethod(methods[0]);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (engine.getFutureQueue().isEmpty()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    engine.getFutureQueue().poll().complete(scanner.nextInt());
                }
            }
        }
        catch (CompileException e) {
            System.out.println("a compile exception: ");
            e.printStackTrace();
        }

    }

    private static List<String> readFile(String filename)
    {
        List<String> records = new ArrayList<String>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();
            return records;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }


}
