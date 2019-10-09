package com.databasepreservation.visualization.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Iterator;

import com.databasepreservation.common.utils.ViewerUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Test(groups = {"unit"})
public class ViewerUtilsTest {

  @DataProvider
  public Iterator<Object[]> testJodaDateTimeConversionsProvider() {
    ArrayList<Object[]> tests = new ArrayList<Object[]>();

    tests.add(testJodaDateTimeConversionsProviderInstantiate1("-08:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("-04:30"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("-04:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("-02:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("-01:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+01:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+02:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+02:30"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+04:00"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+04:30"));
    tests.add(testJodaDateTimeConversionsProviderInstantiate1("+08:00"));

    return tests.iterator();
  }

  private Object[] testJodaDateTimeConversionsProviderInstantiate1(String tzStr) {
    return testJodaDateTimeConversionsProviderInstantiate(2000, 1, 1, 2, 30, 59, 999, tzStr);
  }

  private Object[] testJodaDateTimeConversionsProviderInstantiate(int year, int monthOfYear, int dayOfMonth,
    int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond, String tzStr) {
    DateTimeZone tz = DateTimeZone.forID(tzStr);
    DateTime dt = new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond,
      tz);
    String str = String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%s", year, monthOfYear, dayOfMonth, hourOfDay,
      minuteOfHour, secondOfMinute, millisOfSecond, tzStr);
    return new Object[] {dt, tz, str};
  }

  @Test(description = "Joda DateTime to and from string with offset 0", dataProvider = "testJodaDateTimeConversionsProvider", groups = {
    "unit"})
  public void testJodaDateTimeConversions(Object... testData) {
    DateTime dt = (DateTime) testData[0];
    DateTimeZone tz = (DateTimeZone) testData[1];
    String str = (String) testData[2];

    assertThat("got bad timezone with getZone()", dt.getZone(), equalTo(tz));

    String dt2str = ViewerUtils.jodaDateTimeToString(dt);
    assertThat("dt to string not working", dt2str, equalTo(str));

    DateTime dt2str2dt = ViewerUtils.parseJodaDateTime(dt2str);
    assertThat("dt to str to dt not working", dt2str2dt, equalTo(dt));

    String dt2str2dt2str = ViewerUtils.jodaDateTimeToString(dt2str2dt);
    assertThat("dt to str to dt to str not working", dt2str2dt2str, equalTo(str));
  }

  @Test(description = "Joda DateTime to and from string (with differences)", groups = {"unit"})
  public void testJodaDateTimeConversionsWithDifferences() {
    Object[] objects = testJodaDateTimeConversionsProviderInstantiate1("UTC");
    DateTime dtZ = (DateTime) objects[0];
    DateTime dtPlusZero = (DateTime) testJodaDateTimeConversionsProviderInstantiate1("+00:00")[0];
    DateTime dtMinusZero = (DateTime) testJodaDateTimeConversionsProviderInstantiate1("-00:00")[0];
    DateTimeZone tz = (DateTimeZone) objects[1];
    String str = (String) objects[2];
    str = str.replace("UTC", "Z");

    // test Z (UTC)

    assertThat("got bad timezone with getZone()", dtZ.getZone(), equalTo(tz));

    String dt2str = ViewerUtils.jodaDateTimeToString(dtZ);
    assertThat("dt to string not working", dt2str, equalTo(str));

    DateTime dt2str2dt = ViewerUtils.parseJodaDateTime(dt2str);
    assertThat("dt to str to dt not working", dt2str2dt, equalTo(dtZ));

    String dt2str2dt2str = ViewerUtils.jodaDateTimeToString(dt2str2dt);
    assertThat("dt to str to dt to str not working", dt2str2dt2str, equalTo(str));

    // test +00:00

    assertThat("got bad timezone with getZone()", dtPlusZero.getZone(), equalTo(tz));

    dt2str = ViewerUtils.jodaDateTimeToString(dtPlusZero);
    assertThat("dt to string not working", dt2str, equalTo(str));

    dt2str2dt = ViewerUtils.parseJodaDateTime(dt2str);
    assertThat("dt to str to dt not working", dt2str2dt, equalTo(dtPlusZero));

    dt2str2dt2str = ViewerUtils.jodaDateTimeToString(dt2str2dt);
    assertThat("dt to str to dt to str not working", dt2str2dt2str, equalTo(str));

    // test -00:00

    assertThat("got bad timezone with getZone()", dtMinusZero.getZone(), equalTo(tz));

    dt2str = ViewerUtils.jodaDateTimeToString(dtMinusZero);
    assertThat("dt to string not working", dt2str, equalTo(str));

    dt2str2dt = ViewerUtils.parseJodaDateTime(dt2str);
    assertThat("dt to str to dt not working", dt2str2dt, equalTo(dtMinusZero));

    dt2str2dt2str = ViewerUtils.jodaDateTimeToString(dt2str2dt);
    assertThat("dt to str to dt to str not working", dt2str2dt2str, equalTo(str));
  }
}
