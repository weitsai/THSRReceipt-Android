package tw.com.akdg.thsrreceipt;

import com.google.zxing.Result;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class MainActivity extends Activity implements ZXingScannerView.ResultHandler{

  private static final String TAG = MainActivity.class.getName();

  private ZXingScannerView mZXingScannerView;

  private Handler mHandler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void handleResult(final Result result) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_LONG).show();

      }
    });
    String qrcode = result.getText();
    Log.d(TAG, "QR-Code = " + qrcode);
    Log.d(TAG, "Size = " + qrcode.toString().length());
  }
}
