import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class WarehouseData {
	public static void main(String args[]) {
		// Category Space - 0x09
		// ASCII Space - 0x32
		// newLine - 0x13
		// Grab text up until space 9 times, then skip to next line
		video headNode = generateNodes("0222\\0.txt"); // Enter the FULL path to each of the raw data files as a parameter in the generateNodes() method
		headNode.getTail().next = generateNodes("0222\\1.txt");
		headNode.getTail().next = generateNodes("0222\\2.txt");
		headNode.getTail().next = generateNodes("0222\\3.txt");
		headNode.getTail().next = generateNodes("0222\\4.txt");
		video currentNode = headNode;
		
		System.out.println(headNode.vidID);
		System.out.println(headNode.uploader);
		System.out.println(headNode.getTail().toString());
		
		System.out.println("Formatting Excel Sheet");
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet spreadsheet = workbook.createSheet("Video Data");
		XSSFRow row;
		Map<String, Object[]> vidKeys = new TreeMap<String, Object[]>();
		
	    vidKeys.put("1", new Object[] {"Video", "Uploader", "Age", "Category", "Length", "Views", "Ratings Average", "Number of Ratings", "Number of Comments"});
		int step = 1;
		while (currentNode!=null) {
			if (!currentNode.vidID.equals("")&&
				!currentNode.uploader.equals("")&&
				!currentNode.age.equals("")&&
				!currentNode.category.equals("")&&
				!currentNode.length.equals("")&&
				!currentNode.views.equals("")&&
				!currentNode.avgRate.equals("")&&
				!currentNode.totalRate.equals("")&&
				!currentNode.totalComment.equals("")) {
				System.out.println("Formatting video " + currentNode.vidID);
				step++;
				vidKeys.put((""+step), new Object[] {currentNode.vidID, currentNode.uploader, currentNode.age, currentNode.category, currentNode.length, currentNode.views, currentNode.avgRate, currentNode.totalRate, currentNode.totalComment});
			} else {
				System.out.println("Skipping video " + currentNode.vidID);
			}
			/*System.out.println("Formatting video " + currentNode.vidID);
			step++;
			vidKeys.put((""+step), new Object[] {currentNode.vidID, currentNode.uploader, currentNode.age, currentNode.category, currentNode.length, currentNode.views, currentNode.avgRate, currentNode.totalRate, currentNode.totalComment});*/
			currentNode = currentNode.next;
		}
		
		System.out.println("Exporting");
		
		Set<String> keyid = vidKeys.keySet();
		int rowid = 0;
		for (String key:keyid) {
	         row = spreadsheet.createRow(rowid++);
	         Object [] objectArr = vidKeys.get(key);
	         int cellid = 0;
	       
	         for (Object obj : objectArr){
	            Cell cell = row.createCell(cellid++);
	            cell.setCellValue((String)obj);
	         }
		}
		
		try {
			FileOutputStream out = new FileOutputStream(new File("C:\\Users\\Podaro\\Documents\\KSUY4-DataMining\\Project\\0222\\VideoData.xlsx"));
			workbook.write(out);
			out.close();
		} catch(Exception e) {}
		
		System.out.println("Completed");
	}
	
	static video generateNodes(String loca) {
		System.out.println("Starting on file " + loca);
		video headNode = new video();
		video currentNode = headNode;
		String hexData = FileToHex(loca);	
		String rawText = HexToASCII(hexData);
		//
		int offset = 0;
		int mode = 1; // At 10, just skip to end of line
		while (offset<rawText.length()-1) {
			if (rawText.charAt(offset) == 0x09) {
				mode++;
				//System.out.println("New Category");
			} else if (rawText.charAt(offset) == 0x0A) {
				System.out.println("Imported video " + currentNode.vidID);
				mode = 1;
				currentNode.next = new video();
				currentNode = currentNode.next;
				//System.out.println("New Line");
			} else if ((rawText.charAt(offset) != 0x09)&&(rawText.charAt(offset) != 0x0A)&&(rawText.charAt(offset) != 0x0D)) {
				switch (mode) {
				case 1:
					currentNode.vidID+=rawText.charAt(offset);
					break;
				case 2:
					currentNode.uploader+=rawText.charAt(offset);
					break;
				case 3:
					currentNode.age+=rawText.charAt(offset);
					break;
				case 4:
					currentNode.category+=rawText.charAt(offset);
					break;
				case 5:
					currentNode.length+=rawText.charAt(offset);
					break;
				case 6:
					currentNode.views+=rawText.charAt(offset);
					break;
				case 7:
					currentNode.avgRate+=rawText.charAt(offset);
					break;
				case 8:
					currentNode.totalRate+=rawText.charAt(offset);
					break;
				case 9:
					currentNode.totalComment+=rawText.charAt(offset);
					break;
				}
			}
			offset++;
		}		
		return(headNode);
	}
	
	public static String FileToHex(String loca) {
		try {	
			String holder;
			StringBuilder hexa = new StringBuilder("");
			File file = new File(loca);
			FileInputStream fis = new FileInputStream(file);
			FileChannel fC = fis.getChannel();

			ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());			
			fC.read(byteBuffer);
			byteBuffer.flip();
			
			for (int i=0;i<byteBuffer.limit();i++) {
				holder = Integer.toHexString(byteBuffer.get() & 0xFF);
				if (holder.length()>1) {
					hexa.append(holder);
				} else {
					hexa.append("0");
					hexa.append(holder);
				}
			}
			
			fC.close();
			fis.close();
			return(hexa.toString());
		} catch(Exception e) {	// Standard Error Catch
			return("z");
		}
	}
	
	public static String HexToASCII(String hexData) {
		StringBuilder rawText = new StringBuilder();
		for (int i = 0; i < hexData.length(); i+=2) {
		    String str = hexData.substring(i, i+2);
		    rawText.append((char) Integer.parseInt(str, 16));
		}
		
		return(rawText.toString());
	}
}

class video {
	String vidID = "";
	String uploader = "";
	String age = "";
	String category = "";
	String length = "";
	String views = "";
	String avgRate = "";	// Ranges from 1 to 5 stars
	String totalRate = "";
	String totalComment = "";
	
	video next = null;
	
	public video getTail() {
		video currentNode = this;
		while (currentNode.next!=null) {
			currentNode = currentNode.next;
		}
		return(currentNode);
	}
	
	public String toString() {
		return("Video: " + vidID + "\n" +
			   "Uploader: " + uploader + "\n" +
			   "Age: " + age + "\n" +
			   "Category: " + category + "\n" +
			   "Length: " + length + "\n" +
			   "Views: " + views + "\n" +
			   "Ratings Average: " + avgRate + "\n" +
			   "Number of Ratings: " + totalRate + "\n" +
			   "Number of Comments: " + totalComment + "\n");
	}
}
