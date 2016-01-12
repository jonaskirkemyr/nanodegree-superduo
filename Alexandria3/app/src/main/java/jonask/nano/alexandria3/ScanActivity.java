package jonask.nano.alexandria3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Jonas on 04.01.2016.
 *
 * @src https://github.com/dm77/barcodescanner#simple-usage
 */
public class ScanActivity extends Activity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        scannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause()
    {
        super.onPause();
        scannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result)
    {
        Intent i = new Intent();
        i.putExtra(AddBook.ISBN_SCAN, result.getText());

        setResult(RESULT_OK, i);
        finish();
    }
}
