package cn.sparrowmini.keycloak.tokenpocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * 因为新版本keycloak oauth2不支持自定参数配置，因此需要单独放外面配置文件读取
 */
public class ConfigHelper {



	public static Properties getProperties(String realmName) {
		Properties properties = new Properties();
		// 使用InPutStream流读取properties文件
		BufferedReader bufferedReader;
		try {
			String PATH = ConfigHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();
			PATH = PATH.substring(0, PATH.lastIndexOf("/") +1);
			bufferedReader = new BufferedReader(new FileReader(PATH + realmName + ".properties"));
			properties.load(bufferedReader);

		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
		// 获取key对应的value值
//	     properties.getProperty(String key);
	}

	public static String getProperty(String realmName, String key) {

		Properties properties = new Properties();
		// 使用InPutStream流读取properties文件
		BufferedReader bufferedReader;
		try {
			String PATH = ConfigHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();
			PATH = PATH.substring(0, PATH.lastIndexOf("/")+1);
			
			System.out.println(String.join("=", "文件路径",PATH,"属性",key));

			bufferedReader = new BufferedReader(new FileReader(PATH + realmName + ".properties"));
			properties.load(bufferedReader);
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取key对应的value值
		return properties.getProperty(key);
	}
}
