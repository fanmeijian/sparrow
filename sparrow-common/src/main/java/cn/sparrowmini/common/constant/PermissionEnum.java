package cn.sparrowmini.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum PermissionEnum {
	ALL, ALL_CRUD, READER, AUTHOR, EDITOR, DELETER, DOWNLOAD, FORWARD, SHARE_ALL, SHARE_READER, SHARE_AUTHOR,
	SHARE_EDITOR, SHARE_DELETER, SHARE_DOWNLOAD, SHARE_FORWARD, EXECUTE, SHARE_EXECUTE;

	private static final Map<String, PermissionEnum> mappings = new HashMap<>(16);

	static {
		for (PermissionEnum permissionEnum : values()) {
			mappings.put(permissionEnum.name(), permissionEnum);
		}
	}

	public static PermissionEnum resolveAll(PermissionEnum permission) {
		if (permission.name().startsWith("SHARE"))
			return mappings.get("SHARE_ALL");
		if ("READER, AUTHOR, EDITOR, DELETER".contains(permission.name()))
			return mappings.get("ALL_CRUD");
		return PermissionEnum.ALL;
	}
}
