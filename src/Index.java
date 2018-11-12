import java.io.Serializable;

public class Index implements Serializable {
	public String indexName;
	public String indexType;
	public String tableName;
	public String[][] cols;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String[][] getCols() {
		return cols;
	}

	public void setCols(String[][] cols) {
		this.cols = cols;
	}

	public Index(String indexName, String indexType, String tableName, String[][] cols) {
		super();
		this.indexName = indexName;
		this.indexType = indexType;
		this.tableName = tableName;
		this.cols = cols;
	}
	//ƴ�����������кʹ��򣬷������
	public String getColsInfo() {
		String s = "";
		String[][] col = this.getCols();
		for (int i = 0; i < col.length; i++) {
			for (int j = 0; j < col[i].length; j++) {
				s += col[i][j] + " ";
			}
			s = s.trim();
			s += ",";
		}
		// System.out.println(s);
		s = s.substring(0, s.length() - 1);
		return s;
	}
	//����������Ϣ�е���󳤶ȣ��������
	public int maxLength() {
		int max = this.getIndexName().length();
		if (max < this.getIndexType().length())
			max = this.getIndexType().length();
		if (max < this.getTableName().length())
			max = this.getTableName().length();
		if (max < this.getColsInfo().length())
			max = this.getColsInfo().length();
		return max;
	}
	//���������Ϣ
	public void printOutFile() {
		String firstline = "";
		String nextline = "";
		for (int i = 0; i < 9; ++i)
			firstline += "��";
		for (int i = 0; i < this.maxLength(); ++i)
			nextline += "��";
		System.out.println("��" + firstline + "��" + nextline + "��");

		System.out.print("��");
		System.out.printf("%s\t  ", "������");
		System.out.print("��");
		System.out.printf("%-" + (this.maxLength()) + "s", this.getIndexName());
		System.out.println("��");

		System.out.println("��" + firstline + "��" + nextline + "��");

		System.out.print("��");
		System.out.printf("%s\t  ", "��������");
		System.out.print("��");
		System.out.printf("%-" + (maxLength()) + "s", this.getIndexType());
		System.out.println("��");

		System.out.println("��" + firstline + "��" + nextline + "��");

		System.out.print("��");
		System.out.printf("%s\t  ", "����");
		System.out.print("��");
		System.out.printf("%-" + (maxLength()) + "s", this.getTableName());
		System.out.println("��");

		System.out.println("��" + firstline + "��" + nextline + "��");

		System.out.print("��");
		System.out.printf("%s\t  ", "�м�����");
		System.out.print("��");
		System.out.printf("%-" + (maxLength()) + "s", this.getColsInfo());
		System.out.println("��");

		System.out.println("��" + firstline + "��" + nextline + "��");

	}
}
