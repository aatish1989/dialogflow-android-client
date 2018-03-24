package ai.api.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIDialog;

import static android.media.CamcorderProfile.get;

/**
 * Created by aatishmittal on 23/03/18.
 */

public class AIDialogueUtils {
    private static final String TAG = "AIDialogueUtils";
    private static Gson gson = GsonFactory.getGson();


    private static void startService(Class<?> cls, Context context, String action) {
        final Intent intent = new Intent(context, cls);
        intent.putExtra(OverlayShowingService.Extras.ACTION, action);
        context.startService(intent);
    }
    public static void openVoiceInputDialog(final Context context)
    {
        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.Google);

        final AIDialog aiDialog = new AIDialog(context.getApplicationContext(), config);
        aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
            @Override
            public void onResult(final AIResponse result) {
                aiDialog.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                startService(OverlayShowingService.class, context, "processed");
                        try {
                            JSONObject json = new JSONObject(gson.toJson(result));
                            JSONArray messages = json.getJSONObject("result").getJSONObject("fulfillment").getJSONArray("messages");

                            if(messages.length() > 1)
                            {
                                JSONObject payload = ((JSONObject)messages.get(1)).optJSONObject("payload");
                                if(payload != null)
                                {
                                    String response = payload.optString("response");
                                    if(!TextUtils.isEmpty(response))
                                    {
                                        TTS.speak(response);
                                    }

                                    String deeplink = payload.optString("deeplink");

                                    if(!TextUtils.isEmpty(deeplink)) {
                                        //chat hack
                                        if(deeplink.contains("composechat"))
                                        {
                                            Uri uri = Uri.parse(deeplink);
                                            Uri.Builder builder = uri.buildUpon();

                                            if(!TextUtils.isEmpty(payload.optString("user")))
                                            {
                                                builder.appendQueryParameter("user", payload.optString("user"));
                                            }else if(!TextUtils.isEmpty(payload.optString("user1"))){
                                                builder.appendQueryParameter("user", payload.optString("user1"));
                                            }

                                            deeplink = builder.build().toString();
                                        }

                                        Intent intent = new Intent();
                                        intent.setData(Uri.parse(deeplink));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                        return;
                                    }
                                }
                            }

                            //Small Talk
                            JSONObject fulfillment = json.getJSONObject("result").optJSONObject("fulfillment");
                            if(fulfillment != null && !TextUtils.isEmpty(fulfillment.optString("speech")))
                            {
                                String fulfillmentText = fulfillment.optString("speech");
                                TTS.speak(fulfillmentText);
                                return;
                            }


                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        onError(new AIError("No match Found"));
                    }
                });

            }

            @Override
            public void onError(AIError error) {
                Log.d(TAG, "onError "+error.getMessage());
                aiDialog.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        TTS.speak("Sorry, Could not process");
                        aiDialog.close();
                        startService(OverlayShowingService.class, context, "not_found");
                    }
                });
            }

            @Override
            public void onCancelled() {
                Log.d(TAG, "onCancelled");
                aiDialog.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        TTS.speak("Cancelling");
                        aiDialog.close();
                        startService(OverlayShowingService.class, context, "not_found");
                    }
                });
            }
        });

        aiDialog.showAndListen();
    }
}
