package cn.sparrowmini.common.service.scopeconstant;

public final class MenuScope {

	private final static String TYPE = "menu";
	private final static String ADMIN = "admin";
	private final static String PREFIX = ADMIN + ":" + TYPE + ":";

	private MenuScope() {

	}

	public final static String CREATE = PREFIX + ScopeOpConstant.CREATE;
	public final static String READ = PREFIX + ScopeOpConstant.READ;
	public final static String UPDATE = PREFIX + ScopeOpConstant.UPDATE;
	public final static String DELETE = PREFIX + ScopeOpConstant.DELETE;
	public final static String LIST = PREFIX + ScopeOpConstant.LIST;
	public final static String SORT = PREFIX + ScopeOpConstant.SORT;
	public final static String TREE = PREFIX + ScopeOpConstant.TREE;

	public static final class MenuPemScope {
		private final static String PREFIX = ADMIN + ":" + MenuScope.TYPE + ":pem:";
		public final static String ADD = PREFIX + ScopeOpConstant.ADD;
		public final static String REMOVE = PREFIX + ScopeOpConstant.REMOVE;
		public final static String LIST = PREFIX + ScopeOpConstant.LIST;
	}

}
