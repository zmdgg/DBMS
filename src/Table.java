import java.io.Serializable;
import java.util.ArrayList;

public class Table implements Serializable {
	public String tableName;
	public String[][] cons;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String[][] getCons() {
		return cons;
	}

	public void setCons(String[][] cons) {
		this.cons = cons;
	}
	//输出表的属性和约束信息
	public void printOutFile() {
		String FKInfo = "";
		ArrayList<String> list = Utils.getFKInfo();
		for (String i : list) {
			String[] temp = i.split("\\|");
			if (temp[0].equals(tableName))
				FKInfo = "FKIno : col'" + temp[1] + "' references col'" + temp[3] + "' in table'" + temp[2] + "'";
		}
		String singleline = "";
		for (int i = 0; i < 70; ++i)
			singleline += "─";
		System.out.println("┌" + singleline + "┐");
		System.out.print("│");
		System.out.printf("\t\t\t\t%s\t\t\t       ", "tableName:" + this.getTableName());
		System.out.print("│");
		System.out.println();
		System.out.println("├" + singleline + "┤");
		String[][] cons = this.getCons();
		for (int i = 0; i < cons.length; i++) {
			System.out.print("│");
			for (int j = 0; j < cons[i].length; j++) {
				System.out.printf("%-10s", cons[i][j]);
			}
			System.out.println("│");
			if (i != cons.length - 1) {
				System.out.println("├" + singleline + "┤");
			} else {
				if (!FKInfo.matches("\\s{0,}")) {
					System.out.println("├" + singleline + "┤");
					System.out.print("│");
					System.out.printf("%-70s", FKInfo);
					System.out.print("│");
					System.out.println();
				}
			}

		}
		System.out.println("└" + singleline + "┘");

	}

	public Table(String tableName, String[][] cons) {
		super();
		this.tableName = tableName;
		this.cons = cons;
	}
}
