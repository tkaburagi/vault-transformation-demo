package run.kabuctl.vaulttokenizationdemo;

import org.json.JSONObject;
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

    public String transform(String value, String transformationName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Vault-Token", "s.zTBJe2IgA033w7tPVmQVOYgz");

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("transformation", transformationName);
        input.put("value", value);

        System.out.println(input.toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = this.restTemplate().postForEntity("http://127.0.0.1:8200/v1/transform/encode/payments", entity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getString("encoded_value");
    }


}
