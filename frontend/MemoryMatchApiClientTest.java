public class MemoryMatchApiClientTest {

    public static void main(String[] args) {
        MemoryMatchApiClient client = new MemoryMatchApiClient();

        try {
            String players = client.getPlayers();
            System.out.println("Backend connection successful!");
            System.out.println("Players: " + players);
        } catch (Exception e) {
            System.out.println("Backend connection failed:");
            e.printStackTrace();
        }
    }
}
