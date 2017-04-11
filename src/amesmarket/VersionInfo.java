//FIXME: LICENSE
package amesmarket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


//TODO: Refactor all the version info scattered through the program here.
//Load in main information from the version.properties file.
public final class VersionInfo {

    static {
        String major = "??";
        String minor = "??";
        String rev = "??";
        String date = "05/04/2017";
        String codename = "";
        String commit="";

        Properties props = new Properties();
        InputStream is = VersionInfo.class.getResourceAsStream("/resources/version.properties");
        if(is != null){
            try{
                props.load(is);
            }catch(IOException e){
                System.err.println("Could not read version.properties: " + e);
            }

            major = props.getProperty("version.major", major);
            minor = props.getProperty("version.minor", minor);
            rev = props.getProperty("version.rev", rev);
            date = props.getProperty("version.date", date);
            codename = props.getProperty("version.codename");
            commit = props.getProperty("version.build", commit);
        }else{
            System.err.println("Could not find the version.properties resource.");
        }

        AMES_NAME     = "AMES" + codename;
        VERSION_MAJOR = major;
        VERSION_MINOR = minor;
        VERSION_REV   = rev;
        VERSION       = major + "." + minor + "." + rev + ("".equals(commit) ? "" : "-" + commit); //only include the commit it has a value. Release builds should set a value for the commit.
        DATE          = date;
    }

    public static final String AMES_NAME;
    public static final String VERSION;
    public static final String VERSION_MAJOR;
    public static final String VERSION_MINOR;
    public static final String VERSION_REV;
    public static final String DATE;


}
