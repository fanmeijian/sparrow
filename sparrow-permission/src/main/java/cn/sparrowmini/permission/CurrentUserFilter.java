package cn.sparrowmini.permission;


import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.UserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CurrentUserFilter extends OncePerRequestFilter {


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 用于设置hibernate获取操作用户日志使用
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			String username = authentication.getName();
			Set<String> roles = authentication.getAuthorities().stream().map(m->m.getAuthority()).collect(Collectors.toSet());
			CurrentUser.logIn(new UserInfo(username,roles));
		} else {
			CurrentUser.logOut();
		}
		filterChain.doFilter(request, response);
	}
}
