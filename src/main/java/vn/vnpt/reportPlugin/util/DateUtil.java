package vn.vnpt.reportPlugin.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getJsDateFormat(
            ApplicationProperties applicationProperties) {
        return applicationProperties
                .getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVASCRIPT_FORMAT);
    }

    public static Date convertStringtoDate(String dateFormat, String input) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            return new Date(format.parse(input).getTime());
        } catch (Exception e) {
            return null;
        }
    }
}
