package gov.cms.esmd.utility;


import gov.cms.esmd.rc.api.client.NotificationApiClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class NotificationUtility {
    private static final Logger log = LoggerFactory.getLogger(NotificationUtility.class);
    public static Optional<String> checkNullEmptyString(String str) {
        return Optional.ofNullable(str).filter(s -> s.trim().length() > 0 && !s.isEmpty());
    }
    public static Date convertDateToString(Date date, String format) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            String formattedDate = dateFormat.format(date);

            String milliSecs = formattedDate.substring(
                    formattedDate.lastIndexOf(".") + 1,
                    formattedDate.lastIndexOf("-"));
            String newMillisecs = StringUtils.rightPad(milliSecs.substring(4, 7), 7, "0");

            String adjustedDateStr = StringUtils.replace(formattedDate, milliSecs, newMillisecs);

            return dateFormat.parse(adjustedDateStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}
