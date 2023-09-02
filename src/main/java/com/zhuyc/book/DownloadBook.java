package com.zhuyc.book;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author zhuyc
 * @version 1.0
 * @description TODO
 * @date 2023/7/29 10:26
 */
public class DownloadBook {

	private final static String HOST="https://www.xpiaotian.com";

	private final  static  String uri="/book/";
	
	  public static void main(String[] args) {
	  	showAll("397991");
	  }

	private static void search(String name) {
		String motanku=doPost(HOST+uri, "keyword=" + name + "&t=1");
		Document doc=Jsoup.parse(motanku);
		Elements elements=doc.getElementsByClass("hot_sale");
		for (Element str : elements) {
			System.out.println("===小说列表===");
			String title=str.getElementsByClass("title").text();
			String author=str.getElementsByClass("author").eq(0).text();
			String news=str.getElementsByClass("author").eq(1).text();
			String ids=str.getElementsByTag("a").attr("href");
			System.out.println("标题：" + title);
			System.out.println("类型|作者：" + author);
			System.out.println("最新消息：" + news);
			System.out.println("小说id：" + ids);
			System.out.println("==小说列表===\n");
		}
		System.out.println("输入想下载的小说id(例如: /mtk2580/)");
		showAll(new Scanner(System.in).next());
	}

	//查找所有章节
	private static void showAll(String urlId) {
		  String url="https://www.xpiaotian.com/book/"+urlId+"/";
		String str=doGet(url);
		Document doc=Jsoup.parse(str);
		Element elementList=doc.getElementById("list");
		Elements tag=elementList.getElementsByTag("dd");
		TreeMap<String,String> bookMap=new TreeMap<>();

		for(int i=0;i<tag.size();i++){
			Element element=tag.get(i);
			Elements aa=element.getElementsByTag("a");
			Element a=aa.get(0);
			String name=a.text();
			String href=a.attr("href");
			if(!bookMap.containsKey(href)){
				bookMap.put(href, name);
			}
		}
		download("我的师兄太强了",url, bookMap);
	/*	Element ele=doc.getElementById("chapterlist");
		Elements title=doc.getElementsByTag()
		Elements list=ele.getElementsByTag("a");
		String[][] bookEle=new String[list.size() - 1][2];
		for (int i=1; i <= bookEle.length; i++) {
			int j=i - 1;
			String zname=list.get(i).text();
			String zurl=list.get(i).attr("href");
			bookEle[j][0]=zname;
			bookEle[j][1]=zurl;
		}
		System.out.println("是否开始下载全本?(是或否)");
		Scanner ss=new Scanner(System.in);
		if (ss.next().equals("是")) {
			download(title.get(0).text(), bookEle);
		}*/
	}

	//下载小说
	private static void download(String bookName, String url,TreeMap<String,String> bookInfo) {
		try {
			File mfile=new File("D:/Mobai/" + bookName + ".txt");
			if (!mfile.exists()) {
				mfile.createNewFile();
			}
			for(Map.Entry<String,String> entry: bookInfo.entrySet()){
				String href=entry.getKey();
				String name=entry.getValue();
				System.out.println("准备下载第"+name);
				String message=doGet(url + href);
				Document doc=Jsoup.parse(message);
				Element ele=doc.getElementById("content");
				List<TextNode> textNodes=ele.textNodes();
				StringBuilder stringBuilder=new StringBuilder();
				for(TextNode textNode:textNodes){
					String first=textNode.text().replace("章节报错（免登陆）", "");
					stringBuilder.append(first);
					stringBuilder.append("\n");
				}
				writeToText(mfile, name + "\n" + stringBuilder.toString() + "\n");
			}
//			for (int i=0; i < len; i++) {
//				String message=doGet("https://m.motanku.com" + bookInfo[i][1]);
//				Document doc=Jsoup.parse(message);
//				Element ele=doc.getElementById("chaptercontent");
//				String first=ele.text().replace("『章节错误,点此举报』", "");
//				String second=first.replace("『加入书签，方便阅读』", "");
//				String three=second.replace(" 　　", "\n    ");
//				writeToText(mfile, bookInfo[i][0] + "\n" + three + "\n\n");
//			}
			System.out.println("下载完毕");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	//get封装
	private static String doGet(String murl) {
		try {
			URL url=new URL(murl);
			HttpsURLConnection conn=(HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
			conn.connect();
			int responseCode=conn.getResponseCode();
			String sum="";
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in=conn.getInputStream();
				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
				String str=null;
				while (true) {
					str=reader.readLine();
					if (str != null) {
						sum=sum + str + "\n";
					} else {
						break;
					}
				}
				reader.close();
				in.close();
			}
			return sum;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "";
		}
	}

	//post封装
	private static String doPost(String bookUrl, String params) {
		try {
			URL url=new URL(bookUrl);
			HttpURLConnection conn=(HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.connect();

			BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
			writer.write(params);
			writer.close();

			InputStream is=conn.getInputStream();
			BufferedReader reader=new BufferedReader(new InputStreamReader(is));
			String str=null;
			String sum="";
			while (true) {
				str=reader.readLine();
				if (str != null) {
					sum=sum + str + "\n";
				} else {
					break;
				}
			}
			reader.close();
			is.close();
			return sum;
		} catch (Exception e) {
			return "";
		}
	}

	//写入文本
	private static void writeToText(File fileName, String content) {
		try {
			FileWriter writer=new FileWriter(fileName, true);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
