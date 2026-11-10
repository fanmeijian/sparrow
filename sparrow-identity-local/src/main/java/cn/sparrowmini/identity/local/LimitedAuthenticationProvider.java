package cn.sparrowmini.identity.local;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import java.time.Clock;
import java.util.Objects;

/** Password verification remains Spring Security's DaoAuthenticationProvider. */
public final class LimitedAuthenticationProvider implements AuthenticationProvider {
    private final AuthenticationProvider delegate;private final LocalIdentityProvider users;
    private final int maximum;private final long lockMillis;private final Clock clock;
    public LimitedAuthenticationProvider(AuthenticationProvider delegate,LocalIdentityProvider users,int maximum,long lockMillis,Clock clock){
        this.delegate=Objects.requireNonNull(delegate);this.users=Objects.requireNonNull(users);this.clock=Objects.requireNonNull(clock);
        if(maximum<1 || lockMillis<1)throw new IllegalArgumentException("Invalid login limits");this.maximum=maximum;this.lockMillis=lockMillis;
    }
    public Authentication authenticate(Authentication authentication)throws AuthenticationException{
        String username=authentication.getName();if(users.loginLocked(username,clock.millis()))throw new BadCredentialsException("Invalid credentials");
        Authentication result;
        try{result=delegate.authenticate(authentication);}catch(BadCredentialsException failed){users.loginFailed(username,maximum,lockMillis,clock.millis());throw failed;}
        if(result==null || !(result.getPrincipal() instanceof LocalPrincipal principal) || !users.loginSucceeded(principal,clock.millis()))throw new BadCredentialsException("Invalid credentials");
        return result;
    }
    public boolean supports(Class<?> authentication){return delegate.supports(authentication);}
}
