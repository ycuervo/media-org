package com.myarecord.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

public class DateParseUtilTest
{
    @Test
    public void testResolveDate()
    {
        Date value = DateParseUtil.resolveDate("20170101");
        Assert.assertEquals(new GregorianCalendar(2017, 0, 1).getTime(), value);

        value = DateParseUtil.resolveDate("2017:11:11 23:30:35");
        Assert.assertEquals(new GregorianCalendar(2017, 10, 11, 23, 30, 35).getTime(), value);

        value = DateParseUtil.resolveDate("Sun Nov 19 14:20:36 -08:00 2017");
        Assert.assertEquals(new GregorianCalendar(2017, 10, 19, 14, 20, 36).getTime(), value);
    }
}
