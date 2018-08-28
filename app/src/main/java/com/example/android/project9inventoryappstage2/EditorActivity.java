package com.example.android.project9inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.project9inventoryappstage2.data.BookContract;
import com.example.android.project9inventoryappstage2.data.BookDbHelper;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 1;

    /**
     * EditText field to enter the book's title
     */
    private EditText mTitleEditText;

    /**
     * EditText field to enter the book's author
     */
    private EditText mAuthorEditText;

    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the book quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the book's supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the supplier's phone
     */
    private EditText mPhoneEditText;

    private BookDbHelper mDbHelper;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if (mCurrentBookUri != null) {
            mCurrentBookUri = intent.getData();
        }

        if (mCurrentBookUri != null) {
            // This is an existing book, so change app bar to say "Edit Book"
            setTitle(R.string.editor_activity_title_edit_book);
            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            invalidateOptionsMenu();
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        } else {
            setTitle(R.string.editor_activity_title_new_book);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_book_title);
        mAuthorEditText = findViewById(R.id.edit_book_author);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierEditText = findViewById(R.id.edit_book_supplier);
        mPhoneEditText = findViewById(R.id.edit_book_supplier_phone);

        ImageView addImageView = findViewById(R.id.label_add_button);
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Initialize quantity to 1 because if the quantity is empty, it will be 1 */
                int quantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                mQuantityEditText.setText(String.valueOf(quantity + 1));
                mBookHasChanged = true;
            }
        });

        ImageView removeImageView = findViewById(R.id.label_remove_button);
        removeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                /** Control to not have a quantity less than 0 */
                if (quantity - 1 >= 0) {
                    quantity--;
                }
                mQuantityEditText.setText(String.valueOf(quantity));
                mBookHasChanged = true;
            }
        });

        ImageView phoneImageView = findViewById(R.id.label_phone_button);
        phoneImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mPhoneEditText.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, "FR");
                } else {
                    phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
                }
                if (phoneNumber == null) {
                    Toast.makeText(EditorActivity.this,
                            getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel: " + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mDbHelper = new BookDbHelper(this);

        mTitleEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangeDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangeDialog(discardButtonClickListener);
    }

    private boolean validData() {
        if (TextUtils.isEmpty(mTitleEditText.getText())) {
            Toast.makeText(this, R.string.book_title_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mAuthorEditText.getText())) {
            Toast.makeText(this, R.string.book_author_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mPriceEditText.getText())
                || Integer.parseInt(mPriceEditText.getText().toString().trim()) == 0) {
            Toast.makeText(this, R.string.book_price_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mQuantityEditText.getText())
                || Integer.parseInt(mQuantityEditText.getText().toString().trim()) < 0) {
            Toast.makeText(this, R.string.book_quantity_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mSupplierEditText.getText())) {
            Toast.makeText(this, R.string.supplier_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mPhoneEditText.getText())) {
            Toast.makeText(this, R.string.supplier_phone_number_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private boolean saveBook() {

        if (!validData()) {
            return false;
        }
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        int priceString = Integer.parseInt(mPriceEditText.getText().toString().trim());
        int quantityString = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_BOOK_TITLE, titleString);
        values.put(BookContract.BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookContract.BookEntry.COLUMN_BOOK_PRICE, priceString);
        values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, quantityString);
        values.put(BookContract.BookEntry.COLUMN_BOOK_SUPPLIER, supplierString);
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE, phoneString);

        //Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri != null) {
            int bookUpdate = getContentResolver().update(mCurrentBookUri, values, null, null);
            if (bookUpdate > 0) {
                Toast.makeText(this, R.string.editor_update_book_successful, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_book_failed), Toast.LENGTH_SHORT).show();
            }

        } else {
            Uri newUri = getContentResolver().insert(BookContract.BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        int rowDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
        if (rowDeleted > 0) {
            Toast.makeText(this, R.string.editor_delete_book_successful, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                boolean saved = saveBook();
                if (saved) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangeDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_BOOK_TITLE,
                BookContract.BookEntry.COLUMN_BOOK_AUTHOR,
                BookContract.BookEntry.COLUMN_BOOK_PRICE,
                BookContract.BookEntry.COLUMN_BOOK_QUANTITY,
                BookContract.BookEntry.COLUMN_BOOK_SUPPLIER,
                BookContract.BookEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,                // Query the content URI for the current book
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    /**
     * The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {link CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            String bookTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_TITLE));
            String authorName = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_AUTHOR));
            int bookPrice = data.getInt(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRICE));
            int bookQuantity = data.getInt(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_QUANTITY));
            String supplierName = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_SUPPLIER));
            String supplierPhoneNumber = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE));

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(bookTitle);
            mAuthorEditText.setText(authorName);
            mPriceEditText.setText(Integer.toString(bookPrice));
            mQuantityEditText.setText(Integer.toString(bookQuantity));
            mSupplierEditText.setText(supplierName);
            mPhoneEditText.setText(supplierPhoneNumber);
        }
    }

    /**
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText(null);
        mAuthorEditText.setText(null);
        mPriceEditText.setText(null);
        mQuantityEditText.setText(null);
        mSupplierEditText.setText(null);
        mPhoneEditText.setText(null);
    }
}