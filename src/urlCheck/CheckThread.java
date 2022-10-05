package urlCheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CheckThread implements Runnable {

	private static String filePath = "D:/SVN/ptcas/SOURCE/PUZZLING_FILE/excel";

	private String dirName; 
	private String threadName; 
	private static int RESPONE_TIME_OUT = 10000; // 1000  = 1 SEC
	
	public CheckThread(String dirName , String threadName) {
		this.dirName = dirName;
		this.threadName = threadName;
	}
	
	@Override
	public void run() {

		int count = 0;
		int fileCnt = 1;
		File errorTextFile = null;
		FileWriter errorWriter = null;
		try {

			//  /a1 /a2 /a3 /a4
			File urlDirPath = new File(filePath + dirName);

			if (!urlDirPath.exists()) {
				System.out.println(threadName + "----- 파일 존재하지 않음");
				return;
			} else {
				System.out.println(threadName + "----- 파일 존재");
			}
			for (File excel : urlDirPath.listFiles()) {
				String excelName = excel.getName();
				System.out.println(threadName + "----- "+excelName);

				String excelPath = excel.getAbsolutePath(); // excel 절대경로
				String excelDir = excel.getParentFile().getName(); // excel 부모 디렉토리명

				FileInputStream inputStream = new FileInputStream(excelPath);

				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				XSSFWorkbook writebook = new XSSFWorkbook();

				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFSheet newSheet = writebook.createSheet(excelDir);

				Row newSheetRow = newSheet.createRow(0);

				int headerCnt = sheet.getRow(0).getPhysicalNumberOfCells();

				String headerString = "";
				for (int i = 0; i < headerCnt; i++) {
					headerString += (headerString.length() > 0 ? "," : "")
							+ sheet.getRow(0).getCell(i).getStringCellValue();
					newSheetRow.createCell(i).setCellValue(sheet.getRow(0).getCell(i).getStringCellValue());
				}

				int rows = sheet.getPhysicalNumberOfRows();
				BufferedReader in = null;
				
				errorTextFile = new File(filePath + dirName +"/error_"+fileCnt+"_" + threadName + ".txt");				
				errorWriter = new FileWriter(errorTextFile,true);
				
				for (int i = 0; i < rows; i++) {

					int j = 0;
					String url = null;
					try {
						if (i == 0)
							continue;
						Row row = sheet.getRow(i);
						newSheetRow = newSheet.createRow(i);
						count++;
						if (i == 10000 || i == 20000 || i == 30000 || i == 40000) {
							System.out.println(threadName+ ": 실행 row  :  "  + i);
						}

						for (j = 0; j < headerCnt; j++) {
							if (j == 1 || j == 3 || j == 5) {

								url = row.getCell(j).getStringCellValue();
								url = url.replaceAll(".+(?=:)", "https");

								if (url != null && url != "") {
									URL obj = new URL(url);

									HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
									con.setRequestMethod("GET");
									con.setConnectTimeout(RESPONE_TIME_OUT);
									con.setReadTimeout(RESPONE_TIME_OUT);

									con.setHostnameVerifier(new HostnameVerifier() {

										@Override
										public boolean verify(String hostname, SSLSession session) {
											// TODO Auto-generated method stub
											return true;
										}
									});

									SSLContext context = SSLContext.getInstance("TLS");
									context.init(null, null, null);
									con.setSSLSocketFactory(context.getSocketFactory());

									con.connect();
									con.setInstanceFollowRedirects(true);

									newSheetRow.createCell(j).setCellValue(url);

									if (con.getResponseCode() == 200) {
										newSheetRow.createCell(j + 1).setCellValue("Y");
									} else {
										newSheetRow.createCell(j + 1).setCellValue("N");
									}

								}

							} else if (j == 0) {
								String etc = row.getCell(j).getStringCellValue();

								if (etc != null && etc != "") {
									newSheetRow.createCell(j).setCellValue(etc);
								}
							}
						}
					} catch (Exception e) {
						
						String errorMsg="------------["+threadName+"  excel row 읽기 도중  에러 발생]-------------------\n";
						errorMsg +="Row : " + (i + 1) + "\nUrl : " + url + "\nErrorMsg :"+ e.getMessage();
						errorMsg += "\n-----------------------------------------------------------------------";
						System.out.println(errorMsg);
						if (i < rows) {

							if (newSheetRow != null) {								
								newSheetRow.createCell(j).setCellValue(url);
								newSheetRow.createCell(j + 1).setCellValue("N");

							}

							
						}
	
						try {
							
							errorWriter.write(errorMsg);
							errorWriter.flush();
							
						}catch (IOException IOE) {
							System.out.println("error txt error :"+IOE.getMessage());
						}

						continue;
					}

				}

				workbook.close();
				inputStream.close();
				System.out.println(threadName+"----- 읽기 완료 ");

				File newExcelFile = new File(filePath + dirName +"/resul_"+fileCnt+"_" + threadName + ".xlsx");
				FileOutputStream fos = null;

				try {
					fos = new FileOutputStream(newExcelFile);
					writebook.write(fos);
				} catch (Exception e) {
					System.out.println(threadName + "------ 쓰기 exception");
					e.printStackTrace();
				} finally {
					try {
						if (writebook != null)
							writebook.close();
						if (fos != null)
							fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				
				System.out.println(threadName + "----- filecnt : " + fileCnt);

				fileCnt++;
			}
			System.out.println(threadName+"----- 쓰기 완료");

		} catch (Exception e) {
			System.out.println(threadName + "----- "+count);
			System.out.println(threadName+"----- exception 발생");
			e.printStackTrace();
		} finally {
			try {
				errorWriter.close();
			} catch (IOException e) {
				System.out.println("error text file close fail : "+ e.getMessage());
			}
		}

	}

}
