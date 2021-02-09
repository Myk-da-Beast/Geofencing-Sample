package com.myk.vance_gimbal_submission;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.myk.vance_gimbal_submission.data.model.GibberishRemoteDataModel;

public class GibberishDomainModel {
    private String text;

    public GibberishDomainModel() {
    }

    public GibberishDomainModel(@NonNull GibberishRemoteDataModel remoteDataModel) {
        // convert html output to string for display
        this.text = HtmlCompat.fromHtml(
                remoteDataModel.getTextOut(),
                HtmlCompat.FROM_HTML_MODE_COMPACT
        ).toString();
    }

    public String getText() {
        return text;
    }
}
