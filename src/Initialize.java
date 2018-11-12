import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.linuxense.javadbf.*;

public class Initialize {
	//初始化数据库文件存储路径
	public static void createDBPath() {
		File file = new File("数据库");
		if (file.exists()) {
			;
		} else {
			file.mkdir();
		}
	}

	// 创建用户信息表
	public static void createUserInfo() {
		if (!new File("userInfo.dbf").exists()) {
			String[][] userInfo = { { "", "type", "PK", "FK", "unique", "canNull", "check" },
					{ "userName", "char(30)", "PK", "", "", "not null", "" },
					{ "password", "char(30)", "", "", "", "not null", "" },
					{ "authority", "char(30)", "", "", "", "not null", "" }, };
			Utils.saveUserInfo("userInfo", userInfo);
			// 存储dbf文件
			DBFWriter writer = null;
			try {
				writer = new DBFWriter(new FileOutputStream("userInfo.dbf"), Charset.forName("GBK"));
				String[] strutName = { "userName", "password", "authority" };
				DBFDataType[] strutType = { DBFDataType.CHARACTER, DBFDataType.CHARACTER, DBFDataType.CHARACTER };
				Integer[] strutLength = { 30, 30, 30 };
				int fieldCount = strutName.length;
				DBFField[] fields = new DBFField[fieldCount];
				for (int i = 0; i < fieldCount; i++) {
					fields[i] = new DBFField();
					fields[i].setName(strutName[i]);
					fields[i].setType(strutType[i]);
					fields[i].setLength(strutLength[i]);
				}
				writer.setFields(fields);
				Object[] user = { "test", "123", "DBA" };
				writer.addRecord(user);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				writer.close();
			}
		}
	}

}
