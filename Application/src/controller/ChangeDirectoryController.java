package controller;


import java.util.Objects;

/**
 * Static Helper method which is the Controller for Application. (takes input from View)
 */
public class ChangeDirectoryController {
    /**
     * Constant empty String for setting null strings in case of NullExceptions
     */
    private final static String EMPTY_STRING = "";
    /**
     *
     */
    private final static String OS = System.getProperty("os.name");

    /**
     * Method to verify the directory String.
     * Refines directory strings to be exceptionable (with the Operating System).
     * @param myFileDirectory unchecked String from changeDirectoryField
     * @return valid directory String
     */
    public static String refineDirectory(final String myFileDirectory) {
        String theDirectory;

        theDirectory = Objects.requireNonNullElse(myFileDirectory, EMPTY_STRING); //might need a try catch for null

        //[INSERT] verify directory string matches the Operating System of the application
        //maybe add ability to modify directory string to match current Operating System

        //if(OS.contains){}

        return theDirectory;
    }

    /**
     * The default directory changes depending on the operating system
     * @return the default directory
     */
    public static String returnDefaultDirectory(){
        if(OS.contains("Linux")){
            return "/home/";
        } else if (OS.contains("Windows")) {
            return "C:\\";
        } else if (OS.contains("Mac OS")) {
            return "/";
        } else {
            return "/";
        }
    }
}
