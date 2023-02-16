import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static int
            numberOfTexts = 100_000,
            lengthOfTexts = 10_000,
            maxNumberOfLines = 100;
    public static ArrayBlockingQueue<String>
            textsA = new ArrayBlockingQueue<>(maxNumberOfLines),
            textsB = new ArrayBlockingQueue<>(maxNumberOfLines),
            textsC = new ArrayBlockingQueue<>(maxNumberOfLines);
    public static AtomicInteger
            countTextsA = new AtomicInteger(0),
            countTextsB = new AtomicInteger(0),
            countTextsC = new AtomicInteger(0);

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static AtomicInteger maxNumberOfCharacters(ArrayBlockingQueue<String> blockingDeque, char symbol) {
        AtomicInteger result = new AtomicInteger(0);
        for (int i = 0; i < lengthOfTexts; i++) {
            try {
                AtomicInteger valueSymbol = new AtomicInteger(0);
                String text = blockingDeque.take();
                for (int j = 0; j < text.length(); j++) {
                    if (text.charAt(j) == symbol) {
                        valueSymbol.getAndIncrement();
                    }
                }
                if (valueSymbol.get() > result.get()) {
                    result = valueSymbol;
                }
            } catch (InterruptedException e) {
                return result;
            }
        }
        return result;
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            for (int i = 0; i < lengthOfTexts; i++) {
                String text = generateText("abc", numberOfTexts);
                try {
                    textsA.put(text);
                    textsB.put(text);
                    textsC.put(text);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        Thread threadA = new Thread(() -> countTextsA = maxNumberOfCharacters(textsA, 'a'));
        Thread threadB = new Thread(() -> countTextsB = maxNumberOfCharacters(textsB, 'b'));
        Thread threadC = new Thread(() -> countTextsC = maxNumberOfCharacters(textsC, 'c'));

        threadA.start();
        threadB.start();
        threadC.start();

        threadA.join();
        threadB.join();
        threadC.join();

        System.out.printf("""
                "Самое максимальное количество символов 'a' = %s"
                "Самое максимальное количество символов 'b' = %s"
                "Самое максимальное количество символов 'c' = %s"
                """, countTextsA.get(), countTextsB.get(), countTextsC.get());
    }
}
