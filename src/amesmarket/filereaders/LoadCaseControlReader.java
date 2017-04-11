//FIXME-X: License
package amesmarket.filereaders;

import java.io.File;

import amesmarket.LoadCaseControl;

/**
 * Read a load scenario control file and create a object representing that data.
 *
 * @author Sean L. Mooney
 *
 */
//TODO-X: Validation order. Several 'odd' error messages happen
//if the CaseName, NumZones,NumLoadScenarios,MaxDay are not
//declared before the rest of the file.
public class LoadCaseControlReader extends AbstractConfigFileReader<LoadCaseControl> {

    private boolean validateAfterRead = true;
    private double probTol = 1e-3;

    private CONTROL_TYPE controlType = CONTROL_TYPE.UNKNOWN;

    /**
     * The load case control object to 'fill'.
     * A new instance of this file will be created when a master
     * control file is read. The existing 'master' reference
     * should be linked in for aux control files.
     */
    private LoadCaseControl lsc = null;

    /**
     * Read in load control file until the end of the file/reader stream.
     *
     * The returned object has not been validated. The client is responsible
     * for doing that at a sensible time.
     *
     * @param expectedEntries expected number of entries in each line of the data stream.
     * @return a list of load profiles.
     * @throws BadDataFileFormatException
     *
     */
    @Override
    protected LoadCaseControl read() throws BadDataFileFormatException {
        /*
         * Breaks the general protocol for {@link #move(boolean)}. This is because
         * of the loop and-a-half issue, coupled with needing to look ahead in
         * the stream to check for the end of loop and no push-back into the input stream.
         *
         * Given that this is the only case were we currently need push-back/look ahead,
         * it is easier to break the protocol than write up a new input reader.
         *
         * If a full-fledged lexer ever gets written, it would be worth adding look ahead/push-back
         * to it and adhering to the protocol.
         */

        //aux files use/extend the case information from the master file.
        if(controlType != CONTROL_TYPE.AUX){
            lsc  = new LoadCaseControl();
        }

        //loop and half
        while( true ) {
            move(false);

            //now that we have moved, check the condition. currentLine will be
            //null if the file ended.
            if(currentLine == null) break;

            LoadCaseLabelInfo labelInfo = null;
            try{
                labelInfo = new LoadCaseLabelInfo(currentLine);
            }catch(BadDataFileFormatException bff) {
                throw new BadDataFileFormatException(sourceFile, lineNum, bff.getMessage());
            }

            if(labelInfo.isCaseName()){
                //Set or verify the case name
                if(controlType == CONTROL_TYPE.AUX){
                    String expCaseName = lsc.getCaseName();
                    if(expCaseName == null || expCaseName.equals("")){
                        throw new BadDataFileFormatException(sourceFile, lineNum,
                                "Must specify the CaseName before aux control files"
                                );
                    }else if(!expCaseName.equals(labelInfo.value())){
                        throw new BadDataFileFormatException(sourceFile, lineNum,
                                "Expected CaseName: " + expCaseName + "." +
                                " Found " + labelInfo.value() + ".");
                    }
                }else {
                    lsc.setCaseName(labelInfo.value());
                }
            } else if (labelInfo.isNumLoadScen()) {
                //Set the number of load scenarios
                lsc.setNumLoadScenarios(stoi(labelInfo.value()));
            } else if (labelInfo.isNumZones()) {
                //Set the number of zones
                lsc.setNumZones(stoi(labelInfo.value()));
            } else if (labelInfo.isMaxDay()) {
                //Set the max number of days
                lsc.setMaxDay(stoi(labelInfo.value()));
            } else if (labelInfo.isAux()) {
                //read an aux load file
                readAuxControlFile(labelInfo, lsc);
            } else if (labelInfo.isScenario()) {
                //for the individual day/scenario entries
                readDayScenarioInfo(labelInfo, lsc);
            } else if (labelInfo.isExpLoad()) {
                //Set the expected load file.
                readDayScenarioInfo(labelInfo, lsc);
            } else if (labelInfo.isActualLoad()) {
                //Read 'actual' load
                readDayScenarioInfo(labelInfo, lsc);
            } else {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        String.format("Unknown label in '%s'", currentLine));
            }
        }

        if(validateAfterRead){
            lsc.validate(probTol);
        }
        return lsc;
    }

    private void readAuxControlFile(LoadCaseLabelInfo decl, LoadCaseControl lsc) throws BadDataFileFormatException{

        File auxFile = null;

        if (!decl.hasDay()){
            throw new BadDataFileFormatException(sourceFile, lineNum,
                    "Incorrect auxiliary control file declaration:.\n"
                    + " Expected: Day INT Aux_Control_File : path/to/file\n"
                    + " Found: " + currentLine
                    );
        }

        int dNum = decl.day();
        String value = decl.value();
        if ("".equals(value) || value == null)
            throw new BadDataFileFormatException(sourceFile, lineNum, "Aux control file name not found.");

        //the aux file is relative to the control file
        auxFile = new File(expandPath(value));
        if(!auxFile.exists()){
            throw new BadDataFileFormatException(sourceFile, lineNum, "Aux control file " +
                    auxFile.getPath() + " not found.");
        }

        //TODO-X: Loop detection e.g. prevent f1 -> f2 -> f1 type of a situation
        LoadCaseControlReader auxReader = new LoadCaseControlReader();
        auxReader.lsc = lsc;
        auxReader.readAuxControlFile(auxFile);
    }

    private LoadCaseControl readAuxControlFile(File auxFile) throws BadDataFileFormatException {
        controlType = CONTROL_TYPE.AUX;
        setValidateAfterRead(false);
        return read(auxFile);
    }

    /**
     * Read either a load scenario file or a load scenario probability.
     *
     * Handles scenarios, exected load and actual. Expected/Actual are just special cases
     * of load files.
     * @param data
     * @param lsc dest for info. Prob or scen. file will be set into the lsc.
     * @throws BadDataFileFormatException
     *
     * Assume lbl is:
     *  Day INT Scenario INT [File | Prob].
     */
    private void readDayScenarioInfo (LoadCaseLabelInfo decl, LoadCaseControl lsc) throws BadDataFileFormatException{

        int sNum = -1;

        //check for the special cases of exp/actual and complain if there is a probability assigned.
        if (decl.isExpLoad()) {
            sNum = LoadCaseControl.EXPECTED_LOAD_SCEN_NUM;
            if (decl.hasProb())
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        "Cannot assign a probability to an expected load");
        } else if (decl.isActualLoad()) {
            sNum = LoadCaseControl.ACTUAL_LOAD_SCEN_NUM;
            if (decl.hasProb())
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        "Cannot assign a probability to an actual load");
        } else {
            sNum = decl.scenario();
        }

        int dNum = -1;
        if (decl.hasDay()) {
            dNum = decl.day();

            //TODO-X: Breaks if called before the max day is set.
            if (dNum > lsc.getMaxDay() || dNum < 1) {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        "Invalid day in '" + currentLine + "'\n."
                                + "Day must be between 1 and "
                                + lsc.getMaxDay());
            }
        }

        if (decl.hasDay()) { //set just 1 day.
            if (decl.hasFile()) { //file decl
                String filePath = expandPath(decl.value());
                if(decl.hasWindDecl()) {
                    //System.err.println("WARNING: Ignoring wind file for "
                    //        + " scenario " + sNum + " day " + dNum
                    //        );
                    lsc.setScenarioDayWindFilePath(dNum, sNum, filePath);
                } else { //if marked or load, or with nothing.
                    lsc.setScenarioDayFilePath(dNum, sNum, filePath);
                }
            }
            if (decl.hasProb()) { //prob decl
                if( lsc.hasScenarioProbality(dNum, sNum) ) {
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format( "Probability already set for scenario %s, day %d.", sNum, dNum));
                }
                lsc.setScenarioDayProbability(dNum, sNum, stod(decl.value()));
            }
        } else { //set for all days.
            if (decl.hasFile()) { //file decl
                String filePath = expandPath(decl.value());
                if(decl.hasWindDecl()) {
                    lsc.setAllScenarioWindFilePaths(sNum, filePath);
                } else {
                    lsc.setAllScenarioFilePaths(sNum, filePath);
                }
            }
            if (decl.hasProb()) { //prob decl
                if( lsc.hasScenarioProbality(sNum) ) {
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format( "Probability already set for scenario %s.", sNum));
                }
                lsc.setAllScenarioProbabilities(sNum, stod(decl.value()));
            }
        }
    }

    /**
     * Compute the path of a another file, relative to the {@link #sourceFile}.
     *
     * If the {@link #sourceFile} is not null, the specification says
     * all scenario files are relative to the location of
     * of the control file. If it is null, just use the scenarioFileName
     * as is.
     *
     * Because the reader may also take an inputstream/reader as the
     * source, it is necessary to check for the relative path issue.
     *
     * @param scenFileName
     * @return
     */
    private String expandPath(String scenFileName){
        File scenarioHome = null;

        if(sourceFile != null){
            scenarioHome = sourceFile.getParentFile();
        }

        if(scenarioHome == null){
            return scenFileName;
        }else{
            File f = new File(scenarioHome, scenFileName);
            return f.getPath();
        }
    }

    /**
     * @return the validateAfterRead
     */
    public boolean isValidateAfterRead() {
        return validateAfterRead;
    }

    /**
     * Change whether or not the read in LoadControl objects are validated.
     *
     * It is occasionally useful (e.g. for unit tests) to turn off validation.
     * @param validateAfterRead if true, validate the LoadCaseControl file after it is read.
     */
    public void setValidateAfterRead(boolean validateAfterRead) {
        this.validateAfterRead = validateAfterRead;
    }

    /**
     * @return the probTol
     */
    public double getProbTol() {
        return probTol;
    }

    /**
     * @param probTol the probTol to set
     */
    public void setProbTol(double probTol) {
        this.probTol = probTol;
    }

    private enum CONTROL_TYPE {
        MASTER, AUX, UNKNOWN
    }
}
