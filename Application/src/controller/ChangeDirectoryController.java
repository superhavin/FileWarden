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

        return theDirectory;
    }

    /**
     * The default directory changes depending on the operating system
     * @return the default directory
     */
    public static String returnDefaultDirectory(){
        //[INSERT] method to find the operating system of the filesystem, and return the default directory
        final String OS = System.getProperty("os.name");
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
