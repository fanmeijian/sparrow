import cn.sparrowmini.keycloak.wechatmini.ConfigHelper;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

public class TestJava {
	private static String path = ConfigHelper.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(0);

	public static void main(String[] args) throws URISyntaxException {

		String baseUrl = "http://localhost:4200/#/tokenpocket-sign";
		String query = "?nonce=3d99fc77-c7ee-422e-beca-ab3180b978d9&code=sdf&msg=bbc&clientId=tokenpocket";
		UriBuilder uriBuilder = UriBuilder.fromUri(baseUrl);
		uriBuilder.queryParam("nonce","asdfsdfasfd");
		// Create a URI that includes the hash as part of the path
		URI uri = URI.create(baseUrl + query);
		System.out.println("URI with hash preserved: " + uriBuilder.build().toString()); // Outputs URL with the hash location
		System.out.println(ConfigHelper.getProperty("sportunione-prd","WECHAT_PUBLIC_ID"));
	}

}
