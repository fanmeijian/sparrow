import cn.sparrowmini.keycloak.tokenpocket.ConfigHelper;

public class TestJava {
	private static String path = ConfigHelper.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(0);

	public static void main(String[] args) {
		System.out.println(ConfigHelper.getProperty("sportunione-prd","WECHAT_PUBLIC_ID"));
	}

}
