import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisSQL {
	public static String DBName = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
	public static String[] singleCol = new String[7];
	//解析SQL语句所属操作类型，并调用相应方法解析
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
				System.out.println("请先选择一个数据库！");
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
					System.out.println("请检查语法错误！");
			}
		} else
			System.out.println("请检查语法错误！");

	}
	//解析create database语句
	public static void createDB(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
			System.out.println("请检查语法错误！");
		}
	}
	//解析use database语句
	public static void useDB(String SQL) {
		String pattern = "((?i)use\\s{1,})" + DBName + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		if (m.find()) {
			String DataBaseName = m.group(2).trim();
			if (!Utils.isExistsDB(DataBaseName).matches("\\s{0,}")) {
				if (Utils.checkUseDB(DataBaseName)) {
					MainSystem.currentDB = DataBaseName;
					System.out.println("已切换到数据库'" + DataBaseName + "'");
				} else
					System.out.println("登录名'" + MainSystem.currentUser + "'对数据库'" + DataBaseName + "'没有任何权限！");
			} else
				System.out.println("数据库'" + DataBaseName + "'不存在！");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析创建用户的语句
	public static void createUser(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
			System.out.println("请检查语法错误！");
		}
	}
	//解析grant语句
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
			// 先检查表名
			for (int i = 0; i < tableNames.length; i++) {
				if (Utils.isExistsTable(tableNames[i]).matches("\\s{0,}")) {
					errInfo += "表'" + tableNames[i] + "'不存在！";
				}
			}
			// 再检查用户名
			if (errInfo.matches("\\s{0,}")) {
				if (CheckData.isExistsUser(userName).matches("\\s{0,}")) {
					errInfo += "登录名'" + userName + "'不存在！";
				}
				String reg = "((?i)all privileges|select|insert|delete|update)";
				// 检查权限
				if (errInfo.matches("\\s{0,}")) {
					if (author[0].trim().matches("((?i)all privileges)") && author.length == 1) {
						author[0] = "all";
					} else if (author[0].trim().matches("((?i)all privileges)") && author.length > 1) {
						errInfo += "已授予ALL权限，不需再加其他权限!";
					} else {
						for (int i = 0; i < author.length; i++) {
							if (!author[i].trim().matches(reg)) {
								errInfo = "不存在权限" + author[i];
								break;
							}
						}
					}
				}
				// 检查是否具备权限
				if (errInfo.matches("\\s{0,}")) {
					String whoGrant = MainSystem.currentUser;
					String localDB = MainSystem.currentDB;
					ArrayList<Authority> list = Utils.getDBAuthorInfo(localDB);
					int resultTable = 0;
					for (Authority i : list) {
						for (int j = 0; j < tableNames.length; j++) {
							// 当前登录名对某个表能授权
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
					// 如果全部符合
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
						String can = canGrant.equals("true") ? ",并且可以授予其他用户" : "!";
						System.out.println(
								"登录名'" + userName + "'对表'" + inputTable + "'" + "具有'" + finalAuthor + "'权限" + can);
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
			System.out.println("请检查语法错误！");
		}
	}
	//解析revoke语句
	public static void parseRevoke(String SQL) {
		// 一次撤销一个用户的一个权限或者全部权限
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
			// 先检查表名
			if (Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				errInfo += "表'" + tableName + "'不存在！";
			}
			// 再看用户名
			if (CheckData.isExistsUser(userName).matches("\\s{0,}")) {
				errInfo += "登录名'" + userName + "'不存在！";
			}
			String reg = "((?i)all privileges|select|insert|delete|update)";
			// 检查权限
			if (errInfo.matches("\\s{0,}")) {
				if (author.matches(reg)) {
					if (author.matches("((?i)all privileges)"))
						author = "all";
				} else
					errInfo += "不存在权限名'" + author + "'";
				// 再检查是否存在要撤销的权限
				if (errInfo.matches("\\s{0,}")) {
					if (Utils.checkAuthority(tableName, author)) {
						Utils.revokeAuthority(userName, tableName, author);
					} else {
						errInfo += "登录名'" + userName + "'本来就没有对表'" + tableName + "'的'" + author + "'的全部权限!";
					}
				}
			}
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析help database语句
	public static void printDB() {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
			return;
		}
		System.out.println("数据库名:" + MainSystem.currentDB);
		String path = "数据库\\" + MainSystem.currentDB;
		System.out.println("各表信息如下:");
		File[] table = new File(path + "\\表").listFiles();
		for (int i = 0; i < table.length; i++) {
			if (table[i].isDirectory()) {
				Utils.getTableOut(table[i].getName()).printOutFile();
			}
		}
		System.out.println("各视图信息如下:");
		File[] view = new File(path + "\\视图").listFiles();
		for (int i = 0; i < view.length; i++) {
			Utils.getView(view[i].getName().split("\\.")[0]).printOutFile();
		}
		System.out.println("各索引信息如下:");
		ArrayList<Index> list = Utils.getIndex();
		for (Index i : list) {
			i.printOutFile();
		}
	}
	//解析help table语句
	public static void printTable(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
				System.out.println("表'" + tableName + "'不存在！");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析help view语句
	public static void printView(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
				System.out.println("视图'" + viewName + "'不存在！");
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析help index语句
	public static void printIndex(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
				System.out.println("索引'" + indexName + "'不存在！");
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析create view语句
	public static void createView(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
				System.out.println("视图'" + viewName + "'已存在！");
			// for(int i=0;i<=m.groupCount();++i){
			// System.out.println(m.group(i));
			// }
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析create index语句
	public static void createIndex(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
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
			// 先看表名是否存在
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				String[][] finalCols = new String[col.length][2];
				// 再看每一列是否存在
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
						errInfo += "表'" + tableName + "'不存在列'" + temp + "'!";
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
							errInfo += "索引'" + indexName + "'已存在！";
							break;
						}
					}
				}
				// 如果都正确
				if (errInfo.matches("\\s{0,}")) {
					Index index = new Index(indexName, indexType, tableName, finalCols);
					System.out.println("索引'" + indexName + "'创建成功！");
					Utils.saveIndexInfo(index);
				}
			} else
				errInfo += "表名'" + tableName + "'不存在！";
		} else {
			errInfo += "请检查语法错误！";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//解析select语句
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
			// 看表名是否存在
			if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
				String[] cols = null;
				// 再看列名
				if (col.equals("*")) {
					cols = new String[1];
					cols[0] = "*";
				} else {
					cols = col.trim().split(",");
					String temp = "";
					for (int i = 0; i < cols.length; i++) {
						if (!Utils.isExistsCol(tableName, cols[i]).matches("\\s{0,}"))
							errInfo += "表'" + tableName + "'不存在列'" + cols[i] + "'!";
					}
				}
				// for (int i = 0; i < cols.length; i++) {
				// System.out.println(cols[i]);
				// }
				// where语句之前都正确
				if (errInfo.matches("\\s{0,}")) {
					// 如果没有where查询
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
							// 如果有or
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
							errInfo += "where存在语法错误！ ";
					}
				}
			} else
				errInfo += "表'" + tableName + "'不存在！ ";
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else {
			errInfo += "请检查语法错误！";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//解析delete语句
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
				// 如果没有where查询
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
						// 如果有or
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
						errInfo += "where存在语法错误！ ";
				}
			} else
				errInfo += "表'" + tableName + "'不存在！ ";
		} else {
			errInfo += "请检查语法错误！";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//解析update语句
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
				// 如果没有where查询
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
							errInfo += "表'" + tableName + "'不存在列'" + set[0] + "'!";
						// for (int i = 0; i <= m.groupCount(); ++i) {
						// System.out.println(m.group(i));
						// }
					} else
						errInfo += "set语句存在错误！";
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
								// 如果有or
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
								errInfo += "where存在语法错误！ ";
						} else
							errInfo += "表'" + tableName + "'不存在列'" + set[0] + "'!";
					} else
						errInfo += "set语句存在错误！";
				}
			} else
				errInfo += "表'" + tableName + "'不存在！ ";
		} else {
			errInfo += "请检查语法错误！";
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//解析检查where子句
	public static String parseWhere(String whereSQL, String tableName) {
		String errInfo = "";
		String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
		String regWhere = reg + "(.*)";
		Pattern p = Pattern.compile(regWhere);
		Matcher m = p.matcher(whereSQL);
		if (m.find()) {
			String col = m.group(1);
			if (!Utils.isExistsCol(tableName, col).matches("\\s{0,}"))
				errInfo += "表'" + tableName + "'不存在列'" + col + "'!";
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
		} else
			errInfo += "where存在语法错误！ ";
		return errInfo;
	}
	//解析insert语句
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
				// 查看列名是否存在
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
					// 如果带select子查询
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
							// 看表名是否存在
							if (!Utils.isExistsTable(tableName).matches("\\s{0,}")) {
								String[] cols = null;
								// 再看列名
								if (col.equals("*")) {
									cols = new String[1];
									cols[0] = "*";
								} else {
									cols = col.trim().split(",");
									String temp = "";
									for (int i = 0; i < cols.length; i++) {
										if (!Utils.isExistsCol(tableName, cols[i]).matches("\\s{0,}"))
											errInfo += "表'" + tableName + "'不存在列'" + cols[i] + "'!";
									}
								}
								// for (int i = 0; i < cols.length; i++) {
								// System.out.println(cols[i]);
								// }
								// where语句之前都正确
								if (errInfo.matches("\\s{0,}")) {
									// 如果没有where查询
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
											// 如果有or
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
											errInfo += "where存在语法错误！ ";
									}
								}
							} else
								errInfo += "表'" + tableName + "'不存在！ ";
							// for (int i = 0; i <= m.groupCount(); ++i) {
							// System.out.println(m.group(i));
							// }
						} else {
							errInfo += "请检查语法错误！";
						}
					}
				}
			} else
				errInfo += "表'" + table + "'不存在！";
			if (!errInfo.matches("\\s{0,}"))
				System.out.println(errInfo);
		} else {
			System.out.println("请检查语法错误！");
		}
	}
	//解析create table语句
	public static void createTable(String SQL) {
		if (!Utils.isDBA()) {
			System.out.println("对不起，该登录名不具备该权限！");
			return;
		}
		String errInfo = "";
		String pattern = "((?i)create\\s{1,}table\\s{1,})" + DBName + "\\s{0,}\\((.*)\\)" + "\\s{0,};";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(SQL);
		String errFK = "";
		if (m.find()) {
			String tableName = m.group(2).trim();
			// 先看表是否存在
			errInfo += Utils.isExistsTable(tableName);
			if (errInfo.matches("\\s{0,}")) {
				// 先看是否存在表级主键约束
				// 看是否存在外键约束
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
						// 判断列明
						errFK += Utils.isExistsCol(refTab, refCol);
					} else
						errFK = "参照表'" + refTab + "'不存在！";
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
						errInfo += "第" + String.valueOf(colNum + 1) + "列:" + temp;
					cons[i + 1] = singleCol;
					colNum++;
					singleCol = new String[7];
				}
				// for(int i=0;i<=m.groupCount();++i){
				// System.out.println(m.group(i));
				// }
				// System.out.println(errInfo+" "+errFK);
				// 记得把FK赋上
				if (FK != null) {
					boolean FKtemp = false;
					for (int i = 1; i < cons.length; i++) {
						if (cons[i][0].equals(FK)) {
							cons[i][3] = "FK";
							FKtemp = true;
						}
					}
					if (!FKtemp)
						errFK += "表'" + tableName + "'不存在列'" + FK + "'!";
				}
				if (errInfo.matches("\\s{0,}") && errFK.matches("\\s{0,}")) {
					Table table = new Table(tableName, cons);

					for (int i = 1; i < cons.length; i++) {
						for (int j = 0; j < cons[0].length; j++) {
							if (cons[i][j] == null)
								cons[i][j] = "";
						}
					}
					System.out.println("表名：" + tableName);
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
						System.out.println("外键信息:列'" + FK + "'参照表'" + refTab + "'中的列'" + refCol + "'");
					}
					Utils.createTable(table, FKinfo);
				} else {
					if (!errInfo.matches("\\s{0,}"))
						System.out.println(errInfo);
					if (!errFK.matches("\\s{0,}"))
						System.out.println(errFK);
				}
			} else
				System.out.println("表'" + tableName + "'已存在！");
		} else
			System.out.println("请检查语法错误！");

	}
	//解析建表时的属性信息
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
			// 先看是否有匹配到数据类型
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
							errInfo += "主键约束不能和null约束同时存在！";
					}
					if (m.group(3) != null) {
						if (m.group(4) != null)
							errInfo += "not null约束不能和null约束同时存在！";
					}
					singleCol[2] = m.group(1) != null ? "PK" : "";
					singleCol[4] = m.group(2) != null ? "unique" : "";
					singleCol[5] = m.group(3) != null ? m.group(3) : "";
					if (m.group(4) != null)
						singleCol[5] = "null";
					// check约束
					if (m.group(5) != null) {
						String check = m.group(5);
						String regCheck = "((?i)check\\((.*)\\))?";
						p = Pattern.compile(regCheck);
						m = p.matcher(check);
						if (m.find()) {
							String[] c = null;
							// 如果check里是between
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
				errInfo += "没有正确的数据类型！";
			}
		} else {
			errInfo += "没有正确的数据类型！";
		}
		return errInfo;
	}
}
