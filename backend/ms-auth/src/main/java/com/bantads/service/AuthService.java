package com.bantads.service;
import com.bantads.entity.Auth;
import com.bantads.repository.AuthRepository;
import com.password4j.Password;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AuthService 
{
 private final AuthRepository authRepository;
 public AuthService(AuthRepository authRepository) 
    {
      this.authRepository = authRepository;
    }
    public Auth authenticate(String login, String password) 
    {
    Auth auth = authRepository.findByLogin(login).orElseThrow(() -> new ResponseStatusException
        (
         HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO"
        ));
        if (!Boolean.TRUE.equals(auth.getActive())) 
        {
         throw new ResponseStatusException
            (
             HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO"
            );
        }
        boolean passwordValid = Password.check(password,auth.getPassword()).withArgon2();
        if (!passwordValid) 
        {
            throw new ResponseStatusException
            (
                HttpStatus.UNAUTHORIZED,"NÃO AUTORIZADO"
            );
        }
    return auth;
    }
    public Auth create
    (
     String cpf,
     String type,
     String login,
     String rawPassword,
     Boolean active
    ) 
    {
        if (authRepository.existsByLogin(login)) 
        {
         throw new ResponseStatusException
         (
          HttpStatus.CONFLICT,"CONTA JÁ EXISTE"
         );
        }
    Auth auth = new Auth();
    auth.setCpf(cpf);
    auth.setType(type);
    auth.setLogin(login);
    auth.setActive(active);
    String hash = Password.hash(rawPassword).withArgon2().getResult();
    auth.setPassword(hash);
    return authRepository.save(auth);
    }
    public List<Auth> findAll() 
    {
     return authRepository.findAll();
    }
}