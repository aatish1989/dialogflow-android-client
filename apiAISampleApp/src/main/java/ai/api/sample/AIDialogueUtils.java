package ai.api.sample;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIDialog;

/**
 * Created by aatishmittal on 23/03/18.
 */

public class AIDialogueUtils {
    private static final String TAG = "AIDialogueUtils";
    private static Gson gson = GsonFactory.getGson();

    public static void openVoiceInputDialog(Context context)
    {
        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Google);

        AIDialog aiDialog = new AIDialog(context.getApplicationContext(), config);
        aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
            @Override
            public void onResult(AIResponse result) {
                Log.d(TAG, "onResult "+gson.toJson(result));
            }

            @Override
            public void onError(AIError error) {
                Log.d(TAG, "onError "+error.getMessage());
            }

            @Override
            public void onCancelled() {
                Log.d(TAG, "onCancelled");
            }
        });

        aiDialog.showAndListen();
    }
}
