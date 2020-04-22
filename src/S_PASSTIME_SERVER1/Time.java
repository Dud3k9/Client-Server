/**
 *
 *  @author Petrykowski Maciej S19267
 *
 */

package S_PASSTIME_SERVER1;


import java.time.*;

import java.time.format.TextStyle;
import java.util.Locale;

import static java.time.temporal.ChronoUnit.*;


public class Time {
    public static String passed(String in, String in1) {
        StringBuilder sb = new StringBuilder();
        try {
            LocalDateTime dateTime = null;
            LocalDateTime dateTime1 = null;
            LocalDate date;
            LocalDate date1;
            LocalTime time = null;
            LocalTime time1 = null;
            boolean hour;

            Locale locale = new Locale("PL", "");

            ZonedDateTime zonedDateTime = null, zonedDateTime1 = null;

            if (in.length() > 10) {
                dateTime = LocalDateTime.parse(in);
                dateTime1 = LocalDateTime.parse(in1);
                zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Warsaw"));
                zonedDateTime1 = dateTime1.atZone(ZoneId.of("Europe/Warsaw"));

                date = dateTime.toLocalDate();
                date1 = dateTime1.toLocalDate();
                time = dateTime.toLocalTime();
                time1 = dateTime1.toLocalTime();
                hour = true;
            } else {
                date = LocalDate.parse(in);
                date1 = LocalDate.parse(in1);
                hour = false;
            }
            sb.append("Od ");
            sb.append(date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, locale) + " " + date.getYear() + " ");
            sb.append("(" + date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale) + ")");
            if (hour)
                sb.append(" godz. " + time.toString());

            sb.append(" do ");
            sb.append(date1.getDayOfMonth() + " " + date1.getMonth().getDisplayName(TextStyle.FULL, locale) + " " + date1.getYear() + " ");
            sb.append("(" + date1.getDayOfWeek().getDisplayName(TextStyle.FULL, locale) + ")");
            if (hour)
                sb.append(" godz. "+time1.toString());

            sb.append("\n - mija: ");
            long daysBetween = DAYS.between(date, date1);
            float weeksBetween = (float) daysBetween / 7 * 100;
            weeksBetween = Math.round(weeksBetween);
            weeksBetween = weeksBetween / 100;
            sb.append(daysBetween);
            if (daysBetween == 1)
                sb.append(" dzień");
            else
                sb.append(" dni");
            sb.append(", tygodni " + weeksBetween);

            if (hour) {
                sb.append("\n - godzin: " + HOURS.between(zonedDateTime, zonedDateTime1));
                sb.append(", minut: " + MINUTES.between(zonedDateTime, zonedDateTime1));
            }

            if (daysBetween >= 1) {
                sb.append("\n - kalendarzowo: ");
                long yearsBetween = YEARS.between(date, date1);
                if (yearsBetween > 0) {
                    sb.append(yearsBetween);
                    if (yearsBetween == 1)
                        sb.append(" rok");
                    else if (yearsBetween > 1 && yearsBetween < 5)
                        sb.append(" lata");
                    else
                        sb.append(" lat");
                    date = date.plusYears(yearsBetween);
                }
                long mounthBetween = MONTHS.between(date, date1);
                if (mounthBetween > 0) {
                    if (yearsBetween > 0)
                        sb.append(", " + mounthBetween);
                    else
                        sb.append(mounthBetween);
                    if (mounthBetween == 1)
                        sb.append(" miesiąc");
                    else if (mounthBetween > 1 && mounthBetween < 5)
                        sb.append(" miesiące");
                    else
                        sb.append(" miesięcy");
                    date = date.plusMonths(mounthBetween);
                }
                daysBetween = DAYS.between(date, date1);
                if (daysBetween > 0) {
                    if (mounthBetween > 0 || yearsBetween > 0)
                        sb.append(", " + daysBetween);
                    else
                        sb.append(daysBetween);
                    if (daysBetween == 1)
                        sb.append(" dzień");
                    else
                        sb.append(" dni");
                }
            }

        } catch (Exception e) {
            sb.append("*** " + e);
        }
        return sb.toString();
    }
}
