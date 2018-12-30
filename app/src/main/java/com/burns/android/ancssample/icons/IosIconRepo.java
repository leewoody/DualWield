package com.burns.android.ancssample.icons;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.burns.android.ancssample.IOSNotification;
import com.burns.android.ancssample.ANCSParser;
import com.burns.android.ancssample.R;

public class IosIconRepo {

    private final Context context;

    public IosIconRepo(Context context) {
        this.context = context;
    }

    public Drawable getIcon(IOSNotification notif) {
        return context.getResources().getDrawable(getResourceIdForCategoryIcon(notif), null);
    }

    public int getResourceIdForCategoryIcon(IOSNotification notification) {
        switch (notification.category) {
            case ANCSParser.CategoryIDIncomingCall:
            case ANCSParser.CategoryIDMissedCall:
                return R.drawable.ic_call_black;
            case ANCSParser.CategoryIDVoicemail:
                return R.drawable.ic_voicemail_black;
            case ANCSParser.CategoryIDSocial:
                return R.drawable.ic_message_black;
            case ANCSParser.CategoryIDSchedule:
                return R.drawable.ic_event_black;
            case ANCSParser.CategoryIDEmail:
                return R.drawable.ic_email_black;
            case ANCSParser.CategoryIDNews:
                return R.drawable.ic_public_black;
            case ANCSParser.CategoryIDHealthAndFitness:
                return R.drawable.ic_fitness_center_black;
            case ANCSParser.CategoryIDBusinessAndFinance:
                return R.drawable.ic_show_chart_black;
            case ANCSParser.CategoryIDLocation:
                return R.drawable.ic_map_black;
            case ANCSParser.CategoryIDEntertainment:
                return R.drawable.ic_live_tv_black;
            case ANCSParser.CategoryIDOther:
                // fall-through!
            default:
                return R.drawable.ic_notifications_black;
        }
    }
}
