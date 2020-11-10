import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        Scanner scanner = new Scanner(new File(ClassLoader.getSystemResource("chat.txt").toURI()));
        scanner.useDelimiter("(?m)(?=^\\d{1,2}/\\d{1,2}/\\d{1,2}, \\d{2}:\\d{2} - (\\S*: )?.+)");

        while (scanner.hasNext()) {
            MessageUtils.parse(scanner.next())
                .ifPresent(MessageUtils::addMessage);
        }

        MessageUtils.displayNumberOfMessagesByAuthor();
        MessageUtils.writeMessagesToJSONFile();
    }
}
