package com.jctsl.jctslonline;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TicketHistory extends AppCompatActivity {
    SwipeMenuListView mListView;
    private AppAdapter mAdapter;
    private List<TicketData> mAppList;
    SharedPreferences pref;
    String email;
    MaterialDialog dialog;
    ImageView qrcodeimg;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history);
        pref = getApplicationContext().getSharedPreferences("LoginPref", 0);
        email = pref.getString("USER_EMAIL",null);
        mListView = (SwipeMenuListView) findViewById(R.id.listView_history);
        qrcodeimg = (ImageView) findViewById(R.id.imgview_qrcode);

        FetchTicketTransactions fetch = new FetchTicketTransactions(this);
        fetch.execute(email);

    }

    private void send(TicketData[] objects) {

        mAppList = new ArrayList<>();
        for(int i=0;i<objects.length;i++) {
            mAppList.add(objects[i]);

            mListView = (SwipeMenuListView) findViewById(R.id.listView_history);

            mAdapter = new AppAdapter();
            mListView.setAdapter(mAdapter);
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    // set item width
                    openItem.setWidth(dp2px(90));
                    // set item title
                    openItem.setTitle("Open");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);

                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(dp2px(90));
                    // set a icon
                    deleteItem.setIcon(R.drawable.emailicon);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };
            // set creator
            mListView.setMenuCreator(creator);
            // Log.d("StoresListFragment",stores[i].storeName);
        }
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                final TicketData item = mAppList.get(position);
                final String qrcode_url= "http://tejasv.pythonanywhere.com"+item.qrcode;
                switch (index) {
                    case 0:
                        // open
                        boolean wrapInScrollView = true;
                        dialog  =   new MaterialDialog.Builder(TicketHistory.this)
                                .customView(R.layout.qrcodedisplay, wrapInScrollView)
                                .positiveText("Okay")
                                .build();
                        dialog.show();

                        view = dialog.getCustomView();
                        qrcodeimg = (ImageView) view.findViewById(R.id.imgview_qrcode);
                        Glide.with(TicketHistory.this).load(qrcode_url).crossFade()
                                .into(qrcodeimg);


                        break;
                    case 1:
                        //mAdapter.notifyDataSetChanged();

                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        mListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });


        // test item long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                //Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    class AppAdapter extends BaseSwipListAdapter {

        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public TicketData getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.item_list_app, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            TicketData item = getItem(position);

            //int resID = getResources().getIdentifier(aa, "drawable", getPackageName());
            //holder.iv_icon.setImageResource(resID);
            holder.busname.setText(item.busname);
            holder.srcstation.setText(item.dest);

            // /holder.qty.setText(item.quantity + "");


            return convertView;
        }

        class ViewHolder {
            TextView busname;
            TextView srcstation;

            public ViewHolder(View view) {
              //  iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                busname = (TextView) view.findViewById(R.id.textView_printbusname);
                srcstation = (TextView)view.findViewById(R.id.textView_printsrcstation);
                view.setTag(this);
            }
        }

        @Override
        public boolean getSwipEnableByPosition(int position) {
            if(position % 2 == 0){
                return false;
            }
            return true;
        }

    }


    class FetchTicketTransactions extends AsyncTask<String,String,JSONArray>
    {
        private Context context;

        public FetchTicketTransactions(Context context) {
            this.context=context;
        }

        SweetAlertDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Loading your transactions...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("email",params[0])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/fetchtickettransactions/")
                    .post(body)
                    .build();
            Response response = null;
            try {
                response= client.newCall(request).execute();
                String jsonData= response.body().string();

                try {

                    JSONArray jsonArray = new JSONArray(jsonData);
                    return jsonArray;
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Log.d("StoresListFragment","AAAAAA");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray res) {
            TicketData[] object=null;
            pDialog.dismissWithAnimation();
            super.onPostExecute(res);
            try {
                object = new TicketData[res.length()];
            }
            catch (Exception e)
            {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Something went wrong!")
                        .show();
            }

            for(int i = 0; i < object.length; i++)
            {
                object[i] = new TicketData();
            }

            for (int i = 0; i < res.length(); i++) {
                try {
                    JSONObject jsonobject = res.getJSONObject(i);

                    String datetime = jsonobject.getString("datetime");
                    String busname = jsonobject.getString("busname");
                    String srcstation = jsonobject.getString("sourcestation");
                    String deststation = jsonobject.getString("deststation");
                    String qty = jsonobject.getString("qty");
                    String totalfare = jsonobject.getString("totalfare");
                    String qrcode = jsonobject.getString("qrcode");

                    object[i] = new TicketData(datetime,busname,srcstation,deststation,qty,totalfare,qrcode);

                    // object[i].setData(storename,latitude,longitude);


                }catch (Exception e)
                {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Something went wrong!")
                            .show();
                    e.printStackTrace();
                }
            }
            send(object);


        }

    }

}

