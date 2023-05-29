package com.kxt29.hmac;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Controller
@RequestMapping("/client")
public class ClientController {

    private static final String SERVER_URL = "http://localhost:8081/server/endpoint";
    private static final String SHARED_SECRET = "mySecretKey";

    @GetMapping("/sendRequest")
    @ResponseBody
    public String sendRequest() throws URISyntaxException, NoSuchAlgorithmException, InvalidKeyException {
        // Create the request with the required parameters
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String request = "This is the request body";

        // Generate the HMAC
        String hmac = generateHmac(request, timestamp);

        // Create the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Timestamp", timestamp);
        headers.add("Authorization", hmac);

        // Send the request to the server
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, new URI(SERVER_URL));
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

        return "Response from server: " + response.getBody();
    }

    private String generateHmac(String request, String timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(SHARED_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);

        byte[] hmacBytes = mac.doFinal((request + timestamp).getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hmacBytes);
    }
}
