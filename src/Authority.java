import java.io.Serializable;

public class Authority implements Serializable {
	private String userName;
	private String ObjectType;
	private String ObjectName;
	private String authority;
	private String canGrant;
	private String whoGrant;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getObjectType() {
		return ObjectType;
	}

	public void setObjectType(String objectType) {
		ObjectType = objectType;
	}

	public String getObjectName() {
		return ObjectName;
	}

	public void setObjectName(String objectName) {
		ObjectName = objectName;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getCanGrant() {
		return canGrant;
	}

	public void setCanGrant(String canGrant) {
		this.canGrant = canGrant;
	}

	public String getWhoGrant() {
		return whoGrant;
	}

	public void setWhoGrant(String whoGrant) {
		this.whoGrant = whoGrant;
	}

	public Authority(String userName, String objectType, String objectName, String authority, String canGrant,
			String whoGrant) {
		super();
		this.userName = userName;
		ObjectType = objectType;
		ObjectName = objectName;
		this.authority = authority;
		this.canGrant = canGrant;
		this.whoGrant = whoGrant;
	}

}
