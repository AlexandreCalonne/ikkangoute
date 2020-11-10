import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {

    private static List<Message> messages = new ArrayList<>();

    public static void addMessage(Message message) {
        messages.add(message);
    }

    public static Optional<Message> parse(String messageString) {
        return Optional.of(Message.builder()
            .author(retrieveAuthor(messageString))
            .sentDate(retrieveSentDate(messageString))
            .body(retrieveBody(messageString))
            .build()
        );
    }

    private static String retrieveAuthor(String messageString) {
        Pattern authorPattern = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{1,2}, \\d{2}:\\d{2} - ([^:]+): ");
        Matcher authorMatcher = authorPattern.matcher(messageString);

        return authorMatcher.find() ? authorMatcher.group(1) : "unknown";
    }

    private static LocalDateTime retrieveSentDate(String messageString) {
        Pattern sentDatePattern = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{1,2}, \\d{2}:\\d{2}");
        Matcher sentDateMatcher = sentDatePattern.matcher(messageString);

        DateTimeFormatter sentDateFormatter = DateTimeFormatter.ofPattern("M/d/yy, HH:mm");

        return sentDateMatcher.find()
            ? LocalDateTime.parse(sentDateMatcher.group(), sentDateFormatter)
            : LocalDateTime.ofInstant(Instant.ofEpochSecond(0), TimeZone.getDefault().toZoneId());
    }

    private static String retrieveBody(String messageString) {
        String body = messageString.replaceFirst("\\d{1,2}/\\d{1,2}/\\d{1,2}, \\d{2}:\\d{2} - (\\S*: )?", "");
        if (body.endsWith("\n")) {
            body = body.substring(0, body.length() - 1);
        }

        return body.replace("\n", "");
    }

    public static void displayNumberOfMessagesByAuthor() {
        messages.stream()
            .collect(Collectors.groupingBy(Message::getAuthor, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(System.out::println);
    }

    public static void writeMessagesToJSONFile() throws IOException {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        File file = new File("target/chat.json");

        writer.writeValue(file, messages);
    }
}
