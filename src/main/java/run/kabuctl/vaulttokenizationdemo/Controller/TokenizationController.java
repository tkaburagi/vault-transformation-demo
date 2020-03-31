package run.kabuctl.vaulttokenizationdemo.Controller;

import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = "/hi")
    public String hi() {
        return "hi";
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

    @PostMapping(value = "/api/v1/plain/add-user")
    public Object addOneUser(@RequestParam String username, String password, String email, String creditcard)  {

        u.setId(UUID.randomUUID().toString());
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setCreditcard(creditcard);
        return userJpaRepository.save(u);
    }

    @PostMapping(value = "/api/v1/encrypt/add-user")
    public Object addOneEncryptedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        System.out.println("DEBUG" + username);
        u.setId(UUID.randomUUID().toString());
        u.setUsername(username);
        u.setPassword(transitUtil.encrypt(password));
        u.setEmail(transitUtil.encrypt(email));
        u.setCreditcard(transitUtil.encrypt(creditcard));
        u.setFlag("transit");
        return userJpaRepository.save(u);
    }

    @PostMapping(value = "/api/v1/transform/add-user")
    public Object addOneTransformedUser(@RequestParam String username, String password, String email, String creditcard)  {
        TransitUtil transitUtil = new TransitUtil();
        TokenizationUtil tokenizationUtil = new TokenizationUtil();
        u.setId(UUID.randomUUID().toString());
        u.setUsername(username);
        u.setPassword(transitUtil.encrypt(password));
        u.setEmail(tokenizationUtil.transform(email, "transform-to-symbolnumericalpha"));
        u.setCreditcard(tokenizationUtil.transform(creditcard, "transform-to-symbolnumeric"));
        u.setFlag("transformation");
        return userJpaRepository.save(u);
    }
}