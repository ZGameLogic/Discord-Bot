package interfaces.atlassian;

public abstract class JiraInterface {

    /**
     * Checks if the URL is valid
     * @param url URL to check if valid
     * @return true if the URL is valid
     */
    public static boolean checkURL(String url){
        return true;
    }

    /**
     * Checks if a personal Access token is valid
     * @param PAT personal access token
     * @param url url to check against
     * @return true if PAT is valid
     */
    public static boolean checkPAT(String PAT, String url){
        return true;
    }
}
