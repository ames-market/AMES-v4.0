//FIXME: LICENSE
package amesmarket;

import java.io.File;
import java.io.PrintStream;

import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.LoadCaseControlReader;
import amesmarket.filereaders.LoadCaseVerifier;

/**
 * Boot strap class for ames.
 *
 * @author Sean L. Mooney
 *
 */
public class AMESMain {

    /**
     * Show the command line options.
     */
    public static void showHelp(){
        System.err.println(
                VersionInfo.AMES_NAME + "\n"
                + VersionInfo.VERSION + "\n"
                + VersionInfo.DATE + "\n"
                + "[-h|--help]: print help and exit\n"
                + "[--verify-loadcase <load_case_file>]:"
                +    " Verify the load case control, scenario and expected load files\n"
                + "No arguments will load the main GUI.\n"
                );
    }



    /**
     *
     * @param args
     */
    public static void runAMESGui(String[] args){
        AMESGUIFrame.AMESFrame.main(args);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) { //no args. So run the GUI
            runAMESGui(args);
        } else { //process the arguments
            if ("-h".equals(args[0]) || "--help".equals(args[0])) {
                showHelp();
                return; //bail after showing the help.
            } else if ("--verify-loadcase".equals(args[0])) {
                if (args.length < 2) {
                    System.err.println("Must supply the name of the control file.");
                    showHelp();
                    return;
                } else {
                    LoadCaseVerifier lcv = new LoadCaseVerifier();
                    //TODO: Add an option for the tolerance on the load case.
                    if(lcv.runLoadCaseVerification(new File(args[1]), 1e-6, System.out)) {
                        System.out.println(args[1] + " is OK");
                    }
                }
            } else { //error, unknown flag
                showHelp();
            }
        }
    }
}
