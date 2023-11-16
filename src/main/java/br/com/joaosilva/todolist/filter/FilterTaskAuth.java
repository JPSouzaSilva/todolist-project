package br.com.joaosilva.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.joaosilva.todolist.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            // Pega autenticacao (usuario e senha)
            var authorization = request.getHeader("Authorization"); // Pega o header (username e password) codificado
            var auth_encoded = authorization.substring("Basic".length()).trim(); // Tira o basic do inicio e remove os espaços em branco
            byte[] auth_decoded = Base64.getDecoder().decode(auth_encoded); // Decodifica o auth_encoded que esta em base 64
            var auth_string = new String(auth_decoded); // Transforma o decodificou em uma String
            String[] credential = auth_string.split(":"); // Separa a string em um array de string nos ":"
            String username = credential[0]; // Pega o username
            String password = credential[1]; // Pega o password
            // Valida usuario
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
            } else {
                // Valida senha
                var password_verify = BCrypt.verifyer()
                        .verify(password.toCharArray(), user.getPassword().toCharArray()); // Transforma em array de char pq a funcao espera isso como parametro
                if (password_verify.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response); // Segue
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
