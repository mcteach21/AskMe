package mc.apps.voice.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Abstract class ==> remplacer AsyncTask<,,> deprecated!
 * @param <TInput>
 * @param <TOutput>
 */
public abstract class MyAsyncTask<TInput, TOutput> {
    private static final String TAG = "async";

    public abstract TOutput doInBackground(TInput input);
    public abstract void onPostExecute(@Nullable TOutput result);
    public abstract void onPreExecute();
    public abstract void onProgress(int progress);


    INotifyListener listener;
    public void execute(TInput input, INotifyListener listener) {
        this.listener = listener;
        new Thread(() -> {
            onPreExecute();
            callAsync(input);
        }).start();
    }

    private CompletableFuture<TOutput> runAsync;

    private void callAsync(TInput input){
        Log.i(TAG, "Build.VERSION.SDK_INT  = "+ Build.VERSION.SDK_INT );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "CompletableFuture.runAsync..");

            /* CompletableFuture<Void> runAsync = CompletableFuture.runAsync(
                    () -> doInBackground(input)
            )*/
            runAsync = CompletableFuture.supplyAsync(() -> doInBackground(input));
            runAsync.thenRunAsync(() -> {
                try {
                    onPostExecute(runAsync.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            runAsync.whenComplete(
                    (o,t)-> {
                        try {
                            listener.notify(runAsync.get());
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            );
            //runAsync.join();
        }
    }

    public void Cancel(){
        if(runAsync!=null) {
            Log.i(TAG, "Cancel!!");
            listener.notify("Async Task Cancelled!!");
            runAsync.cancel(true);
        }
    }
    public boolean isCancelled() {
       return runAsync.isCancelled();
    };
}