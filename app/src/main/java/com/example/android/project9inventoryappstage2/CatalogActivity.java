package com.example.android.project9inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.project9inventoryappstage2.data.BookContract;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int BOOK_LOADER = 0;

    private BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        ListView bookListView = findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = findViewById(R.id.empty_view);

        // Setup an adapter to create a list item for each row of book data in the Cursor.
        // There is no book data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);

        bookListView.setAdapter(mCursorAdapter);
        bookListView.setEmptyView(emptyView);

        // Setup item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link Editor Activity.class}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.project9inventoryappstage2/books/2"
                // if the book with ID 2 was clicked on.
                Uri uri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(uri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }


    /**
     * Helper method to insert hardcoded book data into the database. For debugging purposes only.
     */
    private void insertBook() {

        // Create a ContentValues object where column names are the keys,
        // and "The BFG"'s book attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_BOOK_TITLE, "The BFG");
        values.put(BookContract.BookEntry.COLUMN_BOOK_AUTHOR, "Roald Dahl");
        values.put(BookContract.BookEntry.COLUMN_BOOK_PRICE, 20);
        values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, 100);
        values.put(BookContract.BookEntry.COLUMN_BOOK_SUPPLIER, "Editorial ART");
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE, "0212240130");

        // Insert a new row for BFG into the provider using the ContentResolver.
        // Use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        // into the books database table.
        // Receive the new content URI that will allow us to access BFG's data in the future.
        getContentResolver().insert(BookContract.BookEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all books in the database.
     */
    private void deleteAllBooks() {
        getContentResolver().delete(BookContract.BookEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg) {
        switch (id) {
            case BOOK_LOADER:
                //Define a projection that specifies the column from the table we care about.
                String[] projection = {
                        BookContract.BookEntry._ID,
                        BookContract.BookEntry.COLUMN_BOOK_TITLE,
                        BookContract.BookEntry.COLUMN_BOOK_AUTHOR,
                        BookContract.BookEntry.COLUMN_BOOK_PRICE,
                        BookContract.BookEntry.COLUMN_BOOK_QUANTITY};
                //This loader will execute the ContentProvider's query method on a background thread
                return new CursorLoader(this,   // Parent activity context
                        BookContract.BookEntry.CONTENT_URI,     // Provider content URI to query
                        projection,                             // Columns to include in the resulting Cursor
                        null,                          // No selection clause
                        null,                       // No Selection arguments
                        null);                         // Default sort order
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}