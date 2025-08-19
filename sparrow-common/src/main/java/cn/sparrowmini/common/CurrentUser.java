package cn.sparrowmini.common;

public class CurrentUser {
//	public static final CurrentUser INSTANCE = new CurrentUser();

	private static final ThreadLocal<String> storage = new ThreadLocal<>();
	private static final ThreadLocal<UserInfo> userInfo_ = new ThreadLocal<>();

	public static void logIn(UserInfo userInfo) {
		storage.set(userInfo.getUsername());
		userInfo_.set(userInfo);
	}

	public static void logOut() {
		storage.remove();
		userInfo_.remove();
	}

	public static String get() {
		return storage.get();
	}

	public static UserInfo getUserInfo(){
		return userInfo_.get();
	}
}
