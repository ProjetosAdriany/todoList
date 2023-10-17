package br.com.adrianyaires.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.adrianyaires.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {


    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var serveletPath = request.getServletPath();
                if(serveletPath.startsWith("/tasks/")) {

                    var authorization = request.getHeader("Authorization");
            
                    var authEncoded = authorization.substring("Basic".length()).trim();
                    byte [] authDecode = Base64.getDecoder().decode(authEncoded);
            
                    var authString = new String(authDecode);
            
                    String[] credentials = authString.split(":");
                    String username = credentials[0];
                    String password = credentials[1];
            
                  
                    var findUser = this.userRepository.findByUsername(username);
                    if(findUser == null){
                        response.sendError(401);
                    }else{
                        var checkPassword = BCrypt.verifyer().verify(password.toCharArray(), findUser.getPassword());
                        if(checkPassword.verified){
                            request.setAttribute("idUser", findUser.getId());
                            filterChain.doFilter(request, response);
                        }else{
                            response.sendError(401);
                        }
                    }
                }else{
                    filterChain.doFilter(request, response);
                }
    }
}
