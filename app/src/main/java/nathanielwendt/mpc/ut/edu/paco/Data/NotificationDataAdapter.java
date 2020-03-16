package nathanielwendt.mpc.ut.edu.paco.Data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.NotificationsFragment;
import nathanielwendt.mpc.ut.edu.paco.R;
import nathanielwendt.mpc.ut.edu.paco.utils.notificationStore;

public class NotificationDataAdapter extends ArrayAdapter<NotificationData> {
    private final Context context;
    private final List<NotificationData> notifications;
    private NotificationsFragment.OnFragmentInteractionListener mListener;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return notifications.size();
    }

    @Override
    public NotificationData getItem(int position) {
        // TODO Auto-generated method stub
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public NotificationDataAdapter(@NonNull Context context, NotificationsFragment.OnFragmentInteractionListener mListener, List<NotificationData> notifications) {
        super(context, R.layout.notification_list_item, notifications);
        this.context = context;
        this.notifications = notifications;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) { final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//

        Log.d("LST", "inside get View");
        View rowView = inflater.inflate(R.layout.notification_list_item, parent, false);
        final TextView data = (TextView) rowView.findViewById(R.id.data);
        ImageView delete = (ImageView) rowView.findViewById(R.id.delete_not_btn);

        final NotificationData notification = notifications.get(position);
        data.setText(notification.getNotification());

        final notificationStore NotificationStore = new notificationStore((Activity) context);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                notifications.remove(position);
                NotificationStore.removeNotification(position);
                NotificationDataAdapter.this.notifyDataSetChanged();
            }
        });

        return rowView;
    }
}
