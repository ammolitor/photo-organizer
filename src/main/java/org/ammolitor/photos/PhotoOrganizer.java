package org.ammolitor.photos;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.log4j.BasicConfigurator;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;

/**
 * Moves JPEGs from their source directory to a directory based on their
 * EXIF creation dates.
 *
 * @author <a href="mailto:evanhoffman@evanhoffman.com">Evan Hoffman</a>
 * @since 2007-03-28
 */
public class PhotoOrganizer extends FileOrganizer {

    private static final String[] jpegExtensions = {".JPG", ".jpg", ".jpeg", ".jpe"};

    private static final DateFormat df = new SimpleDateFormat("yyyy/yyyy-MM");
    private static final DateFormat dfPrefix = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");

    private final File targetDir;

    private PhotoOrganizer(File sourceDir, File targetDir, boolean recurse) {
        super(sourceDir, recurse);
        this.targetDir = targetDir;
    }

    /**
     * @see <a href="http://www.drewnoakes.com/code/exif/sampleUsage.html">http://www.drewnoakes.com/code/exif/sampleUsage.html</a>
     */
    @Override
    protected File getTargetDirForFile(File f) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            for (Directory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {

                Date date = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME)) {
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
                }
                Date dateDigitized = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED)) {
                    dateDigitized = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                }
                Date dateOriginal = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                    dateOriginal = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                }

                String path = getPathFromDate(date, dateDigitized, dateOriginal);
                if (path != null) {
                    return new File(targetDir, path);
                } else {
                    logger.error("NOT HERE!!");

                }
            }
        } catch (Exception e) {
            logger.error("Error processing file " + f + ": " + e.getMessage(), e);
            return null;
        }
        return null;
    }

    @Override
    protected String getPrefixForFile(File f) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            for (Directory directory : metadata.getDirectories()) {
                Date date = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME)) {
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
                }
                Date dateDigitized = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED)) {
                    dateDigitized = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                }
                Date dateOriginal = null;
                if (directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                    dateOriginal = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                }
                if (date != null) {
                    return dfPrefix.format(date);
                }
                if (dateDigitized != null) {
                    return dfPrefix.format(dateDigitized);
                }
                if (dateOriginal != null) {
                    return dfPrefix.format(dateOriginal);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing file " + f + ": " + e.getMessage(), e);
            return null;
        }
        return null;
    }

    private static String getPathFromDate(Date d1, Date d2, Date d3) {
        if (d1 != null) {
            return df.format(d1);
        }
        if (d2 != null) {
            return df.format(d2);
        }
        if (d3 != null) {
            return df.format(d3);
        }
        return null;
//		throw new NullPointerException("All 3 dates were null");
    }

    @Override
    protected boolean accept(File f) {
        for (String ext : jpegExtensions) {
            if (f.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String args[]) {
        BasicConfigurator.configure();
        int sourceDir = 0, targetDir = 1;
        PhotoOrganizer jo = new PhotoOrganizer(new File(args[sourceDir]), new File(args[targetDir]), true);
        jo.run();

    }
}
