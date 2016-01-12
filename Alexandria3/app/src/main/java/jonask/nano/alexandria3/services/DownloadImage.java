package jonask.nano.alexandria3.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import jonask.nano.alexandria3.Utility;

import java.io.InputStream;

/**
 * Created by saj on 11/01/15.
 */
public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private boolean hasInternet;

    public DownloadImage(ImageView bmImage) {
        this.bmImage = bmImage;
        //be sure that internet connection exists before trying to fetch image
        hasInternet= Utility.hasInternetConnection(bmImage.getContext());
    }

    protected Bitmap doInBackground(String... urls) {
        if(!hasInternet)
            return null;

        String urlDisplay = urls[0];
        Bitmap bookCover = null;
        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            bookCover = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bookCover;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}

