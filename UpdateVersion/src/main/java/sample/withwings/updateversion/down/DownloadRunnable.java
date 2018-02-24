package sample.withwings.updateversion.down;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;

import sample.withwings.updateversion.down.info.TaskInfo;

public class DownloadRunnable implements Runnable {
    private TaskInfo mTaskInfo;//下载信息JavaBean

    private boolean isStop;//是否暂停

    private DownLoadingListener mDownLoadingListener;

    /**
     * 构造器
     * @param info 任务信息
     */
    public DownloadRunnable(TaskInfo info, DownLoadingListener downLoadingListener) {
        mTaskInfo = info;
        mDownLoadingListener = downLoadingListener;
    }

    /**
     * 停止下载
     */
    public void stop() {
        isStop = true;
    }

    /**
     * Runnable的run方法，进行文件下载
     */
    @Override
    public void run() {
        getFileName(mTaskInfo.getUrl());
    }

    public void startDown() {

        HttpURLConnection conn;//http连接对象
        BufferedInputStream bis;//缓冲输入流，从服务器获取
        RandomAccessFile raf;//随机读写器，用于写入文件，实现断点续传
        int len = 0;//每次读取的数组长度
        byte[] buffer = new byte[1024 * 8];//流读写的缓冲区
        try {
            //通过文件路径和文件名实例化File
            File file = new File(mTaskInfo.getPath() + mTaskInfo.getName());
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            //实例化RandomAccessFile，rwd模式
            raf = new RandomAccessFile(file, "rwd");
            conn = (HttpURLConnection) new URL(mTaskInfo.getUrl()).openConnection();
            conn.setConnectTimeout(120000);//连接超时时间
            conn.setReadTimeout(120000);//读取超时时间
            conn.setRequestMethod("GET");//请求类型为GET
            if (mTaskInfo.getContentLen() == 0) {//如果文件长度为0，说明是新任务需要从头下载
                //获取文件长度
                mTaskInfo.setContentLen(Long.parseLong(conn.getHeaderField("content-length") == null ? "0" : conn.getHeaderField("content-length")));
            } else {//否则设置请求属性，请求制定范围的文件流
                conn.setRequestProperty("Range", "bytes=" + mTaskInfo.getCompletedLen() + "-" + mTaskInfo.getContentLen());
            }
            raf.seek(mTaskInfo.getCompletedLen());//移动RandomAccessFile写入位置，从上次完成的位置开始
            conn.connect();//连接
            bis = new BufferedInputStream(conn.getInputStream());//获取输入流并且包装为缓冲流
            //从流读取字节数组到缓冲区
            while (!isStop && -1 != (len = bis.read(buffer))) {
                //把字节数组写入到文件
                raf.write(buffer, 0, len);
                //更新任务信息中的完成的文件长度属性
                mTaskInfo.setCompletedLen(mTaskInfo.getCompletedLen() + len);
            }
            if (len == -1) {//如果读取到文件末尾则下载完成
                Log.i("tag", "下载完了");
            } else {//否则下载系手动停止
                Log.i("tag", "下载停止了");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(e instanceof SSLException) {
                mDownLoadingListener.netWorkError();
            }
            if(e instanceof UnknownHostException) {
                mDownLoadingListener.netWorkError();
            }
        }
    }



    public void getFileName(String url) {
        String filename = "";
        boolean isok = false;
        // 从UrlConnection中获取文件名称
        try {
            URL myURL = new URL(url);

            URLConnection conn = myURL.openConnection();
            if (conn == null) {
                filename = null;
            }
            Map<String, List<String>> hf = conn.getHeaderFields();
            if (hf == null) {
                filename = null;
            }
            Set<String> key = hf.keySet();
            if (key == null) {
                filename = null;
            }

            for (String skey : key) {
                List<String> values = hf.get(skey);
                for (String value : values) {
                    String result;
                    try {
                        result = new String(value.getBytes("ISO-8859-1"), "GBK");
                        int location = result.indexOf("filename");
                        if (location >= 0) {
                            result = result.substring(location
                                    + "filename".length());
                            filename = result
                                    .substring(result.indexOf("=") + 1);
                            isok = true;
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }// ISO-8859-1 UTF-8 gb2312
                }
                if (isok) {
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 从路径中获取
        if (filename == null || "".equals(filename)) {
            filename = url.substring(url.lastIndexOf("/") + 1);

            int i = filename.indexOf(".apk");
            filename = filename.substring(0, i + ".apk".length());
        }

        mTaskInfo.setName(filename);
        startDown();
    }
}
