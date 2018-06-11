package com.cftf.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileManager {

	private File file = null;
	private BufferedReader bufferReader = null;
	private BufferedWriter bufferWriter = null;
	public long fileLength = 0l;

	public FileManager() {

	}

	public FileManager(String pathname) {
		this.file = new File(pathname);
	}

	public String[] getFileList() {
		String[] files = this.file.list();
		return files;
	}

	private void visitAllFiles(ArrayList files, File dir) {

		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File f : children) {

				visitAllFiles(files, f);
			}
		} else {
			files.add(dir);
		}
	}

	public String[] getFileFullPathList() {

		ArrayList<String> arrfile = new ArrayList<String>();
		ArrayList<File> files = new ArrayList<File>();
		visitAllFiles(files, this.file);
		for (File f : files) {
			String line = f.getAbsolutePath();

			arrfile.add(line);
		}
		return arrfile.toArray(new String[0]);
	}

	public String extractFileName(String fullPath) {
		int beginIndex = fullPath.lastIndexOf(File.separatorChar) + 1;
		int endIndex = fullPath.length();
		String fileName = fullPath.substring(beginIndex, endIndex);
		return fileName;

	}

	public BufferedReader openDocumentReader(String fullPath) throws IOException {

		String szTextEncoding = "UTF-8";
		File file = new File(fullPath);
		this.fileLength = file.length();
		
		
		FileInputStream in = new FileInputStream(file);
		int len = 0;
		byte[] leadByte = new byte[3];

		len = in.read(leadByte, 0, 2);
		if (len == 2) {
			if ((leadByte[0] == (byte) 0xFF) && (leadByte[1] == (byte) 0xFE))
				szTextEncoding = "UTF-16LE";
			else if ((leadByte[0] == (byte) 0xFE) && (leadByte[1] == (byte) 0xFF))
				szTextEncoding = "UTF-16BE";
			else if ((leadByte[0] == (byte) 0xEF) && (leadByte[1] == (byte) 0xBB)) {
				len = in.read(leadByte, 0, 1);
				if ((len >= 1) && (leadByte[0] == (byte) 0xBF))
					szTextEncoding = "UTF-8";
				else {
					in.close();
					in = new FileInputStream(file);
				}
				;
			} else {
				in.close();
				in = new FileInputStream(file);
			}
			;
		}

		else {
			in.close();
			in = new FileInputStream(file);
		}
		;

		if (szTextEncoding.isEmpty()) {
			this.bufferReader = new BufferedReader(new InputStreamReader(in));
		} else {
			this.bufferReader = new BufferedReader(new InputStreamReader(in, szTextEncoding));
			//this.bufferReader = new BufferedReader(new InputStreamReader(in, "euc-kr"));

		}

		return this.bufferReader;
	}

	public void closeDocumentReader() throws IOException {
		this.bufferReader.close();
	}

	synchronized public void openBufferWriter(String fullPath) throws IOException {
		// if (this.bufferWriter== null) {
		// this.bufferWriter = new BufferedWriter(new FileWriter(fullPath));
		// }
		if (this.file == null) {
			this.file = new File(fullPath);
			//String szTextEncoding = "UTF-8";
			String szTextEncoding = "EUC-KR";
			this.bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), szTextEncoding));
		}
	}

	synchronized public void writeBuffer(String text) throws IOException {
		this.bufferWriter.write(text);
		this.bufferWriter.flush();

	}

	public void writeBufferAppend(String text) throws IOException {
		this.bufferWriter.append(text);

	}

	synchronized public String closeBuffer() throws IOException {
		String fullFilePath = this.file.getAbsolutePath();
		if (this.bufferWriter != null) {
			this.bufferWriter.flush();
			this.bufferWriter.close();
			this.bufferWriter = null;
			this.file = null;
		}
		return fullFilePath;
	}

	public void bufferCheck(String path) throws IOException {
		closeBuffer();
		long l = this.file.length();
		if (this.file.length() > 104857600) {
			openBufferWriter(path);
		} else {
			openBufferWriter(this.file.getPath());
		}
	}

}
