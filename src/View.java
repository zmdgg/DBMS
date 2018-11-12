import java.io.Serializable;
import java.util.ArrayList;

public class View  implements Serializable  {
	public String viewName ;
	public String defSQL ;
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getDefSQL() {
		return defSQL;
	}
	public void setDefSQL(String defSQL) {
		this.defSQL = defSQL;
	}
	public View(String viewName, String defSQL) {
		super();
		this.viewName = viewName;
		this.defSQL = defSQL;
	}
	//�����ͼ����Ϣ
	public void printOutFile() {
		String firstline = "";
		String nextline = "";
		for (int i = 0; i < 9; ++i)
			firstline += "��";
		for (int i = 0; i < this.getDefSQL().length(); ++i)
			nextline += "��";
		System.out.println("��" + firstline+"��"+nextline + "��");
		
		System.out.print("��");
		System.out.printf("%s\t  ","��ͼ��");
		System.out.print("��");
		System.out.printf("%-"+(this.getDefSQL().length())+"s",this.getViewName());
		System.out.println("��");
		
		System.out.println("��" + firstline+"��"+nextline + "��");
		
		System.out.print("��");
		System.out.printf("%s\t  ","�������");
		System.out.print("��");
		System.out.printf("%-"+(this.getDefSQL().length())+"s",this.getDefSQL());
		System.out.println("��");
		
		System.out.println("��" + firstline+"��"+nextline + "��");

	}
}
