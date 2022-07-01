package com.example.soilcontentanalyzer;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.soilcontentanalyzer.adapters.ExpandableListAdapter;
import com.example.soilcontentanalyzer.utility.NetworkChangeListener;

public class InstructionsActivity extends Activity {

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);

        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.green_black));
        }
        // get the listview
        expandableListView = findViewById(R.id.instructions_list);
        // preparing list data
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        expandableListView.setAdapter(listAdapter);
        // Listview Group click listener
        expandableListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });
        // Listview Group expanded listener
        expandableListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });
        // Listview Group collasped listener
        expandableListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });
        // Listview on child click listener
        expandableListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }
    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",Activity.MODE_PRIVATE);
        String language = sharedPreferences.getString("My_Lang","");
        int isSinhala = 0;
        if (language != null && language.equals("si")) {
            isSinhala = 1;
        }

        List<String> instruction1, instruction2, instruction3, instruction4, instruction5, instruction6, instruction7, instruction8, instruction9, instruction10, instruction11;

        if (isSinhala == 0) {
            // Adding header data
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_1));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_2));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_3));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_4));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_5));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_6));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_7));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_8));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_9));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_10));
            listDataHeader.add(getResources().getString(R.string.instructions_activity_header_11));

            // Adding child data
            instruction1 = new ArrayList<>();
            instruction1.add(getResources().getString(R.string.instructions_activity_header_1_child_data));
            instruction2 = new ArrayList<>();
            instruction2.add(getResources().getString(R.string.instructions_activity_header_2_child_data));
            instruction3 = new ArrayList<>();
            instruction3.add(getResources().getString(R.string.instructions_activity_header_3_child_data));
            instruction4 = new ArrayList<>();
            instruction4.add(getResources().getString(R.string.instructions_activity_header_4_child_data));
            instruction5 = new ArrayList<>();
            instruction5.add(getResources().getString(R.string.instructions_activity_header_5_child_data));
            instruction6 = new ArrayList<>();
            instruction6.add(getResources().getString(R.string.instructions_activity_header_6_child_data));
            instruction7 = new ArrayList<>();
            instruction7.add(getResources().getString(R.string.instructions_activity_header_7_child_data));
            instruction8 = new ArrayList<>();
            instruction8.add(getResources().getString(R.string.instructions_activity_header_8_child_data));
            instruction9 = new ArrayList<>();
            instruction9.add(getResources().getString(R.string.instructions_activity_header_9_child_data));
            instruction10 = new ArrayList<>();
            instruction10.add(getResources().getString(R.string.instructions_activity_header_10_child_data));
            instruction11 = new ArrayList<>();
            instruction11.add(getResources().getString(R.string.instructions_activity_header_11_child_data));
        } else {
            // Adding header data
            listDataHeader.add("ස්මාර්ට් NPK යෙදුම වෙත උපාංගය සම්බන්ධ කරන්නේ කෙසේද?");
            listDataHeader.add("NPK යන්නෙන් අදහස් කරන්නේ කුමක්ද? එය වැදගත් ඇයි?");
            listDataHeader.add("ක්ෂේත්ර සිතියම නිර්මාණය කරන්නේ කෙසේද?");
            listDataHeader.add("ක්ෂේත්රයේ ස්ථානයක NPK අගයන් මනින්නේ කෙසේද?");
            listDataHeader.add("උපාංගය මගින් මනිනු ලබන දත්ත වලට කුමක් සිදුවේද?");
            listDataHeader.add("මිනුම් සඳහා වාර්තාව බලන්නේ කෙසේද?");
            listDataHeader.add("මට ස්ථාන කීයක් මිනුම් ගත හැකිද?");
            listDataHeader.add("ක්ෂේත්ර සිතියම සහ සියලු මිනුම් ඉවත් කරන්නේ කෙසේද?");
            listDataHeader.add("පෞද්ගලිකත්වය");
            listDataHeader.add("ඔබ අපගේ යෙදුමට කැමතිද?");
            listDataHeader.add("යෙදුම තුළ අසාමාන්ය දෙයක් දුටුවහොත් මා කුමක් කළ යුතුද?");

            // Adding child data
            instruction1 = new ArrayList<>();
            instruction1.add("පහළ සංචාලන තීරුව භාවිතයෙන් උපාංග කොටස වෙත යන්න. ඔබට බ්ලූටූත් සක්රිය කර උපාංගයට සම්බන්ධ වීමට බ්ලූටූත් වෙත සම්බන්ධ කරන්න ක්ලික් කළ හැකිය.");
            instruction2 = new ArrayList<>();
            instruction2.add("NPK යනු ශාකයකට අවශ්ය ඉතා වැදගත් පෝෂ්ය පදාර්ථ තුනක් සඳහා කෙටි ආකාරයකි. එනම් නයිට්රජන්, පොස්පරස් සහ පොටෑසියම් ය. හොඳ ඵල ඇති ඕනෑම ශාකයක් වගා කිරීමට එම ශාකයේ NPK මට්ටම් අවශ්ය ප්රමාණය සපුරාලිය යුතුය.");
            instruction3 = new ArrayList<>();
            instruction3.add("ඔබට ක්ෂේත්ර සිතියම නිර්මාණය කිරීමට අවශ්ය ක්ෂේත්රයට යන්න.මුලින්ම ඔබගේ ඇන්ඩ්රොයිඩ් දුරකථනයේ ස්ථාන විශේෂාංගය ක්රියාත්මක කරන්න.එසේම ඔබට ජංගම දත්ත/wifi ක්රියාත්මක කිරීමට සිදුවේ.ඔබ පළමු වරට Smart NPK මීටරය භාවිතා කරන්නේ නම්, එය භාවිත ස්ථාන විශේෂාංග සඳහා ඔබෙන් අවසර අසනු ඇත. පසුව, ඔබට පහළ සංචාලන තීරුවේ ඇති සිතියම් කොටස වෙත යා යුතුය. ආරම්භයේදී එය ඔබගේ වත්මන් ස්ථානය පෙන්වයි. රේඛාවක් ඇඳීමට, ඔබට වෙනත් ස්ථානයකට ගොස් 'නැවතුම එක් කරන්න' ක්ලික් කළ හැක.මෙම පියවර කිහිප වතාවක් දිගටම කරගෙන යන්න, එවිට ඔබට අවශ්ය ක්ෂේත්ර සිතියම ලැබෙනු ඇත.");
            instruction4 = new ArrayList<>();
            instruction4.add("බ්ලූටූත් හරහා ස්මාර්ට් NPK මීටරය වෙත උපාංගය සම්බන්ධ කිරීමෙන් පසු, ඔබට පහළ සංචාලන තීරුවේ ඇති 'සිතියම' කොටසේ ඇති මිනුම් බොත්තම ක්ලික් කළ හැක. එය උපාංගයෙන් මිනුම් ඉල්ලා සිටින අතර උපාංගය NPK ප්රතිඵල ආපසු එවනු ඇත.");
            instruction5 = new ArrayList<>();
            instruction5.add("ක්ෂේත්ර සිතියම සහ NPK මිනුම් දත්ත සුරැකීම සඳහා ඔබ පහළ සංචාලන තීරුවේ ඇති 'සිතියම' කොටසේ සුරකින්න බොත්තම ක්ලික් කළ යුතුය. සුරකින්න බොත්තම ක්ලික් කිරීමෙන් පසු දත්ත දත්ත සමුදාය දිගටම පවතිනු ඇත. ඔබ සුරකින්න බොත්තම ක්ලික් නොකරන්නේ නම්, දත්ත ගබඩා නොවන අතර යෙදුම වැසීමෙන් පසු ඒවා ඉවත් කරනු ලැබේ.");
            instruction6 = new ArrayList<>();
            instruction6.add("ක්ෂේත්ර සිතියම නිර්මාණය කර මිනුම් ස්ථාන එකතු කිරීමෙන් පසුව, ඔබට NPK මිනුම් වාර්තාව බැලීම සඳහා පහළ සංචාලන තීරුවේ ඇති 'වාර්තාව' කොටස ක්ලික් කළ හැක.");
            instruction7 = new ArrayList<>();
            instruction7.add("මිනුම් ගැනීම සඳහා ස්ථාන ගණන සඳහා සීමාවක් නොමැත.");
            instruction8 = new ArrayList<>();
            instruction8.add("ක්ෂේත්ර සිතියම සහ මිනුම් ඇතුළු සියලු දත්ත පිරිසිදු කිරීමට ඔබට සියල්ල clear බොත්තම භාවිත කළ හැක.");
            instruction9 = new ArrayList<>();
            instruction9.add("100% පුද්ගලික. පුද්ගලික දත්ත එකතු කිරීමක් නැත. තෙවන පාර්ශවයන් සමඟ දත්ත බෙදාගැනීමක් නොමැත.");
            instruction10 = new ArrayList<>();
            instruction10.add("PlayStore හි Smart NPK මීටරය ශ්රේණිගත කිරීමට අමතක නොකරන්න.");
            instruction11 = new ArrayList<>();
            instruction11.add("PlayStore හි Review Section භාවිතයෙන් ගැටලුව අපට පැවසීමට කාරුණික වන්න.");
        }

        listDataChild.put(listDataHeader.get(0), instruction1);
        listDataChild.put(listDataHeader.get(1), instruction2);
        listDataChild.put(listDataHeader.get(2), instruction3);
        listDataChild.put(listDataHeader.get(3), instruction4);
        listDataChild.put(listDataHeader.get(4), instruction5);
        listDataChild.put(listDataHeader.get(5), instruction6);
        listDataChild.put(listDataHeader.get(6), instruction7);
        listDataChild.put(listDataHeader.get(7), instruction8);
        listDataChild.put(listDataHeader.get(8), instruction9);
        listDataChild.put(listDataHeader.get(9), instruction10);
        listDataChild.put(listDataHeader.get(10), instruction11);
    }
}
