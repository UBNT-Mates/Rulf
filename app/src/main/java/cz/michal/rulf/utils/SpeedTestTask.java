package cz.michal.rulf.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import cz.michal.rulf.activities.MainActivity;
import okhttp3.ResponseBody;

public class SpeedTestTask extends AsyncTask<ResponseBody, Double, Boolean> {
    private static final String TAG = SpeedTestTask.class.getSimpleName();
    private final WeakReference<Activity> weakActivity;

    public SpeedTestTask(Activity myActivity) {
        this.weakActivity = new WeakReference<>(myActivity);
    }


    @Override
    protected Boolean doInBackground(ResponseBody... responseBodies) {
        try {
            Activity activity = weakActivity.get();
            if (activity == null || activity.isFinishing()) {
                return false;
            }

            File downloadSpeedTestFile = new File(activity.getExternalFilesDir(null) + File.separator + "DownloadSpeedTest");
            ResponseBody body = responseBodies[0];
            long startTime = System.currentTimeMillis();
            double averageSpeedInMbps = 0.0D;
            double speedInMbps = 0.0D;
            int counter = 0;

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(downloadSpeedTestFile);

                while (true) {
                    if (isCancelled() || System.currentTimeMillis() - startTime >= 15000) break;

                    int read = inputStream.read(fileReader);
                    counter += 1;

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;

                    try {
                        long timeInSecs = (System.currentTimeMillis() - startTime) / 1000;
                        speedInMbps += ((fileSizeDownloaded / timeInSecs) / 1024D) * 8;
                        averageSpeedInMbps = speedInMbps / counter;

                        if ((counter % 1000) == 0) {
                            publishProgress(averageSpeedInMbps);
                        }
                    } catch (ArithmeticException ae) {

                    }

                    Log.d(TAG, "File download: " + fileSizeDownloaded + " of " + fileSize + ". Speed: " + averageSpeedInMbps);
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
        Activity activity = weakActivity.get();
        if (activity != null || !activity.isFinishing()) {
            ((MainActivity) activity).updateDownloadSpeed(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Activity activity = weakActivity.get();
        if (activity != null || !activity.isFinishing()) {
            ((MainActivity) activity).updateButtonText();
        }
    }
}