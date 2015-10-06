import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Renamer {

	private String url = "";
	private ArrayList<String[]> shows = new ArrayList<String[]>();
	private HashMap<String, String[]> data = new HashMap<String, String[]>();
	private JTextArea textArea;

	public Renamer() {
		this.setupLayout();
		this.fetchShowInfo();
		this.runScript();
	}

	private void setupLayout() {
		JFrame frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		this.textArea = new JTextArea("Starting script...");
		this.textArea.setSize(640, 480);
		this.textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.textArea);
		frame.add(scrollPane);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

	private void appendOutput(String output) {
		this.textArea.append("\n" + output);
		this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
	}

	private void fetchShowInfo() {
		try {
			this.appendOutput("Downloading show info...");
			URL url = new URL(this.url);
			InputStream is = url.openStream();
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			String body = scanner.hasNext() ? scanner.next() : "";
			String[] lines = body.split("\n");
			for (String line : lines) {
				this.shows.add(line.split("#"));
			}
		} catch (Exception ex) {
			this.appendOutput("Error occurred: " + ex.getMessage());
		}
	}

	private void runScript() {
		String dirName = "./";
		File dir = new File(dirName);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			this.appendOutput("Processing '" + files[i].getName() + "'...");
			String fileName = files[i].getName();
			if (fileName.length() > 14 && fileName.substring(0, 14).toLowerCase().equals("the.daily.show")) {
				String date = fileName.substring(15, 25).replace(".", "-");
				String title = fileName.substring(26).toLowerCase().split(".hdtv")[0].replace(".", " ");
				try {
					String newName = "The Daily Show - " + date + " - " + this.capitalizeString(title) + "." + this.getExt(files[i].getName());
					this.appendOutput("Renaming to '" + newName + "'...");
					this.renameFile(files[i], newName);
				} catch (Exception e) {
					this.appendOutput("Error occurred: " + e.getMessage());
				}
			} else if (fileName.length() > 15 && fileName.substring(0, 15).toLowerCase().equals("david.letterman")) {
				String date = fileName.substring(16, 26).replace(".", "-");
				String title = fileName.substring(27).toLowerCase().split(".hdtv")[0].replace(".", " ");
				try {
					String newName = "David Letterman - " + date + " - " + this.capitalizeString(title) + "." + this.getExt(files[i].getName());
					this.appendOutput("Renaming to '" + newName + "'...");
					this.renameFile(files[i], newName);
				} catch (Exception e) {
					this.appendOutput("Error occurred: " + e.getMessage());
				}
			} else if (fileName.length() > 12 && fileName.substring(0, 12).toLowerCase().equals("jimmy.fallon")) {
				String date = fileName.substring(13, 23).replace(".", "-");
				String title = fileName.substring(24).toLowerCase().split(".hdtv")[0].replace(".", " ");
				this.appendOutput(fileName.substring(24).toLowerCase().split(".hdtv")[0] + ": " + fileName.substring(24).toLowerCase().split(".hdtv")[1]);
				try {
					String newName = "Jimmy Fallon - " + date + " - " + this.capitalizeString(title) + "." + this.getExt(files[i].getName());
					this.appendOutput("Renaming to '" + newName + "'...");
					this.renameFile(files[i], newName);
				} catch (Exception e) {
					this.appendOutput("Error occurred: " + e.getMessage());
				}
			} else {
				fileName = this.simplifyName(fileName);
				String se = "(.+?)([s]{0,1})([0-9]{1,2})([ex]{1})([0-9]{1,2})(.*?)";
				if (!Pattern.matches(se, fileName)) {
					se = "(.+?)(([0-9]{1,2}))(([0-9]{2}))(.*?)";
					if (!Pattern.matches(se, fileName)) {
						this.appendOutput("Skipping file...");
						continue;
					}
				}
				Pattern p = Pattern.compile(se);
				Matcher m = p.matcher(fileName);
				m.find();
				try {
					String[] info = this.identifyShow(m.group(1));
					String title = this.fetchData(info[1], m.group(3), m.group(5));
					String newName = info[0] + " - s" + this.leadingZero(m.group(3)) + "e" + this.leadingZero(m.group(5)) + title + "." + this.getExt(files[i].getName());
					this.appendOutput("Renaming to '" + newName + "'...");
					this.renameFile(files[i], newName);
				} catch (Exception e) {
					this.appendOutput("Error occurred: " + e.getMessage());
				}
			}
		}
		this.appendOutput("Finished");
	}

	private String simplifyName(String fileName) {
		return fileName.toLowerCase().replace(".", " ");
	}

	private String getExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}

	private String[] identifyShow(String name) {
		String[] info = {};
		for (String[] show : this.shows) {
			if (name.contains(show[1])) {
				info = new String[]{ show[0], show[2] };
			}
		}
		return info;
	}

	private String fetchData(String imdb, String season, String episode) {
		int s = Integer.parseInt(season);
		int e = Integer.parseInt(episode);
		if (this.data.containsKey(imdb)) {
			String[] data = this.data.get(imdb);
			if (data[s] != null && data[s] != "") {
				return this.fetchTitle(data[s], s, e);
			}
		}
		try {
			this.appendOutput("Downloading 'http://www.imdb.com/title/" + imdb + "/episodes?season=" + s + "'...");
			URL url = new URL("http://www.imdb.com/title/" + imdb + "/episodes?season=" + s);
			InputStream is = url.openStream();
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			String body = scanner.hasNext() ? scanner.next() : "";
			if (!this.data.containsKey(imdb)) {
				String[] seasons = new String[20];
				seasons[s] = body;
				this.data.put(imdb, seasons);
			} else {
				String[] seasons = this.data.get(imdb);
				seasons[s] = body;
			}
			return this.fetchTitle(body, s, e);
		} catch (Exception ex) {
			this.appendOutput("Error occurred: " + ex.getMessage());
		}
		return "";
	}

	private String fetchTitle(String body, int s, int e) {
		String[] lines = body.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].matches("<div>S" + s + ", Ep" + e + "</div>")) {
				String se = "<img(.*?)alt=\"(.*?)\" src=\"(.*?)\">";
				if (!Pattern.matches(se, lines[i-1])) return "";
				Pattern p = Pattern.compile(se);
				Matcher m = p.matcher(lines[i-1]);
				m.find();
				return " - " + this.cleanTitle(m.group(2));
			}
		}
		return "";
	}

	private String leadingZero(String str) {
		if (str.length() == 1) {
			return "0" + str;
		}
		return str;
	}

	private String cleanTitle(String title) {
		return title.replace(":", "").replace("\\", "").replace("/", "").replace("*", "").replace("?", "").replace("\"", "").replace("<", "").replace(">", "").replace("|", "");
	}

	private String capitalizeString(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	private void renameFile(File file, String name) throws IOException {
		File tempFile = new File(file.getParent() + "/temp-" + name);
		if(tempFile.exists()) {
			return;
		}
		boolean success = file.renameTo(tempFile);
		if (!success) {
			throw new IOException("File could not be renamed");
		}

		File file2 = new File(file.getParent() + "/" + name);
		if(file2.exists()) {
			return;
		}
		boolean success2 = tempFile.renameTo(file2);
		if (!success2) {
			throw new IOException("File could not be renamed");
		}
	}

	public static void main(String[] args) {
		Renamer r = new Renamer();
	}
}