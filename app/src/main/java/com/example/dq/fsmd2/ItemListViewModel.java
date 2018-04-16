package com.example.dq.fsmd2;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
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

    public LiveData<List<Item>> getItemList() {
        return itemList;
    }

    public void addItem(Item item) {
        new InsertAsyncTask(loggerDatabase).execute(item);
    }

    public void removeYear(Item item) {
        new RemoveAsyncTask(loggerDatabase).execute(item);
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
}
