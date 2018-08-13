package be.maxgaj.protripbook;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class ProtripBookWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, String[] widgetData, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.protrip_book_widget);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        Intent carIntent = new Intent(context, CarActivity.class);
        PendingIntent pendingCarIntent = PendingIntent.getActivity(context,0,carIntent,0);
        views.setOnClickPendingIntent(R.id.widget_edit_button, pendingCarIntent);

        views.setTextViewText(R.id.widget_name_value, widgetData[0]);
        views.setTextViewText(R.id.widget_ratio, widgetData[1]);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ProtripBookWidgetService.startActionReport(context);
    }

    public static void updateReportWidgets(Context context, AppWidgetManager appWidgetManager, String[] widgetData, int[] appWidgetIds){
        for (int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, widgetData, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }
}

