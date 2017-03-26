package xyz.digzdigital.cunavigator.graphhopper;

import android.os.AsyncTask;

/**
 * Created by Digz on 25/03/2017.
 */

public abstract class GHAsyncTask<A, B, C> extends AsyncTask<A, B, C>{
    private Throwable error;

    protected abstract C saveDoInBackground(A... params) throws Exception;

    protected C doInBackground(A... params) {
        try {
            return saveDoInBackground(params);
        } catch (Throwable t) {
            error = t;
            return null;
        }
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorMessage() {
        if (hasError()) {
            return error.getMessage();
        }
        return "No Error";
    }
}
