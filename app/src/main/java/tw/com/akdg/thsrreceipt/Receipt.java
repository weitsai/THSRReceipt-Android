package tw.com.akdg.thsrreceipt;

import android.accounts.NetworkErrorException;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by weitsai on 14/12/19.
 */
public class Receipt {

    private final String THSR_RECEIPT_URL = "http://www4.thsrc.com.tw/tc/TExp/page_print.asp?lang=tc&pnr=%s&tid=%s";
    private final String FILD_DIR_NAME = "PDF";
    private final String TAG = "Receipt";
    private Context context;

    /**
     *
     * @param context
     */
    public Receipt(Context context) {
        this.context = context;
    }

    /**
     *
     * @param pnr 訂位代號(8碼)
     * @param tid 票號(13碼)
     * @param file
     * @throws IOException
     */
    private void downloadReceipt(String pnr, String tid, File file) throws IOException, NetworkErrorException {
        if (pnr.length() != 8) {
            throw new NumberFormatException("pnr length number not 8");
        }

        if (tid.length() != 13) {
            throw new NumberFormatException("tid length number not 16");
        }

        if (!file.exists()) {
            file.mkdirs();
        }

        File pdf = new File(file.getAbsolutePath(), pnr + "-" + tid + ".pdf");
        if (pdf.exists()) {
            return;
        }

        URL mUrl = new URL(String.format(THSR_RECEIPT_URL, pnr, tid));
        HttpURLConnection mHttpURLCooenction = (HttpURLConnection) mUrl.openConnection();
        mHttpURLCooenction.setRequestMethod("GET");
        mHttpURLCooenction.connect();

        if (mHttpURLCooenction.getResponseCode() != 200) {
            throw new NetworkErrorException("Connent to " + String.format(THSR_RECEIPT_URL, pnr, tid) + ", status number code :" + mHttpURLCooenction.getResponseCode());
        }

        InputStream inputStream = mHttpURLCooenction.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(pdf);
        int totalSize = mHttpURLCooenction.getContentLength();
        byte[] buffer = new byte[totalSize];
        int bufferLength = 0;

        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, bufferLength);
        }

    }

    /**
     * @param pnr 訂位代號(8碼)
     * @param tid 票號(13碼)
     */
    public void downloadReceipt(String pnr, String tid) throws IOException, NetworkErrorException {
        if (pnr.length() != 8) {
            throw new NumberFormatException("pnr length number not 8");
        }

        if (tid.length() != 13) {
            throw new NumberFormatException("tid length number not 16");
        }
        downloadReceipt(pnr, tid, context.getDir(FILD_DIR_NAME, Context.MODE_PRIVATE));
    }

    /**
     * @param pnr  訂位代號(8碼)
     * @param tid  票號(13碼)
     * @param path 檔案存檔路徑
     */
    private void downloadReceipt(String pnr, String tid, String path) throws IOException, NetworkErrorException {
        if (pnr.length() != 8) {
            throw new NumberFormatException("tid length number not 8");
        }

        if (tid.length() != 13) {
            throw new NumberFormatException("pnr length number not 16");
        }

        downloadReceipt(pnr, tid, new File(path));
    }

    /**
     * 把所有 Receipt 打包成 zip
     * @return zip path
     * @throws IOException
     */
    public String getZipFilePath() throws IOException {
        File dir = context.getDir("", Context.MODE_PRIVATE);
        File zipFile = new File(dir.getAbsolutePath(), "Receipt.zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        File[] files = dir.listFiles();
        byte[] buffer = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            FileInputStream fis = new FileInputStream(files[i]);
            zos.putNextEntry(new ZipEntry(files[i].getName()));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();

        }

        return zipFile.getAbsolutePath();
    }
}
