package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.checklist.CheckListAdapter;
import com.droidplanner.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListSysLink;
import com.droidplanner.checklist.CheckListXmlParser;
import com.droidplanner.checklist.xml.ListXmlParser.OnXmlParserError;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.InfoListner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ChecklistFragment extends Fragment implements OnXmlParserError,
		OnCheckListItemUpdateListener, InfoListner {
	private Context context;
	private Drone drone;
	private View view;
	private ExpandableListView expListView;
	private List<String> listDataHeader;
	private List<CheckListItem> checklistItems;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private LayoutInflater inflater;
	private CheckListSysLink sysLink;

	public ChecklistFragment() {
		// TODO Auto-generated constructor stub
	}

	// Load checklist from file
	private void loadXMLChecklist() {
		if (context == null)
			return;

		CheckListXmlParser xml = new CheckListXmlParser("checklist_ext.xml",
				context, R.xml.checklist_default);

		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checklistItems = xml.getCheckListItems();
	}

	// create hash list
	private void prepareListData() {
		listDataChild = new HashMap<String, List<CheckListItem>>();
		List<CheckListItem> cli;

		for (int h = 0; h < listDataHeader.size(); h++) {
			cli = new ArrayList<CheckListItem>();
			for (int i = 0; i < checklistItems.size(); i++) {
				CheckListItem c = checklistItems.get(i);
				if (c.getCategoryIndex() == h)
					cli.add(c);
			}
			listDataChild.put(listDataHeader.get(h), cli);
		}
	}

	// create listAdapter
	private void createListAdapter() {
		if (drone == null || inflater == null || listDataHeader == null
				|| listDataChild == null)
			return;

		if (listAdapter != null) {
			listAdapter = null;
		}

		listAdapter = new CheckListAdapter(drone, inflater, listDataHeader,
				listDataChild);

		listAdapter.setHeaderLayout(R.layout.list_group_header);
		listAdapter.setOnCheckListItemUpdateListener(this);
		expListView.setAdapter(listAdapter);

		listViewAutoExpand(true,true);
	}

	private void listViewAutoExpand(boolean autoExpand, boolean autoCollapse) {
		boolean allVerified;
		for (int h = 0; h < listDataHeader.size(); h++) {
			allVerified = listAdapter.areAllVerified(h);
			if (!allVerified && autoExpand)
				expListView.expandGroup(h);
			else if (allVerified && autoCollapse)
				expListView.collapseGroup(h);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.fragment_checklist, null);
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity;
		this.drone = ((SuperActivity) activity).drone;
		sysLink = new CheckListSysLink(drone);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadXMLChecklist();
		prepareListData();
		createListAdapter();
	}

	@Override
	public void onPause() {
		if(drone!=null){
			drone.removeInfoListener(this);
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		if(drone!=null){
			drone.addInfoListener(this);
		}
		super.onResume();
	}

	@Override
	public void onError(XmlPullParser parser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRowItemChanged(CheckListItem checkListItem, String mSysTag,
			boolean isChecked) {
		sysLink.setSystemData(checkListItem);
		listAdapter.notifyDataSetChanged();
		listViewAutoExpand(false,true);
	}

	@Override
	public void onRowItemGetData(CheckListItem checkListItem, String mSysTag) {
		sysLink.getSystemData(checkListItem, mSysTag);
	}

	@Override
	public void onInfoUpdate() {
		Log.d("CHKLST", "checklistItems - " + String.valueOf(checklistItems.size()));
		for(CheckListItem item : checklistItems){
			if(item.getSys_tag()!=null)
				sysLink.getSystemData(item, item.getSys_tag());
		}
		if(listAdapter!=null)
			listAdapter.notifyDataSetChanged();
	}

}