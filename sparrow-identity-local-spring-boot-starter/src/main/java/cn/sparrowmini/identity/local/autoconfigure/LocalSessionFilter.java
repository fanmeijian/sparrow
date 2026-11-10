package cn.sparrowmini.identity.local.autoconfigure;

import cn.sparrowmini.identity.local.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

final class LocalSessionFilter extends OncePerRequestFilter {
    private final LocalIdentityProvider users;
    LocalSessionFilter(LocalIdentityProvider users){this.users=users;}
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null && auth.isAuthenticated() && auth.getPrincipal() instanceof LocalPrincipal principal && !users.isCurrent(principal)){
            SecurityContextHolder.clearContext();
            var session=request.getSession(false);if(session!=null)session.invalidate();
            response.sendError(401);return;
        }
        chain.doFilter(request,response);
    }
}
