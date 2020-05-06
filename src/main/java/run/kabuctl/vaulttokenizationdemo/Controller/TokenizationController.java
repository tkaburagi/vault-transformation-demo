package run.kabuctl.vaulttokenizationdemo.Controller;

import org.springframework.web.bind.annotation.*;
import run.kabuctl.vaulttokenizationdemo.ApiAppUtil;
import run.kabuctl.vaulttokenizationdemo.Entity.User;
import run.kabuctl.vaulttokenizationdemo.Repository.UserJpaRepository;
import run.kabuctl.vaulttokenizationdemo.TokenizationUtil;
import run.kabuctl.vaulttokenizationdemo.TransitUtil;

import java.util.*;

@RestController
public class TokenizationController {

    private User u = new User();
    private final UserJpaRepository userJpaRepository;

    public TokenizationController(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @GetMapping(value = "/api/v1/get-all-users")
    public Object getAllUsers() {
        return userJpaRepository.findAll();
    }

    @GetMapping(value = "/api/v1/get-encrypted-users")
    public Object getEncryptedUsers() {
        return userJpaRepository.findUsersByFlag("transit");
    }

    @GetMapping(value = "/api/v1/get-transformed-users")
    public Object getTransformedUsers() {
        return userJpaRepository.findUsersByFlag("transformation");
    }

    @GetMapping(value = "/api/v1/get-simple-transformed-users")
    public Object getSimpleTransformedUsers() {
        return userJpaRepository.findUsersByFlag("simple-transformation");
    }

    @GetMapping(value = "/api/v1/get-simplest-transformed-users")
    public Object getSimplestTransformedUsers() {
        return userJpaRepository.findUsersByFlag("simplest-transformation");
    }

    @PostMapping(value = "/api/v1/transit/add-user")
    public Object addOneEncryptedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        u = new ApiAppUtil().userSetup(
                username,
                transitUtil.encrypt(password),
                transitUtil.encrypt(email),
                transitUtil.encrypt(creditcard),
                "transit"
        );
        return userJpaRepository.save(u);
    }

    @PostMapping(value = "/api/v1/transformation/add-user")
    public Object addOneTransformedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        TokenizationUtil tokenizationUtil = new TokenizationUtil();
        u = new ApiAppUtil().userSetup(
                username,
                transitUtil.encrypt(password),
                tokenizationUtil.encode(email, "email"),
                tokenizationUtil.encode(creditcard, "creditcard-symbolnumericalpha"),
                "transformation"
        );
        return userJpaRepository.save(u);
    }

    @PostMapping(value = "/api/v1/simple-transformation/add-user")
    public Object addOneSimpleTransformedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        TokenizationUtil tokenizationUtil = new TokenizationUtil();
        u = new ApiAppUtil().userSetup(
                username,
                transitUtil.encrypt(password),
                tokenizationUtil.encode(email, "email-exdomain"),
                tokenizationUtil.encode(creditcard, "creditcard-numericupper"),
                "simple-transformation"
        );
        return userJpaRepository.save(u);
    }

    @PostMapping(value = "/api/v1/simplest-transformation/add-user")
    public Object addOneSimplestTransformedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        TokenizationUtil tokenizationUtil = new TokenizationUtil();
        u = new ApiAppUtil().userSetup(
                username,
                transitUtil.encrypt(password),
                tokenizationUtil.encode(email, "email-exdomain"),
                tokenizationUtil.encode(creditcard, "creditcard-numeric"),
                "simplest-transformation"
        );
        return userJpaRepository.save(u);
    }

    @RequestMapping(value = "/api/v1/decrypt")
    public Object decryptOneUser(@RequestParam String username){
        TransitUtil transitUtil = new TransitUtil();
        u = userJpaRepository.findUserByUsername(username);
        Map<String, String> decryptedUser = new LinkedHashMap<>();
        decryptedUser.put("username", username);
        decryptedUser.put("email", decodeBase64(transitUtil.decrypt(u.getEmail())));
        decryptedUser.put("creditcard", decodeBase64(transitUtil.decrypt(u.getCreditcard())));
        return decryptedUser;
    }


    @RequestMapping(value = "/api/v1/decode")
    public Object decodeOneUser(@RequestParam String username, String flag){
        TokenizationUtil tokenizationUtil = new TokenizationUtil();
        u = userJpaRepository.findUserByUsername(username);
        Map<String, String> decodedUser = new LinkedHashMap<>();
        decodedUser.put("username", username);
        if (flag.equals("transformation")) {
            decodedUser.put("email", tokenizationUtil.decode(u.getEmail(), "email"));
            String str = tokenizationUtil.decode(u.getCreditcard(), "creditcard-symbolnumericalpha");
            decodedUser.put("creditcard", tokenizationUtil.masking(str, "ccn-masking"));
        } else if (flag.equals("simple-transformation")) {
            decodedUser.put("email", tokenizationUtil.decode(u.getEmail(), "email-exdomain"));
            String str = tokenizationUtil.decode(u.getCreditcard(), "creditcard-numericupper");
            decodedUser.put("creditcard", tokenizationUtil.masking(str, "ccn-masking"));
        } else if (flag.equals("simplest-transformation")) {
            decodedUser.put("email", tokenizationUtil.decode(u.getEmail(), "email-exdomain"));
            String str = tokenizationUtil.decode(u.getCreditcard(), "creditcard-numeric");
            decodedUser.put("creditcard", tokenizationUtil.masking(str, "ccn-masking"));
        }
        return decodedUser;
    }

    public String decodeBase64(String text) {
        return new String(Base64.getDecoder().decode(text));
    }
}