package bot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public abstract class Helpers {

    public static Date stringToDate(String dateString){
        dateString = dateString.toUpperCase().replace("TODAY ", "").replace("AT ", "").replace(",", "");
        boolean tomorrow = dateString.contains("TOMORROW ");
        if(tomorrow) dateString = dateString.replace("TOMORROW ", "");
        HashMap<String, Integer[]> patterns = new HashMap<>();
        patterns.put("EEEE h:mma", new Integer[]{Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd/yyyy h:mma", new Integer[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd h:mma", new Integer[]{Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("h:mma", new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("EEEE h:mm a", new Integer[]{Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd/yyyy h:mm a", new Integer[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd h:mm a", new Integer[]{Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("h:mm a", new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        for(String pattern: patterns.keySet()){
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
            Calendar date = Calendar.getInstance();
            Calendar formatted = Calendar.getInstance();
            try {
                formatted.setTime(formatter.parse(dateString));
                for(int field: patterns.get(pattern)){
                    date.set(field, formatted.get(field));
                }
                if(tomorrow) date.add(Calendar.DAY_OF_MONTH, 1);
                while(date.getTime().before(new Date())){
                    date.add(Calendar.DAY_OF_MONTH, 7);
                }
                return date.getTime();
            } catch(ParseException ignored){

            }
        }
        return null;
    }
}
