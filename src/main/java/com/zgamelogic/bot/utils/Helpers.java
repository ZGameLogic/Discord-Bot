package com.zgamelogic.bot.utils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.util.*;

public abstract class Helpers {

    public static final String STD_HELPER_MESSAGE = """
            Invalid date and time. Here are some examples of valid dates:
            7:00pm
            Today at 7:00pm
            Tomorrow 6pm
            3/20/2024 4:15pm
            6/20 3:45pm
            9pm
            Wednesday at 4pm
            0930
            Thursday at 15:45
            """;

    public static Date stringToDate(String dateString){
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(dateString);

        if (groups.isEmpty()) return null;

        DateGroup group = groups.get(0);
        List<Date> dates = group.getDates();

        return dates.isEmpty() ? null : dates.get(0);
    }
}
