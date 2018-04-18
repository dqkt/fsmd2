package com.example.dq.fsmd2;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

public class MonitorDataListViewModel extends AndroidViewModel {

    private final LiveData<List<MonitorData>> monitorDataList;
    private LoggerDatabase loggerDatabase;

    public MonitorDataListViewModel(Application application) {
        super(application);

        this.loggerDatabase = LoggerDatabase.getLoggerDatabase(this.getApplication());

        this.monitorDataList = loggerDatabase.monitorDataModel().getAllMonitorDatas();
    }

    public MonitorDataListViewModel(Application application, int itemID) {
        super(application);

        this.loggerDatabase = LoggerDatabase.getLoggerDatabase(this.getApplication());

        this.monitorDataList = loggerDatabase.monitorDataModel().getAllMonitorDatasFromItem(itemID);
    }

    public LiveData<List<MonitorData>> getMonitorDataList() {
        return monitorDataList;
    }

    public void addMonitorData(MonitorData monitorData) {
        new InsertAsyncTask(loggerDatabase).execute(monitorData);
    }

    public void removeMonitorData(MonitorData monitorData) {
        new RemoveAsyncTask(loggerDatabase).execute(monitorData);
    }

    public void removeMonitorDataFromItem(int itemID) {
        loggerDatabase.monitorDataModel().deleteMonitorDatasFromItem(itemID);
    }

    private static class InsertAsyncTask extends AsyncTask<MonitorData, Void, Void> {

        private LoggerDatabase loggerDatabase;

        InsertAsyncTask(LoggerDatabase loggerDatabase) {
            this.loggerDatabase = loggerDatabase;
        }

        @Override
        protected Void doInBackground(final MonitorData... params) {
            loggerDatabase.monitorDataModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<MonitorData, Void, Void> {

        private LoggerDatabase loggerDatabase;

        RemoveAsyncTask(LoggerDatabase loggerDatabase) {
            this.loggerDatabase = loggerDatabase;
        }

        @Override
        protected Void doInBackground(final MonitorData... params) {
            loggerDatabase.monitorDataModel().delete(params[0]);
            return null;
        }
    }
}

class MonitorDataListViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private int itemID;

    public MonitorDataListViewModelFactory(Application application, int itemID) {
        this.itemID = itemID;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MonitorDataListViewModel.class)) {
            return (T) new MonitorDataListViewModel(application, itemID);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}