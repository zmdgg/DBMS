import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisSQL {
	public static String DBName = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
	public static String[] singleCol = new String[7];
	//����SQL��������������ͣ���������Ӧ��������
	public static void parseSQL(String SQL) {
		SQL = SQL.replaceAll("\\s{2,}", " ");
		if (SQL.matches("^((?i)create\\s{1,}database(.*))"))
			createDB(SQL);
		else if (SQL.matches("^((?i)create\\s{1,}(user|admin).*)"))
			createUser(SQL);
		else if (SQL.matches("^((?i)use(.*))"))
			useDB(SQL);
		else if (SQL.matches("^((?i)create|insert|delete|update|select|help|grant|revoke)(.*)")) {
			if (MainSystem.currentDB == null)
				System.out.println("����ѡ��һ�����ݿ⣡");
			else {
				if (SQL.matches("^((?i)create\\s{1,}table(.*))"))
					createTable(SQL);
				else if (SQL.matches("^((?i)create\\s{1,}view.*)"))
					createView(SQL);
				else if (SQL.matches("^((?i)create(.*)index.*)"))
					createIndex(SQL);
				else if (SQL.matches("^((?i)insert\\s{1,}into(.*))"))
					insertTable(SQL);
				else if (SQL.matches("^((?i)delete(.*))"))
					deleteData(SQL);
				else if (SQL.matches("^((?i)update(.*))"))
					updateData(SQL);
				else if (SQL.matches("^((?i)select(.*))"))
					selectData(SQL);
				else if (SQL.matches("^((?i)help\\s{1,}database;)"))
					printDB();
				else if (SQL.matches("^((?i)help\\s{1,}table(.*))"))
					printTable(SQL);
				else if (SQL.matches("^((?i)help\\s{1,}view.*)"))
					printView(SQL);
				else if (SQL.matches("^((?i)help(.*)index.*)"))
					printIndex(SQL);
				else if (SQL.matches("^((?i)grant.*)"))
					parseGrant(SQL);
				else if (SQL.matches("^((?i)revoke.*)"))
					parseRevoke(SQL);
				else
					System.out.println("�����﷨����");
			}
		} else
			System.out.println("�����﷨����");

	}
	//����create database���
	public static void createDB(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)create\\s{1,}database\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String DBName = m.group(2).trim();
			Utils.createDB(DBName);
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����use database���
	public static void useDB(String SQL) {
		String pattern = "((?i)use\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String DataBaseName = m.group(2).trim();
			if (!Utils.isExistsDB(DataBaseName).matches("\\s{0,}")) {
				if (Utils.checkUseDB(DataBaseName)) {
					MainSystem.currentDB = DataBaseName;
					System.out.println("���л������ݿ�'" + DataBaseName + "'");
				} else
					System.out.println("��¼��'" + MainSystem.currentUser + "'�����ݿ�'" + DataBaseName + "'û���κ�Ȩ�ޣ�");
			} else
				System.out.println("���ݿ�'" + DataBaseName + "'�����ڣ�");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//���������û������
	public static void createUser(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)create\\s{1,}(user|admin)\\s{1,})" + DBName + "(\\s{1,}(?i)and\\s{1,})"
				+ "((?i)password\\s{0,}=\\s{0,})" + "'(.*)'" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String[] values = new String[3];
			values[0] = m.group(3).trim();
			values[1] = m.group(6).trim();
			values[2] = (m.group(2).trim().equalsIgnoreCase("admin")) ? "DBA" : "user";
			CheckData.CheckUser("userInfo", values);
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����grant���
	public static void parseGrant(String SQL) {
		String errInfo = "";
		String pattern = "((?i)grant\\s{1,})" + "(.*)" + "((?i)\\s{1,}on\\s{1,})" + "((?i)table\\s{1,})" + "(.*)"
				+ "((?i)to\\s{1,})" + DBName + "((?i)\\s{0,}with grant option)?" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String[] author = m.group(2).trim().split(",");
			String[] tableNames = m.group(5).trim().split(",");
			String ObjectType = "table";
			String userName = m.group(7).trim();
			String canGrant = (m.group(8) != null) ? "true" : "false";
			// �ȼ�����
			for (int i = 0; i < tableNames.length; i++) {
				if (Utils.isExistsTable(tableNames[i]).matches("\\s{0,}")) {
					errInfo += "��'" + tableNames[i] + "'�����ڣ�";
				}
			}
			// �ټ���û���
			if (errInfo.matches("\\s{0,}")) {
				if (CheckData.isExistsUser(userName).matches("\\s{0,}")) {
					errInfo += "��¼��'" + userName + "'�����ڣ�";
				}
				String reg = "((?i)all privileges|select|insert|delete|update)";
				// ���Ȩ��
				if (errInfo.matches("\\s{0,}")) {
					if (author[0].trim().matches("((?i)all privileges)") && author.length == 1) {
						author[0] = "all";
					} else if (author[0].trim().matches("((?i)all privileges)") && author.length > 1) {
						errInfo += "������ALLȨ�ޣ������ټ�����Ȩ��!";
					} else {
						for (int i = 0; i < author.length; i++) {
							if (!author[i].trim().matches(reg)) {
								errInfo = "������Ȩ��" + author[i];
								break;
							}
						}
					}
				}
				// ����Ƿ�߱�Ȩ��
				if (errInfo.matches("\\s{0,}")) {
					String whoGrant = MainSystem.currentUser;
					String localDB = MainSystem.currentDB;
					ArrayList<Authority> list = Utils.getDBAuthorInfo(localDB);
					int resultTable = 0;
					for (Authority i : list) {
						for (int j = 0; j < tableNames.length; j++) {
							// ��ǰ��¼����ĳ��������Ȩ
							if ((i.getUserName().equals(whoGrant) && i.getCanGrant().equals("true")
									&& i.getObjectName().equals(tableNames[j]))
									|| i.getUserName().equals(whoGrant) && i.getObjectName().equalsIgnoreCase("ALL")) {
								if (i.getAuthority().equalsIgnoreCase("all")) {
									++resultTable;
								} else {
									String[] canAuthority = i.getAuthority().split(",");
									int temp = 0;
									for (int k = 0; k < author.length; k++) {
										for (int l = 0; l < author.length; l++) {
											if (canAuthority[k].equalsIgnoreCase(author[k])) {
												++temp;
											}
										}
									}
									if (temp == author.length)
										++resultTable;
								}
							}
						}

					}
					// ���ȫ������
					if (resultTable == tableNames.length) {
						String finalAuthor = "";
						for (int i = 0; i < author.length; i++) {
							finalAuthor += author[i] + ",";
						}
						finalAuthor = finalAuthor.substring(0, finalAuthor.length() - 1);
						String inputTable = "";
						for (int i = 0; i < tableNames.length; i++) {
							inputTable += tableNames[i] + ",";
						}
						inputTable = inputTable.substring(0, inputTable.length() - 1);
						String can = canGrant.equals("true") ? ",���ҿ������������û�" : "!";
						System.out.println(
								"��¼��'" + userName + "'�Ա�'" + inputTable + "'" + "����'" + finalAuthor + "'Ȩ��" + can);
						for (int i = 0; i < tableNames.length; i++) {
							Authority a = new Authority(userName, ObjectType, tableNames[i], finalAuthor, canGrant,
									whoGrant);
							Utils.saveAuthorityInfo(localDB, a);
						}
					}
				}
			}
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����revoke���
	public static void parseRevoke(String SQL) {
		// һ�γ���һ���û���һ��Ȩ�޻���ȫ��Ȩ��
		String errInfo = "";
		String pattern = "((?i)revoke\\s{1,})" + "(.*)" + "((?i)\\s{1,}on\\s{1,})" + "((?i)table\\s{1,})" + "(.*)"
				+ "((?i)from\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String localUser = MainSystem.currentUser;
			String localDB = MainSystem.currentDB;
			String author = m.group(2).trim();
			String tableName = m.group(5).trim();
			String userName = m.group(7).trim();
			// �ȼ�����
			if (Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				errInfo += "��'" + tableName + "'�����ڣ�";
			}
			// �ٿ��û���
			if (CheckData.isExistsUser(userName).matches("\\s{0,}")) {
				errInfo += "��¼��'" + userName + "'�����ڣ�";
			}
			String reg = "((?i)all privileges|select|insert|delete|update)";
			// ���Ȩ��
			if (errInfo.matches("\\s{0,}")) {
				if (author.matches(reg)) {
					if (author.matches("((?i)all privileges)"))
						author = "all";
				} else
					errInfo += "������Ȩ����'" + author + "'";
				// �ټ���Ƿ����Ҫ������Ȩ��
				if (errInfo.matches("\\s{0,}")) {
					if (Utils.checkAuthority(tableName, author)) {
						Utils.revokeAuthority(userName, tableName, author);
					} else {
						errInfo += "��¼��'" + userName + "'������û�жԱ�'" + tableName + "'��'" + author + "'��ȫ��Ȩ��!";
					}
				}
			}
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����help database���
	public static void printDB() {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		System.out.println("���ݿ���:" + MainSystem.currentDB);
		String path = "���ݿ�\\" + MainSystem.currentDB;
		System.out.println("������Ϣ����:");
		File[] table = new File(path + "\\��").listFiles();
		for (int i = 0; i < table.length; i++) {
			if (table[i].isDirectory()) {
				Utils.getTableOut(table[i].getName()).printOutFile();
			}
		}
		System.out.println("����ͼ��Ϣ����:");
		File[] view = new File(path + "\\��ͼ").listFiles();
		for (int i = 0; i < view.length; i++) {
			Utils.getView(view[i].getName().split("\\.")[0]).printOutFile();
		}
		System.out.println("��������Ϣ����:");
		ArrayList<Index> list = Utils.getIndex();
		for (Index i : list) {
			i.printOutFile();
		}
	}
	//����help table���
	public static void printTable(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)help\\s{1,}table\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String tableName = m.group(2).trim();
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				Utils.getTableOut(tableName).printOutFile();
			} else
				System.out.println("��'" + tableName + "'�����ڣ�");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����help view���
	public static void printView(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)help\\s{1,}view\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String viewName = m.group(2).trim();
			if (!Utils.isExistsView(viewName).matches("\\s{0,}")) {
				Utils.getView(viewName).printOutFile();
			} else
				System.out.println("��ͼ'" + viewName + "'�����ڣ�");
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����help index���
	public static void printIndex(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)help\\s{1,}index\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String indexName = m.group(2).trim();
			Index index = null;
			ArrayList<Index> list = Utils.getIndex();
			for (Index i : list) {
				if (i.getIndexName().equals(indexName)) {
					index = i;
					break;
				}
			}
			if (index != null) {
				index.printOutFile();
			} else
				System.out.println("����'" + indexName + "'�����ڣ�");
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����create view���
	public static void createView(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String pattern = "((?i)create\\s{1,}view\\s{1,})" + DBName + "(\\s{1,}(?i)as\\s{1,})" + "(.*)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String viewName = m.group(2).trim();
			if (Utils.isExistsView(viewName).matches("\\s{0,}")) {
				String defSQL = "";
				for (int i = 1; i <= m.groupCount(); ++i) {
					defSQL += m.group(i).trim() + " ";
				}
				defSQL = defSQL.trim();
				View v = new View(viewName, defSQL);
				Utils.saveView(v);
			} else
				System.out.println("��ͼ'" + viewName + "'�Ѵ��ڣ�");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����create index���
	public static void createIndex(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String errInfo = "";
		String pattern = "((?i)create\\s{1,}(unique|cluster)\\s{1,}index\\s{1,})" + DBName + "(\\s{1,}(?i)on\\s{1,})"
				+ DBName + "\\((.*)\\)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
			String indexName = m.group(3).trim();
			String indexType = m.group(2).trim();
			String tableName = m.group(5).trim();
			String[] col = m.group(6).trim().split(",");
			// �ȿ������Ƿ����
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				String[][] finalCols = new String[col.length][2];
				// �ٿ�ÿһ���Ƿ����
				for (int i = 0; i < col.length; i++) {
					String order = "";
					if (col[i].split("\\s{1,}").length > 1) {
						order = col[i].split("\\s{1,}")[1];
					} else {
						order = "ASC";
					}
					String temp = col[i].split("\\s{1,}")[0];
					// System.out.println(temp +" "+order);
					finalCols[i][0] = temp;
					finalCols[i][1] = order;
					if (!Utils.isExistsCol(tableName, temp).matches("\\s{0,}"))
						errInfo += "��'" + tableName + "'��������'" + temp + "'!";
				}
				// for (int i = 0; i < finalCols.length; i++) {
				// for (int j = 0; j < finalCols[i].length; j++) {
				// System.out.println(finalCols[i][j]);
				// }
				// }
				if (errInfo.matches("\\s{0,}")) {
					ArrayList<Index> list = Utils.getIndex();
					for (Index i : list) {
						if (i.getIndexName().equals(indexName)) {
							errInfo += "����'" + indexName + "'�Ѵ��ڣ�";
							break;
						}
					}
				}
				// �������ȷ
				if (errInfo.matches("\\s{0,}")) {
					Index index = new Index(indexName, indexType, tableName, finalCols);
					System.out.println("����'" + indexName + "'�����ɹ���");
					Utils.saveIndexInfo(index);
				}
			} else
				errInfo += "����'" + tableName + "'�����ڣ�";
		} else {
			errInfo += "�����﷨����";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//����select���
	public static void selectData(String SQL) {
		String errInfo = "";
		String reg = "\\s{0,}([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String pattern = "((?i)select\\s{1,}(.*)\\s{1,}from)" + reg + "(.*)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String tableName = m.group(3).trim();
			String col = m.group(2).trim();
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			// �������Ƿ����
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				String[] cols = null;
				// �ٿ�����
				if (col.equals("*")) {
					cols = new String[1];
					cols[0] = "*";
				} else {
					cols = col.trim().split(",");
					String temp = "";
					for (int i = 0; i < cols.length; i++) {
						if (!Utils.isExistsCol(tableName, cols[i]).matches("\\s{0,}"))
							errInfo += "��'" + tableName + "'��������'" + cols[i] + "'!";
					}
				}
				// for (int i = 0; i < cols.length; i++) {
				// System.out.println(cols[i]);
				// }
				// where���֮ǰ����ȷ
				if (errInfo.matches("\\s{0,}")) {
					// ���û��where��ѯ
					if (m.group(4).matches("\\s{0,}")) {
						String[] cons = new String[1];
						cons[0] = "*";
						Utils.selectPartialData(tableName, cols, cons, false);
					} else {
						String where = m.group(4).trim();
						// System.out.println(where);
						String regWhere = "((?i)where\\s{1,})" + "(.*)";
						p = Pattern.compile(regWhere);
						m = p.matcher(where);
						if (m.find()) {
							// for (int i = 0; i <= m.groupCount(); ++i) {
							// System.out.println(m.group(i));
							// }
							String whereSQL = m.group(2).trim();
							// System.out.println(whereSQL);
							// �����or
							if (whereSQL.split("((?i)or)").length > 1) {
								String[] cons = whereSQL.split("((?i)or)");
								for (int i = 0; i < cons.length; i++) {
									errInfo += parseWhere(cons[i], tableName);
								}
								if (errInfo.matches("\\s{0,}")) {
									Utils.selectPartialData(tableName, cols, cons, false);
								}
							} else {
								if (whereSQL.split("((?i)between)").length > 1) {
									String[] cons = new String[1];
									cons[0] = whereSQL;
									for (int i = 0; i < cons.length; i++) {
										errInfo += parseWhere(cons[i], tableName);
									}
									if (errInfo.matches("\\s{0,}")) {
										Utils.selectPartialData(tableName, cols, cons, false);
									}
								} else {
									String[] cons = whereSQL.split("((?i)and)");
									for (int i = 0; i < cons.length; i++) {
										errInfo += parseWhere(cons[i], tableName);
									}
									if (errInfo.matches("\\s{0,}")) {
										Utils.selectPartialData(tableName, cols, cons, true);
									}
								}
							}
						} else
							errInfo += "where�����﷨���� ";
					}
				}
			} else
				errInfo += "��'" + tableName + "'�����ڣ� ";
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			errInfo += "�����﷨����";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//����delete���
	public static void deleteData(String SQL) {
		String errInfo = "";
		String reg = "\\s{1,}([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String pattern = "((?i)delete\\s{1,}from)" + reg + "(.*)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			String tableName = m.group(2).trim();
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				// ���û��where��ѯ
				if (m.group(3).matches("\\s{0,}")) {
					Utils.deleteAllData(tableName);
				} else {
					String where = m.group(3).trim();
					String regWhere = "((?i)where\\s{1,})" + "(.*)";
					p = Pattern.compile(regWhere);
					m = p.matcher(where);
					if (m.find()) {
						String whereSQL = m.group(2).trim();
						// System.out.println(whereSQL);
						// �����or
						if (whereSQL.split("((?i)or)").length > 1) {
							String[] cons = whereSQL.split("((?i)or)");
							for (int i = 0; i < cons.length; i++) {
								errInfo += parseWhere(cons[i], tableName);
							}
							if (errInfo.matches("\\s{0,}")) {
								Utils.deletePartialData(tableName, cons, false);
							}
						} else {
							if (whereSQL.split("((?i)between)").length > 1) {
								String[] cons = new String[1];
								cons[0] = whereSQL;
								for (int i = 0; i < cons.length; i++) {
									errInfo += parseWhere(cons[i], tableName);
								}
								if (errInfo.matches("\\s{0,}")) {
									Utils.deletePartialData(tableName, cons, false);
								}
							} else {
								String[] cons = whereSQL.split("((?i)and)");
								for (int i = 0; i < cons.length; i++) {
									errInfo += parseWhere(cons[i], tableName);
								}
								if (errInfo.matches("\\s{0,}")) {
									Utils.deletePartialData(tableName, cons, true);
								}
							}
						}
					} else
						errInfo += "where�����﷨���� ";
				}
			} else
				errInfo += "��'" + tableName + "'�����ڣ� ";
		} else {
			errInfo += "�����﷨����";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//����update���
	public static void updateData(String SQL) {
		String errInfo = "";
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String pattern = "((?i)update\\s{1,})" + reg + "\\s{0,}((?i)set)" + "(.*)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			String tableName = m.group(2).trim();
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				// ���û��where��ѯ
				if (m.group(4).split("((?i)where)").length <= 1) {
					// Utils.deleteAllData(tableName);
					String setSQL = m.group(4).trim();
					String[] set = new String[3];
					String regset = reg + "\\s{0,}(=)\\s{0,}" + "'(.*)'";
					p = Pattern.compile(regset);
					m = p.matcher(SQL);
					if (m.find()) {
						set[0] = m.group(1);
						set[1] = m.group(2);
						set[2] = m.group(3);
						if (Utils.isExistsCol(tableName, set[0]).matches("\\s{0,}")) {
							Utils.updateAllData(tableName, set);
						} else
							errInfo += "��'" + tableName + "'��������'" + set[0] + "'!";
						// for (int i = 0; i <= m.groupCount(); ++i) {
						// System.out.println(m.group(i));
						// }
					} else
						errInfo += "set�����ڴ���";
				} else {
					String setSQL = m.group(4).split("((?i)where)")[0].trim();
					String where = "where " + m.group(4).split("((?i)where)")[1].trim();
					String regWhere = "((?i)where\\s{1,})" + "(.*)";
					String[] set = new String[3];
					String regset = reg + "\\s{0,}(=)\\s{0,}" + "'(.*)'";
					p = Pattern.compile(regset);
					m = p.matcher(setSQL);
					if (m.find()) {
						set[0] = m.group(1);
						set[1] = m.group(2);
						set[2] = m.group(3);
						if (Utils.isExistsCol(tableName, set[0]).matches("\\s{0,}")) {
							p = Pattern.compile(regWhere);
							m = p.matcher(where);
							if (m.find()) {
								String whereSQL = m.group(2).trim();
								// System.out.println(whereSQL);
								// �����or
								if (whereSQL.split("((?i)or)").length > 1) {
									String[] cons = whereSQL.split("((?i)or)");
									for (int i = 0; i < cons.length; i++) {
										errInfo += parseWhere(cons[i], tableName);
									}
									if (errInfo.matches("\\s{0,}")) {
										Utils.updatePartialData(tableName, set, cons, false);
									}
								} else {
									if (whereSQL.split("((?i)between)").length > 1) {
										String[] cons = new String[1];
										cons[0] = whereSQL;
										for (int i = 0; i < cons.length; i++) {
											errInfo += parseWhere(cons[i], tableName);
										}
										if (errInfo.matches("\\s{0,}")) {
											Utils.updatePartialData(tableName, set, cons, false);
										}
									} else {
										String[] cons = whereSQL.split("((?i)and)");
										for (int i = 0; i < cons.length; i++) {
											errInfo += parseWhere(cons[i], tableName);
										}
										if (errInfo.matches("\\s{0,}")) {
											Utils.updatePartialData(tableName, set, cons, true);
										}
									}
								}
							} else
								errInfo += "where�����﷨���� ";
						} else
							errInfo += "��'" + tableName + "'��������'" + set[0] + "'!";
					} else
						errInfo += "set�����ڴ���";
				}
			} else
				errInfo += "��'" + tableName + "'�����ڣ� ";
		} else {
			errInfo += "�����﷨����";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//�������where�Ӿ�
	public static String parseWhere(String whereSQL, String tableName) {
		String errInfo = "";
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String regWhere = reg + "(.*)";
		Pattern p = Pattern.compile(regWhere);
		Matcher m = p.matcher(whereSQL);
		if (m.find()) {
			String col = m.group(1);
			if (!Utils.isExistsCol(tableName, col).matches("\\s{0,}"))
				errInfo += "��'" + tableName + "'��������'" + col + "'!";
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else
			errInfo += "where�����﷨���� ";
		return errInfo;
	}
	//����insert���
	public static void insertTable(String SQL) {
		String errInfo = "";
		String pattern = "((?i)insert\\s{1,}into\\s{0,})" + DBName + "(.*)" + "((?i)values\\((.*)\\)|select(.*))"
				+ "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			String table = m.group(2).trim();
			errInfo += Utils.isExistsTable(table);
			String colstemp = m.group(3).trim();
			String sql = m.group(4).trim();
			String[] column = null;
			if (!errInfo.matches("\\s{0,}")) {
				errInfo = "";
				// �鿴�����Ƿ����
				if (!colstemp.matches("\\s{0,}")) {
					column = colstemp.substring(1, colstemp.length() - 1).split(",");
					for (int i = 0; i < column.length; i++) {
						errInfo += Utils.isExistsCol(table, column[i]);
					}
				}
				if (errInfo.matches("\\s{0,}")) {
					if (sql.matches("((?i)values(.*))")) {
						String[] temp = m.group(5).trim().split(",");
						String[] values = new String[temp.length];
						for (int i = 0; i < temp.length; i++) {
							values[i] = temp[i].trim().substring(1, temp[i].length() - 1);
						}
						// for (int i = 0; i < values.length; i++) {
						// System.out.println(values[i]);
						// }
						CheckData.CheckInsert(table, column, values);
					}
					// �����select�Ӳ�ѯ
					else if (sql.matches("((?i)select(.*))")) {
						String reg = "\\s{0,}([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
						String sel = "((?i)select\\s{1,}(.*)\\s{1,}from)" + reg + "(.*)" + "\\s{0,};";
						p = Pattern.compile(sel);
						m = p.matcher(SQL);
						if (m.find()) {
							String tableName = m.group(3).trim();
							String col = m.group(2).trim();
							// for (int i = 0; i <= m.groupCount(); ++i) {
							// System.out.println(m.group(i));
							// }
							// �������Ƿ����
							if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
								String[] cols = null;
								// �ٿ�����
								if (col.equals("*")) {
									cols = new String[1];
									cols[0] = "*";
								} else {
									cols = col.trim().split(",");
									String temp = "";
									for (int i = 0; i < cols.length; i++) {
										if (!Utils.isExistsCol(tableName, cols[i]).matches("\\s{0,}"))
											errInfo += "��'" + tableName + "'��������'" + cols[i] + "'!";
									}
								}
								// for (int i = 0; i < cols.length; i++) {
								// System.out.println(cols[i]);
								// }
								// where���֮ǰ����ȷ
								if (errInfo.matches("\\s{0,}")) {
									// ���û��where��ѯ
									if (m.group(4).matches("\\s{0,}")) {
										String[] cons = new String[1];
										cons[0] = "*";
										CheckData.CheckInsertSel(table, column, tableName, cols, cons, false);
									} else {
										String where = m.group(4).trim();
										// System.out.println(where);
										String regWhere = "((?i)where\\s{1,})" + "(.*)";
										p = Pattern.compile(regWhere);
										m = p.matcher(where);
										if (m.find()) {
											// for (int i = 0; i <=
											// m.groupCount(); ++i) {
											// System.out.println(m.group(i));
											// }
											String whereSQL = m.group(2).trim();
											// System.out.println(whereSQL);
											// �����or
											if (whereSQL.split("((?i)or)").length > 1) {
												String[] cons = whereSQL.split("((?i)or)");
												for (int i = 0; i < cons.length; i++) {
													errInfo += parseWhere(cons[i], tableName);
												}
												if (errInfo.matches("\\s{0,}")) {
													CheckData.CheckInsertSel(table, column, tableName, cols, cons,
															false);
												}
											} else {
												if (whereSQL.split("((?i)between)").length > 1) {
													String[] cons = new String[1];
													cons[0] = whereSQL;
													for (int i = 0; i < cons.length; i++) {
														errInfo += parseWhere(cons[i], tableName);
													}
													if (errInfo.matches("\\s{0,}")) {
														CheckData.CheckInsertSel(table, column, tableName, cols, cons,
																false);
													}
												} else {
													String[] cons = whereSQL.split("((?i)and)");
													for (int i = 0; i < cons.length; i++) {
														errInfo += parseWhere(cons[i], tableName);
													}
													if (errInfo.matches("\\s{0,}")) {
														CheckData.CheckInsertSel(table, column, tableName, cols, cons,
																false);
													}
												}
											}
										} else
											errInfo += "where�����﷨���� ";
									}
								}
							} else
								errInfo += "��'" + tableName + "'�����ڣ� ";
							// for (int i = 0; i <= m.groupCount(); ++i) {
							// System.out.println(m.group(i));
							// }
						} else {
							errInfo += "�����﷨����";
						}
					}
				}
			} else
				errInfo += "��'" + table + "'�����ڣ�";
			if (!errInfo.matches("\\s{0,}"))
				System.out.println(errInfo);
		} else {
			System.out.println("�����﷨����");
		}
	}
	//����create table���
	public static void createTable(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("�Բ��𣬸õ�¼�����߱���Ȩ�ޣ�");
			return;
		}
		String errInfo = "";
		String pattern = "((?i)create\\s{1,}table\\s{1,})" + DBName + "\\s{0,}\\((.*)\\)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		String errFK = "";
		if (m.find()) {
			String tableName = m.group(2).trim();
			// �ȿ����Ƿ����
			errInfo += Utils.isExistsTable(tableName);
			if (errInfo.matches("\\s{0,}")) {
				// �ȿ��Ƿ���ڱ�����Լ��
				// ���Ƿ�������Լ��
				String regcons = "((?i)foreign\\s{1,}key\\s{1,})" + "\\(" + DBName + "\\)" + "\\s{1,}references\\s{1,}"
						+ DBName + "\\((.*)\\)";
				String colSQLTemp = m.group(3).trim();
				Pattern pt = Pattern.compile(regcons);
				Matcher mt = pt.matcher(colSQLTemp);
				String FK = null;
				String refTab = "";
				String refCol = "";
				String[][] cons = null;
				if (mt.find()) {
					// for (int i = 0; i <= mt.groupCount(); ++i) {
					// System.out.println(mt.group(i));
					// }
					FK = mt.group(2).trim();
					refTab = mt.group(3).trim();
					refCol = mt.group(4).trim();
					errFK += Utils.isExistsTable(refTab);
					if (!errFK.matches("\\s{0,}")) {
						errFK = "";
						// �ж�����
						errFK += Utils.isExistsCol(refTab, refCol);
					} else
						errFK = "���ձ�'" + refTab + "'�����ڣ�";
					colSQLTemp = colSQLTemp.replaceAll(regcons, " FK ");
				}
				String[] colSQL = colSQLTemp.split(",");
				if (FK != null)
					cons = new String[colSQL.length][7];
				else
					cons = new String[colSQL.length + 1][7];
				String[] t = { "", "type", "PK", "FK", "unique", "canNull", "check" };
				cons[0] = t;
				int colNum = 0;
				for (int i = 0; i < colSQL.length; i++) {
					if (colSQL[i].trim().matches("\\s{0,}FK\\s{0,}")) {
						break;
					}
					String temp = isRight(colSQL[i].trim());
					if (!temp.matches("\\s{0,}"))
						errInfo += "��" + String.valueOf(colNum + 1) + "��:" + temp;
					cons[i + 1] = singleCol;
					colNum++;
					singleCol = new String[7];
				}
				// for(int i=0;i<=m.groupCount();++i){
				// System.out.println(m.group(i));
				// }
				// System.out.println(errInfo+" "+errFK);
				// �ǵð�FK����
				if (FK != null) {
					boolean FKtemp = false;
					for (int i = 1; i < cons.length; i++) {
						if (cons[i][0].equals(FK)) {
							cons[i][3] = "FK";
							FKtemp = true;
						}
					}
					if (!FKtemp)
						errFK += "��'" + tableName + "'��������'" + FK + "'!";
				}
				if (errInfo.matches("\\s{0,}") && errFK.matches("\\s{0,}")) {
					Table table = new Table(tableName, cons);

					for (int i = 1; i < cons.length; i++) {
						for (int j = 0; j < cons[0].length; j++) {
							if (cons[i][j] == null)
								cons[i][j] = "";
						}
					}
					System.out.println("������" + tableName);
					for (int i = 1; i < cons.length; i++) {
						for (int j = 0; j < cons[0].length; j++) {
							if (!cons[i][j].matches("\\s{0,}"))
								System.out.print(cons[i][j] + " ");
						}
						if (i != cons.length)
							System.out.println();
					}
					String FKinfo = null;
					if (FK != null) {
						FKinfo = tableName + "|" + FK + "|" + refTab + "|" + refCol;
						System.out.println("�����Ϣ:��'" + FK + "'���ձ�'" + refTab + "'�е���'" + refCol + "'");
					}
					Utils.createTable(table, FKinfo);
				} else {
					if (!errInfo.matches("\\s{0,}"))
						System.out.println(errInfo);
					if (!errFK.matches("\\s{0,}"))
						System.out.println(errFK);
				}
			} else
				System.out.println("��'" + tableName + "'�Ѵ��ڣ�");
		} else
			System.out.println("�����﷨����");

	}
	//��������ʱ��������Ϣ
	public static String isRight(String col) {
		String errInfo = "";
		col += " ";
		String pattern = DBName
				+ "\\s{1,}((?i)int\\s{1,}|smallint\\s{1,}|boolean\\s{1,}|char\\(\\d{1,}\\)\\s{1,}|varchar\\(\\d{1,}\\)\\s{1,}|float\\(\\d{1,}\\)\\s{1,})?"
				+ "(.*)";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(col);
		if (m.find()) {
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			// �ȿ��Ƿ���ƥ�䵽��������
			if (m.group(2) != null) {
				singleCol[0] = m.group(1).trim();
				singleCol[1] = m.group(2).trim();
				String regcons = "((?i)primary key)?((?i)unique)?((?i)not null)?((?i)null)?((?i)check\\(.*\\))?";
				p = Pattern.compile(regcons);
				m = p.matcher(m.group(3));
				if (m.find()) {
					// for (int i = 0; i <= m.groupCount(); ++i) {
					// System.out.println(m.group(i));
					// }
					if (m.group(1) != null) {
						if (m.group(4) != null)
							errInfo += "����Լ�����ܺ�nullԼ��ͬʱ���ڣ�";
					}
					if (m.group(3) != null) {
						if (m.group(4) != null)
							errInfo += "not nullԼ�����ܺ�nullԼ��ͬʱ���ڣ�";
					}
					singleCol[2] = m.group(1) != null ? "PK" : "";
					singleCol[4] = m.group(2) != null ? "unique" : "";
					singleCol[5] = m.group(3) != null ? m.group(3) : "";
					if (m.group(4) != null)
						singleCol[5] = "null";
					// checkԼ��
					if (m.group(5) != null) {
						String check = m.group(5);
						String regCheck = "((?i)check\\((.*)\\))?";
						p = Pattern.compile(regCheck);
						m = p.matcher(check);
						if (m.find()) {
							String[] c = null;
							// ���check����between
							if ((c = m.group(2).split("((?i)between)")).length > 1) {
								String[] temp = c[1].trim().split("((?i)\\s{0,}and\\s{0,})");
								singleCol[6] = "bet,";
								for (int i = 0; i < temp.length; i++) {
									singleCol[6] += temp[i] + ",";
								}
								singleCol[6] = singleCol[6].substring(0, singleCol[6].length() - 1);
							} else if ((c = m.group(2).split("((?i)or)")).length > 1) {
								String[] temp = new String[c.length];
								for (int i = 0; i < c.length; i++) {
									temp[i] = c[i].trim().split("\\s{0,}=\\s{0,}")[1];
									temp[i] = temp[i].trim().substring(1, temp[i].length() - 1);
								}
								singleCol[6] = "or,";
								for (int i = 0; i < temp.length; i++) {
									singleCol[6] += temp[i] + ",";
								}
								singleCol[6] = singleCol[6].substring(0, singleCol[6].length() - 1);
							}
							// for (int i = 0; i <= m.groupCount(); i++) {
							// System.out.println(m.group(i));
							// }
						}
					} else
						singleCol[6] = "";
				}
			} else {
				errInfo += "û����ȷ���������ͣ�";
			}
		} else {
			errInfo += "û����ȷ���������ͣ�";
		}
		return errInfo;
	}
}
