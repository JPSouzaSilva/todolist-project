package br.com.joaosilva.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody User user) {
        var verify_username = this.userRepository.findByUsername(user.getUsername());
        if (verify_username != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe");
        }
        var password_encrypted = BCrypt.withDefaults()
                .hashToString(12, user.getPassword().toCharArray());

        user.setPassword(password_encrypted);
        var user_created = this.userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(user_created);
    }
}
