package jonask.nano.alexandria3;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import jonask.nano.alexandria3.api.BookListAdapter;
import jonask.nano.alexandria3.api.Callback;
import jonask.nano.alexandria3.data.AlexandriaContract;


public class ListOfBooks extends FragmentTitle implements LoaderManager.LoaderCallbacks<Cursor>
{
    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int currentSelectedBook = ListView.INVALID_POSITION;
    private EditText searchText;
    private final int LOADER_ID = 10;

    private static final String STORE_CURRENT_POSITION = "bookpos";

    public final int TITLE = R.string.books;


    public ListOfBooks()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            currentSelectedBook = savedInstanceState.getInt(STORE_CURRENT_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Cursor cursor = getActivity().getContentResolver().query(AlexandriaContract.BookEntry.CONTENT_URI, null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );


        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);


        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        searchText = (EditText) rootView.findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)//update list everytime text is changed
            {
                ListOfBooks.this.restartLoader();
            }
        });

        //since list is updated on each text input, not really need for handling click events on button.
        /*vSearchButton=(ImageButton)rootView.findViewById(R.id.searchButton);
        vSearchButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.d("LIST", "onclick search");
                        ListOfBooks.this.restartLoader();
                    }
                }
        );*/

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position))
                {//be sure same book isn't selected in tablet mode

                    currentSelectedBook = position;
                    ((Callback) getActivity()).onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    private void restartLoader()
    {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = searchText.getText().toString();

        if (searchString.length() > 0)
        {
            searchString = "%" + searchString + "%";
            return new CursorLoader(getActivity(), AlexandriaContract.BookEntry.CONTENT_URI, null, selection, new String[]{
                    searchString,
                    searchString
            }, null);
        }

        return new CursorLoader(getActivity(), AlexandriaContract.BookEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        bookListAdapter.swapCursor(data);
        if (currentSelectedBook != ListView.INVALID_POSITION)
        {
            bookList.smoothScrollToPosition(currentSelectedBook);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        bookListAdapter.swapCursor(null);
    }


    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        bundle.putInt(STORE_CURRENT_POSITION, currentSelectedBook);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public int getTitleResource()
    {
        return TITLE;
    }

    @Override
    public int getNavigationResource()
    {
        return R.id.nav_list_books;
    }
}
