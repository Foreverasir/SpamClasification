package Judge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

class A {
	int spamCount;
	int hamCount;

	public A() {
		spamCount = 0;
		hamCount = 0;
	}

	public int getSpamCount() {
		return spamCount;
	}

	public void setSpamCount(int spamCount) {
		this.spamCount = spamCount;
	}

	public void addSpamCount() {
		this.spamCount++;
	}

	public void addHamCount() {
		this.hamCount++;
	}

	public int getHamCount() {
		return hamCount;
	}

	public void setHamCount(int hamCount) {
		this.hamCount = hamCount;
	}
}

public class SpamMailJudge {
	public static final String BASE_PATH = "D:\\data";
	static Map<String, A> allMap = new HashMap<String, A>();
	static int spamNum;
	static int hamNum;

	public static void main(String[] args) {
		try {
			readfile(BASE_PATH + "\\train\\spam", 1);
			readfile(BASE_PATH + "\\train\\ham", -1);
			// min1,min2,max1,max2
			maphandle(0, 6, 1550, 7800);
			System.out.println(allMap.size());
			// second argument is rate
			generateOutput(BASE_PATH + "\\test", 1.1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void maphandle(int min1, int min2, int max1, int max2) {
		for (Iterator<String> iter = allMap.keySet().iterator(); iter.hasNext();) {
			String string = (String) iter.next();
			if (allMap.get(string).getSpamCount() < min1 || allMap.get(string).getSpamCount() > max1
					|| allMap.get(string).getHamCount() < min2 || allMap.get(string).getHamCount() > max2)
				iter.remove();
		}
		for (Iterator<String> iter = allMap.keySet().iterator(); iter.hasNext();) {
			String string = (String) iter.next();
			double a1 = (double) allMap.get(string).getSpamCount() / spamNum;
			double a2 = (double) allMap.get(string).getHamCount() / hamNum;
			double a = Math.abs(a1 - a2);
			if (a <= 0.0364)// B
				iter.remove();
		}
	}

	// 读取训练集邮件同时生成字典
	public static boolean readfile(String filepath, int flag) throws FileNotFoundException, IOException {
		File file = new File(filepath);
		if (!file.isDirectory()) {
			System.err.println("Unexpected!");
			return false;
		} else if (file.isDirectory()) {
			String[] filelist = file.list();
			if (flag == 1)
				spamNum = filelist.length;
			else
				hamNum = filelist.length;
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(filepath + "\\" + filelist[i]);
				BufferedReader reader = null;
				if (!readfile.isDirectory()) {
					try {
						reader = new BufferedReader(new FileReader(readfile));
						String tempString = null;
						// 一次读入一行，直到读入null为文件结束
						while ((tempString = reader.readLine()) != null) {
							StringTokenizer st = new StringTokenizer(tempString);
							while (st.hasMoreElements()) {
								String string = st.nextToken();
								if (flag == 1) {
									if (!allMap.containsKey(string))
										allMap.put(string, new A());
									allMap.get(string).addSpamCount();
								} else {
									if (!allMap.containsKey(string))
										allMap.put(string, new A());
									allMap.get(string).addHamCount();
								}
							}
						}
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (IOException e1) {
							}
						}
					}
				}
			}
		}
		return true;
	}

	public static int test(String filepath, double rate) throws IOException {
		double spamP = 1.0;
		double hamP = 1.0;
		File readfile = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(readfile));
		String tempString = null;
		List<String> list = new ArrayList<String>();
		while ((tempString = reader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(tempString);
			while (st.hasMoreElements()) {
				String string = st.nextToken();
				list.add(string);
			}
		}
		reader.close();
		for (Iterator<String> iter = allMap.keySet().iterator(); iter.hasNext();) {
			String string = (String) iter.next();
			if (list.contains(string)) {
				spamP *= (double) (allMap.get(string).getSpamCount() + 1) / (spamNum * rate + 2);
				hamP *= (double) (allMap.get(string).getHamCount() + 1) / (hamNum * rate + 2);
			} else {
				spamP *= (1.0 - (double) (allMap.get(string).getSpamCount() + 1) / (spamNum * rate + 2));
				hamP *= (1.0 - (double) (allMap.get(string).getHamCount() + 1) / (hamNum * rate + 2));
			}
		}
		spamP *= (double) spamNum / (spamNum + hamNum);
		hamP *= (double) hamNum / (spamNum + hamNum);
		if (spamP > hamP)
			return 1;
		else
			return -1;
	}

	static public void generateOutput(String filepath, double rate) throws IOException {
		File file = new File(filepath);
		if (!file.isDirectory()) {
			System.err.println("Unexpected!");
			return;
		} else if (file.isDirectory()) {
			String[] filelist = file.list();
			String name = "131220058.txt";
			File output = new File(name);
			if (output.exists())
				output.delete();
			try {
				int count = 0;
				output.createNewFile();
				FileWriter resultFile = new FileWriter(output);
				PrintWriter myNewFile = new PrintWriter(resultFile);
				int a[] = new int[filelist.length];
				for (int i = 1; i <= filelist.length; i++) {
					int judge = test(filepath + "\\" + filelist[i - 1], rate);
					String ss = filelist[i - 1];
					ss = ss.substring(0, ss.length() - 4);
					int index = Integer.parseInt(ss);
					a[index - 1] = judge;
					if (judge == 1)
						count++;
				}
				for (int i = 1; i <= filelist.length; i++) {
					myNewFile.println(i + ".txt " + a[i - 1]);
				}
				myNewFile.close();
				System.out.println(count);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
