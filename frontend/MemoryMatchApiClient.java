
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MemoryMatchApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient;

    public MemoryMatchApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public String getPlayers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/players"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Backend returned HTTP " + response.statusCode()
            );
        }

        return response.body();
    }

    public String registerPlayer(String playerName)
            throws IOException, InterruptedException {

        String escapedName = playerName
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String jsonBody = "{\"name\":\"" + escapedName + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/players"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200
                && response.statusCode() != 201) {
            throw new IOException(
                    "Player registration failed with HTTP "
                            + response.statusCode()
                            + ": " + response.body()
            );
        }

        return response.body();
    }

    public String saveGameResult(
            int playerId,
            int score,
            int moves,
            boolean completed)
            throws IOException, InterruptedException {
        String jsonBody = "{"
                + "\"playerId\":" + playerId + ","
                + "\"score\":" + score + ","
                + "\"moves\":" + moves + ","
                + "\"completed\":" + completed
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/game-results"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200
                && response.statusCode() != 201) {
            throw new IOException(
                    "Saving game result failed with HTTP "
                            + response.statusCode()
                            + ": " + response.body()
            );
        }

        return response.body();
    }

    public String getPlayerResults(int playerId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        BASE_URL + "/api/game-results/player/" + playerId
                ))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Could not retrieve game history. HTTP "
                            + response.statusCode()
            );
        }

        return response.body();
    }

    
    public String getPlayerResultsForName(String playerName)
            throws IOException, InterruptedException {

        String playersJson = getPlayers();
        StringBuilder combinedResults = new StringBuilder();

        java.util.regex.Matcher objects =
                java.util.regex.Pattern.compile("\\{[^{}]*\\}")
                        .matcher(playersJson);

        while (objects.find()) {
            String player = objects.group();

            java.util.regex.Matcher nameMatcher =
                    java.util.regex.Pattern
                            .compile("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                            .matcher(player);

            java.util.regex.Matcher idMatcher =
                    java.util.regex.Pattern
                            .compile("\"id\"\\s*:\\s*(\\d+)")
                            .matcher(player);

            if (nameMatcher.find()
                    && idMatcher.find()
                    && nameMatcher.group(1).equals(playerName)) {

                int matchingPlayerId =
                        Integer.parseInt(idMatcher.group(1));

                String results =
                        getPlayerResults(matchingPlayerId).trim();

                if (results.length() > 2) {
                    if (combinedResults.length() > 0) {
                        combinedResults.append(",");
                    }

                    combinedResults.append(
                            results.substring(1, results.length() - 1)
                    );
                }
            }
        }

        return "[" + combinedResults + "]";
    }


}
