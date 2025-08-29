package cn.sparrowmini.permission.scope.bean;

import cn.sparrowmini.common.service.scopeconstant.ScopeOpConstant;

public final class SysroleScope {

	private final static String TYPE = "sysrole";
	private final static String ADMIN = "admin";
	private final static String PREFIX = ADMIN + ":" + TYPE + ":";

	private SysroleScope() {

	}

	public final static String CREATE = PREFIX + ScopeOpConstant.CREATE;
	public final static String READ = PREFIX + ScopeOpConstant.READ;
	public final static String UPDATE = PREFIX + ScopeOpConstant.UPDATE;
	public final static String DELETE = PREFIX + ScopeOpConstant.DELETE;
	public final static String LIST = PREFIX + ScopeOpConstant.LIST;

	public final class SysroleUserScope {
		private final static String PREFIX = ADMIN + ":" + SysroleScope.TYPE + ":user:";
		public final static String ADD = PREFIX + ScopeOpConstant.ADD;
		public final static String REMOVE = PREFIX + ScopeOpConstant.REMOVE;
		public final static String LIST = PREFIX + ScopeOpConstant.LIST;
	}
}
