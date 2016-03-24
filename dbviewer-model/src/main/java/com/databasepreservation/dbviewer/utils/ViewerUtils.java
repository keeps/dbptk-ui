package com.databasepreservation.dbviewer.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.databasepreservation.dbviewer.ViewerConstants;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerUtils {
  public static Date parseDate(String date) throws ParseException {
    Date ret;
    if (date != null) {
      SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ViewerConstants.ISO8601);
      ret = iso8601DateFormat.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }
}
