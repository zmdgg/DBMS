import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckData {
	//����û����Ƿ����
	public static String isExistsUser(String userName) {
		String errInfo = "";
		Object[][] datas = Utils.getUserData("userInfo");
		// �ж��û����Ƿ����
		if (!isUnique(userName, datas[0])) {
			errInfo += "��¼��'" + userName + "'�Ѵ���!";
		}
		return errInfo;
	}
	//��鴴���û�ʱ���û���Ϣ�Ƿ���Ϲ淶
	public static void CheckUser(String tableName, String[] values) {
		String errInfo = "";
		String[][] cons = Utils.getUserInfo(tableName);
		Object[][] datas = Utils.getUserData(tableName);
		if (!isNotNull(values[0])) {
			errInfo += "��¼������Ϊ�գ�";
		}
		if (!isNotNull(values[1])) {
			errInfo += "���벻��Ϊ�գ�";
		}
		if (!isNotNull(values[2])) {
			errInfo += "Ȩ�޲���Ϊ�գ�";
		}
		// �ж��û����Ƿ����
		if (!isUnique(values[0], datas[0])) {
			errInfo += "��¼��'" + values[0] + "'�Ѵ���!";
		}
		if (errInfo.matches("\\s{0,}")) {
			Utils.saveUserData(tableName, values);
		}
		if (!errInfo.matches("\\s{0,}"))
			System.out.println(errInfo);
	}
	//���insert����е��¼�¼�Ƿ��������Լ��
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
				errInfo += "�����ֵ��������";
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
				errInfo += "ǰ����к�ֵ�ĸ�����ƥ��";
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
						errInfo += "'" + cons[j][0] + "'��" + tempInfo+"\r\n";
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
	//����select�Ӳ�ѯ��insert����е��¼�¼�Ƿ��������Լ��
	public static void CheckInsertSel(String insertTable, String[] insertCols, String selTable, String[] selCols,
			String[] whereSQL, boolean isAnd) {
		String errInfo = "";
		// Ҫ����ı����Ϣ
		Table t = Utils.getTableOut(insertTable);
		String[][] cons = t.getCons();
		String[] finalValues = new String[cons.length - 1];
		String[] finalCol = new String[cons.length - 1];
		// ѡ�еı����Ϣ
		Table selT = Utils.getTableOut(selTable);
		Object[][] oriValues = Utils.getAllRecord(selTable);
		String[][] selCons = new String[whereSQL.length][3];

		// ������ô�������Ҫ���������У�û�и���û�б�ѡ�У���ô���е�ֵΪ�գ�
		for (int i = 1; i < cons.length; i++) {
			finalCol[i - 1] = cons[i][0];
		}

		// ���ǰ�������Ƿ�һ��
		if (insertCols == null) {
			if (selCols[0].equals("*")) {
				if ((selT.getCons().length - 1) > (cons.length - 1))
					errInfo += "selectѡ�е��������࣡";
			} else {
				if (selCols.length > (cons.length - 1))
					errInfo += "selectѡ�е��������࣡";
			}
		} else if (insertCols != null) {
			if (selCols[0].equals("*")) {
				if ((selT.getCons().length - 1) > (insertCols.length))
					errInfo += "ѡ�е�������Ҫ�����������һ��";
			} else {
				if (selCols.length > (insertCols.length))
					errInfo += "ѡ�е�������Ҫ�����������һ��";
			}
		}
		if (errInfo.matches("\\s{0,}")) {
			if (!Utils.checkAuthority(insertTable, "insert")) {
				System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + insertTable + "'û��'insert'Ȩ��");
				return;
			}
			if (!Utils.checkAuthority(selTable, "select")) {
				System.out.println("��¼��'" + MainSystem.currentUser + "'�Ա�'" + selTable + "'û��'select'Ȩ��");
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
			// �����where
			if (!whereSQL[0].equals("*")) {
				// �����or
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

				// ƴ�Ӳ��������
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
								errInfo += "'" + cons[j][0] + "'��" + tempInfo;
						}
					}
					if (!errInfo.matches("\\s{0,}"))
						break;
				}
				// ��������
				if (errInfo.matches("\\s{0,}")) {
					System.out.println("���" + (selValues.length) + "�����ݣ�");
					// ��������
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
				// ƴ�Ӳ��������
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
								errInfo += "'" + cons[j][0] + "'��" + tempInfo+"\n\r";
						}
						if (!errInfo.matches("\\s{0,}"))
							errInfo = errInfo.substring(0,errInfo.length()-1);
					}
					if (!errInfo.matches("\\s{0,}"))
						break;
				}
				// ��������
				if (errInfo.matches("\\s{0,}")) {
					System.out.println("���" + (selValues.length) + "�����ݣ�");
					// ��������
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
	//���update����е��¼�¼�Ƿ��������Լ��
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
						errInfo += "'" + cons[j][0] + "'��" + tempInfo+"\n\r";
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
	//����¼�¼��ʵ�������ԺͲ���������
	public static String isRight(String tableName, String colName, String cons, String finalValues, Object[] record,
			String opType) {
		String errInfo = "";
		if (cons.equalsIgnoreCase("PK")) {
			if (!isNotNull(finalValues) || !isUnique(finalValues, record)) {
				errInfo += "Υ������Լ����";
			}
			if (!isNotNull(finalValues)) {
				errInfo += "Υ���ǿ�Լ�� ";
			}
			if (!isUnique(finalValues, record)) {
				errInfo += "Υ��ΨһԼ�� ";
			}
		} else if (cons.equalsIgnoreCase("FK")) {
			String temp = isPassFK(tableName, colName, opType, finalValues);
			if (!temp.matches("\\s{0,}"))
				errInfo += temp;
		} else if (cons.equalsIgnoreCase("not null")) {
			if (!isNotNull(finalValues)) {
				errInfo += "Υ���ǿ�Լ�� ";
			}
		} else if (cons.equalsIgnoreCase("unique")) {
			if (!isUnique(finalValues, record)) {
				errInfo += "Υ��ΨһԼ�� ";
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
	//���checkԼ��
	public static String isPassCheck(String cons, String finalValues) {
		String s = "";
		String[] check = cons.split(",");
		if (check[0].equals("bet")) {
			int min = Integer.parseInt(check[1]);
			int max = Integer.parseInt(check[2]);
			int values = Integer.parseInt(finalValues);
			if (!(values >= min && values <= max))
				s = "Υ��checkԼ��:ֵ������" + min + "��" + max + "�ı������ڣ�";
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
				s = "Υ��checkԼ��:������" + def + "��ȡֵ��";
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
	//������������Ƿ���ȷ
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
						errInfo += "��������Ӧ��Ϊ" + type + " ";
				} else if (type.matches("((?i)boolean)?")) {
					if (!finalValues.matches("((?i)true|false)"))
						errInfo += "��������Ӧ��Ϊ" + type + " ";
				}
			} else {
				if (type.matches("((?i)char\\((.*)\\))")) {
					int maxLength = Integer.parseInt(m.group(2));
					if (finalValues.length() > maxLength)
						errInfo += "���ݳ��Ȳ��ܳ���" + (maxLength) + " ";
				} else if (type.matches("((?i)varchar\\((.*)\\))")) {
					int maxLength = Integer.parseInt(m.group(3));
					if (finalValues.length() > maxLength)
						errInfo += "���ݳ��Ȳ��ܳ���" + (maxLength) + " ";
				} else if (type.matches("((?i)float\\((.*)\\))")) {
					if (!isDouble(finalValues))
						errInfo += "��������Ӧ��Ϊ" + type + " ";
					else {
						int acc = Integer.parseInt(m.group(4));
						if (finalValues.split("\\.")[1].length() < acc)
							errInfo += "��������Ϊ" + (acc) + "λ ";
					}
				}
			}
		}
		return errInfo;
	}
	//������Լ��
	public static String isPassFK(String tableName, String colName, String opType, String finalValues) {
		String errInfo = "";
		// ���в����ڣ�����true
		boolean result = true;
		ArrayList<String> list = Utils.getFKInfo();
		for (String i : list) {
			String[] temp = i.split("\\|");
			if (opType.equals("ins")) {
				if (temp[0].equals(tableName) && temp[1].equals(colName)) {
					Object[] record = Utils.getColData(temp[2], temp[3]);
					if (isUnique(finalValues, record))
						errInfo += "Υ�����Լ�������ܲ��뱻���ձ��в����ڵ�ֵ��";
				}
			} else if (opType.equals("upd")) {
				if (temp[0].equals(tableName) && temp[1].equals(colName)) {
					Object[] record = Utils.getColData(temp[2], temp[3]);
					if (isUnique(finalValues, record))
						errInfo += "Υ�����Լ���������޸ĳɱ����ձ��в����ڵ�ֵ��";
				}
				if (temp[2].equals(tableName) && temp[3].equals(colName)) {
					Object[] record = Utils.getColData(temp[0], temp[1]);
					if (!isUnique(finalValues, record))
						errInfo += "Υ�����Լ���������޸��ڲ��ձ��д��ڵ�ֵ��";
				}
			} else if (opType.equals("del")) {
				if (temp[2].equals(tableName) && temp[3].equals(colName)) {
					Object[] record = Utils.getColData(temp[0], temp[1]);
					if (!isUnique(finalValues, record))
						errInfo += "Υ�����Լ��������ɾ���ڲ��ձ��д��ڵ�ֵ��";
				}
			}
		}
		return errInfo;
	}
	//���Ψһ��Լ��
	public static boolean isUnique(String finalValues, Object[] record) {
		// ���в����ڣ�����true
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
	//���ǿ�Լ��
	public static boolean isNotNull(String finalValues) {
		// ��Ϊ�գ�����true
		boolean result = true;
		if (finalValues.matches("\\s{0,}"))
			result = false;
		return result;
	}
}
