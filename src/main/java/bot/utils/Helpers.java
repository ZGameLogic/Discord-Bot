package bot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public abstract class Helpers {

    public static final String STD_HELPER_MESSAGE = "Invalid date and time. Here are some examples of valid dates:\n" +
            "7:00pm\n" +
            "Today at 7:00pm\n" +
            "Tomorrow 6pm\n" +
            "3/20/2024 4:15pm\n" +
            "6/20 3:45pm\n" +
            "9pm\n" +
            "Wednesday at 4pm";

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
        patterns.put("EEEE ha", new Integer[]{Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd/yyyy ha", new Integer[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd ha", new Integer[]{Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("ha", new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("EEEE h a", new Integer[]{Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd/yyyy h a", new Integer[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("M/dd h a", new Integer[]{Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE});
        patterns.put("h a", new Integer[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE});
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
