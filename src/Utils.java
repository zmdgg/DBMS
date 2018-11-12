import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFUtils;
import com.linuxense.javadbf.DBFWriter;

public class Utils {
	public static String[] aut = {"insert","delete","update","select"};
	//�洢�û���������Ϣ
	public static void saveUserInfo(String path, String[][] info) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path + ".out")));
			oos.writeObject(info);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//��ȡ�û���������Ϣ
	public static String[][] getUserInfo(String path) {
		String[][] cons = null;
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(new File(path + ".out")));
			cons = (String[][]) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cons;
	}
	//��ȡ�û����м�¼
	public static Object[][] getUserData(String userTableName) {
		DBFReader reader = null;
		Object[][] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(userTableName + ".dbf")), Charset.forName("GBK"));
			record = new Object[reader.getFieldCount()][reader.getRecordCount()];
			Object[] rowObjects;
			// һ��Ϊһ������
			int i = 0;
			while ((rowObjects = reader.nextRecord()) != null) {
				for (int k = 0; k < record.length; k++) {
					record[k][i] = (Object) rowObjects[k];
				}
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		return record;
	}
	//�洢�û���¼
	public static void saveUserData(String tableName, Object[] finalValues) {
		DBFWriter writer = null;
		String path = tableName;
		String[][] t = getUserInfo(tableName);
		// for (int i = 0; i < finalValues.length; i++) {
		// System.out.println(finalValues[i]);
		// }
		String[] type = new String[t.length - 1];
		for (int i = 0; i < t.length - 1; i++) {
			type[i] = String.valueOf(t[i + 1][1]);
		}
		Object[] values = new Object[finalValues.length];
		try {
			writer = new DBFWriter(new File(path + ".dbf"), Charset.forName("GBK"));
			for (int i = 0; i < finalValues.length; i++) {
				if (type[i].matches("((?i)int|smallint)")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Integer.parseInt(temp);
				} else if (type[i].matches("((?i)boolean)")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Boolean.parseBoolean(temp);
				} else if (type[i].matches("((?i)(char|varchar)\\(.*\\))")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = temp;
				} else if (type[i].matches("((?i)float(.*))")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Double.parseDouble(temp);
				}
				// System.out.println(values[i]);
			}
			writer.addRecord(values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		System.out.println("��¼��'" + finalValues[0] + "'�����ɹ�,����Ϊ'" + finalValues[1] + "',Ȩ��Ϊ'" + finalValues[2] + "'!");
	}
	//�ж��û�Ȩ���Ƿ�ΪDBA
	public static boolean isDBA() {
		String userName = MainSystem.currentUser;
		boolean result = false;
		Object[][] values = getUserData("userInfo");
		for (int i = 0; i < values[0].length; i++) {
			String userValue = String.valueOf(values[0][i]);
			if (userName.equals(userValue)) {
				String authorityValue = String.valueOf(values[2][i]);
				if (authorityValue.equals("DBA")) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	//����û��������ݿ�Ȩ��
	public static boolean checkUseDB(String DBName) {
		String userName = MainSystem.currentUser;
		boolean result = false;
		ArrayList<Authority> list = getDBAuthorInfo(DBName);
		for (Authority i : list) {
			if (i.getUserName().equals(MainSystem.currentUser)) {
				result = true;
			}
		}
		return result;
	}
	//����û��������ݿ����Ȩ��
	public static boolean checkAuthority(String objectName, String OPType) {
		boolean result = false;
		String userName = MainSystem.currentUser;
		String database = MainSystem.currentDB;
		ArrayList<Authority> list = getDBAuthorInfo(database);
		for (Authority i : list) {
			// ��������ݿⴴ����
			if (i.getUserName().equals(userName) && i.getObjectName().equalsIgnoreCase("all")) {
				result = true;
				break;
			} else if (i.getUserName().equals(userName) && i.getObjectName().equals(objectName)) {
				if (i.getAuthority().equalsIgnoreCase("all")) {
					result = true;
					break;
				} else {
					String[] author = i.getAuthority().split(",");
					for (int j = 0; j < author.length; j++) {
						if (OPType.equalsIgnoreCase(author[j])) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}
	//���revoke��䣬����ȷ��ִ��
	public static void revokeAuthority(String userName, String objectName, String OPType) {
		// �ȼ���Ƿ���г�����Ȩ��
		boolean result = false;
		String localUser = MainSystem.currentUser;
		String database = MainSystem.currentDB;
		ArrayList<Authority> list = getDBAuthorInfo(database);
		for (Authority i : list) {
			// ���Ҫ�������ĵ�¼�������ݿⴴ����
			if (i.getUserName().equals(userName) && i.getObjectName().equalsIgnoreCase("all")) {
				result = false;
				break;
			}
			// �������¼�������ݿⴴ����
			if (i.getUserName().equals(localUser) && i.getObjectName().equalsIgnoreCase("all")) {
				result = true;
				break;
			} else if (i.getUserName().equals(userName) && i.getObjectName().equals(objectName)) {
				if (i.getAuthority().equalsIgnoreCase("all")) {
					if (i.getWhoGrant().equals(localUser)) {
						result = true;
						break;
					}
				} else {
					String[] author = i.getAuthority().split(",");
					for (int j = 0; j < author.length; j++) {
						if (OPType.equalsIgnoreCase(author[j])) {
							if (i.getWhoGrant().equals(localUser)) {
								result = true;
								break;
							}
						}
					}
				}
			}
		}
		// �������
		if (result) {
			//һ�����һ���û���һ����Ĳ�����¼ֻ����һ��???
			for (Authority i : list) {
				if (i.getUserName().equals(userName) && i.getObjectName().equals(objectName)) {
					if (i.getAuthority().equalsIgnoreCase("all")) {
						if(OPType.equalsIgnoreCase("all")){
							i.setAuthority("");
						}else{
							String temp = "";
							for (int j = 0; j < aut.length; j++) {
								if(!OPType.equalsIgnoreCase(aut[j]))
									temp += aut[j]+",";
							}
							temp = temp.substring(0, temp.length()-1);
							i.setAuthority(temp);
						}
					} else {
						String[] author = i.getAuthority().split(",");
						for (int j = 0; j < author.length; j++) {
							if (author[j].equalsIgnoreCase(OPType)) {
								author[j] = "";
								break;
							}
						}
						String temp = "";
						for (int j = 0; j < author.length; j++) {
							if(!author[j].matches("\\s{0,}")){
								temp += author[j]+",";
							}
						}
						i.setAuthority(temp);
					}
				}
			}
			//����һ���µ�
			ArrayList<Authority> newList = new ArrayList<Authority>();
			for (Authority i : list) {
				if(!i.getAuthority().matches("\\s{0,}")){
					newList.add(i);
				}
			}
			//����ԭ����
			alterAuthorityInfo(database,newList);
			System.out.println("��¼��'" + localUser + "'�ѳ�����¼��'"+userName+"'�Ա�'"+objectName+"'��'"+OPType+"'Ȩ�ޣ�");
		} else
			System.out.println("��¼��'" + localUser + "'�޷�������Ȩ�ޣ�");
	}
	// �޸�Ȩ�ޱ�,revoke������
	public static void alterAuthorityInfo(String DBName, ArrayList<Authority> list) {
		String path = "���ݿ�\\" + DBName + "\\";
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path + "authorityInfo.out")));
			oos.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//�ж�ĳ�����ݿ��Ƿ����
	public static String isExistsDB(String DataBaseName) {
		// �����ڷ���true
		String path = "���ݿ�\\" + DataBaseName;
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				return "���ݿ�'" + DataBaseName + "'�Ѵ��ڣ�";
			}
		}
		return "";
	}
	//ִ��create database���
	public static void createDB(String DBName) {
		String path = "���ݿ�\\" + DBName;
		if (isExistsDB(DBName).matches("\\s{0,}")) {
			new File(path).mkdir();
			new File(path + "\\��").mkdir();
			new File(path + "\\��ͼ").mkdir();
			// ��ʼ������ļ�
			ArrayList<String> list = new ArrayList<String>();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(new FileOutputStream(new File(path + "\\��\\" + "FKInfo.out")));
				oos.writeObject(list);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// ��ʼ��������Ϣ
			saveIndex(path + "\\��\\" + "index.out");
			// ��ʼ��Ȩ�ޱ�
			Authority a = new Authority(MainSystem.currentUser, "ALL", "ALL", "ALL", "true", MainSystem.currentUser);
			saveAuthorityInfo(DBName, a);
			System.out.println("���ݿ�'" + DBName + "'�����ɹ���");
			System.out.println("��¼��'" + MainSystem.currentUser + "'�����ݿ�'" + DBName + "'�������͵Ķ������ȫ��Ȩ�ޣ�");
		}
	}
	// ����Ȩ�ޱ�grant��create database����
	public static void saveAuthorityInfo(String DBName, Authority a) {
		ArrayList<Authority> list = null;
		String path = "���ݿ�\\" + DBName + "\\";
		// û�в�����
		if (!new File(path + "authorityInfo.out").exists()) {
			list = new ArrayList<Authority>();
			list.add(a);
		} else {
			list = getDBAuthorInfo(DBName);
			list.add(a);
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path + "authorityInfo.out")));
			oos.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//��ȡĳ�����ݿ�ķ���Ȩ����Ϣ
	public static ArrayList<Authority> getDBAuthorInfo(String DBName) {
		String path = "���ݿ�\\" + DBName + "\\";
		ArrayList<Authority> list = null;
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(new File(path + "authorityInfo.out")));
			list = (ArrayList<Authority>) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	//�ж�ĳ�����Ƿ����
	public static String isExistsTable(String tableName) {
		// �����ڷ���true
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName;
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				return "��'" + tableName + "'�Ѵ��ڣ�";
			}
		}
		return "";
	}
	//ִ��create view���
	public static void saveView(View v) {
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��ͼ\\" + v.getViewName() + ".out";
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
			oos.writeObject(v);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("��ͼ'" + v.getViewName() + "'�����ɹ���");
	}
	//��ȡĳ����ͼ
	public static View getView(String viewName) {
		View v = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��ͼ\\" + viewName + ".out";
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(new File(path)));
			v = (View) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return v;
	}
	//�ж�ĳ����ͼ�Ƿ����
	public static String isExistsView(String viewName) {
		// �����ڷ���true
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��ͼ\\" + viewName + ".out";
		File file = new File(path);
		if (file.exists()) {
			return "��ͼ'" + viewName + "'�Ѵ��ڣ�";
		} else
			return "";
	}
	//�洢�����Ϣ
	public static void saveFKInfo(String FKInfo) {
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + "FKInfo.out";
		ArrayList<String> list = getFKInfo();
		list.add(FKInfo);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
			oos.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	//��ȡ�����Ϣ
	public static ArrayList<String> getFKInfo() {
		ArrayList<String> list = null;
		// �����ڷ���true
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + "FKInfo.out";
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(new File(path)));
			list = (ArrayList<String>) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	//ִ��insert���
	public static void insertData(String tableName, Object[] finalValues, String opType) {
		if (!checkAuthority(tableName, "insert")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'insert'Ȩ��");
			return;
		}
		DBFWriter writer = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName;
		Table t = getTableOut(tableName);
		// for (int i = 0; i < finalValues.length; i++) {
		// System.out.println(finalValues[i]);
		// }
		String[] type = new String[t.getCons().length - 1];
		for (int i = 0; i < t.getCons().length - 1; i++) {
			type[i] = String.valueOf(t.getCons()[i + 1][1]);
		}
		Object[] values = new Object[finalValues.length];
		try {
			writer = new DBFWriter(new File(path + ".dbf"), Charset.forName("GBK"));
			for (int i = 0; i < finalValues.length; i++) {
				if (type[i].matches("((?i)int|smallint)")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Integer.parseInt(temp);
				} else if (type[i].matches("((?i)boolean)")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Boolean.parseBoolean(temp);
				} else if (type[i].matches("((?i)(char|varchar)\\(.*\\))")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = temp;
				} else if (type[i].matches("((?i)float(.*))")) {
					String temp = String.valueOf(finalValues[i]);
					values[i] = Double.parseDouble(temp);
				}
				// System.out.println(values[i]);
			}
			writer.addRecord(values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		if (opType.equals("ins"))
			System.out.println("���һ������");
	}
	//�ж�ĳ�����Ƿ����
	public static String isExistsCol(String tableName, String cols) {
		Table t = getTableOut(tableName);
		String errInfo = "";
		boolean result = false;
		String[][] cons = t.getCons();
		for (int i = 1; i < cons.length; i++) {
			if (cons[i][0].equals(cols)) {
				result = true;
			}
		}
		if (!result)
			errInfo += "��'" + tableName + "'��������'" + cols + "'!";
		return errInfo;
	}
	//��ȡĳ�����������Ϣ
	public static Table getTableOut(String tableName) {
		// �����ڷ���true
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName;
		File file = new File(path + "\\" + tableName + ".out");
		Table t = null;
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(file));
			t = (Table) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return t;
	}
	//��ȡĳ�����ȫ����¼��������������select��
	public static String[][] getAllData(String tableName) {
		DBFReader reader = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName + ".dbf";
		String[][] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(path)), Charset.forName("GBK"));
			record = new String[reader.getRecordCount() + 1][reader.getFieldCount()];
			// ��ȡ����
			for (int i = 0; i < record[0].length; i++) {
				record[0][i] = reader.getField(i).getName();
			}
			Object[] rowObjects;
			int i = 1;
			// ��ȡ����
			while ((rowObjects = reader.nextRecord()) != null) {
				for (int k = 0; k < rowObjects.length; k++) {
					record[i][k] = String.valueOf(rowObjects[k]);
				}
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		// for (int i = 0; i < record.length; i++) {
		// for (int j = 0; j < record[i].length; j++) {
		// System.out.println(record[i][j]);
		// }
		// }
		return record;
	}
	//�����¼������
	public static void printData(String[] cols, String[][] data) {
		// ����м�¼
		if (data.length > 0) {
			String singleline = "";
			for (int i = 0; i < data[0].length * 8 + 1; ++i)
				singleline += "��";
			System.out.println("��" + singleline + "��");
			System.out.print("��");
			for (int i = 0; i < cols.length; i++) {
				System.out.printf("%s\t  ", cols[i]);
			}
			System.out.println("��");
			System.out.println("��" + singleline + "��");
			for (int i = 0; i < data.length; i++) {
				System.out.print("��");
				for (int j = 0; j < data[i].length; j++) {
					System.out.printf("%s\t  ", data[i][j]);
				}
				System.out.println("��");
				if (i != data.length - 1) {
					System.out.println("��" + singleline + "��");
				}
			}
			System.out.println("��" + singleline + "��");
		} else {
			String singleline = "";
			for (int i = 0; i < cols.length * 8 + 1; ++i)
				singleline += "��";
			System.out.println("��" + singleline + "��");
			System.out.print("��");
			for (int i = 0; i < cols.length; i++) {
				System.out.printf("%s\t  ", cols[i]);
			}
			System.out.println("��");
			System.out.println("��" + singleline + "��");
		}
	}
	//ִ��select���
	public static void selectPartialData(String tableName, String[] cols, String[] whereSQL, boolean isAnd) {
		if (!checkAuthority(tableName, "select")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'select'Ȩ��");
			return;
		}
		Table t = getTableOut(tableName);
		Object[][] oriValues = getAllRecord(tableName);
		String[][] cons = new String[whereSQL.length][3];
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String regWhere = reg + "\\s{0,}((?i)in|between|like|=)\\s{0,}" + "(.*)";
		for (int i = 0; i < whereSQL.length; i++) {
			Pattern p = Pattern.compile(regWhere);
			Matcher m = p.matcher(whereSQL[i]);
			if (m.find()) {
				cons[i][0] = m.group(1).trim();
				cons[i][1] = m.group(2).trim();
				cons[i][2] = m.group(3).trim();
			}
		}
		int selCount = 0;
		if (!whereSQL[0].equals("*")) {
			// �����or
			if (!isAnd) {
				for (int i = 1; i < oriValues.length; i++) {
					for (int j = 0; j < cons.length; j++) {
						for (int k = 1; k < oriValues[0].length; k++) {
							if (oriValues[0][k].equals(cons[j][0])) {
								if (cons[j][1].equals("=") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))) {
										oriValues[i][0] = "sel";
										selCount++;
									}
								} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
									int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
									int value = Integer.parseInt(temp);
									if (value <= max && min >= min) {
										oriValues[i][0] = "sel";
										selCount++;
									}
								} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
									for (int m = 0; m < v.length; m++) {
										if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
											oriValues[i][0] = "del";
											selCount++;
											break;
										}
									}
								} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									String regLike = cons[j][2].replaceAll("_", ".");
									regLike = cons[j][2].replaceAll("%", ".*");
									regLike = regLike.substring(1, regLike.length() - 1);
									// System.out.println(regLike);
									if (temp.matches(regLike)) {
										oriValues[i][0] = "del";
										selCount++;
									}
								}
							}
						}
					}
				}
			} else {
				int andSum = cons.length;
				for (int i = 1; i < oriValues.length; i++) {
					for (int j = 0; j < cons.length; j++) {
						for (int k = 1; k < oriValues[0].length; k++) {
							if (oriValues[0][k].equals(cons[j][0])) {
								if (cons[j][1].equals("=")) {
									String temp = String.valueOf(oriValues[i][k]);
									// System.out.println(cons[j][2].substring(1,
									// cons[j][2].length() - 1)+" "+temp);
									if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))
											&& !oriValues[i][0].equals("sel")) {
										String c = String.valueOf(oriValues[i][0]);
										if (c.matches("\\s{0,}")) {
											oriValues[i][0] = "1";
											if (andSum == 1) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										} else {
											int a = Integer.parseInt(c);
											if (a != andSum)
												oriValues[i][0] = String.valueOf(++a);
											if (a == andSum) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										}
									}
								} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
									int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
									int value = Integer.parseInt(temp);
									if (value <= max && min >= min) {
										String c = String.valueOf(oriValues[i][0]);
										if (c.matches("\\s{0,}")) {
											oriValues[i][0] = "1";
											if (andSum == 1) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										} else {
											int a = Integer.parseInt(c);
											if (a != andSum)
												oriValues[i][0] = String.valueOf(++a);
											if (a == andSum) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										}
									}
								} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
									for (int m = 0; m < v.length; m++) {
										if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
											String c = String.valueOf(oriValues[i][0]);
											if (c.matches("\\s{0,}")) {
												oriValues[i][0] = "1";
												if (andSum == 1) {
													oriValues[i][0] = "sel";
													selCount++;
												}
											} else {
												int a = Integer.parseInt(c);
												if (a != andSum)
													oriValues[i][0] = String.valueOf(++a);
												if (a == andSum) {
													oriValues[i][0] = "sel";
													selCount++;
												}
											}
											break;
										}
									}
								} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("sel")) {
									String temp = String.valueOf(oriValues[i][k]);
									String regLike = cons[j][2].replaceAll("_", ".");
									regLike = cons[j][2].replaceAll("%", ".*");
									regLike = regLike.substring(1, regLike.length() - 1);
									// System.out.println(regLike);
									if (temp.matches(regLike)) {
										String c = String.valueOf(oriValues[i][0]);
										if (c.matches("\\s{0,}")) {
											oriValues[i][0] = "1";
											if (andSum == 1) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										} else {
											int a = Integer.parseInt(c);
											if (a != andSum)
												oriValues[i][0] = String.valueOf(++a);
											if (a == andSum) {
												oriValues[i][0] = "sel";
												selCount++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			// for (int i = 0; i < oriValues.length; i++) {
			// for (int j = 0; j < oriValues[0].length; j++) {
			// System.out.print(oriValues[i][j] + " ");
			// }
			// System.out.println();
			// }
			String[][] finalValues = null;
			if (!cols[0].equals("*")) {
				int index = 0;
				finalValues = new String[selCount][cols.length];
				for (int i = 1; i < oriValues.length; i++) {
					if (oriValues[i][0].equals("sel")) {
						for (int j = 1; j < oriValues[0].length; j++) {
							for (int k = 0; k < cols.length; k++) {
								if (oriValues[0][j].equals(cols[k]))
									finalValues[index][k] = (String) oriValues[i][j];
							}
						}
						index++;
					}
				}
			} else {
				int index = 0;
				finalValues = new String[selCount][oriValues[0].length - 1];
				for (int i = 1; i < oriValues.length; i++) {
					if (oriValues[i][0].equals("sel")) {
						for (int j = 1; j < oriValues[0].length; j++) {
							finalValues[index][j - 1] = (String) oriValues[i][j];
						}
						index++;
					}
				}
				cols = new String[oriValues[0].length - 1];
				for (int i = 0; i < oriValues[0].length - 1; ++i)
					cols[i] = String.valueOf(oriValues[0][i + 1]);
			}
			printData(cols, finalValues);
			// for (int i = 0; i < finalValues.length; i++) {
			// for (int j = 0; j < finalValues[0].length; j++) {
			// System.out.print(finalValues[i][j] + " ");
			// }
			// System.out.println();
			// }
		} else {
			String[][] finalValues = null;
			if (!cols[0].equals("*")) {
				finalValues = new String[oriValues.length - 1][cols.length];
				for (int i = 1; i < oriValues.length; i++) {
					for (int j = 1; j < oriValues[0].length; j++) {
						for (int k = 0; k < cols.length; k++) {
							if (oriValues[0][j].equals(cols[k])) {
								finalValues[i - 1][k] = (String) oriValues[i][j];
							}
						}
					}
				}
			} else {
				int index = 0;
				finalValues = new String[oriValues.length - 1][oriValues[0].length - 1];
				for (int i = 1; i < oriValues.length; i++) {
					for (int j = 1; j < oriValues[0].length; j++) {
						finalValues[i - 1][j - 1] = (String) oriValues[i][j];
					}
				}
				cols = new String[oriValues[0].length - 1];
				for (int i = 0; i < oriValues[0].length - 1; ++i)
					cols[i] = String.valueOf(oriValues[0][i + 1]);
			}
			printData(cols, finalValues);
		}
		// for (int i = 0; i < oriValues.length; i++) {
		// for (int j = 0; j < oriValues[0].length; j++) {
		// System.out.print(oriValues[i][j] + " ");
		// }
		// System.out.println();
		// }
	}
	//��ȡĳ������ĳһ��ȫ������
	public static Object[] getColData(String tableName, String colName) {
		DBFReader reader = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName + ".dbf";
		Object[] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(path)), Charset.forName("GBK"));
			record = new Object[reader.getRecordCount()];
			int index = 0;
			for (int i = 0; i < reader.getFieldCount(); i++) {
				if (reader.getField(i).getName().equals(colName)) {
					index = i;
					break;
				}
			}
			Object[] rowObjects;
			// һ��Ϊһ������
			int i = 0;
			while ((rowObjects = reader.nextRecord()) != null) {
				record[i] = (Object) rowObjects[index];
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		return record;
	}
	//��ȡȫ����¼������insertԼ�����
	public static Object[][] getData(String tableName) {
		DBFReader reader = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName + ".dbf";
		Object[][] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(path)), Charset.forName("GBK"));
			record = new Object[reader.getFieldCount()][reader.getRecordCount()];
			Object[] rowObjects;
			// һ��Ϊһ������
			int i = 0;
			int j = 0;
			while ((rowObjects = reader.nextRecord()) != null) {
				for (int k = 0; k < record.length; k++) {
					record[k][i] = (Object) rowObjects[k];
				}
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		return record;
	}
	//��ȡȫ����¼������deleteԼ�����
	public static String[][] getAllRecord(String tableName) {
		DBFReader reader = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName + ".dbf";
		String[][] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(path)), Charset.forName("GBK"));
			record = new String[reader.getRecordCount() + 1][reader.getFieldCount() + 1];
			record[0][0] = "";
			// ��ȡ����
			for (int i = 0; i < record[0].length - 1; i++) {
				record[0][i + 1] = reader.getField(i).getName();
			}
			Object[] rowObjects;
			int i = 1;
			// ��ȡ����
			while ((rowObjects = reader.nextRecord()) != null) {
				for (int k = 0; k < rowObjects.length; k++) {
					record[i][k + 1] = String.valueOf(rowObjects[k]);
				}
				record[i][0] = "";
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		// for (int i = 0; i < record.length; i++) {
		// for (int j = 0; j < record[i].length; j++) {
		// System.out.println(record[i][j]);
		// }
		// }
		return record;
	}
	//ִ�в���where�Ӿ��delete���
	public static void deleteAllData(String tableName) {
		if (!checkAuthority(tableName, "delete")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'delete'Ȩ��");
			return;
		}
		Table t = getTableOut(tableName);
		Object[][] oriValues = getAllRecord(tableName);
		String errInfo = "";
		for (int i = 1; i < oriValues.length; i++) {
			for (int j = 1; j < oriValues[i].length; j++) {
				String colName = String.valueOf(oriValues[0][j]);
				String finalValues = String.valueOf(oriValues[i][j]);
				errInfo += CheckData.isPassFK(tableName, colName, "del", finalValues);
				if (!errInfo.matches("\\s{0,}")) {
					break;
				}
			}
		}
		if (errInfo.matches("\\s{0,}")) {
			System.out.println("ɾ��" + (getAllData(tableName).length - 1) + "����¼��");
			createTableDBF(t);
		} else
			System.out.println(errInfo);
	}
	//ִ�д�where�Ӿ��delete���
	public static void deletePartialData(String tableName, String[] whereSQL, boolean isAnd) {
		if (!checkAuthority(tableName, "delete")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'delete'Ȩ��");
			return;
		}
		Table t = getTableOut(tableName);
		Object[][] oriValues = getAllRecord(tableName);
		String[][] cons = new String[whereSQL.length][3];
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String regWhere = reg + "\\s{0,}((?i)in|between|like|=)\\s{0,}" + "(.*)";
		for (int i = 0; i < whereSQL.length; i++) {
			Pattern p = Pattern.compile(regWhere);
			Matcher m = p.matcher(whereSQL[i]);
			if (m.find()) {
				cons[i][0] = m.group(1).trim();
				cons[i][1] = m.group(2).trim();
				cons[i][2] = m.group(3).trim();
			}
		}
		int delCount = 0;
		// �����or
		if (!isAnd) {
			for (int i = 1; i < oriValues.length; i++) {
				for (int j = 0; j < cons.length; j++) {
					for (int k = 1; k < oriValues[0].length; k++) {
						if (oriValues[0][k].equals(cons[j][0])) {
							if (cons[j][1].equals("=") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))) {
									oriValues[i][0] = "del";
									delCount++;
								}
							} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
								int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
								int value = Integer.parseInt(temp);
								if (value <= max && min >= min) {
									oriValues[i][0] = "del";
									delCount++;
								}
							} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
								for (int m = 0; m < v.length; m++) {
									if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
										oriValues[i][0] = "del";
										delCount++;
										break;
									}
								}
							} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								String regLike = cons[j][2].replaceAll("_", ".");
								regLike = cons[j][2].replaceAll("%", ".*");
								regLike = regLike.substring(1, regLike.length() - 1);
								// System.out.println(regLike);
								if (temp.matches(regLike)) {
									oriValues[i][0] = "del";
									delCount++;
								}
							}
						}
					}
				}
			}
		} else {
			int andSum = cons.length;
			for (int i = 1; i < oriValues.length; i++) {
				for (int j = 0; j < cons.length; j++) {
					for (int k = 1; k < oriValues[0].length; k++) {
						if (oriValues[0][k].equals(cons[j][0])) {
							if (cons[j][1].equals("=")) {
								String temp = String.valueOf(oriValues[i][k]);
								// System.out.println(cons[j][2].substring(1,
								// cons[j][2].length() - 1)+" "+temp);
								if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))
										&& !oriValues[i][0].equals("del")) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "del";
											delCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "del";
											delCount++;
										}
									}
								}
							} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
								int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
								int value = Integer.parseInt(temp);
								if (value <= max && min >= min) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "del";
											delCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "del";
											delCount++;
										}
									}
								}
							} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
								for (int m = 0; m < v.length; m++) {
									if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
										String c = String.valueOf(oriValues[i][0]);
										if (c.matches("\\s{0,}")) {
											oriValues[i][0] = "1";
											if (andSum == 1) {
												oriValues[i][0] = "del";
												delCount++;
											}
										} else {
											int a = Integer.parseInt(c);
											if (a != andSum)
												oriValues[i][0] = String.valueOf(++a);
											if (a == andSum) {
												oriValues[i][0] = "del";
												delCount++;
											}
										}
										break;
									}
								}
							} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("del")) {
								String temp = String.valueOf(oriValues[i][k]);
								String regLike = cons[j][2].replaceAll("_", ".");
								regLike = cons[j][2].replaceAll("%", ".*");
								regLike = regLike.substring(1, regLike.length() - 1);
								// System.out.println(regLike);
								if (temp.matches(regLike)) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "del";
											delCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "del";
											delCount++;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		// for (int i = 0; i < oriValues.length; i++) {
		// for (int j = 0; j < oriValues[0].length; j++) {
		// System.out.print(oriValues[i][j] + " ");
		// }
		// System.out.println();
		// }
		int index = 0;
		String errInfo = "";
		Object[][] finalValues = new Object[oriValues.length - delCount - 1][oriValues[0].length - 1];
		for (int i = 1; i < oriValues.length; i++) {
			if (!oriValues[i][0].equals("del")) {
				for (int j = 1; j < oriValues[0].length; j++) {
					finalValues[index][j - 1] = (Object) oriValues[i][j];
				}
				index++;
			} else {
				// ��鱻ɾ����
				for (int j = 1; j < oriValues[i].length; j++) {
					String colName = String.valueOf(oriValues[0][j]);
					String values = String.valueOf(oriValues[i][j]);
					errInfo += CheckData.isPassFK(tableName, colName, "del", values);
					if (!errInfo.matches("\\s{0,}")) {
						break;
					}
				}
			}
		}
//		for (int i = 0; i < finalValues.length; i++) {
//			for (int j = 0; j < finalValues[0].length; j++) {
//				System.out.print(finalValues[i][j] + " ");
//			}
//			System.out.println();
//		}

		if (errInfo.matches("\\s{0,}")) {
			createTableDBF(t);
			for (int i = 0; i < finalValues.length; i++) {
				insertData(tableName, finalValues[i], "del");
			}
			System.out.println("ɾ��" + (delCount) + "����¼��");
		} else
			System.out.println(errInfo);
	}
	//��ȡȫ����¼������updateԼ�����
	public static String[][] getDataForUpd(String tableName) {
		DBFReader reader = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName + "\\" + tableName + ".dbf";
		String[][] record = null;
		try {
			reader = new DBFReader(new FileInputStream(new File(path)), Charset.forName("GBK"));
			record = new String[reader.getRecordCount() + 1][reader.getFieldCount()];
			// ��ȡ����
			for (int i = 0; i < record[0].length; i++) {
				record[0][i] = reader.getField(i).getName();
			}
			Object[] rowObjects;
			int i = 1;
			// ��ȡ����
			while ((rowObjects = reader.nextRecord()) != null) {
				for (int k = 0; k < rowObjects.length; k++) {
					record[i][k] = String.valueOf(rowObjects[k]);
				}
				++i;
			}
		} catch (DBFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		// for (int i = 0; i < record.length; i++) {
		// for (int j = 0; j < record[i].length; j++) {
		// System.out.println(record[i][j]);
		// }
		// }
		return record;
	}
	//ִ�в���where�Ӿ��update���
	public static void updateAllData(String tableName, String[] set) {
		if (!checkAuthority(tableName, "update")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'update'Ȩ��");
			return;
		}
		Table t = getTableOut(tableName);
		Object[][] oriValues = getDataForUpd(tableName);
		Object[][] backValues = getDataForUpd(tableName);
		int updSum = oriValues.length - 1;
		for (int i = 0; i < oriValues[0].length; i++) {
			if (set[0].equals(String.valueOf(oriValues[0][i]))) {
				for (int j = 1; j < oriValues.length; j++) {
					oriValues[j][i] = set[2];
				}
			}
		}
		createTableDBF(t);
		String errInfo = "";
		for (int i = 1; i < oriValues.length; i++) {
			if (!(errInfo = CheckData.CheckUpdate(tableName, oriValues[i])).matches("\\s{0,}")) {
				System.out.println(errInfo);
				break;
			}
		}
		// for (int i = 0; i < oriValues.length; i++) {
		// for (int j = 0; j < oriValues[i].length; j++) {
		// System.out.print(oriValues[i][j]+" ");
		// }
		// System.out.println();
		// }
		if (errInfo.matches("\\s{0,}"))
			System.out.println("�޸�" + (updSum) + "����¼��");
		else {
			createTableDBF(t);
			for (int i = 1; i < backValues.length; i++) {
				CheckData.CheckUpdate(tableName, backValues[i]);
			}
		}
	}
	//ִ�д�where�Ӿ��update���
	public static void updatePartialData(String tableName, String[] set, String[] whereSQL, boolean isAnd) {
		if (!checkAuthority(tableName, "update")) {
			System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + tableName + "'û��'update'Ȩ��");
			return;
		}
		Table t = getTableOut(tableName);
		Object[][] oriValues = getAllRecord(tableName);
		Object[][] backValues = getDataForUpd(tableName);
		String[][] cons = new String[whereSQL.length][3];
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String regWhere = reg + "\\s{0,}((?i)in|between|like|=)\\s{0,}" + "(.*)";
		for (int i = 0; i < whereSQL.length; i++) {
			Pattern p = Pattern.compile(regWhere);
			Matcher m = p.matcher(whereSQL[i]);
			if (m.find()) {
				cons[i][0] = m.group(1).trim();
				cons[i][1] = m.group(2).trim();
				cons[i][2] = m.group(3).trim();
			}
		}
		// for (int i = 0; i < set.length; i++) {
		// System.out.println(set[i]);
		// }
		int updCount = 0;
		// �����or
		if (!isAnd) {
			for (int i = 1; i < oriValues.length; i++) {
				for (int j = 0; j < cons.length; j++) {
					for (int k = 1; k < oriValues[0].length; k++) {
						if (oriValues[0][k].equals(cons[j][0])) {
							if (cons[j][1].equals("=") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))) {
									oriValues[i][0] = "upd";
									updCount++;
								}
							} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
								int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
								int value = Integer.parseInt(temp);
								if (value <= max && min >= min) {
									oriValues[i][0] = "upd";
									updCount++;
								}
							} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
								for (int m = 0; m < v.length; m++) {
									if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
										oriValues[i][0] = "upd";
										updCount++;
										break;
									}
								}
							} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								String regLike = cons[j][2].replaceAll("_", ".");
								regLike = cons[j][2].replaceAll("%", ".*");
								regLike = regLike.substring(1, regLike.length() - 1);
								// System.out.println(regLike);
								if (temp.matches(regLike)) {
									oriValues[i][0] = "upd";
									updCount++;
								}
							}
						}
					}
				}
			}
		} else {
			int andSum = cons.length;
			for (int i = 1; i < oriValues.length; i++) {
				for (int j = 0; j < cons.length; j++) {
					for (int k = 1; k < oriValues[0].length; k++) {
						if (oriValues[0][k].equals(cons[j][0])) {
							if (cons[j][1].equals("=")) {
								String temp = String.valueOf(oriValues[i][k]);
								// System.out.println(cons[j][2].substring(1,
								// cons[j][2].length() - 1)+" "+temp);
								if (temp.equals(cons[j][2].substring(1, cons[j][2].length() - 1))
										&& !oriValues[i][0].equals("upd")) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									}
								}
							} else if (cons[j][1].equals("between") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								int min = Integer.parseInt(cons[j][2].split("((?i)and)")[0].trim());
								int max = Integer.parseInt(cons[j][2].split("((?i)and)")[1].trim());
								int value = Integer.parseInt(temp);
								if (value <= max && min >= min) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									}
								}
							} else if (cons[j][1].equals("in") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								String[] v = cons[j][2].substring(1, cons[j][2].length() - 1).split(",");
								for (int m = 0; m < v.length; m++) {
									if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
										String c = String.valueOf(oriValues[i][0]);
										if (c.matches("\\s{0,}")) {
											oriValues[i][0] = "1";
											if (andSum == 1) {
												oriValues[i][0] = "upd";
												updCount++;
											}
										} else {
											int a = Integer.parseInt(c);
											if (a != andSum)
												oriValues[i][0] = String.valueOf(++a);
											if (a == andSum) {
												oriValues[i][0] = "upd";
												updCount++;
											}
										}
										break;
									}
								}
							} else if (cons[j][1].equals("like") && !oriValues[i][0].equals("upd")) {
								String temp = String.valueOf(oriValues[i][k]);
								String regLike = cons[j][2].replaceAll("_", ".");
								regLike = cons[j][2].replaceAll("%", ".*");
								regLike = regLike.substring(1, regLike.length() - 1);
								// System.out.println(regLike);
								if (temp.matches(regLike)) {
									String c = String.valueOf(oriValues[i][0]);
									if (c.matches("\\s{0,}")) {
										oriValues[i][0] = "1";
										if (andSum == 1) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									} else {
										int a = Integer.parseInt(c);
										if (a != andSum)
											oriValues[i][0] = String.valueOf(++a);
										if (a == andSum) {
											oriValues[i][0] = "upd";
											updCount++;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		// for (int i = 0; i < oriValues.length; i++) {
		// for (int j = 0; j < oriValues[0].length; j++) {
		// System.out.print(oriValues[i][j] + " ");
		// }
		// System.out.println();
		// }
		Object[][] finalValues = new Object[oriValues.length - 1][oriValues[0].length - 1];
		for (int i = 1; i < oriValues.length; i++) {
			for (int j = 1; j < oriValues[0].length; j++) {
				if (oriValues[i][0].equals("upd")) {
					if (oriValues[0][j].equals(set[0]))
						finalValues[i - 1][j - 1] = set[2];
					else
						finalValues[i - 1][j - 1] = (Object) oriValues[i][j];
				} else
					finalValues[i - 1][j - 1] = (Object) oriValues[i][j];
			}
		}
		// for (int i = 0; i < finalValues.length; i++) {
		// for (int j = 0; j < finalValues[0].length; j++) {
		// System.out.print(finalValues[i][j] + " ");
		// }
		// System.out.println();
		// }

		createTableDBF(t);
		String errInfo = "";
		for (int i = 0; i < finalValues.length; i++) {
			if (!(errInfo = CheckData.CheckUpdate(tableName, finalValues[i])).matches("\\s{0,}")) {
				System.out.println(errInfo);
				break;
			}
		}
		// for (int i = 0; i < oriValues.length; i++) {
		// for (int j = 0; j < oriValues[i].length; j++) {
		// System.out.print(oriValues[i][j]+" ");
		// }
		// System.out.println();
		// }
		if (errInfo.matches("\\s{0,}"))
			System.out.println("�޸�" + (updCount) + "����¼��");
		else {
			createTableDBF(t);
			for (int i = 1; i < backValues.length; i++) {
				CheckData.CheckUpdate(tableName, backValues[i]);
			}
		}
	}
	//�洢���ݱ��������Ϣ
	public static void saveTable(String path, Table t) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
			oos.writeObject(t);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//ִ��create table���
	public static void createTable(Table t, String FKinfo) {
		String[][] cons = t.getCons();
		String tableName = t.getTableName();
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName;
		new File(path).mkdir();
		// �������Ϣ
		if (FKinfo != null) {
			saveFKInfo(FKinfo);
		}
		// ��out��Ϣ
		createTableDBF(t);
		Utils.saveTable(path + "\\" + tableName + ".out", t);
		System.out.println("��'" + tableName + "'�����ɹ���λ��" + path);

	}
	//��ȡȫ��������Ϣ
	public static ArrayList<Index> getIndex() {
		ArrayList<Index> list = null;
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + "index.out";
		ObjectInputStream ooi = null;
		try {
			ooi = new ObjectInputStream(new FileInputStream(new File(path)));
			list = (ArrayList<Index>) ooi.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ooi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	//��ʼ�������Ĵ���ļ�
	public static void saveIndex(String path) {
		ArrayList<Index> list = new ArrayList<Index>();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
			oos.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	//ִ��create index���
	public static void saveIndexInfo(Index index) {
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + "index.out";
		ArrayList<Index> list = getIndex();
		list.add(index);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
			oos.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//�������ݱ�ļ�¼�ļ�����dbf�ļ�
	public static void createTableDBF(Table t) {
		String[][] cons = t.getCons();
		String tableName = t.getTableName();
		String path = "���ݿ�\\" + MainSystem.currentDB + "\\��\\" + tableName;
		// �洢dbf�ļ�
		DBFWriter writer = null;
		String[] strutName = new String[cons.length - 1];
		DBFDataType[] strutType = new DBFDataType[cons.length - 1];
		Integer[] strutLength = new Integer[cons.length - 1];
		for (int i = 1; i < cons.length; i++) {
			strutName[i - 1] = cons[i][0];
			Pattern p = Pattern.compile("((?i)int|smallint|boolean|char\\(.*\\)|varchar\\(.*\\)|float\\(.*\\))");
			Matcher m = p.matcher(cons[i][1]);
			if (m.find()) {
				String type = m.group(1);
				if (cons[i][1].matches("((?i)(char|varchar|float)\\((.*)\\))?")) {
					Pattern pt = Pattern.compile("((?i)(char|varchar|float)\\((.*)\\))?");
					Matcher mt = pt.matcher(cons[i][1]);
					if (mt.find()) {
						strutLength[i - 1] = Integer.valueOf(mt.group(3));
						if (mt.group(2).matches("((?)char|varchar)"))
							strutType[i - 1] = DBFDataType.CHARACTER;
						else
							strutType[i - 1] = DBFDataType.DOUBLE;
					}
				} else {
					strutLength[i - 1] = 30;
					if (cons[i][1].matches("((?i)int|smallint)"))
						strutType[i - 1] = DBFDataType.NUMERIC;
					else if (cons[i][1].matches("((?i)boolean)"))
						strutType[i - 1] = DBFDataType.LOGICAL;
				}
			}

		}
		// for (int i = 0; i < strutType.length; i++) {
		// System.out.println(strutType[i]);
		// }
		try {
			writer = new DBFWriter(new FileOutputStream(path + "\\" + tableName + ".dbf"), Charset.forName("GBK"));
			int fieldCount = strutName.length;
			DBFField[] fields = new DBFField[fieldCount];
			for (int i = 0; i < fieldCount; i++) {
				fields[i] = new DBFField();
				fields[i].setName(strutName[i]);
				fields[i].setType(strutType[i]);
				fields[i].setLength(strutLength[i]);
			}
			writer.setFields(fields);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
}
