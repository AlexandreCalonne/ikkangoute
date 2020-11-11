import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {

    public static final String DATE_REGEX = "^\\d{1,2}/\\d{1,2}/\\d{1,2}, \\d{2}:\\d{2}";
    public static final String AUTHOR_REGEX = "([^:]+)";

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
        Pattern authorPattern = Pattern.compile(DATE_REGEX + " - " + AUTHOR_REGEX + ": ");
        Matcher authorMatcher = authorPattern.matcher(messageString);

        return authorMatcher.find() ? authorMatcher.group(1) : "unknown";
    }

    private static LocalDateTime retrieveSentDate(String messageString) {
        Pattern sentDatePattern = Pattern.compile(DATE_REGEX);
        Matcher sentDateMatcher = sentDatePattern.matcher(messageString);

        DateTimeFormatter sentDateFormatter = DateTimeFormatter.ofPattern("M/d/yy, HH:mm");

        return sentDateMatcher.find()
            ? LocalDateTime.parse(sentDateMatcher.group(), sentDateFormatter)
            : LocalDateTime.MIN;
    }

    private static String retrieveBody(String messageString) {
        String body = messageString.replaceFirst(DATE_REGEX + " - " + AUTHOR_REGEX + ": ", "");
        if (body.endsWith("\n")) {
            body = body.substring(0, body.length() - 1);
        }

        return body;
    }

    public static Map<String, Long> numberOfMessagesByAuthor() {
        return messages.stream()
            .collect(Collectors.groupingBy(Message::getAuthor, Collectors.counting()));
    }

    public static Map<String, Double> averageMessageLengthByAuthor() {
        return messages.stream()
            .map(message -> Map.entry(message.getAuthor(), message.getBody().length()))
            .collect(Collectors.groupingBy(Entry::getKey, Collectors.averagingInt(Entry::getValue)));
    }

    public static Map<String, Double> averageNumberOfMessagesByDay() {
        return messages.stream()
            .collect(Collectors.groupingBy(message -> message.getSentDate().toLocalDate(), Collectors.counting()))
            .entrySet()
            .stream()
            .collect(Collectors.groupingBy(day -> day.getKey().getDayOfWeek().name(), Collectors.averagingLong(Entry::getValue)));
    }

    public static Entry<LocalDate, Long> dayWithMostMessages() {
        return messages.stream()
            .collect(Collectors.groupingBy(message -> message.getSentDate().toLocalDate(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Entry.comparingByValue())
            .orElse(Map.entry(LocalDate.MIN, 0L));
    }

    public static void writeMessagesToJSONFile() throws IOException {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        File file = new File("target/chat.json");

        writer.writeValue(file, messages);
    }
}
