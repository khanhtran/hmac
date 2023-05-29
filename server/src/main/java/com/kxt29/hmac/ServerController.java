package com.kxt29.hmac;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
@Controller
@RequestMapping("/server")
public class ServerController {

    private static final String SHARED_SECRET = "mySecretKey";

    @GetMapping("/endpoint")
    @ResponseBody
    public String handleRequest(HttpHeaders headers) throws NoSuchAlgorithmException, InvalidKeyException {
        // Extract the request headers
        String timestamp = headers.getFirst("X-Timestamp");
        String authorization = headers.getFirst("Authorization");

        // Generate the HMAC for the received request
        String hmac = generateHmac("This is the request body", timestamp);

        // Compare the received HMAC with the calculated HMAC
        boolean isAuthentic = hmac.equals(authorization);

        if (isAuthentic) {
            return "Request authenticated successfully";
        } else {
            return "Authentication failed";
        }
    }

    private String generateHmac(String request, String timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(SHARED_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);

        byte[] hmacBytes = mac.doFinal((request + timestamp).getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hmacBytes);
    }
}
