package cn.sparrowmini.permission.constant;

import org.springframework.security.core.context.SecurityContextHolder;

public final class PermissionUserAndRole {
	public final static String ROLE_SUPER_SYSADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_SYSADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_SUPER_ADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_ADMIN = "SUPER_SYSADMIN";
	public final static String ROOT_USER="SUPER_SYSADMIN";

	public static boolean isSuperSysAdmin(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if (username.equalsIgnoreCase(PermissionUserAndRole.ROOT_USER)
				|| SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equalsIgnoreCase(PermissionUserAndRole.ROLE_SUPER_SYSADMIN))) {
			return true;
		}
		return false;
	}
}
