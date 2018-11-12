import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckData {
	//检查用户名是否存在
	public static String isExistsUser(String userName) {
		String errInfo = "";
		Object[][] datas = Utils.getUserData("userInfo");
		// 判断用户名是否存在
		if (!isUnique(userName, datas[0])) {
			errInfo += "登录名'" + userName + "'已存在!";
		}
		return errInfo;
	}
	//检查创建用户时，用户信息是否符合规范
	public static void CheckUser(String tableName, String[] values) {
		String errInfo = "";
		String[][] cons = Utils.getUserInfo(tableName);
		Object[][] datas = Utils.getUserData(tableName);
		if (!isNotNull(values[0])) {
			errInfo += "登录名不能为空！";
		}
		if (!isNotNull(values[1])) {
			errInfo += "密码不能为空！";
		}
		if (!isNotNull(values[2])) {
			errInfo += "权限不能为空！";
		}
		// 判断用户名是否存在
		if (!isUnique(values[0], datas[0])) {
			errInfo += "登录名'" + values[0] + "'已存在!";
		}
		if (errInfo.matches("\\s{0,}")) {
			Utils.saveUserData(tableName, values);
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//检查insert语句中的新纪录是否符合属性约束
	public static void CheckInsert(String tableName, String[] cols, String[] values) {
		String errInfo = "";
		Table t = Utils.getTableOut(tableName);
		String[][] cons = t.getCons();
		String[] finalValues = new String[cons.length - 1];
		String[] finalCol = new String[cons.length - 1];
		for (int i = 1; i < cons.length; i++) {
			finalCol[i - 1] = cons[i][0];
		}
		if (cols == null) {
			if (values.length > (cons.length - 1))
				errInfo += "插入的值个数错误";
			else {
				for (int i = 0; i < finalValues.length; i++) {
					if (i >= values.length)
						finalValues[i] = "";
					else
						finalValues[i] = values[i];
				}
			}
		} else if (cols != null) {
			if (values.length > (cols.length))
				errInfo += "前后的列和值的个数不匹配";
			else {
				for (int i = 0; i < finalValues.length; i++) {
					for (int k = 0; k < cols.length; k++) {
						if (cols[k].equals(cons[i + 1][0])) {
							if (i >= values.length)
								finalValues[i] = "";
							else
								finalValues[i] = values[i];
						}
					}
				}
				for (int i = 0; i < finalValues.length; i++) {
					if (finalValues[i] == null) {
						finalValues[i] = "";
					}
				}
			}
		}
		if (errInfo.matches("\\s{0,}")) {
			Object[][] record = Utils.getData(tableName);
			// for (int i = 0; i < finalCol.length; i++) {
			// System.out.println(finalCol[i]+" "+finalValues[i]);
			// }
			for (int j = 1; j < cons.length; j++) {
				String[] con = new String[6];
				for (int k = 0; k < con.length; k++) {
					con[k] = cons[j][k + 1];
				}
				// String tempInfo = "";
				for (int k = 0; k < con.length; k++) {
					// System.out.println(cons[j][0]+" "+ con[k]);
					String tempInfo = isRight(tableName, cons[j][0], con[k], finalValues[j - 1], record[j - 1], "ins");
					if (!tempInfo.matches("\\s{0,}"))
						errInfo += "'" + cons[j][0] + "'列" + tempInfo+"\r\n";
				}
				if (!errInfo.matches("\\s{0,}"))
					errInfo = errInfo.substring(0,errInfo.length()-1);
			}
			// for (int i = 0; i < finalValues.length; i++) {
			// System.out.println(finalValues[i]);
			// }
			if (errInfo.matches("\\s{0,}")) {
				Utils.insertData(tableName, finalValues, "ins");
			}
		}
		if (!errInfo.matches("\\s{0,}"))
			errInfo = errInfo.substring(0,errInfo.length()-1);
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//检查带select子查询的insert语句中的新纪录是否符合属性约束
	public static void CheckInsertSel(String insertTable, String[] insertCols, String selTable, String[] selCols,
			String[] whereSQL, boolean isAnd) {
		String errInfo = "";
		// 要插入的表的信息
		Table t = Utils.getTableOut(insertTable);
		String[][] cons = t.getCons();
		String[] finalValues = new String[cons.length - 1];
		String[] finalCol = new String[cons.length - 1];
		// 选中的表的信息
		Table selT = Utils.getTableOut(selTable);
		Object[][] oriValues = Utils.getAllRecord(selTable);
		String[][] selCons = new String[whereSQL.length][3];

		// 不管怎么样，最后要插入所有列（没有该列没有被选中，那么该列的值为空）
		for (int i = 1; i < cons.length; i++) {
			finalCol[i - 1] = cons[i][0];
		}

		// 检查前后列数是否一致
		if (insertCols == null) {
			if (selCols[0].equals("*")) {
				if ((selT.getCons().length - 1) > (cons.length - 1))
					errInfo += "select选中的列数过多！";
			} else {
				if (selCols.length > (cons.length - 1))
					errInfo += "select选中的列数过多！";
			}
		} else if (insertCols != null) {
			if (selCols[0].equals("*")) {
				if ((selT.getCons().length - 1) > (insertCols.length))
					errInfo += "选中的列数和要插入的列数不一致";
			} else {
				if (selCols.length > (insertCols.length))
					errInfo += "选中的列数和要插入的列数不一致";
			}
		}
		if (errInfo.matches("\\s{0,}")) {
			if (!Utils.checkAuthority(insertTable, "insert")) {
				System.out.println("登录名'" + MainSystem.currentUser + "'对表'" + insertTable + "'没有'insert'权限");
				return;
			}
			if (!Utils.checkAuthority(selTable, "select")) {
				System.out.println("登录名'" + MainSystem.currentUser + "'对表'" + selTable + "'没有'select'权限");
				return;
			}
			String reg = "([\u4e00-\u9fa5[a-z][A-Z][_]]{1,}[\u4e00-\u9fa5[\\w]]{0,})";
			String regWhere = reg + "\\s{0,}((?i)in|between|like|=)\\s{0,}" + "(.*)";
			for (int i = 0; i < whereSQL.length; i++) {
				Pattern p = Pattern.compile(regWhere);
				Matcher m = p.matcher(whereSQL[i]);
				if (m.find()) {
					selCons[i][0] = m.group(1).trim();
					selCons[i][1] = m.group(2).trim();
					selCons[i][2] = m.group(3).trim();
				}
			}
			int selCount = 0;
			// 如果有where
			if (!whereSQL[0].equals("*")) {
				// 如果是or
				if (!isAnd) {
					for (int i = 1; i < oriValues.length; i++) {
						for (int j = 0; j < selCons.length; j++) {
							for (int k = 1; k < oriValues[0].length; k++) {
								if (oriValues[0][k].equals(selCons[j][0])) {
									if (selCons[j][1].equals("=") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										if (temp.equals(selCons[j][2].substring(1, selCons[j][2].length() - 1))) {
											oriValues[i][0] = "sel";
											selCount++;
										}
									} else if (selCons[j][1].equals("between") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										int min = Integer.parseInt(selCons[j][2].split("((?i)and)")[0].trim());
										int max = Integer.parseInt(selCons[j][2].split("((?i)and)")[1].trim());
										int value = Integer.parseInt(temp);
										if (value <= max && min >= min) {
											oriValues[i][0] = "sel";
											selCount++;
										}
									} else if (selCons[j][1].equals("in") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										String[] v = selCons[j][2].substring(1, selCons[j][2].length() - 1).split(",");
										for (int m = 0; m < v.length; m++) {
											if (temp.equals(v[m].trim().substring(1, v[m].length() - 1))) {
												oriValues[i][0] = "del";
												selCount++;
												break;
											}
										}
									} else if (selCons[j][1].equals("like") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										String regLike = selCons[j][2].replaceAll("_", ".");
										regLike = selCons[j][2].replaceAll("%", ".*");
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
					int andSum = selCons.length;
					for (int i = 1; i < oriValues.length; i++) {
						for (int j = 0; j < selCons.length; j++) {
							for (int k = 1; k < oriValues[0].length; k++) {
								if (oriValues[0][k].equals(selCons[j][0])) {
									if (selCons[j][1].equals("=")) {
										String temp = String.valueOf(oriValues[i][k]);
										// System.out.println(selCons[j][2].substring(1,
										// selCons[j][2].length() - 1)+"
										// "+temp);
										if (temp.equals(selCons[j][2].substring(1, selCons[j][2].length() - 1))
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
									} else if (selCons[j][1].equals("between") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										int min = Integer.parseInt(selCons[j][2].split("((?i)and)")[0].trim());
										int max = Integer.parseInt(selCons[j][2].split("((?i)and)")[1].trim());
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
									} else if (selCons[j][1].equals("in") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										String[] v = selCons[j][2].substring(1, selCons[j][2].length() - 1).split(",");
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
									} else if (selCons[j][1].equals("like") && !oriValues[i][0].equals("sel")) {
										String temp = String.valueOf(oriValues[i][k]);
										String regLike = selCons[j][2].replaceAll("_", ".");
										regLike = selCons[j][2].replaceAll("%", ".*");
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
				String[][] selValues = null;
				if (!selCols[0].equals("*")) {
					int index = 0;
					selValues = new String[selCount][selCols.length];
					for (int i = 1; i < oriValues.length; i++) {
						if (oriValues[i][0].equals("sel")) {
							for (int j = 1; j < oriValues[0].length; j++) {
								for (int k = 0; k < selCols.length; k++) {
									if (oriValues[0][j].equals(selCols[k]))
										selValues[index][k] = (String) oriValues[i][j];
								}
							}
							index++;
						}
					}
				} else {
					int index = 0;
					selValues = new String[selCount][oriValues[0].length - 1];
					for (int i = 1; i < oriValues.length; i++) {
						if (oriValues[i][0].equals("sel")) {
							for (int j = 1; j < oriValues[0].length; j++) {
								selValues[index][j - 1] = (String) oriValues[i][j];
							}
							index++;
						}
					}
					selCols = new String[oriValues[0].length - 1];
					for (int i = 0; i < oriValues[0].length - 1; ++i)
						selCols[i] = String.valueOf(oriValues[0][i + 1]);
				}

				// 拼接并检查数据
				Object[][] record = Utils.getData(insertTable);
				for (int i = 0; i < selValues.length; i++) {
					for (int j = 0; j < finalValues.length; j++) {
						if (j >= selValues[i].length)
							finalValues[j] = "";
						else
							finalValues[j] = selValues[i][j];
					}
					for (int j = 1; j < cons.length; j++) {
						String[] con = new String[6];
						for (int k = 0; k < con.length; k++) {
							con[k] = cons[j][k + 1];
						}
						// String tempInfo = "";
						for (int k = 0; k < con.length; k++) {
							// System.out.println(cons[j][0]+" "+ con[k]);
							String tempInfo = isRight(insertTable, cons[j][0], con[k], finalValues[j - 1],
									record[j - 1], "ins");
							if (!tempInfo.matches("\\s{0,}"))
								errInfo += "'" + cons[j][0] + "'列" + tempInfo;
						}
					}
					if (!errInfo.matches("\\s{0,}"))
						break;
				}
				// 插入数据
				if (errInfo.matches("\\s{0,}")) {
					System.out.println("添加" + (selValues.length) + "条数据！");
					// 插入数据
					for (int i = 0; i < selValues.length; i++) {
						for (int j = 0; j < finalValues.length; j++) {
							if (j >= selValues[i].length)
								finalValues[j] = "";
							else
								finalValues[j] = selValues[i][j];
						}
						Utils.insertData(insertTable, finalValues, "insSel");
					}
				}
			} else {
				String[][] selValues = null;
				if (!selCols[0].equals("*")) {
					selValues = new String[oriValues.length - 1][selCols.length];
					for (int i = 1; i < oriValues.length; i++) {
						for (int j = 1; j < oriValues[0].length; j++) {
							for (int k = 0; k < selCols.length; k++) {
								if (oriValues[0][j].equals(selCols[k])) {
									selValues[i - 1][k] = (String) oriValues[i][j];
								}
							}
						}
					}
				} else {
					int index = 0;
					selValues = new String[oriValues.length - 1][oriValues[0].length - 1];
					for (int i = 1; i < oriValues.length; i++) {
						for (int j = 1; j < oriValues[0].length; j++) {
							selValues[i - 1][j - 1] = (String) oriValues[i][j];
						}
					}
					selCols = new String[oriValues[0].length - 1];
					for (int i = 0; i < oriValues[0].length - 1; ++i)
						selCols[i] = String.valueOf(oriValues[0][i + 1]);
				}
				// printData(selCols, selValues);
				// 拼接并检查数据
				Object[][] record = Utils.getData(insertTable);
				for (int i = 0; i < selValues.length; i++) {
					for (int j = 0; j < finalValues.length; j++) {
						if (j >= selValues[i].length)
							finalValues[j] = "";
						else
							finalValues[j] = selValues[i][j];
					}
					for (int j = 1; j < cons.length; j++) {
						String[] con = new String[6];
						for (int k = 0; k < con.length; k++) {
							con[k] = cons[j][k + 1];
						}
						// String tempInfo = "";
						for (int k = 0; k < con.length; k++) {
							// System.out.println(cons[j][0]+" "+ con[k]);
							String tempInfo = isRight(insertTable, cons[j][0], con[k], finalValues[j - 1],
									record[j - 1], "ins");
							if (!tempInfo.matches("\\s{0,}"))
								errInfo += "'" + cons[j][0] + "'列" + tempInfo+"\n\r";
						}
						if (!errInfo.matches("\\s{0,}"))
							errInfo = errInfo.substring(0,errInfo.length()-1);
					}
					if (!errInfo.matches("\\s{0,}"))
						break;
				}
				// 插入数据
				if (errInfo.matches("\\s{0,}")) {
					System.out.println("添加" + (selValues.length) + "条数据！");
					// 插入数据
					for (int i = 0; i < selValues.length; i++) {
						for (int j = 0; j < finalValues.length; j++) {
							if (j >= selValues[i].length)
								finalValues[j] = "";
							else
								finalValues[j] = selValues[i][j];
						}
						Utils.insertData(insertTable, finalValues, "insSel");
					}
				}
			}
			// for (int i = 0; i < oriValues.length; i++) {
			// for (int j = 0; j < oriValues[0].length; j++) {
			// System.out.print(oriValues[i][j] + " ");
			// }
			// System.out.println();
			// }

		}
		if (!errInfo.matches("\\s{0,}"))
			errInfo = errInfo.substring(0,errInfo.length()-1);
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//检查update语句中的新纪录是否符合属性约束
	public static String CheckUpdate(String tableName, Object[] values) {
		String errInfo = "";
		Table t = Utils.getTableOut(tableName);
		String[][] cons = t.getCons();
		String[] finalValues = new String[cons.length - 1];
		for (int i = 0; i < finalValues.length; i++) {
			finalValues[i] = String.valueOf(values[i]);
		}
		String[] finalCol = new String[cons.length - 1];
		for (int i = 1; i < cons.length; i++) {
			finalCol[i - 1] = cons[i][0];
		}
		if (errInfo.matches("\\s{0,}")) {
			Object[][] record = Utils.getData(tableName);
			// for (int i = 0; i < finalCol.length; i++) {
			// System.out.println(finalCol[i]+" "+finalValues[i]);
			// }
			for (int j = 1; j < cons.length; j++) {
				String[] con = new String[6];
				for (int k = 0; k < con.length; k++) {
					con[k] = cons[j][k + 1];
				}
				// String tempInfo = "";
				for (int k = 0; k < con.length; k++) {
					String tempInfo = isRight(tableName, cons[j][0], con[k], finalValues[j - 1], record[j - 1], "upd");
					if (!tempInfo.matches("\\s{0,}"))
						errInfo += "'" + cons[j][0] + "'列" + tempInfo+"\n\r";
				}
				if (!errInfo.matches("\\s{0,}"))
					errInfo = errInfo.substring(0,errInfo.length()-1);
			}
			if (errInfo.matches("\\s{0,}")) {
				Utils.insertData(tableName, finalValues, "upd");
			}
			// for (int i = 0; i < finalValues.length; i++) {
			// System.out.println(finalValues[i]);
			// }
		}
//		if (!errInfo.matches("\\s{0,}"))
//			errInfo = errInfo.substring(0,errInfo.length()-1);
		return errInfo;
	}
	//检查新纪录的实体完整性和参照完整性
	public static String isRight(String tableName, String colName, String cons, String finalValues, Object[] record,
			String opType) {
		String errInfo = "";
		if (cons.equalsIgnoreCase("PK")) {
			if (!isNotNull(finalValues) || !isUnique(finalValues, record)) {
				errInfo += "违反主键约束：";
			}
			if (!isNotNull(finalValues)) {
				errInfo += "违反非空约束 ";
			}
			if (!isUnique(finalValues, record)) {
				errInfo += "违反唯一约束 ";
			}
		} else if (cons.equalsIgnoreCase("FK")) {
			String temp = isPassFK(tableName, colName, opType, finalValues);
			if (!temp.matches("\\s{0,}"))
				errInfo += temp;
		} else if (cons.equalsIgnoreCase("not null")) {
			if (!isNotNull(finalValues)) {
				errInfo += "违反非空约束 ";
			}
		} else if (cons.equalsIgnoreCase("unique")) {
			if (!isUnique(finalValues, record)) {
				errInfo += "违反唯一约束 ";
			}
		} else if (cons.matches("((?i)int|boolean|char\\(.*\\)|varchar\\(.*\\)|float\\(.*\\))")) {
			String temp = isRightType(cons, finalValues);
			if (!temp.matches("\\s{0,}"))
				errInfo += temp;
		} else if (cons.matches("((?i)(or|bet).*)")) {
			String temp = isPassCheck(cons, finalValues);
			if (!temp.matches("\\s{0,}"))
				errInfo += temp;
		}
		return errInfo;
	}
	//检查check约束
	public static String isPassCheck(String cons, String finalValues) {
		String s = "";
		String[] check = cons.split(",");
		if (check[0].equals("bet")) {
			int min = Integer.parseInt(check[1]);
			int max = Integer.parseInt(check[2]);
			int values = Integer.parseInt(finalValues);
			if (!(values >= min && values <= max))
				s = "违反check约束:值必须在" + min + "到" + max + "的闭区间内！";
		}
		if (check[0].equals("or")) {
			boolean result = false;
			String def = "";
			for (int i = 1; i < check.length; i++) {
				def += "'" + check[i] + "',";
				if (finalValues.equals(check[i])) {
					result = true;
					break;
				}
			}
			def = def.substring(0, def.length() - 1);
			if (!result)
				s = "违反check约束:必须在" + def + "内取值！";
		}
		return s;
	}

	public static boolean isInt(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	//检查数据类型是否正确
	public static String isRightType(String cons, String finalValues) {
		String errInfo = "";
		String pattern = "((?i)int|smallint|boolean|char\\((.*)\\)|varchar\\((.*)\\)|float\\((.*)\\))";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(cons);
		if (m.find()) {
			// for (int i = 0; i <= m.groupCount(); ++i) {
			// System.out.println(m.group(i));
			// }
			String type = m.group(1);
			if (type.matches("((?i)int|smallint|boolean)")) {
				if (type.matches("((?i)int|smallint)")) {
					if (!isInt(finalValues))
						errInfo += "数据类型应该为" + type + " ";
				} else if (type.matches("((?i)boolean)?")) {
					if (!finalValues.matches("((?i)true|false)"))
						errInfo += "数据类型应该为" + type + " ";
				}
			} else {
				if (type.matches("((?i)char\\((.*)\\))")) {
					int maxLength = Integer.parseInt(m.group(2));
					if (finalValues.length() > maxLength)
						errInfo += "数据长度不能超过" + (maxLength) + " ";
				} else if (type.matches("((?i)varchar\\((.*)\\))")) {
					int maxLength = Integer.parseInt(m.group(3));
					if (finalValues.length() > maxLength)
						errInfo += "数据长度不能超过" + (maxLength) + " ";
				} else if (type.matches("((?i)float\\((.*)\\))")) {
					if (!isDouble(finalValues))
						errInfo += "数据类型应该为" + type + " ";
					else {
						int acc = Integer.parseInt(m.group(4));
						if (finalValues.split("\\.")[1].length() < acc)
							errInfo += "精度至少为" + (acc) + "位 ";
					}
				}
			}
		}
		return errInfo;
	}
	//检查外键约束
	public static String isPassFK(String tableName, String colName, String opType, String finalValues) {
		String errInfo = "";
		// 表中不存在，返回true
		boolean result = true;
		ArrayList<String> list = Utils.getFKInfo();
		for (String i : list) {
			String[] temp = i.split("\\|");
			if (opType.equals("ins")) {
				if (temp[0].equals(tableName) && temp[1].equals(colName)) {
					Object[] record = Utils.getColData(temp[2], temp[3]);
					if (isUnique(finalValues, record))
						errInfo += "违反外键约束：不能插入被参照表中不存在的值！";
				}
			} else if (opType.equals("upd")) {
				if (temp[0].equals(tableName) && temp[1].equals(colName)) {
					Object[] record = Utils.getColData(temp[2], temp[3]);
					if (isUnique(finalValues, record))
						errInfo += "违反外键约束：不能修改成被参照表中不存在的值！";
				}
				if (temp[2].equals(tableName) && temp[3].equals(colName)) {
					Object[] record = Utils.getColData(temp[0], temp[1]);
					if (!isUnique(finalValues, record))
						errInfo += "违反外键约束：不能修改在参照表中存在的值！";
				}
			} else if (opType.equals("del")) {
				if (temp[2].equals(tableName) && temp[3].equals(colName)) {
					Object[] record = Utils.getColData(temp[0], temp[1]);
					if (!isUnique(finalValues, record))
						errInfo += "违反外键约束：不能删除在参照表中存在的值！";
				}
			}
		}
		return errInfo;
	}
	//检查唯一性约束
	public static boolean isUnique(String finalValues, Object[] record) {
		// 表中不存在，返回true
		boolean result = true;
		for (int i = 0; i < record.length; i++) {
			String temp = String.valueOf(record[i]);
			if (finalValues.equals(temp)) {
				result = false;
				break;
			}
		}
		return result;
	}
	//检查非空约束
	public static boolean isNotNull(String finalValues) {
		// 不为空，返回true
		boolean result = true;
		if (finalValues.matches("\\s{0,}"))
			result = false;
		return result;
	}
}
