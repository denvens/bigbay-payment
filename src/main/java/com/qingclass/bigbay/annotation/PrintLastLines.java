package com.qingclass.bigbay.annotation;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
 
public class PrintLastLines {
 
    public static void main(String[] args) {
 
        final Path path = Paths.get("D:\\Exam\\test-files\\large.txt");
        RandomAccessFile randomAccessFile = null;
        int lineNumValue = 12;
        try {
            randomAccessFile = new RandomAccessFile(path.toFile(), "r");
 
 
            printLastLines(randomAccessFile, lineNumValue);
 
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
 
    /**
     * 正序打印文本最后几行
     * @param randomAccessFile
     * @param lineNumValue
     * @return
     * @throws IOException
     */
    private static void printLastLines(RandomAccessFile randomAccessFile, int lineNumValue) throws IOException {
        long length = randomAccessFile.length();
        if (length == 0L) {
            return;
        }
        long seekNext = length - 1;
        while (seekNext > 0 && lineNumValue > 0) {
            randomAccessFile.seek(seekNext);
            seekNext --;
            if (randomAccessFile.read() == '\n') {
                lineNumValue --;
            }
        }
 
        byte[] bytes = new byte[(int) (length - seekNext)];
        randomAccessFile.read(bytes);
        System.out.println(new String(bytes));
    }
 
    /**
     * 倒序打印出文本文件的最后n行文本
     * @param randomAccessFile
     * @param lineNumValue
     * @throws IOException
     */
    private static void reversePrintLastLines(RandomAccessFile randomAccessFile, int lineNumValue) throws IOException {
        long length = randomAccessFile.length();
        if (length == 0L) {
            return;
        }
        long seekNext = length - 1;
        randomAccessFile.seek(seekNext);
 
        int lineNum = 0;
        while (seekNext >= 0) {
            int read = randomAccessFile.read();
            //只有行与行之间才有\r\n，这表示读到每一行上一行的末尾的\n，而执行完read后，指针指到了这一行的开头字符
            if (read == '\n' && lineNum <= lineNumValue - 1) {
                //RandomAccessFile的readLine方法读取文本为ISO-8859-1，需要转化为windows默认编码gbk
                lineNum ++;
                printLine(randomAccessFile, lineNum);
            }
            // 当文件指针退至文件开始处，输出第一行
            if (seekNext == 0) {
                lineNum ++;
                randomAccessFile.seek(0);//不需要为下次做准备了，但是因为read()方法指针从0到了1，需重置为0
 
                if (lineNum <= lineNumValue - 1) {
                    printLine(randomAccessFile, lineNum);
                }
            } else {
                //为下一次循环做准备
                randomAccessFile.seek(seekNext - 1);
            }
            seekNext --;
        }
    }
 
    /**
     * 倒序打印方法二
     * @param randomAccessFile
     * @throws IOException
     */
    private static void reversePrint2(RandomAccessFile randomAccessFile) throws IOException {
        long length = randomAccessFile.length();
        if (length == 0L) {
            return;
        }
        long seekNext = length - 1;
        randomAccessFile.seek(seekNext);
 
        int lineNum = 0;
        while (seekNext >= 0) {
            int read = randomAccessFile.read();
            //只有行与行之间才有\r\n，这表示读到每一行上一行的末尾的\n，而执行完read后，指针指到了这一行的开头字符
            if (read == '\n') {
                //RandomAccessFile的readLine方法读取文本为ISO-8859-1，需要转化为windows默认编码gbk
                lineNum ++;
                printLine(randomAccessFile, lineNum);
            }
            // 当文件指针退至文件开始处，输出第一行
            if (seekNext == 0) {
                lineNum ++;
                randomAccessFile.seek(0);//不需要为下次做准备了，但是因为read()方法指针从0到了1，需重置为0
                printLine(randomAccessFile, lineNum);
            } else {
                //为下一次循环做准备
                randomAccessFile.seek(seekNext - 1);
            }
            seekNext --;
        }
    }
 
    /**
     * 倒序打印方法一
     * @param randomAccessFile
     * @throws IOException
     */
    private static void reversePrint(RandomAccessFile randomAccessFile) throws IOException {
        long length = randomAccessFile.length();
        if (length == 0L) {
            return;
        }
        long seekNext = length - 1;
        randomAccessFile.seek(seekNext);
 
        int lineNum = 0;
        while (seekNext >= 0) {
            int read = randomAccessFile.read();
            //只有行与行之间才有\r\n，这表示读到每一行上一行的末尾的\n，而执行完read后，指针指到了这一行的开头字符
            if (read == '\n') {
                //RandomAccessFile的readLine方法读取文本为ISO-8859-1，需要转化为windows默认编码gbk
                lineNum ++;
                printLine(randomAccessFile, lineNum);
            }
            //这一句必须在这个位置，如果在nextend--后，那么导致进0循环后去seek-1索引，报异常,如果在read()以前，那么导致进入0循环时，因为read指针到1，第一行少读取一个字符
            randomAccessFile.seek(seekNext);
            // 当文件指针退至文件开始处，输出第一行
            if (seekNext == 0) {
                lineNum ++;
                printLine(randomAccessFile, lineNum);
            }
            seekNext --;
        }
    }
 
    private static void printLine(RandomAccessFile randomAccessFile, int lineNum) throws IOException {
        String line = randomAccessFile.readLine();
        if (line == null || "".equals(line.trim())) {
            System.out.println(lineNum + "  " + (line == null ? "" : line));
        } else {
            System.out.println(lineNum + "  " + new String(line.getBytes(StandardCharsets.ISO_8859_1), "UTF-8"));
        }
    }
 
    /**
     * 正序打印所有行
     * @param randomAccessFile
     * @throws IOException
     */
    private static void positiveSequencePrint(RandomAccessFile randomAccessFile) throws IOException {
        String line;
        //行号
        int lineNum = 0;
        while ((line = randomAccessFile.readLine()) != null) {
            System.out.println(++lineNum + "  " + new String(line.getBytes(StandardCharsets.ISO_8859_1), "UTF-8"));
        }
    }
}