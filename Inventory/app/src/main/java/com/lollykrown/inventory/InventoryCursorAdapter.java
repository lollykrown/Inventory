package com.lollykrown.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lollykrown.inventory.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView descTextView = (TextView) view.findViewById(R.id.desc);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView supplierTextView = (TextView) view.findViewById(R.id.supplier);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int descColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCR);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);

        // Read the product attributes from the Cursor for the current product
        String name = cursor.getString(nameColumnIndex);
        String desc = cursor.getString(descColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String supplier = cursor.getString(supplierColumnIndex);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(name);
        descTextView.setText(desc);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);
        supplierTextView.setText(supplier);
    }
}
