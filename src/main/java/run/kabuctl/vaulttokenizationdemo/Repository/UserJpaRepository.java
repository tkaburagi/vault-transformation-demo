package run.kabuctl.vaulttokenizationdemo.Repository;

import org.springframework.data.jpa.repository.Query;
import run.kabuctl.vaulttokenizationdemo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u where u.flag = :flag")
    List<User> findUsersByFlag(String flag);

    @Query("SELECT u FROM User u where u.username = :username")
    User findUserByUsername(String username);
}