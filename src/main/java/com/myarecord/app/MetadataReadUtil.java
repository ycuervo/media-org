package com.myarecord.app;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.util.Collection;

public class MetadataReadUtil
{
    private static final String ORIGINAL = "DATE/TIME ORIGINAL";
    private static final String DIGITIZED = "DATE/TIME DIGITIZED";
    private static final String DATE_TIME = "DATE/TIME";
    private static final String MODIFIED_DATE = "FILE MODIFIED DATE";

    public static String getDateFromMetadata(File file)
    {
        String dateToUse = null;

        String dateValueOriginal = null;
        String dateValueDigitized = null;
        String dateValueDateTime = null;
        String dateValueModifiedDate = null;
        String dateValue = null;

        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            Iterable<Directory> tags = metadata.getDirectories();
            for (Directory tag : tags)
            {
                Collection<Tag> tagList = tag.getTags();

                for (Tag t : tagList)
                {
                    String tagName = t.getTagName().toUpperCase();

                    if (tagName.contains("DATE"))
                    {
                        if (tagName.equalsIgnoreCase(ORIGINAL))
                        {
                            dateValueOriginal = t.getDescription();
                        }
                        else if (tagName.equalsIgnoreCase(DIGITIZED))
                        {
                            dateValueDigitized = t.getDescription();
                        }
                        else if (tagName.equalsIgnoreCase(DATE_TIME))
                        {
                            dateValueDateTime = t.getDescription();
                        }
                        else if (tagName.equalsIgnoreCase(MODIFIED_DATE))
                        {
                            dateValueModifiedDate = t.getDescription();
                        }
                        else
                        {
                            dateValue = t.getDescription();
                        }
                    }
                }
            }

            if (dateValueOriginal != null)
            {
                dateToUse = dateValueOriginal;
            }
            else if (dateValueDigitized != null)
            {
                dateToUse = dateValueDigitized;
            }
            else if (dateValueDateTime != null)
            {
                dateToUse = dateValueDateTime;
            }
            else if (dateValueModifiedDate != null)
            {
                dateToUse = dateValueModifiedDate;
            }
            else
            {
                dateToUse = dateValue;
            }
        }
        catch (Exception e)
        {
            System.err.println("MetaData Error Processing[" + file.getAbsolutePath() + "]");
        }

        return dateToUse;
    }

}
