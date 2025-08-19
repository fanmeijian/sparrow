package cn.sparrowmini.common.service;

import java.util.Collection;
import java.util.Set;

public final class PermissionUserAndRole {
	public final static String ROLE_SUPER_SYSADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_SYSADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_SUPER_ADMIN = "SUPER_SYSADMIN";
	public final static String ROLE_ADMIN = "SUPER_SYSADMIN";
	public final static String ROOT_USER="SUPER_SYSADMIN";

	public static boolean isSuperSysAdmin(String username, Collection<String> roles){
		if (username.equalsIgnoreCase(PermissionUserAndRole.ROOT_USER)
				|| roles.stream().anyMatch(a -> a.equalsIgnoreCase(PermissionUserAndRole.ROLE_SUPER_SYSADMIN))) {
			return true;
		}
		return false;
	}


}
