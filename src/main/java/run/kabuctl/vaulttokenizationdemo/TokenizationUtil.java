package run.kabuctl.vaulttokenizationdemo;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TokenizationUtil {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Value("${VAULT_TOKEN_TRANS}")
    private String token;

    public String encode(String value, String transformationName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", token);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("transformation", transformationName);
        input.put("value", value);

        System.out.println(input.toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transform/encode/payments", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("encoded_value");
    }


    public String decode(String value, String transformationName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", token);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("transformation", transformationName);
        input.put("value", value);

        System.out.println(input.toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transform/decode/payments", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("decoded_value");
    }

    public String masking(String value, String transformationName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", token);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("transformation", transformationName);
        input.put("value", value);

        System.out.println(input.toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transform/encode/payments-masking", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("encoded_value");
    }

}
