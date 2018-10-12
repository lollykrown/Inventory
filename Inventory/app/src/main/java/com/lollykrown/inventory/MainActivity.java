package com.lollykrown.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.lollykrown.inventory.data.InventoryContract.InventoryEntry;

import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRODUCT_LOADER = 0;

    public static final String TAG = MainActivity.class.getSimpleName();

    /** Adapter for the ListView */
    InventoryCursorAdapter mCursorAdapter;

    Uri currentProductUri;

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.lollykrown.inventory", MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView lv = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        lv.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        lv.setAdapter(mCursorAdapter);

        //Check if adapter is null and set empty view
//        if (mCursorAdapter.getCount() == 0){
//            excelInsert();
//        }


        // Setup the item click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Setup the item long click listener
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                showDeleteConfirmationDialog();
                return true;
            }
        });



        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);


    }

    //method inserts one dummy product
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Samsung Galaxy");
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCR, "Mobile Phone");
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 6);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 34000);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, "Affordable Phones");

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    //Method for Deleting all products
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "excel" menu option
            case R.id.excel:
                excelInsert();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCR,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                InventoryEntry.COLUMN_PRODUCT_NAME + " ASC"); //sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


    //Confirmation dialog when delete option is selected
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the currentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.deletion_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }


    }
    public void excelInsert() {
        try {

            //get ref to the asset folder
            AssetManager am = getAssets();
            InputStream is = am.open("products.xls");
            Workbook wb = Workbook.getWorkbook(is);
            Sheet s = wb.getSheet(0);
            int row = s.getRows();
            int col = s.getColumns();

            //loop through the columns
            for(int i=1; i<5; i++) {

                String name, desc, quant, price, supp;

                Cell nameCell = s.getCell(0, i);
                name = nameCell.getContents();

                Cell descCell = s.getCell(1, i);
                desc = descCell.getContents();

                Cell quantCell = s.getCell(2, i);
                quant = quantCell.getContents();
                int quantity = Integer.parseInt(quant);

                Cell pricCell = s.getCell(3, i);
                price = pricCell.getContents();
                int pric = Integer.parseInt(price);

                Cell suppCell = s.getCell(4, i);
                supp = suppCell.getContents();

                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
                values.put(InventoryEntry.COLUMN_PRODUCT_DESCR, desc);
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, pric);
                values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, supp);

                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

        if (prefs.getBoolean("firstrun", true)){
            prefs.edit().putBoolean("firstrun", false).commit();
            excelInsert();
        }
    }

}

