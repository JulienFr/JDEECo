package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.List;

public class LogHelper {
	public static String getIdsString(List<String> ids){
		String str = "";
		for (String i : ids){
			str += i + (!i.equals(ids.get(ids.size()-1)) ? ", " : "");
		}
		return str;
	}
}
