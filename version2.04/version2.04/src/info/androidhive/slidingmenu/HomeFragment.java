package info.androidhive.slidingmenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sails.engine.LocationRegion;
import com.sails.engine.MarkerManager;
import com.sails.engine.PathRoutingManager;
import com.sails.engine.PinMarkerManager;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.SAILSMapView.OnModeChangedListener;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.Marker;

import info.androidhive.slidingmenu.R;
//import com.sails.example.MainActivity.ExpandableAdapter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {

	// private Button btnMapActivity;
	static SAILS mSails;
	static SAILSMapView mSailsMapView;
	ImageView zoomin;
	ImageView zoomout;
	ImageView lockcenter;
	Spinner floorList;
	Spinner salleList;
	ArrayAdapter<String> adapter;
	ArrayAdapter<String> adapter1;
	byte zoomSav = 0;

	Button endRouteButton;
	Button pinMarkerButton;
	TextView distanceView;
	TextView currentFloorDistanceView;
	TextView msgView;

	ActionBar actionBar;
	ExpandableListView expandableListView;
	//ExpandableAdapter eAdapter;
	Vibrator mVibrator;
	Button button1;

	public HomeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		zoomin = (ImageView) rootView.findViewById(R.id.zoomin);
		zoomout = (ImageView) rootView.findViewById(R.id.zoomout);
		lockcenter = (ImageView) rootView.findViewById(R.id.lockcenter);
		floorList = (Spinner) rootView.findViewById(R.id.spinner);
		//salleList =(Spinner) rootView.findViewById(R.id.spinner1);
		zoomin.setOnClickListener(controlListener);
		zoomout.setOnClickListener(controlListener);
		lockcenter.setOnClickListener(controlListener);
		mVibrator = (Vibrator) getActivity().getSystemService(
				Service.VIBRATOR_SERVICE);
		button1 = (Button) rootView.findViewById(R.id.button1);
		expandableListView = (ExpandableListView) rootView
				.findViewById(R.id.expandableListView);
		distanceView = (TextView)rootView.findViewById(R.id.distanceView);
		distanceView.setVisibility(View.INVISIBLE);

		// localisation
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if ((mSailsMapView.getMode() & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) {
					// TODO Auto-generated method stub
					mSailsMapView.setMode(0000);// modifier la mode de la
												// localisation en normal

				} else if (((mSailsMapView.getMode() & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK)
						&& ((mSailsMapView.getMode() & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)) {
					mSailsMapView.setMode(0000);
				}
				mSailsMapView.loadFloorMap("U2.0.7 - Salle des - Examens");
				mSailsMapView.getMapViewPosition().setZoomLevel((byte) 20);
				GeoPoint poi = new GeoPoint(49.3872784, 1.0693531);
				mSailsMapView.setAnimationMoveMapTo(poi);

			}
		});

		LocationRegion.FONT_LANGUAGE = LocationRegion.NORMAL;

		mSails = new SAILS(getActivity());
		// set location mode.
		mSails.setMode(SAILS.WIFI_GFP_IMU);
		// create location change call back.

		// set floor number sort rule from descending to ascending.
		mSails.setReverseFloorList(true);
		// create location change call back.
		mSails.setOnLocationChangeEventListener(new SAILS.OnLocationChangeEventListener() {
			@Override
			public void OnLocationChange() {

				if (mSailsMapView.isCenterLock()
						&& !mSailsMapView.isInLocationFloor()
						&& !mSails.getFloor().equals("")
						&& mSails.isLocationFix()) {
					// set the map that currently location engine recognize.
					mSailsMapView.getMapViewPosition().setZoomLevel((byte) 20);
					mSailsMapView.loadCurrentLocationFloorMap();
					Toast t = Toast.makeText(getActivity(),
							mSails.getFloorDescription(mSails.getFloor()),
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

		mSails.setOnBLEPositionInitialzeCallback(10000,
				new SAILS.OnBLEPositionInitializeCallback() {
					@Override
					public void onStart() {
					}

					@Override
					public void onFixed() {

					}

					@Override
					public void onTimeOut() {
						if (!mSails.checkMode(SAILS.WIFI_GFP_IMU))
							mSails.stopLocatingEngine();
						new AlertDialog.Builder(getActivity())
								.setTitle("Positioning Timeout")
								.setMessage("Put some time out message!")
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialoginterface,
													int i) {
												mSailsMapView
														.setMode(SAILSMapView.GENERAL);
											}
										})
								.setPositiveButton("Retry",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												mSails.startLocatingEngine();
											}
										}).show();
					}
				});

		mSailsMapView = new SAILSMapView(getActivity());
		((FrameLayout) rootView.findViewById(R.id.SAILSMap))
				.addView(mSailsMapView);
		// configure SAILS map after map preparation finish.
		mSailsMapView.post(new Runnable() {
			@Override
			public void run() {
				// please change token and building id to your own building
				// project in cloud.
				mSails.loadCloudBuilding("69b66fd80ecf4561aaced3f0d1840865",
						"54c62c61d98797a814000769",
						new SAILS.OnFinishCallback() {
							@Override
							public void onSuccess(String response) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {

										mapViewInitial();
										routingInitial();

									}
								});

							}

							@Override
							public void onFailed(String response) {
								Toast t = Toast
										.makeText(
												getActivity(),
												"Load cloud project fail, please check network connection.",
												 Toast.LENGTH_SHORT);
								t.show();
							}
						});
			}
		});
		return rootView;
	}

	void mapViewInitial() {
		// establish a connection of SAILS engine into SAILS MapView.
		mSailsMapView.setSAILSEngine(mSails);

		// set location pointer icon.
		mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow,
				null, 35);

		// set location marker visible.
		mSailsMapView.setLocatorMarkerVisible(true);

		// load first floor map in package.
		mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));

		// Auto Adjust suitable map zoom level and position to best view
		// position.
		mSailsMapView.autoSetMapZoomAndView();

		// set location region click call back.
		mSailsMapView
				.setOnRegionClickListener(new SAILSMapView.OnRegionClickListener() {
					@Override
					public void onClick(List<LocationRegion> locationRegions) {
						LocationRegion lr = locationRegions.get(0);
						// begin to routing
						if (mSails.isLocationEngineStarted()) {
							// set routing start point to current user location.
							mSailsMapView.getRoutingManager().setStartRegion(
									PathRoutingManager.MY_LOCATION);

							// set routing end point marker icon.
							mSailsMapView
									.getRoutingManager()
									.setTargetMakerDrawable(
											Marker.boundCenterBottom(getResources()
													.getDrawable(
															R.drawable.destination)));

							// set routing path's color.
							mSailsMapView.getRoutingManager().getPathPaint()
									.setColor(0xFF35b3e5);

							endRouteButton.setVisibility(View.VISIBLE);
							currentFloorDistanceView
									.setVisibility(View.VISIBLE);
							msgView.setVisibility(View.VISIBLE);

						} else {
							mSailsMapView
									.getRoutingManager()
									.setTargetMakerDrawable(
											Marker.boundCenterBottom(getResources()
													.getDrawable(
															R.drawable.map_destination)));
							mSailsMapView.getRoutingManager().getPathPaint()
									.setColor(0xFF85b038);
							//if (mSailsMapView.getRoutingManager()
								//	.getStartRegion() != null)
								//endRouteButton.setVisibility(View.VISIBLE);
						}

						// set routing end point location.
						mSailsMapView.getRoutingManager().setTargetRegion(lr);

						// begin to route.
						if (mSailsMapView.getRoutingManager().enableHandler())
							distanceView.setVisibility(View.VISIBLE);
					}
				});

		mSailsMapView.getPinMarkerManager().setOnPinMarkerClickCallback(
				new PinMarkerManager.OnPinMarkerClickCallback() {
					@Override
					public void OnClick(
							MarkerManager.LocationRegionMarker locationRegionMarker) {
						Toast.makeText(
								getActivity(),
								"("
										+ Double.toString(locationRegionMarker.locationRegion
												.getCenterLatitude())
										+ ","
										+ Double.toString(locationRegionMarker.locationRegion
												.getCenterLongitude()) + ")",
								Toast.LENGTH_SHORT).show();
					}
				});

		// set location region long click call back.
		mSailsMapView
				.setOnRegionLongClickListener(new SAILSMapView.OnRegionLongClickListener() {
					@Override
					public void onLongClick(List<LocationRegion> locationRegions) {
						if (mSails.isLocationEngineStarted())
							return;

						mVibrator.vibrate(70);
						mSailsMapView.getMarkerManager().clear();
						mSailsMapView.getRoutingManager().setStartRegion(
								locationRegions.get(0));
						mSailsMapView
								.getMarkerManager()
								.setLocationRegionMarker(
										locationRegions.get(0),
										Marker.boundCenter(getResources()
												.getDrawable(
														R.drawable.start_point)));
					}
				});

		// design some action in floor change call back.
		mSailsMapView
				.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
					@Override
					public void onFloorChangedBefore(String floorName) {
						// get current map view zoom level.
						zoomSav = mSailsMapView.getMapViewPosition()
								.getZoomLevel();
					}

					@Override
					public void onFloorChangedAfter(final String floorName) {
						Runnable r = new Runnable() {
							@Override
							public void run() {
								// check is locating engine is start and current
								// brows map is in the locating floor or not.
								if (mSails.isLocationEngineStarted()
										&& mSailsMapView.isInLocationFloor()) {
									// change map view zoom level with
									// animation.
									mSailsMapView.setAnimationToZoom(zoomSav);
								}
							}
						};
						new Handler().postDelayed(r, 1000);

						int position = 0;
						for (String mS : mSails.getFloorNameList()) {
							if (mS.equals(floorName))
								break;
							position++;
						}
						floorList.setSelection(position);
					}
				});

		// design some action in mode change call back.
		mSailsMapView
				.setOnModeChangedListener(new SAILSMapView.OnModeChangedListener() {
					@Override
					public void onModeChanged(int mode) {
						if (((mode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK)
								&& ((mode & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)) {
							lockcenter.setImageDrawable(getResources()
									.getDrawable(R.drawable.center3));
						} else if ((mode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) {
							lockcenter.setImageDrawable(getResources()
									.getDrawable(R.drawable.center2));
						} else {
							lockcenter.setImageDrawable(getResources()
									.getDrawable(R.drawable.center1));
						}
					}
				});

		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, mSails.getFloorDescList());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		floorList.setAdapter(adapter);
		floorList
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (!mSailsMapView.getCurrentBrowseFloorName().equals(
								mSails.getFloorNameList().get(position)))
							mSailsMapView.loadFloorMap(mSails
									.getFloorNameList().get(position));
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
	}



	View.OnClickListener controlListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mSails.startLocatingEngine();
			mSailsMapView.setLocatorMarkerVisible(true);
			mSailsMapView.setMode(SAILSMapView.LOCATION_CENTER_LOCK
					| SAILSMapView.FOLLOW_PHONE_HEADING);
			lockcenter.setVisibility(View.VISIBLE);
			if (v == zoomin) {
				// set map zoomin function.
				mSailsMapView.zoomIn();
			} else if (v == zoomout) {
				// set map zoomout function.
				mSailsMapView.zoomOut();
			} else if (v == lockcenter) {
				if (!mSails.isLocationFix()
						|| !mSails.isLocationEngineStarted()) {
					Toast t = Toast.makeText(getActivity(),
							"Location Not Found.", Toast.LENGTH_SHORT);
					t.show();
					return;
				}
				if (!mSailsMapView.isCenterLock()
						&& !mSailsMapView.isInLocationFloor()) {
					// set the map that currently location engine recognize.
					mSailsMapView.loadCurrentLocationFloorMap();

					Toast t = Toast.makeText(getActivity(),
							"Go Back to Locating Floor First.",
							Toast.LENGTH_SHORT);
					t.show();
					return;
				}
				// set map mode.
				// FOLLOW_PHONE_HEADING: the map follows the phone's heading.
				// LOCATION_CENTER_LOCK: the map locks the current location in
				// the center of map.
				// ALWAYS_LOCK_MAP: the map will keep the mode even user moves
				// the map.
				if (mSailsMapView.isCenterLock()) {
					if ((mSailsMapView.getMode() & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)
						// if map control mode is follow phone heading, then set
						// mode to location center lock when button click.
						mSailsMapView.setMode(mSailsMapView.getMode()
								& ~SAILSMapView.FOLLOW_PHONE_HEADING);
					else
						// if map control mode is location center lock, then set
						// mode to follow phone heading when button click.
						mSailsMapView.setMode(mSailsMapView.getMode()
								| SAILSMapView.FOLLOW_PHONE_HEADING);
				} else {
					// if map control mode is none, then set mode to loction
					// center lock when button click.
					mSailsMapView.setMode(mSailsMapView.getMode()
							| SAILSMapView.LOCATION_CENTER_LOCK);
				}
			}
			/*
			 * else if (v == endRouteButton) {
			 * endRouteButton.setVisibility(View.INVISIBLE);
			 * distanceView.setVisibility(View.INVISIBLE);
			 * currentFloorDistanceView.setVisibility(View.INVISIBLE);
			 * msgView.setVisibility(View.INVISIBLE); //end route.
			 * mSailsMapView.getRoutingManager().disableHandler(); } else if (v
			 * == pinMarkerButton) { Toast.makeText(getApplication(),
			 * "Please Touch Map and Set PinMarker.",
			 * Toast.LENGTH_SHORT).show();
			 * mSailsMapView.getPinMarkerManager().setOnPinMarkerGenerateCallback
			 * (Marker.boundCenterBottom(getResources().getDrawable(R.drawable.
			 * parking_target)), new
			 * PinMarkerManager.OnPinMarkerGenerateCallback() {
			 * 
			 * @Override public void
			 * OnGenerate(MarkerManager.LocationRegionMarker
			 * locationRegionMarker) { Toast.makeText(getApplication(),
			 * "One PinMarker Generated.", Toast.LENGTH_SHORT).show(); } }); }
			 */
		}
	};
	
	
	void routingInitial() {
		mSailsMapView.getRoutingManager().setStartMakerDrawable(
				Marker.boundCenter(getResources().getDrawable(
						R.drawable.start_point)));
		mSailsMapView.getRoutingManager().setTargetMakerDrawable(
				Marker.boundCenterBottom(getResources().getDrawable(
						R.drawable.map_destination)));
		mSailsMapView.getRoutingManager().setOnRoutingUpdateListener(
				new PathRoutingManager.OnRoutingUpdateListener() {
					@Override
					public void onArrived(LocationRegion targetRegion) {
						Toast.makeText(getActivity(), "Arrive.",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onRouteSuccess() {
						List<GeoPoint> gplist = mSailsMapView
								.getRoutingManager()
								.getCurrentFloorRoutingPathNodes();
						mSailsMapView.autoSetMapZoomAndView(gplist);
					}

					@Override
					public void onRouteFail() {
						Toast.makeText(getActivity(), "Route Fail.",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onPathDrawFinish() {
					}

					@Override
					public void onTotalDistanceRefresh(int distance) {
						distanceView.setText("Total Routing Distance: "
								+ Integer.toString(distance) + " (m)");
					}

					@Override
					public void onReachNearestTransferDistanceRefresh(
							int distance, int nodeType) {
						switch (nodeType) {
						case PathRoutingManager.SwitchFloorInfo.ELEVATOR:
							currentFloorDistanceView
									.setText("To Nearest Elevator Distance: "
											+ Integer.toString(distance)
											+ " (m)");
							break;
						case PathRoutingManager.SwitchFloorInfo.ESCALATOR:
							currentFloorDistanceView
									.setText("To Nearest Escalator Distance: "
											+ Integer.toString(distance)
											+ " (m)");
							break;
						case PathRoutingManager.SwitchFloorInfo.STAIR:
							currentFloorDistanceView
									.setText("To Nearest Stair Distance: "
											+ Integer.toString(distance)
											+ " (m)");
							break;
						case PathRoutingManager.SwitchFloorInfo.DESTINATION:
							currentFloorDistanceView
									.setText("To Destination Distance: "
											+ Integer.toString(distance)
											+ " (m)");
							break;
						}
					}

					@Override
					public void onSwitchFloorInfoRefresh(
							List<PathRoutingManager.SwitchFloorInfo> infoList,
							int nearestIndex) {

						// set markers for every transfer location
						for (PathRoutingManager.SwitchFloorInfo mS : infoList) {
							if (mS.direction != PathRoutingManager.SwitchFloorInfo.GO_TARGET)
								mSailsMapView
										.getMarkerManager()
										.setLocationRegionMarker(
												mS.fromBelongsRegion,
												Marker.boundCenter(getResources()
														.getDrawable(
																R.drawable.transfer_point)));
						}

						// when location engine not turn,there is no current
						// switch floor info.
						if (nearestIndex == -1)
							return;

						PathRoutingManager.SwitchFloorInfo sf = infoList
								.get(nearestIndex);

						switch (sf.nodeType) {
						case PathRoutingManager.SwitchFloorInfo.ELEVATOR:
							if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
								msgView.setText("導航提示: \n請搭電梯上樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
								msgView.setText("導航提示: \n請搭電梯下樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							break;

						case PathRoutingManager.SwitchFloorInfo.ESCALATOR:
							if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
								msgView.setText("導航提示: \n請搭手扶梯上樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
								msgView.setText("導航提示: \n請搭手扶梯下樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							break;

						case PathRoutingManager.SwitchFloorInfo.STAIR:
							if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
								msgView.setText("導航提示: \n請走樓梯上樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
								msgView.setText("導航提示: \n請走樓梯下樓至"
										+ mSails.getFloorDescription(sf.toFloorname));
							break;

						case PathRoutingManager.SwitchFloorInfo.DESTINATION:
							msgView.setText("導航提示: \n前往"
									+ sf.fromBelongsRegion.getName());
							break;
						}
					}
				});
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		mSailsMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mSailsMapView.onPause();
	}

}
