package com.example.android.project9inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project9inventoryappstage2.data.BookContract;

import java.text.NumberFormat;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the title for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView titleTextView = view.findViewById(R.id.edit_book_title);
        TextView authorTextView = view.findViewById(R.id.edit_book_author);
        TextView priceTextView = view.findViewById(R.id.edit_book_price);
        TextView quantityTextView = view.findViewById(R.id.edit_book_quantity);
        ImageView sellImageView = view.findViewById(R.id.sell_button);

        long id = cursor.getLong(cursor.getColumnIndex(BookContract.BookEntry._ID));
        String bookTitle = cursor.getString(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_TITLE));
        String bookAuthor = cursor.getString(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_AUTHOR));
        int bookPrice = cursor.getInt(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRICE));
        int bookQuantity = cursor.getInt(cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_QUANTITY));

        if (bookQuantity == 0) {
            sellImageView.setImageResource(R.drawable.remove_shopping_cart);
        } else {
            sellImageView.setImageResource(R.drawable.add_shopping_cart);
        }

        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);

        NumberFormat format = NumberFormat.getCurrencyInstance();
        priceTextView.setText(String.valueOf(format.format(bookPrice)));
        quantityTextView.setText(String.valueOf(bookQuantity));

        sellImageView.setOnClickListener(new sellOnClickListener(id, context, quantityTextView));
    }

    class sellOnClickListener implements View.OnClickListener {

        long mId;
        Context mContext;
        TextView mQuantityTextView;
        Uri mUri;

        sellOnClickListener(long id, Context context, TextView quantityTextView) {
            this.mId = id;
            this.mContext = context;
            this.mQuantityTextView = quantityTextView;
            mUri = ContentUris.withAppendedId(BookContract.BookEntry.SELL_URI, id);
        }

        @Override
        public void onClick(View v) {
            int quantity = Integer.valueOf(mQuantityTextView.getText().toString()) - 1;

            if (quantity < 0) {
                Toast.makeText(mContext, R.string.book_unavailable, Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, quantity);
            mContext.getContentResolver().update(mUri, values, null, null);
        }
    }
}