package com.myarecord.app;

import java.io.File;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Tool for organizing media.
 * Uses ExifTool, metadata, folder name, file name to try to figure out date created.
 */
public class MediaOrg
{
    //set via -f parameter
    private static File DIR_FROM;
    //set via -t parameter
    private static File DIR_TO;

    private static Calendar calendar = new GregorianCalendar();
    private static NumberFormat yearFormat = new DecimalFormat("0000");
    private static NumberFormat monthFormat = new DecimalFormat("00");
    private static NumberFormat dayFormat = new DecimalFormat("00");
    private static NumberFormat counterFormat = new DecimalFormat("00");
    private static SimpleDateFormat formatFileOut = new SimpleDateFormat("yyyyMMdd_kkmm");

    private static String md5 = "";

    private static void processDir(File directory)
    {
        System.out.println("Processing directory: " + directory.getAbsolutePath());
        File fileList[] = directory.listFiles();

        if (fileList != null)
        {
            Arrays.sort(fileList);

            for (File file : fileList)
            {
                if (file.isDirectory())
                {
                    processDir(file);

                    if (file.list().length == 0 &&
                        !file.getAbsolutePath().equalsIgnoreCase(DIR_FROM.getAbsolutePath()))
                    {
                        //directory is empty... delete it
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                else
                {
                    String fileNewName;
                    String fileOldName = file.getAbsolutePath();

                    fileNewName = processByMetaData(file);
                    if (fileNewName != null)
                    {
                        System.out.println("MetaData Handled: " + fileOldName + " -> " + fileNewName);
                    }
                    else
                    {
                        fileNewName = processByFolderName(file);
                        if (fileNewName != null)
                        {
                            System.out.println("Folder Name Handled: " + fileOldName + " -> " + fileNewName);
                        }
                        else
                        {
                            fileNewName = processByFileName(file);
                            if (fileNewName != null)
                            {
                                System.out.println("File Name Handled: " + fileOldName + " -> " + fileNewName);
                            }
                            else
                            {
                                fileNewName = moveToUnknown(file);
                                if (fileNewName != null)
                                {
                                    System.out.println("Moved To Unknown: " + fileOldName + " -> " + fileNewName);
                                }
                                else
                                {
                                    System.err.println(" File Skipped: " + fileOldName);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private static String processByMetaData(File file)
    {
        String dateToUse = MetadataReadUtil.getDateFromMetadata(file);

        return resolveDateFound(file, dateToUse);
    }

    private static String processByFolderName(File file)
    {
        String dateToUse = null;

        try
        {
            //get the folder this file is in
            File folder = file.getParentFile();
            if (folder.isDirectory())
            {
                String folderName = folder.getName();

                //looking for folders name yyyy-mm-dd... assuming first 10 characters are the date
                if (folderName.length() > 9)
                {
                    dateToUse = folderName.substring(0, 10);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Folder Name Error Processing[" + file.getAbsolutePath() + "]");
        }

        return resolveDateFound(file, dateToUse);
    }

    private static String processByFileName(File file)
    {
        String dateToUse = null;

        try
        {
            String fileName = file.getName().toUpperCase();

            if (fileName.length() > 9)
            {
                if (fileName.startsWith("VID_") ||
                    fileName.startsWith("IMG_"))
                {
                    //looking for file name VID_yyyymmdd_... assuming after VID_ is the date
                    dateToUse = fileName.substring(4, 12);
                }
                else
                {
                    //looking for file name yyyy-mm-dd... assuming first 10 characters are the date
                    dateToUse = fileName.substring(0, 10);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("File Name Error Processing[" + file.getAbsolutePath() + "]");
        }

        return resolveDateFound(file, dateToUse);
    }

    private static String resolveDateFound(File file, String dateString)
    {
        String fileRenamedTo = null;

        Date dateValue = DateParseUtil.resolveDate(dateString);

        if (dateValue == null)
        {
            if (dateString != null)
            {
                System.out.println(file.getAbsolutePath() + " -> FAILED TO PARSE DATE [" + dateString + "]");
            }
        }
        else
        {
            fileRenamedTo = moveFile(file, dateValue);
        }

        return fileRenamedTo;
    }

    private static String moveFile(File file, Date dateValue)
    {
        String fileRenamedTo = null;

        calendar.setTime(dateValue);
        int year = calendar.get(Calendar.YEAR);
        if (year > 1900)
        {
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            String folder = buildFolder(year, month, day);
            if (folder != null)
            {
                File newFile = new File(getNewName(folder, file, dateValue));

                if (file.renameTo(newFile))
                {
                    fileRenamedTo = newFile.getAbsolutePath();
                }
            }
        }

        return fileRenamedTo;
    }

    private static String buildFolder(int year, int month, int day)
    {
        String folderBuilt = null;

        File folder = new File(DIR_TO +
                               File.separator + yearFormat.format(year) +
                               File.separator + monthFormat.format(month) +
                               File.separator + dayFormat.format(day));
        if (folder.exists())
        {
            folderBuilt = folder.getAbsolutePath();
        }
        else
        {
            if (folder.mkdirs())
            {
                if (folder.exists())
                {
                    folderBuilt = folder.getAbsolutePath();
                }
            }
        }

        return folderBuilt;
    }

    private static String getNewName(String folder, File file, Date date)
    {
        String newName = null;

        int counter = 0;

        String ext = getExt(file);

        while (newName == null)
        {
            File fileName = new File(folder + File.separator +
                                     formatFileOut.format(date) + "-" + counterFormat.format(counter) + ext);

            if (!fileName.exists())
            {
                newName = fileName.getAbsolutePath();
            }
            else
            {
                counter++;
            }
        }

        return newName;
    }

    private static String getExt(File file)
    {
        String ext = null;

        String fileName = file.getName();

        int idx = fileName.lastIndexOf(".");
        if (idx > 0)
        {
            ext = fileName.substring(idx);
        }

        return ext;
    }

    private static String moveToUnknown(File file)
    {
        File unknownDir = new File(DIR_TO + File.separator + "UNKNOWN");

        if (!unknownDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            unknownDir.mkdirs();
        }

        if (unknownDir.exists())
        {
            File newFileName = new File(unknownDir.getAbsolutePath() + File.separator + file.getParentFile().getName());

            if (!newFileName.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                newFileName.mkdirs();
            }

            if (newFileName.exists())
            {
                newFileName = new File(newFileName + File.separator + file.getName());

                //make sure we don't overwrite existing files
                if (newFileName.exists())
                {
                    String newName = null;

                    String folder = newFileName.getParentFile().getName();

                    String currentFileName = newFileName.getName();

                    int extIndex = currentFileName.lastIndexOf(".");

                    currentFileName = currentFileName.substring(0, extIndex);

                    int counter = 0;

                    String ext = getExt(file);

                    while (newName == null)
                    {
                        File fileName = new File(unknownDir.getAbsolutePath() + File.separator +
                                                 folder + File.separator +
                                                 currentFileName + "-" + counterFormat.format(counter) + ext);

                        if (!fileName.exists())
                        {
                            newName = fileName.getAbsolutePath();
                        }
                        else
                        {
                            counter++;
                        }
                    }

                    newFileName = new File(newName);
                }

                if (file.renameTo(newFileName))
                {
                    return newFileName.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static void processArgs(String args[])
    {
        if (args.length > 1)
        {
            processArgPair(args[0], args[1]);
        }
        if (args.length > 3)
        {
            processArgPair(args[2], args[3]);
        }
        if (args.length > 5)
        {
            processArgPair(args[4], args[5]);
        }
    }

    private static void processArgPair(String key, String value)
    {
        if (key.equalsIgnoreCase("-f"))
        {
            DIR_FROM = new File(value);
            if (!DIR_FROM.exists() || !DIR_FROM.isDirectory())
            {
                DIR_FROM = null;
                System.err.println("Invalid From Path: " + value);
            }
            else
            {
                System.out.println("From Path Set: " + DIR_FROM.getAbsolutePath());
            }
        }
        else if (key.equalsIgnoreCase("-t"))
        {
            DIR_TO = new File(value);
            if (!DIR_TO.exists() || !DIR_TO.isDirectory())
            {
                System.err.println("Invalid To Path: " + value);
                DIR_TO = null;
            }
            else
            {
                System.out.println("  To Path Set: " + DIR_TO.getAbsolutePath());
            }
        }
    }

    private static boolean directoryHasChanged(File dir)
    {

        StringBuilder content = new StringBuilder();

        buildDirectoryContent(dir, content);

        String currentMD5 = hashString(content.toString(), "MD5");

//        System.out.println("   Last MD5: " + md5);
//        System.out.println("Current MD5: " + currentMD5);
        if (md5.equals(currentMD5))
        {
            return false;
        }
        else
        {
            md5 = currentMD5;
            return true;
        }
    }

    private static void buildDirectoryContent(File directory, StringBuilder content)
    {
        content.append("D-").append(directory.getName()).append("[").append(directory.length()).append("]");

        File fileList[] = directory.listFiles();
        if (fileList != null)
        {
            Arrays.sort(fileList);

            for (File file : fileList)
            {
                if (file.isDirectory())
                {
                    buildDirectoryContent(file, content);
                }
                else
                {
                    content.append("F-").append(file.getName()).append("[").append(file.length()).append("]");
                }
            }
        }
    }

    private static String hashString(String message, String algorithm)
    {
        String hashValue = String.valueOf(System.currentTimeMillis());
        try
        {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return hashValue;
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes)
    {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes)
        {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuffer.toString();
    }

    public static void main(String[] args)
    {
        System.out.println("Starting MediaOrg...");
        processArgs(args);

        if (DIR_FROM == null || DIR_TO == null)
        {
            System.out.println("Usage: java MediaOrg -f \\\\path\\from -t \\\\path\\to -d 10m\n" +
                               "Monitor a -f directory and move media to -t directory.\n\n" +
                               "-f  From directory. The directory to monitor.\n" +
                               "-t  To directory. The directory to copy media to.\n");
            System.exit(0);
        }

        System.out.println("Supported date formats:");
        DateParseUtil.printSupportedFormats();

        //exifTool = new ExifTool(ExifTool.Feature.STAY_OPEN);
        System.out.println("ExifTool ready.");

        System.out.println("Waiting for directory to stop changing.");
        //wait until directory is not changing (Files or folders are not being written to it.
        while (directoryHasChanged(DIR_FROM))
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                System.err.println("Error in sleep thread.");
            }
            System.out.print(".");
        }
        System.out.println();
        processDir(DIR_FROM);
    }
}
