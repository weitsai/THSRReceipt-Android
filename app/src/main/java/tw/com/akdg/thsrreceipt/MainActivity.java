package tw.com.akdg.thsrreceipt;

import com.google.zxing.Result;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class MainActivity extends Activity implements ZXingScannerView.ResultHandler {

    private static final String TAG = MainActivity.class.getName();

    public static final int MAIL_RESULT = 0;

    private ZXingScannerView mZXingScannerView;

    private Handler mHandler = new Handler();

    private Receipt mReceipt;

    private final static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mReceipt = new Receipt(this);
        mZXingScannerView = (ZXingScannerView) findViewById(R.id.view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mZXingScannerView.setResultHandler(this);
        mZXingScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mZXingScannerView.stopCamera();
    }

    private TextView mTextView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_pdf_count);
        menuItem.setActionView(R.layout.action_num_message);
        mTextView = (TextView) menuItem.getActionView().findViewById(R.id.textView);
        updatePDFCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            try {
                sendMail(new File(mReceipt.getZipFilePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMail(File fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                getPreferences(Context.MODE_PRIVATE).getString("RECEIVEMAIL", "")});
        intent.setClassName("com.google.android.gm",
                "com.google.android.gm.ComposeActivityGmail");
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_SUBJECT,
                String.format("%s_%s", getString(R.string.mail_title),
                        mDateFormat.format(System.currentTimeMillis())));
        intent.putExtra(Intent.EXTRA_TEXT, "");
	intent.putExtra(Intent.EXTRA_STREAM,
		Uri.parse("content://" + CachedFileProvider.getAuthority() + "/" + fileName.getName()));
        startActivityForResult(intent, MAIL_RESULT);
    }

    @Override
    public void handleResult(final Result result) {
        String qrcode = result.getText();
        if (qrcode.length() != 124) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Please re-scan.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        final String pnr = qrcode.substring(13, 21);
        final String tid = qrcode.substring(0, 13);
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.setMessage(getString(R.string.download));
                        mDialog.show();
                    }
                });
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mReceipt.downloadReceipt(pnr, tid);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NetworkErrorException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updatePDFCount();
                mZXingScannerView.startCamera();
                mDialog.dismiss();
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MAIL_RESULT) {
            File filePDFDir = getDir("PDF", Context.MODE_PRIVATE);
            for (File file : filePDFDir.listFiles()) {
                file.delete();
            }
            updatePDFCount();
        }
    }

    private void updatePDFCount() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                File filePDFDir = getDir("PDF", Context.MODE_PRIVATE);
                mTextView.setText(String.format("%d", filePDFDir.listFiles().length));
            }
        });
    }
}
