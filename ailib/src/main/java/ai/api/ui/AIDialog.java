/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.api.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.R;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class AIDialog {

    private static final String TAG = AIDialog.class.getName();

    private final Context context;
    private final AIConfiguration config;

    private AIDialogListener resultsListener;
    /*private final Dialog dialog;*/
    private final AIButton aiButton;
    private final TextView partialResultsTextView;

    private final Handler handler;
    private WindowManager wm;
    private View customView;
    private WindowManager.LayoutParams params;

    public interface AIDialogListener {
        void onResult(final AIResponse result);
        void onError(final AIError error);
        void onCancelled();
    }

    public AIDialog(final Context context, final AIConfiguration config) {
        this(context, config, R.layout.aidialog);
    }

    public AIDialog(final Context context, final AIConfiguration config, final int customLayout) {
        this.context = context;
        this.config = config;

        handler = new Handler(Looper.getMainLooper());

        customView = View.inflate(context, R.layout.aidialog, null);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        partialResultsTextView = (TextView) customView.findViewById(R.id.partialResultsTextView);


        aiButton = (AIButton) customView.findViewById(R.id.micButton);
        aiButton.initialize(config);
        setAIButtonCallback(aiButton);
    }

    public void setResultsListener(final AIDialogListener resultsListener) {
        this.resultsListener = resultsListener;
    }

    public Dialog getDialog() {
        return /*dialog*/null;
    }

    public void showAndListen() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                wm.addView(customView, params);
                ifViewAdded = true;
                startListening();
            }
        });
    }

    public AIResponse textRequest(final AIRequest request) throws AIServiceException {
        return aiButton.textRequest(request);
    }

    public AIResponse textRequest(final String request) throws AIServiceException {
        return textRequest(new AIRequest(request));
    }


    private void setAIButtonCallback(final AIButton aiButton) {
        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(final AIResponse result) {

                AIDialog.this.close();

                if (resultsListener != null) {
                    resultsListener.onResult(result);
                }
            }

            @Override
            public void onError(final AIError error) {
                if (resultsListener != null) {
                    resultsListener.onError(error);
                }
            }

            @Override
            public void onCancelled() {

                AIDialog.this.close();

                if (resultsListener != null) {
                    resultsListener.onCancelled();
                }
            }
        });

        aiButton.setPartialResultsListener(new PartialResultsListener() {
            @Override
            public void onPartialResults(final List<String> partialResults) {
                final String result = partialResults.get(0);
                if (!TextUtils.isEmpty(result)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (partialResultsTextView != null) {
                                partialResultsTextView.setText(result);
                                ViewGroup.LayoutParams params = customView.getLayoutParams();
                                wm.updateViewLayout(customView, params);
                            }
                        }
                    });
                }
            }
        });

    }

    private void startListening() {
        if (aiButton != null) {
            aiButton.startListening();
        }
    }

    boolean ifViewAdded;

    public void close() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(ifViewAdded) {
                    wm.removeViewImmediate(customView);
                    ifViewAdded = false;
                }
            }
        });
    }

    /**
     * Get AIService object for making different data requests
     * @return
     */
    public AIService getAIService() {
        return aiButton.getAIService();
    }

    /**
     * Disconnect aiDialog from the recognition service.
     * Use pause/resume methods when you have permanent reference to the AIDialog object in your Activity.
     * pause() call should be added to the onPause() method of the Activity.
     * resume() call should be added to the onResume() method of the Activity.
     */
    public void pause() {
        if (aiButton != null) {
            aiButton.pause();
        }
    }

    /**
     * Reconnect aiDialog to the recognition service.
     * Use pause/resume methods when you have permanent reference to the AIDialog object in your Activity.
     * pause() call should be added to the onPause() method of the Activity.
     * resume() call should be added to the onResume() method of the Activity.
     */
    public void resume() {
        if (aiButton != null) {
            aiButton.resume();
        }
    }

    public Handler getHandler()
    {
        return handler;
    }
}
