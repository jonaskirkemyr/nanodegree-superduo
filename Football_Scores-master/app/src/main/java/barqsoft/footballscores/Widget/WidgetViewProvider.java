package barqsoft.footballscores.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jonas on 13.01.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetViewProvider implements RemoteViewsService.RemoteViewsFactory
{
    private Cursor cursor;
    private Context context;

    private ArrayList<String> list;

    public WidgetViewProvider(Context context, Intent intent)
    {
        this.context = context;
        cursor = null;
        list = new ArrayList<String>();
    }


    @Override
    public void onCreate()
    {
        list.add("1");
    }

    @Override
    public void onDataSetChanged()
    {
        //fetch data and move cursor
        Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        cursor = context.getContentResolver().query(dateUri, new String[]{
                DatabaseContract.scores_table.DATE_COL,
                DatabaseContract.scores_table.HOME_COL,
                DatabaseContract.scores_table.AWAY_COL,
                DatabaseContract.scores_table.HOME_GOALS_COL,
                DatabaseContract.scores_table.AWAY_GOALS_COL,
        }, DatabaseContract.scores_table.DATE_COL, new String[]{today}, null);
    }

    @Override
    public void onDestroy()
    {
        if (cursor != null)
            cursor.close();
    }

    @Override
    public int getCount()
    {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i)
    {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        cursor.moveToPosition(i);

        //match data
        String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.DATE_COL));
        String home = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.HOME_COL));
        String home_goals = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.HOME_GOALS_COL));
        String away = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.AWAY_COL));
        String away_goals = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.AWAY_GOALS_COL));

        //images
        int home_crest = Utilies.getTeamCrestByTeamName(home);
        int away_crest = Utilies.getTeamCrestByTeamName(home);

        //make sure only "legal" goals will be printed
        if (Integer.valueOf(home_goals) == -1)
            home_goals = "";

        if (Integer.valueOf(away_goals) == -1)
            away_goals = "";

        String formattedDate = "";
        try
        {
            Date formatDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            formattedDate = new SimpleDateFormat(context.getString(R.string.date_format)).format(formatDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }


        view.setTextViewText(R.id.date_textview, formattedDate);
        view.setTextViewText(R.id.home_name, home);
        view.setTextViewText(R.id.away_name, away);
        view.setTextViewText(R.id.score_textview, home_goals + " : " + away_goals);

        view.setImageViewResource(R.id.home_crest, home_crest);
        view.setImageViewResource(R.id.away_crest, away_crest);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(WidgetProvider.EXTRA_WORD, i);
        view.setOnClickFillInIntent(R.id.widgetlistView, fillInIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView()
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }
}
