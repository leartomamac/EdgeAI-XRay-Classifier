package com.example.edge_ai_classifier;

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {


    public static abstract class UiState {

        public static class Idle extends UiState {}
        public static class Loading extends UiState {}
        public static class Success extends UiState {
            public final String prediction;
            public final int    confidence;
            public final boolean isPositive;
            public final int    probPneumonia;
            public final int    probNormal;
            public final long   inferenceTimeMs;
            public final String modelSize;
            public final long   memoryUsedMb;
            public final long   memoryAvailableMb;
            public final int    memoryUsedPercent;

            public Success(String prediction, int confidence,boolean isPositive,int probPneumonia,int probNormal,
                           long inferenceTimeMs,String modelSize,long memoryUsedMb,long memoryAvailableMb,int memoryUsedPercent) {
                this.prediction         = prediction;
                this.confidence         = confidence;
                this.isPositive         = isPositive;
                this.probPneumonia      = probPneumonia;
                this.probNormal         = probNormal;
                this.inferenceTimeMs    = inferenceTimeMs;
                this.modelSize          = modelSize;
                this.memoryUsedMb       = memoryUsedMb;
                this.memoryAvailableMb  = memoryAvailableMb;
                this.memoryUsedPercent  = memoryUsedPercent;
            }
        }

        public static class Error extends UiState {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }

    // LiveData
    private final MutableLiveData<Uri>     selectedImageUri = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState          = new MutableLiveData<>(new UiState.Idle());
    public LiveData<Uri>     getSelectedImageUri() { return selectedImageUri; }
    public LiveData<UiState> getUiState()          { return uiState; }

    // Public API
    public void onImageSelected(Uri uri) {
        selectedImageUri.setValue(uri);
        classifyImage();
    }

    // Private helpers
    private void classifyImage() {
        uiState.setValue(new UiState.Loading());

        long startTime = System.currentTimeMillis();

        // Simulate processing delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            long inferenceTime = System.currentTimeMillis() - startTime;

            // Simulated classification result
            int probPneumonia = 96;
            int probNormal    = 100 - probPneumonia;

            // Model is always 4.8 MB (TFLite asset — fixed)
            String modelSize = "4.8 MB";

            // Read real device memory via ActivityManager
            // NOTE: pass application context from Application class or
            //       store it in ViewModel constructor if needed.
            // Here we use placeholder values; wire up real context as shown below.
            long   memUsedMb      = 128;   // placeholder — replace with readMemoryUsed()
            long   memAvailableMb = 512;   // placeholder — replace with readMemoryAvailable()
            int    memPercent     = (int) ((memUsedMb * 100L) / (memUsedMb + memAvailableMb));

            UiState.Success result = new UiState.Success("Pneumonia",probPneumonia,true,probPneumonia, probNormal,inferenceTime,
                    modelSize,memUsedMb,memAvailableMb,memPercent
            );
            uiState.setValue(result);
        }, 1500);
    }

    public static long readMemoryUsedMb() {
        Runtime rt = Runtime.getRuntime();
        long usedBytes = rt.totalMemory() - rt.freeMemory();
        return usedBytes / (1024 * 1024);
    }

    public static long readMemoryAvailableMb(Context context) {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.getMemoryInfo(info);
            return info.availMem / (1024 * 1024);
        }
        return 0;
    }
}