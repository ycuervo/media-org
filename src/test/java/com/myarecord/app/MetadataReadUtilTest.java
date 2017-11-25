package com.myarecord.app;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Unit test for simple App.
 */
public class MetadataReadUtilTest
{
    @Test
    public void testGetDateFromMetadataJPG()
            throws Exception
    {
        File file = new File("src/test/resources/20171111_233035.jpg");

        String dateFound = MetadataReadUtil.getDateFromMetadata(file);

        Assert.assertEquals("2017:11:11 23:30:35", dateFound);
    }

    @Test
    public void testGetDateFromMetadataMP4()
            throws Exception
    {
        File file = new File("src/test/resources/20171119_142034.mp4");

        String dateFound = MetadataReadUtil.getDateFromMetadata(file);

        Assert.assertEquals("Sun Nov 19 14:20:36 -08:00 2017", dateFound);
    }
}
