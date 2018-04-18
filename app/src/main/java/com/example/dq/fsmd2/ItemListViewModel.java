package com.example.dq.fsmd2;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

public class ItemListViewModel extends AndroidViewModel {

    private final LiveData<List<Item>> itemList;
    private LoggerDatabase loggerDatabase;

    public ItemListViewModel(Application application) {
        super(application);

        this.loggerDatabase = LoggerDatabase.getLoggerDatabase(this.getApplication());

        this.itemList = loggerDatabase.itemModel().getAllItems();
    }

    public LiveData<Item> getItem(int itemID) { return loggerDatabase.itemModel().getItem(itemID); }
    public LiveData<List<Item>> getItemList() {
        return itemList;
    }

    public void addItem(Item item) {
        new InsertAsyncTask(loggerDatabase).execute(item);
    }

    public void removeItem(Item item) {
        new RemoveAsyncTask(loggerDatabase).execute(item);
    }

    public void updateItem(Item item) {
        new UpdateAsyncTask(loggerDatabase).execute(item);
    }

    public int getNumItems() {
        return loggerDatabase.itemModel().getNumItems();
    }

    public int getNumItemsWithStatus(int status) {
        return loggerDatabase.itemModel().getNumItemsWithStatus(status);
    }

    public Integer itemWithIpExists(String ip) {
        return loggerDatabase.itemModel().itemIpInUse(ip);
    }

    private static class InsertAsyncTask extends AsyncTask<Item, Void, Void> {

        private LoggerDatabase loggerDatabase;

        InsertAsyncTask(LoggerDatabase loggerDatabase) {
            this.loggerDatabase = loggerDatabase;
        }

        @Override
        protected Void doInBackground(final Item... params) {
            loggerDatabase.itemModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Item, Void, Void> {

        private LoggerDatabase loggerDatabase;

        RemoveAsyncTask(LoggerDatabase loggerDatabase) {
            this.loggerDatabase = loggerDatabase;
        }

        @Override
        protected Void doInBackground(final Item... params) {
            loggerDatabase.itemModel().delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Item, Void, Void> {

        private LoggerDatabase loggerDatabase;

        UpdateAsyncTask(LoggerDatabase loggerDatabase) {
            this.loggerDatabase = loggerDatabase;
        }

        @Override
        protected Void doInBackground(final Item... params) {
            loggerDatabase.itemModel().update(params[0]);
            return null;
        }
    }
}
