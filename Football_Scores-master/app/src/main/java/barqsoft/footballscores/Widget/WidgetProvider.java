package barqsoft.footballscores.Widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.WidgetService;

/**
 * Created by Jonas on 12.01.2016.
 */
public class WidgetProvider extends android.appwidget.AppWidgetProvider
{
    public static String EXTRA_WORD = "WORD";
    public static String UPDATE_LIST = "UPDATE_LIST";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++)
        {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, WidgetService.class);

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
            view.setRemoteAdapter(appWidgetId, R.id.widgetlistView, intent);

            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent pending = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setPendingIntentTemplate(R.id.widgetlistView, pending);

            clickIntent = new Intent(context, WidgetProvider.class);
            clickIntent.setAction(UPDATE_LIST);
            PendingIntent refresh = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
            view.setOnClickPendingIntent(R.id.update_list, refresh);

            appWidgetManager.updateAppWidget(appWidgetId, view);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        if (intent.getAction().equals(UPDATE_LIST))
        {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, WidgetProvider.class);
            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(component), R.id.widgetlistView);

        }
    }
}