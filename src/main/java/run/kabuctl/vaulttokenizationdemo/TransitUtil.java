package run.kabuctl.vaulttokenizationdemo;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransitUtil {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Value("${VAULT_TOKEN_TRANS}")
    private String token;

    public String encrypt(String plaintext) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", token);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("plaintext", Base64.getEncoder().encodeToString(plaintext.getBytes()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transit/encrypt/my-key", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("ciphertext");
    }

    public String decrypt(String ciphertext) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", token);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("ciphertext", ciphertext);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transit/decrypt/my-key", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("plaintext");
    }


}
