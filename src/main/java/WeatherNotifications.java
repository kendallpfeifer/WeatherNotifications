import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.weather.Weather;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.time.LocalTime;
import java.text.*;
import java.util.*;


public class WeatherNotifications {
    public static final String ACCOUNT_SID = "ACd52baed8b7aa3964748b11a1c2476305";
    public static final String AUTH_TOKEN = "64e0fde8442f7a0b0447899527e67b97";
    public static final String API_TOKEN = "0ec1b697947476413186e8044c15a12f";

    public static void main(String[] args) throws ParseException {
        new Foo().sendMessage();
    }

    private static class Foo {
        public int getDiffTime(int m1, int h1, int m2, int h2) {
            int mins = 0;
            int hours = 0;

            if (h1 > h2) {
                hours = h1-h2;
                if (m1 > m2)
                    mins = m1-m2;
                else
                    mins = m2-m1;
                return (hours * 3600000 + mins * 60000);
            } else if (h1 == h2) {
                if (m1 > m2) {
                    mins = m1-m2;
                } else {
                    hours = 24;
                    mins = m2-m1;
                }
                return (hours * 3600000 + mins * 60000);
            } else {
                if (m1 > m2)
                    mins = m1-m2;
                else
                    mins = m2 - m1;
                hours = 24 - Math.abs(m1 - m2);
                return (hours * 3600000 + mins * 60000);
            }
        }



        public synchronized void sendMessage() throws ParseException {

            OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(API_TOKEN);
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            final Weather weather = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCityName("College Park")
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.IMPERIAL)
                    .retrieve()
                    .asJava();

            System.out.println(weather);
            StringBuilder sb = new StringBuilder();
            Double mintemp = weather.getTemperature().getMinTemperature();
            Double maxtemp = weather.getTemperature().getMaxTemperature();
            Double feelslike = weather.getTemperature().getFeelsLike();

            sb.append("Today the min is ");
            sb.append(mintemp);
            sb.append(" °F, the max is ");
            sb.append(maxtemp);
            sb.append(" °F, it feels like ");
            sb.append(feelslike);
            sb.append(" °F. \n");

            if (weather.getRain() != null && weather.getRain().getOneHourLevel() > 0) {
                sb.append("It is raining\n");
            }
            if (weather.getSnow() != null && weather.getSnow().getOneHourLevel() > 0) {
                sb.append("It is snowing\n");
            }
            if (weather.getWind() != null && weather.getWind().getSpeed() > 5) {
                sb.append("It is windy, with ");
                sb.append(weather.getWind().getSpeed());
                sb.append(" MPH winds.\n");
            }

            System.out.println(sb.toString());

            LocalTime goalTime = LocalTime.of(14, 55, 0, 0);
            LocalTime currTime = LocalTime.now();
            int diffTime = getDiffTime(goalTime.getMinute(), goalTime.getHour(),
                    currTime.getMinute(), currTime.getHour());
            System.out.println(diffTime);

            while (currTime != goalTime) {
                try {
                    wait(diffTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread Interrupted");
                }
            }

            Message message = Message.creator(
                    new PhoneNumber("+14437097617"),
                    new PhoneNumber("+19034857118"),
                    sb.toString())
                    .create();
        }
    }
}
