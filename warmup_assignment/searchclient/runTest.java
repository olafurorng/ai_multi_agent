import java.io.IOException;

public class runTest {

    public static void main(String[] args) throws IOException{
        // Prints "Hello, World" to the terminal window.
        // System.out.println("Hello, World");

        // Run a java app in a separate system process
        // Runtime.getRuntime().exec("java -jar server.jar -l levels/SAD1.lvl -c 'java searchclient.SearchClient' -g 50 -t 300");
       
        try {
            Runtime.getRuntime().exec("java -jar server.jar -l levels/SAD1.lvl -c java searchclient.SearchClient -g 50 -t 300");
            System.out.println("Hello, World");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}