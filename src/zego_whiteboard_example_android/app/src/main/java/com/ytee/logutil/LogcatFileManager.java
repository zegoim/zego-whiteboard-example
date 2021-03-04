package com.ytee.logutil;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.content.Context;
import android.os.Environment;


public class LogcatFileManager {
    private static LogcatFileManager INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String LOGCAT_FILENAME_PREFIX = "logcat-";
    private String LOGCAT_FILENAME_EXTENSION = ".log";

    public static LogcatFileManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LogcatFileManager();
        }
        return INSTANCE;
    }


    private LogcatFileManager() {
        mPId = android.os.Process.myPid();
    }


    public void startLogcatManager(Context context) {
        String folderPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && context.getExternalFilesDir(null) != null) {
            folderPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "zegologs";
        } else {
            folderPath = context.getFilesDir().getAbsolutePath() + File.separator + "zegologs";
        }

        LogcatFileManager.getInstance().deleteOldenDaysLogs(folderPath, 7);
        LogcatFileManager.getInstance().start(folderPath);
    }


    public void stopLogcatManager() {
        LogcatFileManager.getInstance().stop();
    }


    private void setFolderPath(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("The logcat folder path is not a directory: " + folderPath);
        }


        PATH_LOGCAT = folderPath.endsWith("/") ? folderPath : folderPath + "/";
//        LogUtils.d(PATH_LOGCAT);
    }


    public void start(String saveDirectory) {
        setFolderPath(saveDirectory);
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        }
        mLogDumper.start();
    }


    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private void deleteOldenDaysLogs(String saveDirectory, int boundDays) {
        long oldenDaysBoundMs = (new Date().getTime()/1000 - boundDays * 24 * 60 * 60) * 1000;  // 7天前为界
        long oldenDaysBoundDay = Long.parseLong(simpleDateFormat1.format(new Date(oldenDaysBoundMs)));

        File[] subFiles = new File(saveDirectory).listFiles();
        if (subFiles == null) return;
        for (File file : subFiles) {
            if (!file.isFile()) {
                continue;
            } else if (!file.getName().startsWith(LOGCAT_FILENAME_PREFIX)) {
                continue;
            }

            String fileName = file.getName().replace(LOGCAT_FILENAME_PREFIX, "").replace(LOGCAT_FILENAME_EXTENSION, "");
            try {
                long fileNameDay = Long.parseLong(fileName);
                if (fileNameDay < oldenDaysBoundDay) {
                    file.delete();
                }
            } catch (NumberFormatException exception) {

            }
        }
    }

    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;


        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, LOGCAT_FILENAME_PREFIX + simpleDateFormat1.format(new Date()) + LOGCAT_FILENAME_EXTENSION), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            /**
             * * * log level：*:v , *:d , *:w , *:e , *:f , *:s * * Show the
             * current mPID process level of E and W log. * *
             */
            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            cmds = "logcat *:e *:w *:i *:d | grep \"(" + "im.zego.gochat" + ")\"";
        }


        public void stopLogs() {
            mRunning = false;
        }


        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((line + "\n").getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }


    }
}

