package com.ivigilate.android.listeners;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ivigilate.android.AppContext;
import com.ivigilate.android.R;
import com.ivigilate.android.utils.Logger;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidget(context);
    }

    public static void updateWidget(Context context) {
        Logger.d("Started...");
        AppContext appContext = (AppContext)context.getApplicationContext();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name));
        //views.setImageViewResource(R.id.widget_button, appContext.settings.getServiceEnabled() ? R.drawable.ic_widget_service_on : R.drawable.ic_widget_service_off);

        Intent startStopServiceIntent = new Intent(context, StartStopServiceListener.class);
        startStopServiceIntent.putExtra(StartStopServiceListener.INTENT_EXTRA_TOGGLE, true);
        PendingIntent pendingStartStopServiceIntent = PendingIntent.getBroadcast(context, 0, startStopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button, pendingStartStopServiceIntent);

        ComponentName widget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widget, views);
        Logger.d("Finished.");
    }
}
