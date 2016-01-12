package jonask.nano.alexandria3;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jonas on 06.01.2016.
 */
public class About extends FragmentTitle
{
    protected final int TITLE=R.string.about;
    public About(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public int getTitleResource()
    {
        return TITLE;
    }

    @Override
    public int getNavigationResource()
    {
        return R.id.nav_about;
    }
}
