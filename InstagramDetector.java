import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstagramDetector {
    private static final String INSTAGRAM_URL = "https://www.instagram.com";
    private static final String RESET_PASSWORD_PATH = "/accounts/password/reset/";
    private static final String API_PATH = "/api/v1/web/accounts/account_recovery_send_ajax/";

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Email: ");
        String email = reader.readLine();
        detect(email);
    }

    public static void detect(String email) throws IOException {
        URL url = new URL(INSTAGRAM_URL + RESET_PASSWORD_PATH);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        Pattern pattern = Pattern.compile("\"csrf_token\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response.toString());
        String csrf = "";
        if (matcher.find()) {
            csrf = matcher.group(1);
        }

        String postData = "email_or_username=" + email;

        String csrfHeader = "X-CSRFToken";
        String csrfHeaderValue = csrf;
        String refererHeader = "Referer";
        String refererHeaderValue = INSTAGRAM_URL + RESET_PASSWORD_PATH;
        String contentTypeHeader = "Content-Type";
        String contentTypeHeaderValue = "application/x-www-form-urlencoded";

        URL apiUrl = new URL(INSTAGRAM_URL + API_PATH);
        HttpURLConnection apiConnection = (HttpURLConnection) apiUrl.openConnection();
        apiConnection.setRequestMethod("POST");
        apiConnection.setRequestProperty(csrfHeader, csrfHeaderValue);
        apiConnection.setRequestProperty(refererHeader, refererHeaderValue);
        apiConnection.setRequestProperty(contentTypeHeader, contentTypeHeaderValue);
        apiConnection.setDoOutput(true);

        apiConnection.getOutputStream().write(postData.getBytes());

        StringBuilder apiResponse = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                apiResponse.append(line);
            }
        }

        if (apiResponse.toString().contains("Nenhum usuário encontrado")) {
            System.out.println("Email não cadastrado");
        } else {
            System.out.println("Email cadastrado");
        }
    }
}
