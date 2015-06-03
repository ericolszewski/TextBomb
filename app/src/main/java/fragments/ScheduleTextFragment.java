package fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ericolszewski.smsbomb.DatabaseAdapter;
import com.ericolszewski.smsbomb.PopupActivity;
import com.ericolszewski.smsbomb.R;

import java.util.ArrayList;

import classes.Message;

/**
 * Created by ericolszewski on 5/27/15.
 */
public class ScheduleTextFragment extends Fragment {

    //region Class Variables
    private ArrayList<Message> messages;
    private View layout;
    private LayoutInflater inflater;
    private ListView messagesList;
    private DatabaseAdapter databaseAdapter;
    //endregion

    public static ScheduleTextFragment getInstance(int position) {
        ScheduleTextFragment scheduleTextFragment = new ScheduleTextFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        scheduleTextFragment.setArguments(args);
        return scheduleTextFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        layout = inflater.inflate(R.layout.fragment_schedule_text, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            databaseAdapter = new DatabaseAdapter(getActivity());
            databaseAdapter.open();

            populateMessageList();
            populateListView();
            Button addSMSButton = (Button) layout.findViewById(R.id.buttonAddText);

            addSMSButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), PopupActivity.class));
                }
            });

            messagesList = (ListView) layout.findViewById(R.id.listView);
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            ViewGroup.LayoutParams params = messagesList.getLayoutParams();
            params.height = (int)(dm.heightPixels * 0.65) - 200;
            messagesList.setLayoutParams(params);
            messagesList.requestLayout();
        }

        return layout;
    }

    private void populateMessageList() {
        messages = new ArrayList<Message>();
        Cursor cursor  =  databaseAdapter.getAllRows();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String text = cursor.getString(1);
                String date = cursor.getString(2);
                String updatedAt = cursor.getString(3);
                String recipients = cursor.getString(4);
                String frequency = cursor.getString(5);
                messages.add(new Message(id, text, date, updatedAt, recipients, frequency));
            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    private void populateListView() {
        ArrayAdapter<Message> adapter = new ListAdapter();
        ListView list = (ListView) layout.findViewById(R.id.listView);
        list.setAdapter(adapter);
    }

    private class ListAdapter extends ArrayAdapter<Message> {
        public ListAdapter() {
            super(getActivity(), R.layout.message_view, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View messageView = convertView;
            if (messageView == null) {
                messageView = inflater.inflate(R.layout.message_view, parent, false);
            }

            Message currentMessage = messages.get(position);

            TextView frequencyText = (TextView) messageView.findViewById(R.id.message_frequency);
            frequencyText.setText(String.format("Occurs Every: %s", currentMessage.getFrequency()));

            TextView recipientsText = (TextView) messageView.findViewById(R.id.message_recipients);
            recipientsText.setText(String.format("Number: %s", currentMessage.getRecipients()));

            TextView dateText = (TextView) messageView.findViewById(R.id.message_nextOccurrence);
            dateText.setText(String.format("First Occurrence: %s", currentMessage.getDate()));

            TextView messageText = (TextView) messageView.findViewById(R.id.message_message);
            messageText.setText(String.format("Message: %s", currentMessage.getText()));

            return messageView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseAdapter.close();
    }

    @Override
    public void onResume() {
        populateMessageList();
        populateListView();
        super.onResume();
    }

}