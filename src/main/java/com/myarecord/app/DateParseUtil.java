package com.myarecord.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParseUtil
{
    private static SimpleDateFormat formatsYYYY[] = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy:MM:dd kk:mm:ss"),
            new SimpleDateFormat("yyyyMMdd_kkmm"),
            new SimpleDateFormat("yyyy:MM:dd"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyyMMdd")
    };

    private static SimpleDateFormat formatsMM[] = new SimpleDateFormat[]{
            new SimpleDateFormat("MM.dd.yyyy kk:mm"),
            new SimpleDateFormat("MM-dd-yyyy")
    };

    private static SimpleDateFormat formatsOthers[] = new SimpleDateFormat[]{
            new SimpleDateFormat("E MMM d kk:mm:ss z yyyy"),
            new SimpleDateFormat("E MMM d kkmmss Z yyyy"),
            new SimpleDateFormat("EEEE, MMMM dd, yyyy")};

    private static SimpleDateFormat formatsNoColons[] = new SimpleDateFormat[]{
            new SimpleDateFormat("E MMM d kkmmss z yyyy"),
            new SimpleDateFormat("E MMM d kkmmss Z yyyy")
    };

    public static Date resolveDate(String dateString)
    {
        Date dateValue = null;

        if (dateString != null)
        {
            if (dateString.matches("(19|20)\\d\\d.*"))
            {
                for (SimpleDateFormat format : formatsYYYY)
                {
                    try
                    {
                        dateValue = format.parse(dateString);
                        break;
                    }
                    catch (ParseException e1)
                    {
                        dateValue = null;
                    }
                }
            }
            else if (dateString.matches("(0[1-9]|1[012]).*"))
            {
                for (SimpleDateFormat format : formatsMM)
                {
                    try
                    {
                        dateValue = format.parse(dateString);
                        break;
                    }
                    catch (ParseException e1)
                    {
                        dateValue = null;
                    }
                }
            }
            else
            {
                for (SimpleDateFormat format : formatsOthers)
                {
                    try
                    {
                        dateValue = format.parse(dateString);
                        break;
                    }
                    catch (ParseException e1)
                    {
                        dateValue = null;
                    }
                }
                if(dateValue == null)
                {
                    for (SimpleDateFormat format : formatsNoColons)
                    {
                        try
                        {
                            dateValue = format.parse(dateString.replaceAll(":",""));
                            break;
                        }
                        catch (ParseException e1)
                        {
                            dateValue = null;
                        }
                    }
                }
            }
        }

        return dateValue;
    }

    public static void printSupportedFormats()
    {
        for (SimpleDateFormat format : formatsYYYY)
        {
            System.out.println("  " + format.format(new Date(System.currentTimeMillis())));
        }
        for (SimpleDateFormat format : formatsMM)
        {
            System.out.println("  " + format.format(new Date(System.currentTimeMillis())));
        }
        for (SimpleDateFormat format : formatsOthers)
        {
            System.out.println("  " + format.format(new Date(System.currentTimeMillis())));
        }
    }
}
