package jonask.nano.alexandria3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import jonask.nano.alexandria3.data.AlexandriaContract;
import jonask.nano.alexandria3.services.BookService;
import jonask.nano.alexandria3.services.DownloadImage;


public class AddBook extends FragmentTitle implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";

    public static final String ISBN_SCAN = "ISBN_SCANNED";
    private final int ISBN_RESULT = 2;

    private Toast toast;

    protected final int TITLE = R.string.scan;

    private TextView vBookTitle;
    private TextView vBookSubtitle;
    private ImageView vBookCover;
    private TextView vAuthors;
    private TextView vCategories;
    private Button vDeleteBook;
    private Button vSaveBook;


    public AddBook()
    {
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (ean != null)
        {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);


        vBookTitle = (TextView) rootView.findViewById(R.id.bookTitle);
        vBookSubtitle = (TextView) rootView.findViewById(R.id.bookSubTitle);
        vBookCover = (ImageView) rootView.findViewById(R.id.bookCover);
        vAuthors = (TextView) rootView.findViewById(R.id.authors);
        vCategories = (TextView) rootView.findViewById(R.id.categories);
        vDeleteBook = (Button) rootView.findViewById(R.id.delete_button);
        vSaveBook = (Button) rootView.findViewById(R.id.save_button);
        ean = (EditText) rootView.findViewById(R.id.ean);


        ean.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978"))
                {
                    ean = "978" + ean;
                }
                if (ean.length() < 13)
                {
                    clearFields();
                    return;
                }
                startBookSearchIntent(ean);
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                Context context = getActivity();

                Intent qrScanner = new Intent(context, ScanActivity.class);
                qrScanner.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(qrScanner, ISBN_RESULT);
            }
        });

        vSaveBook.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ean.setText("");
            }
        });

        vDeleteBook.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null)
        {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }


    private void startBookSearchIntent(String ean)
    {
        //Once we have an ISBN, start a book intent
        if (Utility.hasInternetConnection(getActivity()))
        {
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, ean);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
            AddBook.this.restartLoader();

        }
        else
        {
            if (toast != null)
                toast.cancel();
            toast = Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private void restartLoader()
    {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (ean.getText().length() == 0)
        {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978"))
        {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(getActivity(), AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)), null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data)
    {
        if (data == null || !data.moveToFirst())
        {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        vBookTitle.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        vBookSubtitle.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null)//make sure authors exists before splitting
        {
            String[] authorsArr = authors.split(",");
            vAuthors.setLines(authorsArr.length);
            vAuthors.setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches())
        {
            new DownloadImage(vBookCover).execute(imgUrl);
            vBookCover.setVisibility(View.VISIBLE);
        }


        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        vCategories.setText(categories);

        vSaveBook.setVisibility(View.VISIBLE);
        vDeleteBook.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader)
    {

    }

    private void clearFields()
    {
        vBookTitle.setText("");
        vBookSubtitle.setText("");
        vAuthors.setText("");
        vCategories.setText("");
        vBookCover.setVisibility(View.INVISIBLE);
        vSaveBook.setVisibility(View.INVISIBLE);
        vDeleteBook.setVisibility(View.INVISIBLE);
    }

    public void onActivityResult(int reqC, int rc, Intent i)
    {
        if (Activity.RESULT_OK == rc && reqC == ISBN_RESULT)
            ean.setText(i.getStringExtra(ISBN_SCAN));
    }

    @Override
    public int getTitleResource()
    {
        return TITLE;
    }

    @Override
    public int getNavigationResource()
    {
        return R.id.nav_add_book;
    }
}
