package co.aerobotics.android.proxy.mission.item.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.SQLiteDatabaseHandler;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.property.CameraProxy;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.R.id;
import co.aerobotics.android.dialogs.AddBoundaryCheckDialog;
import co.aerobotics.android.dialogs.TreeSizeDialog;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.item.adapters.CamerasAdapter;
import co.aerobotics.android.utils.unit.providers.area.AreaUnitProvider;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import co.aerobotics.android.view.spinnerWheel.adapters.NumericWheelAdapter;
import co.aerobotics.android.view.spinners.SpinnerSelfSelect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MissionSurveyFragment<T extends Survey> extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, Drone.OnMissionItemsBuiltCallback, View.OnClickListener {

    private static final String TAG = MissionSurveyFragment.class.getSimpleName();
    private static final IntentFilter eventFilter = new IntentFilter(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
    private List<Double> updated_gridlength;

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            updated_gridlength = new ArrayList<>();

            if (MissionProxy.ACTION_MISSION_PROXY_UPDATE.equals(action)) {
                for (T survey : getMissionItems()) updated_gridlength.add(survey.getGridLength());

                if(!recommendedflight) updateViews();
                else {
                    System.out.println("Survey.getgrid length on receive: " + updated_gridlength);
                    updateViewsRecommended();
                    onScrollingEnded(mAnglePicker, 0, 0);
                    recommendedflight=false;
                }
            }
        }
    };

    private final SpinnerSelfSelect.OnSpinnerItemSelectedListener cameraSpinnerListener = new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
        @Override
        public void onSpinnerItemSelected(Spinner spinner, int position) {
            if (spinner.getId() == id.cameraFileSpinner) {
                if(cameraAdapter.isEmpty())
                    return;

                CameraDetail cameraInfo = cameraAdapter.getItem(position);
                for (T survey : getMissionItems()) {
                    survey.getSurveyDetail().setCameraDetail(cameraInfo);
                    //boundaryDetail.setCamera(cameraInfo.toString());
                }

                onScrollingEnded(mAnglePicker, 0, 0);
                //dbHandler.runUpdateTask(boundaryDetail);
            }
        }
    };

    private CardWheelHorizontalView<Integer> mOverlapPicker;
    private CardWheelHorizontalView<Integer> mAnglePicker;
    private CardWheelHorizontalView<LengthUnit> mAltitudePicker;
    private CardWheelHorizontalView<Integer> mSidelapPicker;
    private CardWheelHorizontalView<Integer> mSpeedPicker;

    public TextView waypointType;
    public TextView distanceBetweenLinesTextView;
    public TextView areaTextView;
    public TextView distanceTextView;
    public TextView footprintTextView;
    public TextView groundResolutionTextView;
    public TextView numberOfPicturesView;
    public TextView numberOfStripsView;
    public TextView lengthView;
    public TextView flightTime;
    public TextView cameraTriggerTimeTextView;
    private CamerasAdapter cameraAdapter;
    private SpinnerSelfSelect cameraSpinner;
    private Button saveButton, optimizeButton, infoButton;
    private SQLiteDatabaseHandler dbHandler;
    private BoundaryDetail boundaryDetail;
    private MixpanelAPI mMixpanel;
    private RadioButton sunnyButton;
    private RadioButton cloudyButton;
    private Button goButton, cancelButton;
    private RadioGroup rg;
    private int tree_size = 2;
    private boolean recommendedflight;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_survey;
    }

    @Override
    protected List<T> getMissionItems() {
        return (List<T>) super.getMissionItems();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        mMixpanel = MixpanelAPI.getInstance(this.getApplication(), DroidPlannerApp.getInstance().getMixpanelToken());

        final View view = getView();
        final Context context = getContext();
        dbHandler = new SQLiteDatabaseHandler(context.getApplicationContext());
        boundaryDetail = new BoundaryDetail();
        waypointType = (TextView) view.findViewById(id.WaypointType);

        CameraProxy camera = getDrone().getAttribute(AttributeType.CAMERA);
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                : camera.getAvailableCameraInfos();

        saveButton = (Button) getActivity().findViewById(id.save_to_aeroview_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMixpanel.track("FPA: TapSaveMissionButton");
                new AddBoundaryCheckDialog().show(getFragmentManager(), null);
            }
        });

        optimizeButton = (Button) getActivity().findViewById(id.button_recommendSettings);
        optimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_treesize);
                dialog.setTitle("Recommend Settings");
                dialog.setCancelable(true);
                dialog.show();

                goButton = (Button) dialog.findViewById(R.id.treesize_gobutton);
                cancelButton = (Button) dialog.findViewById(id.treesize_cancelbutton);
                infoButton = (Button) dialog.findViewById(id.treesize_infobutton);
                rg = (RadioGroup) dialog.findViewById(id.treesize_radiogroup);
                ((RadioButton)rg.getChildAt(tree_size)).setChecked(true);

                goButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tree_size = rg.indexOfChild(rg.findViewById(rg.getCheckedRadioButtonId()));
                        dialog.dismiss();

                        updateViewsRecommended();
                        onScrollingEnded(mAnglePicker, 0, 0);

                        if(recommendedflight) Toast.makeText(getContext(),"Changes applied", Toast.LENGTH_SHORT).show();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                infoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(getContext());
                        dialog.setContentView(R.layout.dialog_treesize_info);
                        dialog.setTitle("Info");
                        dialog.setCancelable(true);
                        dialog.show();
                        dialog.findViewById(id.textView6).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });

        sunnyButton = (RadioButton) getActivity().findViewById(id.sunny);
        sunnyButton.setOnClickListener(this);

        cloudyButton = (RadioButton) getActivity().findViewById(id.cloudy);
        cloudyButton.setOnClickListener(this);

        updateSaveButton();
        updateWhiteBalance();

        cameraAdapter = new CamerasAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, cameraDetails);

        cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(cameraSpinnerListener);

        mAnglePicker = (CardWheelHorizontalView) view.findViewById(id.anglePicker);
        mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 359, "%dº"));

        mOverlapPicker = (CardWheelHorizontalView) view.findViewById(id.overlapPicker);
        mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 65, 99, "%d %%"));

        mSidelapPicker = (CardWheelHorizontalView) view.findViewById(id.sidelapPicker);
        mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 65, 99, "%d %%"));

        mSpeedPicker = (CardWheelHorizontalView) view.findViewById(id.speedPicker);
        mSpeedPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 4, 14, "%d m/s"));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
        mAltitudePicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE)));

        areaTextView = (TextView) view.findViewById(id.areaTextView);
        distanceBetweenLinesTextView = (TextView) view.findViewById(id.distanceBetweenLinesTextView);
        //footprintTextView = (TextView) view.findViewById(id.footprintTextView);
        groundResolutionTextView = (TextView) view.findViewById(id.groundResolutionTextView);
        distanceTextView = (TextView) view.findViewById(id.distanceTextView);
        numberOfPicturesView = (TextView) view.findViewById(id.numberOfPicturesTextView);
        numberOfStripsView = (TextView) view.findViewById(id.numberOfStripsTextView);
        lengthView = (TextView) view.findViewById(id.lengthTextView);
        flightTime = (TextView) view.findViewById(id.flightTimeTextView);
        cameraTriggerTimeTextView = (TextView) view.findViewById(id.cameraTriggerTextView);

        updateViews();
        updateCamera();

        mAnglePicker.addScrollListener(this);
        mOverlapPicker.addScrollListener(this);
        mSidelapPicker.addScrollListener(this);
        mAltitudePicker.addScrollListener(this);
        mSpeedPicker.addScrollListener(this);

        if(!getMissionItems().isEmpty()) {
            //typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));
        }

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        dbHandler.runUpdateTask(boundaryDetail);
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {
        recommendedflight = false;
    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {
        recommendedflight = false;
    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, Object startValue, Object endValue) {
        switch (cardWheel.getId()) {
            case R.id.anglePicker:
            case R.id.altitudePicker:
            case R.id.overlapPicker:
            case R.id.sidelapPicker:
            case id.speedPicker:
                final Drone drone = getDrone();
                try {
                    final List<T> surveyList = getMissionItems();
                    if (!surveyList.isEmpty()) {
                        for (final T survey : surveyList) {
                            SurveyDetail surveyDetail = survey.getSurveyDetail();
                            surveyDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            surveyDetail.setSpeed(mSpeedPicker.getCurrentValue());
                            surveyDetail.setAngle(mAnglePicker.getCurrentValue());
                            surveyDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            surveyDetail.setSidelap(mSidelapPicker.getCurrentValue());
                        }

                        getAppPrefs().persistSurveyPreferences(surveyList.get(0));
                        final MissionItem.ComplexItem<T>[] surveys = surveyList
                                .toArray(new MissionItem.ComplexItem[surveyList.size()]);

                        drone.buildMissionItemsAsync(surveys, this);

                        if(null != surveyList.get(0).getID()) {
                            boundaryDetail = dbHandler.getBoundaryDetail(surveyList.get(0).getID());
                            boundaryDetail.setBoundaryId(surveyList.get(0).getID());
                            boundaryDetail.setAngle(mAnglePicker.getCurrentValue());
                            boundaryDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            boundaryDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            boundaryDetail.setSidelap(mSidelapPicker.getCurrentValue());
                            boundaryDetail.setSpeed(mSpeedPicker.getCurrentValue());
                            boundaryDetail.setCamera(surveyList.get(0).getSurveyDetail().getCameraDetail().toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while building the survey.", e);
                }
                break;
        }
    }

    private void checkIfValid(T survey) {
        if (mAltitudePicker == null)
            return;
        if(!isAdded()){
            return;
        }
        boolean isCameraValid = checkCameraTriggerTime();
        boolean isFlightTimeValid = checkFlightTime();

        if (survey.isValid() && isCameraValid && isFlightTimeValid)
            mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
        else
            mAltitudePicker.setBackgroundColor(Color.RED);
    }

    private boolean checkFlightTime(){
        double time  = getFlightTime();
        if (time < 16f){
            flightTime.setTextColor(getResources().getColor(R.color.dark_title_bg));
            return true;
        } else
            flightTime.setTextColor(Color.RED);
            return false;
    }

    private double getFlightTime() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            double time = ( (survey.getGridLength()) / surveyDetail.getSpeed()) / 60;
            double roundedTime = Math.round((time * 2) / 2.0);
            return roundedTime;
        }
        return -1;
    }

    private boolean checkCameraTriggerTime() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);

            double cameraDistance = survey.getSurveyDetail().getLongitudinalPictureDistance();
            double triggerSpeed = cameraDistance/mSpeedPicker.getCurrentValue();

            Log.d("time", Double.toString(triggerSpeed));
            if(triggerSpeed > 2f) {
                cameraTriggerTimeTextView.setTextColor(getResources().getColor(R.color.dark_title_bg));
                return true;
            }
            else {
                cameraTriggerTimeTextView.setTextColor(Color.RED);
                return false;
            }
        }
        return false;
    }

    private double getCameraTriggerTime(){
        List<T> surveyList = getMissionItems();
        T survey = surveyList.get(0);

        double cameraDistance = survey.getSurveyDetail().getLongitudinalPictureDistance();
        return cameraDistance / mSpeedPicker.getCurrentValue();
    }

    private void updateViews() {
        if (getActivity() == null)
            return;

        updateSeekBars();
        updateTextViews();
    }

    private void updateViewsRecommended() {
        if (getActivity() == null)
            return;

        updateSeekBarsToRecommendedSettings();
        updateTextViews();
    }

    private void updateCamera() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            final int cameraSelection = cameraAdapter.getPosition(survey.getSurveyDetail().getCameraDetail());
            cameraSpinner.setSelection(Math.max(cameraSelection, 0));
        }
    }

    private void updateWhiteBalance(){
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            if (survey.getSurveyDetail().isSunny()){
                sunnyButton.setChecked(true);
            } else {
                cloudyButton.setChecked(true);
            }
        }
    }

    private void updateSaveButton(){
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            if(survey.getSurveyDetail().isSaveable()){
                saveButton.setVisibility(View.VISIBLE);
            } else {
                saveButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateSeekBars() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if (surveyDetail != null) {
                mAnglePicker.setCurrentValue((int) surveyDetail.getAngle());
                mOverlapPicker.setCurrentValue((int) surveyDetail.getOverlap());
                mSidelapPicker.setCurrentValue((int) surveyDetail.getSidelap());
                mAltitudePicker.setCurrentValue(getLengthUnitProvider().boxBaseValueToTarget(surveyDetail.getAltitude()));
                mSpeedPicker.setCurrentValue((int) surveyDetail.getSpeed());
            }
            checkIfValid(survey);
        }
    }

    private void updateSeekBarsToRecommendedSettings(){
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if (surveyDetail != null) {
                mAnglePicker.setCurrentValue((int) surveyDetail.getAngle());
                mOverlapPicker.setCurrentValue((int) surveyDetail.getOverlap());
                mSidelapPicker.setCurrentValue((int) surveyDetail.getSidelap());
                mAltitudePicker.setCurrentValue(getLengthUnitProvider().boxBaseValueToTarget( surveyDetail.getReccommendedAltitude(getRecommendedResolution())));
                //Find mean of best speed for flight time and best speed for camera trigger speed
                //Double desired_speed = (((survey.getGridLength()) / (16.1 * 60))+(survey.getSurveyDetail().getLongitudinalPictureDistance() / 2.1)) / 2;

                //Or set speed to whatever will allow it to finish in 14-15 seconds... The slowest it can afford to go...
                Double desired_speed = Math.ceil((updated_gridlength.get(0)) / (15 * 60));
                System.out.println("Survey.getgrid length (UpdateSeekBarsRecommended) = " + updated_gridlength.get(0) + "\n\n");
                System.out.println("Survey.getgrid length (UpdateSeekBarsRecommended) SPEED = " + (updated_gridlength.get(0)/(15 * 60)) + "\n\n");
                System.out.println("Survey.getgrid length (UpdateSeekBarsRecommended) SPEED CEIL = " + Math.ceil(updated_gridlength.get(0) / (15 * 60)) + "\n\n");

                mSpeedPicker.setCurrentValue(desired_speed.intValue());
                if(desired_speed > 14.0 || getCameraTriggerTime() < 2.0) {
                    Toast.makeText(getContext(), "Flight unrecommended: Mission too long.", Toast.LENGTH_SHORT).show();
                    mSpeedPicker.setCurrentValue(14);
                    recommendedflight = false;
                } else recommendedflight = true;

            }
            checkIfValid(survey);
        }
    }

    private double getRecommendedResolution(){
        int pixels = 0; double diameter = 0;

        switch(tree_size){
            case 0: pixels = 54; diameter = 250;  return 85;    //alt 70   //smallest tree < 0.5m
            case 1: pixels = 100; diameter = 1000; return 100;   //alt 76
            case 2: pixels = 135; diameter = 1500; return 123;   //alt 84
            case 3: pixels = 180; diameter = 2000; return 141;   //alt 90  //biggest trees > 2m
        }

        System.out.println("Suggested INACCURATE resolution = "  + (diameter*diameter)/(pixels*pixels) + " mm^2/px");
        return ((diameter*diameter)/(pixels*pixels));
    }

    private void updateTextViews() {
        boolean setDefault = true;
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if(survey instanceof SplineSurvey){
                waypointType.setText(getResources().getText(R.string.waypointType_Spline_Survey));
            }

            try {
                final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
                final AreaUnitProvider areaUnitProvider = getAreaUnitProvider();

                cameraTriggerTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %2.1f s", "Camera Trigger Speed", getCameraTriggerTime()));

                groundResolutionTextView.setText(String.format("%s: %s /px",
                        getString(R.string.ground_resolution),
                        areaUnitProvider.boxBaseValueToTarget(surveyDetail.getGroundResolution())));

                distanceTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_pictures),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalPictureDistance())));

                distanceBetweenLinesTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_lines),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralPictureDistance())));

                areaTextView.setText(String.format(Locale.ENGLISH, "%s: %2.1f ha", getString(R.string.area),  (survey.getPolygonArea())));


                lengthView.setText(String.format("%s: %s", getString(R.string.mission_length),
                        lengthUnitProvider.boxBaseValueToTarget(survey.getGridLength())));

                flightTime.setText(String.format(Locale.ENGLISH, "%s: %2.1f mins",
                        getString(R.string.flight_time),
                        getFlightTime()));

                numberOfPicturesView.setText(String.format(Locale.ENGLISH, "%s: %d", getString(R.string.pictures),
                        survey.getCameraCount()));

                numberOfStripsView.setText(String.format(Locale.ENGLISH, "%s: %d", getString(R.string.number_of_strips),
                        survey.getNumberOfLines()));

                setDefault = false;
            } catch (Exception e) {
                setDefault = true;
            }
        }

        if (setDefault) {
            cameraTriggerTimeTextView.setText("Camera Trigger Speed" + ": ???");
            groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": ???");
            distanceTextView.setText(getString(R.string.distance_between_pictures) + ": ???");
            distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
                    + ": ???");
            areaTextView.setText(getString(R.string.area) + ": ???");
            lengthView.setText(getString(R.string.mission_length) + ": ???");
            numberOfPicturesView.setText(getString(R.string.pictures) + "???");
            numberOfStripsView.setText(getString(R.string.number_of_strips) + "???");
            flightTime.setText(getString(R.string.flight_time) + "???");
        }
    }

    @Override
    public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
        for (MissionItem.ComplexItem<T> item : complexItems) {
            checkIfValid((T) item);
        }

        getMissionProxy().notifyMissionUpdate();
    }

    @Override
    public void onClick(View view) {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            switch (view.getId()) {
                case id.sunny:
                    surveyDetail.setSunny(true);
                    break;
                case id.cloudy:
                    surveyDetail.setSunny(false);
                    break;
                default:
                    break;
            }
        }
    }
}
