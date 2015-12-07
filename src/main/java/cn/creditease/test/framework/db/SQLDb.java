package cn.creditease.test.framework.db;

import java.util.List;
import java.util.Map;

public interface SQLDb {
	void execute(String sql);
	
	List<Map<String, Object>> queryForList(String sql);
}
