package jonask.nano.alexandria3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Jonas on 05.01.2016.
 */
public class Utility
{
    public static boolean hasInternetConnection(Context context)
    {
        try
        {
            NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
        }
        catch (NullPointerException e)
        {
        }
        return false;
    }
}
