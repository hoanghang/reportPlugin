package vn.vnpt.reportPlugin.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

public class DateUtil {

    public static String getJsDateFormat(
            ApplicationProperties applicationProperties) {
        return applicationProperties
                .getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVASCRIPT_FORMAT);
    }
}
