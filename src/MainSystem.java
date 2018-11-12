import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Scanner;

public class MainSystem {
	public static String currentUser;
	public static String currentDB;
	public static boolean isLogin = false;
	//实现Eclipse控制台清屏（鼠标需要在控制台内）
	public static void clear() throws AWTException {
		Robot r = new Robot();
		r.mousePress(InputEvent.BUTTON3_MASK);
		r.mouseRelease(InputEvent.BUTTON3_MASK);
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_R);
		r.keyRelease(KeyEvent.VK_R);
		r.keyRelease(KeyEvent.VK_CONTROL);
		r.delay(100);
	}
	//每次输入的分界线
	public static void probar(int time) {
		for (int i = 0; i < 27; i++) {
			try {
				System.out.print(">");
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println();
	}
	//登录界面
	public static String getInput(Scanner sc) {
		String input = "";
		input += sc.next();
		if (input.equalsIgnoreCase("exit")) {
			return input;
		}
		while (true) {
			input += sc.nextLine();
			if (input.charAt(input.length() - 1) == ';') {
				break;
			}
			input += "\n\r";
		}
		input = input.replaceAll("\n\r", " ");
		// System.out.println(input);
		return input;
	}

	public static boolean welcomeView(Scanner sc) {
		String tab = "";
		String singleLine = "";
		for (int i = 0; i < 60; i++)
			tab += " ";
		for (int i = 0; i < 30; ++i)
			singleLine += "─";
		System.out.println("\n\n\n");
		System.out.println(tab + singleLine);
		System.out.println(tab + "       Welcome to DBMS!");
		System.out.println(tab + singleLine);
		System.out.print(tab + "userName:");
		String useName = sc.nextLine();
		System.out.println(tab + singleLine);
		System.out.print(tab + "password:");
		String password = sc.nextLine();
		System.out.println(tab + singleLine);
		if (UserInfo.isLogin(useName.trim(), password.trim())) {
			currentUser = useName.trim();
			return true;
		} else
			return false;
		// System.out.println(useName + " "+password);
	}

	public static void main(String[] args) throws AWTException {
		MainSystem ms = new MainSystem();
		Scanner sc = null;
		Scanner welsc = null;
		Initialize.createUserInfo();
		Initialize.createDBPath();
		while (true) {
			// 登录界面
			while (!isLogin) {
				welsc = new Scanner(System.in);
				isLogin = welcomeView(welsc);
				clear();
				if (isLogin) {
					break;
				} else {
					clear();
					System.out.println("Access fail!Please check your loginInfo!");
				}
			}
			clear();
			// 输入SQL
			sc = new Scanner(System.in);
			System.out.println("Access success!");
			String input = null;
			int flag = 0;
			do {
				if (input != null) {
					AnalysisSQL.parseSQL(input);
				}
				probar(26);
				System.out.println("Please input SQL statement:");

			} while (!(input = getInput(sc)).equalsIgnoreCase("exit"));
			isLogin = false;
			clear();
		}
	}
}
