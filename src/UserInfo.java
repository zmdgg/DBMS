import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFUtils;

public class UserInfo implements Serializable {
	public String userName;
	public String password;
	public String authority;
	//ÅÐ¶ÏÓÃ»§µÇÂ¼
	public static boolean isLogin(String userName, String password) {
		boolean result = false;
		DBFReader reader = null;
		try {
			reader = new DBFReader(new FileInputStream(new File("userInfo.dbf")));
			int numberOfFields = reader.getFieldCount();
			Object[] rowObjects;
			while ((rowObjects = reader.nextRecord()) != null) {
				if (userName.equals(rowObjects[0]) && password.equals(rowObjects[1]))
					result = true;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			DBFUtils.close(reader);
		}
		return result;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public UserInfo(String userName, String password, String authority) {
		super();
		this.userName = userName;
		this.password = password;
		this.authority = authority;
	}
}
