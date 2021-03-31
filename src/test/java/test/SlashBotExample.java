package test;

import org.springframework.web.client.RestTemplate;

public class SlashBotExample
{
    public static void main(String[] args)
    {
    	System.out.println(getNewComitList());
    }
    
    private static String getNewComitList() {
		String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/commits/development";
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(link, String.class);
		return result;
	}
}