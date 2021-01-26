package assign4;

import tokenizer.Tokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse Jack source files and output an indication of either success or failure.
 * 
 * @author djb
 * @version 2020.12.05
 */
public class Main {
    private static final String JACK_SUFFIX = ".jack";
    /**
     * @param args Jack files or a directory of Jack files.
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Usage: java Main file.jack OR dir");
        }
        else {
            File arg = new File(args[0]);
            if(arg.exists() && arg.canRead()) {
                String srcName = arg.getName();
                if(arg.isDirectory() || srcName.endsWith(JACK_SUFFIX)) {
                    List<File> jackFiles = new ArrayList<>();
                    if(arg.isDirectory()) {
                        File[] fileList = arg.listFiles();
                        for(File f : fileList) {
                            if(f.canRead() && f.getName().endsWith(JACK_SUFFIX)) {
                                jackFiles.add(f);
                            }
                        }
                    }
                    else {
                        jackFiles.add(arg);
                    }
                    if(!jackFiles.isEmpty()) {
                        parse(jackFiles);
                    }
                    else {
                        System.err.println("No Jack files to translate.");
                    }
                }
                else {
                    System.err.println(arg.getName() + " is neither a Jack file nor a directory.");
                }
            }
            else {
                System.err.println(arg.getName() + " not found.");
            }
        }
    }

    /**
     * Parse the given list of source files.
     * For each file output the last part of the name of the source
     * file and one of either:
     *     + OK
     *     + Error line N
     * where N is the number of the line most recently read by the tokenizer.
     * @param jackFiles The files to be translated.
     */
    private static void parse(List<File> jackFiles) 
    {
        for(File src : jackFiles) {
            String fullName = src.getAbsolutePath();
            Tokenizer lex = null;
            try(BufferedReader reader = new BufferedReader(new FileReader(src))) {
                System.out.print(src.getName());
                lex = new Tokenizer(reader);
                Parser parser = new Parser(lex);
                parser.parseClass();
                // If we reach here, it is assumed that the parse was successful.
                System.out.println(" OK");
                lex = null;
            }
            catch(Exception ex) {
                if(lex != null) {
                    System.out.println(String.format(" Error line %d", lex.getLineNumber()));
                }
            }
        }
    }
}