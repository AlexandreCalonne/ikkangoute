import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        Scanner scanner = new Scanner(new File(ClassLoader.getSystemResource("chat.txt").toURI()));
        scanner.useDelimiter("(?m)(?=^" + MessageUtils.DATE_REGEX + " - " + MessageUtils.AUTHOR_REGEX + ": .+$)");

        while (scanner.hasNext()) {
            MessageUtils.parse(scanner.next())
                .ifPresent(MessageUtils::addMessage);
        }

        System.out.println(MessageUtils.numberOfMessagesByAuthor());
        System.out.println(MessageUtils.averageMessageLengthByAuthor());
        MessageUtils.writeMessagesToJSONFile();
    }
}
