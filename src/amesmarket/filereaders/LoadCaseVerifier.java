//TODO: LICENCE
package amesmarket.filereaders;

import java.io.File;
import java.io.PrintStream;

import amesmarket.LoadCaseControl;
import amesmarket.Support;
import amesmarket.VersionInfo;

/**
 *
 * @author Sean L. Mooney
 *
 */
public class LoadCaseVerifier {

    private boolean printStackTrace = false;

    /**
     * Verify the control file, scenario and load for f.
     * @param f
     * @param probTol how close the sum of the scenario probs need to be to be considered equal to 1.
     * @param out as a param instead of System.out to have option of using a file stream later.
     * @return true if all the files validate, false otherwise
     */
    public boolean runLoadCaseVerification(File f, double probTol, PrintStream out){
        if(!f.exists()){
            out.println(f.getPath() + " does not exist");
            return false;
        } else {
            out.println("Checking LoadCase control file " + f.getPath());
        }

        LoadCaseControlReader lccr = new LoadCaseControlReader();
        lccr.setProbTol(probTol);

        try {
            LoadCaseControl control = lccr.read(f);
            control.setZoneNameIndexes(new IZoneIndexProvider.
                    AutomaticIndexProvider());


            StringBuilder problemLog = new StringBuilder();
            //read each scenario
            out.println("Checking Load Scenarios:");
            for(int sn = 1; sn <= control.getNumLoadScenarios(); sn++){
                try{
                    control.getLoadScenario(sn);
                    out.println("Scenario " + sn + " OK");
                }catch(BadDataFileFormatException e){
                    if(printStackTrace)
                        e.printStackTrace();
                    problemLog.append("Problem in Load Scenario ");
                    problemLog.append(sn); problemLog.append('\n');
                    problemLog.append(e.getMessage());
                    problemLog.append("\n\n");
                }
            }

            //read the expected load, if exists
            if(control.hasExternalExpectedLoadProfile()){
                out.println("Checking Expected Load file:");
                try{
                    control.getExpectedLoadProfiles();
                    out.println("Expected Load OK");
                }catch(BadDataFileFormatException e){
                    if(printStackTrace)
                        e.printStackTrace();
                    problemLog.append("Problem in ExpectedLoad\n");
                    problemLog.append(e.getMessage());
                    problemLog.append("\n\n");
                }
            }else{
                out.println("No ExpectedLoad file specified");
            }

            //read the expected load, if exists
            if(control.hasExternalActualLoadProfiles()){
                out.println("Checking Actual Load file:");
                try{
                    control.getActualLoadProfiles();
                    out.println("Actual Load OK");
                }catch(BadDataFileFormatException e){
                    if(printStackTrace)
                        e.printStackTrace();
                    problemLog.append("Problem in ActualLoad\n");
                    problemLog.append(e.getMessage());
                    problemLog.append("\n\n");
                }
            }else{
                out.println("No ActualLoad file specified");
            }



            if(problemLog.length() > 0){
                out.println("Validation Failed!!!\n");
                out.println(problemLog.toString());
                return false;
            }
        } catch (BadDataFileFormatException e) {
            out.println("Control File Validate Failed\n" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        if(args.length == 0){
            showHelp();
            return;
        }

        double probTol = 1e-6;

        if ("-h".equals(args[0]) || "--help".equals(args[0])) {
            showHelp();
            return; //bail after showing the help.
        } else if ("-v".equals(args[0]) || "--version".equals(args[0])) {
            showVersion();
            return;
        }

        //TODO: Should be a command line arg.
        String spt = System.getProperty("tolerance", "1e-6");
        try {
            probTol = Support.parseDouble(spt);
        }
        catch (NumberFormatException nfe) {
            System.err.println(spt + " is not a valid number.");
            return;
        }

        if ( !( 0.0 <= probTol ) && !( probTol < 1.0 ) ) {
            System.err.println("tolerance property must be between 0 and 1");
            return;
        }

        LoadCaseVerifier lcv = new LoadCaseVerifier();
        for(String fName : args) {
            File controlFile = new File(fName);
            System.out.println(
                    //Delmit a single control file set of checks
"################################################################################");
            lcv.runLoadCaseVerification(controlFile, probTol, System.out);
            System.out.println(
                    String.format(
"################################################################################%n"));
        }
    }

    /**
     * Show the command line options.
     */
    private static void showHelp(){
        StringBuilder sb = new StringBuilder();
        final String EOL = System.getProperty("line.separator");
        sb.append(VersionInfo.AMES_NAME);
        sb.append(" LoadCase Verifier ");
        //sb.append(VersionInfo.DATE); sb.append(EOL);
        sb.append("checks a list of load case control files and associated scenario files ");
        sb.append("for well-formedness and completeness.");
        sb.append(EOL); sb.append(EOL);
        sb.append("Usage: java -jar lcv.jar [OPTIONS] <path/to/control_file>[, path/to/control_file]*");
            sb.append(EOL); sb.append(EOL);
        sb.append("Options:"); sb.append(EOL);
        sb.append("  -v, --version\t\tshow version information "); sb.append(EOL);
        sb.append("  -h, --help\t\tprint help and exit\n"); sb.append(EOL);
        sb.append("Properties (set with -Dname=value"); sb.append(EOL);
        sb.append("  tolerance, tolerance for how close sum of probabilities needs to be to 1.0");
        System.out.println(sb.toString());
    }

    private static void showVersion() {
        System.out.println(VersionInfo.VERSION);
    }
}
