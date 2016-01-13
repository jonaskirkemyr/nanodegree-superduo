package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;
import barqsoft.footballscores.Widget.WidgetViewProvider;

/**
 * Created by Jonas on 13.01.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new WidgetViewProvider(this.getApplicationContext(), intent);
    }
}
