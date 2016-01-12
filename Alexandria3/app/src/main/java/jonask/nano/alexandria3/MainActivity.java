package jonask.nano.alexandria3;

import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import jonask.nano.alexandria3.api.Callback;
import jonask.nano.alexandria3.services.BookService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Callback
{
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    private static final String MAINFRAGMENT_TAG = "MTAG";
    private static final String DETAILFRAGMENT_TAG = "DTAG";

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    private static final String STORE_EAS_KEY = "easstore";

    private String activeEan = "";

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //set layout, toolbar and sidebar
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        IS_TABLET = isTablet();
        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        if (savedInstanceState != null)//restore current active ean
            activeEan = savedInstanceState.getString(STORE_EAS_KEY);
        else
            openPrefferedStartScreen();


        FragmentManager fManager = getSupportFragmentManager();
        //check if tablet is in landscape mode and whether the detailfragment is in the container..
        if (fManager.findFragmentByTag(DETAILFRAGMENT_TAG) != null && findViewById(R.id.right_container) != null)
        {
            int count = fManager.getBackStackEntryCount();
            if (count > 0)//..check if any history exists to go back to, to fill the container
                getSupportFragmentManager().popBackStack();
        }


        updatePage();
    }

    private void updatePage()
    {
        FragmentTitle currentFragment = (FragmentTitle) getSupportFragmentManager().findFragmentByTag(MAINFRAGMENT_TAG);
        if (currentFragment != null)
        {
            setTitle(currentFragment.getTitleResource());
            int navId=currentFragment.getNavigationResource();//check sidebar item
            if(navId!=0)
                navigationView.setCheckedItem(navId);
        }
    }

    /**
     * Load preferences, and start preferred start page according to chosen start page
     */
    private void openPrefferedStartScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int fragment = Integer.parseInt(sharedPref.getString(SettingsActivity.GeneralPreferenceFragment.KEY_PREF_START_FRAGMENT, getString(R.string.default_pref_start)));

        fragment=(fragment==0)?R.id.nav_list_books:R.id.nav_add_book;

        navigationView.getMenu().performIdentifierAction(fragment, 0);
        navigationView.setCheckedItem(fragment);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
            updatePage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTitle nextFragment = null;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            default:
            case R.id.nav_list_books:
                nextFragment = new ListOfBooks();
                break;
            case R.id.nav_add_book:
                nextFragment = new AddBook();
                break;

            case R.id.nav_about:
                nextFragment = new About();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        //set title for the current fragment to be replaced, and put in container
        setTitle(nextFragment.getTitleResource());
        fragmentManager.beginTransaction().replace(R.id.container, nextFragment, MAINFRAGMENT_TAG).addToBackStack(null).commit();
        return true;
    }

    @Override
    public void onItemSelected(String ean)
    {

        View right = findViewById(R.id.right_container);

        if (right != null && activeEan.equals(ean))//if right container is defined and ean is already selected..
            return;//..don't update view

        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);//arguments to pass to fragment
        activeEan = ean;//set input ean as current active

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        FragmentTransaction temp = getSupportFragmentManager().beginTransaction();

        if (right != null)//if tablet, replace right container with new fragment
        {
            temp.replace(R.id.right_container, fragment);
        }
        else
        {
            temp.replace(R.id.container, fragment, DETAILFRAGMENT_TAG);
        }

        temp.addToBackStack(getString(fragment.getTitleResource())).commit();
    }

    private boolean isTablet()
    {
        return (getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    private class MessageReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getStringExtra(MESSAGE_KEY) != null)
            {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setTitle(int titleId)
    {
        ActionBar bar = getSupportActionBar();
        if (bar != null && titleId != 0)
            bar.setTitle(getString(titleId));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        bundle.putString(STORE_EAS_KEY, activeEan);//store current active eas
        super.onSaveInstanceState(bundle);
    }
}
