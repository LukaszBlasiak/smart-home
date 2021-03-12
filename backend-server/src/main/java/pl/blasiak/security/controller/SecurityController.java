package pl.blasiak.security.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.blasiak.security.config.JwtConstants;
import pl.blasiak.security.converter.JwtMapper;
import pl.blasiak.security.model.JwtRequest;
import pl.blasiak.security.service.JwtServiceImpl;
import pl.blasiak.security.util.CookieUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class SecurityController {

    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;
    private final JwtMapper jwtMapper;
    private final CookieUtil cookieUtil;

    @PostMapping(value = "/logon")
    public ResponseEntity<Void> generateAuthenticationToken(@RequestBody final JwtRequest authenticationRequest, final HttpServletResponse httpServletResponse)
            throws Exception {

        final var authentication =
                this.authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final var jwtResponse = this.jwtMapper.toJwtResponse(JwtConstants.TOKEN_TYPE, jwtService.createToken(authentication));
        this.cookieUtil.saveJwtCookie(jwtResponse, httpServletResponse);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private Authentication authenticate(final String username, final String password) throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
