package cn.sparrowmini.bpm.server.common;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoggedUserGenerator implements ValueGenerator<String> {

	@Override
	public String generateValue(Session session, Object owner) {
		return SecurityContextHolder.getContext().getAuthentication()==null ?"Anonymous": SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
