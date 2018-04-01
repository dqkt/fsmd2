package com.example.dq.fsmd2;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout addItemManually;
    private RelativeLayout addItemWithQRCode;
    private RelativeLayout addItemWithPhoneDetect;

    private LinearLayout addItemInfoLayout;

    private AlertDialog addNewItemManuallyInfoDialog;
    private AlertDialog addNewItemWithQRCodeInfoDialog;
    private AlertDialog addNewItemWithPhoneDetectInfoDialog;

    private MenuItem deleteItem;
    private MenuItem searchItems;

    private SearchView searchItemsView;

    private Toolbar mainToolbar;
    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainDrawerToggle;

    private SwitchCompat collectingDataSwitch;

    private View summary;

    private RelativeLayout itemAreaLayout;
    private ArrayList<Item> itemList;
    private ItemRecyclerViewAdapter itemRecyclerViewAdapter;
    private SwipeRefreshLayout itemListRefreshLayout;
    private RecyclerView itemRecyclerView;
    private TextView noItemsView;
    private SwipeUtil swipeUtil;

    private DividerItemDecoration dividerItemDecoration;

    private AlertDialog.Builder addNewItemDialog;
    private EditText newItemName;
    private EditText newItemIP;

    private TextView numSafeTextView;
    private TextView numVibratingTextView;
    private TextView numLostTextView;

    private boolean isShowingContextualActionBar;

    private static final Pattern IP_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = (Toolbar) findViewById(R.id.action_bar_main);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        isShowingContextualActionBar = false;

        mainDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setUpItemsArea();
        setUpSummary();
        setUpAddItemButtons();
        setUpNavigationDrawer();

        new RequestTask().execute("http://svshizzle.com/test/getStatement.php?test=klopt", "1");

        /*
        //This gets the saved IP address, and puts it in the variable, if not set, then it starts the Setting activity.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ip = preferences.getString("IP", "NONE");
        if (ip.equals("NONE")) {//There is no IP set.
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        } else {

            //requestAll();
            //This is when the app is started, you can already retrieve data.
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        searchItems = menu.findItem(R.id.search_items);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(MainActivity.this.LAYOUT_INFLATER_SERVICE);
        searchItemsView = (SearchView) inflater.inflate(R.layout.search_items_view, null);

        searchItems.setActionView(R.layout.search_items_view);
        searchItemsView = (SearchView) searchItems.getActionView();

        searchItemsView.setIconifiedByDefault(true);
        ImageView searchItemsImageView = (ImageView) searchItemsView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchItemsImageView.setImageResource(R.drawable.ic_search_white);

        searchItemsView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchItemsView.setSubmitButtonEnabled(true);
        searchItemsView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // itemArrayAdapter.getFilter().filter(newText);

                return true;
            }
        });

        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (R.id.action_add_item):
                openAddItemManuallyPrompt();
                break;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onResume() {
        super.onResume();

        numSafeTextView = (TextView) findViewById(R.id.textview_num_safe);
        numVibratingTextView = (TextView) findViewById(R.id.textview_num_vibrating);
        numLostTextView = (TextView) findViewById(R.id.textview_num_lost);

        loadItems();
        showSummary();
        setUpItemsSwipe();
    }

    @Override
    public void onPause() {
        super.onPause();

        Item.setNumSafe(0);
        Item.setNumVibrating(0);
        Item.setNumLost(0);

        saveItems();
    }

    private void setUpItemsArea() {
        itemAreaLayout = (RelativeLayout) findViewById(R.id.view_items_area);
        itemListRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_items);
        itemRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_items);
        noItemsView = (TextView) findViewById(R.id.textview_no_items);

        int safeColor = getResources().getColor(R.color.safeStatusColor);
        int vibratingColor = getResources().getColor(R.color.vibratingStatusColor);
        int lostColor = getResources().getColor(R.color.lostStatusColor);

        itemListRefreshLayout.setColorSchemeColors(safeColor, vibratingColor, lostColor);
        itemListRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemRecyclerViewAdapter.notifyDataSetChanged();
                itemListRefreshLayout.setRefreshing(false);
            }
        });

        itemRecyclerView.bringToFront();
    }

    private void setUpAddItemButtons() {
        addItemManually = (RelativeLayout) findViewById(R.id.button_add_item_manually);
        addItemWithQRCode = (RelativeLayout) findViewById(R.id.button_add_item_with_qr_code);
        addItemWithPhoneDetect = (RelativeLayout) findViewById(R.id.button_add_item_phone_detect);

        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

        addNewItemManuallyInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
        View view = inflater.inflate(R.layout.layout_add_item_manually_info, null);
        addItemInfoLayout = (LinearLayout) view.findViewById(R.id.layout_add_item_info);
        addNewItemManuallyInfoDialog.setView(addItemInfoLayout);

        addNewItemWithQRCodeInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
        view = inflater.inflate(R.layout.layout_add_item_with_qr_code_info, null);
        addItemInfoLayout = (LinearLayout) view.findViewById(R.id.layout_add_item_info);
        addNewItemWithQRCodeInfoDialog.setView(addItemInfoLayout);

        addNewItemWithPhoneDetectInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
        view = inflater.inflate(R.layout.layout_add_item_with_phone_detect_info, null);
        addItemInfoLayout = (LinearLayout) view.findViewById(R.id.layout_add_item_info);
        addNewItemWithPhoneDetectInfoDialog.setView(addItemInfoLayout);

        addItemManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddItemManuallyPrompt();
            }
        });
        addItemManually.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                addNewItemManuallyInfoDialog.show();
                scaleAlertDialog(addNewItemManuallyInfoDialog, 0.9f, 0.4f);

                return false;
            }
        });
        addItemManually.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    addNewItemManuallyInfoDialog.dismiss();
                }
                return false;
            }
        });

        addItemWithQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        addItemWithQRCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                addNewItemWithQRCodeInfoDialog.show();
                scaleAlertDialog(addNewItemWithQRCodeInfoDialog, 0.9f, 0.4f);

                return false;
            }
        });
        addItemWithQRCode.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    addNewItemWithQRCodeInfoDialog.dismiss();
                }
                return false;
            }
        });

        addItemWithPhoneDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        addItemWithPhoneDetect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                addNewItemWithPhoneDetectInfoDialog.show();
                scaleAlertDialog(addNewItemWithPhoneDetectInfoDialog, 0.9f, 0.4f);

                return false;
            }
        });
        addItemWithPhoneDetect.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    addNewItemWithPhoneDetectInfoDialog.dismiss();

                }
                return false;
            }
        });
    }

    private void setUpNavigationDrawer() {
        mainDrawerToggle = new ActionBarDrawerToggle(this, mainDrawer, mainToolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mainDrawer.setDrawerListener(mainDrawerToggle);

        mainDrawer.post(new Runnable() {
            @Override
            public void run() {
                mainDrawerToggle.syncState();
            }
        });

        NavigationView navDrawer = (NavigationView) findViewById(R.id.nav_drawer);
        final Menu drawerMenu = navDrawer.getMenu();

        MenuItem collectingDataMenuItem = (MenuItem) drawerMenu.findItem(R.id.collecting_data);

        collectingDataSwitch = (SwitchCompat) MenuItemCompat.getActionView(collectingDataMenuItem).findViewById(R.id.switch_collecting_data);

        if (collectingDataSwitch != null) {
            collectingDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });
        }

        collectingDataMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                collectingDataSwitch.performClick();
                return true;
            }
        });

    }

    private void loadItems() {
        itemList = new ArrayList<Item>();

        SharedPreferences itemNamesFile = getSharedPreferences("ITEMS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = itemNamesFile.edit();

        int numItems = itemNamesFile.getInt("NUM ITEMS", 0);

        Scanner itemNamesScanner;

        Item item;
        String itemInfo = "";
        String itemName = "";
        String itemIP;
        int itemStatus;

        SharedPreferences itemDataFile;

        String itemData = "";
        int numMonitorData;

        Scanner itemDataScanner;

        MonitorData monitorData;
        double peakXVib, peakYVib, peakZVib, avgXVib, avgYVib, avgZVib;
        double longitude, latitude;

        Date dateAdded;
        String dateString;
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm-a-z,MMM/dd/yyyy");

        Date date;

        for (int i = 0; i < numItems; i++) {
            itemInfo = itemNamesFile.getString("ITEM " + String.valueOf(i + 1), "");
            itemNamesScanner = new Scanner(itemInfo);
            itemNamesScanner.useDelimiter("\t");

            itemName = itemNamesScanner.next();
            itemIP = itemNamesScanner.next();
            itemStatus = itemNamesScanner.nextInt();
            dateString = itemNamesScanner.next();

            try {
                dateAdded = formatter.parse(dateString);
            } catch (ParseException e) {
                dateAdded = new Date();
                e.printStackTrace();
            }

            itemDataFile = getSharedPreferences(itemName + " DATA", Context.MODE_PRIVATE);

            item = new Item(itemName, itemIP, dateAdded);
            item.setStatus(itemStatus);

            numMonitorData = itemDataFile.getInt("NUM DATA", 0);

            for (int j = 0; j < numMonitorData; j++) {
                itemData = itemDataFile.getString("DATA " + String.valueOf(j + 1), "");
                itemDataScanner = new Scanner(itemData);
                itemDataScanner.useDelimiter("\t");

                peakXVib = itemDataScanner.nextDouble();
                peakYVib = itemDataScanner.nextDouble();
                peakZVib = itemDataScanner.nextDouble();

                avgXVib = itemDataScanner.nextDouble();
                avgYVib = itemDataScanner.nextDouble();
                avgZVib = itemDataScanner.nextDouble();

                longitude = itemDataScanner.nextDouble();
                latitude = itemDataScanner.nextDouble();

                try {
                    date = formatter.parse(itemDataScanner.next());
                } catch (ParseException e) {
                    date = new Date();
                    e.printStackTrace();
                }

                monitorData = new MonitorData(peakXVib, peakYVib, peakZVib, avgXVib, avgYVib, avgZVib, longitude, latitude, date);
                item.addMonitorData(monitorData);
            }

            itemList.add(item);
        }

        itemRecyclerViewAdapter = new ItemRecyclerViewAdapter(this, itemList);
        itemRecyclerView.setAdapter(itemRecyclerViewAdapter);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemRecyclerViewAdapter.notifyDataSetChanged();

        if (dividerItemDecoration == null) {
            dividerItemDecoration = new DividerItemDecoration(itemRecyclerView.getContext(),
                    ((LinearLayoutManager) itemRecyclerView.getLayoutManager()).getOrientation());
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_items_divider));
            itemRecyclerView.addItemDecoration(dividerItemDecoration);
        }

        if (itemList.size() == 0) {
            showNoItems();
        } else {
            showItemsVisible();
            showSummary();
        }
    }

    private void saveItems() {
        SharedPreferences itemNamesFile = getSharedPreferences("ITEMS", Context.MODE_PRIVATE);
        SharedPreferences.Editor itemNamesFileEditor = itemNamesFile.edit();

        int numItems = itemList.size();
        itemNamesFileEditor.putInt("NUM ITEMS", numItems);

        Item item;
        String itemInfo = "";
        ArrayList<MonitorData> monitorDataList;
        int numMonitorData;

        SharedPreferences itemDataFile;
        SharedPreferences.Editor itemDataFileEditor;

        String data = "";

        MonitorData monitorData;

        VibrationData vibData;
        PositionData posData;
        TimeData timeData;

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm-a-z,MMM/dd/yyyy");

        Date date;

        for (int i = 0; i < numItems; i++) {
            item = itemList.get(i);
            itemInfo = item.getName() + "\t" + item.getIP() + "\t" + item.getStatus() + "\t" + formatter.format(item.getDateAdded());

            itemNamesFileEditor.putString("ITEM " + String.valueOf(i + 1), itemInfo);

            itemDataFile = getSharedPreferences(item.getName() + " DATA", Context.MODE_PRIVATE);
            itemDataFileEditor = itemDataFile.edit();

            monitorDataList = item.getMonitorDataList();
            numMonitorData = monitorDataList.size();

            itemDataFileEditor.putInt("NUM DATA", numMonitorData);

            for (int j = 0; j < numMonitorData; j++) {
                monitorData = monitorDataList.get(j);

                vibData = monitorData.getVibrationData();
                posData = monitorData.getPositionData();
                timeData = monitorData.getTimeData();

                date = timeData.getDate();

                data = vibData.getPeakXVibration() + "\t" + vibData.getPeakYVibration() + "\t" + vibData.getPeakZVibration() + "\t" +
                        vibData.getAvgXVibration() + "\t" + vibData.getAvgYVibration() + "\t" + vibData.getAvgZVibration() + "\t" +
                        posData.getLongitude() + "\t" + posData.getLatitude() + "\t" + formatter.format(date);

                itemDataFileEditor.putString("DATA " + String.valueOf(j + 1), data);
            }

            itemDataFileEditor.commit();
        }

        itemNamesFileEditor.commit();
    }

    private void setUpSummary() {
        View statusReport = findViewById(R.id.view_safe_status);
        GradientDrawable statusIndicator = (GradientDrawable) statusReport.getBackground();
        statusIndicator.setColor(getResources().getColor(R.color.safeStatusColor));

        statusReport = findViewById(R.id.view_vibrating_status);
        statusIndicator = (GradientDrawable) statusReport.getBackground();
        statusIndicator.setColor(getResources().getColor(R.color.vibratingStatusColor));

        statusReport = findViewById(R.id.view_lost_status);
        statusIndicator = (GradientDrawable) statusReport.getBackground();
        statusIndicator.setColor(getResources().getColor(R.color.lostStatusColor));
    }

    private void setUpItemsSwipe() {
        if (swipeUtil == null) {
            swipeUtil = new SwipeUtil(0, ItemTouchHelper.LEFT, this) {
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    Collections.swap(itemList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    itemRecyclerViewAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return true;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int swipedPosition = viewHolder.getAdapterPosition();
                    itemRecyclerViewAdapter.pendingRemoval(swipedPosition);
                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int position = viewHolder.getAdapterPosition();
                    if (itemRecyclerViewAdapter.isPendingRemoval(position)) {
                        return 0;
                    }
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeUtil);
            itemTouchHelper.attachToRecyclerView(itemRecyclerView);
        }
    }

    private void openAddItemManuallyPrompt() {
        addNewItemDialog = new AlertDialog.Builder(MainActivity.this);

        addNewItemDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        addNewItemDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_new_item_dialog, null);

        LinearLayout addNewItemLayout = (LinearLayout) view.findViewById(R.id.layout_adding_item_text);

        final TextInputLayout itemNameTextInput = (TextInputLayout) view.findViewById(R.id.textinput_item_name);
        itemNameTextInput.setErrorEnabled(true);
        itemNameTextInput.setHint("Name");
        newItemName = (TextInputEditText) view.findViewById(R.id.edittext_item_name);
        newItemName.setText("Item " + (itemList.size() + 1));

        final TextInputLayout itemIPTextInput = (TextInputLayout) view.findViewById(R.id.textinput_item_ip);
        itemIPTextInput.setErrorEnabled(true);
        itemIPTextInput.setHint("IP Address");
        newItemIP = (TextInputEditText) view.findViewById(R.id.edittext_item_ip);

        addNewItemDialog.setView(addNewItemLayout);

        final AlertDialog dialog = addNewItemDialog.create();

        dialog.getWindow().setLayout(150, 150);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String itemName = newItemName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                String itemIP = newItemIP.getText().toString();
                boolean valid = true;

                if (itemName.equalsIgnoreCase("")) {
                    itemNameTextInput.setError("Empty field");
                    valid = false;
                }

                if (isValidIP(itemIP) == false) {
                    itemIPTextInput.setError("Invalid IP address format");
                    valid = false;
                }

                for (Item item : itemList) {
                    if (item.getName().equalsIgnoreCase(itemName)) {
                        itemNameTextInput.setError("This item name already exists");
                        valid = false;
                        break;
                    }
                    if (item.getIP().equals(itemIP)) {
                        itemIPTextInput.setError("This IP address is already in use");
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    createNewItem(itemName, itemIP);
                    dialog.dismiss();
                }
            }
        });
    }

    private void createNewItem(String itemName, String itemIP) {
        Item newItem = new Item(itemName, itemIP);
        newItem.setStatus(Item.SAFE_STATUS);
        itemList.add(newItem);
        showItemsVisible();
        showSummary();

        SharedPreferences saveFile = getSharedPreferences("ITEMS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = saveFile.edit();

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm-a-z,MMM/dd/yyyy");

        String itemInfo = itemName + "\t" + itemIP + "\t" + newItem.getStatus() + "\t" + formatter.format(newItem.getDateAdded());

        editor.putString("ITEM " + String.valueOf(itemList.size()), itemInfo);
        editor.putInt("NUM ITEMS", itemList.size());

        editor.commit();

        /*
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this);
        notificationBuilder.setContentTitle("New item added");

        notificationBuilder.setSmallIcon(R.drawable.ic_hearing);

        notificationBuilder.setContentText("Now tracking " + itemName + ".");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(uri);
        */
    }

    class RequestTask extends AsyncTask<String, String, String> {
        private int MODE = 0; //1 == title of song , 2 == volume
        private boolean error = false; //If there was an error.
        //The actual task which requests the data from the Arduino.
        //Can be fired with: new RequestTask().execute(String url, String MODE(1 for main label, rest you can change/add));
        @Override
        protected String doInBackground(String... uri) {

            MODE = Integer.parseInt(uri[1]);    //Set the mode.

            String responseString;

            try {
                URLConnection connection = new URL(uri[0]).openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);

                }
                responseString = sb.toString();
            } catch (Exception e){
                error = true;
                responseString = e.getLocalizedMessage();
            }

            return responseString;
        }

        //After requesting the data.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            result = result.replace("HTTP/1.1 200 OKContent-type:text/html", "");
            if(!error){
                if(MODE == 0){
                    Toast.makeText(MainActivity.this, "Starting the Async went wrong", Toast.LENGTH_LONG).show();
                }
                else if(MODE == 1){

                }
                else if(MODE == 2){

                }
            }
            else{
                Toast.makeText(MainActivity.this, "Oops, something went wrong.",Toast.LENGTH_LONG).show();
            }

        }
    }

    private void showNoItems() {
        noItemsView.setVisibility(View.VISIBLE);
        itemAreaLayout.invalidate();
    }

    private void showItemsVisible() {
        noItemsView.setVisibility(View.INVISIBLE);
        itemRecyclerView.bringToFront();
        itemAreaLayout.invalidate();
    }

    public void showSummary() {
        int numSafe = Item.getNumSafe();
        int numVibrating = Item.getNumVibrating();
        int numLost = Item.getNumLost();

        numSafeTextView.setText(String.valueOf(numSafe));
        numVibratingTextView.setText(String.valueOf(numVibrating));
        numLostTextView.setText(String.valueOf(numLost));

        int numItems = itemList.size();
        String title = "Tracking " + numItems + " item";

        if (numItems == 0) {
            showNoItems();
        } else {
            showItemsVisible();
        }

        if (numItems != 1) {
            title += "s";
        }

        setTitle(title);
    }

    private void scaleAlertDialog(AlertDialog dialog, float widthScale, float heightScale) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        int dialogWindowWidth = (int) (displayWidth * widthScale);
        int dialogWindowHeight = (int) (displayHeight * heightScale);

        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;

        dialog.getWindow().setAttributes(layoutParams);
    }

    public boolean showingContextualActionBar() {
        return isShowingContextualActionBar;
    }

    public static boolean isValidIP(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }
}